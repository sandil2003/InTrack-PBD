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
import com.example.intrack.notification.sms.ParsedTransaction
import com.example.intrack.notification.sms.QuickLogExpenseReceiver
import java.net.URLEncoder
import java.util.Locale

object InTrackNotificationManager {

    const val CHANNEL_ID_SUBSCRIPTIONS = "channel_intrack_subscriptions"
    const val CHANNEL_ID_SAFETY_MARGIN = "channel_intrack_safety_margin"
    const val CHANNEL_ID_REMINDERS = "channel_intrack_reminders"
    const val CHANNEL_ID_EXPENSE_DETECTION = "channel_intrack_expense_detection"

    const val NOTIF_ID_SUBSCRIPTION_BASE = 1000
    const val NOTIF_ID_SAFETY_MARGIN = 2001
    const val NOTIF_ID_CATCH_UP = 3001
    const val NOTIF_ID_DETECTED_BASE = 5000

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

            val expenseDetectionChannel = NotificationChannel(
                CHANNEL_ID_EXPENSE_DETECTION,
                "Transaction & Expense Detection",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Zero-friction heads-up alerts for detected bank SMS and ride-hailing receipts"
                enableVibration(true)
                setShowBadge(true)
            }

            notificationManager.createNotificationChannels(
                listOf(subChannel, marginChannel, reminderChannel, expenseDetectionChannel)
            )
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

    /**
     * Dispatches a high-priority heads-up notification when an expense is detected
     * from a bank SMS or ride-hailing app (PickMe, Uber, ComBank, Sampath, HNB).
     * Provides a single-tap action button to immediately log the expense.
     */
    fun showDetectedExpenseNotification(
        context: Context,
        tx: ParsedTransaction
    ) {
        val prefs = NotificationPreferences(context)
        if (!prefs.pushNotificationsEnabled || !prefs.transactionReaderEnabled) {
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val amountStr = if (tx.amountLkr % 1.0 == 0.0) tx.amountLkr.toInt().toString() else String.format(Locale.US, "%.2f", tx.amountLkr)
        val commitmentFormatted = tx.commitmentType.lowercase().replaceFirstChar { it.uppercase() }
        val categoryFormatted = tx.category.replace("_", " ").lowercase().split(" ")
            .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }

        // Exact pattern: "Detected LKR 850 PickMe ride. Tap to log as Discretionary Transport."
        val title = "Detected LKR $amountStr ${tx.merchantName} ${tx.typeLabel}"
        val message = "Tap to log as $commitmentFormatted $categoryFormatted."
        val expenseTitle = "${tx.merchantName} ${tx.typeLabel.replaceFirstChar { it.uppercase() }}"

        val notifId = NOTIF_ID_DETECTED_BASE + (tx.merchantName.hashCode() + tx.amountLkr.hashCode()).let { Math.abs(it) % 1000 }

        // 1. Single-tap Action Button Intent (logs directly in background)
        val quickLogIntent = Intent(context, QuickLogExpenseReceiver::class.java).apply {
            action = QuickLogExpenseReceiver.ACTION_QUICK_LOG_EXPENSE
            putExtra(QuickLogExpenseReceiver.EXTRA_TITLE, expenseTitle)
            putExtra(QuickLogExpenseReceiver.EXTRA_AMOUNT, tx.amountLkr)
            putExtra(QuickLogExpenseReceiver.EXTRA_CATEGORY, tx.category)
            putExtra(QuickLogExpenseReceiver.EXTRA_COMMITMENT, tx.commitmentType)
            putExtra(QuickLogExpenseReceiver.EXTRA_ACCOUNT, tx.paymentAccount)
            putExtra(QuickLogExpenseReceiver.EXTRA_NOTIF_ID, notifId)
        }
        val quickLogPendingIntent = PendingIntent.getBroadcast(
            context,
            notifId,
            quickLogIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 2. Notification Body Tap Intent: Opens MainActivity with auto-log or prefilled add_expense
        val encodedTitle = try {
            URLEncoder.encode(expenseTitle, "UTF-8")
        } catch (_: Exception) {
            expenseTitle
        }
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_AUTO_LOG_EXPENSE", true)
            putExtra("EXTRA_EXPENSE_TITLE", expenseTitle)
            putExtra("EXTRA_EXPENSE_AMOUNT", tx.amountLkr)
            putExtra("EXTRA_EXPENSE_CATEGORY", tx.category)
            putExtra("EXTRA_EXPENSE_COMMITMENT", tx.commitmentType)
            putExtra("EXTRA_EXPENSE_ACCOUNT", tx.paymentAccount)
            putExtra("NAV_ROUTE", "add_expense?category=${tx.category}&amount=$amountStr&title=$encodedTitle&commitmentType=${tx.commitmentType}&paymentAccount=${tx.paymentAccount}")
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            notifId + 1,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val smallIconRes = R.drawable.ic_notification

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_EXPENSE_DETECTION)
            .setSmallIcon(smallIconRes)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .addAction(
                android.R.drawable.ic_input_add,
                "Log LKR $amountStr",
                quickLogPendingIntent
            )
            .build()

        NotificationManagerCompat.from(context).notify(notifId, notification)
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
