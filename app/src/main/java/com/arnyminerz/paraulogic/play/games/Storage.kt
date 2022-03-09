package com.arnyminerz.paraulogic.play.games

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.games.Games
import timber.log.Timber

fun showSavedGamesUI(context: Context, resultLauncher: ActivityResultLauncher<Intent>) {
    val account = GoogleSignIn.getLastSignedInAccount(context) ?: run {
        Timber.w("Could not show saved games UI. Not logged in.")
        return
    }
    val snapshotsClient = Games.getSnapshotsClient(context, account)
    val maxNumberOfSavedGamesToShow = 1

    snapshotsClient.getSelectSnapshotIntent(
        "My Saves", // TODO: Hardcoded string
        true,
        true,
        maxNumberOfSavedGamesToShow,
    )
        .addOnSuccessListener { resultLauncher.launch(it) }
        .addOnFailureListener { Timber.e(it, "Could not get snapshot intent.") }
}
