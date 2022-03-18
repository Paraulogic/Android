package com.arnyminerz.paraulogic.storage.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
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

    fun jsonObject(): JSONObject =
        JSONObject().apply {
            put("uid", uid)
            put("timestamp", timestamp)
            put("hash", hash)
            put("word", word)
            put("isCorrect", isCorrect)
        }
}
