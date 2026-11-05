package com.example.intrack.notification

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.intrack.util.CurrencyFormatter
import com.example.intrack.util.DateUtils
import dagger.hilt.android.EntryPointAccessors

class InTrackScheduledWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d(TAG, "InTrackScheduledWorker triggered")
        return try {
            val entryPoint = EntryPointAccessors.fromApplication(
                applicationContext,
                NotificationWorkerEntryPoint::class.java
            )
            val prefs = entryPoint.notificationPreferences()

            // Respect master toggle
            if (!prefs.pushNotificationsEnabled) {
                Log.d(TAG, "Push notifications disabled by user. Skipping background evaluation.")
                return Result.success()
            }

            val now = System.currentTimeMillis()
            val auth = entryPoint.firebaseAuth()
            val userId = auth.currentUser?.uid ?: ""

            val subDao = entryPoint.subscriptionDao()
            val subscriptions = if (userId.isNotBlank()) {
                subDao.getSubscriptionsForUser(userId)
            } else {
                subDao.getAllSubscriptions()
            }

            val expenseDao = entryPoint.expenseDao()
            val expenses = expenseDao.getAllExpenses()

            val incomeDao = entryPoint.incomeDao()
            val incomes = incomeDao.getAllIncomes()

            // 1. Subscription Renewal Reminders (72h window)
            if (prefs.renewalRemindersEnabled) {
                val seventyTwoHoursMillis = 72L * 60 * 60 * 1000L
                val upcomingSubs = subscriptions.filter { sub ->
                    sub.status != "CANCELLED" &&
                            sub.nextBillingMillis in now..(now + seventyTwoHoursMillis)
                }

                for (sub in upcomingSubs) {
                    val diffMillis = sub.nextBillingMillis - now
                    val days = (diffMillis / (24L * 3600L * 1000L)).coerceAtLeast(0L)
                    val timingText = when (days) {
                        0L -> "today"
                        1L -> "tomorrow"
                        else -> "in $days days"
                    }
                    val amountStr = CurrencyFormatter.formatLkr(sub.amountLkr)
                    val title = "Upcoming Subscription Renewal"
                    val message = "${sub.name} ($amountStr) is due to renew $timingText. Ensure your account is funded."
                    val notifId = InTrackNotificationManager.NOTIF_ID_SUBSCRIPTION_BASE +
                            (sub.id.hashCode() and 0x7FFF)

                    InTrackNotificationManager.showSubscriptionRenewalNotification(
                        context = applicationContext,
                        notificationId = notifId,
                        title = title,
                        message = message
                    )
                }
                if (upcomingSubs.isNotEmpty()) {
                    prefs.lastRenewalNotifiedDate = now
                }
            }

            // 2. Auto-Debit Safety Margin & Low Reserve Warning
            if (prefs.autoDebitMarginEnabled && !prefs.isAlreadyNotifiedToday(prefs.lastSafetyMarginNotifiedDate, now)) {
                val currentMonthIncomes = incomes.filter {
                    it.status.equals("PAID", ignoreCase = true) && DateUtils.isCurrentMonth(it.dateReceivedMillis)
                }
                val totalIncome = currentMonthIncomes.sumOf { it.realizedAmountLkr }

                val currentMonthExpenses = expenses.filter {
                    !it.isInternalTransfer() && DateUtils.isCurrentMonth(it.dateSpentMillis)
                }
                val totalExpense = currentMonthExpenses.sumOf { it.amountLkr }
                val remainingBalance = totalIncome - totalExpense

                val upcomingDebitsIn7Days = subscriptions.filter {
                    it.isAutoPayment && it.status != "CANCELLED" &&
                            it.nextBillingMillis in now..(now + 7L * 24 * 3600 * 1000L)
                }.sumOf { it.amountLkr }

                if (upcomingDebitsIn7Days > 0.0 && remainingBalance < upcomingDebitsIn7Days) {
                    val balStr = CurrencyFormatter.formatLkr(remainingBalance)
                    val debitStr = CurrencyFormatter.formatLkr(upcomingDebitsIn7Days)
                    val title = "Auto-Debit Safety Margin Alert"
                    val message = "Your remaining monthly cashflow ($balStr) is below upcoming auto-debit payments ($debitStr) scheduled within 7 days."

                    InTrackNotificationManager.showSafetyMarginNotification(
                        context = applicationContext,
                        title = title,
                        message = message
                    )
                    prefs.lastSafetyMarginNotifiedDate = now
                }
            }

            // 3. Catch-Up & Inactivity Reminder (>= 3 days)
            if (prefs.catchUpRemindersEnabled && !prefs.isAlreadyNotifiedToday(prefs.lastCatchUpNotifiedDate, now)) {
                val latestExpenseMillis = expenses.maxOfOrNull { it.dateSpentMillis } ?: 0L
                val daysSinceLastExpense = if (latestExpenseMillis > 0L) {
                    ((now - latestExpenseMillis) / (24L * 3600L * 1000L)).coerceAtLeast(0L)
                } else {
                    0L
                }

                if (daysSinceLastExpense >= 3L) {
                    val title = "Catch-Up Mode Available"
                    val message = "You haven't recorded any expenses in $daysSinceLastExpense days. Open InTrack to log recent activity in seconds."

                    InTrackNotificationManager.showCatchUpNotification(
                        context = applicationContext,
                        title = title,
                        message = message
                    )
                    prefs.lastCatchUpNotifiedDate = now
                }
            }

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error executing InTrackScheduledWorker: ${e.message}", e)
            Result.failure()
        }
    }

    companion object {
        private const val TAG = "InTrackScheduledWorker"
    }
}
