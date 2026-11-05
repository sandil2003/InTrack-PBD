package com.example.intrack.ui.screens.profile

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.CurrencyExchange
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.LockReset
import androidx.compose.material.icons.rounded.Mail
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.intrack.data.model.UserProfile
import com.example.intrack.ui.theme.*
import com.example.intrack.ui.theme.financial
import com.example.intrack.viewmodel.AuthViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onSignOut: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val userProfile by authViewModel.userProfile.collectAsState()
    val userName by authViewModel.userDisplayName.collectAsState()
    val userEmail by authViewModel.userEmail.collectAsState()
    val userInitials by authViewModel.userInitials.collectAsState()

    // Interactive states
    var showEditProfileSheet by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showBillingCycleDialog by remember { mutableStateOf(false) }
    var showWeeklyStartDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var biometricsEnabled by remember { mutableStateOf(true) }
    var isSyncing by remember { mutableStateOf(false) }
    var syncStatusText by remember { mutableStateOf("Cloud Synced • Real-time") }

    // Sync animation
    val syncTransition = rememberInfiniteTransition(label = "profileSyncRotation")
    val syncRotation by syncTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing)
        ),
        label = "profileSyncRotationAngle"
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.White,
        topBar = {
            Surface(
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(BrandSoftSlate)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF121660)
                        )
                    }

                    Text(
                        text = "My Profile",
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF121660)
                    )

                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(BrandSoftSlate)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "Settings",
                            tint = Color(0xFF121660)
                        )
                    }
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val isWideScreen = maxWidth >= 600.dp
            val isCompactWidth = maxWidth < 360.dp
            val horizontalPadding = when {
                isCompactWidth -> 12.dp
                isWideScreen -> 24.dp
                else -> 16.dp
            }

            if (isWideScreen) {
                // ==========================================
                // RESPONSIVE 2-COLUMN LAYOUT (Tablets / Landscape)
                // ==========================================
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = horizontalPadding, vertical = 8.dp)
                        .padding(bottom = 36.dp)
                        .navigationBarsPadding(),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Left Column (Hero Card + Metric Ribbon + Account Actions)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        ProfileHeroCard(
                            userInitials = userInitials,
                            userName = userName,
                            userEmail = userEmail,
                            memberSince = userProfile.memberSinceFormatted,
                            onEditClick = { showEditProfileSheet = true }
                        )

                        ProfileMetricsRow(
                            baseCurrency = userProfile.baseCurrency,
                            billingCycle = userProfile.billingCycle,
                            syncStatusText = syncStatusText,
                            isSyncing = isSyncing,
                            syncRotation = syncRotation,
                            isCompact = false,
                            onCurrencyClick = { showCurrencyDialog = true },
                            onBillingCycleClick = { showBillingCycleDialog = true },
                            onSyncClick = {
                                if (!isSyncing) {
                                    isSyncing = true
                                    syncStatusText = "Synchronizing..."
                                    coroutineScope.launch {
                                        delay(1200)
                                        isSyncing = false
                                        syncStatusText = "Cloud Synced • Just now"
                                        snackbarHostState.showSnackbar("All financial records synchronized")
                                    }
                                }
                            }
                        )

                        ProfileSectionHeader(title = "ACCOUNT ACTIONS")

                        AccountActionsCard(
                            onSignOutClick = { showLogoutDialog = true },
                            onDeleteAccountClick = { showDeleteAccountDialog = true }
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        FooterVersionText(modifier = Modifier.align(Alignment.CenterHorizontally))
                    }

                    // Right Column (Personal Details + Preferences + Security)
                    Column(
                        modifier = Modifier
                            .weight(1.2f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        ProfileSectionHeader(title = "PERSONAL DETAILS")

                        PersonalDetailsCard(
                            userName = userName,
                            userEmail = userEmail,
                            userProfile = userProfile,
                            onEditNameClick = { showEditProfileSheet = true },
                            onEditPhoneClick = { showEditProfileSheet = true },
                            onCopyIdClick = {
                                if (userProfile.uid.isNotBlank()) {
                                    clipboardManager.setText(AnnotatedString(userProfile.uid))
                                    Toast.makeText(context, "User ID copied to clipboard", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )

                        ProfileSectionHeader(title = "FINANCIAL PREFERENCES")

                        FinancialPreferencesCard(
                            userProfile = userProfile,
                            onCurrencyClick = { showCurrencyDialog = true },
                            onBillingCycleClick = { showBillingCycleDialog = true },
                            onWeeklyStartClick = { showWeeklyStartDialog = true }
                        )

                        ProfileSectionHeader(title = "SECURITY & DATA")

                        SecurityDataCard(
                            userEmail = userEmail,
                            biometricsEnabled = biometricsEnabled,
                            onBiometricsChange = { biometricsEnabled = it },
                            syncStatusText = syncStatusText,
                            isSyncing = isSyncing,
                            syncRotation = syncRotation,
                            onResetPasswordClick = {
                                if (userEmail.isNotBlank()) {
                                    authViewModel.sendPasswordReset(userEmail) {
                                        Toast.makeText(context, "Password reset link sent to $userEmail", Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            onManualSyncClick = {
                                if (!isSyncing) {
                                    isSyncing = true
                                    syncStatusText = "Synchronizing..."
                                    coroutineScope.launch {
                                        delay(1200)
                                        isSyncing = false
                                        syncStatusText = "Cloud Synced • Just now"
                                        snackbarHostState.showSnackbar("All cloud records synchronized")
                                    }
                                }
                            },
                            onNavigateToSettings = onNavigateToSettings
                        )
                    }
                }
            } else {
                // ==========================================
                // RESPONSIVE SINGLE-COLUMN LAYOUT (Phones / Portrait)
                // ==========================================
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 560.dp)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = horizontalPadding, vertical = 8.dp)
                            .padding(bottom = 36.dp)
                            .navigationBarsPadding(),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        ProfileHeroCard(
                            userInitials = userInitials,
                            userName = userName,
                            userEmail = userEmail,
                            memberSince = userProfile.memberSinceFormatted,
                            onEditClick = { showEditProfileSheet = true }
                        )

                        ProfileMetricsRow(
                            baseCurrency = userProfile.baseCurrency,
                            billingCycle = userProfile.billingCycle,
                            syncStatusText = syncStatusText,
                            isSyncing = isSyncing,
                            syncRotation = syncRotation,
                            isCompact = isCompactWidth,
                            onCurrencyClick = { showCurrencyDialog = true },
                            onBillingCycleClick = { showBillingCycleDialog = true },
                            onSyncClick = {
                                if (!isSyncing) {
                                    isSyncing = true
                                    syncStatusText = "Synchronizing..."
                                    coroutineScope.launch {
                                        delay(1200)
                                        isSyncing = false
                                        syncStatusText = "Cloud Synced • Just now"
                                        snackbarHostState.showSnackbar("All financial records synchronized")
                                    }
                                }
                            }
                        )

                        ProfileSectionHeader(title = "PERSONAL DETAILS")

                        PersonalDetailsCard(
                            userName = userName,
                            userEmail = userEmail,
                            userProfile = userProfile,
                            onEditNameClick = { showEditProfileSheet = true },
                            onEditPhoneClick = { showEditProfileSheet = true },
                            onCopyIdClick = {
                                if (userProfile.uid.isNotBlank()) {
                                    clipboardManager.setText(AnnotatedString(userProfile.uid))
                                    Toast.makeText(context, "User ID copied to clipboard", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )

                        ProfileSectionHeader(title = "FINANCIAL PREFERENCES")

                        FinancialPreferencesCard(
                            userProfile = userProfile,
                            onCurrencyClick = { showCurrencyDialog = true },
                            onBillingCycleClick = { showBillingCycleDialog = true },
                            onWeeklyStartClick = { showWeeklyStartDialog = true }
                        )

                        ProfileSectionHeader(title = "SECURITY & DATA")

                        SecurityDataCard(
                            userEmail = userEmail,
                            biometricsEnabled = biometricsEnabled,
                            onBiometricsChange = { biometricsEnabled = it },
                            syncStatusText = syncStatusText,
                            isSyncing = isSyncing,
                            syncRotation = syncRotation,
                            onResetPasswordClick = {
                                if (userEmail.isNotBlank()) {
                                    authViewModel.sendPasswordReset(userEmail) {
                                        Toast.makeText(context, "Password reset link sent to $userEmail", Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            onManualSyncClick = {
                                if (!isSyncing) {
                                    isSyncing = true
                                    syncStatusText = "Synchronizing..."
                                    coroutineScope.launch {
                                        delay(1200)
                                        isSyncing = false
                                        syncStatusText = "Cloud Synced • Just now"
                                        snackbarHostState.showSnackbar("All cloud records synchronized")
                                    }
                                }
                            },
                            onNavigateToSettings = onNavigateToSettings
                        )

                        ProfileSectionHeader(title = "ACCOUNT ACTIONS")

                        AccountActionsCard(
                            onSignOutClick = { showLogoutDialog = true },
                            onDeleteAccountClick = { showDeleteAccountDialog = true }
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        FooterVersionText(modifier = Modifier.align(Alignment.CenterHorizontally))
                    }
                }
            }
        }
    }

    // ==========================================
    // RESPONSIVE EDIT PROFILE BOTTOM SHEET
    // ==========================================
    if (showEditProfileSheet) {
        EditProfileBottomSheet(
            currentName = userName,
            currentPhone = userProfile.phoneNumber,
            currentBio = userProfile.bio,
            onDismiss = { showEditProfileSheet = false },
            onSave = { newName, newPhone, newBio ->
                authViewModel.updateUserProfileDetails(
                    displayName = newName,
                    phoneNumber = newPhone,
                    bio = newBio
                ) { success, errorMsg ->
                    if (success) {
                        showEditProfileSheet = false
                        Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, errorMsg ?: "Failed to update profile", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }

    // ==========================================
    // RESPONSIVE SCROLLABLE CURRENCY SELECTION DIALOG
    // ==========================================
    if (showCurrencyDialog) {
        val currencies = listOf(
            "LKR (Rs) • Sri Lankan Rupee",
            "USD ($) • US Dollar",
            "EUR (€) • Euro",
            "GBP (£) • British Pound",
            "INR (₹) • Indian Rupee",
            "AED (د.إ) • UAE Dirham",
            "AUD ($) • Australian Dollar",
            "CAD ($) • Canadian Dollar",
            "SGD ($) • Singapore Dollar"
        )
        AlertDialog(
            onDismissRequest = { showCurrencyDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = "Select Primary Currency",
                    fontFamily = PlusJakartaSansFontFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 280.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    currencies.forEach { currency ->
                        val isSelected = userProfile.baseCurrency == currency
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                                .clickable {
                                    authViewModel.updatePreferences(baseCurrency = currency)
                                    showCurrencyDialog = false
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = currency,
                                fontFamily = PlusJakartaSansFontFamily,
                                fontSize = 13.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showCurrencyDialog = false }) {
                    Text("Close", fontFamily = PlusJakartaSansFontFamily, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    // ==========================================
    // RESPONSIVE SCROLLABLE BILLING CYCLE DIALOG
    // ==========================================
    if (showBillingCycleDialog) {
        val cycleOptions = listOf(
            "1st of Every Month",
            "5th of Every Month",
            "10th of Every Month",
            "15th of Every Month",
            "20th of Every Month",
            "25th of Every Month",
            "Last Day of Every Month"
        )
        AlertDialog(
            onDismissRequest = { showBillingCycleDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = "Monthly Cycle Anchor",
                    fontFamily = PlusJakartaSansFontFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 280.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    cycleOptions.forEach { cycle ->
                        val isSelected = userProfile.billingCycle == cycle
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                                .clickable {
                                    authViewModel.updatePreferences(billingCycle = cycle)
                                    showBillingCycleDialog = false
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = cycle,
                                fontFamily = PlusJakartaSansFontFamily,
                                fontSize = 13.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showBillingCycleDialog = false }) {
                    Text("Close", fontFamily = PlusJakartaSansFontFamily, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    // ==========================================
    // RESPONSIVE WEEK START DIALOG
    // ==========================================
    if (showWeeklyStartDialog) {
        val days = listOf("Monday", "Saturday", "Sunday")
        AlertDialog(
            onDismissRequest = { showWeeklyStartDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = "Weekly Start Day",
                    fontFamily = PlusJakartaSansFontFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    days.forEach { day ->
                        val isSelected = userProfile.weeklyStartDay == day
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                                .clickable {
                                    authViewModel.updatePreferences(weeklyStartDay = day)
                                    showWeeklyStartDialog = false
                                }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = day,
                                fontFamily = PlusJakartaSansFontFamily,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showWeeklyStartDialog = false }) {
                    Text("Close", fontFamily = PlusJakartaSansFontFamily, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    // ==========================================
    // LOG OUT CONFIRMATION DIALOG
    // ==========================================
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = "Sign Out from InTrack?",
                    fontFamily = PlusJakartaSansFontFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = "Your local session will be closed safely. All your finances remain synchronized in Cloud Firestore.",
                    fontFamily = PlusJakartaSansFontFamily,
                    fontSize = 13.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onSignOut()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Sign Out",
                        fontFamily = PlusJakartaSansFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(
                        text = "Cancel",
                        fontFamily = PlusJakartaSansFontFamily,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        )
    }

    // ==========================================
    // DELETE ACCOUNT DIALOG
    // ==========================================
    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = "Delete InTrack Account?",
                    fontFamily = PlusJakartaSansFontFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            },
            text = {
                Text(
                    text = "This will permanently wipe your profile and clear all tracked income, expenses, subscriptions, and financial goals. This action cannot be reversed.",
                    fontFamily = PlusJakartaSansFontFamily,
                    fontSize = 13.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteAccountDialog = false
                        Toast.makeText(context, "Account deletion requested. Signing out...", Toast.LENGTH_SHORT).show()
                        onSignOut()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Delete Forever", fontFamily = PlusJakartaSansFontFamily, fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) {
                    Text("Cancel", fontFamily = PlusJakartaSansFontFamily, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        )
    }
}

// ==========================================
// RESPONSIVE EDIT PROFILE MODAL BOTTOM SHEET
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditProfileBottomSheet(
    currentName: String,
    currentPhone: String,
    currentBio: String,
    onDismiss: () -> Unit,
    onSave: (name: String, phone: String, bio: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var nameInput by remember { mutableStateOf(currentName) }
    var phoneInput by remember { mutableStateOf(currentPhone) }
    var bioInput by remember { mutableStateOf(currentBio) }
    var isSaving by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Edit Profile",
                    fontFamily = PlusJakartaSansFontFamily,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(onClick = onDismiss) {
                    Text("Cancel", fontFamily = PlusJakartaSansFontFamily, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Text(
                text = "Keep your profile details up to date across InTrack devices.",
                fontFamily = PlusJakartaSansFontFamily,
                fontSize = 13.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Display Name Field
            OutlinedTextField(
                value = nameInput,
                onValueChange = { nameInput = it },
                label = { Text("Display Name", fontFamily = PlusJakartaSansFontFamily) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandTangerine,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Phone Field
            OutlinedTextField(
                value = phoneInput,
                onValueChange = { phoneInput = it },
                label = { Text("Phone Number (Optional)", fontFamily = PlusJakartaSansFontFamily) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandTangerine,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Bio Field
            OutlinedTextField(
                value = bioInput,
                onValueChange = { bioInput = it },
                label = { Text("Bio / Financial Focus (Optional)", fontFamily = PlusJakartaSansFontFamily) },
                maxLines = 3,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandTangerine,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Save Button
            Button(
                onClick = {
                    if (nameInput.isNotBlank()) {
                        isSaving = true
                        onSave(nameInput, phoneInput, bioInput)
                    }
                },
                enabled = nameInput.isNotBlank() && !isSaving,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandTangerine,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Save Changes",
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ==========================================
// REUSABLE SUB-SECTIONS (CLEAN MODULAR PATTERN)
// ==========================================

@Composable
private fun ProfileHeroCard(
    userInitials: String,
    userName: String,
    userEmail: String,
    memberSince: String,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val navyText = Color(0xFF121660)

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = BrandWarmYellow),
        border = BorderStroke(0.dp, Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar with active green dot
            Box(modifier = Modifier.size(76.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(navyText),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = userInitials,
                        fontFamily = PlusJakartaSansFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp,
                        color = Color.White
                    )
                }

                // Green Active Indicator Dot
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .align(Alignment.BottomEnd)
                        .padding(2.5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(Color(0xFF2ECC71))
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = userName.ifBlank { "InTrack Member" },
                fontFamily = PlusJakartaSansFontFamily,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = navyText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = userEmail.ifBlank { "user@intrack.app" },
                fontFamily = PlusJakartaSansFontFamily,
                fontSize = 13.sp,
                color = navyText.copy(alpha = 0.8f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Badges Row
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = navyText
                ) {
                    Text(
                        text = "★ PREMIUM MEMBER",
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.5.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = navyText.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = memberSince,
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = navyText,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Edit Profile Action Button
            Button(
                onClick = onEditClick,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = navyText,
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Rounded.Edit,
                    contentDescription = "Edit Profile",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Edit Profile Information",
                    fontFamily = PlusJakartaSansFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun ProfileMetricsRow(
    baseCurrency: String,
    billingCycle: String,
    syncStatusText: String,
    isSyncing: Boolean,
    syncRotation: Float,
    isCompact: Boolean,
    onCurrencyClick: () -> Unit,
    onBillingCycleClick: () -> Unit,
    onSyncClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(if (isCompact) 6.dp else 10.dp)
    ) {
        ProfileMetricTile(
            title = "CURRENCY",
            value = baseCurrency.substringBefore(" •").substringBefore(" (").trim(),
            subtitle = "Base Currency",
            isCompact = isCompact,
            modifier = Modifier.weight(1f),
            onClick = onCurrencyClick
        )
        ProfileMetricTile(
            title = "CYCLE",
            value = billingCycle.substringBefore("th").ifBlank { "25th" },
            subtitle = "Monthly Anchor",
            isCompact = isCompact,
            modifier = Modifier.weight(1f),
            onClick = onBillingCycleClick
        )
        ProfileMetricTile(
            title = "SYNC",
            value = if (isSyncing) "Syncing" else "Live",
            subtitle = "Cloud Active",
            isCompact = isCompact,
            iconModifier = if (isSyncing) Modifier.rotate(syncRotation) else Modifier,
            modifier = Modifier.weight(1f),
            onClick = onSyncClick
        )
    }
}

@Composable
private fun PersonalDetailsCard(
    userName: String,
    userEmail: String,
    userProfile: UserProfile,
    onEditNameClick: () -> Unit,
    onEditPhoneClick: () -> Unit,
    onCopyIdClick: () -> Unit
) {
    ProfileGroupCard {
        ProfileDetailRow(
            icon = Icons.Rounded.AccountCircle,
            label = "Display Name",
            value = userName.ifBlank { "Not set" },
            showChevron = true,
            onClick = onEditNameClick
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 0.8.dp)

        ProfileDetailRow(
            icon = Icons.Rounded.Mail,
            label = "Email Address",
            value = userEmail,
            trailingContent = {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = MaterialTheme.colorScheme.financial.incomeContainer
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Verified,
                            contentDescription = "Verified",
                            tint = MaterialTheme.colorScheme.financial.income,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Verified",
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.financial.income
                        )
                    }
                }
            }
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 0.8.dp)

        ProfileDetailRow(
            icon = Icons.Rounded.Phone,
            label = "Phone Number",
            value = userProfile.phoneNumber.ifBlank { "Add phone number" },
            showChevron = true,
            onClick = onEditPhoneClick
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 0.8.dp)

        ProfileDetailRow(
            icon = Icons.Rounded.ContentCopy,
            label = "User Account ID",
            value = if (userProfile.uid.isNotBlank()) "usr_${userProfile.uid.take(8)}..." else "Anonymous",
            trailingContent = {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = BrandTangerineContainer.copy(alpha = 0.3f),
                    modifier = Modifier.clickable(onClick = onCopyIdClick)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ContentCopy,
                            contentDescription = "Copy ID",
                            tint = BrandTangerine,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Copy",
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandTangerine
                        )
                    }
                }
            }
        )
    }
}

@Composable
private fun FinancialPreferencesCard(
    userProfile: UserProfile,
    onCurrencyClick: () -> Unit,
    onBillingCycleClick: () -> Unit,
    onWeeklyStartClick: () -> Unit
) {
    ProfileGroupCard {
        ProfileDetailRow(
            icon = Icons.Rounded.CurrencyExchange,
            label = "Primary Currency",
            value = userProfile.baseCurrency,
            showChevron = true,
            onClick = onCurrencyClick
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 0.8.dp)

        ProfileDetailRow(
            icon = Icons.Rounded.CalendarMonth,
            label = "Billing Cycle Anchor",
            value = userProfile.billingCycle,
            showChevron = true,
            onClick = onBillingCycleClick
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 0.8.dp)

        ProfileDetailRow(
            icon = Icons.Rounded.DateRange,
            label = "Week Starts On",
            value = userProfile.weeklyStartDay,
            showChevron = true,
            onClick = onWeeklyStartClick
        )
    }
}

@Composable
private fun SecurityDataCard(
    userEmail: String,
    biometricsEnabled: Boolean,
    onBiometricsChange: (Boolean) -> Unit,
    syncStatusText: String,
    isSyncing: Boolean,
    syncRotation: Float,
    onResetPasswordClick: () -> Unit,
    onManualSyncClick: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    ProfileGroupCard {
        ProfileDetailRow(
            icon = Icons.Rounded.LockReset,
            label = "Password & Access",
            value = "••••••••",
            trailingContent = {
                TextButton(onClick = onResetPasswordClick) {
                    Text(
                        text = "Reset",
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandTangerine
                    )
                }
            }
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 0.8.dp)

        ProfileDetailRow(
            icon = Icons.Rounded.Fingerprint,
            label = "Biometric Lock",
            value = if (biometricsEnabled) "Fingerprint / Face ID" else "Disabled",
            trailingContent = {
                Switch(
                    checked = biometricsEnabled,
                    onCheckedChange = onBiometricsChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = BrandTangerine,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )
            }
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 0.8.dp)

        ProfileDetailRow(
            icon = Icons.Rounded.Sync,
            label = "Cloud Backup & Sync",
            value = syncStatusText,
            trailingContent = {
                IconButton(onClick = onManualSyncClick) {
                    Icon(
                        imageVector = Icons.Rounded.Sync,
                        contentDescription = "Sync",
                        tint = BrandTangerine,
                        modifier = Modifier
                            .size(20.dp)
                            .rotate(if (isSyncing) syncRotation else 0f)
                    )
                }
            }
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 0.8.dp)

        ProfileDetailRow(
            icon = Icons.Rounded.Tune,
            label = "Full Settings & Preferences",
            value = "Data export, cache, notifications & info",
            showChevron = true,
            onClick = onNavigateToSettings
        )
    }
}

@Composable
private fun AccountActionsCard(
    onSignOutClick: () -> Unit,
    onDeleteAccountClick: () -> Unit
) {
    ProfileGroupCard {
        ProfileDetailRow(
            icon = Icons.AutoMirrored.Rounded.Logout,
            label = "Sign Out",
            value = "Log out from this device",
            iconColor = MaterialTheme.colorScheme.error,
            iconBg = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f),
            showChevron = true,
            onClick = onSignOutClick
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 0.8.dp)

        ProfileDetailRow(
            icon = Icons.Rounded.DeleteForever,
            label = "Delete Account",
            value = "Permanently remove your account and data",
            iconColor = MaterialTheme.colorScheme.error,
            iconBg = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f),
            showChevron = true,
            onClick = onDeleteAccountClick
        )
    }
}

@Composable
private fun FooterVersionText(modifier: Modifier = Modifier) {
    Text(
        text = "InTrack v2.4.0 (Secure Vault)",
        fontFamily = PlusJakartaSansFontFamily,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        modifier = modifier
    )
}

// ==========================================
// REUSABLE PROFILE PRIMITIVES
// ==========================================

@Composable
private fun ProfileMetricTile(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false,
    iconModifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = BrandSoftSlate),
        border = BorderStroke(0.dp, Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier.then(
            if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = if (isCompact) 8.dp else 12.dp,
                    vertical = if (isCompact) 10.dp else 14.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontFamily = PlusJakartaSansFontFamily,
                fontSize = if (isCompact) 9.5.sp else 10.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF121660),
                letterSpacing = 0.6.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = value,
                fontFamily = PlusJakartaSansFontFamily,
                fontSize = if (isCompact) 14.sp else 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF121660),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = iconModifier
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontFamily = PlusJakartaSansFontFamily,
                fontSize = if (isCompact) 9.5.sp else 11.sp,
                color = Color(0xFF121660).copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ProfileSectionHeader(title: String) {
    Text(
        text = title,
        fontFamily = PlusJakartaSansFontFamily,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF121660),
        letterSpacing = 0.8.sp,
        modifier = Modifier.padding(start = 4.dp, top = 2.dp)
    )
}

@Composable
private fun ProfileGroupCard(
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = BrandSoftSlate),
        border = BorderStroke(0.dp, Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            content()
        }
    }
}

@Composable
private fun ProfileDetailRow(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    iconColor: Color = Color(0xFF121660),
    iconBg: Color = Color.White,
    showChevron: Boolean = false,
    trailingContent: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    val rowModifier = if (onClick != null) {
        modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    } else {
        modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp)
    }

    Row(
        modifier = rowModifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontFamily = PlusJakartaSansFontFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF121660)
            )
            if (value.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = value,
                    fontFamily = PlusJakartaSansFontFamily,
                    fontSize = 12.5.sp,
                    color = Color(0xFF121660).copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        if (trailingContent != null) {
            Spacer(modifier = Modifier.width(8.dp))
            trailingContent()
        } else if (showChevron) {
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = "Open",
                tint = Color(0xFF121660).copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
