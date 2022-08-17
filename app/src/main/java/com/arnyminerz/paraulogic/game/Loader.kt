package com.arnyminerz.paraulogic.game

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.arnyminerz.paraulogic.singleton.DatabaseSingleton
import com.arnyminerz.paraulogic.singleton.VolleySingleton
import com.arnyminerz.paraulogic.storage.entity.GameInfoEntity
import com.arnyminerz.paraulogic.utils.format
import com.arnyminerz.paraulogic.utils.ioContext
import com.arnyminerz.paraulogic.utils.mapJsonObject
import com.google.firebase.FirebaseException
import com.google.firebase.perf.metrics.AddTrace
import kotlinx.coroutines.flow.first
import org.json.JSONObject
import timber.log.Timber
import java.util.Calendar
import java.util.Date
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Fetches the server's data.
 * @author Arnau Mora
 * @since 20220312
 * @param context The context to initialize Firebase Firestore from.
 * @param limit How many items to load at most.
 * @throws FirebaseException If there happens to be an error while loading data from server.
 */
@AddTrace(name = "ServerLoad")
@Throws(FirebaseException::class)
private suspend fun serverData(
    context: Context,
    limit: Long = 1,
): JSONObject =
    suspendCoroutine { cont ->
        val request = StringRequest(
            Request.Method.GET,
            if (limit <= 1)
                "http://paraulogic.arnyminerz.com/v1/game_info"
            else
                "http://paraulogic.arnyminerz.com/v1/history",
            { response ->
                try {
                    cont.resume(JSONObject(response))
                } catch (e: Exception) {
                    cont.resumeWithException(e)
                }
            },
            { cont.resumeWithException(it) })
        VolleySingleton
            .getInstance(context)
            .addToRequestQueue(request)
    }

/**
 * Obtains the last game info from the Firestore database.
 * @author Arnau Mora
 * @since 20220312
 * @param context The context to initialize the Firestore instance from.
 * @throws FirebaseException If there happens to be an error while loading data from server.
 * @throws NoSuchElementException Could not get the game data from server.
 */
@AddTrace(name = "DataLoad")
@Throws(NoSuchElementException::class, FirebaseException::class)
private suspend fun loadGameInfoFromServer(context: Context): GameInfo {
    val result = serverData(context)
    val success = result.getBoolean("success")
    if (!success)
        throw NoSuchElementException("Could not get data from server")

    Timber.d("Decoding GameInfo...")
    val data = result
        .getJSONObject("data")
    return GameInfo.fromJson(data)
}

/**
 * Loads all the games there has ever been from the server.
 * @author Arnau Mora
 * @since 20220312
 * @param context The context to initialize the Firestore instance from.
 * @param itemLoaded Will get called once each item of the history gets loaded.
 * @return A list of the found [GameHistoryItem].
 * @throws FirebaseException If there happens to be an error while loading data from server.
 * @throws NoSuchElementException Could not get the game data from server.
 */
@AddTrace(name = "HistoryLoad")
@Throws(NoSuchElementException::class, FirebaseException::class)
suspend fun loadGameHistoryFromServer(
    context: Context,
    itemLoaded: ((item: GameInfo) -> Unit)? = null
): List<GameInfo> {
    val result = serverData(context, limit = 10000)

    val success = result.getBoolean("success")
    if (!success)
        throw NoSuchElementException("Could not get data from server")

    Timber.d("Got documents, decoding GameHistoryItem...")
    val history = arrayListOf<GameInfo>()
    val historyData = result
        .getJSONArray("data")
        .mapJsonObject { it }
    for (data in historyData) {
        val gameInfo = GameInfo.fromJson(data)
        history.add(gameInfo)
        itemLoaded?.invoke(gameInfo)

        Timber.i("Added game from ${gameInfo.timestamp}.")
    }
    return history
}

/**
 * Gets new data from the server, and stores it into [DatabaseSingleton].
 * @author Arnau Mora
 * @since 20220817
 * @param context The context to run from.
 * @return An instance of [GameInfo] with the loaded data.
 * @throws NoSuchElementException When an error occurs while getting the data from the server.
 * @see loadGameHistoryFromServer
 */
@Throws(NoSuchElementException::class)
suspend fun fetchAndStoreGameInfo(context: Context): GameInfo {
    val databaseSingleton = DatabaseSingleton.getInstance(context)

    Timber.d("GameInfo not stored in Database, getting from server...")
    return loadGameInfoFromServer(context)
        .also { gameInfo ->
            try {
                Timber.d("Storing GameInfo in db...")
                ioContext {
                    databaseSingleton
                        .db
                        .gameInfoDao()
                        .put(GameInfoEntity.fromGameInfo(gameInfo))
                }
            } catch (e: SQLiteConstraintException) {
                Timber.w("Could not store GameInfo since already stored.")
            }
        }
}

/**
 * Gets the [GameInfo] data for today. If not available, null is returned.
 * @author Arnau Mora
 * @since 20220817
 * @param context The context to run from.
 * @return null if no [GameInfo] is available. A [GameInfo] instance otherwise.
 */
suspend fun gameInfoForToday(context: Context): GameInfo? =
    DatabaseSingleton.getInstance(context)
        // Get the database
        .db
        // Access the GameInfo Dao
        .gameInfoDao()
        // Call the getAll method to fetch all the stored games
        .getAll()
        // Wait until a result is thrown
        .first()
        // Take only if there are stored games. If there aren't the null catcher at the end
        // loads the data from the server.
        .takeIf { it.isNotEmpty() }
        // Log all the Games
        ?.also { l -> Timber.d("Game infos: ${l.map { "[ ${it.gameInfo.letters} ]" }}") }
        // Take the greatest date, this is the most recent stored game
        ?.maxByOrNull { it.date }
        // Take only if the game is in the correct date
        ?.takeIf { entity ->
            // Get the current date
            val now = Calendar.getInstance()
            // Get a date instance with the entity's date
            val entityDate = Date(entity.date)

            // Check if the entity's date is from the same day as now
            val r = entityDate.format("yyyy-MM-dd") == now.format("yyyy-MM-dd")
            // Show warning if the dates do not match
            if (!r)
                Timber.w(
                    "Game's date is not from today. now=${now.format("yyyy-MM-dd")}, entity=${
                        entityDate.format(
                            "yyyy-MM-dd"
                        )
                    }"
                )
            // Return the result of the comparison for taking the value or loading a new one
            r
        }
        // Get the game info from the entity
        ?.gameInfo
