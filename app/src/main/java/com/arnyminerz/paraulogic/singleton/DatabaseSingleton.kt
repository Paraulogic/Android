package com.arnyminerz.paraulogic.singleton

import android.content.Context
import androidx.room.Room
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

    val db: AppDatabase by lazy {
        Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, DATABASE_NAME)
            .build()
    }
}
