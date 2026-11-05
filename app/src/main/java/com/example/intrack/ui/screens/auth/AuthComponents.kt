package com.example.intrack.ui.screens.auth

import com.example.intrack.viewmodel.PasswordStrengthLevel
import com.example.intrack.viewmodel.AuthViewModel
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.intrack.R
import com.example.intrack.ui.theme.BrandTangerine

// Color Tokens matching Figma InTrack Auth
val AuthCanvasBg: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.background
val AuthDarkText: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onSurface
val AuthMutedText: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onSurfaceVariant
val AuthBorder: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.outlineVariant
val AuthFieldBg: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.surfaceContainerHighest
val AuthTerracotta: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.primary
val AuthSoftPeach: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.primaryContainer
val AuthPlaceholder: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
val AuthPrimaryOrange: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.primary

/**
 * Top bar with Circular Back Button, Security Guaranteed Badge, and Help Button
 */
@Composable
fun AuthTopBar(
    onBackClick: (() -> Unit)? = null,
    onHelpClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back Button (40x40 circular white button with subtle border)
        if (onBackClick != null) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, AuthBorder, CircleShape)
                    .clickable { onBackClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = AuthDarkText,
                    modifier = Modifier.size(18.dp)
                )
            }
        } else {
            Spacer(modifier = Modifier.size(40.dp))
        }

        // Security Guaranteed Pill
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(AuthSoftPeach)
                .border(1.dp, Color(0xFFF0DCD3), RoundedCornerShape(999.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Lock,
                contentDescription = null,
                tint = AuthTerracotta,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = "SECURITY GUARANTEED",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp
                ),
                color = AuthTerracotta
            )
        }

        // Help & Support Button (40x40 circular white button)
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, AuthBorder, CircleShape)
                .clickable { onHelpClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.HelpOutline,
                contentDescription = "Help & Support",
                tint = AuthDarkText,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * Tactile Logo Badge rendering the new InTrack brand logo matching app icon
 */
@Composable
fun TactileLogoBadge(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(76.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White)
            .border(1.dp, AuthBorder.copy(alpha = 0.6f), RoundedCornerShape(22.dp)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.new_logo),
            contentDescription = "InTrack Logo",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
    }
}

/**
 * Capsule Input Field matching Figma nodes 2007:67, 2007:78, 2007:159
 */
@Composable
fun CapsuleInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    isPasswordVisible: Boolean = false,
    onTogglePasswordVisibility: () -> Unit = {},
    topTrailingContent: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Label Row with optional top-right element (e.g. Forgot Password link)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = Color(0xFF302B27)
            )
            topTrailingContent?.invoke()
        }

        // Capsule Input Container (50dp height, fully rounded pill)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(RoundedCornerShape(9999.dp))
                .background(AuthFieldBg)
                .border(1.dp, Color(0xFFE8E3DD), RoundedCornerShape(9999.dp))
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Leading Icon
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = AuthMutedText,
                    modifier = Modifier.size(18.dp)
                )

                // Input Text
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 14.sp
                            ),
                            color = AuthPlaceholder
                        )
                    }
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = AuthDarkText
                        ),
                        singleLine = true,
                        visualTransformation = if (isPassword && !isPasswordVisible) PasswordVisualTransformation() else VisualTransformation.None,
                        keyboardOptions = keyboardOptions,
                        keyboardActions = keyboardActions,
                        cursorBrush = SolidColor(AuthPrimaryOrange)
                    )
                }

                // Trailing Icon (Visibility toggle for password)
                if (isPassword) {
                    IconButton(
                        onClick = onTogglePasswordVisibility,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isPasswordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                            contentDescription = if (isPasswordVisible) "Hide password" else "Show password",
                            tint = AuthMutedText,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Social Auth Section with "Or continue with" hairline and Google button
 */
@Composable
fun SocialAuthSection(
    onGoogleClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Centered "Or continue with" divider
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 0.8.dp,
                color = Color(0xFFE5DFD7)
            )
            Text(
                text = "Or continue with",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal
                ),
                color = AuthMutedText,
                modifier = Modifier
                    .background(AuthCanvasBg)
                    .padding(horizontal = 12.dp)
            )
        }

        // Google Sign-In Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(9999.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, AuthBorder, RoundedCornerShape(9999.dp))
                .clickable { onGoogleClick() },
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            GoogleLogo(modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Continue with Google",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = AuthDarkText
            )
        }
    }
}

/**
 * Google Logo loaded from drawable PNG
 */
@Composable
fun GoogleLogo(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(id = R.drawable.google_icon),
        contentDescription = "Google Logo",
        modifier = modifier
    )
}

/**
 * Apple Logo loaded from drawable PNG (apple_icon.png)
 */
@Composable
fun AppleLogo(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(id = R.drawable.apple_icon),
        contentDescription = "Apple Logo",
        modifier = modifier
    )
}

/**
 * Bottom Sheet displaying Google Sign-In setup details and 1-tap SHA-1 copy
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoogleSetupModal(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isCopied by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GoogleLogo(modifier = Modifier.size(28.dp))
                Column {
                    Text(
                        text = "Google Sign-In Setup",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = AuthDarkText
                    )
                    Text(
                        text = "Firebase Authentication Integration",
                        style = MaterialTheme.typography.bodySmall,
                        color = AuthMutedText
                    )
                }
            }

            Text(
                text = "To enable Google Sign-In with Firebase, register your debug SHA-1 certificate in the Firebase Console:",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.5.sp,
                    lineHeight = 19.sp
                ),
                color = AuthDarkText
            )

            // SHA-1 Card with Copy Button
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AuthSoftPeach),
                border = BorderStroke(1.dp, Color(0xFFF0DCD3)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "DEBUG SHA-1 CERTIFICATE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = AuthTerracotta
                        )

                        // Copy Button
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isCopied) Color(0xFFE6F4EA) else MaterialTheme.colorScheme.surface)
                                .border(1.dp, if (isCopied) Color(0xFF34A853) else AuthBorder, RoundedCornerShape(8.dp))
                                .clickable {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("InTrack SHA-1", AuthViewModel.DEBUG_SHA1_FINGERPRINT)
                                    clipboard.setPrimaryClip(clip)
                                    isCopied = true
                                    Toast.makeText(context, "SHA-1 copied to clipboard!", Toast.LENGTH_SHORT).show()
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = if (isCopied) Icons.Rounded.CheckCircle else Icons.Rounded.ContentCopy,
                                contentDescription = "Copy SHA-1",
                                tint = if (isCopied) Color(0xFF1E8E3E) else AuthDarkText,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = if (isCopied) "Copied!" else "Copy SHA-1",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = if (isCopied) Color(0xFF1E8E3E) else AuthDarkText
                            )
                        }
                    }

                    Text(
                        text = AuthViewModel.DEBUG_SHA1_FINGERPRINT,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.5.sp
                        ),
                        color = Color(0xFF1B1C1A)
                    )
                }
            }

            // Quick Steps list
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Steps in Firebase Console:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = AuthDarkText
                )
                Text(
                    text = "1. In Firebase Console, go to Project Settings > General > com.example.intrack.\n2. Click 'Add fingerprint' and paste the copied SHA-1.\n3. Web Client ID is already configured — Google Sign-In will authenticate immediately!",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    ),
                    color = AuthMutedText
                )
            }

            // Primary action button
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(9999.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AuthPrimaryOrange,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    text = "Done",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}
