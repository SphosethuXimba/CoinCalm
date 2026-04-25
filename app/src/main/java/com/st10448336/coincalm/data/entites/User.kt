package com.st10448336.coincalm.data.entites

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * User Entity — stores the profile data tied to a Firebase Auth UUID.
 *
 * [firebaseUuid] is the Firebase Auth UID (String), NOT an auto-incremented Int.
 * Every other entity stores this value as a `userId` foreign key so all data
 * belonging to one account can be efficiently queried and isolated from others.
 *
 * REQUIREMENT-01: User Authentication & Profile Management
 */
@Entity(tableName = "users")
data class User(

    // Firebase Auth UID — e.g. "uXk9wP3mZqT..." (28-char alphanumeric string)
    @PrimaryKey
    @ColumnInfo(name = "firebase_uuid")
    val firebaseUuid: String,

    // Display name entered during registration (min 3 chars enforced in UI)
    @ColumnInfo(name = "username")
    val username: String,

    // Email — also stored in Firebase; kept locally for fast profile display
    @ColumnInfo(name = "email")
    val email: String,

    // Used to calculate remaining budget; displayed on Dashboard
    @ColumnInfo(name = "monthly_income")
    val monthlyIncome: Float,

    // e.g. "ZAR", "USD", "EUR" — prepended to all money display strings
    @ColumnInfo(name = "currency_preference")
    val currencyPreference: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)