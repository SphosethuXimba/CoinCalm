package com.st10448336.coincalm.data.remote

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.storage.Storage

object SupabaseClientProvider {

    private const val SUPABASE_URL = "https://miufdersfkvlqdiaofxr.supabase.co"
    private const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im1pdWZkZXJzZmt2bHFkaWFvZnhyIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzczODYzMTIsImV4cCI6MjA5Mjk2MjMxMn0.PZQ5X_TcGt1PhiUuoBqCpmxR-t8V0lsPVH2R6uU-ETg"

    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        install(Storage)
    }
}