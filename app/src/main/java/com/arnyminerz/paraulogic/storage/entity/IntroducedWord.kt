package com.arnyminerz.paraulogic.storage.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class IntroducedWord(
    @PrimaryKey(autoGenerate = true) val uid: Int,
    @ColumnInfo(name = "timestamp") val timestamp: Long,
    @ColumnInfo(name = "hash") val hash: String,
    @ColumnInfo(name = "word") val word: String,
    @ColumnInfo(name = "isCorrect") val isCorrect: Boolean,
)
