package com.arnyminerz.paraulogic.log

import android.util.Log
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import timber.log.Timber

class CrashReportingTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority == Log.VERBOSE || priority == Log.DEBUG)
            return

        val crashlytics = Firebase.crashlytics
        crashlytics.setCustomKey("priority", priority)
        if (tag != null)
            crashlytics.setCustomKey("tag", tag)
        crashlytics.log(message)
    }
}