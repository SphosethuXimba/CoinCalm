package com.st10448336.coincalm.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.st10448336.coincalm.data.entites.User

/**
 * UserDao — CRUD operations for the [User] table.
 * All functions are suspend — always call from a coroutine on Dispatchers.IO.
 */
@Dao
interface UserDao {

    /**
     * Insert or replace user profile after Firebase registration.
     * REPLACE handles re-registration (stale local row) gracefully.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    /**
     * Load profile by Firebase UUID — called on every app launch
     * once Firebase confirms the session is still valid.
     */
    @Query("SELECT * FROM users WHERE firebase_uuid = :firebaseUuid LIMIT 1")
    suspend fun getUserById(firebaseUuid: String): User?

    /** Update income or currency when user edits their profile. */
    @Update
    suspend fun updateUser(user: User)

    /** Hard-delete user and cascade to all related rows. */
    @Query("DELETE FROM users WHERE firebase_uuid = :firebaseUuid")
    suspend fun deleteUser(firebaseUuid: String)
}