package com.arnyminerz.paraulogic.storage.dao

import androidx.annotation.WorkerThread
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.arnyminerz.paraulogic.storage.entity.IntroducedWord
import kotlinx.coroutines.flow.Flow

@Dao
interface WordsDao {
    @WorkerThread
    @Query("SELECT * FROM IntroducedWord")
    fun getAll(): Flow<List<IntroducedWord>>

    @WorkerThread
    @Query("SELECT * FROM IntroducedWord WHERE isCorrect='true'")
    fun getAllCorrect(): Flow<List<IntroducedWord>>

    @WorkerThread
    @Insert
    fun put(vararg words: IntroducedWord)

    @WorkerThread
    @Delete
    fun delete(word: IntroducedWord)

    @WorkerThread
    @Update
    fun update(word: IntroducedWord)
}
