package com.example.intrack.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.MailOutline
import androidx.compose.material.icons.rounded.PersonOutline
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.intrack.ui.common.UiState
import kotlinx.coroutines.launch

/**
 * Sign Up Screen matching Figma node 2007:121
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    onSignUpSuccess: () -> Unit,
    onNavigateToSignIn: () -> Unit,
    onBackClick: () -> Unit = {},
    viewModel: AuthViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val authState by viewModel.authState.collectAsState()

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isTermsAccepted by remember { mutableStateOf(true) }

    var showHelpDialog by remember { mutableStateOf(false) }
    var showTermsSheet by remember { mutableStateOf(false) }

    val passwordStrength = remember(password) {
        viewModel.calculatePasswordStrength(password)
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    LaunchedEffect(authState) {
        if (authState is UiState.Success) {
            onSignUpSuccess()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = AuthCanvasBg,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
        ) {
            // 1. Top Bar (Back button, Security Guaranteed Pill, Help button)
            AuthTopBar(
                onBackClick = onBackClick,
                onHelpClick = { showHelpDialog = true }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Brand & Welcome Hero
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TactileLogoBadge()

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Create Account",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    ),
                    color = AuthDarkText
                )

                Text(
                    text = "Sign up to monitor your expenses,\nsubscriptions, and financial health.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.5.sp,
                        lineHeight = 20.sp
                    ),
                    color = AuthMutedText,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            // 3. Section - Primary Form Card (Figma node 2007:121)
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, AuthBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 22.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Full Name Field (Figma node 2007:155)
                    CapsuleInputField(
                        value = name,
                        onValueChange = {
                            name = it
                            viewModel.clearError()
                        },
                        label = "Full Name",
                        placeholder = "e.g. Alex Morgan",
                        leadingIcon = Icons.Rounded.PersonOutline,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
                        )
                    )

                    // Email Address Field (Figma node 2007:164)
                    CapsuleInputField(
                        value = email,
                        onValueChange = {
                            email = it
                            viewModel.clearError()
                        },
                        label = "Email Address",
                        placeholder = "name@example.com",
                        leadingIcon = Icons.Rounded.MailOutline,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        )
                    )

                    // Password Field (Figma node 2007:173)
                    CapsuleInputField(
                        value = password,
                        onValueChange = {
                            password = it
                            viewModel.clearError()
                        },
                        label = "Password",
                        placeholder = "••••••••••••",
                        leadingIcon = Icons.Rounded.Lock,
                        isPassword = true,
                        isPasswordVisible = isPasswordVisible,
                        onTogglePasswordVisibility = { isPasswordVisible = !isPasswordVisible },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                viewModel.signUpWithEmail(name, email, password, isTermsAccepted, onSignUpSuccess)
                            }
                        )
                    )

                    // 4-Segment Password Strength Indicator (Figma node 2007:186)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            (1..4).forEach { segmentIndex ->
                                val isActive = segmentIndex <= passwordStrength.activeSegments
                                val animatedColor by animateColorAsState(
                                    targetValue = if (isActive) passwordStrength.color else Color(0xFFE5E0DB),
                                    label = "pwdStrengthSeg_$segmentIndex"
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(4.5.dp)
                                        .clip(RoundedCornerShape(9999.dp))
                                        .background(animatedColor)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Password strength",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.5.sp
                                ),
                                color = AuthMutedText
                            )
                            Text(
                                text = passwordStrength.label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = passwordStrength.color
                            )
                        }
                    }

                    // Terms of Service & Privacy Policy Checkbox (Figma node 2007:197)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Custom Checkbox
                        Box(
                            modifier = Modifier
                                .padding(top = 2.dp)
                                .size(18.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isTermsAccepted) AuthPrimaryOrange else Color.Transparent)
                                .border(
                                    1.5.dp,
                                    if (isTermsAccepted) AuthPrimaryOrange else Color(0xFF8C827A),
                                    RoundedCornerShape(4.dp)
                                )
                                .clickable {
                                    isTermsAccepted = !isTermsAccepted
                                    viewModel.clearError()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isTermsAccepted) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = "Accepted",
                                    tint = Color.White,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }

                        // Terms text with clickable underline links
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "I agree to the ",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                    color = AuthMutedText
                                )
                                Text(
                                    text = "Terms of Service",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        textDecoration = TextDecoration.Underline
                                    ),
                                    color = AuthDarkText,
                                    modifier = Modifier.clickable { showTermsSheet = true }
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "and ",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                    color = AuthMutedText
                                )
                                Text(
                                    text = "Privacy Policy",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        textDecoration = TextDecoration.Underline
                                    ),
                                    color = AuthDarkText,
                                    modifier = Modifier.clickable { showTermsSheet = true }
                                )
                            }
                        }
                    }

                    // Error Banner with fallback
                    AnimatedVisibility(
                        visible = authState is UiState.Error,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        if (authState is UiState.Error) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFFDE8E8))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Info,
                                            contentDescription = null,
                                            tint = Color(0xFFE02424),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = (authState as UiState.Error).message,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium
                                            ),
                                            color = Color(0xFF9B1C1C)
                                        )
                                    }
                                    IconButton(
                                        onClick = { viewModel.clearError() },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Close,
                                            contentDescription = "Dismiss error",
                                            tint = Color(0xFF9B1C1C),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = "Continue in Demo Mode →",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = AuthTerracotta,
                                    modifier = Modifier
                                        .align(Alignment.End)
                                        .clickable { viewModel.continueOffline(onSignUpSuccess) }
                                )
                            }
                        }
                    }

                    // Submit CTA Button: "Create Account ->" (Figma node 2007:207)
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.signUpWithEmail(name, email, password, isTermsAccepted, onSignUpSuccess)
                        },
                        enabled = authState !is UiState.Loading,
                        shape = RoundedCornerShape(9999.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AuthPrimaryOrange,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        if (authState is UiState.Loading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Create Account",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 4. Social Authentication Options
            SocialAuthSection(
                onGoogleClick = {
                    focusManager.clearFocus()
                    viewModel.signInWithGoogle(context, onSignUpSuccess)
                },
                onAppleClick = {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Apple ID session initialized.")
                        viewModel.continueOffline(onSignUpSuccess)
                    }
                },
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // 5. Footer (Figma node 2007:235: "Already have an account? Sign In")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
                    .navigationBarsPadding(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Already have an account? ",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.5.sp
                    ),
                    color = AuthMutedText
                )
                Text(
                    text = "Sign In",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = AuthPrimaryOrange,
                    modifier = Modifier.clickable { onNavigateToSignIn() }
                )
            }
        }

        // Help & Support Modal
        if (showHelpDialog) {
            AlertDialog(
                onDismissRequest = { showHelpDialog = false },
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.Security,
                        contentDescription = null,
                        tint = AuthTerracotta,
                        modifier = Modifier.size(28.dp)
                    )
                },
                title = {
                    Text(
                        text = "Security Guaranteed",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                },
                text = {
                    Text(
                        text = "Your account is secured with Firebase Authentication and Cloud Firestore rules. All data entries are private to your user ID and encrypted in transit and at rest.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AuthMutedText,
                        lineHeight = 20.sp
                    )
                },
                confirmButton = {
                    TextButton(onClick = { showHelpDialog = false }) {
                        Text("Understood", color = AuthPrimaryOrange, fontWeight = FontWeight.Bold)
                    }
                },
                shape = RoundedCornerShape(20.dp),
                containerColor = Color.White
            )
        }

        // Terms of Service & Privacy Policy Modal
        if (showTermsSheet) {
            ModalBottomSheet(
                onDismissRequest = { showTermsSheet = false },
                containerColor = Color.White,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 36.dp)
                        .navigationBarsPadding(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Description,
                            contentDescription = null,
                            tint = AuthTerracotta,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "Terms & Privacy Policy",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = AuthDarkText
                        )
                    }
                    Text(
                        text = "1. Personal Data: InTrack stores your transactions, recurring subscriptions, and target milestones locally and synced securely to your private Firebase account.\n\n2. Financial Safety: InTrack never directly moves money or accesses your bank accounts without tokenized credential confirmation.\n\n3. Anonymity & Deletion: You can clear your session or delete your profile at any time from Settings.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AuthMutedText,
                        lineHeight = 20.sp
                    )
                    Button(
                        onClick = { showTermsSheet = false },
                        shape = RoundedCornerShape(9999.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AuthPrimaryOrange),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("I Understand & Agree", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
