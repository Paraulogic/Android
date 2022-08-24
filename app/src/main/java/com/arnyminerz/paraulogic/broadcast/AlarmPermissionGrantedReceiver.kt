package com.arnyminerz.paraulogic.broadcast

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import timber.log.Timber
import java.util.Calendar

class AlarmPermissionGrantedReceiver : BroadcastReceiver() {
    companion object {
        fun scheduleAlarm(context: Context) {
            val alarmManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                context.getSystemService(AlarmManager::class.java)
            else
                context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val calendar = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                // Call at 00:00
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
            }
            Timber.i("Scheduling alarm for new data notifications...")
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                Intent(ACTION_UPDATE_CLOCK).let {
                    PendingIntent.getBroadcast(
                        context,
                        0,
                        it,
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                            PendingIntent.FLAG_IMMUTABLE
                        else 0,
                    )
                },
            )
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action
        if (action != AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED)
            return

        if (context == null)
            return

        scheduleAlarm(context)
    }
}