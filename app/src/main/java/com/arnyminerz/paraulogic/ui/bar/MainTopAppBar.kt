package com.arnyminerz.paraulogic.ui.bar

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.arnyminerz.paraulogic.R
import com.arnyminerz.paraulogic.play.games.showAchievements
import com.arnyminerz.paraulogic.ui.viewmodel.MainViewModel
import com.arnyminerz.paraulogic.utils.launchUrl
import com.google.android.gms.common.images.ImageManager
import com.google.android.gms.games.PlayGames
import timber.log.Timber

/**
 * Displays the top app bar for the main app screen. Contains the Vilaweb's logo, the Paraulògic's
 * logo, and the login button, which also serves as the achievements viewer.
 * @author Arnau Mora
 * @since 20220323
 */
@ExperimentalMaterial3Api
@Composable
fun AppCompatActivity.MainTopAppBar(
    viewModel: MainViewModel,
    popupLauncher: ActivityResultLauncher<Intent>,
) {
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
                alignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth(.6f)
                    .height(56.dp),
            )
        },
        actions = {
            if (viewModel.isAuthenticated == false)
                IconButton(
                    onClick = {
                        PlayGames.getGamesSignInClient(this@MainTopAppBar)
                            .signIn()
                            .addOnFailureListener {
                                Timber.e(it, "Could not sign in.")
                            }
                            .addOnCompleteListener {
                                viewModel.loadAuthenticatedState(this@MainTopAppBar)
                            }
                    },
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
                    showAchievements(this@MainTopAppBar, popupLauncher)
                }

                val player = viewModel.player
                if (player == null || !player.hasIconImage()) {
                    Timber.w("User data still not available or null. Has icon image: ${player?.hasIconImage()}")
                    IconButton(
                        onClick = showAchievements,
                        modifier = Modifier
                            .size(48.dp)
                    ) {
                        Icon(
                            Icons.Outlined.AccountCircle,
                            contentDescription = stringResource(R.string.image_desc_loading)
                        )
                    }
                } else {
                    var src: Any by remember { mutableStateOf(R.drawable.round_account_circle_24) }

                    if (src is Int)
                        ImageManager.create(this@MainTopAppBar)
                            .loadImage({ uri, drawable, isRequestedDrawable ->
                                src = if (isRequestedDrawable)
                                    drawable!!
                                else
                                    uri
                            }, player.iconImageUri!!, R.drawable.round_account_circle_24)

                    AsyncImage(
                        src,
                        contentDescription = stringResource(R.string.image_desc_profile),
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(48.dp)
                            .clickable(onClick = showAchievements)
                            .clip(CircleShape),
                    )
                }
            }
        },
    )
}
