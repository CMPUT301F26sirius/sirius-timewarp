package com.example.timewarp

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.timewarp.domain.usecase.LotteryDrawPollerUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CancellationException

// Debug tag
const val TAG = "LotteryDrawWorker"

/**
 * NEEDS_DOCUMENTATION
 */
@HiltWorker
class LotteryDrawWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val poller: LotteryDrawPollerUseCase,
) : CoroutineWorker(appContext, workerParams) {
    // Attempt to call pollOnce
    override suspend fun doWork(): Result {
        return try {
            poller.pollOnce()
            Result.success()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (error: Exception) {
            Log.e(TAG, "Lottery draw poll failed; scheduling retry", error)
            Result.retry()
        }
    }
}
