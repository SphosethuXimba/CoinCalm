package com.st10448336.coincalm.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.st10448336.coincalm.data.dao.CategoryDao
import com.st10448336.coincalm.data.dao.ExpenseDao
import com.st10448336.coincalm.data.dao.GoalDao
import com.st10448336.coincalm.data.dao.UserDao
import com.st10448336.coincalm.data.entites.Category
import com.st10448336.coincalm.data.entites.Expense
import com.st10448336.coincalm.data.entites.Goal
import com.st10448336.coincalm.data.entites.User

/**
 * AppDatabase — Single RoomDB instance for CoinCalm.
 *
 * Part 2 prototype: fully offline SQLite via Room.
 * Final PoE (Part 3): data will also mirror to Firebase Firestore.
 *
 * Singleton pattern with @Volatile + synchronized() prevents multiple
 * concurrent connections, which would corrupt the SQLite WAL journal.
 *
 * @version 1 — PROG7313 POE Part 2
 */
@Database(
    entities = [User::class, Category::class, Expense::class, Goal::class],
    version  = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun goalDao(): GoalDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Thread-safe singleton getter.
         * Always pass [context.applicationContext] to avoid Activity memory leaks.
         */
        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "coincalm_db"
                )
                    // Development only — replace with Migration objects before release
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
    }
}