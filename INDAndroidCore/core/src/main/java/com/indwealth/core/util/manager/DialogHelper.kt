package com.indwealth.core.util.manager

import android.app.Activity
import android.app.AlertDialog
import androidx.fragment.app.Fragment
import com.indwealth.core.R

fun Activity.showInfoDialog(
    title: String = "",
    message: String,
    positiveButton: String = "",
    negativeButton: String = "",
    cancellable: Boolean = false,
    callback: (callback: DialogCallback) -> Unit
): AlertDialog? {
    return try {
        val alertDialogBuilder = AlertDialog.Builder(this)
        with(alertDialogBuilder) {
            setCancelable(cancellable)
            if (title.isNotBlank()) setTitle(title)
            if (message.isNotBlank()) setMessage(message)
            if (positiveButton.isNotBlank()) setPositiveButton(positiveButton) { _, _ -> callback(DialogCallback.Positive) }
            if (negativeButton.isNotBlank()) setNegativeButton(negativeButton) { _, _ -> callback(DialogCallback.Negative) }
        }

        alertDialogBuilder.show()
    } catch (throwable: Throwable) {
        null
    }
}

fun Fragment.showInfoDialog(
    title: String = "",
    message: String,
    positiveButton: String = "",
    negativeButton: String = "",
    cancellable: Boolean = false,
    callback: (callback: DialogCallback) -> Unit
): AlertDialog? {
    return activity?.showInfoDialog(title, message, positiveButton, negativeButton, cancellable, callback)
}

fun Activity.showDialogWithFinishApp(errorMessage: String) {
    showInfoDialog(getString(R.string.dialog_title_alert),
            errorMessage,
            getString(android.R.string.ok),
            "",
            false
    ) {
        when (it) {
            DialogCallback.Positive -> {
                finishAffinity()
            }
        }
    }
}

fun Fragment.showDialogWithFinishApp(errorMessage: String) {
    activity?.showDialogWithFinishApp(errorMessage)
}

sealed class DialogCallback {
    object Positive : DialogCallback()
    object Negative : DialogCallback()
}