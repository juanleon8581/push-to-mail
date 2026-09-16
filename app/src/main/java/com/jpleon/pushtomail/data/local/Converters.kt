package com.jpleon.pushtomail.data.local

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromPackageList(value: List<String>): String = value.joinToString(",")

    @TypeConverter
    fun toPackageList(value: String): List<String> =
        if (value.isBlank()) emptyList() else value.split(",")
}
