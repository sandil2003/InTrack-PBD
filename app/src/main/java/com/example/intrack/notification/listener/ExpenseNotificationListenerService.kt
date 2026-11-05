package com.example.intrack.notification.listener

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.example.intrack.notification.InTrackNotificationManager
import com.example.intrack.notification.NotificationPreferences
import com.example.intrack.notification.sms.TransactionParser

/**
 * Background notification listener service that captures transaction notifications
 * posted by PickMe, Uber, UberEats, and Sri Lankan banking apps.
 */
class ExpenseNotificationListenerService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val pkgName = sbn.packageName ?: return
        // Do not process InTrack's own notifications to avoid feedback loops
        if (pkgName == applicationContext.packageName) return

        val prefs = NotificationPreferences(applicationContext)
        if (!prefs.pushNotificationsEnabled || !prefs.transactionReaderEnabled) {
            return
        }

        try {
            val extras = sbn.notification?.extras ?: return
            val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
            val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
            val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString() ?: ""

            val combinedContent = buildString {
                if (title.isNotBlank()) append(title).append(". ")
                if (text.isNotBlank()) append(text).append(" ")
                if (bigText.isNotBlank() && bigText != text) append(bigText)
            }.trim()

            if (combinedContent.isBlank()) return

            val parsedTx = TransactionParser.parse(pkgName, combinedContent)
            if (parsedTx != null) {
                Log.i(TAG, "Notification from [$pkgName] parsed: LKR ${parsedTx.amountLkr} for ${parsedTx.merchantName}")
                InTrackNotificationManager.showDetectedExpenseNotification(applicationContext, parsedTx)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error inspecting status bar notification: ${e.message}", e)
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.i(TAG, "ExpenseNotificationListenerService connected and monitoring financial alerts.")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.i(TAG, "ExpenseNotificationListenerService disconnected.")
    }

    companion object {
        const val TAG = "ExpenseNotifListener"
    }
}
