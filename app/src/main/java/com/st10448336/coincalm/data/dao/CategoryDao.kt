package com.st10448336.coincalm.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.st10448336.coincalm.data.entites.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertCategory(category: Category): Long

    @Query("SELECT * FROM categories WHERE user_id = :userId ORDER BY category_name ASC")
    fun getCategoriesForUser(userId: String): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE user_id = :userId ORDER BY category_name ASC")
    suspend fun getCategoriesOnce(userId: String): List<Category>

    @Query("SELECT * FROM categories WHERE category_id = :categoryId LIMIT 1")
    suspend fun getCategoryById(categoryId: Int): Category?

    @Delete
    suspend fun deleteCategory(category: Category)
}