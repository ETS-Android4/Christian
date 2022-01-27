package com.christian.common.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.ArrayList

class Converters {
    @TypeConverter
    fun fromArray(list: List<String>): String {
        return Gson().toJson(list)
    }

    @TypeConverter
    fun toArray(s: String): List<String> {
        return Gson().fromJson(s, object : TypeToken<List<String>>() {}.type)
    }
}