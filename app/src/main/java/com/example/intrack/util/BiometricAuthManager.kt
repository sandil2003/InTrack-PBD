package com.example.intrack.util

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import javax.inject.Inject
import javax.inject.Singleton

enum class BiometricAvailability {
    AVAILABLE,
    NOT_ENROLLED,
    NO_HARDWARE,
    UNAVAILABLE
}

@Singleton
class BiometricAuthManager @Inject constructor() {

    private val authenticators: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
    } else {
        BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL
    }

    /**
     * Checks if the device has biometric or screen lock hardware and whether credentials are enrolled.
     */
    fun checkBiometricAvailability(context: Context): BiometricAvailability {
        return try {
            val biometricManager = BiometricManager.from(context)
            val authResult = biometricManager.canAuthenticate(authenticators)
            Log.d("InTrackBiometric", "checkBiometricAvailability with authenticators=$authenticators -> result=$authResult")
            
            when (authResult) {
                BiometricManager.BIOMETRIC_SUCCESS -> BiometricAvailability.AVAILABLE
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricAvailability.NOT_ENROLLED
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricAvailability.NO_HARDWARE
                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricAvailability.UNAVAILABLE
                BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> BiometricAvailability.UNAVAILABLE
                else -> {
                    // Fallback check with BIOMETRIC_STRONG only
                    val strongResult = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                    Log.d("InTrackBiometric", "fallback check with BIOMETRIC_STRONG -> result=$strongResult")
                    when (strongResult) {
                        BiometricManager.BIOMETRIC_SUCCESS -> BiometricAvailability.AVAILABLE
                        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricAvailability.NOT_ENROLLED
                        BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricAvailability.NO_HARDWARE
                        else -> BiometricAvailability.UNAVAILABLE
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("InTrackBiometric", "Error checking biometric availability: ${e.message}", e)
            BiometricAvailability.UNAVAILABLE
        }
    }

    /**
     * Launches the system BiometricPrompt dialog.
     */
    fun showBiometricPrompt(
        activity: FragmentActivity,
        title: String = "Unlock InTrack",
        subtitle: String = "Verify your biometric or device lock to access your finances",
        onSuccess: () -> Unit,
        onError: (errorCode: Int, errString: CharSequence) -> Unit = { _, _ -> },
        onFailed: () -> Unit = {}
    ) {
        val executor = ContextCompat.getMainExecutor(activity)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                Log.d("InTrackBiometric", "BiometricPrompt: onAuthenticationSucceeded")
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Log.d("InTrackBiometric", "BiometricPrompt: onAuthenticationError code=$errorCode, msg=$errString")
                onError(errorCode, errString)
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Log.d("InTrackBiometric", "BiometricPrompt: onAuthenticationFailed")
                onFailed()
            }
        }

        try {
            val promptInfo = try {
                BiometricPrompt.PromptInfo.Builder()
                    .setTitle(title)
                    .setSubtitle(subtitle)
                    .setAllowedAuthenticators(authenticators)
                    .build()
            } catch (e: Exception) {
                Log.w("InTrackBiometric", "Failed to build PromptInfo with authenticators, falling back: ${e.message}")
                BiometricPrompt.PromptInfo.Builder()
                    .setTitle(title)
                    .setSubtitle(subtitle)
                    .setNegativeButtonText("Cancel")
                    .build()
            }

            Log.d("InTrackBiometric", "Launching BiometricPrompt on activity=${activity::class.java.simpleName}")
            val biometricPrompt = BiometricPrompt(activity, executor, callback)
            biometricPrompt.authenticate(promptInfo)
        } catch (e: Exception) {
            Log.e("InTrackBiometric", "Exception during BiometricPrompt.authenticate(): ${e.message}", e)
            onError(BiometricPrompt.ERROR_UNABLE_TO_PROCESS, e.localizedMessage ?: "Failed to open biometric prompt")
        }
    }
}
