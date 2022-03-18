package com.arnyminerz.paraulogic.utils

import android.app.Activity
import android.content.Context
import androidx.annotation.UiThread
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.datastore.preferences.core.edit
import com.arnyminerz.paraulogic.R
import com.arnyminerz.paraulogic.pref.PreferencesModule
import com.arnyminerz.paraulogic.pref.dataStore
import timber.log.Timber
import java.util.Locale

/**
 * Updates the user preference about the app's locale.
 * @author Arnau Mora
 * @since 20220317
 * @param locales The list of locales ordered by user's preference.
 */
@UiThread
fun Context.updateAppLocales(vararg locales: Locale) {
    Timber.v("Setting application locales...")
    AppCompatDelegate
        .setApplicationLocales(
            LocaleListCompat.create(*locales)
        )

    doAsync {
        Timber.v("Storing language preference into DataStore...")
        if (locales.isNotEmpty())
            dataStore.edit {
                it[PreferencesModule.Language] = locales[0].isO3Country
            }
        else
            Timber.w("Will not update language preference since locales is empty.")
    }
}

/**
 * Returns the currently selected Locale.
 * @author Arnau Mora
 * @since 20220317
 */
fun Activity.getLocale(): Locale =
    AppCompatDelegate.getApplicationLocales()
        .takeIf { !it.isEmpty }
        ?.get(0)
        ?: Locale.forLanguageTag(getString(R.string.system_locale))
