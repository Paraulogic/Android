package com.arnyminerz.paraulogic.activity

import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModelProvider
import com.arnyminerz.paraulogic.R
import com.arnyminerz.paraulogic.broadcast.ACTION_UPDATE_GAME_DATA
import com.arnyminerz.paraulogic.pref.PrefNumberOfLaunches
import com.arnyminerz.paraulogic.pref.dataStore
import com.arnyminerz.paraulogic.ui.dialog.BuyCoffeeDialog
import com.arnyminerz.paraulogic.ui.elements.MainScreen
import com.arnyminerz.paraulogic.ui.theme.AppTheme
import com.arnyminerz.paraulogic.ui.utils.LockScreenOrientation
import com.arnyminerz.paraulogic.ui.viewmodel.MainViewModel
import com.arnyminerz.paraulogic.utils.doAsync
import com.arnyminerz.paraulogic.utils.doOnUi
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.android.gms.games.PlayGames
import kotlinx.coroutines.flow.first
import timber.log.Timber

@OptIn(
    ExperimentalPagerApi::class,
    ExperimentalMaterialApi::class,
    ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class,
    ExperimentalMaterial3WindowSizeClassApi::class,
)
class MainActivity : AppCompatActivity() {
    /**
     * Used for displaying popups to the user, such as the achievements screen, or the leaderboard.
     * @author Arnau Mora
     * @since 20220825
     */
    private val popupLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data
        Timber.i("Closed popup. Result code: ${result.resultCode}. Data: ${data?.data}")
    }

    /**
     * The view model used for doing all the hard work.
     * @author Arnau Mora
     * @since 20220825
     */
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.d("Initializing main view model...")
        viewModel = ViewModelProvider(
            this,
            MainViewModel.Factory(this)
        )[MainViewModel::class.java]

        viewModel.loadAuthenticatedState(this)

        val filter = IntentFilter(ACTION_UPDATE_GAME_DATA)
        registerReceiver(viewModel.broadcastReceiver, filter)

        increaseLaunches()

        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            val snackbarHostState = remember { SnackbarHostState() }

            LockScreenOrientation(
                when (windowSizeClass.widthSizeClass) {
                    WindowWidthSizeClass.Compact -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    else -> ActivityInfo.SCREEN_ORIENTATION_SENSOR
                }
            )

            AppTheme {
                MainScreen(snackbarHostState, viewModel, popupLauncher)

                var showingDialog by remember { mutableStateOf(false) }
                BuyCoffeeDialog(showingDialog) {
                    increaseLaunches().invokeOnCompletion {
                        showingDialog = false
                    }
                }

                val disabledDonationDialog by viewModel.prefDisableDonationDialog
                    .collectAsState(initial = false)
                val numberOfLaunches: Int? by viewModel.prefNumberOfLaunches
                    .collectAsState(initial = null)

                // Show dialog every 15 launches
                val numberOfLaunchesRem = numberOfLaunches?.rem(15) ?: Int.MAX_VALUE
                if (numberOfLaunchesRem == 0 && !disabledDonationDialog)
                    showingDialog = true
            }

            // This makes sure data only gets loaded once, and not every redraw
            if (!viewModel.isLoading && viewModel.gameInfo == null)
                viewModel.loadGameInfo(this) { finished ->
                    doOnUi {
                        snackbarHostState.currentSnackbarData?.dismiss()
                        if (finished)
                            snackbarHostState.showSnackbar(
                                message = getString(R.string.status_loaded_server),
                                duration = SnackbarDuration.Short,
                            )
                        else
                            snackbarHostState.showSnackbar(
                                message = getString(R.string.status_loading_server),
                                duration = SnackbarDuration.Indefinite,
                            )
                    }
                }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        if (this::viewModel.isInitialized)
            unregisterReceiver(viewModel.broadcastReceiver)
    }

    /**
     * Increase the launches counter by 1.
     * @author Arnau Mora
     * @since 20220825
     * @see dataStore
     * @see PrefNumberOfLaunches
     */
    private fun increaseLaunches() =
        doAsync {
            // Increase number of launches
            dataStore.edit {
                val numberOfLaunches = dataStore
                    .data
                    .first()[PrefNumberOfLaunches]
                it[PrefNumberOfLaunches] = numberOfLaunches?.plus(1) ?: 1
            }
        }
}
