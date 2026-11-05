package com.example.intrack.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ExitToApp
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.TrackChanges
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.intrack.ui.navigation.Screen
import com.example.intrack.ui.theme.AppThemeMode
import com.example.intrack.ui.theme.BrandSoftSlate
import com.example.intrack.ui.theme.BrandWarmYellow
import com.example.intrack.viewmodel.AuthViewModel

data class DrawerMenuItem(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val badge: String? = null
)

@Composable
fun AppDrawerContent(
    currentRoute: String,
    themeMode: AppThemeMode = AppThemeMode.LIGHT,
    onThemeModeChange: (AppThemeMode) -> Unit = {},
    onRouteSelected: (String) -> Unit,
    onSignOut: () -> Unit,
    onCloseDrawer: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val userName by authViewModel.userDisplayName.collectAsState()
    val userEmail by authViewModel.userEmail.collectAsState()
    val userInitials by authViewModel.userInitials.collectAsState()

    val navyText = Color(0xFF121660)

    val menuItems = listOf(
        DrawerMenuItem(
            route = Screen.Dashboard.route,
            title = "Dashboard",
            icon = Icons.Rounded.Dashboard
        ),
        DrawerMenuItem(
            route = Screen.IncomeHub.route,
            title = "Income",
            icon = Icons.Rounded.AccountBalanceWallet
        ),
        DrawerMenuItem(
            route = Screen.ExpenseAnalytics.route,
            title = "Expenses",
            icon = Icons.Rounded.Analytics
        ),
        DrawerMenuItem(
            route = Screen.ExpenseAnalytics.createRoute("subscriptions"),
            title = "Subscriptions",
            icon = Icons.AutoMirrored.Rounded.ReceiptLong
        ),
        DrawerMenuItem(
            route = Screen.GoalTracker.route,
            title = "Goals",
            icon = Icons.Rounded.TrackChanges
        )
    )

    ModalDrawerSheet(
        modifier = Modifier.width(300.dp),
        drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
        drawerContainerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // User Profile Header Card
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = BrandWarmYellow),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(navyText),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userInitials.ifBlank { "U" },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = userName.ifBlank { "Account User" },
                                style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
                                fontWeight = FontWeight.Bold,
                                color = navyText
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(navyText)
                                    .padding(horizontal = 5.dp, vertical = 1.5.dp)
                            ) {
                                Text(
                                    text = "PRO",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = userEmail,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                            color = navyText.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp),
            color = navyText.copy(alpha = 0.12f)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Navigation Menu List
        Column(modifier = Modifier.padding(horizontal = 12.dp)) {
            menuItems.forEach { item ->
                val isSelected = currentRoute == item.route

                val itemBg = if (isSelected) BrandSoftSlate else Color.Transparent

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(itemBg)
                        .clickable {
                            onRouteSelected(item.route)
                            onCloseDrawer()
                        }
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        tint = navyText,
                        modifier = Modifier.size(22.dp)
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.5.sp),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = navyText,
                        modifier = Modifier.weight(1f)
                    )

                    item.badge?.let { label ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(navyText)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp),
            color = navyText.copy(alpha = 0.12f)
        )

        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "App Theme",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                fontWeight = FontWeight.Bold,
                color = navyText,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                listOf(
                    AppThemeMode.SYSTEM to "Mobile",
                    AppThemeMode.LIGHT to "Light",
                    AppThemeMode.DARK to "Dark"
                ).forEachIndexed { index, (mode, label) ->
                    SegmentedButton(
                        selected = themeMode == mode,
                        onClick = { onThemeModeChange(mode) },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = 3),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = navyText,
                            activeContentColor = Color.White,
                            inactiveContainerColor = BrandSoftSlate,
                            inactiveContentColor = navyText
                        )
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (themeMode == mode) FontWeight.Bold else FontWeight.Medium
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable {
                        onCloseDrawer()
                        onSignOut()
                    }
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ExitToApp,
                    contentDescription = "Sign Out",
                    tint = navyText,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Sign Out",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                    color = navyText,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
