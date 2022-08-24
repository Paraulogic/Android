package com.arnyminerz.paraulogic.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import timber.log.Timber

/**
 * Starts the [activity] with the set [parameters].
 * @author Arnau Mora
 * @since 20220307
 * @param activity The class type of the Activity to launch.
 * @param parameters Used to apply some options to the internal [Intent].
 */
fun Context.launch(activity: Class<*>, parameters: Intent.() -> Unit = {}) {
    startActivity(
        Intent(this, activity)
            .apply(parameters)
    )
}

/**
 * Launches the set [uri] as [action] with the set [parameters].
 * @author Arnau Mora
 * @since 20220307
 * @param action The action to run.
 * @param uri The uri to use.
 * @param parameters Used to apply some options to the internal [Intent].
 */
fun Context.launch(action: String, uri: Uri, parameters: Intent.() -> Unit = {}) =
    startActivity(
        Intent(action, uri)
            .apply(parameters)
    )

/**
 * Launches the [url] into the default web browser.
 * @author Arnau Mora
 * @since 20220307
 * @param url The url to launch
 */
fun Context.launchUrl(url: String) = try {
    launch(Intent.ACTION_VIEW, Uri.parse(url))
} catch (e: ActivityNotFoundException) {
    Timber.w(e, "Could not launch URL: $url.")
}
