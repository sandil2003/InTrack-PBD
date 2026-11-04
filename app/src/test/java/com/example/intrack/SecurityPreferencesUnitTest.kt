package com.example.intrack

import android.content.Context
import android.content.SharedPreferences
import com.example.intrack.data.local.SecurityPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyBoolean
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

class SecurityPreferencesUnitTest {

    private lateinit var mockContext: Context
    private lateinit var mockPrefs: SharedPreferences
    private lateinit var mockEditor: SharedPreferences.Editor

    @Before
    fun setUp() {
        mockContext = mock(Context::class.java)
        mockPrefs = mock(SharedPreferences::class.java)
        mockEditor = mock(SharedPreferences.Editor::class.java)

        `when`(mockContext.getSharedPreferences(eq("intrack_security_prefs"), eq(Context.MODE_PRIVATE)))
            .thenReturn(mockPrefs)
        `when`(mockPrefs.edit()).thenReturn(mockEditor)
        `when`(mockEditor.putBoolean(anyString(), anyBoolean())).thenReturn(mockEditor)
        `when`(mockEditor.putLong(anyString(), anyLong())).thenReturn(mockEditor)
        `when`(mockEditor.clear()).thenReturn(mockEditor)
    }

    @Test
    fun shouldLockOnResume_returnsFalseWhenBiometricDisabled() {
        `when`(mockPrefs.getBoolean(eq("pref_biometric_enabled"), eq(true))).thenReturn(false)
        val securityPreferences = SecurityPreferences(mockContext)

        val result = securityPreferences.shouldLockOnResume(nowMillis = 100000L)
        assertFalse(result)
    }

    @Test
    fun shouldLockOnResume_returnsTrueOnColdStartWhenBiometricEnabled() {
        `when`(mockPrefs.getBoolean(eq("pref_biometric_enabled"), eq(true))).thenReturn(true)
        `when`(mockPrefs.getLong(eq("pref_last_background_time"), eq(0L))).thenReturn(0L)
        val securityPreferences = SecurityPreferences(mockContext)

        val result = securityPreferences.shouldLockOnResume(nowMillis = 100000L)
        assertTrue(result)
    }

    @Test
    fun shouldLockOnResume_returnsTrueWhenElapsedExceedsTimeout() {
        `when`(mockPrefs.getBoolean(eq("pref_biometric_enabled"), eq(true))).thenReturn(true)
        `when`(mockPrefs.getLong(eq("pref_lock_timeout_seconds"), eq(0L))).thenReturn(30L) // 30 sec timeout
        `when`(mockPrefs.getLong(eq("pref_last_background_time"), eq(0L))).thenReturn(100000L)
        val securityPreferences = SecurityPreferences(mockContext)

        // 35 seconds later (35000ms >= 30000ms)
        val result = securityPreferences.shouldLockOnResume(nowMillis = 135000L)
        assertTrue(result)
    }

    @Test
    fun shouldLockOnResume_returnsFalseWhenWithinGracePeriod() {
        `when`(mockPrefs.getBoolean(eq("pref_biometric_enabled"), eq(true))).thenReturn(true)
        `when`(mockPrefs.getLong(eq("pref_lock_timeout_seconds"), eq(0L))).thenReturn(30L) // 30 sec timeout
        `when`(mockPrefs.getLong(eq("pref_last_background_time"), eq(0L))).thenReturn(100000L)
        val securityPreferences = SecurityPreferences(mockContext)

        // 10 seconds later (10000ms < 30000ms)
        val result = securityPreferences.shouldLockOnResume(nowMillis = 110000L)
        assertFalse(result)
    }

    @Test
    fun setBiometricEnabled_writesToPreferencesAndFlow() {
        val securityPreferences = SecurityPreferences(mockContext)
        securityPreferences.isBiometricEnabled = true

        verify(mockEditor).putBoolean("pref_biometric_enabled", true)
        verify(mockEditor).apply()
        assertEquals(true, securityPreferences.isBiometricEnabledFlow.value)
    }

    @Test
    fun clear_clearsPreferencesAndResetsFlow() {
        val securityPreferences = SecurityPreferences(mockContext)
        securityPreferences.clear()

        verify(mockEditor).clear()
        verify(mockEditor).apply()
        assertEquals(false, securityPreferences.isBiometricEnabledFlow.value)
    }
}
