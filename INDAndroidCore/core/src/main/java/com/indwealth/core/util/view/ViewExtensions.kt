package com.indwealth.core.util.view

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Build
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.PixelCopy
import android.view.View
import androidx.appcompat.widget.AppCompatEditText
import com.indwealth.core.util.dpToPx
import kotlin.math.roundToInt
import kotlinx.coroutines.*

fun AppCompatEditText.afterTextChangedDebounce(delayMillis: Long, input: (String) -> Unit) {
    var lastInput = ""
    var debounceJob: Job? = null
    val uiScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            if (editable != null) {
                val newtInput = editable.toString()
                debounceJob?.cancel()
                if (lastInput != newtInput) {
                    lastInput = newtInput
                    debounceJob = uiScope.launch {
                        delay(delayMillis)
                        if (lastInput == newtInput) {
                            input(newtInput)
                        }
                    }
                }
            }
        }

        override fun beforeTextChanged(cs: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(cs: CharSequence?, start: Int, before: Int, count: Int) {}
    })
}

private fun getScreenShotDeprecated(view: View, callback: (Bitmap) -> Unit) {
    view.isDrawingCacheEnabled = true
    view.buildDrawingCache(true)
    val bitmap = Bitmap.createBitmap(view.drawingCache)
    view.isDrawingCacheEnabled = false
    callback(bitmap)
}

// for api level 28
private fun getScreenShotFromView(view: View, activity: Activity, callback: (Bitmap) -> Unit) {
    activity.window?.let { window ->
        val bitmap = Bitmap.createBitmap(view.width, view.height - (144.dpToPx(view.context).roundToInt()), Bitmap.Config.ARGB_8888)
        val locationOfViewInWindow = IntArray(2)
        view.getLocationInWindow(locationOfViewInWindow)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                PixelCopy.request(
                    window,
                    Rect(
                        locationOfViewInWindow[0],
                        locationOfViewInWindow[1] + 64.dpToPx(view.context).roundToInt(),
                        locationOfViewInWindow[0] + view.width,
                        locationOfViewInWindow[1] + view.height - 80.dpToPx(view.context).roundToInt()
                    ),
                    bitmap, { copyResult ->
                    if (copyResult == PixelCopy.SUCCESS) {
                        callback(bitmap)
                    }
                }, Handler())
            }
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
    }
}

fun View.takeScreenshot(activity: Activity, callback: (Bitmap) -> Unit) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        getScreenShotFromView(this, activity, callback)
    } else {
        getScreenShotDeprecated(this, callback)
    }
}