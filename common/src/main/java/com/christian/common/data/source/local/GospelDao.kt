package com.christian.common.data.source.local

import androidx.lifecycle.LiveData
import androidx.room.*
import com.christian.common.data.Result
import com.christian.common.data.Gospel

@Dao
interface GospelDao {
//    Insert
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGospel(gospel: Gospel)

//    Delete
    @Query("DELETE FROM gospels")
    suspend fun deleteWritings()

    @Query("DELETE FROM gospels WHERE gospelId = :writingId")
    suspend fun deleteWritingById(writingId: String): Int

    @Query("DELETE FROM gospels WHERE completed = 1")
    suspend fun deleteCompletedWritings(): Int

//    Update
    @Update
    suspend fun updateWriting(gospel: Gospel): Int

    @Query("UPDATE gospels SET completed = :completed WHERE gospelId = :gospelId")
    suspend fun updateCompleted(gospelId: String, completed: Boolean)

//    Query
    @Query("SELECT * FROM gospels")
    fun observeWritings(): LiveData<List<Gospel>>

    @Query("SELECT * FROM gospels WHERE gospelId = :gospelId")
    fun observeWritingById(gospelId: String): LiveData<Gospel>

    @Query("SELECT * FROM gospels")
    suspend fun getWritings(): List<Gospel>

    @Query("SELECT * FROM gospels WHERE gospelId = :gospelId")
    suspend fun getWritingById(gospelId: String): Gospel
}