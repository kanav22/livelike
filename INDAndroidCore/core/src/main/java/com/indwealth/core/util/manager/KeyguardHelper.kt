package com.indwealth.core.util.manager

import android.app.Activity
import android.app.Application
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner

/**
 * Helper class to enable KeyGuard in the application. It needs to be singleton as we need to maintain
 * the authenticated state across multiple activities. It needs to be attached to all the activities so
 * Base class is the best place to call attachActivity method with a boolean which decides whether Keyguard
 * needs to be shown in the respective activity. You must call activityResult to observe the result from
 * activities.
 */
class KeyguardHelper private constructor(application: Application) : LifecycleObserver {

    var identityAuthenticated = false
    private val keyguardManager: KeyguardManager = application.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
    private var activity: Activity? = null
    private var shouldShowKeyGuard: () -> Boolean = { true }
    private var lastBackGroundTime: Long = 0L

    private var isForEnableDisable = false

    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    fun attachActivity(activity: Activity, shouldShowKeyGuard: () -> Boolean) {
        this.activity = activity
        this.shouldShowKeyGuard = shouldShowKeyGuard
        authenticateApp()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onMoveToForeground() {
        // app moved to foreground
            authenticateApp()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onMoveToBackground() {
//        lastBackGroundTime = System.currentTimeMillis()
//        identityAuthenticated = false
    }

    // method to authenticate app
    fun authenticateApp() {
        if (identityAuthenticated) return
        if (!shouldShowKeyGuard()) return
        if (isForEnableDisable) return

        // Create an intent to open device screen lock screen to authenticate
        // Pass the Screen Lock screen Title and Description
        var intent = keyguardManager.createConfirmDeviceCredentialIntent("Unlock", "Confirm Pattern")
        try {
            // Start activity for result
            activity?.startActivityForResult(intent, LOCK_REQUEST_CODE)
        } catch (e: Exception) {
            // If some exception occurs means Screen lock is not set up please set screen lock
            // Open Security screen directly to enable patter lock
            intent = Intent(Settings.ACTION_SECURITY_SETTINGS)
            try {
                // Start activity for result
                activity?.startActivityForResult(intent, SECURITY_SETTING_REQUEST_CODE)
            } catch (ex: Exception) {
                // If app is unable to find any Security settings then user has to set screen lock manually
                // textView.setText(resources.getString(R.string.setting_label))
            }
        } finally {
            identityAuthenticated = false
        }
    }

    // method to authenticate app
    fun authenticateForEnableDisable() {
        isForEnableDisable = true
        // Create an intent to open device screen lock screen to authenticate
        // Pass the Screen Lock screen Title and Description
        var intent = keyguardManager.createConfirmDeviceCredentialIntent("Unlock", "Confirm Pattern")
        try {
            // Start activity for result
            activity?.startActivityForResult(intent, REQUEST_CODE_ENABLE_DISABLE_LOCK)
        } catch (e: Exception) {
            // If some exception occurs means Screen lock is not set up please set screen lock
            // Open Security screen directly to enable patter lock
            intent = Intent(Settings.ACTION_SECURITY_SETTINGS)
            try {
                // Start activity for result
                activity?.startActivityForResult(intent, SECURITY_SETTING_REQUEST_CODE)
            } catch (ex: Exception) {
                // If app is unable to find any Security settings then user has to set screen lock manually
                // textView.setText(resources.getString(R.string.setting_label))
            }
        }
    }

    fun activityResult(requestCode: Int, resultCode: Int) {
        when (requestCode) {
            LOCK_REQUEST_CODE -> if (resultCode == Activity.RESULT_OK) {
                identityAuthenticated = true
            } else {
                activity?.finishAffinity()
            }
            SECURITY_SETTING_REQUEST_CODE -> if (resultCode == Activity.RESULT_OK) {
                // authenticate app if user has setup the password
                authenticateApp()
            } else {
                // user shall pass if he doesn't setup the lock screen,
                // don't navigate the user to settings screen again
            }
            REQUEST_CODE_ENABLE_DISABLE_LOCK -> {
                isForEnableDisable = false
                identityAuthenticated = true
            }
        }
    }

    fun attachWithoutAuthenticate(activity: Activity, shouldShowKeyGuard: () -> Boolean) {
        this.activity = activity
        this.shouldShowKeyGuard = shouldShowKeyGuard
    }

    fun isScreenLockEnabled(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            keyguardManager.isDeviceSecure
        } else {
            keyguardManager.isKeyguardSecure
        }
    }

    /**
     * Clears the saved instance of the activity
     * @param activity instance of activity to detach
     */
    fun detachActivity(activity: Activity) {
        if (this.activity == activity) {
            this.activity = null
            this.shouldShowKeyGuard = { false }
        }
    }

    companion object : SingletonHolder<KeyguardHelper, Application>(::KeyguardHelper)
}

const val LOCK_REQUEST_CODE = 101
const val SECURITY_SETTING_REQUEST_CODE = 102
const val REQUEST_CODE_ENABLE_DISABLE_LOCK = 103