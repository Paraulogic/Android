package com.arnyminerz.paraulogic.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.arnyminerz.paraulogic.App
import com.arnyminerz.paraulogic.annotation.LoadError
import com.arnyminerz.paraulogic.annotation.LoadError.Companion.RESULT_FIREBASE_EXCEPTION
import com.arnyminerz.paraulogic.annotation.LoadError.Companion.RESULT_NO_SUCH_ELEMENT
import com.arnyminerz.paraulogic.annotation.LoadError.Companion.RESULT_OK
import com.arnyminerz.paraulogic.game.GameHistoryItem
import com.arnyminerz.paraulogic.game.GameInfo
import com.arnyminerz.paraulogic.game.calculatePoints
import com.arnyminerz.paraulogic.game.getLevelFromPoints
import com.arnyminerz.paraulogic.game.getServerIntroducedWordsList
import com.arnyminerz.paraulogic.game.getTutis
import com.arnyminerz.paraulogic.game.loadGameHistoryFromServer
import com.arnyminerz.paraulogic.game.loadGameInfoFromServer
import com.arnyminerz.paraulogic.play.games.loadSnapshot
import com.arnyminerz.paraulogic.play.games.startSignInIntent
import com.arnyminerz.paraulogic.play.games.startSynchronization
import com.arnyminerz.paraulogic.play.games.writeSnapshot
import com.arnyminerz.paraulogic.pref.PreferencesModule
import com.arnyminerz.paraulogic.pref.dataStore
import com.arnyminerz.paraulogic.singleton.DatabaseSingleton
import com.arnyminerz.paraulogic.storage.entity.IntroducedWord
import com.arnyminerz.paraulogic.utils.doAsync
import com.arnyminerz.paraulogic.utils.ioContext
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.tasks.RuntimeExecutionException
import com.google.firebase.FirebaseException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import timber.log.Timber
import java.util.Calendar
import java.util.Date

class MainViewModel(application: Application) : AndroidViewModel(application) {
    var gameInfo by mutableStateOf<GameInfo?>(null)
        private set

    val correctWords = mutableStateListOf<IntroducedWord>()

    /**
     * Specifies if an error has happened.
     * List:
     * * <pre>0</pre>: No error
     * * <pre>1</pre>: [NoSuchElementException]
     * * <pre>2</pre>: [FirebaseException]
     * @author Arnau Mora
     * @since 20220320
     */
    var error by mutableStateOf<@LoadError Int>(0)
        private set

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

