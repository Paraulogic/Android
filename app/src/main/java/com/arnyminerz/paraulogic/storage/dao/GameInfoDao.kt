package com.arnyminerz.paraulogic.storage.dao

import androidx.annotation.WorkerThread
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.arnyminerz.paraulogic.storage.entity.GameInfoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GameInfoDao {
    @WorkerThread
    @Query("SELECT * FROM GameInfoList ORDER BY date")
    fun getAll(): Flow<List<GameInfoEntity>>

    @WorkerThread
    @Insert
    fun put(gameInfo: GameInfoEntity)
}