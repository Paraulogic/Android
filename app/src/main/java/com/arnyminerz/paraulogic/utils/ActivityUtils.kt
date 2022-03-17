package com.arnyminerz.paraulogic.utils

import android.app.Activity
import android.app.LocaleManager
import android.os.Build
import android.os.LocaleList
import androidx.annotation.RequiresApi
import com.arnyminerz.paraulogic.R
import java.util.Locale

/**
 * Updates the user preference about the app's locale.
 * @author Arnau Mora
 * @since 20220317
 * @param locales The list of locales ordered by user's preference.
 */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
fun Activity.updateAppLocales(vararg locales: Locale) {
    val localeManager = getSystemService(LocaleManager::class.java)
    localeManager.applicationLocales = LocaleList(*locales)
}

/**
 * Returns the currently selected Locale.
 * @author Arnau Mora
 * @since 20220317
 */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
fun Activity.getLocale(): Locale = getSystemService(LocaleManager::class.java)
    .applicationLocales
    .takeIf { !it.isEmpty }
    ?.get(0)
    ?: Locale.forLanguageTag(getString(R.string.system_locale))
