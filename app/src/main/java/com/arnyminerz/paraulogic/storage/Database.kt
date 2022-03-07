package com.arnyminerz.paraulogic.storage

import androidx.room.Database
import androidx.room.RoomDatabase
import com.arnyminerz.paraulogic.storage.dao.WordsDao
import com.arnyminerz.paraulogic.storage.entity.IntroducedWord

@Database(entities = [IntroducedWord::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun wordsDao(): WordsDao
}
