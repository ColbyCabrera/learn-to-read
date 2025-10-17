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

    /**
     * Serializes a Map<Int, Int> to its JSON string representation for Room storage.
     *
     * @param map The map to serialize; when `null`, no serialization is performed.
     * @return The JSON string representing `map`, or `null` if `map` is `null`.
     */
    @TypeConverter
    fun fromMapToString(map: Map<Int, Int>?): String? {
        return map?.let { gson.toJson(it, mapIntIntType) }
    }

    private val mapStringListIntType = object : TypeToken<Map<String, List<Int>>>() {}.type

    /**
     * Converts a JSON string into a map from strings to lists of integers.
     *
     * @param value JSON representation of the map, or null.
     * @return The parsed `Map<String, List<Int>>`, or `null` if `value` is null.
     */
    @TypeConverter
    fun fromStringToMapStringListInt(value: String?): Map<String, List<Int>>? {
        return value?.let { gson.fromJson(it, mapStringListIntType) }
    }

    /**
     * Converts a Map<String, List<Int>> to its JSON string representation.
     *
     * @param map The map to serialize; may be null.
     * @return The JSON string representation of the map, or `null` if `map` is null.
     */
    @TypeConverter
    fun fromMapStringListIntToString(map: Map<String, List<Int>>?): String? {
        return map?.let { gson.toJson(it, mapStringListIntType) }
    }

    /**
     * Convert a QuestionType enum to its string name.
     *
     * @param questionType The QuestionType to convert.
     * @return The enum constant's name as a String.
     */
    @TypeConverter
    fun fromQuestionType(questionType: com.example.readingfoundations.data.models.QuestionType): String {
        return questionType.name
    }

    @TypeConverter
    fun toQuestionType(value: String): com.example.readingfoundations.data.models.QuestionType {
        return com.example.readingfoundations.data.models.QuestionType.valueOf(value)
    }
}