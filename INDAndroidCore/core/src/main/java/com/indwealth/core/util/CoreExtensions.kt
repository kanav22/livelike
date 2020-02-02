package com.indwealth.core.util

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Handler
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.util.TypedValue
import android.view.*
import android.view.animation.Animation
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.parseAsHtml
import androidx.fragment.app.Fragment
import com.crashlytics.android.core.CrashlyticsCore
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.indwealth.core.R
import com.indwealth.core.util.view.CircleTransform
import com.indwealth.core.util.view.DebouncedOnClickListener
import com.indwealth.core.util.view.ProgressBarAnimation
import com.indwealth.core.util.view.ViewUtils
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.io.File
import java.util.*
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.random.Random
import timber.log.Timber

fun Double.toFixed(decimals: Int): String {
    if (this == 0.0) return "0"
    val factor = 10.0.pow(decimals.toDouble())
    return ((this * factor).roundToInt() / factor).toString()
}

fun Float.toFixed(decimals: Int): String {
    if (this == 0f) return "0"
    val factor = 10.0.pow(decimals.toDouble())
    return ((this * factor).roundToInt() / factor).toString()
}

fun Double.toFixedNumber(decimals: Int): Double {
    if (this == 0.0) return 0.0
    val factor = 10.0.pow(decimals.toDouble())
    return ((this * factor).roundToInt() / factor)
}

fun ProgressBar.animateProgress(to: Int) {
    ProgressBarAnimation(this, 1000).setProgress(to)
}

fun Number?.rupeeString(): String {
    if (this == null) return ""
    return CoreUtils.Rs(this)
}

fun Context.toast(text: CharSequence, duration: Int = Toast.LENGTH_SHORT) =
    Toast.makeText(this, text, duration).show()

fun Activity.toast(text: CharSequence, duration: Int = Toast.LENGTH_SHORT) =
    runOnUiThread { Toast.makeText(this, text, duration).show() }

fun Fragment.toast(text: CharSequence, duration: Int = Toast.LENGTH_SHORT) =
    Toast.makeText(this.context, text, duration).show()

fun Activity.setOnClickListeners(listener: View.OnClickListener, vararg views: View) =
    views.forEach { it.setOnClickListener(listener) }

fun Number?.prependSign(): String? {
    if (this == null) return null
    return when {
        this.toDouble() > 0 -> "▲ ${abs(this.toDouble().toFixedNumber(2))}"
        this.toDouble() == 0.0 -> "${abs(this.toDouble().toFixedNumber(2))}"
        else -> "▼ ${abs(this.toDouble().toFixedNumber(2))}"
    }
}

fun Number.getSignSymbol(): String = if (this.toDouble() > 0) "▲ " else "▼ "

fun Any?.log() = Timber.d(this.toString())
val Any.log: Unit
    get() = Timber.d(this.toString())

fun Number?.shortRupeeString(hideThousands: Boolean = true): String {
    if (this == null) return ""
    val value = toDouble() // forget decimals in short form
    val absolute = abs(value)
    val string = when {
        absolute < 1000 -> absolute.rupeeString()
        absolute < 10_000 -> // 1,000 - 9,999
            if (hideThousands) {
                if (absolute % 100.0 == 0.0)
                    RUPEE + absolute / 1_000 + "K"
                RUPEE + CoreUtils.roundOffTo2Places(absolute / 1_000f) + "K"
            } else
                value.rupeeString() // 0 - 9,999
        absolute < 1_00_000 -> {
            if (absolute % 100.0 == 0.0)
                RUPEE + absolute / 1_000 + "K"
            RUPEE + CoreUtils.roundOffTo2Places(absolute / 1_000f) + "K"
        }
        absolute < 1_00_00_000 -> {
            if (absolute % 100.0 == 0.0)
                RUPEE + absolute / 1_00_000 + "L"
            RUPEE + CoreUtils.roundOffTo2Places(absolute / 1_00_000f) + "L"
        }
        else -> {
            if (absolute % 100.0 == 0.0)
                RUPEE + absolute / 1_00_000 + "Cr"
            RUPEE + CoreUtils.roundOffTo2Places(absolute / 1_00_00_000f) + "Cr"
        }
    }
    return if (value < 0) "-$string" else string
}

