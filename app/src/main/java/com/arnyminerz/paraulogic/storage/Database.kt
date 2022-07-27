package com.arnyminerz.paraulogic.storage

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.arnyminerz.paraulogic.storage.dao.GameInfoDao
import com.arnyminerz.paraulogic.storage.dao.WordsDao
import com.arnyminerz.paraulogic.storage.entity.GameInfoEntity
import com.arnyminerz.paraulogic.storage.entity.IntroducedWord
import com.arnyminerz.paraulogic.storage.entity.SynchronizedWord

@Database(
    version = 3,
    entities = [IntroducedWord::class, SynchronizedWord::class, GameInfoEntity::class],
    autoMigrations = [
        AutoMigration(from = 2, to = 3),
    ],
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun wordsDao(): WordsDao

    abstract fun gameInfoDao(): GameInfoDao
}
