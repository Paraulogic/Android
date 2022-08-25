package com.arnyminerz.paraulogic.storage

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.DeleteTable
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.AutoMigrationSpec
import com.arnyminerz.paraulogic.storage.dao.GameInfoDao
import com.arnyminerz.paraulogic.storage.dao.WordsDao
import com.arnyminerz.paraulogic.storage.entity.GameInfoEntity
import com.arnyminerz.paraulogic.storage.entity.IntroducedWord

@Database(
    version = 4,
    entities = [IntroducedWord::class, GameInfoEntity::class],
    autoMigrations = [
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4, spec = AppDatabase.AutoMigrate3to4Spec::class),
    ],
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun wordsDao(): WordsDao

    abstract fun gameInfoDao(): GameInfoDao

    @DeleteTable(tableName = "SynchronizedWords")
    class AutoMigrate3to4Spec : AutoMigrationSpec
}