fun String.shortRupeeString(hideThousands: Boolean = false): String {
    return try {
        toDouble().shortRupeeString(hideThousands)
    } catch (e: Exception) {
        ""
    }
}

fun String.removeDecimal(): String {
    if (contains(".")) {

        return replace("0L", "L")
            .replace("0Cr", "Cr")
            .replace("0k", "k")
            .replace(".L", "L")
            .replace(".Cr", "Cr")
            .replace(".k", "k")
        // return replace("\\.[0-9]0".toRegex(), "")
    }
    return this
}

fun Number.dpToPx(context: Context): Float {
    return CoreUtils.convertDpToPixel(this.toFloat(), context)
}

fun Context.getDrawableById(@DrawableRes id: Int): Drawable? {
    return ContextCompat.getDrawable(this, id)
}

fun Context.getColorById(@ColorRes id: Int): Int {
    return ContextCompat.getColor(this, id)
}

inline fun View.debouncedOnClick(debounceTill: Long = 500, crossinline onClick: (v: View) -> Unit) {
    this.setOnClickListener(object : DebouncedOnClickListener(debounceTill) {
        override fun onDebouncedClick(v: View) {
            onClick(v)
        }
    })
}

inline fun View.onClick(crossinline onClicked: (v: View?) -> Unit) {
    this.setOnClickListener { v -> onClicked(v) }
}

fun <R> (() -> R).withDelay(delay: Long = 250L) {
    Handler().postDelayed({ this.invoke() }, delay)
}

fun Int.leftPad0(): String {
    return String.format("%02d", this)
}

fun Double?.percentOrDash() = if (this != null) "${toFixed(2)}%" else "--"

inline fun EditText.onNext(crossinline onNext: () -> Unit) {
    setOnKeyListener { _, keyCode, event ->
        if (event.action == EditorInfo.IME_ACTION_SEND ||
            (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER)) {
            onNext()
            true
        } else false
    }
}

inline fun EditText.afterTextChanged(crossinline afterTextChanged: (String) -> Unit): ViewUtils.EditTextUtils.DefaultTextWatcher {
    val watcher = object : ViewUtils.EditTextUtils.DefaultTextWatcher() {
        override fun afterTextChanged(s: Editable) {
            val input = s.toString().trim()
            afterTextChanged(input)
        }
    }
    addTextChangedListener(watcher)
    return watcher
}

fun Context.openUrl(url: String) {

    try {
        val customTabsIntent = CustomTabsIntent.Builder()
            .addDefaultShareMenuItem()
            .setToolbarColor(getColorById(R.color.colorPrimary))
            .setShowTitle(true)
            .setCloseButtonIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_arrow_back_white_24dp))
            .build()
        // check if chrome available
        val packageName = CustomTabHelper().getPackageNameToUse(this, url)

        if (packageName != null) {
            customTabsIntent.intent.setPackage(packageName)
            customTabsIntent.launchUrl(this, Uri.parse(url))
        }
    } catch (e: ActivityNotFoundException) {
        e.printStackTrace()
        CrashlyticsCore.getInstance().logException(e)
        CrashlyticsCore.getInstance().log(url)
    }
}

fun Calendar.diffInMonths(endCalendar: Calendar = Calendar.getInstance()): Int {
    val diffYear = endCalendar.age()
    val diffMonth = diffYear * 12 + endCalendar.get(Calendar.MONTH) - this.get(Calendar.MONTH)
    return abs(diffMonth)
}

