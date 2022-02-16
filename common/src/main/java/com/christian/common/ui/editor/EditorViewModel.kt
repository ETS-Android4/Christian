package com.christian.common.ui.editor

import androidx.lifecycle.ViewModel
import com.christian.common.CommonApp
import com.christian.common.data.Result
import com.christian.common.data.Gospel
import com.christian.common.data.source.GospelRepository
import com.christian.common.data.source.GospelServiceLocator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EditorViewModel : ViewModel() {

    val gospelRepository: GospelRepository =
        GospelServiceLocator.provideGospelsRepository(CommonApp.context)

    //    Insert
    suspend fun saveGospel(gospel: Gospel, local: Boolean = true): Result<Void>? = withContext(Dispatchers.IO) {
        gospelRepository.saveGospel(gospel, local)
    }

    //    Delete
    suspend fun deleteGospels() = withContext(Dispatchers.IO) {
        gospelRepository.deleteAllTasks()
    }

    suspend fun deleteGospel(gospelId: String, local: Boolean = true) = withContext(Dispatchers.IO) {
        gospelRepository.deleteTask(gospelId, local)
    }
    //    Update
    //    Query
    suspend fun getGospels(): Result<List<Gospel>> = withContext(Dispatchers.IO) {
        gospelRepository.getGospels()
    }

    suspend fun getGospel(writingId: String, local: Boolean = true): Result<Gospel> = withContext(Dispatchers.IO) {
        gospelRepository.getGospel(writingId, local)
    }

}