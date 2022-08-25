package com.arnyminerz.paraulogic.sound

import android.content.Context
import android.media.MediaPlayer
import androidx.annotation.RawRes
import androidx.annotation.WorkerThread
import com.arnyminerz.paraulogic.utils.append
import com.arnyminerz.paraulogic.utils.doAsync
import kotlinx.coroutines.delay

class SoundManager private constructor() {
    companion object {
        @Volatile
        private var INSTANCE: SoundManager? = null

        fun getInstance() =
            INSTANCE ?: synchronized(this) {
                SoundManager().also { INSTANCE = it }
            }
    }

    @Volatile
    private var soundsQueue = listOf<MediaPlayer>()

    /**
     * Starts playing a new sound, or adds it into the queue.
     * @author Arnau Mora
     * @since 250220825
     * @param context The context that wants to play the sound.
     * @param resId The resource id from `R.raw.*` of the media file to play.
     */
    fun playSound(context: Context, @RawRes resId: Int): SoundManager {
        val mp = MediaPlayer.create(context, resId)
        soundsQueue = soundsQueue
            .toMutableList()
            .append(mp)

        // This means there's just the current sound stored. Start a new thread that will observe
        // the playing status, and start playing new sounds from the queue.
        if (soundsQueue.size == 1)
            doAsync {
                do {
                    soundsQueue
                        .getOrNull(0)
                        ?.start()
                        ?: break
                    waitUntilFinishedPlaying(60000, 10)
                } while (soundsQueue.isNotEmpty())
            }

        return this
    }

    @WorkerThread
    private tailrec suspend fun waitUntilFinishedPlaying(
        maxDelay: Long,
        checkPeriod: Long
    ): Boolean {
        if (maxDelay < 0) return false
        val mp = soundsQueue.getOrNull(0)
        if (mp == null || !mp.isPlaying) {
            mp!!.release()
            soundsQueue = soundsQueue
                .toMutableList()
                .also { it.removeAt(0) }
            return true
        }
        delay(checkPeriod)
        return waitUntilFinishedPlaying(maxDelay - checkPeriod, checkPeriod)
    }
}