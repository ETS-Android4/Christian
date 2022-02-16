package com.christian.common.data.source

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.room.Room
import com.christian.common.data.Gospel
import com.christian.common.data.Result
import com.christian.common.data.source.local.GospelDatabase
import com.christian.common.data.source.local.GospelLocalDataSource
import com.christian.common.data.source.remote.GospelRemoteDataSource
import kotlinx.coroutines.*

private const val DB_NAME = "gospel.db"

object GospelServiceLocator {

    private val lock = Any()
    private var database: GospelDatabase? = null

    @Volatile
    var gospelRepository: GospelRepository? = null
        @VisibleForTesting set

    fun provideGospelsRepository(context: Context): GospelRepository {
        synchronized(this) {
            return gospelRepository ?: gospelRepository ?: createGospelRepository(context)
        }
    }

    private fun createGospelRepository(context: Context): GospelRepository {
        val newRepo =
            GospelRepository(GospelRemoteDataSource(), createGospelLocalDataSource(context))
        gospelRepository = newRepo
        return newRepo
    }

    fun createGospelLocalDataSource(context: Context): GospelDataSource {
        val database = database ?: createDataBase(context)
        return GospelLocalDataSource(database.writingDao())
    }

    private fun createDataBase(context: Context): GospelDatabase {
        val result = Room.databaseBuilder(
            context.applicationContext,
            GospelDatabase::class.java, DB_NAME
        ).fallbackToDestructiveMigration().build()
        database = result
        return result
    }

    @VisibleForTesting
    fun resetRepository() {
        synchronized(lock) {
            runBlocking {
                GospelRemoteDataSource().deleteAllWritings()
            }
            // Clear all data to avoid test pollution.
            database?.apply {
                clearAllTables()
                close()
            }
            database = null
            gospelRepository = null
        }
    }
}

class GospelRepository(
    private val gospelRemoteDataSource: GospelDataSource,
    private val gospelLocalDataSource: GospelDataSource,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    //    Insert
    suspend fun saveGospel(gospel: Gospel, local: Boolean): Result<Void>? {
        return when (local) {
            true -> gospelLocalDataSource.saveGospel(gospel)
            else -> gospelRemoteDataSource.saveGospel(gospel)
        }
    }

    //    Delete

    fun observeWritings(): LiveData<Result<List<Gospel>>> {
        return gospelLocalDataSource.observeWritings()
    }

    fun observeTask(writingId: String): LiveData<Result<Gospel>> {
        return gospelLocalDataSource.observeWriting(writingId)
    }

    suspend fun getGospels(): Result<List<Gospel>> {
        return gospelLocalDataSource.getGospels()
    }

    suspend fun getGospel(gospelId: String, local: Boolean): Result<Gospel> {
        return when (local) {
            true -> gospelLocalDataSource.getGospel(gospelId)
            else -> gospelRemoteDataSource.getGospel(gospelId)
        }
    }

    suspend fun completeTask(task: Gospel) {
        coroutineScope {
            launch { gospelRemoteDataSource.completeWriting(task) }
            launch { gospelLocalDataSource.completeWriting(task) }
        }
    }

    suspend fun completeTask(taskId: String) {
        withContext(ioDispatcher) {
            (getTaskWithId(taskId) as? Result.Success)?.let { it ->
//                completeWriting(it.data)
            }
        }
    }

    suspend fun activateTask(gospel: Gospel) = withContext<Unit>(ioDispatcher) {
        coroutineScope {
            launch { gospelRemoteDataSource.activateWriting(gospel) }
            launch { gospelLocalDataSource.activateWriting(gospel) }
        }
    }

    suspend fun activateTask(taskId: String) {
        withContext(ioDispatcher) {
            (getTaskWithId(taskId) as? Result.Success)?.let { it ->
//                activateWriting(it.data)
            }
        }
    }

    suspend fun clearCompletedTasks() {
        coroutineScope {
            launch { gospelRemoteDataSource.clearCompletedWritings() }
            launch { gospelLocalDataSource.clearCompletedWritings() }
        }
    }

    suspend fun deleteAllTasks() {
        withContext(ioDispatcher) {
            coroutineScope {
                launch { gospelRemoteDataSource.deleteAllWritings() }
                launch { gospelLocalDataSource.deleteAllWritings() }
            }
        }
    }

    suspend fun deleteTask(writingId: String, local: Boolean) {
        when (local) {
            true -> gospelLocalDataSource.deleteWriting(writingId)
            else -> gospelRemoteDataSource.deleteWriting(writingId)
        }
    }

    private suspend fun getTaskWithId(id: String): Result<Gospel> {
        return gospelLocalDataSource.getGospel(id)
    }
}