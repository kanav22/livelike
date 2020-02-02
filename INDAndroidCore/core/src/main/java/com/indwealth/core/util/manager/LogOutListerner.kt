package com.indwealth.core.util.manager

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.indwealth.core.ui.CoreActivity

class LogOutListener(activity: CoreActivity, private val needLogOutListener: Boolean = true) : DefaultLifecycleObserver {

    private var _logOutLiveData = MutableLiveData<Boolean>()
    val logOutLiveData: LiveData<Boolean>
        get() {
            return _logOutLiveData
        }

    private val broadcastReceiver by lazy {
        object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == INTENT_USER_LOGGED_OUT) {
                    if (_logOutLiveData.value != true) { // set value only if current value is not true
                        _logOutLiveData.value = true
                    }
                }
            }
        }
    }

    init {
        (activity as LifecycleOwner).lifecycle.addObserver(this)
    }

    override fun onResume(owner: LifecycleOwner) {
        if (needLogOutListener) {
            LocalBroadcastManager.getInstance((owner as AppCompatActivity)).registerReceiver(broadcastReceiver, IntentFilter(INTENT_USER_LOGGED_OUT))
        }
    }

    override fun onPause(owner: LifecycleOwner) {
        if (needLogOutListener) {
            LocalBroadcastManager.getInstance((owner as AppCompatActivity)).unregisterReceiver(broadcastReceiver)
        }
    }
}

const val REQUEST_USER_LOGIN_AFTER_LOGOUT = 8005