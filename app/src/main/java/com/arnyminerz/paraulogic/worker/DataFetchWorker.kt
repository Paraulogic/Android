package com.arnyminerz.paraulogic.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.arnyminerz.paraulogic.annotation.LoadError
import com.arnyminerz.paraulogic.game.loadGameInfoFromServer
import com.arnyminerz.paraulogic.singleton.DatabaseSingleton
import com.arnyminerz.paraulogic.storage.entity.GameInfoEntity
import com.arnyminerz.paraulogic.utils.failure
import com.google.firebase.FirebaseException
import timber.log.Timber
import java.util.Calendar

class DataFetchWorker(appContext: Context, workerParameters: WorkerParameters) :
    CoroutineWorker(appContext, workerParameters) {
    override suspend fun doWork(): Result {
        Timber.i("Loading GameInfo from server...")
        val gameInfo = try {
            loadGameInfoFromServer(applicationContext)
        } catch (e: NoSuchElementException) {
            Timber.e(e, "Could not get game info from server.")
            return failure(LoadError.RESULT_NO_SUCH_ELEMENT)
        } catch (e: FirebaseException) {
            Timber.e(e, "Could not get game info from server.")
            return failure(LoadError.RESULT_FIREBASE_EXCEPTION)
        }
        val now = Calendar.getInstance().time

        Timber.i("Storing GameInfo (${gameInfo.letters}, ${gameInfo.centerLetter})...")
        val gameInfoDao = DatabaseSingleton.getInstance(applicationContext).db.gameInfoDao()
        gameInfoDao.put(
            GameInfoEntity.fromGameInfo(now.time, gameInfo)
        )

        return Result.success()
    }
}