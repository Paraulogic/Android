package com.arnyminerz.paraulogic.play.games

import android.app.Activity
import androidx.annotation.WorkerThread
import com.arnyminerz.paraulogic.R
import com.google.android.gms.games.AnnotatedData
import com.google.android.gms.games.LeaderboardsClient
import com.google.android.gms.games.LeaderboardsClient.LeaderboardScores
import com.google.android.gms.games.PlayGames
import com.google.android.gms.games.leaderboard.LeaderboardVariant
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@WorkerThread
private suspend fun LeaderboardsClient.getLeaderboardAnnotatedData(
    leaderboardId: String,
    maxResults: Int = 1
) = suspendCoroutine<AnnotatedData<LeaderboardsClient.LeaderboardScores>> { cont ->
    loadPlayerCenteredScores(
        leaderboardId,
        LeaderboardVariant.TIME_SPAN_ALL_TIME,
        LeaderboardVariant.COLLECTION_PUBLIC,
        maxResults,
    )
        .addOnSuccessListener { cont.resume(it) }
        .addOnFailureListener { cont.resumeWithException(it) }
}

suspend fun getPlayerPoints(
    activity: Activity,
    leaderboardsClient: LeaderboardsClient = PlayGames.getLeaderboardsClient(activity),
): Long {
    Timber.d("Getting leaderboard id...")
    val leaderboardId = activity.getString(R.string.leaderboard_world_ranking)

    var leaderboardScore: LeaderboardScores? = null
    return try {
        Timber.v("Getting current player leaderboard...")
        val lsad = leaderboardsClient.getLeaderboardAnnotatedData(leaderboardId, 1)
        leaderboardScore = lsad.get() ?: run {
            Timber.e("Could not fetch user score. Leaderboard score is null.")
            return 0
        }

        val scores = leaderboardScore.scores
        Timber.d("Scores (${scores.count}): $scores")

        scores
            .takeIf { it.count > 0 }
            ?.get(0)
            ?.rawScore
            ?: 0
    } catch (e: IllegalStateException) {
        Timber.e(e, "Could not get score.")
        0
    } finally {
        leaderboardScore?.release()
    }
}

/**
 * Adds [points] to the player's leaderboard.
 * @author Arnau Mora
 * @since 20220309
 * @param activity The activity that is requesting the update.
 * @param points The amount of points to add.
 */
@WorkerThread
suspend fun addPlayerPoints(
    activity: Activity,
    points: Long,
) {
    Timber.d("Getting leaderboard id...")
    val leaderboardId = activity.getString(R.string.leaderboard_world_ranking)

    val leaderboardsClient = PlayGames.getLeaderboardsClient(activity)

    Firebase.analytics
        .logEvent(FirebaseAnalytics.Event.POST_SCORE) {
            param(FirebaseAnalytics.Param.SCORE, points)
        }

    val score = getPlayerPoints(activity, leaderboardsClient)
    val newScore = score + points

    Timber.v("Submitting new score: $newScore")
    leaderboardsClient.submitScore(leaderboardId, newScore)
}
