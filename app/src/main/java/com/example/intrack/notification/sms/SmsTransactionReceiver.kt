package com.example.intrack.notification.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import com.example.intrack.notification.InTrackNotificationManager
import com.example.intrack.notification.NotificationPreferences

/**
 * BroadcastReceiver listening for incoming SMS transaction messages from
 * Commercial Bank, Sampath Bank, HNB, PickMe, Uber, etc.
 */
class SmsTransactionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val prefs = NotificationPreferences(context)
        if (!prefs.pushNotificationsEnabled || !prefs.transactionReaderEnabled) {
            Log.d(TAG, "Transaction reader disabled in preferences; skipping SMS check.")
            return
        }

        try {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            if (messages.isNullOrEmpty()) return

            // Group multipart messages by originating sender
            val groupedBySender = messages.groupBy { it.displayOriginatingAddress ?: it.originatingAddress ?: "Unknown" }

            for ((sender, partList) in groupedBySender) {
                val fullBody = partList.joinToString(separator = "") { it.displayMessageBody ?: it.messageBody ?: "" }
                Log.d(TAG, "Received SMS from [$sender]: $fullBody")

                val parsedTx = TransactionParser.parse(sender, fullBody)
                if (parsedTx != null) {
                    Log.i(TAG, "Successfully parsed transaction: ${parsedTx.amountLkr} LKR for ${parsedTx.merchantName}")
                    InTrackNotificationManager.showDetectedExpenseNotification(context, parsedTx)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing incoming SMS transaction: ${e.message}", e)
        }
    }

    companion object {
        const val TAG = "SmsTransactionReceiver"
    }
}
