package com.arnyminerz.paraulogic.game

import android.content.Context
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import com.google.firebase.perf.metrics.AddTrace
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Obtains the last game info from the Firestore database.
 * @author Arnau Mora
 * @since 20220312
 * @param context The context to initialize the Firestore instance from.
 * @throws NoSuchElementException Could not get the game data from server.
 */
@AddTrace(name = "DataLoad")
@Throws(NoSuchElementException::class)
suspend fun loadGameInfoFromServer(context: Context) = suspendCoroutine<GameInfo> { cont ->
    try {
        Firebase.firestore
    } catch (e: IllegalStateException) {
        Timber.w("FirebaseApp not initialized. Initializing...")
        Firebase.initialize(context)
        Firebase.firestore
    }
        .collection("paraulogic")
        .orderBy("timestamp", Query.Direction.DESCENDING)
        .limit(1)
        .get()
        .addOnSuccessListener { snapshot ->
            if (snapshot.isEmpty)
                cont.resumeWithException(NoSuchElementException("Could not get data from server"))
            else {
                Timber.d("Got documents, fetching first...")
                val documents = snapshot.documents
                val document = documents.first()
                Timber.d("Decoding GameInfo...")
                val gameInfo = GameInfo.fromServer(document)
                cont.resume(gameInfo)
            }
        }
        .addOnFailureListener { cont.resumeWithException(it) }
}
