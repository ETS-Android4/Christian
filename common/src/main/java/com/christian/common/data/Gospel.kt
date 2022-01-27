package com.christian.common.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.google.firebase.firestore.FieldValue
import java.util.*

// Published text entity class, for editor activity
@Entity(tableName = "gospels")
data class Gospel @JvmOverloads constructor(
    @PrimaryKey @ColumnInfo(name = "gospelId") val gospelId: String = FieldValue.serverTimestamp().toString(),
    @ColumnInfo(name = "author") val author: String = "",
    @ColumnInfo(name = "classify") val classify: String = "",
    @ColumnInfo(name = "content") val content: String = "",
    @ColumnInfo(name = "contentType") val contentType: String = "",
    @ColumnInfo(name = "createTime") val createTime: String = "",
    @ColumnInfo(name = "defaultImageUrl") val defaultImageUrl: String = "",
    @ColumnInfo(name = "digest") val digest: String = "",
    @ColumnInfo(name = "isShow") val isShow: Int = -1,
    @ColumnInfo(name = "label") val label: List<String> = arrayListOf(),
    @ColumnInfo(name = "originalUrl") val originalUrl: String = "",
    @ColumnInfo(name = "qrCode") val qrCode: String = "",
    @ColumnInfo(name = "sourceFrom") val sourceFrom: String = "",
    @ColumnInfo(name = "timestamp") val timestamp: Long = -1,
    @ColumnInfo(name = "title") val title: String = "",
    @ColumnInfo(name = "totalCollection") val totalCollection: Long = -1,
    @ColumnInfo(name = "totalComments") val totalComments: Long = -1,
    @ColumnInfo(name = "totalVisits") val totalVisits: Long = -1,
    @ColumnInfo(name = "totalLikes") val totalLikes: Long = -1,
    @ColumnInfo(name = "completed") val isCompleted: Boolean = false,
)
