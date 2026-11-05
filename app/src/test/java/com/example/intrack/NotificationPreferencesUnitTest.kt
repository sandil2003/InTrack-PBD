package com.example.intrack

import android.content.Context
import android.content.SharedPreferences
import com.example.intrack.notification.NotificationPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyBoolean
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import java.util.Calendar

class NotificationPreferencesUnitTest {

    private lateinit var mockContext: Context
    private lateinit var mockPrefs: SharedPreferences
    private lateinit var mockEditor: SharedPreferences.Editor

    @Before
    fun setUp() {
        mockContext = mock(Context::class.java)
        mockPrefs = mock(SharedPreferences::class.java)
        mockEditor = mock(SharedPreferences.Editor::class.java)

        `when`(mockContext.getSharedPreferences(eq("intrack_notification_prefs"), eq(Context.MODE_PRIVATE)))
            .thenReturn(mockPrefs)
        `when`(mockPrefs.edit()).thenReturn(mockEditor)
        `when`(mockEditor.putBoolean(anyString(), anyBoolean())).thenReturn(mockEditor)
        `when`(mockEditor.putInt(anyString(), anyInt())).thenReturn(mockEditor)
        `when`(mockEditor.putLong(anyString(), anyLong())).thenReturn(mockEditor)
        `when`(mockEditor.clear()).thenReturn(mockEditor)
    }

    @Test
    fun pushNotificationsEnabled_defaultsToTrue() {
        `when`(mockPrefs.getBoolean(eq("pref_push_notifications_enabled"), eq(true))).thenReturn(true)
        val notifPrefs = NotificationPreferences(mockContext)

        assertTrue(notifPrefs.pushNotificationsEnabled)
        assertTrue(notifPrefs.pushNotificationsEnabledFlow.value)
    }

    @Test
    fun pushNotificationsEnabled_setterUpdatesPrefsAndFlow() {
        `when`(mockPrefs.getBoolean(eq("pref_push_notifications_enabled"), eq(true))).thenReturn(true)
        val notifPrefs = NotificationPreferences(mockContext)

        notifPrefs.pushNotificationsEnabled = false

        verify(mockEditor).putBoolean(eq("pref_push_notifications_enabled"), eq(false))
        verify(mockEditor).apply()
        assertFalse(notifPrefs.pushNotificationsEnabledFlow.value)
    }

    @Test
    fun renewalRemindersEnabled_setterUpdatesPrefsAndFlow() {
        `when`(mockPrefs.getBoolean(eq("pref_renewal_reminders_enabled"), eq(true))).thenReturn(true)
        val notifPrefs = NotificationPreferences(mockContext)

        notifPrefs.renewalRemindersEnabled = false

        verify(mockEditor).putBoolean(eq("pref_renewal_reminders_enabled"), eq(false))
        verify(mockEditor).apply()
        assertFalse(notifPrefs.renewalRemindersEnabledFlow.value)
    }

    @Test
    fun autoDebitMarginEnabled_setterUpdatesPrefsAndFlow() {
        `when`(mockPrefs.getBoolean(eq("pref_auto_debit_margin_enabled"), eq(true))).thenReturn(true)
        val notifPrefs = NotificationPreferences(mockContext)

        notifPrefs.autoDebitMarginEnabled = false

        verify(mockEditor).putBoolean(eq("pref_auto_debit_margin_enabled"), eq(false))
        verify(mockEditor).apply()
        assertFalse(notifPrefs.autoDebitMarginEnabledFlow.value)
    }

    @Test
    fun scheduledTime_readsAndUpdatesProperly() {
        `when`(mockPrefs.getInt(eq("pref_scheduled_hour"), eq(9))).thenReturn(9)
        `when`(mockPrefs.getInt(eq("pref_scheduled_minute"), eq(0))).thenReturn(0)

        val notifPrefs = NotificationPreferences(mockContext)
        assertEquals(9, notifPrefs.scheduledHour)
        assertEquals(0, notifPrefs.scheduledMinute)

        notifPrefs.scheduledHour = 20
        verify(mockEditor).putInt(eq("pref_scheduled_hour"), eq(20))
    }

    @Test
    fun isAlreadyNotifiedToday_returnsTrueForSameCalendarDay() {
        val notifPrefs = NotificationPreferences(mockContext)
        val cal = Calendar.getInstance()
        val now = cal.timeInMillis

        // 2 hours ago today
        cal.add(Calendar.HOUR_OF_DAY, -2)
        val earlierToday = cal.timeInMillis

        assertTrue(notifPrefs.isAlreadyNotifiedToday(earlierToday, now))
    }

    @Test
    fun isAlreadyNotifiedToday_returnsFalseForDifferentDay() {
        val notifPrefs = NotificationPreferences(mockContext)
        val cal = Calendar.getInstance()
        val now = cal.timeInMillis

        // Yesterday
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterday = cal.timeInMillis

        assertFalse(notifPrefs.isAlreadyNotifiedToday(yesterday, now))
        assertFalse(notifPrefs.isAlreadyNotifiedToday(0L, now))
    }

    @Test
    fun subscriptionRenewalWindowEvaluation_detects72HoursProperly() {
        val now = System.currentTimeMillis()
        val seventyTwoHoursMillis = 72L * 60 * 60 * 1000L

        val subDueIn2Days = now + (48L * 60 * 60 * 1000L)
        val subDueIn5Days = now + (120L * 60 * 60 * 1000L)
        val subPast = now - (24L * 60 * 60 * 1000L)

        val isInWindow = { billingMillis: Long ->
            billingMillis in now..(now + seventyTwoHoursMillis)
        }

        assertTrue(isInWindow(subDueIn2Days))
        assertFalse(isInWindow(subDueIn5Days))
        assertFalse(isInWindow(subPast))
    }

    @Test
    fun safetyMarginEvaluation_flagsLowRemainingBalance() {
        val remainingBalance = 5000.0
        val upcomingAutoDebitsIn7Days = 8500.0

        val isSafetyMarginBreached = remainingBalance < upcomingAutoDebitsIn7Days
        assertTrue(isSafetyMarginBreached)

        val safeRemainingBalance = 50000.0
        val isSafe = safeRemainingBalance < upcomingAutoDebitsIn7Days
        assertFalse(isSafe)
    }

    @Test
    fun catchUpEvaluation_triggersWhenInactiveForThreeOrMoreDays() {
        val now = System.currentTimeMillis()
        val twoDaysAgo = now - (2L * 24 * 3600 * 1000L)
        val fourDaysAgo = now - (4L * 24 * 3600 * 1000L)

        val daysSinceTwoDays = (now - twoDaysAgo) / (24L * 3600 * 1000L)
        val daysSinceFourDays = (now - fourDaysAgo) / (24L * 3600 * 1000L)

        assertFalse(daysSinceTwoDays >= 3L)
        assertTrue(daysSinceFourDays >= 3L)
    }
}
