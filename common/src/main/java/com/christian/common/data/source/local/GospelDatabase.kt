package com.christian.common.data.source.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.christian.common.data.Converters
import com.christian.common.data.Gospel

@Database(entities = [Gospel::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class GospelDatabase : RoomDatabase() {
    abstract fun writingDao(): GospelDao
}