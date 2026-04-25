package com.st10448336.coincalm.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.st10448336.coincalm.data.entites.Expense
import kotlinx.coroutines.flow.Flow

/**
 * ExpenseDao — CRUD + filtered queries for the [Expense] table.
 *
 * Date strings MUST be "YYYY-MM-DD" (ISO-8601).
 * Lexicographic ordering works correctly for this format in SQL.
 */
@Dao
interface ExpenseDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertExpense(expense: Expense): Long

    /**
     * REQUIREMENT-05: Expenses for a user in an inclusive date range.
     *
     * @param userId    Firebase UUID
     * @param startDate "YYYY-MM-DD" inclusive lower bound
     * @param endDate   "YYYY-MM-DD" inclusive upper bound
     */
    @Query("""
        SELECT * FROM expenses
        WHERE user_id = :userId
        AND date >= :startDate
        AND date <= :endDate
        ORDER BY date DESC, created_at DESC
    """)
    suspend fun getExpensesForPeriod(
        userId: String,
        startDate: String,
        endDate: String
    ): List<Expense>

    /**
     * REQUIREMENT-05: Aggregate spending per category for a period.
     * Returns a list of [CategoryTotal] projection objects — not full entities.
     */
    @Query("""
        SELECT category_id, SUM(amount) AS total
        FROM expenses
        WHERE user_id = :userId
        AND date >= :startDate
        AND date <= :endDate
        GROUP BY category_id
    """)
    suspend fun getCategoryTotalsForPeriod(
        userId: String,
        startDate: String,
        endDate: String
    ): List<CategoryTotal>

    /**
     * All expenses as a Flow — collected in Dashboard ViewModel
     * to keep the "total spent this month" badge live.
     */
    @Query("""
        SELECT * FROM expenses
        WHERE user_id = :userId
        ORDER BY date DESC, created_at DESC
    """)
    fun getAllExpensesFlow(userId: String): Flow<List<Expense>>

    /**
     * Sum of all expenses for a given month.
     * COALESCE returns 0.0 if no expenses exist (avoids null SUM).
     *
     * @param yearMonth "YYYY-MM" e.g. "2026-04"
     */
    @Query("""
        SELECT COALESCE(SUM(amount), 0.0)
        FROM expenses
        WHERE user_id = :userId
        AND date LIKE :yearMonth || '%'
    """)
    suspend fun getTotalSpentThisMonth(userId: String, yearMonth: String): Float

    @Delete
    suspend fun deleteExpense(expense: Expense)
}

/**
 * Projection for the category-totals query.
 * Room maps query result columns to these fields by name.
 */
data class CategoryTotal(
    val category_id: Int?,
    val total: Float
)