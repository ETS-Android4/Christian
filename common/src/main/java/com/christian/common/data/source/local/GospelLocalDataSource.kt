package com.christian.common.data.source.local

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.christian.common.data.Gospel
import com.christian.common.data.Result
import com.christian.common.data.source.GospelDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GospelLocalDataSource internal constructor(
    private val gospelDao: GospelDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : GospelDataSource {
    //    Insert
    override suspend fun saveGospel(gospel: Gospel): Result<Void>? = withContext(ioDispatcher) {
        Result.Success(gospelDao.insertGospel(gospel))
        return@withContext null
    }

    //    Delete
    override suspend fun clearCompletedWritings() = withContext<Unit>(ioDispatcher) {
        gospelDao.deleteCompletedWritings()
    }

    override suspend fun deleteAllWritings() = withContext(ioDispatcher) {
        gospelDao.deleteWritings()
    }

    override suspend fun deleteWriting(writingId: String): Result<Void>? = withContext(ioDispatcher) {
        Result.Success(gospelDao.deleteWritingById(writingId))
        return@withContext null
    }

    //    Update
    override suspend fun completeWriting(gospel: Gospel) = withContext(ioDispatcher) {
        gospelDao.updateCompleted(gospel.gospelId, true)
    }

    override suspend fun completeWriting(writingId: String) {
        gospelDao.updateCompleted(writingId, true)
    }

    override suspend fun activateWriting(gospel: Gospel) = withContext(ioDispatcher) {
        gospelDao.updateCompleted(gospel.gospelId, false)
    }

    override suspend fun activateWriting(writingId: String) {
        gospelDao.updateCompleted(writingId, false)
    }

    //    Query
    override fun observeWritings(): LiveData<Result<List<Gospel>>> {
        return gospelDao.observeWritings().map {
            Result.Success(it)
        }
    }

    override fun observeWriting(writingId: String): LiveData<Result<Gospel>> {
        return gospelDao.observeWritingById(writingId).map {
            Result.Success(it)
        }
    }

    override suspend fun getGospels(): Result<List<Gospel>> = withContext(ioDispatcher) {
        return@withContext try {
            Result.Success(gospelDao.getWritings())
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getGospel(writingId: String): Result<Gospel> =
        withContext(ioDispatcher) {
            try {
                val writing = gospelDao.getWritingById(writingId)
//            if (writing != null) {
                return@withContext Result.Success(writing)
//            } else {
//                return@withContext Error(Exception("Task not found!"))
//            }
            } catch (e: Exception) {
                return@withContext Result.Error(e)
            }
        }

    override suspend fun refreshWritings() {
        // NO-OP
    }

    override suspend fun refreshWriting(writingId: String) {
        // NO-OP
    }
}