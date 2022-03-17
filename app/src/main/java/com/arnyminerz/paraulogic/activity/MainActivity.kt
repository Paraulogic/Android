package com.arnyminerz.paraulogic.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.lifecycle.ViewModelProvider
import com.arnyminerz.paraulogic.R
import com.arnyminerz.paraulogic.play.games.createSignInClient
import com.arnyminerz.paraulogic.play.games.signInSilently
import com.arnyminerz.paraulogic.play.games.startSignInIntent
import com.arnyminerz.paraulogic.play.games.tryToAddPoints
import com.arnyminerz.paraulogic.ui.elements.MainScreen
import com.arnyminerz.paraulogic.ui.theme.AppTheme
import com.arnyminerz.paraulogic.ui.toast
import com.arnyminerz.paraulogic.ui.viewmodel.MainViewModel
import com.arnyminerz.paraulogic.utils.doAsync
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import timber.log.Timber

class MainActivity : ComponentActivity() {
    /**
     * The client for performing sign in operations with Google.
     * @author Arnau Mora
     * @since 20220309
     */
    private lateinit var signInClient: GoogleSignInClient

    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data
        if (data != null) {
            val signInResult = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if (signInResult?.isSuccess == true) {
                Timber.i("Signed in successfully.")
                toast(R.string.toast_signed_in)
            } else {
                Timber.e("Could not sign in. Status: ${signInResult?.status}")
                signInResult?.status?.statusMessage?.let { toast(it) }
            }
        } else
            Timber.w("Cannot process sign in result since data is null.")
    }

    private val popupLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalPagerApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.d("Creating sign in client...")
        signInClient = createSignInClient()

        Timber.d("Initializing main view model...")
        val viewModel: MainViewModel = ViewModelProvider(
            this,
            MainViewModel.Factory(application)
        )[MainViewModel::class.java]

        setContent {
            AppTheme {
                MainScreen(viewModel, popupLauncher) {
                    startSignInIntent(signInClient, signInLauncher)
                }
            }
        }

        viewModel.loadGameInfo()
        viewModel.loadGameHistory()
    }

    override fun onResume() {
        super.onResume()

        doAsync {
            val account = signInSilently(signInClient)
            if (account != null)
                Timber.i("Log in successful")

            Timber.i("Trying to add missing points...")
            tryToAddPoints(this@MainActivity)
        }
    }
}
