package com.example.intrack.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.intrack.MainActivity
import com.example.intrack.R

object InTrackNotificationManager {

    const val CHANNEL_ID_SUBSCRIPTIONS = "channel_intrack_subscriptions"
    const val CHANNEL_ID_SAFETY_MARGIN = "channel_intrack_safety_margin"
    const val CHANNEL_ID_REMINDERS = "channel_intrack_reminders"

    const val NOTIF_ID_SUBSCRIPTION_BASE = 1000
    const val NOTIF_ID_SAFETY_MARGIN = 2001
    const val NOTIF_ID_CATCH_UP = 3001

    /**
     * Initializes notification channels required for Android 8.0 (API 26) and above.
     */
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                ?: return

            val subChannel = NotificationChannel(
                CHANNEL_ID_SUBSCRIPTIONS,
                "Subscription & Renewal Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for upcoming subscription renewals and auto-debit payments"
                enableVibration(true)
                setShowBadge(true)
            }

            val marginChannel = NotificationChannel(
                CHANNEL_ID_SAFETY_MARGIN,
                "Safety Margin & Budget Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Warnings when remaining balance is low relative to upcoming commitments"
                enableVibration(true)
                setShowBadge(true)
            }

            val reminderChannel = NotificationChannel(
                CHANNEL_ID_REMINDERS,
                "Daily Ledger & Catch-Up Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Gentle reminders to record daily transactions and keep ledgers up to date"
                enableVibration(false)
                setShowBadge(true)
            }

            notificationManager.createNotificationChannels(listOf(subChannel, marginChannel, reminderChannel))
        }
    }

    /**
     * Dispatches an alert for an upcoming subscription renewal.
     */
    fun showSubscriptionRenewalNotification(
        context: Context,
        notificationId: Int,
        title: String,
        message: String,
        route: String = "subscription_audit"
    ) {
        postNotification(
            context = context,
            channelId = CHANNEL_ID_SUBSCRIPTIONS,
            notificationId = notificationId,
            title = title,
            message = message,
            navRoute = route,
            priority = NotificationCompat.PRIORITY_HIGH
        )
    }

    /**
     * Dispatches a warning when balance is critically low before scheduled payments.
     */
    fun showSafetyMarginNotification(
        context: Context,
        notificationId: Int = NOTIF_ID_SAFETY_MARGIN,
        title: String,
        message: String,
        route: String = "dashboard"
    ) {
        postNotification(
            context = context,
            channelId = CHANNEL_ID_SAFETY_MARGIN,
            notificationId = notificationId,
            title = title,
            message = message,
            navRoute = route,
            priority = NotificationCompat.PRIORITY_HIGH
        )
    }

    /**
     * Dispatches an inactivity/catch-up reminder.
     */
    fun showCatchUpNotification(
        context: Context,
        notificationId: Int = NOTIF_ID_CATCH_UP,
        title: String,
        message: String,
        route: String = "dashboard"
    ) {
        postNotification(
            context = context,
            channelId = CHANNEL_ID_REMINDERS,
            notificationId = notificationId,
            title = title,
            message = message,
            navRoute = route,
            priority = NotificationCompat.PRIORITY_DEFAULT
        )
    }

    private fun postNotification(
        context: Context,
        channelId: String,
        notificationId: Int,
        title: String,
        message: String,
        navRoute: String,
        priority: Int
    ) {
        // Enforce runtime permission on Android 13 (TIRAMISU) and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("NAV_ROUTE", navRoute)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val smallIconRes = R.drawable.ic_notification

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(smallIconRes)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(priority)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }
}
