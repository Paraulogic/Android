package com.arnyminerz.paraulogic.play.games

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

fun startSignInIntent(signInClient: GoogleSignInClient, launcher: ActivityResultLauncher<Intent>) {
    val intent = signInClient.signInIntent
    launcher.launch(intent)
}

fun Context.createSignInClient(): GoogleSignInClient =
    GoogleSignIn.getClient(
        this,
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)
            .requestProfile()
            .build()
    )

suspend fun Context.signInSilently(client: GoogleSignInClient) =
    suspendCoroutine<GoogleSignInAccount?> { cont ->
        Timber.d("Checking if signed in...")
        val signInOptions = GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN
        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (GoogleSignIn.hasPermissions(account, *signInOptions.scopeArray))
            cont.resume(account!!)
        else {
            Timber.d("Logging in with Games...")
            client
                .silentSignIn()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful)
                        cont.resume(task.result)
                    else {
                        Timber.e("User needs to sign in manually.")
                        cont.resume(null)
                    }
                }
        }
    }
