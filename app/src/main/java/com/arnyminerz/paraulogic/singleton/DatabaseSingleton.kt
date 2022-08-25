package com.arnyminerz.paraulogic.singleton

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.arnyminerz.paraulogic.storage.AppDatabase

const val DATABASE_NAME = "ParaulogicDB"

class DatabaseSingleton constructor(context: Context) {
    companion object {
        @Volatile
        private var INSTANCE: DatabaseSingleton? = null

        fun getInstance(context: Context) =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: DatabaseSingleton(context).also {
                    INSTANCE = it
                }
            }
    }

    val appDatabase: AppDatabase by lazy {
        Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, DATABASE_NAME)
            .addMigrations(
                object : Migration(1, 2) {
                    override fun migrate(database: SupportSQLiteDatabase) {
                        database.execSQL(
                            """
                            CREATE TABLE `SynchronizedWords` (
                                `uid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                                `wordId` INTEGER NOT NULL
                            )
                        """.trimIndent()
                        )
                    }
                }
            )
            .build()
    }
}
