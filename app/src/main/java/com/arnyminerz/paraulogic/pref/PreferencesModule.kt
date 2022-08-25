package com.arnyminerz.paraulogic.pref

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

val Context.dataStore by preferencesDataStore(name = "Paraulogic")

val PrefErrorReportingEnabled = booleanPreferencesKey(PREF_ERROR_REPORTING)

val PrefLanguage = stringPreferencesKey(PREF_LANGUAGE)

val PrefDisableDonationDialog = booleanPreferencesKey(PREF_DISABLE_DONATE_DIALOG)

val PrefNumberOfLaunches = intPreferencesKey(PREF_NUMBER_OF_LAUNCHES)

object PreferencesModule {
    @Deprecated(
        "Use direct access values.",
        replaceWith = ReplaceWith("PrefErrorReportingEnabled")
    )
    val ErrorReportingEnabledKey = booleanPreferencesKey(PREF_ERROR_REPORTING)

    @Deprecated(
        "Use direct access values.",
        replaceWith = ReplaceWith("PrefLanguage")
    )
    val Language = stringPreferencesKey(PREF_LANGUAGE)

    @Deprecated(
        "Use direct access values.",
        replaceWith = ReplaceWith("PrefDisableDonationDialog")
    )
    val DisableDonationDialog = booleanPreferencesKey(PREF_DISABLE_DONATE_DIALOG)

    @Deprecated(
        "Use direct access values.",
        replaceWith = ReplaceWith("PrefNumberOfLaunches")
    )
    val NumberOfLaunches = intPreferencesKey(PREF_NUMBER_OF_LAUNCHES)
}
