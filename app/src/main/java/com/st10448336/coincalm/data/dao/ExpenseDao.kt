package com.st10448336.coincalm.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.st10448336.coincalm.data.entites.Expense
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertExpense(expense: Expense): Long

    @Query("""
        SELECT * FROM expenses 
        WHERE user_id = :userId 
        AND date >= :startDate 
        AND date <= :endDate 
        ORDER BY date DESC, created_at DESC
    """)
    suspend fun getExpensesForPeriod(userId: String, startDate: String, endDate: String): List<Expense>

    @Query("""
        SELECT category_id, SUM(amount) AS total 
        FROM expenses 
        WHERE user_id = :userId 
        AND date >= :startDate 
        AND date <= :endDate 
        GROUP BY category_id
    """)
    suspend fun getCategoryTotalsForPeriod(userId: String, startDate: String, endDate: String): List<CategoryTotal>

    @Query("SELECT * FROM expenses WHERE user_id = :userId ORDER BY date DESC, created_at DESC")
    fun getAllExpensesFlow(userId: String): Flow<List<Expense>>

    @Query("""
        SELECT COALESCE(SUM(amount), 0.0) 
        FROM expenses 
        WHERE user_id = :userId 
        AND date LIKE :yearMonth || '%'
    """)
    suspend fun getTotalSpentThisMonth(userId: String, yearMonth: String): Float

    @Delete
    suspend fun deleteExpense(expense: Expense)

    @Query("SELECT * FROM expenses WHERE user_id = :userId ORDER BY date DESC, created_at DESC")
    suspend fun getAllExpensesForUser(userId: String): List<Expense>
}

data class CategoryTotal(
    val category_id: Int?,
    val total: Float
)