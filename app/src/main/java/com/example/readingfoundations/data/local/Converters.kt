package com.example.readingfoundations.data.local

import androidx.room.TypeConverter
import com.example.readingfoundations.data.models.Level
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    // Converters for List<Level>
    private val levelListType = object : TypeToken<List<Level>>() {}.type

    @TypeConverter
    fun fromLevelList(levels: List<Level>?): String? {
        return gson.toJson(levels, levelListType)
    }

    @TypeConverter
    fun toLevelList(json: String?): List<Level>? {
        return gson.fromJson(json, levelListType)
    }

    // Converters for List<String> (for PunctuationQuestion options)
    private val stringListType = object : TypeToken<List<String>>() {}.type

    @TypeConverter
    fun fromStringList(value: String?): List<String>? {
        return value?.let { gson.fromJson(it, stringListType) }
    }

    @TypeConverter
    fun toStringList(list: List<String>?): String? {
        return list?.let { gson.toJson(it, stringListType) }
    }
}