fun Calendar.age(endCalendar: Calendar = Calendar.getInstance()): Int {
    var diffYear = endCalendar.get(Calendar.YEAR) - this.get(Calendar.YEAR)
    if (endCalendar.get(Calendar.MONTH) > this.get(Calendar.MONTH) ||
        (endCalendar.get(Calendar.MONTH) == this.get(Calendar.MONTH) && endCalendar.get(Calendar.DATE) > this.get(Calendar.DATE))) {
        diffYear--
    }
    return diffYear
}

fun String.sizeSpannable(relativeSize: Float, start: Int, end: Int): SpannableStringBuilder {
    val ssBuilder = SpannableStringBuilder(this)
    ssBuilder.setSpan(RelativeSizeSpan(relativeSize), start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
    return ssBuilder
}

fun String.sizeAndColorSpannable(relativeSize: Float, @ColorInt colorInt: Int, start: Int, end: Int): SpannableStringBuilder {
    val ssBuilder = SpannableStringBuilder(this)
    ssBuilder.setSpan(RelativeSizeSpan(relativeSize), start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
    ssBuilder.setSpan(ForegroundColorSpan(colorInt), start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
    return ssBuilder
}

fun String.colorSpannable(@ColorInt colorInt: Int, start: Int, end: Int): SpannableStringBuilder {
    val ssBuilder = SpannableStringBuilder(this)
    ssBuilder.setSpan(ForegroundColorSpan(colorInt), start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
    return ssBuilder
}

fun String.boldSpannable(start: Int, end: Int): SpannableStringBuilder {
    val ssBuilder = SpannableStringBuilder(this)
    ssBuilder.setSpan(StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
    return ssBuilder
}

fun EditText.makeEditable(isEditable: Boolean) {
    isEnabled = isEditable
    isFocusable = isEditable
    isFocusableInTouchMode = isEditable
}

fun EditText.doneListener(onDone: () -> Boolean) {
    setOnEditorActionListener { _, actionId, _ ->

        if ((actionId == EditorInfo.IME_ACTION_DONE)) {
            return@setOnEditorActionListener onDone()
        }
        return@setOnEditorActionListener false
    }
}

var TextView.drawableEnd: Drawable?
    get() = compoundDrawablesRelative[2]
    set(value) = setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, value, null)

var TextView.drawableStart: Drawable?
    get() = compoundDrawablesRelative[0]
    set(value) = setCompoundDrawablesRelativeWithIntrinsicBounds(value, null, null, null)

var TextView.drawableBottom: Drawable?
    get() = compoundDrawablesRelative[3]
    set(value) = setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, null, value)

var TextView.drawableTop: Drawable?
    get() = compoundDrawablesRelative[1]
    set(value) = setCompoundDrawablesRelativeWithIntrinsicBounds(null, value, null, null)

/**
 * Returns string with suffix added to integer
 * e.g  1.withSuffix() returns 1st
 * e.g  2.withSuffix() returns 2nd
 * e.g  13.withSuffix() returns 13th
 * e.g  102.withSuffix() returns 102nd
 */
fun Int.withSuffix(): String {
    val suffix = arrayOf("th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th")

    return "$this" + when (this % 100) {
        11, 12, 13 -> "th"
        else -> suffix[this % 10]
    }
}

fun Double.timeLeft(months: Double = 0.0): String {
    val years = months / 12
    val remainingMonths = months % 12
    if (remainingMonths.toInt() == 0) {
        return years.toInt().toString() + "y "
    }
    return years.toInt().toString() + "y " + remainingMonths.toInt() + " m"
}

fun EditText.showKeyboard() {
    if (requestFocus()) {
        (getActivity()?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            .showSoftInput(this, InputMethodManager.SHOW_FORCED)
        setSelection(text.length)
    }
}

fun View.getActivity(): AppCompatActivity? {
    var context = this.context
    while (context is ContextWrapper) {
        if (context is AppCompatActivity) {
            return context
        }
        context = context.baseContext
    }
    return null
}

fun Activity.hideKeyBoard() {
    val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

    // check if no view has focus:
    currentFocus?.let {
        inputManager.hideSoftInputFromWindow(it.windowToken, 0)
    }
}

fun ImageView.tint(@ColorRes colorInt: Int) {
    setColorFilter(ContextCompat.getColor(context, colorInt), android.graphics.PorterDuff.Mode.MULTIPLY)
}

fun ViewGroup.inflate(layoutRes: Int): View {
    return LayoutInflater.from(context).inflate(layoutRes, this, false)
}

fun View.addRipple() = with(TypedValue()) {
    context.theme.resolveAttribute(android.R.attr.selectableItemBackground, this, true)
    setBackgroundResource(resourceId)
}

fun EditText.disableCopyPaste() {
    customSelectionActionModeCallback = object : ActionMode.Callback {
        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?) = false
        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?) = false
        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?) = false
        override fun onDestroyActionMode(mode: ActionMode?) {}
    }
    isLongClickable = false
    setTextIsSelectable(false)
}

fun String.capitalizeWords(): String = split(" ").joinToString(" ") { it.toLowerCase().capitalize() }

fun CollapsingToolbarLayout.setTypeface() {
    val typeface = ResourcesCompat.getFont(context, R.font.avenir600)
    if (typeface != null) {
        setCollapsedTitleTypeface(typeface)
        setExpandedTitleTypeface(typeface)
    }
}

inline fun Animation.doOnEnd(crossinline callback: () -> Unit) {
    setAnimationListener(object : Animation.AnimationListener {
        override fun onAnimationRepeat(animation: Animation?) {
        }

        override fun onAnimationEnd(animation: Animation?) {
            callback()
        }

        override fun onAnimationStart(animation: Animation?) {
        }
    })
}

fun MenuItem.setTitleColor(color: Int) {
    val hexColor = Integer.toHexString(color).toUpperCase().substring(2)
    val html = "<font color='#$hexColor'>$title</font>"
    this.title = html.parseAsHtml()
}

/**
 * Loads a url into imageView using picasso
 *
 * @return tag: randomly generated tag marking the request with picasso
 * Use this tag to cancel the task on view destroyed to avoid memory leaks
 */
fun ImageView.loadWithPicasso(
    url: String,
    circleTransform: Boolean = false,
    @DrawableRes errorResId: Int? = null,
    onSuccess: (() -> Unit)? = null,
    onError: (() -> Unit)? = null
): String? {

    if (url.isBlank()) {
        onError?.invoke()
        return null
    }

    val request = Picasso.get().load(url)

    if (circleTransform) {
        request.transform(CircleTransform())
    }
    if (errorResId != null) {
        request.error(errorResId)
    }

    val tag = Random.nextDouble().toString()
    request.tag(tag)
    request.into(this, object : Callback {
        override fun onError(e: java.lang.Exception?) {
            onError?.invoke()
        }

        override fun onSuccess() {
            onSuccess?.invoke()
        }
    })
    return tag
}

/**
 * Loads a url into imageView using picasso
 *
 * @return tag: randomly generated tag marking the request with picasso
 * Use this tag to cancel the task on view destroyed to avoid memory leaks
 */
fun ImageView.loadWithPicasso(
    url: File,
    circleTransform: Boolean = false,
    centerCrop: Boolean = false,
    @DrawableRes errorResId: Int? = null,
    onSuccess: (() -> Unit)? = null,
    onError: (() -> Unit)? = null
): String? {

    val request = Picasso.get().load(url)

    if (circleTransform) {
        request.transform(CircleTransform())
    }

    if (centerCrop) {
        request.fit().centerCrop()
    }

    if (errorResId != null) {
        request.error(errorResId)
    }

    val tag = Random.nextDouble().toString()
    request.tag(tag)
    request.into(this, object : Callback {
        override fun onError(e: java.lang.Exception?) {
            onError?.invoke()
        }

        override fun onSuccess() {
            onSuccess?.invoke()
        }
    })
    return tag
}