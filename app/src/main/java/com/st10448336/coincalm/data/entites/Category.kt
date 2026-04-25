package com.st10448336.coincalm.data.entites

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Category Entity — user-defined spending categories (e.g. "Groceries", "Transport").
 *
 * Multi-user isolation: [userId] FK → [User.firebaseUuid].
 * Index on [userId] makes "get all categories for this user" fast.
 *
 * REQUIREMENT-02: Dynamic Category Creation
 */
@Entity(
    tableName = "categories",
    foreignKeys = [
        ForeignKey(
            entity        = User::class,
            parentColumns = ["firebase_uuid"],
            childColumns  = ["user_id"],
            onDelete      = ForeignKey.CASCADE   // delete categories when user is deleted
        )
    ],
    indices = [Index(value = ["user_id"])]
)
data class Category(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "category_id")
    val categoryId: Int = 0,

    // Firebase UUID of the owning user
    @ColumnInfo(name = "user_id")
    val userId: String,

    // Human-readable name shown in dropdowns and lists
    @ColumnInfo(name = "category_name")
    val categoryName: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)