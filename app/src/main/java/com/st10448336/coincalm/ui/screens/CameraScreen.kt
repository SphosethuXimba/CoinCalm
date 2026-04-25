package com.st10448336.coincalm.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import com.st10448336.coincalm.data.repository.StorageRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun CameraScreen(navController: NavController) {

    val context = LocalContext.current
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var statusMessage by remember { mutableStateOf("Opening camera…") }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            val uri = imageUri
            if (uri != null) {
                statusMessage = "Uploading receipt…"
                val file = uriToFile(context, uri)
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val url = StorageRepository.uploadReceipt(file)
                        println("✅ Uploaded: $url")
                        // No Room save — URL is only logged/used here
                    } catch (e: Exception) {
                        e.printStackTrace()
                        println(" Upload failed: ${e.message}")
                    }
                }
            }
        } else {
            navController.popBackStack()
        }
    }

    LaunchedEffect(Unit) {
        val uri = createImageUri(context)
        imageUri = uri
        cameraLauncher.launch(uri)
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = statusMessage, color = Color.White)
    }
}

fun createImageUri(context: Context): Uri {
    val file = File(context.cacheDir, "receipt_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        file
    )
}

fun uriToFile(context: Context, uri: Uri): File {
    val inputStream = context.contentResolver.openInputStream(uri)!!
    val file = File(context.cacheDir, "upload_${System.currentTimeMillis()}.jpg")
    file.outputStream().use { output -> inputStream.copyTo(output) }
    return file
}