    fun loadGameInfo(
        signInClient: GoogleSignInClient,
        signInLauncher: ActivityResultLauncher<Intent>,
        @WorkerThread loadingGameProgressCallback: (finished: Boolean) -> Unit,
    ) {
        viewModelScope.launch {
            /*if (correctWords.isNotEmpty()) {
                Timber.i("Tried to load GameInfo when already loaded.")
                return@launch
            }*/

            Timber.v("Resetting error flag...")
            error = RESULT_OK

            Timber.v("Checking if tried to sign in ever...")
            val context = getApplication<App>()
            val dataStore = context.dataStore
            if (dataStore.data.first()[PreferencesModule.TriedToSignIn] != true) {
                Timber.i("Never shown sign in intent. Showing...")
                startSignInIntent(signInClient, signInLauncher)
                Timber.i("Updating tried to sign in to true...")
                dataStore.edit { it[PreferencesModule.TriedToSignIn] = true }
            }

            doAsync {
                Timber.v("Adding collector for words...")
                DatabaseSingleton.getInstance(context)
                    .db
                    .wordsDao()
                    .getAll()
                    .collect { wordsList ->
                        Timber.i("Introduced new word.")
                        GoogleSignIn
                            .getLastSignedInAccount(context)
                            ?.let { account ->
                                Timber.i("Saving game progress...")
                                Timber.d("Decoding words list...")
                                val array = JSONArray()
                                wordsList.forEachIndexed { i, t -> array.put(i, t.jsonObject()) }
                                val serializedString = array.toString()
                                // Timber.v("Progress json: $serializedString")
                                try {
                                    Timber.d("Loading snapshot for account...")
                                    val snapshot = context.loadSnapshot(account)
                                    if (snapshot != null) {
                                        Timber.d("Writing snapshot...")
                                        val snapshotMetadata = context.writeSnapshot(
                                            account,
                                            snapshot,
                                            serializedString.toByteArray(Charsets.UTF_8),
                                            null,
                                            "Paraulogic game save",
                                        )
                                        Timber.i("Saved game for ${snapshotMetadata.title}")
                                    } else
                                        Timber.w("Could not write snapshot since not available on server.")
                                } catch (e: IllegalStateException) {
                                    Timber.e(e, "Could not load snapshot.")
                                    startSignInIntent(signInClient, signInLauncher)
                                } catch (e: RuntimeExecutionException) {
                                    Timber.e(e, "There's no stored snapshot.")
                                }
                            } ?: run { Timber.w("User not logged in") }
                    }
            }

            val gameInfo = try {
                loadGameInfoFromServer(getApplication())
            } catch (e: NoSuchElementException) {
                Timber.e(e, "Could not get game info from server.")
                error = RESULT_NO_SUCH_ELEMENT
                return@launch
            } catch (e: FirebaseException) {
                Timber.e(e, "Could not get game info from server.")
                error = RESULT_FIREBASE_EXCEPTION
                return@launch
            }
            this@MainViewModel.gameInfo = gameInfo

            Timber.d("Loading words from server...")
            val serverIntroducedWordsList = getServerIntroducedWordsList(
                getApplication(),
                gameInfo,
                loadingGameProgressCallback,
            )
            Timber.d("Got ${serverIntroducedWordsList.size} words from server.")

            loadCorrectWords(gameInfo, serverIntroducedWordsList)
        }
    }

    fun loadGameHistory() {
        viewModelScope.launch {
            Timber.d("Loading game history...")
            try {
                gameHistory.clear()
                loadGameHistoryFromServer(getApplication()) { gameHistory.add(it) }
            } catch (e: NoSuchElementException) {
                Timber.e(e, "Data from server is not valid.")
            } catch (e: FirebaseException) {
                Timber.e(e, "Could not load data from server.")
            }
        }
    }

    /**
     * Runs [startSynchronization] in the view model scope.
     * @author Arnau Mora
     * @since 20220323
     * @param context The context to launch the synchronization from.
     * @param gameInfo The [GameInfo] instance of the currently playing game.
     * @param history The history of all the games.
     */
    fun synchronize(
        context: Context,
        gameInfo: GameInfo,
        history: List<GameHistoryItem>,
    ) {
        viewModelScope.launch(context = Dispatchers.IO) {
            startSynchronization(context, gameInfo, history)
        }
    }

    @UiThread
    private suspend fun loadCorrectWords(
        gameInfo: GameInfo,
        serverIntroducedWordsList: List<IntroducedWord>,
    ) {
        val databaseSingleton = DatabaseSingleton.getInstance(getApplication())
        val hash = gameInfo.hash
        val dao = databaseSingleton.db.wordsDao()
        withContext(Dispatchers.IO) { dao.getAll() }
            .collect { list ->
                correctWords.clear()
                correctWords.addAll(
                    listOf(
                        serverIntroducedWordsList,
                        list.filter { !serverIntroducedWordsList.contains(it) }
                    )
                        .flatten()
                        .filter { it.isCorrect && it.hash == hash }
                )

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
            val dbWords = ioContext { dao.getAll() }
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
                .filter { it.hash == gameInfo.hash }
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

    fun introduceWord(
        gameInfo: GameInfo,
        word: String,
        isCorrect: Boolean,
    ) {
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

    class Factory(private val application: Application) : ViewModelProvider.NewInstanceFactory() {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainViewModel(application) as T
        }
    }
}
