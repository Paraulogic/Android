package com.arnyminerz.paraulogic.storage.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "SynchronizedWords")
data class SynchronizedWord(
    @PrimaryKey(autoGenerate = true) val uid: Int,
    val wordId: Int,
)
