package com.arnyminerz.paraulogic.play.games

import com.arnyminerz.paraulogic.MainActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

fun MainActivity.createSignInClient(): GoogleSignInClient =
    GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)

suspend fun MainActivity.signInSilently(client: GoogleSignInClient) =
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
