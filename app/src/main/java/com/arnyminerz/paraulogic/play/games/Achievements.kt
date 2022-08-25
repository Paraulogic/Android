package com.arnyminerz.paraulogic.play.games

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.WorkerThread
import com.arnyminerz.paraulogic.R
import com.arnyminerz.paraulogic.game.GameInfo
import com.arnyminerz.paraulogic.storage.entity.IntroducedWord
import com.arnyminerz.paraulogic.storage.entity.associate
import com.arnyminerz.paraulogic.ui.toast
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.games.PlayGames
import timber.log.Timber

/**
 * Synchronizes and grants all the achievements that correspond to the user.
 * @author Arnau Mora
 * @since 20220825
 * @param activity The activity that is requesting the synchronization.
 * @param wordsList The registry of all the words the player has ever introduced.
 */
@WorkerThread
fun synchronizeAchievements(
    activity: Activity,
    history: List<GameInfo>,
    wordsList: List<IntroducedWord>,
) {
    val achievementsClient = PlayGames.getAchievementsClient(activity)

    val playedStrike = arrayListOf(0)
    val tutisStrike = arrayListOf(0)

    (history to wordsList)
        .associate()
        .entries
        .forEach { (_, entry) ->
            val (gi, words) = entry
            if (words.isEmpty() || words.find { it.isCorrect } == null)
            // If there's a non-played day, add a new strike registry
                playedStrike.add(0)
            else {
                // Increase the played strike last item by 1
                playedStrike[playedStrike.size - 1] = playedStrike.last() + 1

                // Iterate all tutis for GameInfo
                for (tuti in gi.tutis) {
                    // Try to find the tuti in the introduced words list
                    val w = words.find { it.hash == gi.hash && it.word.equals(tuti, true) }

                    // If not null, it means that the tuti was introduced. Then, increase strike
                    if (w != null)
                        tutisStrike[tutisStrike.size - 1] = tutisStrike.last() + 1
                    // If it's null, it means that not all tutis were found for the current gi. Then,
                    // add a new strike registry for tutis, and stop iterating current gi
                    else {
                        tutisStrike.add(0)
                        break
                    }
                }
            }
        }

    val playedForMaxDays = playedStrike.max()
    val allTutisForMaxDays = tutisStrike.max()

    Timber.i("You have played at most for $playedForMaxDays consecutive days.")
    Timber.i("You have found all tutis for at most $allTutisForMaxDays consecutive days.")

    // Check that the user has found at least one correct word
    if (wordsList.find { it.isCorrect } != null)
        achievementsClient.unlock(activity.getString(R.string.achievement_a_la_punta_de_la_llengua))

    // If strike is greater than 0 it means that at least 1 tuti was found
    if (allTutisForMaxDays > 0)
        achievementsClient.unlock(activity.getString(R.string.achievement_quin_embarbussament))

    // Play for 7 straight days
    if (playedForMaxDays >= 7)
        achievementsClient.unlock(activity.getString(R.string.achievement_fent_i_desfent_sensenya_la_gent))
    // Find all tutis for 7 straight days
    if (allTutisForMaxDays >= 7)
        achievementsClient.unlock(activity.getString(R.string.achievement_quin_embarbussament))
}

/**
 * Increments all word-related achievements.
 * @author Arnau Mora
 * @since 20220825
 * @param activity The activity that is requesting the update.
 * @param word The word that has been introduced.
 */
@WorkerThread
fun incrementAchievements(activity: Activity, word: IntroducedWord) {
    if (!word.isCorrect)
        return

    val achievementsClient = PlayGames.getAchievementsClient(activity)

    val ach50 = activity.getString(R.string.achievement_a_ja_va)
    val ach69 = activity.getString(R.string.achievement_tinc_la_figa_calenta)
    val ach100 = activity.getString(R.string.achievement_a_fer_punyetes)
    val ach200 = activity.getString(R.string.achievement_com_cagall_per_squia)
    val ach300 = activity.getString(R.string.achievement_sem_cau_la_cara_de_vergonya)
    val ach500 = activity.getString(R.string.achievement_mest_fent_la_guitza)

    // Find 50 words
    achievementsClient.increment(ach50, 1)
    // Find 69 words
    achievementsClient.increment(ach69, 1)
    // Find 100 words
    achievementsClient.increment(ach100, 1)
    // Find 200 words
    achievementsClient.increment(ach200, 1)
    // Find 300 words
    achievementsClient.increment(ach300, 1)
    // Find 500 words
    achievementsClient.increment(ach500, 1)
}

/**
 * Shows the achievements screen.
 * @author Arnau Mora
 * @since 20220824
 * @param activity The activity from which the window is launching.
 * @param launcher An [ActivityResultLauncher] with [Intent] source.
 * [ActivityResultContracts.StartActivityForResult] is a good option.
 */
fun showAchievements(activity: Activity, launcher: ActivityResultLauncher<Intent>) =
    PlayGames.getAchievementsClient(activity)
        .achievementsIntent
        .addOnSuccessListener {
            launcher.launch(it)
        }
        .addOnFailureListener { e ->
            Timber.e(e, "Could not display achievements")
            val statusCode = (e as? ApiException)?.statusCode
            if (statusCode == 4)
                activity.toast(R.string.toast_error_not_logged_in)
            else
                activity.toast(R.string.toast_error_achievements)
        }
