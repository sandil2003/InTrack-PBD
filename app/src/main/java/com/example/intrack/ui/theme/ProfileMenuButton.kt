package com.example.intrack.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ExitToApp
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Standard user profile button that opens a dropdown menu with Settings and Log Out options.
 * Used identically across Expenses and Goals headers.
 */
@Composable
fun InTrackProfileMenuButton(
    onNavigateToSettings: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
    buttonSize: Dp = 38.dp,
    iconTint: Color = FigmaTerracotta,
    borderColor: Color = FigmaBorder
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .size(buttonSize)
                .clip(CircleShape)
                .background(Color.White)
                .border(1.dp, borderColor, CircleShape)
                .clickable { expanded = true },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PersonOutline,
                contentDescription = "User Profile",
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, borderColor)
        ) {
            DropdownMenuItem(
                text = {
                    Text(
                        text = "Settings",
                        color = FigmaDarkText,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                },
                onClick = {
                    expanded = false
                    onNavigateToSettings()
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = "Settings",
                        tint = FigmaTerracotta,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )

            HorizontalDivider(color = FigmaDivider)

            DropdownMenuItem(
                text = {
                    Text(
                        text = "Log Out",
                        color = Color(0xFFD32F2F),
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                },
                onClick = {
                    expanded = false
                    onSignOut()
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ExitToApp,
                        contentDescription = "Log Out",
                        tint = Color(0xFFD32F2F),
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        }
    }
}
