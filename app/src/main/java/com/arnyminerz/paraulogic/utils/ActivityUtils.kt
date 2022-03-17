package com.arnyminerz.paraulogic.utils

import android.app.Activity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.arnyminerz.paraulogic.R
import java.util.Locale

/**
 * Updates the user preference about the app's locale.
 * @author Arnau Mora
 * @since 20220317
 * @param locales The list of locales ordered by user's preference.
 */
fun updateAppLocales(vararg locales: Locale) =
    AppCompatDelegate
        .setApplicationLocales(
            LocaleListCompat.create(*locales)
        )

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
