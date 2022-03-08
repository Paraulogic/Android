package com.arnyminerz.paraulogic.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.arnyminerz.paraulogic.R
import com.arnyminerz.paraulogic.activity.FeedbackActivity
import com.arnyminerz.paraulogic.pref.PREF_ERROR_REPORTING
import com.arnyminerz.paraulogic.pref.rememberBooleanPreference
import com.arnyminerz.paraulogic.ui.elements.SettingsCategory
import com.arnyminerz.paraulogic.ui.elements.SettingsItem
import com.arnyminerz.paraulogic.utils.activity
import com.arnyminerz.paraulogic.utils.launch
import com.arnyminerz.paraulogic.utils.launchUrl

@Composable
@ExperimentalMaterial3Api
@ExperimentalMaterialApi
fun SettingsScreen() {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(text = "Settings")
                },
                navigationIcon = {
                    IconButton(onClick = { context.activity?.onBackPressed() }) {
                        Icon(
                            Icons.Rounded.ChevronLeft,
                            contentDescription = stringResource(R.string.action_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors()
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(it)
        ) {
            var enableErrorReporting by rememberBooleanPreference(
                PREF_ERROR_REPORTING,
                initialValue = true,
                defaultValue = true
            )

            SettingsCategory(text = stringResource(R.string.settings_category_info))
            SettingsItem(
                title = stringResource(R.string.settings_info_github_title),
                subtitle = stringResource(R.string.settings_info_github_summary),
                onClick = { context.launchUrl("https://github.com/ArnyminerZ/Paraulogic-Android") }
            )
            SettingsItem(
                title = stringResource(R.string.settings_info_feedback_title),
                subtitle = stringResource(R.string.settings_info_feedback_summary),
                onClick = { context.launch(FeedbackActivity::class.java) }
            )

            SettingsCategory(text = stringResource(R.string.settings_category_advanced))
            SettingsItem(
                title = stringResource(R.string.settings_error_reporting_title),
                subtitle = stringResource(R.string.settings_error_reporting_summary),
                stateBoolean = enableErrorReporting,
                setBoolean = { state -> enableErrorReporting = state },
                onClick = { enableErrorReporting = !enableErrorReporting },
                switch = true,
            )
        }
    }
}

@Composable
@Preview(showSystemUi = true)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
fun SettingsScreenPreview() {
    SettingsScreen()
}
