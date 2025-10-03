package com.example.readingfoundations.data.local

import androidx.room.TypeConverter
import com.example.readingfoundations.data.models.Level
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()
    private val type = object : TypeToken<List<Level>>() {}.type

    @TypeConverter
    fun fromLevelList(levels: List<Level>?): String? {
        return gson.toJson(levels, type)
    }

    @TypeConverter
    fun toLevelList(json: String?): List<Level>? {
        return gson.fromJson(json, type)
    }
}