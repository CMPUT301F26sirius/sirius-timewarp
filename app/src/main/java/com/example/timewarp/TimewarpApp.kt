package com.example.timewarp

import android.app.Application
import androidx.work.BackoffPolicy
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.hilt.work.HiltWorkerFactory
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

// Configuration for auto-draw worker
const val DRAW_INTERVAL_MINUTES = 15L
const val LOTTERY_DRAW_WORK_NAME = "lottery-draw-poll"

/**
 * Entry point for the app, spawns workers.
 */
@HiltAndroidApp
class TimewarpApp: Application(), Configuration.Provider {
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    // Entry
    override fun onCreate() {
        super.onCreate()

        // Setup auto-draw worker
        val request = PeriodicWorkRequestBuilder<LotteryDrawWorker>(DRAW_INTERVAL_MINUTES, TimeUnit.MINUTES)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                DRAW_INTERVAL_MINUTES,
                TimeUnit.MINUTES,
            )
            .build()

        // Schedule auto-draw worker
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            LOTTERY_DRAW_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }
}