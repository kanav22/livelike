package com.indwealth.core.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.net.Uri
import android.os.Build
import android.util.DisplayMetrics
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.indwealth.core.BaseApplication.Companion.tokenManager
import com.indwealth.core.BuildConfig
import com.indwealth.core.R
import com.indwealth.core.ui.CoreActivity
import com.indwealth.core.util.data.CorePrefs
import com.philliphsu.bottomsheetpickers.date.DatePickerDialog
import com.whiteelephant.monthpicker.MonthPickerDialog
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern
import kotlin.math.floor
import kotlin.math.roundToInt

object CoreUtils {

    fun replaceFragment(
        activity: FragmentActivity,
        fragment: Fragment,
        @IdRes holder: Int,
        addToBackStack: Boolean = true,
        showSlideAnimation: Boolean = true,
        vararg transitions: View,
        commitNow: Boolean = false
    ) {
        val ft = activity.supportFragmentManager.beginTransaction()
        if (showSlideAnimation)
            ft.setCustomAnimations(R.animator.slide_in_left, R.animator.slide_out_right, R.animator.slide_in_right, R.animator.slide_out_left)
        ft.replace(holder, fragment, fragment.javaClass.simpleName)
        transitions.forEach { ft.addSharedElement(it, it.transitionName) }
        if (addToBackStack)
            ft.addToBackStack(fragment.javaClass.simpleName)
        if (commitNow) ft.commitNow() else ft.commit()
    }

