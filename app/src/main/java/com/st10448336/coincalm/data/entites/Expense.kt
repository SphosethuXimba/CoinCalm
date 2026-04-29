package com.st10448336.coincalm.data.entites

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Individual spending records linked to a User and a Category. */
@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity        = User::class,
            parentColumns = ["firebase_uuid"],
            childColumns  = ["user_id"],
            onDelete      = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity        = Category::class,
            parentColumns = ["category_id"],
            childColumns  = ["category_id"],
            onDelete      = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["user_id"]),
        Index(value = ["category_id"])
    ]
)
data class Expense(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "expense_id")
    val expenseId: Int = 0,

    @ColumnInfo(name = "user_id")
    val userId: String,

    @ColumnInfo(name = "category_id")
    val categoryId: Int?,

    @ColumnInfo(name = "amount")
    val amount: Float,

    @ColumnInfo(name = "date")
    val date: String,

    @ColumnInfo(name = "start_time")
    val startTime: String,

    @ColumnInfo(name = "end_time")
    val endTime: String,

    @ColumnInfo(name = "description")
    val description: String,

    @ColumnInfo(name = "supabase_image_url")
    val supabaseImageUrl: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)