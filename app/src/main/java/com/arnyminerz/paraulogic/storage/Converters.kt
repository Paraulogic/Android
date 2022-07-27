package com.arnyminerz.paraulogic.storage

import androidx.room.TypeConverter
import com.arnyminerz.paraulogic.utils.toMap
import org.json.JSONObject

object Converters {
    @JvmStatic
    @TypeConverter
    @Suppress("UNCHECKED_CAST")
    fun fromString(value: String): Map<String, String> =
        JSONObject(value).toMap() as Map<String, String>

    @JvmStatic
    @TypeConverter
    fun fromMap(map: Map<String, String>): String =
        JSONObject(map).toString()
}