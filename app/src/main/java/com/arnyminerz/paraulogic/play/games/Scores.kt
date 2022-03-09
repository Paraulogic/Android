package com.arnyminerz.paraulogic.play.games

import android.content.Context
import com.arnyminerz.paraulogic.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.games.Games
import com.google.android.gms.games.leaderboard.LeaderboardVariant
import timber.log.Timber

private var missingAddedPoints = arrayListOf<Long>()

/**
 * Tries to add all the points that have not been able to be added.
 * @author Arnau Mora
 * @since 20220309
 */
fun tryToAddPoints(context: Context) {
    val points = missingAddedPoints.sum()
    if (points > 0)
        addPlayerPoints(
            context,
            points,
            { },
            { error ->
                Timber.e(error, "Could still not add points.")
            },
            { missingAddedPoints.clear() },
        )
}

/**
 * Adds [points] to the player's leaderboard.
 * @author Arnau Mora
 * @since 20220309
 */
private fun addPlayerPoints(
    context: Context,
    points: Long,
    loginRequired: () -> Unit,
    errorListener: (e: ApiException) -> Unit,
    onAdded: () -> Unit
) {
    Timber.d("Getting player account...")
    GoogleSignIn.getLastSignedInAccount(context)?.let { account ->
        Timber.d("Getting leaderboard id...")
        val leaderboardId = context.getString(R.string.leaderboard_world_ranking)

        Timber.v("Getting current player leaderboard...")
        val leaderboardsClient = Games.getLeaderboardsClient(context, account)
        val limitResultsTo = 1
        leaderboardsClient
            .loadPlayerCenteredScores(
                leaderboardId,
                LeaderboardVariant.TIME_SPAN_ALL_TIME,
                LeaderboardVariant.COLLECTION_PUBLIC,
                limitResultsTo,
            )
            .addOnSuccessListener { leaderboardScoreAnnotatedData ->
                if (leaderboardScoreAnnotatedData != null) {
                    val leaderboardScore = leaderboardScoreAnnotatedData.get()
                    if (leaderboardScore != null)
                        try {
                            val scores = leaderboardScore.scores
                            Timber.d("Scores (${scores.count}): $scores")

                            val score = if (scores.count <= 0)
                                0
                            else
                                scores
                                    .get(0)
                                    .rawScore
                            Timber.d("Current player score: $score")

                            val newScore = score + points
                            Timber.v("Submitting new score: $newScore")
                            leaderboardsClient.submitScore(leaderboardId, newScore)
                            onAdded()
                        } catch (e: IllegalStateException) {
                            Timber.e(e, "Could not get score.")
                        } finally {
                            leaderboardScore.release()
                        }
                    else
                        Timber.e("Could not fetch user score. Leaderboard score is null.")
                } else
                    Timber.e("Could not fetch user score. Annotated data is null.")
            }
            .addOnFailureListener { error ->
                Timber.e(error, "Could not load leaderboard.")
                (error as? ApiException)?.let { e ->
                    if (e.statusCode == 26502) { // CLIENT_RECONNECT_REQUIRED
                        missingAddedPoints.add(points)
                        loginRequired()
                    }
                    errorListener(e)
                }
            }
    }
}

/**
 * Adds [points] to the player's leaderboard.
 * @author Arnau Mora
 * @since 20220309
 */
fun addPlayerPoints(context: Context, points: Long, loginRequired: () -> Unit) =
    addPlayerPoints(context, points, loginRequired, { }, { })
