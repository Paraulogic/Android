package com.arnyminerz.paraulogic.service

import androidx.compose.runtime.mutableStateOf
import com.arnyminerz.paraulogic.game.GameInfo
import com.arnyminerz.paraulogic.singleton.DatabaseSingleton
import com.arnyminerz.paraulogic.storage.entity.GameInfoEntity
import com.arnyminerz.paraulogic.utils.map
import com.arnyminerz.paraulogic.utils.toMap
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Locale

class MessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        Timber.d("Got new Firebase Messaging Token: $token")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val data = message.data
        if (data.isNotEmpty()) {
            val letters = data
                .takeIf { it.containsKey("letters") }
                ?.let { JSONArray(data.getValue("letters")) }
                ?.map<String, Char> { it.toCharArray()[0] }
            val centerLetter = data["centerLetter"]
                ?.toCharArray()
                ?.takeIf { it.isNotEmpty() }
                ?.get(0)

            @Suppress("UNCHECKED_CAST")
            val words = data
                .takeIf { it.containsKey("words") }
                ?.let { JSONObject(data.getValue("words")) }
                ?.toMap() as? Map<String, String>?
            val date = data["date"]
                ?.let { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it) }

            if (letters?.isNotEmpty() == true && centerLetter != null && words?.isNotEmpty() == true && date != null) {
                Timber.i("Got Game data for $date.")
                val db = DatabaseSingleton.getInstance(applicationContext).db

                Timber.d("Parsing GameInfo...")
                val gameInfo = GameInfo(date, mutableStateOf(letters), centerLetter, words)

                Timber.d("Storing GameInfo in db...")
                db.gameInfoDao()
                    .put(GameInfoEntity.fromGameInfo(gameInfo))
            } else
                Timber.e("Data received from Messaging is not valid: $data")
        }
    }
}
