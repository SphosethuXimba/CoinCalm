package com.st10448336.coincalm.data.repository

import com.st10448336.coincalm.data.remote.SupabaseClientProvider
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object StorageRepository {

    suspend fun uploadReceipt(file: File): String = withContext(Dispatchers.IO) {
        try {
            val bucket = SupabaseClientProvider.client.storage.from("receipts")
            val fileName = "${System.currentTimeMillis()}.jpg"
            bucket.upload(path = fileName, data = file.readBytes(), upsert = false)
            val url = bucket.publicUrl(fileName)
            println("✅ Upload success: $url")
            url
        } catch (e: Exception) {
            println("❌ Upload error class: ${e::class.simpleName}")
            println("❌ Upload error message: ${e.message}")
            throw e  // still rethrows so the UI catches it
        }
    }
}