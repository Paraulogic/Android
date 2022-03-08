package com.arnyminerz.paraulogic.game

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.arnyminerz.paraulogic.singleton.VolleySingleton
import com.google.firebase.perf.metrics.AddTrace
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
@AddTrace(name = "SourceFetching")
suspend fun fetchSource(context: Context) = suspendCoroutine<String> { cont ->
    val request = StringRequest(
        Request.Method.GET,
        GAME_URL,
        { cont.resume(it) },
        { cont.resumeWithException(it); },
    )
    VolleySingleton
        .getInstance(context)
        .addToRequestQueue(request)
}

@AddTrace(name = "SourceDecoding")
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
