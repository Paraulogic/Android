package com.arnyminerz.paraulogic.activity.model

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.arnyminerz.paraulogic.pref.PreferencesModule
import com.arnyminerz.paraulogic.pref.dataStore
import com.arnyminerz.paraulogic.utils.doAsync
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.util.Locale

abstract class LanguageActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase)

        doAsync {
            Timber.v("Getting language preference from DataStore...")
            val language = dataStore.data.first()[PreferencesModule.Language]
            if (language != null) {
                Timber.i("Setting application locale to \"$language\"")
                AppCompatDelegate
                    .setApplicationLocales(
                        LocaleListCompat.create(
                            Locale(language)
                        )
                    )
            }
        }
    }
}