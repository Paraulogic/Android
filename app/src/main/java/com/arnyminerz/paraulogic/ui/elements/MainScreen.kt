package com.arnyminerz.paraulogic.ui.elements

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Gamepad
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.arnyminerz.paraulogic.R
import com.arnyminerz.paraulogic.activity.SettingsActivity
import com.arnyminerz.paraulogic.play.games.startSynchronization
import com.arnyminerz.paraulogic.ui.Game
import com.arnyminerz.paraulogic.ui.dialog.HelpDialog
import com.arnyminerz.paraulogic.ui.screen.StatsScreen
import com.arnyminerz.paraulogic.ui.viewmodel.MainViewModel
import com.arnyminerz.paraulogic.utils.doAsync
import com.arnyminerz.paraulogic.utils.launch
import com.arnyminerz.paraulogic.utils.launchUrl
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.games.Games
import kotlinx.coroutines.launch
import timber.log.Timber

@Composable
@ExperimentalPagerApi
@ExperimentalMaterialApi
@ExperimentalMaterial3Api
fun ComponentActivity.MainScreen(
    viewModel: MainViewModel,
    popupLauncher: ActivityResultLauncher<Intent>,
    signInRequest: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val gameInfo = viewModel.gameInfo
    val gameHistory = viewModel.gameHistory
    var showHelpDialog by remember { mutableStateOf(false) }

    val pagerState = rememberPagerState()

    if (showHelpDialog)
        HelpDialog { showHelpDialog = false }

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
                    val account = GoogleSignIn.getLastSignedInAccount(context)
                    if (account == null)
                        IconButton(
                            onClick = signInRequest,
                            modifier = Modifier
                                .size(48.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Person,
                                contentDescription = stringResource(R.string.image_desc_login)
                            )
                        }
                    else {
                        val showAchievements: () -> Unit = {
                            Games.getAchievementsClient(context, account)
                                .achievementsIntent
                                .addOnSuccessListener { popupLauncher.launch(it) }
                                .addOnFailureListener {
                                    Timber.e(
                                        it,
                                        "Could not launch achievements popup."
                                    )
                                }
                        }

                        val photoUrl = account.photoUrl
                        if (photoUrl == null) {
                            Timber.w("User does not have a photo or permission is denied")
                            IconButton(
                                onClick = showAchievements,
                                modifier = Modifier
                                    .size(48.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.Person,
                                    contentDescription = stringResource(R.string.image_desc_login)
                                )
                            }
                        } else
                            Image(
                                painter = rememberImagePainter(account.photoUrl),
                                contentDescription = stringResource(R.string.image_desc_profile),
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clickable(onClick = showAchievements)
                            )
                    }
                },
            )
        },
        bottomBar = {
            val points = viewModel.points
            val level = viewModel.level

            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ) {
                AnimatedVisibility(visible = points > 0) {
                    Text(
                        text = when (level) {
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

                Text(
                    text = if (gameInfo == null)
                        stringResource(R.string.status_loading)
                    else
                        stringResource(
                            R.string.points,
                            points,
                            gameInfo.maxPoints
                        )
                )
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(if (pagerState.currentPage == 0) 1 else 0)
                            }
                        }
                    ) {
                        Icon(
                            if (pagerState.currentPage == 0)
                                Icons.Outlined.Analytics
                            else Icons.Outlined.Gamepad,
                            stringResource(
                                if (pagerState.currentPage == 0)
                                    R.string.image_desc_paraulogic
                                else
                                    R.string.image_desc_analytics
                            )
                        )
                    }
                    IconButton(
                        onClick = { showHelpDialog = true }
                    ) {
                        Icon(
                            Icons.Outlined.Info,
                            stringResource(R.string.image_desc_help)
                        )
                    }
                    IconButton(
                        onClick = { launch(SettingsActivity::class.java) }
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
        HorizontalPager(
            count = 2,
            modifier = Modifier.padding(paddingValues),
            state = pagerState,
        ) { page ->
            when (page) {
                0 -> if (gameInfo != null) {
                    Timber.i("Game info: $gameInfo")

                    Game(gameInfo, viewModel, signInRequest)
                } else
                    LoadingBox()
                1 -> if (gameHistory.isNotEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        StatsScreen(viewModel, popupLauncher, gameHistory)
                    }
                } else
                    LoadingBox()
            }
        }

        if (gameInfo != null && gameHistory.isNotEmpty())
            doAsync { startSynchronization(context, gameInfo, gameHistory) }
    }
}
