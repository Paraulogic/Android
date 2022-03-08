package com.arnyminerz.paraulogic

import android.app.Application
import com.arnyminerz.paraulogic.pref.PreferencesModule
import com.arnyminerz.paraulogic.pref.dataStore
import io.sentry.SentryLevel
import io.sentry.android.core.SentryAndroid
import io.sentry.android.timber.SentryTimberIntegration
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import timber.log.Timber.Forest.plant

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        SentryAndroid.init(this) { options ->
            if (!BuildConfig.DEBUG)
                options.addIntegration(
                    SentryTimberIntegration(
                        minEventLevel = SentryLevel.ERROR,
                        minBreadcrumbLevel = SentryLevel.INFO,
                    )
                )
            else
                plant(Timber.DebugTree())
            val errorReportingEnabled = runBlocking {
                dataStore
                    .data
                    .map { preferences -> preferences[PreferencesModule.ErrorReportingEnabledKey] }
                    .first()
            }
            options.enableAllAutoBreadcrumbs(errorReportingEnabled ?: true)
        }
    }
}