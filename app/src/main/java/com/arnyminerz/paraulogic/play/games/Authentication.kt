package com.arnyminerz.paraulogic.play.games

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.drive.Drive
import com.google.android.gms.games.Games
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

fun startSignInIntent(signInClient: GoogleSignInClient, launcher: ActivityResultLauncher<Intent>) {
    val intent = signInClient.signInIntent
    launcher.launch(intent)
}

@Suppress("DEPRECATION")
val signInOptions: GoogleSignInOptions
    get() = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)
        .requestProfile()
        .requestScopes(Games.SCOPE_GAMES_LITE, Drive.SCOPE_APPFOLDER)
        .build()

fun Context.createSignInClient(): GoogleSignInClient =
    GoogleSignIn.getClient(this, signInOptions)

/**
 * Alias for [GoogleSignIn.getLastSignedInAccount].
 * @author Arnau Mora
 * @since 20220313
 */
fun Context.getLastSignedInAccount() = GoogleSignIn.getLastSignedInAccount(this)

suspend fun Context.signInSilently(client: GoogleSignInClient) =
    suspendCoroutine<GoogleSignInAccount?> { cont ->
        try {
            Timber.d("Checking if signed in...")
            val account = GoogleSignIn.getLastSignedInAccount(this)
            if (GoogleSignIn.hasPermissions(account, *signInOptions.scopeArray)) {
                Timber.d("Already logged in.")
                cont.resume(account!!)
            } else {
                Timber.d("Logging in with Games...")
                client
                    .silentSignIn()
                    .addOnFailureListener {
                        Timber.e(it, "User needs to sign in manually.")
                        cont.resume(null)
                    }
                    .addOnSuccessListener { cont.resume(it) }
            }
        } catch (e: ApiException) {
            Timber.e(e, "An error occurred with the Google Play Api.")
            cont.resume(null)
        }
    }
