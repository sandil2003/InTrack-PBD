package com.example.intrack.ui.theme

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Standard top application bar matching the Expenses page header style:
 * - Title: 20sp Bold, -0.5sp letter spacing, FigmaDarkText, 4dp start padding
 * - Optional Subtitle: 12sp FigmaMutedSlate
 * - Optional Navigation Icon (e.g. Back button)
 * - Profile Avatar with dropdown menu (Settings & Log Out)
 * - Additional actions slot for page-specific buttons (e.g. Date picker, delete, search)
 * - Background container color matching canvas surface (FigmaBg)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InTrackTopBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    navigationIcon: (@Composable () -> Unit)? = null,
    onNavigateToSettings: (() -> Unit)? = null,
    onSignOut: (() -> Unit)? = null,
    showProfileMenu: Boolean = onNavigateToSettings != null || onSignOut != null,
    containerColor: Color = FigmaBg,
    titleColor: Color = FigmaDarkText,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            if (subtitle != null) {
                Column(modifier = Modifier.padding(start = 4.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        ),
                        color = titleColor
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal
                        ),
                        color = FigmaMutedSlate
                    )
                }
            } else {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    ),
                    color = titleColor,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        },
        navigationIcon = {
            navigationIcon?.invoke()
        },
        actions = {
            actions()
            if (showProfileMenu) {
                InTrackProfileMenuButton(
                    onNavigateToSettings = onNavigateToSettings ?: {},
                    onSignOut = {
                        try {
                            com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                        } catch (_: Exception) {}
                        onSignOut?.invoke()
                    },
                    buttonSize = 36.dp,
                    iconTint = titleColor
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = containerColor
        ),
        modifier = modifier
    )
}
