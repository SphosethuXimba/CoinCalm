package com.st10448336.coincalm.data.repository

import com.st10448336.coincalm.data.remote.SupabaseClientProvider
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object StorageRepository {

    suspend fun uploadReceipt(file: File): String = withContext(Dispatchers.IO) {
        val bucket = SupabaseClientProvider.client.storage.from("receipts")
        val fileName = "receipts/${System.currentTimeMillis()}.jpg"

        // In supabase-kt 2.5.1, upsert is a plain parameter — not a lambda block
        bucket.upload(path = fileName, data = file.readBytes(), upsert = false)

        bucket.publicUrl(fileName)
    }
}