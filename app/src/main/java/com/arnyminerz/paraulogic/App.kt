package com.arnyminerz.paraulogic

import android.app.Application
import android.content.IntentFilter
import com.arnyminerz.paraulogic.broadcast.ACTION_UPDATE_CLOCK
import com.arnyminerz.paraulogic.broadcast.UpdateGameDataReceiver
import com.arnyminerz.paraulogic.log.CrashReportingTree
import timber.log.Timber
import timber.log.Timber.Forest.plant

class App : Application() {
    private val br = UpdateGameDataReceiver()

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG)
            plant(Timber.DebugTree())
        else
            plant(CrashReportingTree())

        val filter = IntentFilter(ACTION_UPDATE_CLOCK)
        registerReceiver(br, filter)
    }
}