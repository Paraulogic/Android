package com.arnyminerz.paraulogic.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.lifecycle.ViewModelProvider
import com.arnyminerz.paraulogic.R
import com.arnyminerz.paraulogic.activity.model.LanguageActivity
import com.arnyminerz.paraulogic.play.games.createSignInClient
import com.arnyminerz.paraulogic.play.games.loadSnapshot
import com.arnyminerz.paraulogic.play.games.signInSilently
import com.arnyminerz.paraulogic.play.games.startSignInIntent
import com.arnyminerz.paraulogic.play.games.tryToAddPoints
import com.arnyminerz.paraulogic.singleton.DatabaseSingleton
import com.arnyminerz.paraulogic.storage.entity.IntroducedWord
import com.arnyminerz.paraulogic.ui.elements.MainScreen
import com.arnyminerz.paraulogic.ui.theme.AppTheme
import com.arnyminerz.paraulogic.ui.toast
import com.arnyminerz.paraulogic.ui.viewmodel.MainViewModel
import com.arnyminerz.paraulogic.utils.doAsync
import com.arnyminerz.paraulogic.utils.mapJsonObject
import com.arnyminerz.paraulogic.utils.toJsonArray
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.tasks.RuntimeExecutionException
import kotlinx.coroutines.flow.first
import org.json.JSONException
import timber.log.Timber

class MainActivity : LanguageActivity() {
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
        val data = result.data
        if (data != null) {
            val signInResult = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if (signInResult?.isSuccess == true) {
                val account = signInResult.signInAccount!!
                Timber.i("Signed in successfully.")
                toast(R.string.toast_signed_in)

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
        } else
            Timber.w("Cannot process sign in result since data is null.")
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

        setContent {
            AppTheme {
                MainScreen(viewModel, popupLauncher) {
                    startSignInIntent(signInClient, signInLauncher)
                }
            }
        }

        viewModel.loadGameInfo(signInClient, signInLauncher)
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

    override fun onStop() {
        super.onStop()

        doAsync {
            val account = signInSilently(signInClient)
            if (account != null)
                Timber.i("Log in successful")

            Timber.i("Trying to add missing points...")
            tryToAddPoints(this@MainActivity)
        }
    }
}
