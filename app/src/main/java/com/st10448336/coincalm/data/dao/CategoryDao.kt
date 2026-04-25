package com.st10448336.coincalm.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.st10448336.coincalm.data.entites.Category
import kotlinx.coroutines.flow.Flow

/**
 * CategoryDao — CRUD for the [Category] table.
 *
 * [getCategoriesForUser] returns a Flow so Compose state updates
 * automatically when a new category is added in AddCategoryScreen.
 */
@Dao
interface CategoryDao {

    /** ABORT on duplicate categoryName + userId combination. */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertCategory(category: Category): Long

    /**
     * Observe categories as a Flow — collected in a ViewModel so
     * the AddExpenseScreen dropdown stays current without manual refresh.
     */
    @Query("SELECT * FROM categories WHERE user_id = :userId ORDER BY category_name ASC")
    fun getCategoriesForUser(userId: String): Flow<List<Category>>

    /**
     * One-shot fetch for use inside suspend functions (e.g. when building
     * a snapshot list for a coroutine-based operation).
     */
    @Query("SELECT * FROM categories WHERE user_id = :userId ORDER BY category_name ASC")
    suspend fun getCategoriesOnce(userId: String): List<Category>

    @Query("SELECT * FROM categories WHERE category_id = :categoryId LIMIT 1")
    suspend fun getCategoryById(categoryId: Int): Category?

    /** FK is SET_NULL on Expense — existing expenses are NOT deleted. */
    @Delete
    suspend fun deleteCategory(category: Category)
}