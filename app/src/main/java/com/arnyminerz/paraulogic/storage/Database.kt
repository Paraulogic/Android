package com.arnyminerz.paraulogic.storage

import androidx.room.Database
import androidx.room.RoomDatabase
import com.arnyminerz.paraulogic.storage.dao.WordsDao
import com.arnyminerz.paraulogic.storage.entity.IntroducedWord
import com.arnyminerz.paraulogic.storage.entity.SynchronizedWord

@Database(
    version = 2,
    entities = [IntroducedWord::class, SynchronizedWord::class],
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun wordsDao(): WordsDao
}