    fun addFragment(activity: CoreActivity, fragment: Fragment, @IdRes holder: Int, hideFragment: Fragment? = null, addToBackStack: Boolean = true) {
        val ft = activity.supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.animator.slide_in_left,
                R.animator.slide_out_right, R.animator.slide_in_right, R.animator.slide_out_left)
        hideFragment?.let { ft.hide(hideFragment) }
        ft.add(holder, fragment, fragment.javaClass.simpleName)
        if (addToBackStack) ft.addToBackStack(fragment.javaClass.simpleName)
        ft.commit()
    }

    fun logout(prefs: CorePrefs) {
        prefs.clear()
        tokenManager.reset(true)
    }

    fun logout(c: Context, prefs: CorePrefs, url: String) {
        prefs.clear()
        val intent = Intent(ACTION_VIEW, Uri.parse(url))
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        c.startActivity(intent)
    }

    fun getActualGraphStartDate(monthsAgo: Int, startDate: Date?): String {
        return if (monthsAgo == -1)
            startDate?.YYYY_MM_DD() ?: ""
        else {
            val c = Calendar.getInstance()
            c.set(Calendar.MONTH, c.get(Calendar.MONTH) - monthsAgo)
            c.time.YYYY_MM_DD()
        }
    }

    fun monthsToText(months: Int): String {
        var string = ""
        if (floor((months / 12).toDouble()) > 0) {
            val years = floor((months / 12).toDouble()).toInt()
            string += if (years == 1) "$years year "
            else "$years years "
        }
        if (months % 12 > 0) {
            string += if (months == 1) "1 month"
            else "${months % 12} months"
        }
        return string
    }

    fun monthsToShortText(months: Int): String {
        var string = ""
        if (floor((months / 12).toDouble()) > 0) {
            val years = floor((months / 12).toDouble()).toInt()
            string += if (years == 1) "${years}y"
            else "${years}y"
        }
        if (months % 12 > 0) {
            string += if (months == 1) " 1m"
            else " ${months % 12}m"
        }
        return string
    }

    fun Rs(number: Number?): String {
        return if (number == null) "\u20B90" else DecimalFormat("\u20B9##,##,##,###.##").format(number)
    }

    fun roundOffTo2Places(value: Double): Float {
        val newValue = ((value * 1000.0).roundToInt().toDouble() / 1000.0)
        return String.format(Locale.ENGLISH, "%.2f", newValue).toFloat()
    }

    fun roundOffTo2Places(value: Float): String {
        return DecimalFormat("0.##").apply { roundingMode = RoundingMode.HALF_EVEN }.format(value)
    }

    fun roundOffTo4Places(value: Double): String {
        val newValue = (value * 1000.0).roundToInt().toDouble() / 1000.0
        return String.format(Locale.ENGLISH, "%.4f", newValue)
    }

    fun convertDpToPixel(dp: Float, context: Context): Float {
        val resources = context.resources
        val metrics = resources.displayMetrics
        return dp * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }

    fun hideKeyboard(c: Context) {
        val view = (c as Activity).currentFocus
        if (view != null) {
            val imm = c.getSystemService(
                Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    fun parseMillis(milliseconds: Long): String {
        return String.format("%02d:%02d:%02d",
            TimeUnit.MILLISECONDS.toHours(milliseconds),
            TimeUnit.MILLISECONDS.toMinutes(milliseconds) - TimeUnit.HOURS.toMinutes(
                TimeUnit.MILLISECONDS.toHours(milliseconds)),
            TimeUnit.MILLISECONDS.toSeconds(milliseconds) - TimeUnit.MINUTES.toSeconds(
                TimeUnit.MILLISECONDS.toMinutes(milliseconds)))
    }

//    fun getLoadingDialog(activity: Context, message: String): MaterialDialog {
//        return MaterialDialog.Builder(activity)
//                .content(message)
//                .progress(true, 100)
//                .build()
//    }

    fun isPanValid(panNumber: String): Boolean {
        var isValid = false
        val pattern = Pattern.compile("[A-Z]{5}[0-9]{4}[A-Z]")
        val matcher = pattern.matcher(panNumber.toUpperCase())
        // Check if pattern matches
        if (matcher.matches()) isValid = true

        return isValid
    }

    fun sendEmailToSupport(context: Context) {
        sendEmail(context, context.getString(R.string.support_email), "", getDeviceInfo(context))
    }

    fun sendEmail(context: Context, to: String, subject: String, body: String) {
        val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.fromParts(
            "mailto", to, null))
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
        emailIntent.putExtra(Intent.EXTRA_TEXT, body)
        context.startActivity(Intent.createChooser(emailIntent, "Send email"))
    }

    fun openGoogleDocs(link: String, activity: Activity) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://docs.google.com/viewer?url=$link"))
        activity.startActivity(browserIntent)
    }

    fun getDaysBetween(fromDate: Date?, toDate: Date?): Long {
        return if (fromDate != null && toDate != null) Math.round((toDate.time - fromDate.time) / 86400000.toDouble()) else 0
    }

    fun getDiffYears(from: Calendar, to: Calendar): Int {
        var diff = to.get(Calendar.YEAR) - from.get(Calendar.YEAR)
        if (from.get(Calendar.MONTH) > to.get(Calendar.MONTH) || from.get(Calendar.MONTH) == to.get(Calendar.MONTH) && from.get(Calendar.DATE) > to.get(Calendar.DATE))
            diff--
        return diff
    }

    fun isEmailValid(sValue: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(sValue).matches()
    }

    fun openUrlInBrowser(context: Context, url: String) {
        val webpage = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, webpage)
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        }
    }

    fun getDeviceName(): String {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL
        return if (model.toLowerCase().startsWith(manufacturer.toLowerCase())) {
            model
        } else {
            "$manufacturer $model"
        }
    }

    fun shareText(c: Context, shareText: String) {
        val sharingIntent = Intent(Intent.ACTION_SEND)
            .setType("text/plain")
            .putExtra(Intent.EXTRA_TEXT, shareText)
        c.startActivity(Intent.createChooser(sharingIntent, "Share"))
    }

    fun getDeviceInfo(context: Context): String {
        val displaymetrics = context.resources.displayMetrics
        val sb = StringBuilder()
        val height = displaymetrics.heightPixels
        val width = displaymetrics.widthPixels

        sb.append("\n\n\n" +
            "-----------------------------\nThis information will make it easier for Piggy to help me:")
            .append("\nManufacturer : ").append(Build.MANUFACTURER)
            .append("\nModel: ").append(Build.MODEL)
            .append("\nProduct: ").append(Build.PRODUCT)
            .append("\nScreen Resolution: ")
            .append(width).append(" x ").append(height).append(" pixels")
            .append("\nAndroid Version: ").append(Build.VERSION.RELEASE)
            .append("\nApp Version: ").append(BuildConfig.VERSION_NAME)
            .append(" (" + BuildConfig.VERSION_CODE + ")")
            .append("\nApp installed on: ").append(Date(context.packageManager.getPackageInfo(context.packageName, 0).firstInstallTime))
            .append("\n-----------------------------")
        return sb.toString()
    }

    fun setFragment(activity: AppCompatActivity, fragment: Fragment, @IdRes holder: Int) {
        val ft = activity.supportFragmentManager.beginTransaction()
        ft.add(holder, fragment, fragment.javaClass.simpleName)
        ft.commit()
    }

    fun showDatePicker(
        activity: FragmentActivity,
        min: Date? = null,
        max: Date? = null,
        default: Date? = null,
        callBack: (String) -> Unit
    ) {
        val now = Calendar.getInstance()

        "showing date picker $default".log
        if (default != null) {
            "showing date picker $default".log
            now.time = default
        }
        val picker = DatePickerDialog.newInstance(
            { _, year, monthOfYear, dayOfMonth ->
                callBack("${dayOfMonth.leftPad0()}/${(monthOfYear + 1).leftPad0()}/$year")
            },
            now.get(Calendar.YEAR),
            now.get(Calendar.MONTH),
            now.get(Calendar.DAY_OF_MONTH)
        )

        if (min != null) {
            val minCal = Calendar.getInstance()
            minCal.time = min
            picker.minDate = minCal
        }
        if (max != null) {
            val maxCal = Calendar.getInstance()
            maxCal.time = max
            picker.maxDate = maxCal
        }

        picker.show(activity.supportFragmentManager, activity.javaClass.simpleName)
    }

    fun showDatePicker(
        fragment: Fragment,
        min: Date? = null,
        max: Date? = null,
        default: Date? = min,
        callBack: (String) -> Unit
    ) {
        val now = Calendar.getInstance()

        "showing date picker $default".log
        if (default != null) {
            "showing date picker $default".log
            now.time = default
        }
        val picker = DatePickerDialog.newInstance(
            { _, year, monthOfYear, dayOfMonth ->
                callBack("${dayOfMonth.leftPad0()}/${(monthOfYear + 1).leftPad0()}/$year")
            },
            now.get(Calendar.YEAR),
            now.get(Calendar.MONTH),
            now.get(Calendar.DAY_OF_MONTH)
        )

        if (min != null) {
            val minCal = Calendar.getInstance()
            minCal.time = min
            picker.minDate = minCal
        }
        if (max != null) {
            val maxCal = Calendar.getInstance()
            maxCal.time = max
            picker.maxDate = maxCal
        }

        picker.show(fragment.childFragmentManager, fragment.javaClass.simpleName)
    }

    fun showDatePickerWithMax(
        activity: FragmentActivity,
        max: String? = "",
        default: String? = null,
        callBack: (String) -> Unit
    ) {
        val now = Calendar.getInstance()

        "showing date picker $default".log
        if (default != null) {
            "showing date picker ${default.toDate()}".log
            now.time = default.toDate()
        }
        val picker = DatePickerDialog.newInstance(
            { _, year, monthOfYear, dayOfMonth ->
                callBack("${dayOfMonth.leftPad0()}/${(monthOfYear + 1).leftPad0()}/$year")
            },
            now.get(Calendar.YEAR),
            now.get(Calendar.MONTH),
            now.get(Calendar.DAY_OF_MONTH)
        )
        if (max!!.isNotEmpty()) {
            val maxCal = Calendar.getInstance()

            maxCal.time = max.toDate()
            picker.maxDate = maxCal
        }

        picker.show(activity.supportFragmentManager, activity.javaClass.simpleName)
    }

    fun showMonthYearPicker(
        activity: FragmentActivity,
        month: Int? = null,
        year: Int? = null,
        futureOnly: Boolean = false,
        callBack: (Int, Int) -> Unit
    ) {
        val now = Calendar.getInstance()
        val builder = MonthPickerDialog.Builder(activity, { m: Int, y: Int ->
            callBack(m, y)
        }, year ?: now.get(Calendar.YEAR), month ?: now.get(Calendar.MONTH))
            .setMinYear(1900)
            .setMaxYear(2100)
            .setOnYearChangedListener { }

        if (futureOnly)
            builder.setMonthAndYearRange(Calendar.JANUARY, Calendar.DECEMBER, now.get(Calendar.YEAR), 2080)

        builder.build().show()
    }

    fun monthsLeftInCurrentFinancialYear(): Int {
        val tenureCalendar = Calendar.getInstance()
        if (tenureCalendar.get(Calendar.MONTH) >= Calendar.MARCH) {
            tenureCalendar.add(Calendar.YEAR, 1)
        }
        tenureCalendar.set(Calendar.MONTH, Calendar.MARCH)
        return tenureCalendar.time.howManyMonthsAwayAbsolute()
    }

    fun getScreenWidth(activity: Activity): Int {
        val displayMetrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.widthPixels
    }

    fun clearBackStack(manager: FragmentManager) {
        if (manager.backStackEntryCount > 0) {
            val first = manager.getBackStackEntryAt(0)
            manager.popBackStack(first.id, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }
    }

    fun getCurrentFragment(fragmentManager: FragmentManager): Fragment? {
        if (fragmentManager.backStackEntryCount < 1) return null

        val fragmentTag = fragmentManager.getBackStackEntryAt(fragmentManager.backStackEntryCount - 1).name
        return fragmentManager.findFragmentByTag(fragmentTag)
    }

    fun openAppPermissionPage(context: Context) {
        context.startActivity(Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:" + context.packageName)).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
    }

    fun checkDeviceHasNavigationBar(context: Context): Boolean {
        var hasNavigationBar = false
        val id = context.resources.getIdentifier("config_showNavigationBar", "bool", "android")
        if (id > 0) {
            hasNavigationBar = context.resources.getBoolean(id)
        }
        try {
            val systemPropertiesClass = Class.forName("android.os.SystemProperties")
            val m = systemPropertiesClass.getMethod("get", String::class.java)
            val navBarOverride = m.invoke(systemPropertiesClass, "qemu.hw.mainkeys") as String
            if ("1" == navBarOverride) {
                hasNavigationBar = false
            } else if ("0" == navBarOverride) {
                hasNavigationBar = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return hasNavigationBar
    }
}