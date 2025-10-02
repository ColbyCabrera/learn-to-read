package com.example.readingfoundations.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    @TypeConverter
    fun fromString(value: String?): List<String>? {
        return value?.let { gson.fromJson(it, type) }
    }
    @TypeConverter
    fun fromList(list: List<String>?): String? {
        return list?.let { gson.toJson(it, type) }
    }

    companion object {
        private val gson = Gson()
        private val type = object : TypeToken<List<String>>() {}.type
    }
}