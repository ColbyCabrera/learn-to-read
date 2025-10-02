package com.example.readingfoundations.data.local

import androidx.room.TypeConverter

class Converters {
    private val gson = com.google.gson.Gson()
    private val type = object : com.google.gson.reflect.TypeToken<List<String>>() {}.type
    @TypeConverter
    fun fromString(value: String?): List<String>? {
        return value?.let { gson.fromJson(it, type) }
    }
    @TypeConverter
    fun fromList(list: List<String>?): String? {
        return list?.let { gson.toJson(it, type) }
    }
}