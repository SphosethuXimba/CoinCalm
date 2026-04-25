package com.st10448336.coincalm.data.remote

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.storage.Storage

object SupabaseClientProvider {

    private const val SUPABASE_URL = "https://evpmemwsqfkobmwxbslo.supabase.co"
    private const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImV2cG1lbXdzcWZrb2Jtd3hic2xvIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzcxMTI0NTQsImV4cCI6MjA5MjY4ODQ1NH0.yrChhrTJuSESZ9OJYBLDNkY9rplH7sixdd7PW0iiQpA"

    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        install(Storage)
    }
}