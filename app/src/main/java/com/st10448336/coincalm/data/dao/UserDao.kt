package com.st10448336.coincalm.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.st10448336.coincalm.data.entites.User

// Attribution: Room DAO Implementation
// Link: https://developer.android.com/training/data-storage/room/accessing-data
// Author: Android Developers
@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Query("SELECT * FROM users WHERE firebase_uuid = :firebaseUuid LIMIT 1")
    suspend fun getUserById(firebaseUuid: String): User?

    @Update
    suspend fun updateUser(user: User)

    @Query("DELETE FROM users WHERE firebase_uuid = :firebaseUuid")
    suspend fun deleteUser(firebaseUuid: String)
}