package com.arnyminerz.paraulogic.utils

import androidx.work.ListenableWorker
import androidx.work.workDataOf

const val WORKER_DATA_ERROR = "error"

fun failure(errorCode: Int) =
    ListenableWorker.Result.failure(workDataOf(WORKER_DATA_ERROR to errorCode))
