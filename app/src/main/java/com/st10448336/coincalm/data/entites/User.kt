package com.st10448336.coincalm.data.entites

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

// Attribution: Room Database Entities
// Link: https://developer.android.com/training/data-storage/room/defining-data
// Author: Android Developers
/** Represents a user profile tied to a Firebase Auth UUID. */
@Entity(tableName = "users")
data class User(
    @PrimaryKey
    @ColumnInfo(name = "firebase_uuid")
    val firebaseUuid: String,

    @ColumnInfo(name = "username")
    val username: String,

    @ColumnInfo(name = "email")
    val email: String,

    @ColumnInfo(name = "monthly_income")
    val monthlyIncome: Float,

    @ColumnInfo(name = "currency_preference")
    val currencyPreference: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)