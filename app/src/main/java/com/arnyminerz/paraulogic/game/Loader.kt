package com.arnyminerz.paraulogic.game

import android.content.Context
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.arnyminerz.paraulogic.singleton.VolleySingleton
import com.arnyminerz.paraulogic.utils.mapJsonObject
import com.google.firebase.FirebaseException
import com.google.firebase.perf.metrics.AddTrace
import org.json.JSONObject
import timber.log.Timber
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
suspend fun loadGameInfoFromServer(context: Context): GameInfo {
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
