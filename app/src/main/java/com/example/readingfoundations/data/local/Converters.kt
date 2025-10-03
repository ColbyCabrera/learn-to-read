package com.example.readingfoundations.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()
    private val listStringType = object : TypeToken<List<String>>() {}.type
    private val mapIntIntType = object : TypeToken<Map<Int, Int>>() {}.type

    @TypeConverter
    fun fromString(value: String?): List<String>? {
        return value?.let { gson.fromJson(it, listStringType) }
    }

    @TypeConverter
    fun fromList(list: List<String>?): String? {
        return list?.let { gson.toJson(it, listStringType) }
    }

    @TypeConverter
    fun fromStringToMap(value: String?): Map<Int, Int>? {
        return value?.let { gson.fromJson(it, mapIntIntType) }
    }

    @TypeConverter
    fun fromMapToString(map: Map<Int, Int>?): String? {
        return map?.let { gson.toJson(it, mapIntIntType) }
    }
}