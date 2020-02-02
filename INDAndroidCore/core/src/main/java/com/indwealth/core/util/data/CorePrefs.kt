package com.indwealth.core.util.data

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import com.indwealth.core.util.log

@SuppressLint("CommitPrefEdits")
open class CorePrefs(context: Context) {

    val prefs: SharedPreferences = context.getSharedPreferences(CorePrefs::class.java.simpleName, Activity.MODE_PRIVATE)
    val editor: SharedPreferences.Editor

    init {
        this.editor = prefs.edit()
    }

    open fun clear() {
        editor.clear().commit()
    }

    fun contains(key: String) = prefs.contains(key)

    fun register(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        prefs.registerOnSharedPreferenceChangeListener(listener)
    }

    fun unregister(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        prefs.unregisterOnSharedPreferenceChangeListener(listener)
    }

    var mobile: String?
        get() = prefs.getString("mobile", "")
        set(token) = editor.putString("mobile", token).apply()

    var rateAppReminder: Long
        get() = prefs.getLong("KEY_RATE_APP_REMINDER", 0)
        set(reminder) = editor.putLong("KEY_RATE_APP_REMINDER", reminder).apply()

    var rateAppDismissCount: Int
        get() = prefs.getInt("KEY_RATE_APP_DISMISS_COUNT", 0)
        set(language) = editor.putInt("KEY_RATE_APP_DISMISS_COUNT", language).commit().log()

    var screenLockEnabled: Boolean?
        get() = if (prefs.contains(KEY_SCREEN_LOCK_ENABLED)) prefs.getBoolean(KEY_SCREEN_LOCK_ENABLED, false) else null
        set(value) = editor.putBoolean(KEY_SCREEN_LOCK_ENABLED, value ?: false).apply()

    companion object {
        const val KEY_SCREEN_LOCK_ENABLED = "KEY_SCREEN_LOCK_ENABLED"
    }
}