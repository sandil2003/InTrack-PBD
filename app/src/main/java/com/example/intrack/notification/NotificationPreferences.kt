package com.example.intrack.notification

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationPreferences @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    private val _pushNotificationsEnabledFlow = MutableStateFlow(pushNotificationsEnabled)
    val pushNotificationsEnabledFlow: StateFlow<Boolean> = _pushNotificationsEnabledFlow.asStateFlow()

    private val _renewalRemindersEnabledFlow = MutableStateFlow(renewalRemindersEnabled)
    val renewalRemindersEnabledFlow: StateFlow<Boolean> = _renewalRemindersEnabledFlow.asStateFlow()

    private val _autoDebitMarginEnabledFlow = MutableStateFlow(autoDebitMarginEnabled)
    val autoDebitMarginEnabledFlow: StateFlow<Boolean> = _autoDebitMarginEnabledFlow.asStateFlow()

    private val _catchUpRemindersEnabledFlow = MutableStateFlow(catchUpRemindersEnabled)
    val catchUpRemindersEnabledFlow: StateFlow<Boolean> = _catchUpRemindersEnabledFlow.asStateFlow()

    private val _transactionReaderEnabledFlow = MutableStateFlow(transactionReaderEnabled)
    val transactionReaderEnabledFlow: StateFlow<Boolean> = _transactionReaderEnabledFlow.asStateFlow()

    var pushNotificationsEnabled: Boolean
        get() = prefs.getBoolean(KEY_PUSH_NOTIFICATIONS_ENABLED, true)
        set(value) {
            prefs.edit().putBoolean(KEY_PUSH_NOTIFICATIONS_ENABLED, value).apply()
            _pushNotificationsEnabledFlow.value = value
        }

    var renewalRemindersEnabled: Boolean
        get() = prefs.getBoolean(KEY_RENEWAL_REMINDERS_ENABLED, true)
        set(value) {
            prefs.edit().putBoolean(KEY_RENEWAL_REMINDERS_ENABLED, value).apply()
            _renewalRemindersEnabledFlow.value = value
        }

    var autoDebitMarginEnabled: Boolean
        get() = prefs.getBoolean(KEY_AUTO_DEBIT_MARGIN_ENABLED, true)
        set(value) {
            prefs.edit().putBoolean(KEY_AUTO_DEBIT_MARGIN_ENABLED, value).apply()
            _autoDebitMarginEnabledFlow.value = value
        }

    var catchUpRemindersEnabled: Boolean
        get() = prefs.getBoolean(KEY_CATCH_UP_REMINDERS_ENABLED, true)
        set(value) {
            prefs.edit().putBoolean(KEY_CATCH_UP_REMINDERS_ENABLED, value).apply()
            _catchUpRemindersEnabledFlow.value = value
        }

    var transactionReaderEnabled: Boolean
        get() = prefs.getBoolean(KEY_TRANSACTION_READER_ENABLED, true)
        set(value) {
            prefs.edit().putBoolean(KEY_TRANSACTION_READER_ENABLED, value).apply()
            _transactionReaderEnabledFlow.value = value
        }

    var scheduledHour: Int
        get() = prefs.getInt(KEY_SCHEDULED_HOUR, DEFAULT_SCHEDULED_HOUR)
        set(value) {
            prefs.edit().putInt(KEY_SCHEDULED_HOUR, value).apply()
        }

    var scheduledMinute: Int
        get() = prefs.getInt(KEY_SCHEDULED_MINUTE, DEFAULT_SCHEDULED_MINUTE)
        set(value) {
            prefs.edit().putInt(KEY_SCHEDULED_MINUTE, value).apply()
        }

    var lastRenewalNotifiedDate: Long
        get() = prefs.getLong(KEY_LAST_RENEWAL_NOTIFIED_DATE, 0L)
        set(value) {
            prefs.edit().putLong(KEY_LAST_RENEWAL_NOTIFIED_DATE, value).apply()
        }

    var lastSafetyMarginNotifiedDate: Long
        get() = prefs.getLong(KEY_LAST_SAFETY_MARGIN_NOTIFIED_DATE, 0L)
        set(value) {
            prefs.edit().putLong(KEY_LAST_SAFETY_MARGIN_NOTIFIED_DATE, value).apply()
        }

    var lastCatchUpNotifiedDate: Long
        get() = prefs.getLong(KEY_LAST_CATCH_UP_NOTIFIED_DATE, 0L)
        set(value) {
            prefs.edit().putLong(KEY_LAST_CATCH_UP_NOTIFIED_DATE, value).apply()
        }

    fun isAlreadyNotifiedToday(timestamp: Long, now: Long = System.currentTimeMillis()): Boolean {
        if (timestamp <= 0L) return false
        val calNotif = Calendar.getInstance().apply { timeInMillis = timestamp }
        val calNow = Calendar.getInstance().apply { timeInMillis = now }
        return calNotif.get(Calendar.YEAR) == calNow.get(Calendar.YEAR) &&
                calNotif.get(Calendar.DAY_OF_YEAR) == calNow.get(Calendar.DAY_OF_YEAR)
    }

    fun clear() {
        prefs.edit().clear().apply()
        _pushNotificationsEnabledFlow.value = true
        _renewalRemindersEnabledFlow.value = true
        _autoDebitMarginEnabledFlow.value = true
        _catchUpRemindersEnabledFlow.value = true
    }

    companion object {
        private const val PREFS_NAME = "intrack_notification_prefs"
        private const val KEY_PUSH_NOTIFICATIONS_ENABLED = "pref_push_notifications_enabled"
        private const val KEY_RENEWAL_REMINDERS_ENABLED = "pref_renewal_reminders_enabled"
        private const val KEY_AUTO_DEBIT_MARGIN_ENABLED = "pref_auto_debit_margin_enabled"
        private const val KEY_CATCH_UP_REMINDERS_ENABLED = "pref_catch_up_reminders_enabled"
        private const val KEY_TRANSACTION_READER_ENABLED = "pref_transaction_reader_enabled"
        private const val KEY_SCHEDULED_HOUR = "pref_scheduled_hour"
        private const val KEY_SCHEDULED_MINUTE = "pref_scheduled_minute"
        private const val KEY_LAST_RENEWAL_NOTIFIED_DATE = "pref_last_renewal_notified_date"
        private const val KEY_LAST_SAFETY_MARGIN_NOTIFIED_DATE = "pref_last_safety_margin_notified_date"
        private const val KEY_LAST_CATCH_UP_NOTIFIED_DATE = "pref_last_catch_up_notified_date"

        const val DEFAULT_SCHEDULED_HOUR = 9 // 09:00 AM
        const val DEFAULT_SCHEDULED_MINUTE = 0
    }
}
