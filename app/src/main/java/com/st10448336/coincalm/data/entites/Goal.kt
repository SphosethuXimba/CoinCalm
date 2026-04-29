package com.st10448336.coincalm.data.entites

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Monthly spending target limits for a specific user. */
@Entity(
    tableName = "goals",
    foreignKeys = [
        ForeignKey(
            entity        = User::class,
            parentColumns = ["firebase_uuid"],
            childColumns  = ["user_id"],
            onDelete      = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["user_id"])]
)
data class Goal(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "goal_id")
    val goalId: Int = 0,

    @ColumnInfo(name = "user_id")
    val userId: String,

    @ColumnInfo(name = "target_month")
    val targetMonth: String,

    @ColumnInfo(name = "min_goal_amount")
    val minGoalAmount: Float,

    @ColumnInfo(name = "max_goal_amount")
    val maxGoalAmount: Float,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)