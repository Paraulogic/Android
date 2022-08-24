package com.arnyminerz.paraulogic.ui.viewmodel

import android.app.Activity
import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.annotation.WorkerThread
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.arnyminerz.paraulogic.App
import com.arnyminerz.paraulogic.annotation.LoadError
import com.arnyminerz.paraulogic.annotation.LoadError.Companion.RESULT_NO_SUCH_ELEMENT
import com.arnyminerz.paraulogic.annotation.LoadError.Companion.RESULT_OK
import com.arnyminerz.paraulogic.broadcast.ACTION_UPDATE_GAME_DATA
import com.arnyminerz.paraulogic.game.GameInfo
import com.arnyminerz.paraulogic.game.calculatePoints
import com.arnyminerz.paraulogic.game.fetchAndStoreGameInfo
import com.arnyminerz.paraulogic.game.gameInfoForToday
import com.arnyminerz.paraulogic.game.getLevelFromPoints
import com.arnyminerz.paraulogic.game.getServerIntroducedWordsList
import com.arnyminerz.paraulogic.game.getTutis
import com.arnyminerz.paraulogic.game.loadGameHistoryFromServer
import com.arnyminerz.paraulogic.play.games.loadGameProgress
import com.arnyminerz.paraulogic.play.games.startSynchronization
import com.arnyminerz.paraulogic.play.games.storeGameProgress
import com.arnyminerz.paraulogic.pref.PreferencesModule
import com.arnyminerz.paraulogic.pref.dataStore
import com.arnyminerz.paraulogic.singleton.DatabaseSingleton
import com.arnyminerz.paraulogic.storage.entity.IntroducedWord
import com.arnyminerz.paraulogic.utils.doAsync
import com.arnyminerz.paraulogic.utils.ioContext
import com.arnyminerz.paraulogic.utils.uiContext
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.games.GamesSignInClient
import com.google.android.gms.games.PlayGames
import com.google.android.gms.games.Player
import com.google.firebase.FirebaseException
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import com.google.firebase.perf.metrics.AddTrace
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    val gameHistory = mutableStateListOf<GameInfo>()
    var dayFoundWords by mutableStateOf<List<IntroducedWord>>(emptyList())
        private set
    var dayFoundTutis by mutableStateOf<List<IntroducedWord>>(emptyList())
        private set
    var dayWrongWords by mutableStateOf<Map<String, Int>>(emptyMap())
        private set

    var isAuthenticated by mutableStateOf(false)
        private set
    var player by mutableStateOf<Player?>(null)
        private set

    val prefNumberOfLaunches = getApplication<App>()
        .dataStore
        .data
        .map { it[PreferencesModule.NumberOfLaunches] ?: 0 }

    val prefDisableDonationDialog = getApplication<App>()
        .dataStore
        .data
        .map { it[PreferencesModule.DisableDonationDialog] ?: false }

    /**
     * Used for fetching the authentication state of the player.
     * @author Arnau Mora
     * @since 20220824
     */
    lateinit var signInClient: GamesSignInClient

    /**
     * Should be registered for receiving broadcasts of [ACTION_UPDATE_GAME_DATA].
     * @author Arnau Mora
     * @since 20220824
     */
    val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            Timber.i("Got notified of new data. Attempting update...")
            doAsync { attemptGameInfoUpdate(context) }
        }
    }

    /**
     * Gets the [GameInfo] for today. If it's not null, it means it has already been loaded from the
     * server, so [gameInfo] is updated. Hashes are also checked, just in case there's a confusion
     * by overlapping threads.
     * @author Arnau Mora
     * @since 20220820
     * @param context The context running on.
     * @see gameInfoForToday
     */
    @WorkerThread
    suspend fun attemptGameInfoUpdate(context: Context) {
        val gameInfo = gameInfoForToday(context)
        if (gameInfo != null && gameInfo.hash != this.gameInfo?.hash) {
            Timber.i("Game info got updated. Refreshing UI...")
            uiContext { this@MainViewModel.gameInfo = gameInfo }
        } else
            Timber.d("There's no new game data available.")
    }

    private fun loadPlayer(activity: Activity) {
        Timber.d("Loading player data...")
        PlayGames.getPlayersClient(activity)
            .currentPlayer
            .addOnSuccessListener {
                Timber.d("Loaded player data.")
                player = it
            }
            .addOnFailureListener { Timber.e(it, "Could not load player data.") }
    }

    /**
     * Loads the current authentication state, and the player's data.
     *
     * Updates [isAuthenticated] and [player] accordingly.
     * @author Arnau Mora
     * @since 20220824
     * @param activity The [Activity] that is requesting the load.
     */
    fun loadAuthenticatedState(activity: Activity) {
        Timber.d("Checking if client is authenticated...")
        signInClient
            .isAuthenticated
            .addOnSuccessListener { result ->
                isAuthenticated = result.isAuthenticated
                Timber.i("Is user authenticated: $isAuthenticated")
                if (isAuthenticated)
                    viewModelScope.launch {
                        ioContext {
                            loadPlayer(activity)
                            loadGameProgress(activity)
                        }
                    }
            }
            .addOnFailureListener {
                Timber.e(it, "Could not get authenticated state.")
            }
    }

    fun loadGameInfo(
        activity: Activity,
        @WorkerThread loadingGameProgressCallback: suspend (finished: Boolean) -> Unit,
    ) {
        viewModelScope.launch {
            Timber.v("Resetting error flag...")
            error = RESULT_OK

            Timber.v("Checking if tried to sign in ever...")
            val databaseSingleton = DatabaseSingleton.getInstance(activity)

            doAsync {
                Timber.v("Adding collector for words...")
                databaseSingleton
                    .db
                    .wordsDao()
                    .getAll()
                    .collect { wordsList ->
                        Timber.i("Introduced new word. Storing progress...")
                        try {
                            storeGameProgress(activity, wordsList)
                        } catch (e: ApiException) {
                            if (e.statusCode == 4)
                                Timber.e(e, "Could not store progress since user is not logged in.")
                            else
                                Timber.e(e, "Could not store progress.")
                        }
                    }
            }

            ioContext {
                val gameInfo = gameInfoForToday(getApplication())
                    ?:
                    // If the data could not be loaded, fetch from server.
                    try {
                        fetchAndStoreGameInfo(getApplication())
                    } catch (e: NoSuchElementException) {
                        Timber.e(e, "Could not get game info from server.")
                        error = RESULT_NO_SUCH_ELEMENT
                        return@ioContext
                    }

                this@MainViewModel.gameInfo = gameInfo

                Timber.d("Loading words from server...")
                val serverIntroducedWordsList = getServerIntroducedWordsList(
                    activity,
                    gameInfo,
                    loadingGameProgressCallback,
                )
                Timber.d("Got ${serverIntroducedWordsList.size} words from server.")

                loadCorrectWords(gameInfo, serverIntroducedWordsList)
            }
        }
    }

    @AddTrace(name = "GameHistoryLoad")
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
     * @param activity The Activity to launch the synchronization from.
     * @param gameInfo The [GameInfo] instance of the currently playing game.
     * @param history The history of all the games.
     */
    fun synchronize(
        activity: Activity,
        gameInfo: GameInfo,
        history: List<GameInfo>,
    ) {
        viewModelScope.launch(context = Dispatchers.IO) {
            startSynchronization(activity, gameInfo, history)
        }
    }

    @WorkerThread
    @AddTrace(name = "CorrectWordsLoad")
    private suspend fun loadCorrectWords(
        gameInfo: GameInfo,
        serverIntroducedWordsList: List<IntroducedWord>,
    ) {
        val databaseSingleton = DatabaseSingleton.getInstance(getApplication())
        val hash = gameInfo.hash
        val dao = databaseSingleton.db.wordsDao()
        ioContext { dao.getAll() }
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

                uiContext {
                    points = correctWords.calculatePoints(gameInfo)
                    level = getLevelFromPoints(points, gameInfo.pointsPerLevel)

                    introducedTutis.clear()
                    introducedTutis.addAll(correctWords.getTutis(gameInfo))
                }
            }
    }

    @AddTrace(name = "DailyWordsLoad")
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
            Firebase.analytics
                .logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                    param(
                        FirebaseAnalytics.Param.ITEM_NAME,
                        word,
                    )
                    param(
                        FirebaseAnalytics.Param.CONTENT_TYPE,
                        if (isCorrect) "correct" else "wrong",
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
