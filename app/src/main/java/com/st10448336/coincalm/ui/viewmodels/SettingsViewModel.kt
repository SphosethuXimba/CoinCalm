package com.st10448336.coincalm.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.st10448336.coincalm.data.AppDatabase
import com.st10448336.coincalm.data.entites.User
import kotlinx.coroutines.launch
import androidx.compose.runtime.*

/** ViewModel responsible for loading user settings asynchronously to persist state across UI recompositions. */
class SettingsViewModel : ViewModel() {
    private val _user = mutableStateOf<User?>(null)
    val user: State<User?> = _user

    private val auth = FirebaseAuth.getInstance()

    fun loadUserData(context: Context) {
        val uid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            val db = AppDatabase.getInstance(context)
            val userFromDb = db.userDao().getUserById(uid)
            _user.value = userFromDb
        }
    }
}