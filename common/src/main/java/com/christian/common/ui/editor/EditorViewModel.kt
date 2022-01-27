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
    suspend fun saveGospel(gospel: Gospel): Result<Void>? = withContext(Dispatchers.IO) {
        gospelRepository.saveGospel(gospel)
    }

    //    Delete
    //    Update
    //    Query
    suspend fun getWriting(writingId: String): Result<Gospel> = withContext(Dispatchers.IO) {
        gospelRepository.getGospel(writingId)
    }

}