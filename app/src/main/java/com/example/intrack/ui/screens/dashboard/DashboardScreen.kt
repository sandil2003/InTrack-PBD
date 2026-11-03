package com.example.intrack.ui.screens.dashboard

import com.example.intrack.ui.theme.BrandTangerine
import com.example.intrack.viewmodel.DashboardViewModel
import com.example.intrack.viewmodel.DashboardData
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
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
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.intrack.data.model.ExpenseEntity
import com.example.intrack.ui.common.EmptyStateView
import com.example.intrack.ui.common.UiState
import com.example.intrack.ui.components.AddGoalSquareCard
import com.example.intrack.ui.components.CommitmentRatioBar
import com.example.intrack.ui.components.GoalSquareCard
import com.example.intrack.ui.components.ProfileSettingsBottomSheet
import com.example.intrack.ui.components.QuickExpenseBottomSheet
import com.example.intrack.ui.screens.expenses.AddExpenseBottomSheet
import com.example.intrack.ui.theme.PrimaryGreen
import com.example.intrack.ui.theme.TangerineShade2
import com.example.intrack.ui.theme.TangerineShade3
import com.example.intrack.util.CurrencyFormatter
import com.example.intrack.ui.theme.AppThemeMode
import com.example.intrack.util.DateUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    themeMode: AppThemeMode = AppThemeMode.LIGHT,
    onThemeModeChange: (AppThemeMode) -> Unit = {},
    onSignOut: () -> Unit = {},
    onNavigateToGoal: (String?) -> Unit,
    onNavigateToIncome: () -> Unit,
    onNavigateToExpenses: () -> Unit = {},
    onNavigateToSubscriptions: () -> Unit = {},
    onNavigateToAllTransactions: () -> Unit = onNavigateToExpenses,
    onNavigateToSettings: () -> Unit = {},
    onNavigateToProfile: () -> Unit = onNavigateToSettings,
    viewModel: DashboardViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val userGreetingName by viewModel.userGreetingName.collectAsState()
    val userInitials by viewModel.userInitials.collectAsState()
    val selectedCategoryFilter by viewModel.selectedCategoryFilter.collectAsState()
    val selectedAccountFilter by viewModel.selectedAccountFilter.collectAsState()
    var showProfileSheet by remember { mutableStateOf(false) }
    var showExpenseSheet by remember { mutableStateOf(false) }
    var showNotificationsSheet by remember { mutableStateOf(false) }
    var hasUnreadNotifications by remember { mutableStateOf(true) }
    var accountFilterExpanded by remember { mutableStateOf(false) }
    var categoryFilterExpanded by remember { mutableStateOf(false) }
    var showActivityFilters by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp),
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onNavigateToProfile() }
                            .padding(horizontal = 6.dp, vertical = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(PrimaryGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = userInitials,
                                style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp),
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Hi, $userGreetingName 👋",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = (-0.2).sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                },
                actions = {
                    Box(modifier = Modifier.padding(end = 12.dp)) {
                        IconButton(
                            onClick = {
                                showNotificationsSheet = true
                                hasUnreadNotifications = false
                            },
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Notifications,
                                contentDescription = "Notifications",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        if (hasUnreadNotifications) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 4.dp, end = 4.dp)
                                    .size(9.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryGreen)
                                    .align(Alignment.TopEnd)
                            )
                        }
                    }
                },
                windowInsets = TopAppBarDefaults.windowInsets,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { innerPadding ->
        when (uiState) {
            is UiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator(strokeWidth = 2.5.dp) }
            }
            is UiState.Error -> {
                val error = (uiState as UiState.Error).message
                EmptyStateView(
                    title = "Unable to Load Dashboard",
                    message = error,
                    onActionClick = {
                        viewModel.loadDashboard()
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            is UiState.Success -> {
                val data = (uiState as UiState.Success<DashboardData>).data
                val filteredRecent = data.filteredRecentTransactions
                val groupedRecent = data.groupedRecentTransactions

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(22.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(2.dp))
                        HighlightedHeroBalanceCard(
                            totalIncome = data.totalIncomeLkr,
                            totalExpense = data.totalExpenseLkr
                        )
                    }

                    if (data.shockInsightMessage.isNotBlank()) {
                        item {
                            ShockInsightBanner(
                                shockInsightMessage = data.shockInsightMessage,
                                onNavigateToExpenses = onNavigateToExpenses
                            )
                        }
                    }

                    item {
                        QuickActionButtons(
                            onLogExpenseClick = { showExpenseSheet = true },
                            onLogIncomeClick = onNavigateToIncome
                        )
                    }

                    item {
                        CommitmentRatioBar(
                            committedLkr = data.committedExpenseLkr,
                            discretionaryLkr = data.discretionaryExpenseLkr
                        )
                    }

                    item {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Savings Goals",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Row(
                                    modifier = Modifier.clickable { onNavigateToGoal(null) },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "See All",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = PrimaryGreen,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        tint = PrimaryGreen,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }

                            if (data.goals.isEmpty()) {
                                AddGoalSquareCard(
                                    onClick = { onNavigateToGoal(null) }
                                )
                            } else {
                                LazyRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(data.goals) { goal ->
                                        GoalSquareCard(
                                            goal = goal,
                                            onClick = { onNavigateToGoal(goal.id) }
                                        )
                                    }
                                    item {
                                        AddGoalSquareCard(
                                            onClick = { onNavigateToGoal(null) }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Section Header with Filter Toggle & "See All"
                    item {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 2.dp, vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "RECENT TRANSACTIONS",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.6.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val hasActiveFilters = selectedCategoryFilter != "ALL" || selectedAccountFilter != "ALL"

                                    Box(
                                        modifier = Modifier
                                            .size(30.dp)
                                            .clip(CircleShape)
                                            .background(if (showActivityFilters || hasActiveFilters) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent)
                                            .clickable { showActivityFilters = !showActivityFilters },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Tune,
                                            contentDescription = "Toggle Filters",
                                            tint = if (showActivityFilters || hasActiveFilters) BrandTangerine else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.clickable { onNavigateToAllTransactions() },
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "See All",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = PrimaryGreen,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = null,
                                            tint = PrimaryGreen,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Toggleable Filters
                    if (showActivityFilters) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // 1. Account Dropdown
                                val accountOptions = listOf(
                                    "ALL" to "All Accounts",
                                    "CASH" to "Cash Wallet",
                                    "CARD_PRIMARY" to "Card Primary",
                                    "ACCOUNT_SECONDARY" to "Bank Primary"
                                )
                                val selectedAccLabel = accountOptions.find { it.first == selectedAccountFilter }?.second ?: "All Accounts"

                                Box(modifier = Modifier.weight(1f)) {
                                    ExposedDropdownMenuBox(
                                        expanded = accountFilterExpanded,
                                        onExpandedChange = { accountFilterExpanded = !accountFilterExpanded },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(MaterialTheme.colorScheme.surface)
                                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                                                .padding(horizontal = 12.dp, vertical = 10.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = selectedAccLabel,
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        fontSize = 12.5.sp,
                                                        fontWeight = FontWeight.SemiBold
                                                    ),
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )

                                                Icon(
                                                    imageVector = Icons.Rounded.ArrowDropDown,
                                                    contentDescription = "Dropdown",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }

                                        ExposedDropdownMenu(
                                            expanded = accountFilterExpanded,
                                            onDismissRequest = { accountFilterExpanded = false },
                                            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                                        ) {
                                            accountOptions.forEach { (key, label) ->
                                                val isSelected = selectedAccountFilter == key
                                                DropdownMenuItem(
                                                    text = {
                                                        Text(
                                                            text = label,
                                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                                fontSize = 13.sp,
                                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                                            ),
                                                            color = if (isSelected) BrandTangerine else MaterialTheme.colorScheme.onSurface
                                                        )
                                                    },
                                                    onClick = {
                                                        viewModel.setAccountFilter(key)
                                                        accountFilterExpanded = false
                                                    },
                                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                // 2. Category Dropdown
                                val categoryOptions = listOf(
                                    "ALL" to "All Categories",
                                    "MISC" to "Groceries",
                                    "DINING_OUT" to "Dining",
                                    "TRANSPORT" to "Transport",
                                    "RENT" to "Housing",
                                    "SUBSCRIPTION" to "Subscriptions",
                                    "UTILITIES" to "Utilities"
                                )
                                val selectedCatLabel = categoryOptions.find { it.first == selectedCategoryFilter }?.second ?: "All Categories"

                                Box(modifier = Modifier.weight(1f)) {
                                    ExposedDropdownMenuBox(
                                        expanded = categoryFilterExpanded,
                                        onExpandedChange = { categoryFilterExpanded = !categoryFilterExpanded },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(MaterialTheme.colorScheme.surface)
                                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                                                .padding(horizontal = 12.dp, vertical = 10.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = selectedCatLabel,
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        fontSize = 12.5.sp,
                                                        fontWeight = FontWeight.SemiBold
                                                    ),
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )

                                                Icon(
                                                    imageVector = Icons.Rounded.ArrowDropDown,
                                                    contentDescription = "Dropdown",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }

                                        ExposedDropdownMenu(
                                            expanded = categoryFilterExpanded,
                                            onDismissRequest = { categoryFilterExpanded = false },
                                            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                                        ) {
                                            categoryOptions.forEach { (key, label) ->
                                                val isSelected = selectedCategoryFilter == key
                                                DropdownMenuItem(
                                                    text = {
                                                        Text(
                                                            text = label,
                                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                                fontSize = 13.sp,
                                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                                            ),
                                                            color = if (isSelected) BrandTangerine else MaterialTheme.colorScheme.onSurface
                                                        )
                                                    },
                                                    onClick = {
                                                        viewModel.setCategoryFilter(key)
                                                        categoryFilterExpanded = false
                                                    },
                                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Transactions List Feed with Weekly Headers
                    if (filteredRecent.isEmpty()) {
                        item {
                            EmptyStateView(
                                title = "No recent transactions",
                                message = "Logged expenses matching active filters will appear here.",
                                actionLabel = "Add Expense",
                                onActionClick = { showExpenseSheet = true }
                            )
                        }
                    } else {
                        groupedRecent.forEach { (weekTier, expensesInGroup) ->
                            item(key = "dashboard_section_$weekTier") {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    // Highlighted Weekly Section Header Row
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 2.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.Start,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(BrandTangerine.copy(alpha = 0.12f))
                                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = weekTier,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    letterSpacing = 0.3.sp
                                                ),
                                                color = BrandTangerine
                                            )
                                        }
                                    }

                                    // Always visible Flat Transaction Rows
                                    expensesInGroup.forEachIndexed { index, expense ->
                                        RecentExpenseRow(expense = expense)
                                        if (index < expensesInGroup.lastIndex) {
                                            HorizontalDivider(
                                                modifier = Modifier.fillMaxWidth(),
                                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                                thickness = 0.8.dp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
            else -> {}
        }
    }

    if (showExpenseSheet) {
        AddExpenseBottomSheet(
            onDismiss = { showExpenseSheet = false },
            onSubmit = { expense ->
                viewModel.logExpense(expense)
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Logged ${expense.title} (-${CurrencyFormatter.formatLkr(expense.amountLkr)})",
                        duration = SnackbarDuration.Short
                    )
                }
                showExpenseSheet = false
            }
        )
    }

    if (showNotificationsSheet) {
        val currentInsight = (uiState as? UiState.Success<DashboardData>)?.data?.shockInsightMessage ?: ""
        DashboardNotificationsBottomSheet(
            shockInsightMessage = currentInsight,
            onDismiss = { showNotificationsSheet = false },
            onNavigateToSubscriptions = {
                showNotificationsSheet = false
                onNavigateToSubscriptions()
            },
            onNavigateToGoal = {
                showNotificationsSheet = false
                onNavigateToGoal(null)
            },
            onNavigateToExpenses = {
                showNotificationsSheet = false
                onNavigateToExpenses()
            }
        )
    }

    if (showProfileSheet) {
        ProfileSettingsBottomSheet(
            themeMode = themeMode,
            onThemeModeChange = onThemeModeChange,
            onSignOut = {
                showProfileSheet = false
                onSignOut()
            },
            onNavigateToProfile = onNavigateToProfile,
            onDismiss = { showProfileSheet = false }
        )
    }
}

@Composable
fun HighlightedHeroBalanceCard(totalIncome: Double, totalExpense: Double) {
    val remainingBalance = totalIncome - totalExpense
    val isPositive = remainingBalance >= 0
    val heroColor = if (isPositive) PrimaryGreen else BrandTangerine

    val totalFlow = (totalIncome + totalExpense).coerceAtLeast(1.0)
    val inflowFraction = (totalIncome / totalFlow).toFloat().coerceIn(0f, 1f)

    val animatedInflowFraction by animateFloatAsState(
        targetValue = inflowFraction,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "animatedInflowFraction"
    )

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(0.dp, Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            // 1. Top Row: Centered "NET CASH FLOW" Header Title
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "NET CASH FLOW",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Centered Hero Balance Amount Row: "LKR XX,XXX.XX"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(
                    space = 6.dp,
                    alignment = Alignment.CenterHorizontally
                ),
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "LKR",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = heroColor,
                    modifier = Modifier.alignByBaseline()
                )
                Text(
                    text = CurrencyFormatter.formatLkr(remainingBalance).replace("LKR", "").trim(),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    modifier = Modifier.alignByBaseline()
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Multi-segment Progress Bar (Inflow vs Outflow Ratio)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(9999.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (animatedInflowFraction > 0f) {
                    Box(
                        modifier = Modifier
                            .weight(animatedInflowFraction.coerceIn(0.01f, 0.99f))
                            .fillMaxHeight()
                            .background(PrimaryGreen)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Subtle Hairline Divider
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 0.8.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 5. Inflow vs Outflow Legend Breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Inflow
                Column(horizontalAlignment = Alignment.Start) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = "Inflow",
                            tint = PrimaryGreen,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "Inflow",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = CurrencyFormatter.formatLkr(totalIncome),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Outflow
                Column(horizontalAlignment = Alignment.End) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                            contentDescription = "Outflow",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "Outflow",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = CurrencyFormatter.formatLkr(totalExpense),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

private fun formatWeeklyDashboardTimeTier(timeMillis: Long): String {
    val nowCal = java.util.Calendar.getInstance()
    val txCal = java.util.Calendar.getInstance().apply { timeInMillis = timeMillis }

    val nowYear = nowCal.get(java.util.Calendar.YEAR)
    val txYear = txCal.get(java.util.Calendar.YEAR)
    val nowWeek = nowCal.get(java.util.Calendar.WEEK_OF_YEAR)
    val txWeek = txCal.get(java.util.Calendar.WEEK_OF_YEAR)

    val weekDiff = if (nowYear == txYear) {
        nowWeek - txWeek
    } else {
        val daysDiff = ((nowCal.timeInMillis - txCal.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()
        daysDiff / 7
    }

    return when {
        weekDiff <= 0 -> "This Week"
        weekDiff == 1 -> "Last Week"
        weekDiff == 2 -> "2 Weeks Ago"
        weekDiff == 3 -> "3 Weeks Ago"
        else -> "Earlier"
    }
}

@Composable
fun QuickActionButtons(
    onLogExpenseClick: () -> Unit,
    onLogIncomeClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onLogExpenseClick,
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Log Expense", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        }

        OutlinedButton(
            onClick = onLogIncomeClick,
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            border = BorderStroke(0.dp, Color.Transparent)
        ) {
            Icon(
                imageVector = Icons.Default.Payments,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Log Income", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun RecentExpenseRow(expense: ExpenseEntity) {
    val categoryIcon = when {
        expense.title.contains("ATM", ignoreCase = true) -> Icons.Default.Payments
        expense.category.uppercase() == "DINING_OUT" -> Icons.Default.Restaurant
        expense.category.uppercase() == "TRANSPORT" -> Icons.Default.DirectionsCar
        expense.category.uppercase() == "RENT" -> Icons.Default.Home
        expense.category.uppercase() == "MISC" && expense.paymentAccount.uppercase() == "CASH" -> Icons.Default.Payments
        expense.category.uppercase() == "MISC" -> Icons.Default.ShoppingCart
        else -> Icons.Default.LocalCafe
    }
    val iconBg = if (expense.paymentAccount.uppercase() == "CASH") {
        TangerineShade3.copy(alpha = 0.2f)
    } else {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
    }
    val iconTint = if (expense.paymentAccount.uppercase() == "CASH") {
        TangerineShade2
    } else {
        MaterialTheme.colorScheme.primary
    }

    val accountLabel = when (expense.paymentAccount.uppercase()) {
        "CARD_PRIMARY" -> "Card Primary"
        "ACCOUNT_SECONDARY" -> "Secondary"
        "CASH" -> "Cash"
        else -> expense.paymentAccount.replace("_", " ")
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = categoryIcon,
                    contentDescription = expense.category,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = expense.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (expense.paymentAccount.uppercase() == "CASH") {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(TangerineShade3.copy(alpha = 0.25f))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "Cash",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                color = TangerineShade2
                            )
                        }
                    } else {
                        Text(
                            text = accountLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = DateUtils.formatDate(expense.dateSpentMillis),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = "- ${CurrencyFormatter.formatLkr(expense.amountLkr)}",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun RecentExpenseItem(expense: ExpenseEntity) {
    RecentExpenseRow(expense = expense)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardNotificationsBottomSheet(
    shockInsightMessage: String,
    onDismiss: () -> Unit,
    onNavigateToSubscriptions: () -> Unit,
    onNavigateToGoal: () -> Unit,
    onNavigateToExpenses: () -> Unit = {}
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Alerts & Insights",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(14.dp))

            if (shockInsightMessage.isNotBlank()) {
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToExpenses() },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = BrandTangerine.copy(alpha = 0.08f)
                    ),
                    border = BorderStroke(1.dp, BrandTangerine.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = BrandTangerine,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Lifestyle Leak Insight",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = BrandTangerine
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = shockInsightMessage,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
            }

            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToSubscriptions() },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = BrandTangerine, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Unused SaaS Plan Flagged",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Review recurring auto-debit on Secondary Account.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToGoal() },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Goal Target Run-Rate Alert",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Maintain monthly savings pace to achieve targets on time.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
            ) {
                Text("Dismiss", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun ShockInsightBanner(
    shockInsightMessage: String,
    onNavigateToExpenses: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onNavigateToExpenses() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(BrandTangerine.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Insight",
                    tint = BrandTangerine,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "LIFESTYLE LEAK ALERT",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = BrandTangerine
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = shockInsightMessage,
                    style = MaterialTheme.typography.bodySmall.copy(
                        lineHeight = 16.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "View Expenses",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
