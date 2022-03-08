package com.arnyminerz.paraulogic.utils

import android.content.Context
import android.content.ContextWrapper
import androidx.appcompat.app.AppCompatActivity

/**
 * Tries to get the Activity instance from context.
 * @author Arnau Mora
 * @since 20220308
 */
val Context.activity: AppCompatActivity?
    get() = when (this) {
        is AppCompatActivity -> this
        is ContextWrapper -> baseContext.activity
        else -> null
    }
