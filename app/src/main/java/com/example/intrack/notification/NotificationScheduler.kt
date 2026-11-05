package com.example.intrack.notification

import android.content.Context
import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

object NotificationScheduler {

    private const val TAG = "NotificationScheduler"
    const val PERIODIC_WORK_NAME = "intrack_periodic_notifications_work"
    private const val IMMEDIATE_WORK_NAME = "intrack_immediate_notifications_check"

    /**
     * Schedules daily periodic evaluation using WorkManager.
     * Calculates the initial delay so the check executes at the user's preferred time.
     */
    fun schedulePeriodicWork(context: Context) {
        val prefs = NotificationPreferences(context)
        if (!prefs.pushNotificationsEnabled) {
            Log.d(TAG, "Push notifications disabled; cancelling periodic work.")
            cancelPeriodicWork(context)
            return
        }

        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, prefs.scheduledHour)
            set(Calendar.MINUTE, prefs.scheduledMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(now)) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        val initialDelayMillis = (target.timeInMillis - now.timeInMillis).coerceAtLeast(0L)

        val periodicRequest = PeriodicWorkRequestBuilder<InTrackScheduledWorker>(
            24, TimeUnit.HOURS
        )
            .setInitialDelay(initialDelayMillis, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PERIODIC_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            periodicRequest
        )

        Log.d(TAG, "Enqueued periodic notification work with initial delay of ${initialDelayMillis / 1000}s")
    }

    /**
     * Cancels scheduled background notification tasks.
     */
    fun cancelPeriodicWork(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(PERIODIC_WORK_NAME)
        Log.d(TAG, "Cancelled unique periodic notification work")
    }

    /**
     * Enqueues a one-off immediate background check.
     */
    fun triggerImmediateCheck(context: Context) {
        val oneTimeRequest = OneTimeWorkRequestBuilder<InTrackScheduledWorker>().build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            IMMEDIATE_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            oneTimeRequest
        )
        Log.d(TAG, "Triggered one-off immediate notification check")
    }
}
