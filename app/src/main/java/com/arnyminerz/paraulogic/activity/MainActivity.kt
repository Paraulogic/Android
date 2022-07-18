package com.arnyminerz.paraulogic.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModelProvider
import com.arnyminerz.paraulogic.R
import com.arnyminerz.paraulogic.play.games.createSignInClient
import com.arnyminerz.paraulogic.play.games.loadSnapshot
import com.arnyminerz.paraulogic.play.games.signInSilently
import com.arnyminerz.paraulogic.play.games.startSignInIntent
import com.arnyminerz.paraulogic.play.games.tryToAddPoints
import com.arnyminerz.paraulogic.pref.PreferencesModule
import com.arnyminerz.paraulogic.pref.dataStore
import com.arnyminerz.paraulogic.singleton.DatabaseSingleton
import com.arnyminerz.paraulogic.storage.entity.IntroducedWord
import com.arnyminerz.paraulogic.ui.dialog.BuyCoffeeDialog
import com.arnyminerz.paraulogic.ui.elements.MainScreen
import com.arnyminerz.paraulogic.ui.theme.AppTheme
import com.arnyminerz.paraulogic.ui.toast
import com.arnyminerz.paraulogic.ui.viewmodel.MainViewModel
import com.arnyminerz.paraulogic.utils.doAsync
import com.arnyminerz.paraulogic.utils.doOnUi
import com.arnyminerz.paraulogic.utils.mapJsonObject
import com.arnyminerz.paraulogic.utils.toJsonArray
import com.arnyminerz.paraulogic.utils.uiContext
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.RuntimeExecutionException
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.first
import org.json.JSONException
import timber.log.Timber
import java.io.IOException

class MainActivity : AppCompatActivity() {
    /**
     * The client for performing sign in operations with Google.
     * @author Arnau Mora
     * @since 20220309
     */
    private lateinit var signInClient: GoogleSignInClient

    @Suppress("BlockingMethodInNonBlockingContext")
    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        result.data?.let { data ->
            val signInResult = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if (signInResult?.isSuccess == true) {
                val account = signInResult.signInAccount!!
                Timber.i("Signed in successfully.")
                toast(R.string.toast_signed_in)

                Firebase.analytics.setUserId(account.id)

                doAsync {
                    Timber.d("Getting words list from logged in account.")
                    val serverList = try {
                        loadSnapshot(account)
                            ?.snapshotContents
                            ?.readFully()
                            ?.let { String(it) }
                            ?.also { Timber.i("Server JSON: $it") }
                            ?.toJsonArray()
                            ?.mapJsonObject { IntroducedWord(it) }
                            ?.toList()
                    } catch (e: JSONException) {
                        Timber.e(e, "Could not parse JSON")
                        null
                    } catch (e: RuntimeExecutionException) {
                        Timber.e(e, "Could not get snapshot.")
                        null
                    } catch (e: ApiException) {
                        Timber.e(e, "Google Play Api thrown an exception.")
                        null
                    } catch (e: IOException) {
                        Timber.e(e, "Could not read the game progress snapshot's stream.")
                        null
                    } ?: emptyList()
                    DatabaseSingleton.getInstance(this@MainActivity)
                        .db
                        .wordsDao()
                        .let { wordsDao ->
                            val wordsList = wordsDao.getAll().first()
                            if (wordsList.isEmpty()) {
                                Timber.i("Adding ${serverList.size} words to local db")
                                wordsDao.put(*serverList.toTypedArray())
                            }
                        }
                }
            } else {
                Timber.e("Could not sign in. Status: ${signInResult?.status}")
                signInResult?.status?.statusMessage?.let { toast(it) }
            }
        } ?: run {
            Timber.w("Cannot process sign in result since data is null.")
        }
    }

    private val popupLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { }

    @OptIn(
        ExperimentalPagerApi::class,
        ExperimentalMaterialApi::class,
        ExperimentalMaterial3Api::class,
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.d("Creating sign in client...")
        signInClient = createSignInClient()

        Timber.d("Initializing main view model...")
        val viewModel: MainViewModel = ViewModelProvider(
            this,
            MainViewModel.Factory(application)
        )[MainViewModel::class.java]

        doAsync {
            // Increase number of launches
            dataStore.edit {
                val dataStoreData = dataStore.data.first()
                it[PreferencesModule.NumberOfLaunches] =
                    dataStoreData[PreferencesModule.NumberOfLaunches]?.plus(1) ?: 0
            }
        }

        setContent {
            val snackbarHostState = remember { SnackbarHostState() }

            AppTheme {
                MainScreen(snackbarHostState, viewModel, popupLauncher) {
                    startSignInIntent(signInClient, signInLauncher)
                }

                var showingDialog by remember { mutableStateOf(false) }
                BuyCoffeeDialog(showingDialog) { showingDialog = false }

                doAsync {
                    val dataStoreData = dataStore.data.first()
                    val numberOfLaunches = dataStoreData[PreferencesModule.NumberOfLaunches]
                    val disabledDonationDialog =
                        dataStoreData[PreferencesModule.DisableDonationDialog]

                    // Show dialog every 15 launches
                    val numberOfLaunchesRem = numberOfLaunches?.rem(15) ?: 0
                    if (numberOfLaunchesRem == 0 && disabledDonationDialog != false)
                        uiContext { showingDialog = true }
                }
            }

            viewModel.loadGameInfo(signInClient, signInLauncher) {
                doOnUi {
                    if (it)
                        snackbarHostState.showSnackbar(
                            message = getString(R.string.status_loaded_server),
                        )
                    else
                        toast(R.string.status_loading_server)
                }
            }
            viewModel.loadGameHistory()
        }
    }

    override fun onResume() {
        super.onResume()

        doAsync {
            signInSilently(signInClient)?.run {
                Timber.i("Log in successful")
            }

            Timber.i("Trying to add missing points...")
            tryToAddPoints(this@MainActivity)
        }
    }

    override fun onStop() {
        super.onStop()

        doAsync {
            signInSilently(signInClient)?.run {
                Timber.i("Log in successful")
            }

            Timber.i("Trying to add missing points...")
            tryToAddPoints(this@MainActivity)
        }
    }
}
