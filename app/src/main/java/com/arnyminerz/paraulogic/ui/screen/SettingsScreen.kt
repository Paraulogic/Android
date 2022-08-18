package com.arnyminerz.paraulogic.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.arnyminerz.paraulogic.BuildConfig
import com.arnyminerz.paraulogic.R
import com.arnyminerz.paraulogic.activity.DonationsActivity
import com.arnyminerz.paraulogic.activity.FeedbackActivity
import com.arnyminerz.paraulogic.pref.PREF_ANALYTICS
import com.arnyminerz.paraulogic.pref.PREF_ERROR_REPORTING
import com.arnyminerz.paraulogic.pref.rememberBooleanPreference
import com.arnyminerz.paraulogic.ui.dialog.LanguageDialog
import com.arnyminerz.paraulogic.ui.elements.SettingsCategory
import com.arnyminerz.paraulogic.ui.elements.SettingsItem
import com.arnyminerz.paraulogic.utils.activity
import com.arnyminerz.paraulogic.utils.launch
import com.arnyminerz.paraulogic.utils.launchUrl
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase

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
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                var enableErrorReporting by rememberBooleanPreference(
                    PREF_ERROR_REPORTING,
                    initialValue = true,
                    defaultValue = true
                )
                var enableAnalytics by rememberBooleanPreference(
                    PREF_ANALYTICS,
                    initialValue = true,
                    defaultValue = true,
                )

                val analytics = Firebase.analytics
                val crashlytics = Firebase.crashlytics

                var showLanguageDialog by remember { mutableStateOf(false) }
                if (showLanguageDialog)
                    LanguageDialog { showLanguageDialog = false }

                SettingsCategory(text = stringResource(R.string.settings_category_general))
                SettingsItem(
                    title = stringResource(R.string.settings_general_language_title),
                    subtitle = stringResource(R.string.settings_general_language_summary),
                    onClick = { showLanguageDialog = true }
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
                SettingsItem(
                    title = stringResource(R.string.settings_info_donation_title),
                    subtitle = stringResource(R.string.settings_info_donation_summary),
                    onClick = { context.launch(DonationsActivity::class.java) }
                )

                SettingsCategory(text = stringResource(R.string.settings_category_advanced))
                SettingsItem(
                    title = stringResource(R.string.settings_error_reporting_title),
                    subtitle = stringResource(R.string.settings_error_reporting_summary),
                    stateBoolean = enableErrorReporting,
                    setBoolean = { state ->
                        enableErrorReporting = state
                        crashlytics.setCrashlyticsCollectionEnabled(enableErrorReporting)
                    },
                    onClick = {
                        enableErrorReporting = !enableErrorReporting
                        crashlytics.setCrashlyticsCollectionEnabled(enableErrorReporting)
                    },
                    switch = true,
                )
                SettingsItem(
                    title = stringResource(R.string.settings_analytics_title),
                    subtitle = stringResource(R.string.settings_analytics_summary),
                    stateBoolean = enableAnalytics,
                    setBoolean = { state ->
                        enableAnalytics = state
                        analytics.setAnalyticsCollectionEnabled(enableAnalytics)
                    },
                    onClick = {
                        enableAnalytics = !enableAnalytics
                        analytics.setAnalyticsCollectionEnabled(enableAnalytics)
                    },
                    switch = true,
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
            ) {
                Text(
                    stringResource(
                        R.string.settings_footer,
                        BuildConfig.VERSION_NAME,
                        BuildConfig.VERSION_CODE
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier
                        .padding(8.dp),
                )
            }
        }
    }
}

@Composable
@Preview(showSystemUi = true)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
fun SettingsScreenPreview() {
    SettingsScreen()
}
