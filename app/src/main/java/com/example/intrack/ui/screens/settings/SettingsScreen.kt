package com.example.intrack.ui.screens.settings

import android.widget.Toast
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.CurrencyExchange
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.intrack.ui.theme.AppThemeMode
import com.example.intrack.ui.theme.BrandTangerine
import com.example.intrack.ui.theme.PlusJakartaSansFontFamily
import com.example.intrack.viewmodel.AuthViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Design Tokens matching Figma node 2015:802
private val FigmaBg: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.background
private val FigmaDark: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onSurface
private val FigmaMuted: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onSurfaceVariant
private val FigmaSubtle: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
private val FigmaBorder: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.outlineVariant
private val FigmaPeachTint: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.primaryContainer
private val FigmaRust: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.primary
private val FigmaIconBg: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.surfaceContainerHighest
private val FigmaActiveGreen = Color(0xFF2ECC71)
private val FigmaSuccessBadgeBg = Color(0xFFE8F8F0)
private val FigmaSuccessBadgeText = Color(0xFF1E8449)
private val FigmaDestructive = Color(0xFFD9381E)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onSignOut: () -> Unit,
    themeMode: AppThemeMode = AppThemeMode.LIGHT,
    onThemeModeChange: (AppThemeMode) -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    authViewModel: AuthViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val userProfile by authViewModel.userProfile.collectAsState()
    val userName by authViewModel.userDisplayName.collectAsState()
    val userEmail by authViewModel.userEmail.collectAsState()
    val userInitials by authViewModel.userInitials.collectAsState()

    // State for interactive preferences persisted to Firestore UserProfile
    val baseCurrency = userProfile.baseCurrency
    val billingCycle = userProfile.billingCycle
    val weeklyStartDay = userProfile.weeklyStartDay

    // Toggle states
    var pushNotificationsEnabled by remember { mutableStateOf(true) }
    var renewalRemindersEnabled by remember { mutableStateOf(true) }
    var autoDebitMarginEnabled by remember { mutableStateOf(true) }
    var biometricsEnabled by remember { mutableStateOf(true) }

    // Dialog & sheet visibility states
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showBillingCycleDialog by remember { mutableStateOf(false) }
    var showWeeklyStartDialog by remember { mutableStateOf(false) }
    var showTwoFactorDialog by remember { mutableStateOf(false) }
    var showExportSheet by remember { mutableStateOf(false) }
    var showClearCacheDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var isSyncing by remember { mutableStateOf(false) }
    var syncStatusText by remember { mutableStateOf("Cloud Active • Synced 2m ago") }

    // Infinite rotation for cloud sync indicator
    val syncTransition = rememberInfiniteTransition(label = "syncRotation")
    val syncRotation by syncTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing)
        ),
        label = "syncRotationAngle"
    )

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 600.dp
        val isCompactWidth = maxWidth < 360.dp
        val horizontalPadding = when {
            isCompactWidth -> 12.dp
            isWideScreen -> 24.dp
            else -> 16.dp
        }
        val contentMaxWidth = if (isWideScreen) 1040.dp else 560.dp

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = FigmaBg,
            topBar = {
                Surface(
                    color = FigmaBg,
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(max = contentMaxWidth)
                                .padding(horizontal = horizontalPadding, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            IconButton(
                                onClick = onNavigateBack,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surface)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                    contentDescription = "Back",
                                    tint = FigmaDark
                                )
                            }

                            Text(
                                text = "Settings & Account",
                                fontFamily = PlusJakartaSansFontFamily,
                                fontSize = if (isCompactWidth) 16.sp else 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = FigmaDark
                            )

                            IconButton(
                                onClick = { showHelpDialog = true },
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surface)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                                    contentDescription = "Help & Info",
                                    tint = FigmaDark
                                )
                            }
                        }
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.TopCenter
            ) {
                if (isWideScreen) {
                    // ==========================================
                    // RESPONSIVE 2-COLUMN LAYOUT (Tablets / Landscape)
                    // ==========================================
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = contentMaxWidth)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = horizontalPadding, vertical = 8.dp)
                            .padding(bottom = 36.dp)
                            .navigationBarsPadding(),
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Left Column (Profile, Appearance, Privacy & Security, Log Out, Version)
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(18.dp)
                        ) {
                            SettingsProfileCard(
                                userInitials = userInitials,
                                userName = userName,
                                userEmail = userEmail,
                                syncStatusText = syncStatusText,
                                isCompactWidth = isCompactWidth,
                                onClick = onNavigateToProfile
                            )

                            SettingsSectionHeader(title = "APPEARANCE")
                            AppearanceGroupCard(
                                themeMode = themeMode,
                                onThemeModeChange = onThemeModeChange,
                                isCompactWidth = isCompactWidth
                            )

                            SettingsSectionHeader(title = "PRIVACY & SECURITY")
                            PrivacySecurityGroupCard(
                                biometricsEnabled = biometricsEnabled,
                                onBiometricsChange = { biometricsEnabled = it },
                                isCompactWidth = isCompactWidth,
                                onTwoFactorClick = { showTwoFactorDialog = true }
                            )

                            Spacer(modifier = Modifier.height(4.dp))
                            LogOutButton(onClick = { showLogoutDialog = true })
                            Spacer(modifier = Modifier.height(2.dp))
                            FooterVersionText(modifier = Modifier.align(Alignment.CenterHorizontally))
                        }

                        // Right Column (Wallet & Currency, Subscriptions & Notifications, Data Management)
                        Column(
                            modifier = Modifier
                                .weight(1.15f)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(18.dp)
                        ) {
                            SettingsSectionHeader(title = "WALLET & CURRENCY")
                            WalletCurrencyGroupCard(
                                baseCurrency = baseCurrency,
                                billingCycle = billingCycle,
                                weeklyStartDay = weeklyStartDay,
                                isCompactWidth = isCompactWidth,
                                onCurrencyClick = { showCurrencyDialog = true },
                                onBillingCycleClick = { showBillingCycleDialog = true },
                                onWeeklyStartClick = { showWeeklyStartDialog = true }
                            )

                            SettingsSectionHeader(title = "SUBSCRIPTIONS & NOTIFICATIONS")
                            NotificationsGroupCard(
                                pushNotificationsEnabled = pushNotificationsEnabled,
                                onPushNotificationsChange = { pushNotificationsEnabled = it },
                                renewalRemindersEnabled = renewalRemindersEnabled,
                                onRenewalRemindersChange = { renewalRemindersEnabled = it },
                                autoDebitMarginEnabled = autoDebitMarginEnabled,
                                onAutoDebitMarginChange = { autoDebitMarginEnabled = it },
                                isCompactWidth = isCompactWidth
                            )

                            SettingsSectionHeader(title = "DATA MANAGEMENT")
                            DataManagementGroupCard(
                                isSyncing = isSyncing,
                                syncRotation = syncRotation,
                                isCompactWidth = isCompactWidth,
                                onExportClick = { showExportSheet = true },
                                onSyncClick = {
                                    if (!isSyncing) {
                                        coroutineScope.launch {
                                            isSyncing = true
                                            syncStatusText = "Syncing with Cloud..."
                                            delay(1200)
                                            isSyncing = false
                                            syncStatusText = "Cloud Active • Just now"
                                            snackbarHostState.showSnackbar("Cloud vault synchronized successfully with Firebase!")
                                        }
                                    }
                                },
                                onClearCacheClick = { showClearCacheDialog = true }
                            )
                        }
                    }
                } else {
                    // ==========================================
                    // RESPONSIVE SINGLE-COLUMN LAYOUT (Phones / Portrait)
                    // ==========================================
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = contentMaxWidth)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = horizontalPadding, vertical = 8.dp)
                            .padding(bottom = 36.dp)
                            .navigationBarsPadding(),
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        SettingsProfileCard(
                            userInitials = userInitials,
                            userName = userName,
                            userEmail = userEmail,
                            syncStatusText = syncStatusText,
                            isCompactWidth = isCompactWidth,
                            onClick = onNavigateToProfile
                        )

                        SettingsSectionHeader(title = "WALLET & CURRENCY")
                        WalletCurrencyGroupCard(
                            baseCurrency = baseCurrency,
                            billingCycle = billingCycle,
                            weeklyStartDay = weeklyStartDay,
                            isCompactWidth = isCompactWidth,
                            onCurrencyClick = { showCurrencyDialog = true },
                            onBillingCycleClick = { showBillingCycleDialog = true },
                            onWeeklyStartClick = { showWeeklyStartDialog = true }
                        )

                        SettingsSectionHeader(title = "SUBSCRIPTIONS & NOTIFICATIONS")
                        NotificationsGroupCard(
                            pushNotificationsEnabled = pushNotificationsEnabled,
                            onPushNotificationsChange = { pushNotificationsEnabled = it },
                            renewalRemindersEnabled = renewalRemindersEnabled,
                            onRenewalRemindersChange = { renewalRemindersEnabled = it },
                            autoDebitMarginEnabled = autoDebitMarginEnabled,
                            onAutoDebitMarginChange = { autoDebitMarginEnabled = it },
                            isCompactWidth = isCompactWidth
                        )

                        SettingsSectionHeader(title = "PRIVACY & SECURITY")
                        PrivacySecurityGroupCard(
                            biometricsEnabled = biometricsEnabled,
                            onBiometricsChange = { biometricsEnabled = it },
                            isCompactWidth = isCompactWidth,
                            onTwoFactorClick = { showTwoFactorDialog = true }
                        )

                        SettingsSectionHeader(title = "DATA MANAGEMENT")
                        DataManagementGroupCard(
                            isSyncing = isSyncing,
                            syncRotation = syncRotation,
                            isCompactWidth = isCompactWidth,
                            onExportClick = { showExportSheet = true },
                            onSyncClick = {
                                if (!isSyncing) {
                                    coroutineScope.launch {
                                        isSyncing = true
                                        syncStatusText = "Syncing with Cloud..."
                                        delay(1200)
                                        isSyncing = false
                                        syncStatusText = "Cloud Active • Just now"
                                        snackbarHostState.showSnackbar("Cloud vault synchronized successfully with Firebase!")
                                    }
                                }
                            },
                            onClearCacheClick = { showClearCacheDialog = true }
                        )

                        SettingsSectionHeader(title = "APPEARANCE")
                        AppearanceGroupCard(
                            themeMode = themeMode,
                            onThemeModeChange = onThemeModeChange,
                            isCompactWidth = isCompactWidth
                        )

                        Spacer(modifier = Modifier.height(6.dp))
                        LogOutButton(onClick = { showLogoutDialog = true })
                        Spacer(modifier = Modifier.height(2.dp))
                        FooterVersionText(modifier = Modifier.align(Alignment.CenterHorizontally))
                    }
                }
            }
        }
    }

    // =========================================================================
    // RESPONSIVE DIALOGS & BOTTOM SHEETS
    // =========================================================================

    // 1. Currency Dialog
    if (showCurrencyDialog) {
        val currencies = listOf(
            "LKR (Rs) • Sri Lankan Rupee",
            "USD ($) • US Dollar",
            "EUR (€) • Euro",
            "GBP (£) • British Pound",
            "AUD ($) • Australian Dollar",
            "SGD ($) • Singapore Dollar",
            "JPY (¥) • Japanese Yen"
        )
        AlertDialog(
            onDismissRequest = { showCurrencyDialog = false },
            shape = RoundedCornerShape(20.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.widthIn(max = 460.dp),
            title = {
                Text(
                    text = "Select Base Currency",
                    fontFamily = PlusJakartaSansFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = FigmaDark
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 280.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    currencies.forEach { item ->
                        val isSelected = baseCurrency == item
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) FigmaPeachTint else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, if (isSelected) FigmaRust else FigmaBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    authViewModel.updatePreferences(baseCurrency = item)
                                    showCurrencyDialog = false
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item,
                                    fontFamily = PlusJakartaSansFontFamily,
                                    fontSize = 13.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) FigmaRust else FigmaDark,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = null,
                                        tint = FigmaRust,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showCurrencyDialog = false }) {
                    Text("Cancel", color = FigmaDark, fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }

    // 2. Monthly Billing Cycle Dialog
    if (showBillingCycleDialog) {
        val cycles = listOf(
            "1st of Every Month",
            "5th of Every Month",
            "10th of Every Month",
            "15th of Every Month",
            "25th of Every Month",
            "Last Day of Every Month"
        )
        AlertDialog(
            onDismissRequest = { showBillingCycleDialog = false },
            shape = RoundedCornerShape(20.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.widthIn(max = 460.dp),
            title = {
                Text(
                    text = "Monthly Billing Cycle",
                    fontFamily = PlusJakartaSansFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = FigmaDark
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 280.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    cycles.forEach { item ->
                        val isSelected = billingCycle == item
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) FigmaPeachTint else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, if (isSelected) FigmaRust else FigmaBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    authViewModel.updatePreferences(billingCycle = item)
                                    showBillingCycleDialog = false
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item,
                                    fontFamily = PlusJakartaSansFontFamily,
                                    fontSize = 13.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) FigmaRust else FigmaDark,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = null,
                                        tint = FigmaRust,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showBillingCycleDialog = false }) {
                    Text("Cancel", color = FigmaDark, fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }

    // 3. Weekly Start Day Dialog
    if (showWeeklyStartDialog) {
        AlertDialog(
            onDismissRequest = { showWeeklyStartDialog = false },
            shape = RoundedCornerShape(20.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.widthIn(max = 460.dp),
            title = {
                Text(
                    text = "Weekly Start Day",
                    fontFamily = PlusJakartaSansFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = FigmaDark
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 280.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Monday", "Sunday").forEach { item ->
                        val isSelected = weeklyStartDay == item
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) FigmaPeachTint else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, if (isSelected) FigmaRust else FigmaBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    authViewModel.updatePreferences(weeklyStartDay = item)
                                    showWeeklyStartDialog = false
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item,
                                    fontFamily = PlusJakartaSansFontFamily,
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) FigmaRust else FigmaDark
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = null,
                                        tint = FigmaRust,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showWeeklyStartDialog = false }) {
                    Text("Cancel", color = FigmaDark, fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }

    // 4. Two Factor Auth Dialog
    if (showTwoFactorDialog) {
        AlertDialog(
            onDismissRequest = { showTwoFactorDialog = false },
            shape = RoundedCornerShape(20.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.widthIn(max = 460.dp),
            icon = {
                Icon(
                    imageVector = Icons.Rounded.Shield,
                    contentDescription = null,
                    tint = BrandTangerine,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Two-Factor Authentication",
                    fontFamily = PlusJakartaSansFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = FigmaDark
                )
            },
            text = {
                Text(
                    text = "Your account is secured with Google Authenticator (TOTP). Backup security codes are saved to your private cloud vault.",
                    fontFamily = PlusJakartaSansFontFamily,
                    fontSize = 13.5.sp,
                    color = FigmaMuted,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { showTwoFactorDialog = false }) {
                    Text("Done", color = BrandTangerine, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // 5. Statement Export BottomSheet
    if (showExportSheet) {
        ModalBottomSheet(
            onDismissRequest = { showExportSheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 560.dp)
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                        .navigationBarsPadding(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Export Financial Statement",
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = FigmaDark
                    )
                    Text(
                        text = "Choose your desired file format for export:",
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 13.sp,
                        color = FigmaMuted
                    )

                    listOf(
                        Triple("Excel Workbook (.xlsx)", "Complete data sheets with category formulas", Icons.Rounded.FileDownload),
                        Triple("CSV Spreadsheet (.csv)", "Plain tabular export for accounting tools", Icons.Rounded.FileDownload),
                        Triple("Audit PDF Statement (.pdf)", "Formatted ledger statement with charts", Icons.Rounded.FileDownload)
                    ).forEach { (format, desc, icon) ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, FigmaBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showExportSheet = false
                                    Toast.makeText(context, "Exporting $format...", Toast.LENGTH_SHORT).show()
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(FigmaIconBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = BrandTangerine,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = format,
                                        fontFamily = PlusJakartaSansFontFamily,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = FigmaDark,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = desc,
                                        fontFamily = PlusJakartaSansFontFamily,
                                        fontSize = 12.sp,
                                        color = FigmaMuted,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }

    // 6. Clear Cache Confirmation
    if (showClearCacheDialog) {
        AlertDialog(
            onDismissRequest = { showClearCacheDialog = false },
            shape = RoundedCornerShape(20.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.widthIn(max = 460.dp),
            title = {
                Text(
                    text = "Clear Cached Data?",
                    fontFamily = PlusJakartaSansFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = FigmaDark
                )
            },
            text = {
                Text(
                    text = "This will clear 14.2 MB of cached images, preview thumbnails, and offline indexes. Your live data synced to Firebase remains safe.",
                    fontFamily = PlusJakartaSansFontFamily,
                    fontSize = 13.5.sp,
                    color = FigmaMuted,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearCacheDialog = false
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Local cache cleared (14.2 MB freed).")
                        }
                    }
                ) {
                    Text("Clear Cache", color = FigmaDestructive, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearCacheDialog = false }) {
                    Text("Cancel", color = FigmaDark, fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }

    // 7. Help & Support Dialog
    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            shape = RoundedCornerShape(20.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.widthIn(max = 460.dp),
            title = {
                Text(
                    text = "InTrack Support & Guide",
                    fontFamily = PlusJakartaSansFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = FigmaDark
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 260.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "• Vault Sync: Keeps your offline ledger and Firestore database synchronized automatically in real-time.\n\n• Safety Margin: Alerts you when active recurring subscriptions exceed 60% of liquid card balances.\n\n• Need assistance? Contact our engineering team at support@intrack.app",
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 13.sp,
                        color = FigmaMuted,
                        lineHeight = 19.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showHelpDialog = false }) {
                    Text("Close", color = BrandTangerine, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // 8. Sign Out Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            shape = RoundedCornerShape(20.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.widthIn(max = 460.dp),
            title = {
                Text(
                    text = "Sign Out?",
                    fontFamily = PlusJakartaSansFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = FigmaDark
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to log out from this device? Your synchronized ledger entries remain secure on the cloud.",
                    fontFamily = PlusJakartaSansFontFamily,
                    fontSize = 13.5.sp,
                    color = FigmaMuted,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        authViewModel.signOut()
                        onSignOut()
                    }
                ) {
                    Text("Log Out", color = FigmaDestructive, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = FigmaDark, fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }
}

// =========================================================================
// REUSABLE & RESPONSIVE SECTION CARDS
// =========================================================================

@Composable
private fun SettingsProfileCard(
    userInitials: String,
    userName: String,
    userEmail: String,
    syncStatusText: String,
    isCompactWidth: Boolean,
    onClick: () -> Unit
) {
    val avatarSize = if (isCompactWidth) 48.dp else 54.dp
    val avatarFontSize = if (isCompactWidth) 17.sp else 19.sp

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, FigmaBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (isCompactWidth) 14.dp else 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar with online status badge
            Box(modifier = Modifier.size(avatarSize)) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(FigmaPeachTint),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = userInitials,
                        fontFamily = PlusJakartaSansFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = avatarFontSize,
                        color = FigmaRust
                    )
                }

                // Green Active Dot
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .align(Alignment.BottomEnd)
                        .padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(FigmaActiveGreen)
                    )
                }
            }

            Spacer(modifier = Modifier.width(if (isCompactWidth) 12.dp else 16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = userName.ifBlank { "Account User" },
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = if (isCompactWidth) 15.sp else 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = FigmaDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    // Premium Plan Badge
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = FigmaPeachTint
                    ) {
                        Text(
                            text = "PREMIUM PLAN",
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = FigmaRust,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = userEmail,
                    fontFamily = PlusJakartaSansFontFamily,
                    fontSize = if (isCompactWidth) 12.sp else 13.sp,
                    color = FigmaMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(FigmaActiveGreen)
                    )
                    Text(
                        text = syncStatusText,
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 11.5.sp,
                        color = FigmaSubtle,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = "View Profile",
                tint = FigmaSubtle,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun WalletCurrencyGroupCard(
    baseCurrency: String,
    billingCycle: String,
    weeklyStartDay: String,
    isCompactWidth: Boolean,
    onCurrencyClick: () -> Unit,
    onBillingCycleClick: () -> Unit,
    onWeeklyStartClick: () -> Unit
) {
    SettingsGroupCard {
        SettingsActionRow(
            icon = Icons.Rounded.CurrencyExchange,
            title = "Base Currency",
            subtitle = baseCurrency,
            isCompact = isCompactWidth,
            onClick = onCurrencyClick
        )

        HorizontalDivider(color = FigmaBorder.copy(alpha = 0.6f), thickness = 0.8.dp)

        SettingsActionRow(
            icon = Icons.Rounded.CalendarMonth,
            title = "Monthly Billing Cycle",
            subtitle = billingCycle,
            isCompact = isCompactWidth,
            onClick = onBillingCycleClick
        )

        HorizontalDivider(color = FigmaBorder.copy(alpha = 0.6f), thickness = 0.8.dp)

        SettingsActionRow(
            icon = Icons.Rounded.DateRange,
            title = "Weekly Start Day",
            subtitle = weeklyStartDay,
            isCompact = isCompactWidth,
            onClick = onWeeklyStartClick
        )
    }
}

@Composable
private fun NotificationsGroupCard(
    pushNotificationsEnabled: Boolean,
    onPushNotificationsChange: (Boolean) -> Unit,
    renewalRemindersEnabled: Boolean,
    onRenewalRemindersChange: (Boolean) -> Unit,
    autoDebitMarginEnabled: Boolean,
    onAutoDebitMarginChange: (Boolean) -> Unit,
    isCompactWidth: Boolean
) {
    SettingsGroupCard {
        SettingsToggleRow(
            icon = Icons.Rounded.Notifications,
            title = "Push Notifications",
            subtitle = "Instant updates on ledger entries",
            checked = pushNotificationsEnabled,
            isCompact = isCompactWidth,
            onCheckedChange = onPushNotificationsChange
        )

        HorizontalDivider(color = FigmaBorder.copy(alpha = 0.6f), thickness = 0.8.dp)

        SettingsToggleRow(
            icon = Icons.Rounded.Alarm,
            title = "Renewal Reminders",
            subtitle = "72h Before Renewal",
            checked = renewalRemindersEnabled,
            isCompact = isCompactWidth,
            onCheckedChange = onRenewalRemindersChange
        )

        HorizontalDivider(color = FigmaBorder.copy(alpha = 0.6f), thickness = 0.8.dp)

        SettingsToggleRow(
            icon = Icons.Rounded.Security,
            title = "Auto-Debit Safety Margin",
            subtitle = "Warns when balance is low",
            checked = autoDebitMarginEnabled,
            isCompact = isCompactWidth,
            onCheckedChange = onAutoDebitMarginChange
        )
    }
}

@Composable
private fun PrivacySecurityGroupCard(
    biometricsEnabled: Boolean,
    onBiometricsChange: (Boolean) -> Unit,
    isCompactWidth: Boolean,
    onTwoFactorClick: () -> Unit
) {
    SettingsGroupCard {
        SettingsToggleRow(
            icon = Icons.Rounded.Fingerprint,
            title = "Biometrics (Face ID)",
            subtitle = "Quick access to vault",
            checked = biometricsEnabled,
            isCompact = isCompactWidth,
            onCheckedChange = onBiometricsChange
        )

        HorizontalDivider(color = FigmaBorder.copy(alpha = 0.6f), thickness = 0.8.dp)

        SettingsActionRow(
            icon = Icons.Rounded.Shield,
            title = "Two-Factor Auth (2FA)",
            subtitle = "Authenticator app enabled",
            badge = "Active",
            isCompact = isCompactWidth,
            onClick = onTwoFactorClick
        )
    }
}

@Composable
private fun DataManagementGroupCard(
    isSyncing: Boolean,
    syncRotation: Float,
    isCompactWidth: Boolean,
    onExportClick: () -> Unit,
    onSyncClick: () -> Unit,
    onClearCacheClick: () -> Unit
) {
    SettingsGroupCard {
        SettingsActionRow(
            icon = Icons.Rounded.FileDownload,
            title = "Statement Export",
            subtitle = ".xlsx, .csv, .pdf",
            isCompact = isCompactWidth,
            onClick = onExportClick
        )

        HorizontalDivider(color = FigmaBorder.copy(alpha = 0.6f), thickness = 0.8.dp)

        SettingsActionRow(
            icon = Icons.Rounded.Sync,
            title = "Force Cloud Vault Sync",
            subtitle = "Manual reconcile with server",
            iconModifier = if (isSyncing) Modifier.rotate(syncRotation) else Modifier,
            isCompact = isCompactWidth,
            onClick = onSyncClick
        )

        HorizontalDivider(color = FigmaBorder.copy(alpha = 0.6f), thickness = 0.8.dp)

        SettingsActionRow(
            icon = Icons.Rounded.DeleteSweep,
            title = "Clear Cache / Local Data",
            subtitle = "14.2 MB cached files",
            isCompact = isCompactWidth,
            onClick = onClearCacheClick
        )
    }
}

@Composable
private fun AppearanceGroupCard(
    themeMode: AppThemeMode,
    onThemeModeChange: (AppThemeMode) -> Unit,
    isCompactWidth: Boolean
) {
    SettingsGroupCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (isCompactWidth) 12.dp else 16.dp)
        ) {
            Text(
                text = "App Theme Mode",
                fontFamily = PlusJakartaSansFontFamily,
                fontSize = if (isCompactWidth) 13.5.sp else 14.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = FigmaDark
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Choose your preferred interface theme",
                fontFamily = PlusJakartaSansFontFamily,
                fontSize = if (isCompactWidth) 11.5.sp else 12.5.sp,
                color = FigmaMuted
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(if (isCompactWidth) 6.dp else 8.dp)
            ) {
                listOf(
                    AppThemeMode.LIGHT to "Light",
                    AppThemeMode.DARK to "Dark",
                    AppThemeMode.SYSTEM to "System"
                ).forEach { (mode, label) ->
                    val isSelected = themeMode == mode
                    FilterChip(
                        selected = isSelected,
                        onClick = { onThemeModeChange(mode) },
                        label = {
                            Text(
                                text = label,
                                fontFamily = PlusJakartaSansFontFamily,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = if (isCompactWidth) 11.5.sp else 12.5.sp,
                                maxLines = 1
                            )
                        },
                        leadingIcon = if (isSelected && !isCompactWidth) {
                            {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = BrandTangerine
                                )
                            }
                        } else null,
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FigmaPeachTint,
                            selectedLabelColor = FigmaRust,
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = FigmaDark
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) FigmaRust else FigmaBorder,
                            selectedBorderColor = FigmaRust
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun LogOutButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.2.dp, FigmaDestructive.copy(alpha = 0.35f)),
        colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.Logout,
                contentDescription = "Log Out",
                tint = FigmaDestructive,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Log Out",
                fontFamily = PlusJakartaSansFontFamily,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = FigmaDestructive
            )
        }
    }
}

@Composable
private fun FooterVersionText(modifier: Modifier = Modifier) {
    Text(
        text = "FinanceFlow v2.4.0 (Build 382)",
        fontFamily = PlusJakartaSansFontFamily,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        color = FigmaSubtle,
        modifier = modifier
    )
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        fontFamily = PlusJakartaSansFontFamily,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.8.sp,
        color = FigmaSubtle,
        modifier = Modifier.padding(start = 4.dp, top = 2.dp)
    )
}

@Composable
private fun SettingsGroupCard(content: @Composable () -> Unit) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, FigmaBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            content()
        }
    }
}

@Composable
private fun SettingsActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    badge: String? = null,
    iconModifier: Modifier = Modifier,
    isCompact: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(
                horizontal = if (isCompact) 12.dp else 16.dp,
                vertical = if (isCompact) 11.dp else 14.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(if (isCompact) 34.dp else 38.dp)
                .clip(CircleShape)
                .background(FigmaIconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = BrandTangerine,
                modifier = iconModifier.size(if (isCompact) 17.dp else 19.dp)
            )
        }

        Spacer(modifier = Modifier.width(if (isCompact) 10.dp else 14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = PlusJakartaSansFontFamily,
                fontSize = if (isCompact) 13.5.sp else 14.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = FigmaDark,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(1.5.dp))
            Text(
                text = subtitle,
                fontFamily = PlusJakartaSansFontFamily,
                fontSize = if (isCompact) 11.5.sp else 12.sp,
                color = FigmaMuted,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (!badge.isNullOrBlank()) {
            Spacer(modifier = Modifier.width(6.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = FigmaSuccessBadgeBg
            ) {
                Text(
                    text = badge,
                    fontFamily = PlusJakartaSansFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = FigmaSuccessBadgeText,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.5.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = FigmaSubtle.copy(alpha = 0.6f),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    isCompact: Boolean = false,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(
                horizontal = if (isCompact) 12.dp else 16.dp,
                vertical = if (isCompact) 10.dp else 12.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(if (isCompact) 34.dp else 38.dp)
                .clip(CircleShape)
                .background(FigmaIconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = BrandTangerine,
                modifier = Modifier.size(if (isCompact) 17.dp else 19.dp)
            )
        }

        Spacer(modifier = Modifier.width(if (isCompact) 10.dp else 14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = PlusJakartaSansFontFamily,
                fontSize = if (isCompact) 13.5.sp else 14.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = FigmaDark,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(1.5.dp))
            Text(
                text = subtitle,
                fontFamily = PlusJakartaSansFontFamily,
                fontSize = if (isCompact) 11.5.sp else 12.sp,
                color = FigmaMuted,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(6.dp))

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.surface,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest
            )
        )
    }
}
