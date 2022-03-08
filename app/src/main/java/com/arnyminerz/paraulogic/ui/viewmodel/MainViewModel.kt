package com.arnyminerz.paraulogic.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.arnyminerz.paraulogic.game.GameInfo
import com.arnyminerz.paraulogic.game.calculatePoints
import com.arnyminerz.paraulogic.game.decodeSource
import com.arnyminerz.paraulogic.game.fetchSource
import com.arnyminerz.paraulogic.game.getLevelFromPoints
import com.arnyminerz.paraulogic.game.getTutis
import com.arnyminerz.paraulogic.singleton.DatabaseSingleton
import com.arnyminerz.paraulogic.storage.entity.IntroducedWord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.Calendar

class MainViewModel(application: Application) : AndroidViewModel(application) {
    var gameInfo by mutableStateOf<GameInfo?>(null)
        private set

    val correctWords = mutableStateListOf<IntroducedWord>()

    var points by mutableStateOf(0)
        private set
    var level by mutableStateOf(0)
        private set
    val introducedTutis = mutableStateListOf<IntroducedWord>()

    fun loadGameInfo() {
        viewModelScope.launch {
            val source = fetchSource(getApplication())
            val gameInfo = decodeSource(source)
            this@MainViewModel.gameInfo = decodeSource(source)

            loadCorrectWords(gameInfo)
        }
    }

    private suspend fun loadCorrectWords(gameInfo: GameInfo) {
        val databaseSingleton = DatabaseSingleton.getInstance(getApplication())
        val hash = gameInfo.hash
        val dao = databaseSingleton.db.wordsDao()
        val dbCorrectWords = withContext(Dispatchers.IO) { dao.getAll() }
        dbCorrectWords.collect { list ->
            correctWords.clear()

            val lWords = arrayListOf<String>()
            list.sortedBy { it.word }
                .forEach {
                    if (it.isCorrect && !lWords.contains(it.word) && it.hash == hash) {
                        correctWords.add(it)
                        lWords.add(it.word)
                    }
                }
            points = correctWords.calculatePoints(gameInfo)
            level = getLevelFromPoints(points, gameInfo.pointsPerLevel)

            introducedTutis.clear()
            introducedTutis.addAll(correctWords.getTutis(gameInfo))
        }
    }

    fun introduceWord(gameInfo: GameInfo, word: String, isCorrect: Boolean) {
        val databaseSingleton = DatabaseSingleton.getInstance(getApplication())
        viewModelScope.launch {
            val dao = databaseSingleton.db.wordsDao()
            val now = Calendar.getInstance().timeInMillis
            val hash = gameInfo.hash
            withContext(Dispatchers.IO) {
                dao.put(
                    IntroducedWord(0, now, hash, word, isCorrect)
                )
            }
            Timber.i("Stored word: $word. Correct: $isCorrect")
        }
    }
}
