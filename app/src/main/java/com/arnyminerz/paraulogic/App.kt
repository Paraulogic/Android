package com.arnyminerz.paraulogic

import android.app.Application
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.arnyminerz.paraulogic.log.CrashReportingTree
import com.arnyminerz.paraulogic.worker.DataFetchWorker
import timber.log.Timber
import timber.log.Timber.Forest.plant
import java.util.concurrent.TimeUnit

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG)
            plant(Timber.DebugTree())
        else
            plant(CrashReportingTree())

        Timber.i("Programming data fetcher...")
        PeriodicWorkRequestBuilder<DataFetchWorker>(24, TimeUnit.HOURS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()
            .let { WorkManager.getInstance(this).enqueue(it) }
    }
}