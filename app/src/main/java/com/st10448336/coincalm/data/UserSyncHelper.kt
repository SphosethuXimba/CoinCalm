package com.st10448336.coincalm.data

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.st10448336.coincalm.data.entites.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * UserSyncHelper — Ensures the local RoomDB always has a User row
 * for whoever Firebase says is currently signed in.
 *
 * THE PROBLEM THIS SOLVES:
 * Registration saves the User to RoomDB on Device A.
 * When the same Firebase account signs in on Device B (team member clone,
 * new phone, fresh install), RoomDB on Device B is empty.
 * Firebase Auth succeeds but getUserById() returns null → infinite spinner.
 *
 * THE FIX:
 * After every Firebase session confirmation, call ensureUserInRoomDb().
 * If the User row is missing, we create one from Firebase Auth data
 * (email + uid). Username defaults to the email prefix until the user
 * updates it in Edit Profile.
 *
 * For Part 3 (Final PoE): replace this with a Firestore read so the
 * full profile (username, income, currency) syncs across devices.
 */
object UserSyncHelper {

    private const val TAG = "UserSyncHelper"

    /**
     * Checks RoomDB for the current Firebase user and inserts a
     * placeholder row if none exists.
     *
     * Call this from SplashScreen and from LoginScreen after
     * successful Firebase sign-in.
     *
     * @param context Application context
     * @return The User object (either existing or newly created)
     */
    suspend fun ensureUserInRoomDb(context: Context): User? {
        return withContext(Dispatchers.IO) {
            val firebaseUser = FirebaseAuth.getInstance().currentUser
            if (firebaseUser == null) {
                Log.w(TAG, "ensureUserInRoomDb: no Firebase session — skipping")
                return@withContext null
            }

            val uid   = firebaseUser.uid
            val email = firebaseUser.email ?: ""
            val db    = AppDatabase.getInstance(context)

            // Check if the user row already exists
            val existing = db.userDao().getUserById(uid)
            if (existing != null) {
                Log.d(TAG, "User already in RoomDB: ${existing.username}")
                return@withContext existing
            }

            // User is authenticated in Firebase but missing from local RoomDB.
            // This happens on fresh installs, cloned repos, or new devices.
            // Create a placeholder row so the app doesn't spin forever.
            Log.w(TAG, "User missing from RoomDB for UID: $uid — creating placeholder")

            val placeholder = User(
                firebaseUuid       = uid,
                // Derive a display name from the email prefix (e.g. "john" from "john@gmail.com")
                // The user can update this in Edit Profile
                username           = email.substringBefore("@").ifBlank { "User" },
                email              = email,
                monthlyIncome      = 0f,      // user should update in Edit Profile
                currencyPreference = "ZAR"    // sensible default for this project
            )

            db.userDao().insertUser(placeholder)
            Log.i(TAG, "Placeholder user created in RoomDB for UID: $uid")

            placeholder
        }
    }
}