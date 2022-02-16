package com.christian.common.data.source

import androidx.lifecycle.LiveData
import com.christian.common.data.Result
import com.christian.common.data.Gospel

interface GospelDataSource {
    //    Insert
    suspend fun saveGospel(gospel: Gospel): Result<Void>?

    //    Delete
    suspend fun clearCompletedWritings()

    suspend fun deleteAllWritings()

    suspend fun deleteWriting(writingId: String): Result<Void>?

    //    Update
    suspend fun completeWriting(gospel: Gospel)

    suspend fun completeWriting(writingId: String)

    suspend fun activateWriting(gospel: Gospel)

    suspend fun activateWriting(writingId: String)

    //    Query
    fun observeWritings(): LiveData<Result<List<Gospel>>>

    fun observeWriting(writingId: String): LiveData<Result<Gospel>>

    suspend fun getGospels(): Result<List<Gospel>>

    suspend fun getGospel(writingId: String): Result<Gospel>

    suspend fun refreshWritings()

    suspend fun refreshWriting(writingId: String)
}