package com.arnyminerz.paraulogic.utils

import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

fun doAsync(@WorkerThread call: suspend CoroutineScope.() -> Unit) {
    CoroutineScope(Dispatchers.IO).launch { call(this) }
}

/**
 * Runs the code on [call] asynchronously in the UI thread.
 * @author Arnau Mora
 * @since 20220404
 * @param call The code block to run.
 */
fun doOnUi(@UiThread call: suspend CoroutineScope.() -> Unit) =
    CoroutineScope(Dispatchers.Main).launch { call(this) }

/**
 * Runs [call] on the UI thread.
 * @author Arnau Mora
 * @since 20220309
 */
suspend fun uiContext(@UiThread call: suspend CoroutineScope.() -> Unit) {
    withContext(Dispatchers.Main) { call(this) }
}
