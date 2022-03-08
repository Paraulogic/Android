package com.arnyminerz.paraulogic.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.arnyminerz.paraulogic.game.GameInfo
import com.arnyminerz.paraulogic.game.decodeSource
import com.arnyminerz.paraulogic.game.fetchSource
import com.arnyminerz.paraulogic.singleton.DatabaseSingleton
import com.arnyminerz.paraulogic.storage.entity.IntroducedWord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.Calendar

class MainViewModel(application: Application) : AndroidViewModel(application) {
    val correctWords = mutableStateOf<List<IntroducedWord>>(emptyList())

    fun loadGameInfo() =
        mutableStateOf<GameInfo?>(null).apply {
            viewModelScope.launch {
                val source = fetchSource(getApplication())
                val gameInfo = decodeSource(source)
                value = gameInfo
            }
        }

    fun loadCorrectWords(gameInfo: GameInfo) {
        val databaseSingleton = DatabaseSingleton.getInstance(getApplication())
        viewModelScope.launch {
            val hash = gameInfo.hash()
            val dao = databaseSingleton.db.wordsDao()
            val correctWords = withContext(Dispatchers.IO) { dao.getAll() }
            correctWords.collect { list ->
                val newList = arrayListOf<IntroducedWord>()
                val lWords = arrayListOf<String>()
                list.sortedBy { it.word }
                    .forEach {
                        if (it.isCorrect && !lWords.contains(it.word) && it.hash == hash) {
                            newList.add(it)
                            lWords.add(it.word)
                        }
                    }
                this@MainViewModel.correctWords.value = newList
            }
        }
    }

    fun introduceWord(gameInfo: GameInfo, word: String, isCorrect: Boolean) {
        val databaseSingleton = DatabaseSingleton.getInstance(getApplication())
        viewModelScope.launch {
            val dao = databaseSingleton.db.wordsDao()
            val now = Calendar.getInstance().timeInMillis
            val hash = gameInfo.hash()
            withContext(Dispatchers.IO) {
                dao.put(
                    IntroducedWord(0, now, hash, word, isCorrect)
                )
            }
            Timber.i("Stored word: $word. Correct: $isCorrect")
        }
    }
}
