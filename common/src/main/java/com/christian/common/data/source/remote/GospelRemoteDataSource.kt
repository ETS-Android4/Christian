package com.christian.common.data.source.remote

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.christian.common.GOSPELS
import com.christian.common.data.Gospel
import com.christian.common.data.Result
import com.christian.common.data.source.GospelDataSource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ktx.toObject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class GospelRemoteDataSource : GospelDataSource {

    private val observableWritings = MutableLiveData<Result<List<Gospel>>>()
    private var firebaseFirestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    init {
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        firebaseFirestore.firestoreSettings = settings
    }


    //    Insert
    override suspend fun saveGospel(gospel: Gospel): Result<Void>? {
        return suspendCoroutine { continuation ->
            firebaseFirestore.collection(GOSPELS).document(gospel.gospelId)
                .set(gospel).addOnSuccessListener {
                    continuation.resume(Result.Success(it))
                }.addOnFailureListener {
                    continuation.resume(Result.Error(it))
                }
        }
    }

    //    Delete
    override suspend fun clearCompletedWritings() {
//        WRITINGS_CACHE_DATA = WRITINGS_CACHE_DATA.filterValues {
//            !it.isCompleted
//        } as LinkedHashMap<String, Writing>
    }

    //    Update
//    Query
    override suspend fun getGospel(writingId: String): Result<Gospel> {
        // Simulate network by delaying the execution.
        return suspendCoroutine { continuation ->
            firebaseFirestore.collection(GOSPELS).document(writingId).get().addOnSuccessListener {
                it.toObject<Gospel>()?.let { it1 -> continuation.resume(Result.Success(it1)) }
            }
        }
    }

    override suspend fun deleteAllWritings() {
//        WRITINGS_CACHE_DATA.clear()
    }

    override suspend fun deleteWriting(writingId: String): Result<Void>? {
        return suspendCoroutine { continuation ->
            firebaseFirestore.collection(GOSPELS).document(writingId)
                .delete().addOnSuccessListener {
                    continuation.resume(Result.Success(it))
                }.addOnFailureListener {
                    continuation.resume(Result.Error(it))
                }
        }
    }

    //    Update
    override suspend fun completeWriting(gospel: Gospel) {
//        val completedTask = Writing(writing.title, writing.description, true, writing.id)
//        TASKS_SERVICE_DATA[writing.id] = completedTask
    }

    override suspend fun completeWriting(writingId: String) {
        // Not required for the remote data source
    }

    override suspend fun activateWriting(gospel: Gospel) {
//        val activeTask = Writing(writing.title, writing.description, false, writing.id)
//        TASKS_SERVICE_DATA[writing.id] = activeTask
    }

    override suspend fun activateWriting(writingId: String) {
        // Not required for the remote data source
    }

    //    Query
    override fun observeWritings(): LiveData<Result<List<Gospel>>> {
        return observableWritings
    }

    override fun observeWriting(writingId: String): LiveData<Result<Gospel>> {
        return observableWritings.map { writings ->
            when (writings) {
                is Result.Loading -> Result.Loading
                is Result.Error -> Result.Error(writings.exception)
                is Result.Success -> {
                    val writing = writings.data.firstOrNull() { it.gospelId == writingId }
                        ?: return@map Result.Error(Exception("Not found"))
                    Result.Success(writing)
                }
            }
        }
    }

    override suspend fun getGospels(): Result<List<Gospel>> {
        TODO("Not yet implemented")
    }

    override suspend fun refreshWritings() {
//        observableWritings.value = getWritings()
    }

    override suspend fun refreshWriting(writingId: String) {
        refreshWritings()
    }
}