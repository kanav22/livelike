package com.indwealth.core.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import java.lang.ref.WeakReference

class SmsReceiver : BroadcastReceiver() {
    private val codePattern = "(\\d{6})".toRegex()
    private var otpReceiver: WeakReference<OTPReceiveListener>? = null

    fun initOTPListener(receiver: OTPReceiveListener) {
        this.otpReceiver = WeakReference(receiver)
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (SmsRetriever.SMS_RETRIEVED_ACTION == intent.action) {
            val extras = intent.extras
            val status = extras!!.get(SmsRetriever.EXTRA_STATUS) as Status
            when (status.statusCode) {
                CommonStatusCodes.SUCCESS -> {
                    // Get SMS message contents
                    val message = extras.get(SmsRetriever.EXTRA_SMS_MESSAGE) as String
                    val code: MatchResult? = codePattern.find(message)
                    if (code?.value != null) {
                        if (otpReceiver != null) {
                            otpReceiver?.get()?.onOTPReceived(code.value)
                        }
                    }
                }
                CommonStatusCodes.TIMEOUT -> {
                }
            }
        }
    }

    interface OTPReceiveListener {
        fun onOTPReceived(otp: String)
        fun onOTPTimeOut()
    }
}