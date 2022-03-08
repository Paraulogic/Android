package com.arnyminerz.paraulogic.utils

import androidx.annotation.WorkerThread
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun doAsync(@WorkerThread call: suspend CoroutineScope.() -> Unit) {
    CoroutineScope(Dispatchers.IO).launch { call(this) }
}
