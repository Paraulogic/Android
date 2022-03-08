package com.arnyminerz.paraulogic

import android.app.Application
import com.arnyminerz.paraulogic.log.CrashReportingTree
import timber.log.Timber
import timber.log.Timber.Forest.plant

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG)
            plant(Timber.DebugTree())
        else
            plant(CrashReportingTree())
    }
}