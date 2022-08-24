package com.arnyminerz.paraulogic.storage.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONArray
import org.json.JSONObject

@Entity
data class IntroducedWord(
    @PrimaryKey(autoGenerate = true) val uid: Int,
    @ColumnInfo(name = "timestamp") val timestamp: Long,
    @ColumnInfo(name = "hash") val hash: String,
    @ColumnInfo(name = "word") val word: String,
    @ColumnInfo(name = "isCorrect") val isCorrect: Boolean,
) {
    constructor(jsonObject: JSONObject) : this(
        jsonObject.getInt("uid"),
        jsonObject.getLong("timestamp"),
        jsonObject.getString("hash"),
        jsonObject.getString("word"),
        jsonObject.getBoolean("isCorrect"),
    )

    constructor(array: JSONArray) : this(
        array.getInt(0),
        array.getLong(1),
        array.getString(2),
        array.getString(3),
        when (val correct = array.get(4)) {
            is Int -> correct == 1
            is Boolean -> correct
            else -> correct.toString().toBoolean() // Won't get called, but keep just in case
        },
    )

    fun simplifiedJson(): JSONArray = JSONArray().apply {
        put(uid)
        put(timestamp)
        put(hash)
        put(word)
        put(if (isCorrect) 1 else 0)
    }

    fun jsonObject(): JSONObject =
        JSONObject().apply {
            put("uid", uid)
            put("timestamp", timestamp)
            put("hash", hash)
            put("word", word)
            put("isCorrect", isCorrect)
        }
}
