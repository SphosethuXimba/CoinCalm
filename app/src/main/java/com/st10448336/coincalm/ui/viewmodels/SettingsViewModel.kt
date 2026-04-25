package com.st10448336.coincalm.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.st10448336.coincalm.data.AppDatabase
import com.st10448336.coincalm.data.entites.User
import kotlinx.coroutines.launch
import androidx.compose.runtime.*

class SettingsViewModel : ViewModel() {
    private val _user = mutableStateOf<User?>(null) // To hold the user data
    val user: State<User?> = _user // Public state to be observed

    private val auth = FirebaseAuth.getInstance() // Firebase Auth instance

    // Load user profile from Room and Firebase
    fun loadUserData(context: Context) {
        val uid = auth.currentUser?.uid // Get the UID of the currently authenticated user

        if (uid == null) {
            // Handle no user session (redirect to login, for example)
            return
        }

        // Load data from Room database (using the user's UID)
        viewModelScope.launch {
            val db = AppDatabase.getInstance(context) // Get the Room database instance
            val userFromDb = db.userDao().getUserById(uid) // Get user data from Room using UID
            _user.value = userFromDb // Update the _user state with the user data
        }
    }
}