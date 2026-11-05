package com.example.intrack

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.example.intrack.data.local.SecurityPreferences
import com.example.intrack.ui.navigation.FinFlowNavGraph
import com.example.intrack.ui.screens.auth.BiometricLockScreen
import com.example.intrack.ui.theme.AppThemeMode
import com.example.intrack.ui.theme.InTrackTheme
import com.example.intrack.util.BiometricAuthManager
import com.example.intrack.util.BiometricAvailability
import com.example.intrack.ui.widget.InTrackGlanceWidget
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var securityPreferences: SecurityPreferences

    @Inject
    lateinit var biometricAuthManager: BiometricAuthManager

    @Inject
    lateinit var auth: FirebaseAuth

    @Inject
    lateinit var expenseDao: com.example.intrack.data.local.ExpenseDao

    @Inject
    lateinit var expenseRepository: com.example.intrack.data.repository.ExpenseRepository

    private var pendingNavRoute by mutableStateOf<String?>(null)
    private var isAppLocked by mutableStateOf(false)
    private var lockErrorMessage by mutableStateOf<String?>(null)
    private var isPromptShowing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleWidgetIntent(intent)
        enableEdgeToEdge()

        val isUserLoggedIn = auth.currentUser != null
        val isBioEnabled = securityPreferences.isBiometricEnabled
        Log.d("InTrackBiometric", "MainActivity.onCreate: isUserLoggedIn=$isUserLoggedIn, isBioEnabled=$isBioEnabled")

        if (isUserLoggedIn && isBioEnabled) {
            val availability = biometricAuthManager.checkBiometricAvailability(this)
            Log.d("InTrackBiometric", "MainActivity.onCreate: availability=$availability")
            if (availability != BiometricAvailability.NO_HARDWARE) {
                isAppLocked = true
            }
        }

        setContent {
            val systemInDark = isSystemInDarkTheme()
            var themeMode by remember { mutableStateOf(AppThemeMode.LIGHT) }

            val isDark = when (themeMode) {
                AppThemeMode.SYSTEM -> systemInDark
                AppThemeMode.LIGHT -> false
                AppThemeMode.DARK -> true
            }

            InTrackTheme(darkTheme = isDark) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (isAppLocked) {
                        LaunchedEffect(isAppLocked) {
                            if (!isPromptShowing) {
                                triggerBiometricPrompt()
                            }
                        }
                        BiometricLockScreen(
                            onUnlockClick = { triggerBiometricPrompt() },
                            onSignOutClick = {
                                auth.signOut()
                                isAppLocked = false
                                lockErrorMessage = null
                            },
                            errorMessage = lockErrorMessage
                        )
                    } else {
                        FinFlowNavGraph(
                            themeMode = themeMode,
                            onThemeModeChange = { themeMode = it },
                            pendingNavRoute = pendingNavRoute,
                            onClearPendingNavRoute = { pendingNavRoute = null },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val isUserLoggedIn = auth.currentUser != null
        val shouldLock = securityPreferences.shouldLockOnResume()
        Log.d("InTrackBiometric", "MainActivity.onStart: isUserLoggedIn=$isUserLoggedIn, shouldLock=$shouldLock")
        if (isUserLoggedIn && shouldLock) {
            val availability = biometricAuthManager.checkBiometricAvailability(this)
            if (availability != BiometricAvailability.NO_HARDWARE) {
                isAppLocked = true
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        Log.d("InTrackBiometric", "MainActivity.onWindowFocusChanged: hasFocus=$hasFocus, isAppLocked=$isAppLocked, isPromptShowing=$isPromptShowing")
        if (hasFocus && isAppLocked && !isPromptShowing) {
            window.decorView.post {
                if (isAppLocked && !isPromptShowing) {
                    triggerBiometricPrompt()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("InTrackBiometric", "MainActivity.onResume: isAppLocked=$isAppLocked, isPromptShowing=$isPromptShowing")
        if (isAppLocked && !isPromptShowing) {
            window.decorView.post {
                if (isAppLocked && !isPromptShowing) {
                    triggerBiometricPrompt()
                }
            }
        }

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                InTrackGlanceWidget.updateWidget(applicationContext)
            } catch (_: Exception) {}
        }
    }

    override fun onStop() {
        super.onStop()
        if (auth.currentUser != null && securityPreferences.isBiometricEnabled && !isPromptShowing) {
            securityPreferences.lastBackgroundTime = System.currentTimeMillis()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleWidgetIntent(intent)
    }

    private fun triggerBiometricPrompt() {
        if (isPromptShowing) {
            Log.d("InTrackBiometric", "triggerBiometricPrompt: prompt already showing, skipping")
            return
        }
        isPromptShowing = true
        lockErrorMessage = null
        Log.d("InTrackBiometric", "triggerBiometricPrompt: launching prompt")

        try {
            biometricAuthManager.showBiometricPrompt(
                activity = this,
                title = "Unlock InTrack",
                subtitle = "Verify your biometric or device lock to access your finances",
                onSuccess = {
                    Log.d("InTrackBiometric", "triggerBiometricPrompt onSuccess")
                    isAppLocked = false
                    lockErrorMessage = null
                    isPromptShowing = false
                    securityPreferences.lastBackgroundTime = System.currentTimeMillis()
                },
                onError = { errorCode, errString ->
                    Log.d("InTrackBiometric", "triggerBiometricPrompt onError: code=$errorCode, msg=$errString")
                    isPromptShowing = false
                    if (errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                        errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON
                    ) {
                        lockErrorMessage = errString.toString()
                    } else {
                        lockErrorMessage = "Authentication cancelled. Tap Unlock to try again."
                    }
                },
                onFailed = {
                    Log.d("InTrackBiometric", "triggerBiometricPrompt onFailed")
                    lockErrorMessage = "Biometric not recognized. Please try again."
                }
            )
        } catch (e: Exception) {
            Log.e("InTrackBiometric", "Exception in triggerBiometricPrompt: ${e.message}", e)
            isPromptShowing = false
            lockErrorMessage = "Biometric error: ${e.localizedMessage}"
        }
    }

    private fun handleWidgetIntent(intent: Intent?) {
        val autoLog = intent?.getBooleanExtra("EXTRA_AUTO_LOG_EXPENSE", false) ?: false
        if (autoLog) {
            val title = intent?.getStringExtra("EXTRA_EXPENSE_TITLE") ?: "Detected Expense"
            val amount = intent?.getDoubleExtra("EXTRA_EXPENSE_AMOUNT", 0.0) ?: 0.0
            val category = intent?.getStringExtra("EXTRA_EXPENSE_CATEGORY") ?: "TRANSPORT"
            val commitment = intent?.getStringExtra("EXTRA_EXPENSE_COMMITMENT") ?: "DISCRETIONARY"
            val account = intent?.getStringExtra("EXTRA_EXPENSE_ACCOUNT") ?: "CARD_PRIMARY"

            if (amount > 0.0) {
                val currentUserId = auth.currentUser?.uid?.ifBlank { "kavindu_demo" } ?: "kavindu_demo"
                val newExpense = com.example.intrack.data.model.ExpenseEntity(
                    id = java.util.UUID.randomUUID().toString(),
                    userId = currentUserId,
                    title = title,
                    amountLkr = amount,
                    category = category,
                    paymentAccount = account,
                    commitmentType = commitment,
                    dateSpentMillis = System.currentTimeMillis(),
                    isRecurring = false,
                    notes = "Auto-ingested via zero-friction SMS / Notification reader"
                )
                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        expenseDao.upsert(newExpense)
                        expenseRepository.addExpense(newExpense)
                        InTrackGlanceWidget.updateWidget(applicationContext)
                    } catch (e: Exception) {
                        Log.e("MainActivity", "Error auto-logging expense from notification: ", e)
                    }
                }
                val amountDisplay = if (amount % 1.0 == 0.0) amount.toInt().toString() else String.format(java.util.Locale.US, "%.2f", amount)
                val commitmentLabel = commitment.lowercase().replaceFirstChar { it.uppercase() }
                val categoryLabel = category.replace("_", " ").lowercase().split(" ").joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
                android.widget.Toast.makeText(
                    this,
                    "✓ Logged LKR   as  ",
                    android.widget.Toast.LENGTH_LONG
                ).show()
                pendingNavRoute = com.example.intrack.ui.navigation.Screen.AllTransactions.route
            }
            intent?.removeExtra("EXTRA_AUTO_LOG_EXPENSE")
            intent?.removeExtra("EXTRA_EXPENSE_TITLE")
            intent?.removeExtra("EXTRA_EXPENSE_AMOUNT")
            intent?.removeExtra("EXTRA_EXPENSE_CATEGORY")
            intent?.removeExtra("EXTRA_EXPENSE_COMMITMENT")
            intent?.removeExtra("EXTRA_EXPENSE_ACCOUNT")
            intent?.removeExtra("NAV_ROUTE")
            return
        }

        val route = intent?.getStringExtra("NAV_ROUTE")
        val category = intent?.getStringExtra("PRESET_CATEGORY")
        if (route != null) {
            pendingNavRoute = if (!category.isNullOrEmpty()) {
                "add_expense?category="
            } else {
                route
            }
            intent.removeExtra("NAV_ROUTE")
            intent.removeExtra("PRESET_CATEGORY")
        }
    }}

@Preview(showBackground = true)
@Composable
fun MainAppScreenPreview() {
    InTrackTheme {
        FinFlowNavGraph()
    }
}
