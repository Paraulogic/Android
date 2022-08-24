package com.arnyminerz.paraulogic.game

import android.app.Activity
import com.arnyminerz.paraulogic.play.games.loadIntroducedWords
import com.arnyminerz.paraulogic.storage.entity.IntroducedWord
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.RuntimeExecutionException
import com.google.firebase.perf.metrics.AddTrace
import org.json.JSONException
import timber.log.Timber

/**
 * Obtains the introduced words from the server.
 * @author Arnau Mora
 * @since 20220404
 * @param activity The Activity running from.
 * @param gameInfo The [GameInfo] with the info for today.
 * @param loadingGameProgressCallback Gets called when the progress starts being loaded.
 */
@AddTrace(name = "ServerDataLoad")
@Suppress("BlockingMethodInNonBlockingContext")
suspend fun getServerIntroducedWordsList(
    activity: Activity,
    gameInfo: GameInfo,
    loadingGameProgressCallback: suspend (finished: Boolean) -> Unit,
): List<IntroducedWord> =
    try {
        loadingGameProgressCallback(false)
        loadIntroducedWords(activity)
            .filter { it.hash == gameInfo.hash }
    } catch (e: JSONException) {
        Timber.e(e, "Could not parse JSON")
        null
    } catch (e: RuntimeExecutionException) {
        Timber.e(e, "There's no stored snapshot.")
        null
    } catch (e: ApiException) {
        Timber.e(e, "Could not read snapshot.")
        null
    } finally {
        loadingGameProgressCallback(true)
    } ?: emptyList()
