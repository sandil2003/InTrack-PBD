package com.example.intrack.ui.screens.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.intrack.R
import com.example.intrack.ui.theme.BrandTangerine

// Color Tokens matching Figma InTrack Auth
val AuthCanvasBg = Color(0xFFFAF9F5)
val AuthDarkText = Color(0xFF1B1C1A)
val AuthMutedText = Color(0xFF6B5B53)
val AuthBorder = Color(0xFFEDE8E3)
val AuthFieldBg = Color(0xFFF8F7F4)
val AuthTerracotta = Color(0xFFB52705)
val AuthSoftPeach = Color(0xFFF7EBE6)
val AuthPlaceholder = Color(0xFFA49E97)
val AuthPrimaryOrange = Color(0xFFFF5722)

/**
 * Top bar with Circular Back Button, Security Guaranteed Badge, and Help Button
 */
@Composable
fun AuthTopBar(
    onBackClick: () -> Unit,
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
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.White)
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
                .background(Color.White)
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
 * Tactile Logo Badge with Wallet Icon and Trending Up Badge matching Figma
 */
@Composable
fun TactileLogoBadge(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(68.dp),
        contentAlignment = Alignment.Center
    ) {
        // Rounded Square Peach Container (64x64)
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFFF7DDD4))
                .border(1.dp, Color(0xFFEED0C5), RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.AccountBalanceWallet,
                contentDescription = "InTrack Logo",
                tint = AuthTerracotta,
                modifier = Modifier.size(32.dp)
            )
        }

        // Overlaid Bottom-Right Circular Badge with Trending Arrow
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(22.dp)
                .clip(CircleShape)
                .background(AuthPrimaryOrange)
                .border(1.5.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.TrendingUp,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(12.dp)
            )
        }
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
                Box(modifier = Modifier.weight(1f)) {
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
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (isPasswordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                            contentDescription = if (isPasswordVisible) "Hide password" else "Show password",
                            tint = AuthMutedText,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Social Auth Section with "Or continue with" hairline and Google / Apple buttons
 */
@Composable
fun SocialAuthSection(
    onGoogleClick: () -> Unit,
    onAppleClick: () -> Unit,
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

        // Circular buttons: Google and Apple (only icons, complete circle background)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Google Button (Circular)
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, AuthBorder, CircleShape)
                    .clickable { onGoogleClick() },
                contentAlignment = Alignment.Center
            ) {
                GoogleLogo(modifier = Modifier.size(24.dp))
            }

            // Apple Button (Circular)
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, AuthBorder, CircleShape)
                    .clickable { onAppleClick() },
                contentAlignment = Alignment.Center
            ) {
                AppleLogo(modifier = Modifier.size(24.dp))
            }
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
