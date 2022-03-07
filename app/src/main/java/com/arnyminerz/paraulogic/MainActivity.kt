package com.arnyminerz.paraulogic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.arnyminerz.paraulogic.ui.Game
import com.arnyminerz.paraulogic.ui.theme.AppTheme
import com.arnyminerz.paraulogic.ui.viewmodel.MainViewModel
import com.arnyminerz.paraulogic.utils.launchUrl
import io.sentry.Sentry
import timber.log.Timber

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Sentry.captureMessage("testing SDK setup")

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
                            }
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
}
