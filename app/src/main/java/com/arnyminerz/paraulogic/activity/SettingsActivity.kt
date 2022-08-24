package com.arnyminerz.paraulogic.activity

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import com.arnyminerz.paraulogic.ui.screen.SettingsScreen
import com.arnyminerz.paraulogic.ui.theme.AppTheme
import com.arnyminerz.paraulogic.ui.utils.LockScreenOrientation

class SettingsActivity : AppCompatActivity() {
    @OptIn(
        ExperimentalMaterial3Api::class,
        ExperimentalMaterialApi::class,
        ExperimentalMaterial3WindowSizeClassApi::class,
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                val windowSizeClass = calculateWindowSizeClass(this)

                LockScreenOrientation(
                    when (windowSizeClass.widthSizeClass) {
                        WindowWidthSizeClass.Compact -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                        else -> ActivityInfo.SCREEN_ORIENTATION_SENSOR
                    }
                )

                SettingsScreen()
            }
        }
    }
}
