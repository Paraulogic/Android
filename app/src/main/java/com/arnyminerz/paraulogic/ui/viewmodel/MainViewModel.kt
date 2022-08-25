package com.arnyminerz.paraulogic.ui.viewmodel

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import androidx.compose.runtime.getValue
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
import com.arnyminerz.paraulogic.play.games.incrementAchievements
import com.arnyminerz.paraulogic.play.games.storeGameProgress
import com.arnyminerz.paraulogic.play.games.synchronizeAchievements
import com.arnyminerz.paraulogic.pref.PrefDisableDonationDialog
import com.arnyminerz.paraulogic.pref.PrefNumberOfLaunches
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
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import com.google.firebase.perf.metrics.AddTrace
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.Calendar
import java.util.Date

class MainViewModel(activity: Activity) : AndroidViewModel(activity.application) {
    var gameInfo by mutableStateOf<GameInfo?>(null)
        @UiThread
        private set

    /**
     * Stores all the words the user has introduced and were correct for the current [GameInfo].
     * @author Arnau Mora
     * @since 20220825
     */
    var correctWords by mutableStateOf(emptyList<IntroducedWord>())
        @UiThread
        private set

    /**
     * Specifies if an error has happened.
     * List:
     * * [RESULT_OK]: No error
     * * [RESULT_NO_SUCH_ELEMENT]: [NoSuchElementException]
     * @author Arnau Mora
     * @since 20220320
     */
    var error by mutableStateOf<@LoadError Int>(0)
        @UiThread
        private set

    var points by mutableStateOf(0)
        @UiThread
        private set
    var level by mutableStateOf(0)
        @UiThread
        private set
    var introducedTutis by mutableStateOf(emptyList<IntroducedWord>())
        @UiThread
        private set

    var gameHistory by mutableStateOf(emptyList<GameInfo>())
        @UiThread
        private set
    var dayFoundWords by mutableStateOf(emptyList<IntroducedWord>())
        @UiThread
        private set
    var dayFoundTutis by mutableStateOf(emptyList<IntroducedWord>())
        @UiThread
        private set
    var dayWrongWords by mutableStateOf(emptyMap<String, Int>())
        @UiThread
        private set

    var isLoading by mutableStateOf(false)
        @UiThread
        private set

    /**
     * Stores whether the user is logged in or not.
     * @author Arnau Mora
     * @since 20220825
     * @see loadAuthenticatedState
     */
    var isAuthenticated by mutableStateOf(false)
        @UiThread
        private set

    /**
     * Stores the [Player] information. May be null if not logged in.
     * @author Arnau Mora
     * @since 20220825
     * @see loadAuthenticatedState
     */
    var player by mutableStateOf<Player?>(null)
        @UiThread
        private set

    val prefNumberOfLaunches = getApplication<App>()
        .dataStore
        .data
        .map { it[PrefNumberOfLaunches] ?: 0 }

    val prefDisableDonationDialog = getApplication<App>()
        .dataStore
        .data
        .map { it[PrefDisableDonationDialog] ?: false }

