package com.arnyminerz.paraulogic.ui

import android.content.Context
import android.widget.Toast
import androidx.annotation.IntDef
import androidx.annotation.StringRes

@IntDef(Toast.LENGTH_SHORT, Toast.LENGTH_LONG)
annotation class ToastDuration

fun Context.toast(
    text: String,
    @ToastDuration duration: Int = Toast.LENGTH_SHORT,
) = Toast.makeText(this, text, duration)
    .show()

fun Context.toast(
    @StringRes text: Int,
    @ToastDuration duration: Int = Toast.LENGTH_SHORT,
) =
    toast(getString(text), duration)

fun Context.toast(@StringRes text: Int, vararg args: Any) =
    toast(getString(text, args))
