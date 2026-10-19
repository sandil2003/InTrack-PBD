package com.example.intrack.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.Autorenew
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.TrackChanges
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.intrack.ui.screens.auth.LoginScreen
import com.example.intrack.ui.screens.dashboard.DashboardScreen
import com.example.intrack.ui.screens.expenses.ExpenseAnalyticsScreen
import com.example.intrack.ui.screens.goal.GoalDetailScreen
import com.example.intrack.ui.screens.goal.GoalTrackerScreen
import com.example.intrack.ui.screens.income.IncomeHubScreen
import com.example.intrack.ui.screens.subscriptions.SubscriptionAuditScreen
import com.example.intrack.ui.screens.transactions.AllTransactionsScreen

import com.example.intrack.ui.theme.AppThemeMode

private data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

@Composable
fun FinFlowNavGraph(
    navController: NavHostController = rememberNavController(),
    themeMode: AppThemeMode = AppThemeMode.LIGHT,
    onThemeModeChange: (AppThemeMode) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Login.route

    val isBottomBarVisible = currentRoute != Screen.Login.route

    val navigateToRoute: (String) -> Unit = { route ->
        if (currentRoute != route) {
            navController.navigate(route) {
                popUpTo(Screen.Dashboard.route) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    Scaffold(
        bottomBar = {
            if (isBottomBarVisible) {
                FinFlowBottomBar(
                    currentRoute = currentRoute,
                    onNavigateToRoute = navigateToRoute
                )
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { scaffoldPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Login.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = scaffoldPadding.calculateBottomPadding())
                .background(MaterialTheme.colorScheme.background),
            enterTransition = { fadeIn(animationSpec = tween(220)) },
            exitTransition = { fadeOut(animationSpec = tween(220)) },
            popEnterTransition = { fadeIn(animationSpec = tween(220)) },
            popExitTransition = { fadeOut(animationSpec = tween(220)) }
        ) {
            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    themeMode = themeMode,
                    onThemeModeChange = onThemeModeChange,
                    onSignOut = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onNavigateToGoal = { goalId ->
                        if (!goalId.isNullOrEmpty()) {
                            navController.navigate(Screen.GoalDetail.createRoute(goalId))
                        } else {
                            navigateToRoute(Screen.GoalTracker.route)
                        }
                    },
                    onNavigateToIncome = {
                        navigateToRoute(Screen.IncomeHub.route)
                    },
                    onNavigateToExpenses = {
                        navigateToRoute(Screen.ExpenseAnalytics.route)
                    },
                    onNavigateToSubscriptions = {
                        navigateToRoute(Screen.ExpenseAnalytics.createRoute("subscriptions"))
                    },
                    onNavigateToAllTransactions = {
                        navigateToRoute(Screen.AllTransactions.route)
                    }
                )
            }

            composable(Screen.AllTransactions.route) {
                AllTransactionsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.IncomeHub.route) {
                IncomeHubScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToGoal = { navigateToRoute(Screen.GoalTracker.route) }
                )
            }

            composable(
                route = Screen.ExpenseAnalytics.navRoute,
                arguments = listOf(
                    navArgument("tab") {
                        type = NavType.StringType
                        defaultValue = "daily"
                        nullable = true
                    }
                )
            ) { backStackEntry ->
                val tab = backStackEntry.arguments?.getString("tab") ?: "daily"
                ExpenseAnalyticsScreen(
                    initialTab = tab,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.SubscriptionAudit.route) {
                ExpenseAnalyticsScreen(
                    initialTab = "subscriptions",
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.GoalTracker.route) {
                GoalTrackerScreen(
                    onNavigateToGoalDetail = { goalId ->
                        navController.navigate(Screen.GoalDetail.createRoute(goalId))
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.GoalDetail.navRoute,
                arguments = listOf(
                    navArgument("goalId") {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                val goalId = backStackEntry.arguments?.getString("goalId") ?: ""
                GoalDetailScreen(
                    goalId = goalId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
private fun FinFlowBottomBar(
    currentRoute: String,
    onNavigateToRoute: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val navItems = listOf(
        BottomNavItem(
            route = Screen.Dashboard.route,
            label = "Home",
            icon = Icons.Rounded.Home
        ),
        BottomNavItem(
            route = Screen.IncomeHub.route,
            label = "Income",
            icon = Icons.Rounded.AccountBalanceWallet
        ),
        BottomNavItem(
            route = Screen.ExpenseAnalytics.route,
            label = "Expenses",
            icon = Icons.Rounded.Analytics
        ),
        BottomNavItem(
            route = Screen.GoalTracker.route,
            label = "Goals",
            icon = Icons.Rounded.TrackChanges
        )
    )

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                thickness = 0.8.dp
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                navItems.forEach { item ->
                    val isSelected = currentRoute == item.route || 
                        currentRoute.startsWith("${item.route}?") ||
                        (item.route == Screen.ExpenseAnalytics.route && (currentRoute == Screen.SubscriptionAudit.route || currentRoute.startsWith("${Screen.SubscriptionAudit.route}?")))
                    val activeBg = MaterialTheme.colorScheme.primaryContainer
                    val activeContentColor = MaterialTheme.colorScheme.primary
                    val inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) activeBg else Color.Transparent)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onNavigateToRoute(item.route) }
                            .padding(all = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = if (isSelected) activeContentColor else inactiveContentColor,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    letterSpacing = 0.sp
                                ),
                                color = if (isSelected) activeContentColor else inactiveContentColor,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }
        }
    }
}

