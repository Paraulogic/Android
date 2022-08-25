package com.arnyminerz.paraulogic.ui.elements

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.arnyminerz.paraulogic.R
import com.arnyminerz.paraulogic.annotation.LoadError
import com.arnyminerz.paraulogic.ui.Game
import com.arnyminerz.paraulogic.ui.bar.MainBottomAppBar
import com.arnyminerz.paraulogic.ui.bar.MainTopAppBar
import com.arnyminerz.paraulogic.ui.screen.StatsScreen
import com.arnyminerz.paraulogic.ui.viewmodel.MainViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState

@Composable
@ExperimentalPagerApi
@ExperimentalMaterialApi
@ExperimentalMaterial3Api
@ExperimentalFoundationApi
fun AppCompatActivity.MainScreen(
    snackbarHostState: SnackbarHostState,
    viewModel: MainViewModel,
    popupLauncher: ActivityResultLauncher<Intent>,
) {
    val pagerState = rememberPagerState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { MainTopAppBar(viewModel, popupLauncher) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            MainBottomAppBar(viewModel.gameInfo, viewModel.points, viewModel.level, pagerState)
        }
    ) { paddingValues ->
        HorizontalPager(
            count = 2,
            modifier = Modifier.padding(paddingValues),
            state = pagerState,
        ) { page ->
            when (page) {
                0 -> if (viewModel.error == LoadError.RESULT_OK) {
                    viewModel.gameInfo?.let {
                        Game(it, viewModel)
                    } ?: LoadingBox()
                } else
                    Box(
                        modifier = Modifier
                            .padding(start = 48.dp, end = 48.dp)
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column {
                            Text(
                                text = stringResource(
                                    if (viewModel.error == 1)
                                        R.string.error_gameinfo_no_such_element
                                    else
                                        R.string.error_gameinfo_firebase
                                ),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            Text(
                                text = stringResource(R.string.error_try_again),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                1 -> viewModel
                    .gameHistory
                    .takeIf { it.isNotEmpty() }
                    ?.let { history ->
                        if (viewModel.gameHistory.isNotEmpty())
                            Column(
                                modifier = Modifier.fillMaxSize(),
                            ) {
                                StatsScreen(viewModel, popupLauncher, history)
                            }
                    }
                    ?: LoadingBox()
            }
        }
    }
}
