package com.arnyminerz.paraulogic.game

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.arnyminerz.paraulogic.singleton.VolleySingleton
import io.sentry.Sentry
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

private const val GAME_URL = "https://vilaweb.cat/paraulogic/"

/**
 * Fetches the source code of the game.
 * @author Arnau Mora
 * @since 20220307
 * @param context The context to run as.
 */
suspend fun fetchSource(context: Context) = suspendCoroutine<String> { cont ->
    val transaction = Sentry.startTransaction("server-data-load", "server-data-load")
    val request = StringRequest(
        Request.Method.GET,
        GAME_URL,
        { cont.resume(it); transaction.finish() },
        { cont.resumeWithException(it); transaction.finish() },
    )
    VolleySingleton
        .getInstance(context)
        .addToRequestQueue(request)
}

fun decodeSource(source: String): GameInfo {
    val tPos = source.indexOf("var t=")
    val sPos = source.indexOf(';', tPos)
    val data = source.substring(tPos, sPos)

    val lettersPos = data.indexOf("\"l\":[")
    val lettersEndPos = data.indexOf("]", lettersPos)
    val lettersArray = data.substring(lettersPos + 5, lettersEndPos)
    val splitLetters = lettersArray.split(',')
    val letters = arrayListOf<Char>()
    for (letter in splitLetters)
        letters.add(letter.replace("\"", "")[0])

    val wordsPos = data.indexOf("\"p\":{")
    val wordsEnd = data.indexOf("}", wordsPos)
    val wordsString = data.substring(wordsPos + 5, wordsEnd)
    val splitWords = wordsString.split(',')
    val correctWords = hashMapOf<String, String>()
    for (word in splitWords) {
        val splitWord = word.split(':')
        val key = splitWord[0]
            .replace("\"", "")
            .lowercase()
            .trim()
        val value = splitWord[1]
            .replace("\"", "")
            .trim()
        correctWords[key] = value
    }

    return GameInfo(
        mutableStateOf(letters.subList(0, letters.size - 1)),
        letters.last(),
        correctWords
    )
}
