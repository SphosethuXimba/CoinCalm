plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    // KSP replaces kapt for Room — faster and fully Kotlin-native
    alias(libs.plugins.ksp)

    // Firebase Google Services — applies google-services.json
    id("com.google.gms.google-services")
}

android {
    namespace  = "com.st10448336.coincalm"
    compileSdk = 35   // AGP 8.x uses a plain integer here — the release() DSL is AGP 9.x only

    defaultConfig {
        applicationId         = "com.st10448336.coincalm"
        minSdk                = 25
        targetSdk             = 35
        versionCode           = 1
        versionName           = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true   // enables Jetpack Compose
    }
}

dependencies {
    // ── AndroidX Core ─────────────────────────────────────────────────────
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // ── Compose BOM — single BOM entry manages all Compose lib versions ───
    // Only declare it ONCE. Do NOT repeat individual compose entries manually.
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // Compose libraries — no version needed; BOM manages them
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    // Extended icons (used for ArrowBack, etc. in Compose screens)
    implementation("androidx.compose.material:material-icons-extended")

    // Debug-only Compose tools
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // ── Compose Navigation ─────────────────────────────────────────────────
    implementation("androidx.navigation:navigation-compose:2.8.3")

    // ── Lifecycle / ViewModel for Compose ─────────────────────────────────
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")

    // ── Room (local offline DB — KSP instead of kapt) ─────────────────────
    // KSP is faster and avoids the kapt/Kotlin version compatibility issues
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")     // KSP annotation processor

    // ── Kotlin Coroutines ──────────────────────────────────────────────────
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    // Required for .await() on Firebase Tasks inside coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1")

    // ── Firebase Auth ──────────────────────────────────────────────────────
    // BOM manages Firebase library versions — only declare the BOM once
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))
    implementation("com.google.firebase:firebase-auth-ktx")

    // ── Coil (async image loading for Supabase receipt URLs) ───────────────
    implementation("io.coil-kt:coil-compose:2.7.0")

    // ── Testing ────────────────────────────────────────────────────────────
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}