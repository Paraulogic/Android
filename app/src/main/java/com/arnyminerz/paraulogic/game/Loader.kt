package com.arnyminerz.paraulogic.game

import android.content.Context
import com.google.firebase.FirebaseException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import com.google.firebase.perf.metrics.AddTrace
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
 * @param sortDirection The sort direction according to timestamp.
 * @throws FirebaseException If there happens to be an error while loading data from server.
 */
@AddTrace(name = "ServerLoad")
@Throws(FirebaseException::class)
private suspend fun serverData(
    context: Context,
    limit: Long = 1,
    sortDirection: Query.Direction = Query.Direction.DESCENDING
) = suspendCoroutine<QuerySnapshot> { cont ->
    try {
        Firebase.firestore
    } catch (e: IllegalStateException) {
        Timber.w("FirebaseApp not initialized. Initializing...")
        Firebase.initialize(context)
        Firebase.firestore
    }
        .collection("paraulogic")
        .orderBy("timestamp", sortDirection)
        .limit(limit)
        .get()
        .addOnSuccessListener { cont.resume(it) }
        .addOnFailureListener { cont.resumeWithException(it) }
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
    val snapshot = serverData(context)
    if (snapshot.isEmpty)
        throw NoSuchElementException("Could not get data from server")
    else {
        Timber.d("Got documents, fetching first...")
        val documents = snapshot.documents
        val document = documents.first()
        Timber.d("Decoding GameInfo...")
        return GameInfo.fromServer(document)
    }
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
    itemLoaded: ((item: GameHistoryItem) -> Unit)? = null
): List<GameHistoryItem> {
    val snapshot = serverData(context, limit = 10000)
    if (snapshot.isEmpty)
        throw NoSuchElementException("Could not get data from server")
    else {
        Timber.d("Got documents, decoding GameHistoryItem...")
        val history = arrayListOf<GameHistoryItem>()
        for (document in snapshot.documents) {
            val timestamp = document.getTimestamp("timestamp") ?: continue

            val date = timestamp.toDate()
            val gameInfoObject = GameInfo.fromServer(document)

            val gameHistoryItem = GameHistoryItem(date, gameInfoObject)
            history.add(gameHistoryItem)
            itemLoaded?.invoke(gameHistoryItem)

            Timber.i("Added game from $date.")
        }
        return history
    }
}
