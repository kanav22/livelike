package com.indwealth.core.util.manager

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.indwealth.core.BaseApplication
import com.indwealth.core.util.data.CorePrefs

class TokenManager(private val context: Application) {

    private val tokenPrefs = TokenPrefs(context)
    private var _mutableToken: String?
    val token: String?
        get() = _mutableToken

    init {
        _mutableToken = tokenPrefs.prefs.getString(TOKEN_KEY, "")
    }

    fun refreshToken(): String {
        return token!!
    }

    fun isUserLoggedIn(): Boolean {
        return !token.isNullOrEmpty()
    }

    fun setAuthToken(token: String) {
        this._mutableToken = token
        tokenPrefs.editor.putString(TOKEN_KEY, token).apply()
    }

    fun reset(sendBroadcast: Boolean = true) {
        setAuthToken("")
        if (sendBroadcast) {
            LocalBroadcastManager.getInstance(context).sendBroadcast(Intent(INTENT_USER_LOGGED_OUT))
        }
        (context as BaseApplication).onLogout()
    }

    inner class TokenPrefs(context: Context) : CorePrefs(context)
}

const val TOKEN_KEY = "token_key"
const val INTENT_USER_LOGGED_OUT = "intent_user_logged_out"