    /**
     * Used for fetching the authentication state of the player.
     * @author Arnau Mora
     * @since 20220824
     */
    private val signInClient: GamesSignInClient = PlayGames.getGamesSignInClient(activity)

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
            uiContext {
                this@MainViewModel.gameInfo = gameInfo
                this@MainViewModel.correctWords = emptyList()
                this@MainViewModel.points = 0
                this@MainViewModel.level = 0
                this@MainViewModel.introducedTutis = emptyList()
            }
        } else
            Timber.d("There's no new game data available.")
    }

    /**
     * Loads the data of the logged in player, and updates [player] accordingly if no errors occur.
     * @author Arnau Mora
     * @since 20220825
     */
    @WorkerThread
    private suspend fun loadPlayer(activity: Activity) {
        try {
            Timber.d("Loading player data...")
            val playerData = PlayGames.getPlayersClient(activity)
                .currentPlayer
                .await()
            uiContext { player = playerData }
        } catch (e: ApiException) {
            Timber.e(e, "Could not load player data.")
        }
    }

    /**
     * Loads the current authentication state, and the player's data.
     *
     * Updates [isAuthenticated] and [player] accordingly.
     * @author Arnau Mora
     * @since 20220824
     * @param activity The [Activity] that is requesting the load.
     * @see isAuthenticated
     */
    fun loadAuthenticatedState(activity: Activity) {
        Timber.d("Checking if client is authenticated...")
        viewModelScope.launch {
            ioContext {
                try {
                    isAuthenticated = signInClient
                        .isAuthenticated
                        .await()
                        .isAuthenticated

                    Timber.i("Is user authenticated: $isAuthenticated")

                    if (isAuthenticated)
                        loadPlayer(activity)
                } catch (e: ApiException) {
                    Timber.e(e, "Could not get authenticated state.")
                }
            }
        }
    }

    /**
     * Loads all the game information for the day. This includes fetching the [GameInfo] from the
     * local storage, or the server if not stored locally (updates [gameInfo]). Also adds a collector
     * to the words dao, which updates [correctWords], [points], [level] and [introducedTutis].
     *
     * Progress can be observed with [isLoading], and errors can be handled with [error].
     * @author Arnau Mora
     * @since 20220825
     * @param activity The activity that is requesting the load.
     * @param loadingGameProgressCallback Will get called asynchronously when loading the player's
     * progress from the server. If `finished` is `false`, it means that the load has been started,
     * and a value of `true` indicates that the load has been finished.
     */
    fun loadGameInfo(
        activity: Activity,
        @WorkerThread loadingGameProgressCallback: suspend (finished: Boolean) -> Unit,
    ) {
        viewModelScope.launch {
            Timber.v("Resetting error flag...")
            error = RESULT_OK
            isLoading = true

            Timber.v("Checking if tried to sign in ever...")
            val databaseSingleton = DatabaseSingleton.getInstance(activity)

            doAsync {
                loadGameHistory()
            }

            // This starts a new thread asynchronously which will keep track of introduced words.
            doAsync {
                Timber.v("Adding collector for words...")
                databaseSingleton
                    .appDatabase
                    .wordsDao()
                    .getAll()
                    // This will get called whenever a new word is introduced
                    // Note that it will also get called once when initializing the app.
                    .collectLatest { wordsList ->
                        Timber.i("Updated words dao.")

                        // Wait until GameInfo is available, or 10 seconds have passed
                        waitForGameInfo(10000, 10)

                        // Declare a new GameInfo for being immutable
                        val gameInfo = gameInfo
                        if (gameInfo == null) {
                            Timber.e("Could not compute new words list. GameInfo is null.")
                            return@collectLatest
                        }

                        // Compute the correct words list asynchronously.
                        val correctWords = ioContext {
                            wordsList
                                // Will store at correctWords all the words that match the current
                                // gameInfo, and are correct.
                                .filter { it.hash == gameInfo.hash && it.isCorrect }
                        }
                        val points = correctWords.calculatePoints(gameInfo)
                        val level = getLevelFromPoints(points, gameInfo.pointsPerLevel)
                        val tutis = correctWords.getTutis(gameInfo)

                        uiContext {
                            // Update correct words
                            this@MainViewModel.correctWords = correctWords
                            this@MainViewModel.points = points
                            this@MainViewModel.level = level
                            this@MainViewModel.introducedTutis = tutis
                        }

                        // Give achievements if any
                        synchronizeAchievements(activity, gameHistory, wordsList)

                        Timber.d("Storing progress...")
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

                // Update the state of gameInfo with the obtained data
                uiContext {

                }
                this@MainViewModel.gameInfo = gameInfo

                // Load all the words the player has introduced
                Timber.d("Loading words from server...")
                val serverIntroducedWordsList = getServerIntroducedWordsList(
                    activity,
                    gameInfo,
                    loadingGameProgressCallback,
                )
                Timber.d("Got ${serverIntroducedWordsList.size} words from server.")

                uiContext {
                    Timber.d("Finished loading")
                    isLoading = false
                }

                loadCorrectWords(serverIntroducedWordsList)
            }
        }
    }

    /**
     * Waits until [gameInfo] is ready and returns true when it is. Otherwise returns false.
     * @author Arnau Mora
     * @since 20220825
     */
    private tailrec suspend fun waitForGameInfo(maxDelay: Long, checkPeriod: Long): Boolean {
        if (maxDelay < 0) return false
        if (gameInfo != null) return true
        delay(checkPeriod)
        return waitForGameInfo(maxDelay - checkPeriod, checkPeriod)
    }

    /**
     * Loads all the games registered in the server, and updates [gameHistory] with the list.
     * @author Arnau Mora
     * @since 20220825
     */
    @WorkerThread
    @AddTrace(name = "GameHistoryLoad")
    private suspend fun loadGameHistory() {
        Timber.d("Loading game history...")
        val gameHistory = try {
            loadGameHistoryFromServer(getApplication())
        } catch (e: NoSuchElementException) {
            Timber.e(e, "Data from server is not valid.")
            return
        }

        uiContext {
            Timber.d("Game history ready. Updating state...")
            this@MainViewModel.gameHistory = gameHistory
        }
    }

    /**
     * Gets all the words stored in the database, and compares them with the ones at
     * [serverIntroducedWordsList]. If there are some missing, they get added to the local database.
     * Those added words will get synchronized with the server automatically with the collector
     * added in [loadGameInfo].
     * @author Arnau Mora
     * @since 20220825
     * @param serverIntroducedWordsList The words the player has stored in the server.
     */
    @WorkerThread
    @AddTrace(name = "CorrectWordsLoad")
    private suspend fun loadCorrectWords(
        serverIntroducedWordsList: List<IntroducedWord>,
    ) {
        val wordsDao = DatabaseSingleton
            .getInstance(getApplication())
            .appDatabase
            .wordsDao()
        val databaseWords = wordsDao
            .getAll()
            .first()
        val newWords = arrayListOf<IntroducedWord>()

        // Iterate all the words in serverIntroducedWordsList and add them to newWords if not
        // present in databaseWords.
        serverIntroducedWordsList.forEach { word ->
            if (!databaseWords.contains(word))
                newWords.add(word)
        }

        // Add all the new words to the words dao if any. This will call the collector at loadGameInfo.
        if (newWords.isNotEmpty())
            wordsDao.put(*newWords.toTypedArray())
    }

    @AddTrace(name = "DailyWordsLoad")
    fun loadWordsForDay(gameInfo: GameInfo, date: Date, includeWrongWords: Boolean = false) {
        viewModelScope.launch {
            val dateCalendar = Calendar.getInstance()
            dateCalendar.time = date

            val databaseSingleton = DatabaseSingleton.getInstance(getApplication())
            val dao = databaseSingleton.appDatabase.wordsDao()
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
        activity: Activity,
        gameInfo: GameInfo,
        word: String,
        isCorrect: Boolean,
    ) {
        val databaseSingleton = DatabaseSingleton.getInstance(getApplication())
        viewModelScope.launch {
            val dao = databaseSingleton.appDatabase.wordsDao()
            val now = Calendar.getInstance().timeInMillis
            val hash = gameInfo.hash
            withContext(Dispatchers.IO) {
                val introducedWord = IntroducedWord(0, now, hash, word, isCorrect)
                incrementAchievements(activity, introducedWord)
                dao.put(introducedWord)
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

    class Factory(private val activity: Activity) : ViewModelProvider.NewInstanceFactory() {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainViewModel(activity) as T
        }
    }
}
