package com.st10448336.coincalm.data.entites

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Expense Entity — a single spending record.
 *
 * Receipt photos are uploaded to Supabase Storage. Only the returned public URL
 * is stored here as [supabaseImageUrl]. Use Coil to load this URL in the UI.
 *
 * REQUIREMENT-03: Expense Entry with Media Capture
 */
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
            onDelete      = ForeignKey.SET_NULL  // keep expense if category deleted
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

    // Nullable: category may be deleted after expense was logged
    @ColumnInfo(name = "category_id")
    val categoryId: Int?,

    // Amount in user's chosen currency (2 decimal places enforced in UI)
    @ColumnInfo(name = "amount")
    val amount: Float,

    // Stored as "YYYY-MM-DD" for correct lexicographic SQL range queries
    @ColumnInfo(name = "date")
    val date: String,

    // "HH:mm" 24-hour format
    @ColumnInfo(name = "start_time")
    val startTime: String,

    // "HH:mm" 24-hour format
    @ColumnInfo(name = "end_time")
    val endTime: String,

    // Short description of the purchase
    @ColumnInfo(name = "description")
    val description: String,

    /**
     * Public URL from Supabase Storage after receipt image upload.
     * Example: "https://xyz.supabase.co/storage/v1/object/public/receipts/abc.jpg"
     * NULL = no receipt attached.
     *
     * Load in UI with:  AsyncImage(model = expense.supabaseImageUrl, ...)
     *
     * TODO (Team): Set this field to the URL returned by Supabase after upload.
     */
    @ColumnInfo(name = "supabase_image_url")
    val supabaseImageUrl: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)