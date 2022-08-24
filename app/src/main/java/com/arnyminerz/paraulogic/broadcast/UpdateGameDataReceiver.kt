package com.arnyminerz.paraulogic.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.arnyminerz.paraulogic.broadcast.AlarmPermissionGrantedReceiver.Companion.scheduleAlarm
import com.arnyminerz.paraulogic.game.fetchAndStoreGameInfo
import com.arnyminerz.paraulogic.game.gameInfoForToday
import com.arnyminerz.paraulogic.utils.doAsync
import timber.log.Timber

const val ACTION_UPDATE_CLOCK = "com.arnyminerz.paraulogic.action.CLOCK"
const val ACTION_UPDATE_GAME_DATA = "com.arnyminerz.paraulogic.action.NEW_DATA"

class UpdateGameDataReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        Timber.i("Received clock tick!")

        doAsync {
            val gameInfo = gameInfoForToday(context)
            if (gameInfo == null) {
                fetchAndStoreGameInfo(context)

                // Notify that the data was updated
                context.sendBroadcast(Intent(ACTION_UPDATE_GAME_DATA))
            } else
                Timber.d("There's no new GameInfo.")
        }

        scheduleAlarm(context)
    }
}
