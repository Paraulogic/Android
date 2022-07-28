package com.arnyminerz.paraulogic

import android.app.Application
import com.arnyminerz.paraulogic.log.CrashReportingTree
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import timber.log.Timber
import timber.log.Timber.Forest.plant

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG)
            plant(Timber.DebugTree())
        else
            plant(CrashReportingTree())

        Timber.i("Subscribing to \"gameInfo\" messaging topic.")
        Firebase.messaging.subscribeToTopic("gameInfo")
            .addOnSuccessListener {
                Timber.i("Subscribed to \"gameInfo\".")
            }
            .addOnFailureListener { Timber.e(it, "Could not subscribe to \"gameInfo\".") }
    }
}