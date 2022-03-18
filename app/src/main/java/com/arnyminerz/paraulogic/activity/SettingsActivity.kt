package com.arnyminerz.paraulogic.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import com.arnyminerz.paraulogic.activity.model.LanguageActivity
import com.arnyminerz.paraulogic.ui.screen.SettingsScreen
import com.arnyminerz.paraulogic.ui.theme.AppTheme

class SettingsActivity : LanguageActivity() {
    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                SettingsScreen()
            }
        }
    }
}
