package com.indwealth.core.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.MenuRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import com.indwealth.core.R
import com.indwealth.core.util.data.CorePrefs
import com.indwealth.core.util.log
import com.indwealth.core.util.manager.*
import com.indwealth.core.util.track
import kotlinx.android.synthetic.main.activity_base.*
import okhttp3.HttpUrl
import timber.log.Timber

abstract class CoreActivity : AppCompatActivity(), NetworkConnectivityObserver.NetworkConnectivityChangeListener {

    open val lightNavBar = true
    open val lightStatusBar = false

    @MenuRes
    open val menu: Int? = null
    val corePrefs by lazy { object : CorePrefs(this@CoreActivity) {} }
    private var progressShowing = false
    private var cancellable = false
    private lateinit var networkConnectivityObserver: NetworkConnectivityObserver

    var keyguardHelper: KeyguardHelper? = null
    private val inAppUpdateManager by lazy { InAppUpdateManager.getInstance(this.application) }
    private val logOutListener: LogOutListener by lazy { LogOutListener(this, needLogOutListener()) }

    val uiHandler = Handler(Looper.getMainLooper())
    private var userLoggedOut = false

    abstract fun initViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        super.setContentView(R.layout.activity_base)
        if (lightStatusBar) {
            setLightStatusBar()
        } else {
            clearLightStatusBar()
        }

        iniLogOutListener()
        initFullScreenFlag()
        initKeyGuardHelper()
        initViewModel()
        initNetworkIndication()

