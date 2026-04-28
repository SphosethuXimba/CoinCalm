package com.st10448336.coincalm.data

import android.content.Context
import android.content.SharedPreferences

/**
 * SessionPreferences — Controls whether the app honours the persisted
 * Firebase Auth session on next launch ("Remember Me" behaviour).
 *
 * THE PROBLEM:
 * Firebase Auth on Android always writes its session token to disk.
 * This means the next time the app launches, FirebaseAuth.currentUser
 * is non-null even if the user never ticked "Remember Me".
 * On a cloned/shared device this causes the app to skip login entirely
 * and go straight to Dashboard as someone else's account.
 *
 * THE SOLUTION:
 * We store a manual "remember_me" flag in SharedPreferences.
 * On SplashScreen, if "remember_me" is false, we call FirebaseAuth.signOut()
 * to discard the persisted token before routing — forcing the user to log in.
 * If "remember_me" is true, we honour the session and skip login as before.
 *
 * This gives users explicit control over session persistence per device.
 */
class SessionPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME       = "coincalm_session_prefs"
        private const val KEY_REMEMBER_ME  = "remember_me"
        private const val KEY_SAVED_EMAIL  = "saved_email"
    }

    // ── Remember Me flag ──────────────────────────────────────────────────

    /**
     * Set when the user taps "Sign In" or "Create Account".
     * If true: Firebase session is honoured on next launch (skip login).
     * If false: Firebase session is cleared on next launch (force login).
     */
    fun setRememberMe(value: Boolean) {
        prefs.edit().putBoolean(KEY_REMEMBER_ME, value).apply()
    }

    /**
     * Returns true only if the user explicitly chose to stay signed in.
     * Defaults to FALSE — meaning the first launch after install always
     * requires login, which prevents the "skipped to Dashboard" bug.
     */
    fun isRememberMe(): Boolean = prefs.getBoolean(KEY_REMEMBER_ME, false)

    // ── Saved email (convenience — pre-fills the email field) ─────────────

    /** Save the last successfully logged-in email for pre-filling the field. */
    fun saveEmail(email: String) {
        prefs.edit().putString(KEY_SAVED_EMAIL, email).apply()
    }

    /** Returns the last successfully logged-in email, or empty string. */
    fun getSavedEmail(): String = prefs.getString(KEY_SAVED_EMAIL, "") ?: ""

    // ── Clear on logout ───────────────────────────────────────────────────

    /**
     * Call this when the user explicitly logs out.
     * Clears the remember-me flag so the next launch goes to login.
     * Keeps the saved email so the field is still pre-filled.
     */
    fun clearSession() {
        prefs.edit()
            .putBoolean(KEY_REMEMBER_ME, false)
            .apply()
    }

    /** Wipes everything — used on account deletion. */
    fun clearAll() {
        prefs.edit().clear().apply()
    }
}