package com.arnyminerz.paraulogic.pref

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

val Context.dataStore by preferencesDataStore(name = "Paraulogic")

object PreferencesModule {
    val ErrorReportingEnabledKey = booleanPreferencesKey(PREF_ERROR_REPORTING)

    val Language = stringPreferencesKey(PREF_LANGUAGE)

    val DisableDonationDialog = booleanPreferencesKey(PREF_DISABLE_DONATE_DIALOG)

    val NumberOfLaunches = intPreferencesKey(PREF_NUMBER_OF_LAUNCHES)
}
