package com.st10448336.coincalm.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.st10448336.coincalm.data.entites.Goal

/**
 * GoalDao — CRUD for the [Goal] table.
 *
 * REPLACE strategy in [insertGoal] acts as an upsert — if the user already has
 * a goal for this month, it is overwritten instead of throwing a conflict error.
 *
 * NOTE: UI must validate maxGoalAmount > minGoalAmount BEFORE calling insertGoal.
 *       The database does NOT enforce this constraint.
 */
@Dao
interface GoalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: Goal): Long

    @Query("""
        SELECT * FROM goals
        WHERE user_id = :userId
        AND target_month = :targetMonth
        LIMIT 1
    """)
    suspend fun getGoalForMonth(userId: String, targetMonth: String): Goal?

    @Query("SELECT * FROM goals WHERE user_id = :userId ORDER BY target_month DESC")
    suspend fun getAllGoalsForUser(userId: String): List<Goal>

    @Update
    suspend fun updateGoal(goal: Goal)

    @Query("DELETE FROM goals WHERE goal_id = :goalId")
    suspend fun deleteGoal(goalId: Int)
}