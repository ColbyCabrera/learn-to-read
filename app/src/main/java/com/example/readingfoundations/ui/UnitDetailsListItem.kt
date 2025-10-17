package com.example.readingfoundations.ui

import com.example.readingfoundations.data.ContentType
import com.example.readingfoundations.data.Level

sealed class UnitDetailsListItem {
    data class Header(val contentType: ContentType) : UnitDetailsListItem()
    data class LevelItem(val level: Level, val contentType: ContentType) : UnitDetailsListItem()
}