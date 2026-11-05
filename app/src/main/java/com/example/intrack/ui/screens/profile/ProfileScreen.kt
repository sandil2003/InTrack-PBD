package com.example.intrack.ui.screens.profile

import android.widget.Toast
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
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.LockReset
import androidx.compose.material.icons.rounded.Mail
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.intrack.data.local.SecurityPreferences
import com.example.intrack.data.model.UserProfile
import com.example.intrack.ui.theme.*
import com.example.intrack.ui.theme.financial
import com.example.intrack.util.BiometricAuthManager
import com.example.intrack.util.BiometricAvailability
import com.example.intrack.viewmodel.AuthViewModel

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
    val snackbarHostState = remember { SnackbarHostState() }

    val userProfile by authViewModel.userProfile.collectAsState()
    val userName by authViewModel.userDisplayName.collectAsState()
    val userEmail by authViewModel.userEmail.collectAsState()
    val userInitials by authViewModel.userInitials.collectAsState()

    // Persistent Security & Biometric Preferences
    val securityPreferences = remember { SecurityPreferences(context) }
    val biometricAuthManager = remember { BiometricAuthManager() }
    val isBiometricEnabled by securityPreferences.isBiometricEnabledFlow.collectAsState()

    val activity = context.findFragmentActivity()
    val handleBiometricToggle: (Boolean) -> Unit = { enabled ->
        if (enabled) {
            when (biometricAuthManager.checkBiometricAvailability(context)) {
                BiometricAvailability.AVAILABLE -> {
                    if (activity != null) {
                        biometricAuthManager.showBiometricPrompt(
                            activity = activity,
                            title = "Enable Biometric Lock",
                            subtitle = "Verify fingerprint or screen lock to enable",
                            onSuccess = {
                                securityPreferences.isBiometricEnabled = true
                                Toast.makeText(context, "Biometric app lock enabled", Toast.LENGTH_SHORT).show()
                            },
                            onError = { _, err ->
                                Toast.makeText(context, "Verification cancelled: $err", Toast.LENGTH_SHORT).show()
                            }
                        )
                    } else {
                        securityPreferences.isBiometricEnabled = true
                        Toast.makeText(context, "Biometric app lock enabled", Toast.LENGTH_SHORT).show()
                    }
                }
                BiometricAvailability.NOT_ENROLLED -> {
                    Toast.makeText(context, "Please enroll fingerprint or screen lock in device Settings first.", Toast.LENGTH_LONG).show()
                }
                BiometricAvailability.NO_HARDWARE -> {
                    Toast.makeText(context, "Biometrics are not supported on this device.", Toast.LENGTH_SHORT).show()
                }
                BiometricAvailability.UNAVAILABLE -> {
                    Toast.makeText(context, "Biometric sensor is currently unavailable.", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            securityPreferences.isBiometricEnabled = false
            Toast.makeText(context, "Biometric app lock disabled", Toast.LENGTH_SHORT).show()
        }
    }

    // Interactive states
    var showEditProfileSheet by remember { mutableStateOf(false) }

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
                    // 1. Profile Hero Banner
                    ProfileHeroCard(
                        userInitials = userInitials,
                        userName = userName,
                        userEmail = userEmail,
                        memberSince = userProfile.memberSinceFormatted,
                        onEditClick = { showEditProfileSheet = true }
                    )

                    // 2. Personal Contact & Identity Details
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

                    // 3. Security & App Lock (Combined Biometrics + Password Reset)
                    ProfileSectionHeader(title = "SECURITY & APP LOCK")

                    SecurityAppLockCard(
                        userEmail = userEmail,
                        biometricsEnabled = isBiometricEnabled,
                        onBiometricsChange = handleBiometricToggle,
                        onSendResetLink = {
                            if (userEmail.isNotBlank()) {
                                authViewModel.sendPasswordReset(userEmail) {
                                    Toast.makeText(context, "Password reset link sent to $userEmail", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    )

                    // 5. Quick Link to App Settings
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onNavigateToSettings),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = BrandSoftSlate),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Settings,
                                    contentDescription = null,
                                    tint = Color(0xFF121660),
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = "Settings & App Preferences",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = Color(0xFF121660)
                                    )
                                    Text(
                                        text = "Currency, notifications, theme, export",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                        color = Color(0xFF121660).copy(alpha = 0.7f)
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.Rounded.ChevronRight,
                                contentDescription = null,
                                tint = Color(0xFF121660).copy(alpha = 0.5f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    FooterVersionText(modifier = Modifier.align(Alignment.CenterHorizontally))
                }
            }
        }
    }

    // Edit Profile Sheet
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
}

// =========================================================================
// HELPER COMPONENTS FOR PROFILE SCREEN
// =========================================================================

@Composable
private fun ProfileSectionHeader(title: String) {
    Text(
        text = title,
        fontFamily = PlusJakartaSansFontFamily,
        fontSize = 11.5.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.8.sp,
        color = Color(0xFF121660),
        modifier = Modifier.padding(start = 2.dp)
    )
}

@Composable
private fun ProfileGroupCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = BrandSoftSlate),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun ProfileHeroCard(
    userInitials: String,
    userName: String,
    userEmail: String,
    memberSince: String,
    onEditClick: () -> Unit
) {
    val navyText = Color(0xFF121660)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = BrandWarmYellow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(navyText),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = userInitials.ifBlank { "U" },
                    fontFamily = PlusJakartaSansFontFamily,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = userName.ifBlank { "Account User" },
                    fontFamily = PlusJakartaSansFontFamily,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = navyText
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(navyText)
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "PRO",
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = userEmail,
                fontFamily = PlusJakartaSansFontFamily,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = navyText.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onEditClick,
                colors = ButtonDefaults.buttonColors(containerColor = navyText),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
            ) {
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
private fun SecurityAppLockCard(
    userEmail: String,
    biometricsEnabled: Boolean,
    onBiometricsChange: (Boolean) -> Unit,
    onSendResetLink: () -> Unit
) {
    val navyText = Color(0xFF121660)

    ProfileGroupCard {
        // Row 1: Biometric Fingerprint Lock
        ProfileDetailRow(
            icon = Icons.Rounded.Fingerprint,
            label = "Biometric Fingerprint Lock",
            value = if (biometricsEnabled) "App lock active" else "Disabled",
            trailingContent = {
                Switch(
                    checked = biometricsEnabled,
                    onCheckedChange = onBiometricsChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = navyText,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )
            }
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 0.8.dp)

        // Row 2: Account Password Change
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.LockReset,
                        contentDescription = null,
                        tint = navyText,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column {
                    Text(
                        text = "Change Account Password",
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = navyText
                    )
                    Text(
                        text = "Send password reset link to $userEmail",
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 11.sp,
                        color = navyText.copy(alpha = 0.7f)
                    )
                }
            }

            Button(
                onClick = onSendResetLink,
                colors = ButtonDefaults.buttonColors(containerColor = navyText),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
            ) {
                Text(
                    text = "Send Password Reset Email",
                    fontFamily = PlusJakartaSansFontFamily,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun ProfileDetailRow(
    icon: ImageVector,
    label: String,
    value: String,
    showChevron: Boolean = false,
    trailingContent: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    val navyText = Color(0xFF121660)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f, fill = false)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = navyText,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = label,
                    fontFamily = PlusJakartaSansFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = navyText.copy(alpha = 0.65f)
                )
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = value,
                    fontFamily = PlusJakartaSansFontFamily,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = navyText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        if (trailingContent != null) {
            trailingContent()
        } else if (showChevron) {
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = "Navigate",
                tint = navyText.copy(alpha = 0.4f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun FooterVersionText(modifier: Modifier = Modifier) {
    Text(
        text = "InTrack v1.0.4 • Security Encrypted",
        fontFamily = PlusJakartaSansFontFamily,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        color = Color(0xFF121660).copy(alpha = 0.5f),
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileBottomSheet(
    currentName: String,
    currentPhone: String,
    currentBio: String,
    onDismiss: () -> Unit,
    onSave: (newName: String, newPhone: String, newBio: String) -> Unit
) {
    var nameText by remember { mutableStateOf(currentName) }
    var phoneText by remember { mutableStateOf(currentPhone) }
    var bioText by remember { mutableStateOf(currentBio) }
    val navyText = Color(0xFF121660)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Edit Profile Details",
                fontFamily = PlusJakartaSansFontFamily,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = navyText
            )

            OutlinedTextField(
                value = nameText,
                onValueChange = { nameText = it },
                label = { Text("Display Name") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = navyText,
                    focusedLabelColor = navyText
                ),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = phoneText,
                onValueChange = { phoneText = it },
                label = { Text("Phone Number") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = navyText,
                    focusedLabelColor = navyText
                ),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = bioText,
                onValueChange = { bioText = it },
                label = { Text("Short Bio / Note") },
                maxLines = 3,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = navyText,
                    focusedLabelColor = navyText
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text("Cancel", color = navyText, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { onSave(nameText, phoneText, bioText) },
                    colors = ButtonDefaults.buttonColors(containerColor = navyText),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text("Save Changes", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private fun android.content.Context.findFragmentActivity(): androidx.fragment.app.FragmentActivity? {
    var ctx = this
    while (ctx is android.content.ContextWrapper) {
        if (ctx is androidx.fragment.app.FragmentActivity) return ctx
        ctx = ctx.baseContext
    }
    return null
}