        if (intent.hasExtra("from_notification")) {
            val title = intent.getStringExtra("notification_title") ?: ""
            track("FCM notification open", "title" to title)
        }
    }

    /**
     * Initialize the log out listener which will be triggered if user is token is reset in TokenManager
     * and needLogOutListener returns true.
     *
     * We send the user to login directly only if user was previously logged-in but session expired
     * on coming to this Activity. Else we open launcher activity and pass on the deep link to launcher
     * so that we can redirect back to same screen after log in
     */
    private fun iniLogOutListener() {
        "iniLogOutListener".log()
        logOutListener.logOutLiveData.observe(this, Observer {
            if (!it && !userLoggedOut) return@Observer
            "(!it && !userLoggedOut)".log()
            userLoggedOut = true
            if (!corePrefs.mobile.isNullOrEmpty()) { //
                "!corePrefs.mobile.isNullOrEmpty()".log()
                val url = HttpUrl.Builder()
                    .scheme("https")
                    .host(getString(R.string.deeplink_host))
                    .addPathSegment("login")

                startActivityForResult(Intent(Intent.ACTION_VIEW, Uri.parse(url.toString())).apply {
                    `package` = packageName
                }, REQUEST_USER_LOGIN_AFTER_LOGOUT)
            } else {
                "iniLogOutListener else".log()
                val intent = Intent(Intent.ACTION_MAIN).apply {
                    if (intent.action == Intent.ACTION_VIEW) { //
                        putExtra("navLink", intent.dataString)
                    }
                    addCategory(Intent.CATEGORY_LAUNCHER)
                    `package` = packageName
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                }
                startActivity(intent)
                finish()
            }
        })
    }

    private fun initFullScreenFlag() {
        if (needFullScreen()) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }
    }

    private fun initKeyGuardHelper() {
        keyguardHelper = KeyguardHelper.getInstance(application)
        keyguardHelper!!.attachActivity(this, shouldShowKeyGuard)
    }

    private fun initNetworkIndication() {
        networkConnectivityObserver = NetworkConnectivityObserver(this, this)
    }

    override fun onStart() {
        inAppUpdateManager.attachActivity(this)
        super.onStart()
        onInternetConnectivityChanged(isNetworkConnected())
    }

    override fun onStop() {
        inAppUpdateManager.detachActivity()
        super.onStop()
    }

    override fun setContentView(layoutResID: Int) {
        if (shouldOverrideContentView()) {
            val view = layoutInflater.inflate(layoutResID, baseContainerFrame, false)
            if (view != null) {
                baseContainerFrame.removeAllViews()
                baseContainerFrame.addView(view)
            }
        } else {
            super.setContentView(layoutResID)
        }
    }

    override fun setContentView(view: View?) {
        if (shouldOverrideContentView()) {
            if (view != null) {
                baseContainerFrame.removeAllViews()
                baseContainerFrame.addView(view)
            }
        } else {
            super.setContentView(view)
        }
    }

    override fun setContentView(view: View?, params: ViewGroup.LayoutParams?) {
        if (shouldOverrideContentView()) {
            if (view != null) {
                baseContainerFrame.removeAllViews()
                baseContainerFrame.addView(view)
            }
        } else {
            super.setContentView(view, params)
        }
    }

    override fun onCreateOptionsMenu(m: Menu): Boolean {
        if (menu == null) return false
        menuInflater.inflate(menu!!, m)
        return true
    }

    override fun onBackPressed() {
        if (progressShowing) {
            if (cancellable) {
                hideProgress()
            }
        } else {
            super.onBackPressed()
        }
    }

    override fun onInternetConnectivityChanged(internetAvailable: Boolean) {
        if (internetAvailable) {
            hideNoInternetView()
        } else {
            showNoInternetView()
        }
    }

    /**
     * @return true (Default) to use our custom abstract root layout,
     * false to use direct setContent View one with android default.
     */
    open fun shouldOverrideContentView(): Boolean {
        return true
    }

    /**
     * @return true (Default) to enable log out listener which would start Login screen if user is
     * logged-out because of any reason.
     */
    open fun needLogOutListener(): Boolean {
        return true
    }

    /**
     * @return true (Default) to add full screen flag and override status bar draw.
     */
    open fun needFullScreen(): Boolean {
        return true
    }

    /**
     * @return true (Default) to show key guard when this activity is resume
     * false to show directly
     */
    open var shouldShowKeyGuard = { true }

    fun go(activity: Class<out Activity>) {
        startActivity(Intent(this, activity))
    }

    fun goForResult(activity: Class<out Activity>, requestCode: Int) {
        startActivityForResult(Intent(this, activity), requestCode)
    }

    fun setLightStatusBar() {
        Timber.d("theme setLightStatusBar")
        val view: View = findViewById(android.R.id.content)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            var flags = view.systemUiVisibility
            flags = flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            view.systemUiVisibility = flags
        }
    }

    fun clearLightStatusBar() {
        Timber.d("theme clearLightStatusBar")
        val view: View = findViewById(android.R.id.content)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            var flags = view.systemUiVisibility
            flags = flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            view.systemUiVisibility = flags
        }
    }

    /**
     * Return status of internet connectivity
     *
     * @return true if connected, false otherwise
     */
    fun isNetworkConnected(): Boolean {
        return networkConnectivityObserver.isNetworkConnected()
    }

    /**
     * Show Progress Dialog
     *
     * @param message message to show
     * @param cancellable dialog cancellable
     */
    fun showProgress(message: String = "Loading...", cancellable: Boolean = false) {
        if (!shouldOverrideContentView()) return
        progressCard.isVisible = true
        progressShowing = true
        progressText.text = message
        this.cancellable = cancellable
        if (!cancellable) {
            window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        }
    }

    fun hideProgress() {
        if (!shouldOverrideContentView()) return

        progressShowing = false
        progressCard.isVisible = false
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    fun hideKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            uiHandler.postDelayed({
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            }, 200)
        }
    }

    /**
     * Show short Toast
     *
     * @param message string message
     */
    fun showShortToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    /**
     * Show short Toast
     *
     * @param message string message
     */
    fun showLongToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    /**
     * Show short Toast
     *
     * @param resId string resource id for message
     */
    fun showShortToast(@StringRes resId: Int) {
        Toast.makeText(this, resId, Toast.LENGTH_SHORT).show()
    }

    /**
     * Show short Toast
     *
     * @param resId string resource id for message
     */
    fun showLongToast(@StringRes resId: Int) {
        Toast.makeText(this, resId, Toast.LENGTH_LONG).show()
    }

    private fun showNoInternetView() {
        if (!shouldOverrideContentView()) return
        noInternetLinear.isVisible = true
        noInternetLinear.animate().alpha(1.0f)
        hideProgress()
    }

    override fun onResume() {
        super.onResume()
        inAppUpdateManager.resume()
        keyguardHelper?.attachWithoutAuthenticate(this, shouldShowKeyGuard)
    }

    override fun onDestroy() {
        keyguardHelper?.detachActivity(this)
        super.onDestroy()
    }

    private fun hideNoInternetView() {
        if (!shouldOverrideContentView()) return
        noInternetLinear.isVisible = false
        noInternetLinear.animate().alpha(0f)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (shouldShowKeyGuard()) {
            keyguardHelper!!.activityResult(requestCode, resultCode)
        }

        "onActivityResult".log()
        if (requestCode == REQUEST_USER_LOGIN_AFTER_LOGOUT) {
            hideProgress()
            if (resultCode == Activity.RESULT_OK) {
                "resultCode == Activity.RESULT_OK".log()
                userLoggedOut = false
                val lastMobile = corePrefs.mobile
                val newMobile = data?.getStringExtra("mobile")

                if (newMobile != lastMobile) { // if the user log-in with a different number then we have to send to launcher so that correct app state is maintained
                    corePrefs.mobile = newMobile
                    val intent = Intent(Intent.ACTION_MAIN).apply {
                        addCategory(Intent.CATEGORY_LAUNCHER)
                        `package` = packageName
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    startActivity(intent)
                    finish()
                } else { // hack to restart the activity, need to find a better way to do this
                    "onActivityResult else".log()
                    finish()
                    startActivity(intent)
                }
            } else {
                finishAffinity()
            }
        }
    }
}