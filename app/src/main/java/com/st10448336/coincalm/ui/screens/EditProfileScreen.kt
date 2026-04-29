package com.st10448336.coincalm.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.st10448336.coincalm.data.AppDatabase
import com.st10448336.coincalm.data.entites.User
import com.st10448336.coincalm.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Profile updater.*/
@Composable
fun EditProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val auth    = FirebaseAuth.getInstance()
    val scope   = rememberCoroutineScope()

    var currentUser   by remember { mutableStateOf<User?>(null) }
    var username      by remember { mutableStateOf("") }
    var monthlyIncome by remember { mutableStateOf("") }
    var isSaving      by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val uid = auth.currentUser?.uid ?: return@LaunchedEffect
        withContext(Dispatchers.IO) {
            val db   = AppDatabase.getInstance(context)
            val user = db.userDao().getUserById(uid)
            withContext(Dispatchers.Main) {
                currentUser   = user
                username      = user?.username ?: ""
                monthlyIncome = user?.monthlyIncome?.toString() ?: ""
            }
        }
    }

    val incomeValue   = monthlyIncome.toFloatOrNull()
    val isSaveEnabled = username.length >= 3 && incomeValue != null && incomeValue > 0f && !isSaving

    Column(modifier = Modifier.fillMaxSize().background(screenBackground()).verticalScroll(rememberScrollState())) {
        Row(modifier = Modifier.fillMaxWidth().padding(start = 8.dp, end = 20.dp, top = 40.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = contentPrimary()) }
            Text("Edit Profile", color = contentPrimary(), fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }

        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(
                value = username, onValueChange = { username = it }, label = { Text("Username") }, singleLine = true,
                isError = username.length < 3 && username.isNotEmpty(), supportingText = { if (username.isNotEmpty() && username.length < 3) Text("Minimum 3 characters", color = ErrorRed) },
                modifier = Modifier.fillMaxWidth(), colors = coinCalmTextFieldColors()
            )

            OutlinedTextField(
                value = currentUser?.email ?: "", onValueChange = {}, label = { Text("Email") }, singleLine = true, enabled = false,
                modifier = Modifier.fillMaxWidth(), colors = coinCalmTextFieldColors()
            )

            OutlinedTextField(
                value = monthlyIncome, onValueChange = { monthlyIncome = it }, label = { Text("Monthly Income") }, singleLine = true,
                isError = monthlyIncome.isNotEmpty() && incomeValue == null, supportingText = { if (monthlyIncome.isNotEmpty() && incomeValue == null) Text("Enter a valid number", color = ErrorRed) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth(), colors = coinCalmTextFieldColors()
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    scope.launch {
                        isSaving = true
                        try {
                            val updated = currentUser!!.copy(username = username.trim(), monthlyIncome = incomeValue!!)
                            withContext(Dispatchers.IO) { AppDatabase.getInstance(context).userDao().updateUser(updated) }
                            Toast.makeText(context, "Profile updated!", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        } catch (e: Exception) {
                            Toast.makeText(context, "Save failed. Try again.", Toast.LENGTH_SHORT).show()
                        } finally {
                            isSaving = false
                        }
                    }
                },
                enabled = isSaveEnabled, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LimeGreen, contentColor = NavyDark, disabledContainerColor = NavyLight, disabledContentColor = TextSecondary)
            ) {
                if (isSaving) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = NavyDark, strokeWidth = 2.dp) else Text("Save Changes", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}