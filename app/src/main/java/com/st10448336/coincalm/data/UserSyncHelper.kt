package com.st10448336.coincalm.data

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.st10448336.coincalm.data.entites.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Validates local RoomDB synchronisation with active Firebase sessions across multiple devices. */
object UserSyncHelper {
    private const val TAG = "UserSyncHelper"

    suspend fun ensureUserInRoomDb(context: Context): User? {
        return withContext(Dispatchers.IO) {
            val firebaseUser = FirebaseAuth.getInstance().currentUser ?: return@withContext null

            val uid   = firebaseUser.uid
            val email = firebaseUser.email ?: ""
            val db    = AppDatabase.getInstance(context)

            val existing = db.userDao().getUserById(uid)
            if (existing != null) return@withContext existing

            val placeholder = User(
                firebaseUuid       = uid,
                username           = email.substringBefore("@").ifBlank { "User" },
                email              = email,
                monthlyIncome      = 0f,
                currencyPreference = "ZAR"
            )

            db.userDao().insertUser(placeholder)
            placeholder
        }
    }
}