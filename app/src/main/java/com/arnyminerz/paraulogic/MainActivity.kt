package com.arnyminerz.paraulogic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.arnyminerz.paraulogic.play.games.createSignInClient
import com.arnyminerz.paraulogic.play.games.signInSilently
import com.arnyminerz.paraulogic.play.games.startSignInIntent
import com.arnyminerz.paraulogic.ui.Game
import com.arnyminerz.paraulogic.ui.theme.AppTheme
import com.arnyminerz.paraulogic.ui.viewmodel.MainViewModel
import com.arnyminerz.paraulogic.utils.doAsync
import com.arnyminerz.paraulogic.utils.launchUrl
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import timber.log.Timber

class MainActivity : ComponentActivity() {
    internal val signInResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val data = result.data ?: return@registerForActivityResult
            val signInResult = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if (signInResult?.isSuccess == true) {
                Timber.i("Signed in correctly.")
                val signedInAccount = signInResult.signInAccount
            } else {
                Timber.e("Sign in status: ${signInResult?.status}")
            }
        }

    private lateinit var signInClient: GoogleSignInClient

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        signInClient = createSignInClient()

        setContent {
            val viewModel by viewModels<MainViewModel>()

            AppTheme {
                // A surface container using the 'background' color from the theme
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        CenterAlignedTopAppBar(
                            navigationIcon = {
                                Image(
                                    painterResource(R.drawable.ic_logo_vilaweb),
                                    contentDescription = stringResource(R.string.image_desc_vilaweb),
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clickable { launchUrl("https://vilaweb.cat") }
                                )
                            },
                            title = {
                                Image(
                                    painterResource(R.drawable.ic_logo),
                                    contentDescription = stringResource(R.string.image_desc_paraulogic),
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .fillMaxWidth(.6f)
                                )
                            },
                            actions = {
                                IconButton(onClick = { startSignInIntent(signInClient) }) {
                                    Icon(Icons.Rounded.Person, contentDescription = "Login button")
                                }
                            },
                        )
                    }
                ) { paddingValues ->
                    val gameInfo by remember { viewModel.loadGameInfo() }

                    if (gameInfo != null) {
                        Timber.i("Game info: $gameInfo")

                        val foundWords by remember { viewModel.loadCorrectWords(gameInfo!!) }
                        if (foundWords != null)
                            Game(paddingValues, gameInfo!!, foundWords!!, viewModel)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        doAsync {
            val account = signInSilently(signInClient)
            if (account == null)
                Timber.e("User must sign in manually.")
        }
    }
}
