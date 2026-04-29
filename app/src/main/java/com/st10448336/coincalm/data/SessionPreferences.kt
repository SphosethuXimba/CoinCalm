package com.st10448336.coincalm.data

import android.content.Context
import android.content.SharedPreferences

/** Manages persistent login states and saved credentials utilizing SharedPreferences. */
class SessionPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME       = "coincalm_session_prefs"
        private const val KEY_REMEMBER_ME  = "remember_me"
        private const val KEY_SAVED_EMAIL  = "saved_email"
    }

    fun setRememberMe(value: Boolean) {
        prefs.edit().putBoolean(KEY_REMEMBER_ME, value).apply()
    }

    fun isRememberMe(): Boolean = prefs.getBoolean(KEY_REMEMBER_ME, false)

    fun saveEmail(email: String) {
        prefs.edit().putString(KEY_SAVED_EMAIL, email).apply()
    }

    fun getSavedEmail(): String = prefs.getString(KEY_SAVED_EMAIL, "") ?: ""

    fun clearSession() {
        prefs.edit().putBoolean(KEY_REMEMBER_ME, false).apply()
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }
}