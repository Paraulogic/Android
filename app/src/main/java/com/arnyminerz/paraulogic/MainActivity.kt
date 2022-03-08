package com.arnyminerz.paraulogic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arnyminerz.paraulogic.game.calculatePoints
import com.arnyminerz.paraulogic.game.getLevelFromPoints
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
                // val signedInAccount = signInResult.signInAccount
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
            val gameInfo by remember { viewModel.loadGameInfo() }
            val foundWords by remember { viewModel.correctWords }
            var showHelpDialog by remember { mutableStateOf(false) }

            AppTheme {
                if (showHelpDialog)
                    AlertDialog(
                        onDismissRequest = { showHelpDialog = false },
                        text = {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = stringResource(R.string.info_rules_title),
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                Text(text = stringResource(R.string.info_rules))
                                Text(
                                    text = stringResource(R.string.info_points_title),
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                Text(text = stringResource(R.string.info_points))
                            }
                        },
                        confirmButton = {
                            Button(onClick = { showHelpDialog = false }) {
                                Text(text = stringResource(R.string.action_close))
                            }
                        },
                    )

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
                    },
                    bottomBar = {
                        BottomAppBar(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ) {
                            if (gameInfo != null) {
                                val points = foundWords.calculatePoints(gameInfo!!)
                                val ppl = gameInfo!!.pointsPerLevel // Points per level
                                AnimatedVisibility(visible = points > 0) {
                                    Text(
                                        text = when (getLevelFromPoints(points, ppl)) {
                                            0 -> "\uD83D\uDC24" // 🐤
                                            1 -> "\uD83D\uDD4A️" // 🕊️
                                            2 -> "\uD83E\uDD86" // 🦆
                                            3 -> "\uD83E\uDDA2" // 🦢
                                            4 -> "\uD83E\uDD89" // 🦉
                                            5 -> "\uD83E\uDD85" // 🦅
                                            else -> "\uD83E\uDD9A" // 🦚
                                        },
                                        fontSize = 22.sp,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                }
                            }
                            Text(
                                text = if (gameInfo == null)
                                    stringResource(R.string.status_loading)
                                else
                                    stringResource(
                                        R.string.points,
                                        foundWords.calculatePoints(gameInfo!!),
                                        gameInfo!!.maxPoints
                                    )
                            )
                            Row(
                                horizontalArrangement = Arrangement.End,
                                modifier = Modifier.weight(1f)
                            ) {
                                IconButton(
                                    onClick = { showHelpDialog = true }
                                ) {
                                    Icon(
                                        Icons.Outlined.Info,
                                        stringResource(R.string.image_desc_help)
                                    )
                                }
                                IconButton(
                                    onClick = { /*TODO*/ }
                                ) {
                                    Icon(
                                        Icons.Outlined.Settings,
                                        stringResource(R.string.image_desc_settings)
                                    )
                                }
                            }
                        }
                    }
                ) { paddingValues ->
                    if (gameInfo != null) {
                        Timber.i("Game info: $gameInfo")

                        viewModel.loadCorrectWords(gameInfo!!)
                        if (foundWords.isNotEmpty())
                            Game(paddingValues, gameInfo!!, foundWords, viewModel)
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        AnimatedVisibility(visible = foundWords.isEmpty()) {
                            CircularProgressIndicator()
                        }
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
