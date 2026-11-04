package com.example.intrack.data.local

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecurityPreferences @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    private val _isBiometricEnabledFlow = MutableStateFlow(isBiometricEnabled)
    val isBiometricEnabledFlow: StateFlow<Boolean> = _isBiometricEnabledFlow.asStateFlow()

    var isBiometricEnabled: Boolean
        get() = prefs.getBoolean(KEY_BIOMETRIC_ENABLED, true)
        set(value) {
            prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, value).apply()
            _isBiometricEnabledFlow.value = value
        }

    var lockTimeoutSeconds: Long
        get() = prefs.getLong(KEY_LOCK_TIMEOUT_SECONDS, DEFAULT_TIMEOUT_SECONDS)
        set(value) {
            prefs.edit().putLong(KEY_LOCK_TIMEOUT_SECONDS, value).apply()
        }

    var lastBackgroundTime: Long
        get() = prefs.getLong(KEY_LAST_BACKGROUND_TIME, 0L)
        set(value) {
            prefs.edit().putLong(KEY_LAST_BACKGROUND_TIME, value).apply()
        }

    /**
     * Determines whether the app should prompt for biometric lock based on whether
     * the feature is enabled and whether the background duration exceeded the timeout.
     */
    fun shouldLockOnResume(nowMillis: Long = System.currentTimeMillis()): Boolean {
        if (!isBiometricEnabled) return false
        val bgTime = lastBackgroundTime
        if (bgTime <= 0L) return true
        val elapsedMillis = nowMillis - bgTime
        return elapsedMillis >= (lockTimeoutSeconds * 1000L)
    }

    fun clear() {
        prefs.edit().clear().apply()
        _isBiometricEnabledFlow.value = false
    }

    companion object {
        private const val PREFS_NAME = "intrack_security_prefs"
        private const val KEY_BIOMETRIC_ENABLED = "pref_biometric_enabled"
        private const val KEY_LOCK_TIMEOUT_SECONDS = "pref_lock_timeout_seconds"
        private const val KEY_LAST_BACKGROUND_TIME = "pref_last_background_time"
        const val DEFAULT_TIMEOUT_SECONDS = 0L // Immediate lock on background
    }
}
