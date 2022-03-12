package com.arnyminerz.paraulogic.ui.viewmodel

import android.app.Application
import androidx.annotation.UiThread
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.arnyminerz.paraulogic.game.GameHistoryItem
import com.arnyminerz.paraulogic.game.GameInfo
import com.arnyminerz.paraulogic.game.calculatePoints
import com.arnyminerz.paraulogic.game.getLevelFromPoints
import com.arnyminerz.paraulogic.game.getTutis
import com.arnyminerz.paraulogic.game.loadGameInfoFromServer
import com.arnyminerz.paraulogic.singleton.DatabaseSingleton
import com.arnyminerz.paraulogic.storage.entity.IntroducedWord
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.Calendar
import java.util.Date

class MainViewModel(application: Application) : AndroidViewModel(application) {
    var gameInfo by mutableStateOf<GameInfo?>(null)
        private set

    val correctWords = mutableStateListOf<IntroducedWord>()

    var points by mutableStateOf(0)
        private set
    var level by mutableStateOf(0)
        private set
    val introducedTutis = mutableStateListOf<IntroducedWord>()

    val gameHistory = mutableStateListOf<GameHistoryItem>()
    var dayFoundWords by mutableStateOf<List<IntroducedWord>>(emptyList())
        private set
    var dayFoundTutis by mutableStateOf<List<IntroducedWord>>(emptyList())
        private set
    var dayWrongWords by mutableStateOf<Map<String, Int>>(emptyMap())
        private set

    fun loadGameInfo() {
        viewModelScope.launch {
            val gameInfo = loadGameInfoFromServer()
            this@MainViewModel.gameInfo = gameInfo

            loadCorrectWords(gameInfo)
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun loadGameHistory() {
        Timber.d("Getting Firestore instance.")
        val db = try {
            Firebase.firestore
        } catch (e: IllegalStateException) {
            Timber.w("FirebaseApp not initialized. Initializing...")
            Firebase.initialize(getApplication())
            Firebase.firestore
        }

        viewModelScope.launch {
            Timber.d("Loading game history...")
            db.collection("paraulogic")
                .get()
                .addOnSuccessListener { snapshot ->
                    Timber.d("Got ${snapshot.documents.size} documents.")
                    gameHistory.clear()
                    for (document in snapshot.documents) {
                        val timestamp = document.getTimestamp("timestamp") ?: continue

                        val date = timestamp.toDate()
                        val gameInfoObject = GameInfo.fromServer(document)

                        gameHistory.add(GameHistoryItem(date, gameInfoObject))
                        Timber.i("Added game from $date.")
                    }
                }
                .addOnFailureListener { error ->
                    Timber.e(error, "Could not get history.")
                }
        }
    }

    @UiThread
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

    fun loadWordsForDay(gameInfo: GameInfo, date: Date, includeWrongWords: Boolean = false) {
        viewModelScope.launch {
            val dateCalendar = Calendar.getInstance()
            dateCalendar.time = date

            val databaseSingleton = DatabaseSingleton.getInstance(getApplication())
            val dao = databaseSingleton.db.wordsDao()
            val dbWords = withContext(Dispatchers.IO) { dao.getAll() }
            val tempDayFoundWords = dbWords
                .first()
                .filter { word ->
                    val wordDate = Date(word.timestamp)
                    val wordCalendar = Calendar.getInstance()
                    wordCalendar.time = wordDate

                    wordCalendar.get(Calendar.YEAR) == dateCalendar.get(Calendar.YEAR) &&
                            wordCalendar.get(Calendar.MONTH) == dateCalendar.get(Calendar.MONTH) &&
                            wordCalendar.get(Calendar.DAY_OF_MONTH) == dateCalendar.get(Calendar.DAY_OF_MONTH) &&
                            word.isCorrect || includeWrongWords
                }
            dayFoundTutis = tempDayFoundWords
                .filter { gameInfo.isTuti(it.word) }

            val wrongWords = hashMapOf<String, Int>()
            dbWords
                .first()
                .forEach { word ->
                    wrongWords[word.word] =
                        if (wrongWords.contains(word.word))
                            wrongWords.getValue(word.word) + 1
                        else
                            1
                }
            dayWrongWords = wrongWords

            dayFoundWords = tempDayFoundWords
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
