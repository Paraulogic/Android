package com.arnyminerz.paraulogic.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity

/**
 * Tries to get the Activity instance from context.
 * @author Arnau Mora
 * @since 20220308
 */
val Context.activity: Activity?
    get() = when (this) {
        is AppCompatActivity -> this
        is ComponentActivity -> this
        is ContextWrapper -> baseContext.activity
        else -> null
    }
