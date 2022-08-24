package com.arnyminerz.paraulogic.play.games

import android.app.Activity
import androidx.annotation.WorkerThread
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmapOrNull
import com.arnyminerz.paraulogic.R
import com.arnyminerz.paraulogic.singleton.DatabaseSingleton
import com.arnyminerz.paraulogic.storage.entity.IntroducedWord
import com.arnyminerz.paraulogic.utils.genericMap
import com.arnyminerz.paraulogic.utils.toJsonArray
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.games.PlayGames
import com.google.android.gms.games.SnapshotsClient
import com.google.android.gms.games.snapshot.SnapshotMetadataChange
import com.google.android.gms.tasks.RuntimeExecutionException
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * The name of the save instance for Google Play Games snapshots.
 * @author Arnau Mora
 * @since 20220824
 */
const val SNAPSHOT_SAVE_NAME = "Paraulogic"

@WorkerThread
suspend fun loadGameSnapshot(
    activity: Activity,
    conflictResolutionPolicy: Int = SnapshotsClient.RESOLUTION_POLICY_MOST_RECENTLY_MODIFIED,
) = suspendCoroutine { cont ->
    PlayGames
        .getSnapshotsClient(activity)
        .open(SNAPSHOT_SAVE_NAME, true, conflictResolutionPolicy)
        .addOnFailureListener { cont.resumeWithException(it) }
        .addOnSuccessListener { task ->
            if (task.isConflict)
            // TODO: Handle conflicts
                cont.resumeWithException(IllegalStateException("Conflict in snapshot: ${task.conflict}"))
            else
                cont.resume(task.data!!)
        }
}

@WorkerThread
suspend fun loadIntroducedWords(activity: Activity) =
    try {
        loadGameSnapshot(activity)
            .snapshotContents
            .readFully()
            .let { String(it) }
            .takeIf { it.isNotBlank() }
            ?.also { Timber.i("Server JSON: $it") }
            ?.toJsonArray()
            ?.genericMap { item ->
                when (item) {
                    // Normal JSON form
                    is JSONObject -> IntroducedWord(item)
                    // Simplified JSON form
                    is JSONArray -> IntroducedWord(item)
                    else -> throw IllegalStateException("Loaded item from server doesn't match any valid type: ${item::class.simpleName}")
                }
            }
            ?.toList()
    } catch (e: JSONException) {
        Timber.e(e, "Could not parse JSON")
        null
    } catch (e: RuntimeExecutionException) {
        Timber.e(e, "Could not get snapshot.")
        null
    } catch (e: ApiException) {
        if (e.statusCode == 4) // Not logged in
            Timber.e(e, "Could not load introduced words since user is not logged in.")
        else
            Timber.e(e, "Google Play Api thrown an exception.")
        null
    } catch (e: IOException) {
        Timber.e(e, "Could not read the game progress snapshot's stream.")
        null
    } ?: emptyList()

@WorkerThread
suspend fun loadGameProgress(activity: Activity) {
    Timber.i("Loading game progress...")
    try {
        val words = loadIntroducedWords(activity)

        DatabaseSingleton.getInstance(activity)
            .db
            .wordsDao()
            .let { wordsDao ->
                val wordsList = wordsDao.getAll().first()
                if (wordsList.isEmpty()) {
                    Timber.i("Adding ${words.size} words to local db")
                    wordsDao.put(*words.toTypedArray())
                }
            }
    } catch (e: ApiException) {
        Timber.e(e, "Could not load game progress.")
    }
}

suspend fun storeGameProgress(activity: Activity, wordsList: List<IntroducedWord>) {
    Timber.i("Saving game progress...")
    Timber.d("Decoding words list...")
    val array = JSONArray()
    wordsList.forEachIndexed { i, t -> array.put(i, t.simplifiedJson()) }
    val bytes = array.toString().toByteArray()

    Timber.d("Loading snapshot for account...")
    val snapshot = loadGameSnapshot(activity).apply {
        snapshotContents.writeBytes(bytes)
    }
    val metadataChange = SnapshotMetadataChange.Builder()
        .setDescription("The Paraulogic's stored progress.")
        .apply {
            ContextCompat.getDrawable(activity, R.mipmap.ic_launcher)
                ?.toBitmapOrNull()
                ?.let { setCoverImage(it) }
        }
        .build()

    PlayGames.getSnapshotsClient(activity)
        .commitAndClose(snapshot, metadataChange)
}
