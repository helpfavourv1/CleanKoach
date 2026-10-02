package com.zdmgold.cleankoach.core.data.db

import androidx.room.TypeConverter

class Converters {

    @TypeConverter
    fun fromLongList(value: List<Long>): String =
        value.joinToString(separator = ",")

    @TypeConverter
    fun toLongList(value: String): List<Long> =
        if (value.isBlank()) emptyList()
        else value.split(",").mapNotNull { it.toLongOrNull() }
}
