package com.arnyminerz.paraulogic.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.arnyminerz.paraulogic.pref.PREF_ERROR_REPORTING
import com.arnyminerz.paraulogic.pref.rememberBooleanPreference
import com.arnyminerz.paraulogic.ui.elements.SettingsItem
import com.arnyminerz.paraulogic.utils.activity

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
        Column(modifier = Modifier.fillMaxWidth()) {
            var enableErrorReporting by rememberBooleanPreference(
                PREF_ERROR_REPORTING,
                initialValue = true,
                defaultValue = true
            )

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
