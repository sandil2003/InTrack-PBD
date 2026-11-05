package com.example.intrack.notification.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import com.example.intrack.data.model.ExpenseEntity
import com.example.intrack.notification.NotificationWorkerEntryPoint
import com.example.intrack.ui.widget.InTrackGlanceWidget
import dagger.hilt.EntryPoints
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Handles single-tap background logging of an expense directly from an InTrack heads-up notification.
 */
class QuickLogExpenseReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action != ACTION_QUICK_LOG_EXPENSE) return

        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Detected Expense"
        val amount = intent.getDoubleExtra(EXTRA_AMOUNT, 0.0)
        val category = intent.getStringExtra(EXTRA_CATEGORY) ?: "TRANSPORT"
        val commitmentType = intent.getStringExtra(EXTRA_COMMITMENT) ?: "DISCRETIONARY"
        val paymentAccount = intent.getStringExtra(EXTRA_ACCOUNT) ?: "CARD_PRIMARY"
        val notificationId = intent.getIntExtra(EXTRA_NOTIF_ID, -1)

        if (amount <= 0.0) {
            Log.w(TAG, "Quick log skipped: invalid amount $amount")
            return
        }

        // Cancel the notification immediately on tap
        if (notificationId != -1) {
            NotificationManagerCompat.from(context).cancel(notificationId)
        }

        val appContext = context.applicationContext
        val entryPoint = EntryPoints.get(appContext, NotificationWorkerEntryPoint::class.java)
        val expenseDao = entryPoint.expenseDao()
        val expenseRepository = entryPoint.expenseRepository()
        val auth = entryPoint.firebaseAuth()

        val userId = auth.currentUser?.uid?.ifBlank { "kavindu_demo" } ?: "kavindu_demo"
        val newExpense = ExpenseEntity(
            id = UUID.randomUUID().toString(),
            userId = userId,
            title = title,
            amountLkr = amount,
            category = category,
            paymentAccount = paymentAccount,
            commitmentType = commitmentType,
            dateSpentMillis = System.currentTimeMillis(),
            isRecurring = false,
            notes = "Auto-ingested via zero-friction SMS / Notification reader"
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 1. Immediate local cache insert in Room
                expenseDao.upsert(newExpense)
                Log.d(TAG, "QuickLogExpenseReceiver: saved expense ${newExpense.id} to local Room DB")

                // 2. Synchronize to Firestore
                expenseRepository.addExpense(newExpense)
                Log.d(TAG, "QuickLogExpenseReceiver: synced expense ${newExpense.id} to Firestore")

                // 3. Update Glance AppWidget
                InTrackGlanceWidget.updateWidget(appContext)

                // 4. Show confirmation Toast on main thread
                Handler(Looper.getMainLooper()).post {
                    val amountDisplay = if (amount % 1.0 == 0.0) amount.toInt().toString() else String.format("%.2f", amount)
                    val commitmentLabel = commitmentType.lowercase().replaceFirstChar { it.uppercase() }
                    val categoryLabel = category.replace("_", " ").lowercase().split(" ").joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
                    Toast.makeText(
                        appContext,
                        "✓ Logged LKR $amountDisplay $title as $commitmentLabel $categoryLabel",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to quick-log expense: ${e.message}", e)
            }
        }
    }

    companion object {
        const val TAG = "QuickLogExpenseReceiver"
        const val ACTION_QUICK_LOG_EXPENSE = "com.example.intrack.ACTION_QUICK_LOG_EXPENSE"
        const val EXTRA_TITLE = "extra_expense_title"
        const val EXTRA_AMOUNT = "extra_expense_amount"
        const val EXTRA_CATEGORY = "extra_expense_category"
        const val EXTRA_COMMITMENT = "extra_expense_commitment"
        const val EXTRA_ACCOUNT = "extra_expense_account"
        const val EXTRA_NOTIF_ID = "extra_expense_notif_id"
    }
}
