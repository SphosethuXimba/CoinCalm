package com.st10448336.coincalm.data.entites

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Goal Entity — monthly spending target with a min/max band.
 *
 * UI RULE enforced in BudgetGoalsScreen:
 *   maxGoalAmount MUST be strictly greater than minGoalAmount.
 * This is validated with Compose State BEFORE any DB write.
 *
 * REQUIREMENT-04: Min/Max Budget Goal Configuration
 */
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

    // "YYYY-MM" — e.g. "2026-04"
    @ColumnInfo(name = "target_month")
    val targetMonth: String,

    // Lower bound (user should spend AT LEAST this)
    @ColumnInfo(name = "min_goal_amount")
    val minGoalAmount: Float,

    // Upper bound (user should NOT spend more than this)
    @ColumnInfo(name = "max_goal_amount")
    val maxGoalAmount: Float,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)