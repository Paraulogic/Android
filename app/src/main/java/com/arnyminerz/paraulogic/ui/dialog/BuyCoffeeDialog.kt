package com.arnyminerz.paraulogic.ui.dialog

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.datastore.preferences.core.edit
import com.arnyminerz.paraulogic.R
import com.arnyminerz.paraulogic.pref.PreferencesModule
import com.arnyminerz.paraulogic.pref.dataStore
import com.arnyminerz.paraulogic.utils.doAsync
import com.arnyminerz.paraulogic.utils.launchUrl
import com.arnyminerz.paraulogic.utils.uiContext
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase

@Composable
fun BuyCoffeeDialog(showingDialog: Boolean, onDismissRequest: () -> Unit) {
    val context = LocalContext.current

    if (showingDialog)
        AlertDialog(
            onDismissRequest = onDismissRequest,
            title = {
                Text(
                    text = stringResource(R.string.dialog_coffee_title),
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.dialog_coffee_message),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        Firebase.analytics
                            .logEvent(FirebaseAnalytics.Event.SELECT_CONTENT) {
                                param(FirebaseAnalytics.Param.ITEM_NAME, "coffee_url")
                            }
                        context.launchUrl("https://ko-fi.com/arnyminerz")
                    },
                ) {
                    Text(
                        text = stringResource(R.string.action_buy_coffee),
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        doAsync {
                            context.dataStore.edit {
                                it[PreferencesModule.ShownDonateDialog] = true
                            }
                            uiContext { onDismissRequest() }
                        }
                    },
                ) {
                    Text(
                        text = stringResource(R.string.action_not_show_again),
                    )
                }
            },
        )
}