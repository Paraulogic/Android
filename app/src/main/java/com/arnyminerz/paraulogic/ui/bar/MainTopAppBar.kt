package com.arnyminerz.paraulogic.ui.bar

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.arnyminerz.paraulogic.R
import com.arnyminerz.paraulogic.utils.launchUrl
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.games.Games
import timber.log.Timber

/**
 * Displays the top app bar for the main app screen. Contains the Vilaweb's logo, the Paraulògic's
 * logo, and the login button, which also serves as the achievements viewer.
 * @author Arnau Mora
 * @since 20220323
 * @param achievementsLauncher Should request the Google Api to show the achievements for the
 * currently logged in user.
 * @param signInRequest When login is required, this will get called.
 */
@ExperimentalMaterial3Api
@Composable
fun MainTopAppBar(
    achievementsLauncher: ActivityResultLauncher<Intent>,
    signInRequest: () -> Unit,
) {
    val context = LocalContext.current

    CenterAlignedTopAppBar(
        navigationIcon = {
            Image(
                painterResource(R.drawable.ic_logo_vilaweb),
                contentDescription = stringResource(R.string.image_desc_vilaweb),
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(48.dp)
                    .clickable { context.launchUrl("https://vilaweb.cat") }
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
                        .addOnSuccessListener { achievementsLauncher.launch(it) }
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
                            Icons.Outlined.AccountCircle,
                            contentDescription = stringResource(R.string.image_desc_login)
                        )
                    }
                } else
                    AsyncImage(
                        model = account.photoUrl,
                        contentDescription = stringResource(R.string.image_desc_profile),
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(48.dp)
                            .clickable(onClick = showAchievements)
                    )
            }
        },
    )
}
