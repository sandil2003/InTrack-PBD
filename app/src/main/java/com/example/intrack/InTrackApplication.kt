package com.example.intrack

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class InTrackApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        com.example.intrack.notification.InTrackNotificationManager.createNotificationChannels(this)
        com.example.intrack.notification.NotificationScheduler.schedulePeriodicWork(this)
    }
}
