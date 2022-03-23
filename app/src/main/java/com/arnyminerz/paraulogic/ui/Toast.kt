package com.arnyminerz.paraulogic.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.arnyminerz.paraulogic.R

/**
 * Shows a toast with the set background color ([containerColor]) and text color ([textColor]).
 * @author Arnau Mora
 * @since 20220323
 * @param context The [Context] that is showing the toast.
 * @param text The text to display in the toast.
 * @param containerColor The background color of the toast.
 * @param textColor The text color of the toast.
 */
@Suppress("DEPRECATION")
@SuppressLint("InflateParams")
fun customToast(
    context: Context,
    text: String,
    containerColor: Color,
    textColor: Color,
) {
    val view = LayoutInflater.from(context)
        .inflate(R.layout.toast_layout, null)
    val toast = Toast(context)
    view.findViewById<LinearLayout>(R.id.toast_root).apply {
        backgroundTintList = ColorStateList.valueOf(containerColor.toArgb())
    }
    view.findViewById<TextView>(R.id.message).apply {
        setTextColor(textColor.toArgb())
        setText(text)
    }
    toast.setGravity(Gravity.TOP, 0, 250)
    toast.view = view
    toast.show()
}

/**
 * Shows a toast with the set background color ([containerColor]) and text color ([textColor]).
 * @author Arnau Mora
 * @since 20220323
 * @param context The [Context] that is showing the toast.
 * @param text The string resource of the text to display on the toast.
 * @param containerColor The background color of the toast.
 * @param textColor The text color of the toast.
 */
fun customToast(
    context: Context,
    @StringRes text: Int,
    containerColor: Color,
    textColor: Color,
) = customToast(context, context.getString(text), containerColor, textColor)
