package com.example.intrack.ui.screens.expenses

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.automirrored.rounded.ExitToApp
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.intrack.data.model.ExpenseEntity
import com.example.intrack.data.model.GoalEntity
import com.example.intrack.data.model.SubscriptionEntity
import com.example.intrack.ui.common.EmptyStateView
import com.example.intrack.ui.common.UiState
import com.example.intrack.ui.components.ProfileSettingsBottomSheet
import com.example.intrack.ui.screens.goal.GoalViewModel
import com.example.intrack.ui.screens.subscriptions.AddSubscriptionBottomSheet
import com.example.intrack.ui.screens.subscriptions.ViewSubscriptionBottomSheet
import com.example.intrack.ui.screens.subscriptions.SubscriptionViewModel
import com.example.intrack.ui.theme.*
import com.example.intrack.util.CurrencyFormatter
import com.example.intrack.util.DateUtils
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Design tokens unified with InTrack Sunset Tangerine Theme
private val FigmaCanvasBg = Color(0xFFFAF9F5)
private val FigmaDarkText = Color(0xFF1B1C1A)
private val FigmaMutedBrown = Color(0xFF6B5B53)
private val FigmaMutedSlate = Color(0xFF585F6B)
private val FigmaTerracotta = BrandTangerine
private val FigmaSoftPillActive = BrandTangerineContainer
private val FigmaActiveText = BrandTangerineOnContainer
private val FigmaCardBg = Color(0xFFF5F3EF)
private val FigmaIconCircleBg = Color(0xFFEFECE5)
private val FigmaBorder = Color(0x4DE3BEB6)
private val FigmaBorderActive = Color(0x80E3BEB6)
private val FigmaDivider = Color(0x33E3BEB6)
private val FigmaLifestyleBar = Color(0xFFC0C7D5)
private val FigmaTrackBg = Color(0xFFE4E2DE)
private val FigmaSwitchBg = Color(0xFFEAE8E4)

enum class ExpenseViewTab {
    DAILY_TRANSACTIONS,
    SUBSCRIPTIONS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseAnalyticsScreen(
    initialTab: String = "daily",
    onNavigateBack: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    themeMode: AppThemeMode = AppThemeMode.LIGHT,
    onThemeModeChange: (AppThemeMode) -> Unit = {},
    onSignOut: () -> Unit = {},
    viewModel: ExpenseViewModel = viewModel(),
    subscriptionViewModel: SubscriptionViewModel = viewModel(),
    goalViewModel: GoalViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val initialViewTab = if (initialTab.equals("subscriptions", ignoreCase = true)) {
        ExpenseViewTab.SUBSCRIPTIONS
    } else {
        ExpenseViewTab.DAILY_TRANSACTIONS
    }
    var currentTab by remember(initialTab) { mutableStateOf(initialViewTab) }

    val expensesState by viewModel.expensesState.collectAsState()
    val subscriptionsState by subscriptionViewModel.subscriptionsState.collectAsState()
    val goalsState by goalViewModel.goalsState.collectAsState()

    val activeGoal = remember(goalsState) {
        (goalsState as? UiState.Success<List<GoalEntity>>)?.data?.find { !it.isCompleted }
            ?: (goalsState as? UiState.Success<List<GoalEntity>>)?.data?.firstOrNull()
            ?: GoalEntity(title = "MacBook Pro M4 Fund", monthlyTargetRunRateLkr = 40833.33)
    }
    val monthlyGoalRunRate = if (activeGoal.monthlyTargetRunRateLkr > 0) activeGoal.monthlyTargetRunRateLkr else 40833.33

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val hapticFeedback = LocalHapticFeedback.current

    val recentMonths = remember { DateUtils.getRecentMonthsList(count = 6) }
    var selectedMonth by remember { mutableStateOf(recentMonths.firstOrNull() ?: DateUtils.getCurrentMonthName()) }
    var selectedTypeFilter by remember { mutableStateOf("ALL") }
    var selectedCategoryFilter by remember { mutableStateOf("ALL") }
    var selectedAccountFilter by remember { mutableStateOf("ALL") }
    var showAddDialog by remember { mutableStateOf(false) }
    var showChartsModal by remember { mutableStateOf(false) }
    var showMonthPicker by remember { mutableStateOf(false) }
    var showProfileSheet by remember { mutableStateOf(false) }

    // Transaction detail modal & action states
    var selectedExpense by remember { mutableStateOf<ExpenseEntity?>(null) }
    var expenseToEdit by remember { mutableStateOf<ExpenseEntity?>(null) }
    var expenseToDelete by remember { mutableStateOf<ExpenseEntity?>(null) }

    // Subscriptions tab state
    var subscriptionFilter by remember { mutableStateOf("ALL") }
    var showAddSubscriptionDialog by remember { mutableStateOf(false) }
    var activeActionSub by remember { mutableStateOf<SubscriptionEntity?>(null) }
    var subscriptionToEdit by remember { mutableStateOf<SubscriptionEntity?>(null) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = FigmaCanvasBg,
        topBar = {
            InTrackTopBar(
                title = "Expense Analytics",
                onNavigateToSettings = onNavigateToSettings,
                onSignOut = onSignOut,
                actions = {
                    // Button - Select Date / Month (Figma: 36x36 circular button)
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .clickable { showMonthPicker = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Select Date / Month",
                            modifier = Modifier.size(18.dp),
                            tint = FigmaDarkText
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))
                }
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            InTrackAddFab(
                onClick = {
                    if (currentTab == ExpenseViewTab.DAILY_TRANSACTIONS) {
                        showAddDialog = true
                    } else {
                        showAddSubscriptionDialog = true
                    }
                },
                contentDescription = if (currentTab == ExpenseViewTab.DAILY_TRANSACTIONS) "Add transaction" else "Add subscription"
            )
        },
        modifier = modifier
    ) { innerPadding ->
        if (showAddDialog) {
            AddExpenseBottomSheet(
                onDismiss = { showAddDialog = false },
                onSubmit = { newExpense ->
                    val currentUid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: "guest_user"
                    viewModel.addExpense(newExpense.copy(userId = currentUid))
                    showAddDialog = false
                }
            )
        }

        if (selectedExpense != null) {
            val exp = selectedExpense!!
            FigmaTransactionDetailSheet(
                expense = exp,
                activeGoal = activeGoal,
                onDismiss = { selectedExpense = null },
                onEdit = {
                    val toEdit = exp
                    selectedExpense = null
                    expenseToEdit = toEdit
                },
                onDelete = {
                    val toDel = exp
                    selectedExpense = null
                    expenseToDelete = toDel
                }
            )
        }

        if (expenseToEdit != null) {
            EditExpenseSheet(
                expense = expenseToEdit!!,
                onDismiss = { expenseToEdit = null },
                onUpdateExpense = { updatedExpense ->
                    viewModel.updateExpense(updatedExpense)
                    expenseToEdit = null
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Updated transaction '${updatedExpense.title}'.")
                    }
                },
                onDeleteExpense = { toDelete ->
                    val currentUid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: "guest_user"
                    viewModel.deleteExpense(currentUid, toDelete.id)
                    expenseToEdit = null
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Deleted transaction '${toDelete.title}'.")
                    }
                }
            )
        }

        if (expenseToDelete != null) {
            val exp = expenseToDelete!!
            AlertDialog(
                onDismissRequest = { expenseToDelete = null },
                shape = RoundedCornerShape(20.dp),
                containerColor = Color.White,
                title = {
                    Text(
                        text = "Delete Transaction?",
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                        fontWeight = FontWeight.Bold,
                        color = FigmaDarkText
                    )
                },
                text = {
                    Text(
                        text = "Are you sure you want to delete '${exp.title}' (${CurrencyFormatter.formatLkr(exp.amountLkr)})? This action cannot be undone.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = FigmaMutedBrown
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val currentUid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: "guest_user"
                            val targetUid = exp.userId.ifBlank { currentUid }
                            viewModel.deleteExpense(targetUid, exp.id)
                            expenseToDelete = null
                            selectedExpense = null
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Deleted transaction '${exp.title}'.")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Delete", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { expenseToDelete = null },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, FigmaBorder)
                    ) {
                        Text("Cancel", color = FigmaDarkText)
                    }
                }
            )
        }

        if (showAddSubscriptionDialog) {
            AddSubscriptionBottomSheet(
                onDismiss = { showAddSubscriptionDialog = false },
                onSubmit = { newSub ->
                    subscriptionViewModel.addSubscription(newSub)
                    showAddSubscriptionDialog = false
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Added recurring charge '${newSub.name}' (${CurrencyFormatter.formatLkr(newSub.amountLkr)}/mo)")
                    }
                }
            )
        }

        if (subscriptionToEdit != null) {
            AddSubscriptionBottomSheet(
                initialSubscription = subscriptionToEdit,
                onDismiss = { subscriptionToEdit = null },
                onSubmit = { updatedSub ->
                    subscriptionViewModel.updateSubscription(updatedSub)
                    subscriptionToEdit = null
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Updated subscription '${updatedSub.name}'")
                    }
                }
            )
        }

        if (activeActionSub != null) {
            val sub = activeActionSub!!
            ViewSubscriptionBottomSheet(
                subscription = sub,
                onDismiss = { activeActionSub = null },
                onEdit = { subToEdit ->
                    activeActionSub = null
                    subscriptionToEdit = subToEdit
                },
                onDelete = { subToDelete ->
                    val uid = subToDelete.userId.ifBlank { com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: "guest_user" }
                    subscriptionViewModel.deleteSubscription(uid, subToDelete.id)
                    activeActionSub = null
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Cancelled & deleted subscription '${subToDelete.name}'")
                    }
                }
            )
        }

        if (showMonthPicker) {
            ModalBottomSheet(
                onDismissRequest = { showMonthPicker = false },
                containerColor = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 32.dp)
                        .navigationBarsPadding(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Select Month",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = FigmaDarkText,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    recentMonths.forEach { month ->
                        val isSelected = month == selectedMonth
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) FigmaSoftPillActive else Color.Transparent)
                                .clickable {
                                    selectedMonth = month
                                    selectedTypeFilter = "ALL"
                                    selectedCategoryFilter = "ALL"
                                    selectedAccountFilter = "ALL"
                                    showMonthPicker = false
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = month,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) FigmaActiveText else FigmaDarkText
                            )
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(FigmaTerracotta)
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showProfileSheet) {
            ProfileSettingsBottomSheet(
                themeMode = themeMode,
                onThemeModeChange = onThemeModeChange,
                onSignOut = {
                    showProfileSheet = false
                    try {
                        com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                    } catch (_: Exception) {}
                    onSignOut()
                },
                onDismiss = { showProfileSheet = false }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Section - Expense views switch (Figma node 2006:55 / 2006:208)
            FigmaExpenseViewsSwitch(
                currentTab = currentTab,
                onTabSelected = { currentTab = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            )

            AnimatedContent(
                targetState = currentTab,
                transitionSpec = {
                    if (targetState == ExpenseViewTab.SUBSCRIPTIONS) {
                        (slideInHorizontally { width -> width / 4 } + fadeIn(animationSpec = tween(220)))
                            .togetherWith(slideOutHorizontally { width -> -width / 4 } + fadeOut(animationSpec = tween(180)))
                    } else {
                        (slideInHorizontally { width -> -width / 4 } + fadeIn(animationSpec = tween(220)))
                            .togetherWith(slideOutHorizontally { width -> width / 4 } + fadeOut(animationSpec = tween(180)))
                    }
                },
                label = "ExpenseViewsTransition",
                modifier = Modifier.fillMaxSize()
            ) { tab ->
                when (tab) {
                ExpenseViewTab.DAILY_TRANSACTIONS -> {
                    when (expensesState) {
                        is UiState.Loading -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = FigmaTerracotta)
                            }
                        }
                        is UiState.Error -> {
                            val errorMsg = (expensesState as UiState.Error).message
                            EmptyStateView(
                                title = "Error Loading Expenses",
                                message = errorMsg,
                                actionLabel = "Retry",
                                onActionClick = {
                                    val currentUid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: "guest_user"
                                    viewModel.loadExpenses(currentUid)
                                }
                            )
                        }
                        is UiState.Empty -> {
                            EmptyStateView(
                                title = "No Expense Records Found",
                                message = "No expense data available to display analytics.",
                                actionLabel = "Log Expense",
                                onActionClick = { showAddDialog = true }
                            )
                        }
                        is UiState.Success -> {
                            val allExpenses = (expensesState as UiState.Success<List<ExpenseEntity>>).data

                            // Filter expenses by the selected month
                            val monthExpenses = allExpenses.filter {
                                DateUtils.matchesMonth(it.dateSpentMillis, selectedMonth)
                            }

                            // Outflow expenses exclude internal transfers (e.g. ATM cash withdrawals) to prevent double counting
                            val outflowExpenses = monthExpenses.filter { !it.isInternalTransfer() }

                            val totalExpense = outflowExpenses.sumOf { it.amountLkr }
                            val committed = outflowExpenses.filter { it.isCommittedExpense() }.sumOf { it.amountLkr }
                            val discretionary = outflowExpenses.filter { it.isDiscretionaryExpense() }.sumOf { it.amountLkr }

                            val cardPrimaryTotal = monthExpenses.filter { it.paymentAccount.equals("CARD_PRIMARY", ignoreCase = true) }.sumOf { it.amountLkr }
                            val secondaryAccountTotal = monthExpenses.filter { it.paymentAccount.equals("ACCOUNT_SECONDARY", ignoreCase = true) }.sumOf { it.amountLkr }
                            val cashTotal = monthExpenses.filter { it.paymentAccount.equals("CASH", ignoreCase = true) }.sumOf { it.amountLkr }

                            // Calculate discretionary dining & coffee outflow for the Shock Insight Banner
                            val diningCoffeeDiscretionaryTotal = monthExpenses.filter { expense ->
                                val isDiningOrCoffee = expense.category.equals("DINING_OUT", ignoreCase = true) ||
                                        expense.category.equals("DINING_IN", ignoreCase = true) ||
                                        expense.title.contains("coffee", ignoreCase = true) ||
                                        expense.title.contains("cafe", ignoreCase = true) ||
                                        expense.title.contains("dining", ignoreCase = true) ||
                                        expense.title.contains("dinner", ignoreCase = true) ||
                                        expense.title.contains("lunch", ignoreCase = true) ||
                                        expense.title.contains("breakfast", ignoreCase = true) ||
                                        expense.title.contains("botanik", ignoreCase = true) ||
                                        expense.title.contains("restaurant", ignoreCase = true)
                                isDiningOrCoffee && expense.isDiscretionaryExpense()
                            }.sumOf { it.amountLkr }

                            val diningPercentOfGoal = if (monthlyGoalRunRate > 0) ((diningCoffeeDiscretionaryTotal / monthlyGoalRunRate) * 100).toInt() else 0
                            val shouldShowShockBanner = diningCoffeeDiscretionaryTotal >= 6000.0 && diningPercentOfGoal >= 15

                            val categoryBreakdownItems = remember(monthExpenses) {
                                calculateCategoryBreakdown(monthExpenses)
                            }

                            val filteredExpenses = monthExpenses.filter { expense ->
                                val matchesType = when (selectedTypeFilter) {
                                    "COMMITTED" -> expense.isCommittedExpense()
                                    "DISCRETIONARY" -> expense.isDiscretionaryExpense()
                                    "TRANSFER" -> expense.isInternalTransfer()
                                    else -> true
                                }
                                val matchesCategory = when (selectedCategoryFilter) {
                                    "ALL" -> true
                                    "ATM_WITHDRAWAL" -> expense.isInternalTransfer() || expense.category.equals("ATM_WITHDRAWAL", ignoreCase = true)
                                    else -> expense.category.equals(selectedCategoryFilter, ignoreCase = true)
                                }
                                val matchesAccount = when (selectedAccountFilter) {
                                    "ALL" -> true
                                    else -> expense.paymentAccount.equals(selectedAccountFilter, ignoreCase = true)
                                }
                                matchesType && matchesCategory && matchesAccount
                            }.sortedByDescending { it.dateSpentMillis }

                            val groupedExpenses = remember(filteredExpenses) {
                                filteredExpenses.groupBy { formatRelativeDateGroup(it.dateSpentMillis) }
                            }

                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                // Item 1: Modern Soft Hero Card (Figma node 2001:69)
                                item {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    FigmaHeroOutflowCard(
                                        monthLabel = selectedMonth,
                                        totalExpenseLkr = totalExpense,
                                        committedLkr = committed,
                                        discretionaryLkr = discretionary,
                                        onOpenCharts = { showChartsModal = true }
                                    )
                                }

                                // Item 2: Pill Filter Bar - ACCOUNTS Tier & CATEGORIES Tier (Figma node 2001:96)
                                item {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        // ACCOUNTS Tier
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = "ACCOUNTS",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    letterSpacing = 0.55.sp
                                                ),
                                                color = FigmaMutedBrown,
                                                modifier = Modifier.padding(start = 4.dp)
                                            )

                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .horizontalScroll(rememberScrollState()),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                listOf(
                                                    "ALL" to "All Accounts",
                                                    "CASH" to "Cash",
                                                    "CARD_PRIMARY" to "Card",
                                                    "ACCOUNT_SECONDARY" to "Bank Primary"
                                                ).forEach { (key, label) ->
                                                    val isSelected = selectedAccountFilter == key
                                                    FigmaFilterPill(
                                                        label = label,
                                                        isSelected = isSelected,
                                                        onClick = { selectedAccountFilter = key }
                                                    )
                                                }
                                            }
                                        }

                                        // CATEGORIES Tier
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = "CATEGORIES",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    letterSpacing = 0.55.sp
                                                ),
                                                color = FigmaMutedBrown,
                                                modifier = Modifier.padding(start = 4.dp)
                                            )

                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .horizontalScroll(rememberScrollState()),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                val isAllCategories = selectedCategoryFilter == "ALL" && selectedTypeFilter == "ALL"
                                                FigmaFilterPill(
                                                    label = "All Categories",
                                                    isSelected = isAllCategories,
                                                    onClick = {
                                                        selectedCategoryFilter = "ALL"
                                                        selectedTypeFilter = "ALL"
                                                    }
                                                )

                                                FigmaFilterPill(
                                                    label = "Essentials",
                                                    isSelected = selectedTypeFilter == "COMMITTED",
                                                    onClick = {
                                                        selectedTypeFilter = if (selectedTypeFilter == "COMMITTED") "ALL" else "COMMITTED"
                                                        selectedCategoryFilter = "ALL"
                                                    }
                                                )

                                                FigmaFilterPill(
                                                    label = "Lifestyle",
                                                    isSelected = selectedTypeFilter == "DISCRETIONARY",
                                                    onClick = {
                                                        selectedTypeFilter = if (selectedTypeFilter == "DISCRETIONARY") "ALL" else "DISCRETIONARY"
                                                        selectedCategoryFilter = "ALL"
                                                    }
                                                )

                                                listOf(
                                                    "MISC" to "Groceries",
                                                    "DINING_OUT" to "Dining",
                                                    "TRANSPORT" to "Transport",
                                                    "RENT" to "Housing",
                                                    "SUBSCRIPTION" to "Subscriptions",
                                                    "UTILITIES" to "Utilities",
                                                    "ATM_WITHDRAWAL" to "ATM / Transfers"
                                                ).forEach { (key, label) ->
                                                    val isSelected = selectedCategoryFilter == key
                                                    FigmaFilterPill(
                                                        label = label,
                                                        isSelected = isSelected,
                                                        onClick = {
                                                            selectedCategoryFilter = if (isSelected) "ALL" else key
                                                            selectedTypeFilter = "ALL"
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                // Item 2b: Dynamic Shock Insight Banner when dining/coffee exceeds threshold
                                if (shouldShowShockBanner) {
                                    item(key = "shock_insight_banner") {
                                        FigmaShockInsightBanner(
                                            diningCoffeeTotal = diningCoffeeDiscretionaryTotal,
                                            goalRunRate = monthlyGoalRunRate,
                                            goalTitle = activeGoal.title
                                        )
                                    }
                                }

                                // Item 3: RECENT ACTIVITY Section Header
                                item {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 2.dp, vertical = 2.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "RECENT ACTIVITY",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                letterSpacing = 0.6.sp
                                            ),
                                            color = FigmaMutedBrown
                                        )
                                        Text(
                                            text = "${filteredExpenses.size} transactions",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold
                                            ),
                                            color = FigmaMutedSlate
                                        )
                                    }
                                }

                                // Item 4: Transactions Feed
                                if (filteredExpenses.isEmpty()) {
                                    item {
                                        EmptyStateView(
                                            title = "No matching expenses",
                                            message = "No transactions found with current filters."
                                        )
                                    }
                                } else {
                                    groupedExpenses.forEach { (dateGroup, expensesInGroup) ->
                                        item(key = "header_$dateGroup") {
                                            Text(
                                                text = dateGroup,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                ),
                                                color = FigmaMutedSlate,
                                                modifier = Modifier.padding(start = 4.dp, top = 4.dp, bottom = 2.dp)
                                            )
                                        }
                                        items(expensesInGroup, key = { "${it.id}_${it.dateSpentMillis}" }) { expense ->
                                            FigmaTransactionCard(
                                                expense = expense,
                                                onClick = {
                                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    selectedExpense = expense
                                                }
                                            )
                                        }
                                    }
                                }

                                item {
                                    Spacer(modifier = Modifier.height(72.dp))
                                }
                            }

                            if (showChartsModal) {
                                ExpenseChartsModal(
                                    monthLabel = selectedMonth,
                                    totalExpenseLkr = totalExpense,
                                    committedLkr = committed,
                                    discretionaryLkr = discretionary,
                                    cardPrimaryTotal = cardPrimaryTotal,
                                    secondaryAccountTotal = secondaryAccountTotal,
                                    cashTotal = cashTotal,
                                    categoryItems = categoryBreakdownItems,
                                    onDismiss = { showChartsModal = false },
                                    onSelectCategory = { catKey ->
                                        selectedCategoryFilter = if (selectedCategoryFilter.equals(catKey, ignoreCase = true)) "ALL" else catKey
                                    }
                                )
                            }
                        }
                        else -> {}
                    }
                }
                ExpenseViewTab.SUBSCRIPTIONS -> {
                    when (subscriptionsState) {
                        is UiState.Loading -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = FigmaTerracotta)
                            }
                        }
                        is UiState.Error -> {
                            val errorMsg = (subscriptionsState as UiState.Error).message
                            EmptyStateView(
                                title = "Error Loading Subscriptions",
                                message = errorMsg,
                                actionLabel = "Retry",
                                onActionClick = {
                                    val currentUid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: "guest_user"
                                    subscriptionViewModel.loadSubscriptions(currentUid)
                                }
                            )
                        }
                        is UiState.Empty -> {
                            EmptyStateView(
                                title = "No Subscriptions Found",
                                message = "No fixed obligations or auto-debits recorded.",
                                actionLabel = "Add Subscription",
                                onActionClick = { showAddSubscriptionDialog = true }
                            )
                        }
                        is UiState.Success -> {
                            val subscriptions = (subscriptionsState as UiState.Success<List<SubscriptionEntity>>).data
                            val activeSubs = subscriptions.filter { it.status != "CANCELLED" }
                            val totalActive = activeSubs.sumOf { it.amountLkr }
                            val secondaryTotal = activeSubs.filter { it.paymentAccount.equals("ACCOUNT_SECONDARY", ignoreCase = true) }.sumOf { it.amountLkr }
                            val primaryCardTotal = activeSubs.filter { it.paymentAccount.equals("CARD_PRIMARY", ignoreCase = true) }.sumOf { it.amountLkr }
                            val cashTotal = activeSubs.filter { it.paymentAccount.equals("CASH", ignoreCase = true) }.sumOf { it.amountLkr }

                            val allCount = subscriptions.size
                            val dueWeekCount = remember(subscriptions) {
                                subscriptions.count {
                                    val diff = it.nextBillingMillis - System.currentTimeMillis()
                                    diff in 0..(7L * 24 * 3600 * 1000)
                                }
                            }
                            val autoDebitCount = remember(subscriptions) {
                                subscriptions.count {
                                    it.paymentAccount.equals("CARD_PRIMARY", ignoreCase = true) || it.paymentAccount.equals("ACCOUNT_SECONDARY", ignoreCase = true)
                                }
                            }

                            val filteredSubs = when (subscriptionFilter) {
                                "DUE_WEEK" -> subscriptions.filter {
                                    val diff = it.nextBillingMillis - System.currentTimeMillis()
                                    diff in 0..(7L * 24 * 3600 * 1000)
                                }
                                "AUTO_DEBIT" -> subscriptions.filter {
                                    it.paymentAccount.equals("CARD_PRIMARY", ignoreCase = true) || it.paymentAccount.equals("ACCOUNT_SECONDARY", ignoreCase = true)
                                }
                                else -> subscriptions
                            }

                            val nextSub = activeSubs.minByOrNull { it.nextBillingMillis }
                            val nextInDays = if (nextSub != null) {
                                val diff = nextSub.nextBillingMillis - System.currentTimeMillis()
                                val days = (diff / (24 * 3600 * 1000)).coerceAtLeast(1)
                                "Next in $days days"
                            } else {
                                "Next in 4 days"
                            }

                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                // 1. Hero Section: Monthly Commitments Card
                                item {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    FigmaMonthlyCommitmentsCard(
                                        totalRecurringLkr = totalActive,
                                        secondaryAccountTotal = secondaryTotal,
                                        primaryCardTotal = primaryCardTotal,
                                        cashTotal = cashTotal
                                    )
                                }

                                // 2. Section - Quick Action / Filter Pills (Figma node 2006:96)
                                item {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        FigmaFilterPill(
                                            label = "All Fixed ($allCount)",
                                            isSelected = subscriptionFilter == "ALL",
                                            onClick = {
                                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                                subscriptionFilter = "ALL"
                                            }
                                        )
                                        FigmaFilterPill(
                                            label = "Due this week ($dueWeekCount)",
                                            isSelected = subscriptionFilter == "DUE_WEEK",
                                            onClick = {
                                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                                subscriptionFilter = if (subscriptionFilter == "DUE_WEEK") "ALL" else "DUE_WEEK"
                                            }
                                        )
                                        FigmaFilterPill(
                                            label = "Auto-debit only ($autoDebitCount)",
                                            isSelected = subscriptionFilter == "AUTO_DEBIT",
                                            onClick = {
                                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                                subscriptionFilter = if (subscriptionFilter == "AUTO_DEBIT") "ALL" else "AUTO_DEBIT"
                                            }
                                        )
                                    }
                                }

                                // 3. Recurring Charges Header
                                item {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 2.dp, vertical = 2.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f, fill = false)
                                        ) {
                                            Text(
                                                text = "Recurring Charges",
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                color = FigmaDarkText,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "(${filteredSubs.size})",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Medium
                                                ),
                                                color = FigmaMutedSlate
                                            )
                                        }
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = nextInDays,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                ),
                                                color = FigmaTerracotta,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                        }
                                    }
                                }

                                // 4. Recurring items listing (Figma node 2006:119)
                                if (filteredSubs.isEmpty()) {
                                    item {
                                        EmptyStateView(
                                            title = "No items in this filter",
                                            message = "All subscriptions in this category are up to date."
                                        )
                                    }
                                } else {
                                    items(filteredSubs, key = { it.id.ifEmpty { it.name } }) { sub ->
                                        FigmaSubscriptionItemCard(
                                            subscription = sub,
                                            onClick = {
                                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                                activeActionSub = sub
                                            },
                                            modifier = Modifier.animateItem()
                                        )
                                    }
                                }

                                // 5. Calendar Schedule Banner / Mini Reminder Insight (Figma node 2006:168)
                                item {
                                    FigmaAutoDebitAlertBanner(
                                        upcomingAmount = nextSub?.amountLkr ?: 4500.0,
                                        upcomingName = nextSub?.name ?: "Upcoming commitments",
                                        onManageClick = {
                                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                            subscriptionFilter = if (subscriptionFilter == "AUTO_DEBIT") "ALL" else "AUTO_DEBIT"
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar(
                                                    if (subscriptionFilter == "AUTO_DEBIT") "Showing auto-debit obligations" else "Showing all fixed commitments"
                                                )
                                            }
                                        }
                                    )
                                }

                                item {
                                    Spacer(modifier = Modifier.height(96.dp))
                                }
                            }
                        }
                        else -> {}
                    }
                }
            }
        }
    }
}
}

/**
 * Modern Hero Outflow Card matching the Monthly Commitments Hero Card theme:
 * - Pure white container with 24dp rounded corners, subtle border, and 0.5dp elevation
 * - Top Row: Month total outflow label on left, chart icon button on right
 * - Hero Outflow Row: LKR in Terracotta, Amount in bold dark text (30sp)
 * - Dual-tone ratio bar (Deep Terracotta Essentials + Soft Peach Lifestyle)
 * - Hairline divider
 * - 2-Column breakdown: (1) Essentials with terracotta dot + LKR Amount, (2) Lifestyle with soft peach dot + LKR Amount
 */
@Composable
fun FigmaHeroOutflowCard(
    monthLabel: String,
    totalExpenseLkr: Double,
    committedLkr: Double,
    discretionaryLkr: Double,
    onOpenCharts: () -> Unit,
    modifier: Modifier = Modifier
) {
    val total = if (totalExpenseLkr > 0) totalExpenseLkr else 1.0
    val committedRatio = (committedLkr / total).toFloat().coerceIn(0f, 1f)
    val discretionaryRatio = (discretionaryLkr / total).toFloat().coerceIn(0f, 1f)

    val animatedCommittedRatio by animateFloatAsState(
        targetValue = committedRatio,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "committedRatio"
    )
    val animatedDiscretionaryRatio by animateFloatAsState(
        targetValue = discretionaryRatio,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "discretionaryRatio"
    )

    val heroTerracotta = Color(0xFFB52705)
    val heroPeachBar = Color(0xFFE3BEB6)
    val darkTextColor = Color(0xFF1B1C1A)
    val mutedLabelColor = Color(0xFF6B5E57)
    val hairlineColor = Color(0xFFF2ECE6)

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFEDE8E3)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            // 1. Top Row: Title + "View Charts" Capsule Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${monthLabel.uppercase()} TOTAL OUTFLOW",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp
                    ),
                    color = mutedLabelColor
                )

                // View charts button (symbol only)
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF7EBE6))
                        .clickable { onOpenCharts() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = "View charts",
                        tint = heroTerracotta,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Hero Outflow Amount Row: "LKR XX,XXX.XX" (Centered)
            InTrackHeroAmount(
                amountLkr = totalExpenseLkr,
                prefixColor = heroTerracotta,
                textColor = darkTextColor
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Multi-segment progress bar (8dp height capsule pill matching hero section image)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(9999.dp))
                    .background(if (totalExpenseLkr > 0) heroPeachBar else Color(0xFFF2ECE6))
            ) {
                if (totalExpenseLkr > 0 && animatedCommittedRatio > 0f) {
                    Box(
                        modifier = Modifier
                            .weight(animatedCommittedRatio.coerceIn(0.01f, 0.99f))
                            .fillMaxHeight()
                            .background(heroTerracotta)
                    )
                }
                if (totalExpenseLkr > 0 && animatedDiscretionaryRatio > 0f) {
                    Box(
                        modifier = Modifier
                            .weight(animatedDiscretionaryRatio.coerceAtLeast(0.01f))
                            .fillMaxHeight()
                            .background(heroPeachBar)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Subtle Hairline Divider
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 0.8.dp,
                color = hairlineColor
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 5. 2-Column Breakdown Legend (Centered and Aligned)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Column 1: Essentials
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(heroTerracotta)
                        )
                        Text(
                            text = "Essentials",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = mutedLabelColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = CurrencyFormatter.formatLkr(committedLkr),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.3).sp
                        ),
                        color = darkTextColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Column 2: Lifestyle
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(heroPeachBar)
                        )
                        Text(
                            text = "Lifestyle",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = mutedLabelColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = CurrencyFormatter.formatLkr(discretionaryLkr),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.3).sp
                        ),
                        color = darkTextColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}



/**
 * Transaction Card matching Figma node 2001:136 (Background+Border+Shadow)
 */
@Composable
fun FigmaTransactionCard(
    expense: ExpenseEntity,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isInternalTransfer = expense.isInternalTransfer()
    val isCommitted = expense.isCommittedExpense()

    val accountLabel = when {
        isInternalTransfer -> "➔ Cash Wallet"
        expense.paymentAccount.uppercase() == "CARD_PRIMARY" -> "Card Primary"
        expense.paymentAccount.uppercase() == "ACCOUNT_SECONDARY" -> "Bank Primary"
        expense.paymentAccount.uppercase() == "CASH" -> "Cash"
        else -> expense.paymentAccount.replace("_", " ")
    }

    val displayCategory = when {
        isInternalTransfer -> "Internal Transfer"
        expense.category.uppercase() == "DINING_OUT" -> "Dining"
        expense.category.uppercase() == "TRANSPORT" -> "Transport"
        expense.category.uppercase() == "RENT" -> "Housing"
        expense.category.uppercase() == "SUBSCRIPTION" -> "Subscription"
        expense.category.uppercase() == "UTILITIES" -> "Utilities"
        expense.category.uppercase() == "MISC" -> "Groceries"
        else -> expense.category
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, if (isInternalTransfer) Color(0xFFBFDBFE) else FigmaBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Title & Account/Time Subtitle
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = expense.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = FigmaDarkText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Mini account badge (Figma node 2001:145)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isInternalTransfer) Color(0xFFEFF6FF) else FigmaIconCircleBg)
                            .padding(horizontal = 6.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = accountLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = if (isInternalTransfer) Color(0xFF1E40AF) else FigmaMutedBrown
                        )
                    }

                    // Time or short date
                    Text(
                        text = formatTimeOrDate(expense.dateSpentMillis),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = FigmaMutedSlate
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Right: Amount + Category (Figma node 2001:149)
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                if (isInternalTransfer) {
                    Text(
                        text = "⇄ LKR ${CurrencyFormatter.formatLkr(expense.amountLkr).replace("LKR ", "")}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color(0xFF2563EB),
                        maxLines = 1,
                        softWrap = false
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Transfer to Cash",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color(0xFF2563EB),
                        maxLines = 1,
                        softWrap = false
                    )
                } else {
                    Text(
                        text = "- LKR ${CurrencyFormatter.formatLkr(expense.amountLkr).replace("LKR ", "")}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = FigmaDarkText,
                        maxLines = 1,
                        softWrap = false
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isCommitted) "Essentials" else displayCategory,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = if (isCommitted) FontWeight.SemiBold else FontWeight.Medium
                        ),
                        color = if (isCommitted) FigmaTerracotta else FigmaMutedSlate,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}

private fun formatRelativeDateGroup(timeMillis: Long): String {
    val cal = java.util.Calendar.getInstance()
    val todayYear = cal.get(java.util.Calendar.YEAR)
    val todayDay = cal.get(java.util.Calendar.DAY_OF_YEAR)

    val expCal = java.util.Calendar.getInstance().apply { timeInMillis = timeMillis }
    val expYear = expCal.get(java.util.Calendar.YEAR)
    val expDay = expCal.get(java.util.Calendar.DAY_OF_YEAR)

    val sdf = SimpleDateFormat("MMM d", Locale.US)
    val formattedDate = sdf.format(Date(timeMillis))

    return when {
        todayYear == expYear && todayDay == expDay -> "Today, $formattedDate"
        todayYear == expYear && todayDay - expDay == 1 -> "Yesterday, $formattedDate"
        else -> formattedDate
    }
}

private fun formatTimeOrDate(timeMillis: Long): String {
    val cal = java.util.Calendar.getInstance()
    val todayYear = cal.get(java.util.Calendar.YEAR)
    val todayDay = cal.get(java.util.Calendar.DAY_OF_YEAR)

    val expCal = java.util.Calendar.getInstance().apply { timeInMillis = timeMillis }
    val expYear = expCal.get(java.util.Calendar.YEAR)
    val expDay = expCal.get(java.util.Calendar.DAY_OF_YEAR)

    return if (todayYear == expYear && todayDay == expDay) {
        SimpleDateFormat("hh:mm a", Locale.US).format(Date(timeMillis))
    } else {
        SimpleDateFormat("MMM dd", Locale.US).format(Date(timeMillis))
    }
}

private fun formatFullDateTime(timeMillis: Long): String {
    return SimpleDateFormat("EEEE, MMMM d, yyyy • hh:mm a", Locale.US).format(Date(timeMillis))
}

@Composable
fun AddExpenseDialog(
    onDismiss: () -> Unit,
    onSubmit: (ExpenseEntity) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("DINING_OUT") }
    var selectedCommitment by remember { mutableStateOf("DISCRETIONARY") }
    var selectedAccount by remember { mutableStateOf("CARD_PRIMARY") }

    val categories = listOf(
        "DINING_OUT" to "Dining",
        "TRANSPORT" to "Transport",
        "RENT" to "Rent",
        "UTILITIES" to "Utilities",
        "SUBSCRIPTION" to "Sub",
        "MISC" to "Misc"
    )
    val accounts = listOf(
        "CARD_PRIMARY" to "Primary Card",
        "ACCOUNT_SECONDARY" to "Bank Account",
        "CASH" to "Cash"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Log Expense",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "Commitment Type",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = selectedCommitment == "DISCRETIONARY",
                        onClick = { selectedCommitment = "DISCRETIONARY" },
                        label = { Text("Discretionary", style = MaterialTheme.typography.labelSmall) },
                        shape = RoundedCornerShape(8.dp)
                    )
                    FilterChip(
                        selected = selectedCommitment == "COMMITTED",
                        onClick = { selectedCommitment = "COMMITTED" },
                        label = { Text("Committed", style = MaterialTheme.typography.labelSmall) },
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { (key, label) ->
                        FilterChip(
                            selected = selectedCategory == key,
                            onClick = { selectedCategory = key },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    placeholder = { Text("e.g. Grocery Store") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount (LKR)") },
                    placeholder = { Text("2500.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Payment Source",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    accounts.forEach { (key, label) ->
                        FilterChip(
                            selected = selectedAccount == key,
                            onClick = { selectedAccount = key },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    if (title.isNotEmpty() && amt > 0) {
                        onSubmit(
                            ExpenseEntity(
                                title = title,
                                amountLkr = amt,
                                category = selectedCategory,
                                commitmentType = selectedCommitment,
                                paymentAccount = selectedAccount,
                                dateSpentMillis = System.currentTimeMillis()
                            )
                        )
                    }
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Save", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun EditExpenseDialog(
    expense: ExpenseEntity,
    onDismiss: () -> Unit,
    onSubmit: (ExpenseEntity) -> Unit
) {
    var title by remember(expense) { mutableStateOf(expense.title) }
    var amountText by remember(expense) { mutableStateOf(if (expense.amountLkr > 0) expense.amountLkr.toString() else "") }
    var selectedCategory by remember(expense) { mutableStateOf(expense.category) }
    var selectedCommitment by remember(expense) { mutableStateOf(expense.commitmentType) }
    var selectedAccount by remember(expense) { mutableStateOf(expense.paymentAccount) }
    var notesText by remember(expense) { mutableStateOf(expense.notes ?: "") }

    val categories = listOf(
        "DINING_OUT" to "Dining",
        "TRANSPORT" to "Transport",
        "RENT" to "Rent",
        "UTILITIES" to "Utilities",
        "SUBSCRIPTION" to "Sub",
        "MISC" to "Misc"
    )
    val accounts = listOf(
        "CARD_PRIMARY" to "Primary Card",
        "ACCOUNT_SECONDARY" to "Bank Account",
        "CASH" to "Cash"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Edit Transaction",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "Commitment Type",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = selectedCommitment == "DISCRETIONARY",
                        onClick = { selectedCommitment = "DISCRETIONARY" },
                        label = { Text("Discretionary", style = MaterialTheme.typography.labelSmall) },
                        shape = RoundedCornerShape(8.dp)
                    )
                    FilterChip(
                        selected = selectedCommitment == "COMMITTED",
                        onClick = { selectedCommitment = "COMMITTED" },
                        label = { Text("Committed", style = MaterialTheme.typography.labelSmall) },
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { (key, label) ->
                        FilterChip(
                            selected = selectedCategory == key,
                            onClick = { selectedCategory = key },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    placeholder = { Text("e.g. Grocery Store") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount (LKR)") },
                    placeholder = { Text("2500.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Notes (Optional)") },
                    placeholder = { Text("e.g. Dinner with team") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Payment Source",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    accounts.forEach { (key, label) ->
                        FilterChip(
                            selected = selectedAccount == key,
                            onClick = { selectedAccount = key },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    if (title.isNotBlank() && amt > 0) {
                        onSubmit(
                            expense.copy(
                                title = title.trim(),
                                amountLkr = amt,
                                category = selectedCategory,
                                commitmentType = selectedCommitment,
                                paymentAccount = selectedAccount,
                                notes = notesText.trim().ifEmpty { null }
                            )
                        )
                    }
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Save Changes", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

/**
 * Expense views switch matching Figma node 2006:55 / 2006:208 with fluid animated indicator
 * and tactile response. Delegates to InTrackSegmentedSwitch.
 */
@Composable
fun FigmaExpenseViewsSwitch(
    currentTab: ExpenseViewTab,
    onTabSelected: (ExpenseViewTab) -> Unit,
    modifier: Modifier = Modifier
) {
    InTrackSegmentedSwitch(
        leftLabel = "Daily Transactions",
        rightLabel = "Subscriptions",
        isLeftSelected = currentTab == ExpenseViewTab.DAILY_TRANSACTIONS,
        onLeftSelect = { onTabSelected(ExpenseViewTab.DAILY_TRANSACTIONS) },
        onRightSelect = { onTabSelected(ExpenseViewTab.SUBSCRIPTIONS) },
        modifier = modifier
    )
}

/**
 * Monthly Commitments Hero Card matching the user's design exactly:
 * - Pure white container with 24dp rounded corners and subtle border
 * - Top Row: "MONTHLY COMMITMENTS" on left, [Calendar View] rounded capsule button on right
 * - Hero Outflow Row: LKR in Terracotta, Amount in bold dark text
 * - Dual-tone ratio bar (Deep Terracotta + Soft Peach)
 * - Hairline divider
 * - 2-Column breakdown: (1) Secondary Account with terracotta dot + LKR Amount, (2) Card Primary with soft peach dot + LKR Amount
 */
@Composable
fun FigmaMonthlyCommitmentsCard(
    totalRecurringLkr: Double,
    secondaryAccountTotal: Double,
    primaryCardTotal: Double,
    cashTotal: Double,
    onCalendarClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val total = if (totalRecurringLkr > 0) totalRecurringLkr else 1.0
    val secondaryRatio = (secondaryAccountTotal / total).toFloat().coerceIn(0f, 1f)
    val primaryRatio = (primaryCardTotal / total).toFloat().coerceIn(0f, 1f)

    val cashRatio = (cashTotal / total).toFloat().coerceIn(0f, 1f)

    val animatedSecondaryRatio by animateFloatAsState(
        targetValue = secondaryRatio,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "secRatio"
    )
    val animatedPrimaryRatio by animateFloatAsState(
        targetValue = primaryRatio,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "primRatio"
    )
    val animatedCashRatio by animateFloatAsState(
        targetValue = cashRatio,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "cashRatio"
    )

    val heroTerracotta = Color(0xFFB52705)
    val heroPeachBar = Color(0xFFE3BEB6)
    val heroCashBar = Color(0xFFD79583)
    val darkTextColor = Color(0xFF1B1C1A)
    val mutedLabelColor = Color(0xFF6B5E57)
    val hairlineColor = Color(0xFFF2ECE6)

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFEDE8E3)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            // 1. Top Row: Title
            Text(
                text = "MONTHLY COMMITMENTS",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp
                ),
                color = mutedLabelColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Hero Outflow Amount Row: "LKR 40,700.00" (Centered)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "LKR",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = heroTerracotta,
                    modifier = Modifier.alignByBaseline()
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = CurrencyFormatter.formatLkr(totalRecurringLkr).replace("LKR ", ""),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = darkTextColor,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.alignByBaseline()
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Multi-account progress bar (8dp height capsule pill matching hero section)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(9999.dp))
                    .background(if (totalRecurringLkr > 0) heroPeachBar else Color(0xFFF2ECE6))
            ) {
                if (totalRecurringLkr > 0 && animatedSecondaryRatio > 0f) {
                    Box(
                        modifier = Modifier
                            .weight(animatedSecondaryRatio.coerceIn(0.01f, 0.99f))
                            .fillMaxHeight()
                            .background(heroTerracotta)
                    )
                }
                if (totalRecurringLkr > 0 && animatedPrimaryRatio > 0f) {
                    Box(
                        modifier = Modifier
                            .weight(animatedPrimaryRatio.coerceAtLeast(0.01f))
                            .fillMaxHeight()
                            .background(heroPeachBar)
                    )
                }
                if (totalRecurringLkr > 0 && animatedCashRatio > 0f) {
                    Box(
                        modifier = Modifier
                            .weight(animatedCashRatio.coerceAtLeast(0.01f))
                            .fillMaxHeight()
                            .background(heroCashBar)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Subtle Hairline Divider
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 0.8.dp,
                color = hairlineColor
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 5. Breakdown Legend (Centered and Aligned)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Column 1: Secondary
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(heroTerracotta)
                        )
                        Text(
                            text = "Secondary",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = mutedLabelColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = CurrencyFormatter.formatLkr(secondaryAccountTotal),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = darkTextColor,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }

                // Column 2: Card
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(heroPeachBar)
                        )
                        Text(
                            text = "Card",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = mutedLabelColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = CurrencyFormatter.formatLkr(primaryCardTotal),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = darkTextColor,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }

                if (cashRatio > 0f) {
                    // Column 3: Cash
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(heroCashBar)
                            )
                            Text(
                                text = "Cash",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = mutedLabelColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = CurrencyFormatter.formatLkr(cashTotal),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = darkTextColor,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

/**
 * Subscription Item Card matching Figma node 2006:119 with responsive press physics,
 * status chip indicator, countdown alert, and chevron touch target.
 */
@Composable
fun FigmaSubscriptionItemCard(
    subscription: SubscriptionEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "sub_card_press_scale"
    )

    val isCancelled = subscription.status.equals("CANCELLED", ignoreCase = true)

    Card(
        onClick = onClick,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCancelled) Color(0xFFFAF9F6) else Color.White
        ),
        border = BorderStroke(1.dp, if (isCancelled) FigmaBorder.copy(alpha = 0.5f) else FigmaBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCancelled) 0.dp else 1.dp),
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = subscription.name,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = if (isCancelled) FigmaMutedSlate else FigmaDarkText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .alignByBaseline()
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = CurrencyFormatter.formatLkr(subscription.amountLkr),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = if (isCancelled) FigmaMutedSlate else FigmaDarkText,
                maxLines = 1,
                softWrap = false,
                textAlign = TextAlign.End,
                modifier = Modifier.alignByBaseline()
            )
        }
    }
}

/**
 * Transaction Detail Modal Card Sheet matching InTrack Figma Design Language:
 * - Slides up on clicking any transaction card in the Daily Transactions tab
 * - Displays all additional information: Title, Amount, Date & Time, Category, Payment Source, Commitment Tier, Notes, ID
 * - Action buttons on bottom: Delete button on bottom-left, Edit button on bottom-right
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FigmaTransactionDetailSheet(
    expense: ExpenseEntity,
    activeGoal: GoalEntity = GoalEntity(title = "MacBook Pro M4 Fund", monthlyTargetRunRateLkr = 40833.33),
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val haptics = LocalHapticFeedback.current
    val isTransfer = expense.isInternalTransfer()
    val isCommitted = expense.isCommittedExpense()

    val categoryIcon: ImageVector = when {
        isTransfer || expense.category == "ATM_WITHDRAWAL" -> Icons.Default.Payments
        expense.title.contains("ATM", ignoreCase = true) || (expense.paymentAccount.equals("CASH", ignoreCase = true) && expense.category.equals("MISC", ignoreCase = true)) -> Icons.Default.Payments
        expense.category.uppercase() == "TRANSPORT" -> Icons.Default.DirectionsCar
        expense.category.uppercase() == "DINING_OUT" -> Icons.Default.LocalCafe
        expense.category.uppercase() == "RENT" -> Icons.Default.Home
        expense.category.uppercase() == "MISC" -> Icons.Default.ShoppingCart
        else -> Icons.Default.Receipt
    }

    val accountLabel = when (expense.paymentAccount.uppercase()) {
        "CARD_PRIMARY" -> "Card Primary"
        "ACCOUNT_SECONDARY" -> "Secondary Bank Account"
        "CASH" -> "Cash"
        else -> expense.paymentAccount.replace("_", " ")
    }

    val displayCategory = when (expense.category.uppercase()) {
        "ATM_WITHDRAWAL" -> "ATM / Cash Withdrawal"
        "DINING_OUT" -> "Dining Out"
        "TRANSPORT" -> "Transportation"
        "RENT" -> "Housing / Rent"
        "SUBSCRIPTION" -> "Subscription"
        "UTILITIES" -> "Utilities"
        "MISC" -> "Groceries & Misc"
        else -> expense.category
    }

    val formattedDateTime = formatFullDateTime(expense.dateSpentMillis)

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
                .navigationBarsPadding()
        ) {
            // Header: Category Icon + Title + Account Badge + Close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(FigmaIconCircleBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = categoryIcon,
                            contentDescription = expense.category,
                            tint = FigmaDarkText,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = expense.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = FigmaDarkText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFF1F3F4))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = accountLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = FigmaMutedBrown
                                )
                            }
                        }
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = FigmaMutedSlate
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Hero Outflow / Transfer Amount Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isTransfer) Color(0xFFF0F5FF) else Color(0xFFFBF8F5)
                ),
                border = BorderStroke(1.dp, if (isTransfer) Color(0xFFBFDBFE) else FigmaBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isTransfer) "INTERNAL TRANSFER" else "EXPENSE AMOUNT",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = if (isTransfer) Color(0xFF2563EB) else FigmaMutedBrown
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(
                                    when {
                                        isTransfer -> Color(0xFFDBEAFE)
                                        isCommitted -> Color(0xFFF7EBE6)
                                        else -> Color(0xFFEDE8E3)
                                    }
                                )
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = when {
                                    isTransfer -> "CASH MOVEMENT"
                                    isCommitted -> "ESSENTIALS"
                                    else -> "LIFESTYLE"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = when {
                                    isTransfer -> Color(0xFF1D4ED8)
                                    isCommitted -> FigmaTerracotta
                                    else -> FigmaDarkText
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isTransfer) "⇄ ${CurrencyFormatter.formatLkr(expense.amountLkr)}" else "- ${CurrencyFormatter.formatLkr(expense.amountLkr)}",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        ),
                        color = if (isTransfer) Color(0xFF2563EB) else FigmaTerracotta
                    )
                }
            }

            // Transfer explanation notification banner
            if (isTransfer) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "ℹ️", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Internal Transfer to Cash Wallet. Excluded from monthly outflow totals to prevent double-counting when spending cash.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.5.sp,
                                lineHeight = 17.sp,
                                color = Color(0xFF475569)
                            )
                        )
                    }
                }
            }

            // Goal Opportunity Cost Chip (for discretionary lifestyle expenses)
            if (!isCommitted && !isTransfer) {
                val goalRunRate = if (activeGoal.monthlyTargetRunRateLkr > 0) activeGoal.monthlyTargetRunRateLkr else 40833.33
                val costPercentage = ((expense.amountLkr / goalRunRate) * 100).toInt()
                val percentDisplay = if (costPercentage > 0) "$costPercentage%" else "< 1%"
                val itemTitle = if (expense.title.isNotBlank() && !expense.title.equals("Expense", ignoreCase = true)) expense.title.lowercase() else "item"

                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9F5)),
                    border = BorderStroke(1.dp, FigmaTerracotta.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🎯", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "This ${CurrencyFormatter.formatLkr(expense.amountLkr)} $itemTitle equals $percentDisplay of your monthly ${activeGoal.title} contribution.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 17.sp,
                                color = FigmaDarkText
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Additional details card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, FigmaBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Date & Time
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Date & Time",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = FigmaMutedBrown
                        )
                        Text(
                            text = formattedDateTime,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = FigmaDarkText
                        )
                    }

                    HorizontalDivider(thickness = 0.8.dp, color = FigmaDivider)

                    // Category
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Category",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = FigmaMutedBrown
                        )
                        Text(
                            text = displayCategory,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = FigmaDarkText
                        )
                    }

                    HorizontalDivider(thickness = 0.8.dp, color = FigmaDivider)

                    // Payment Method
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Payment Method",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = FigmaMutedBrown
                        )
                        Text(
                            text = accountLabel,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = FigmaDarkText
                        )
                    }

                    HorizontalDivider(thickness = 0.8.dp, color = FigmaDivider)

                    // Commitment Classification
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Commitment Tier",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = FigmaMutedBrown
                        )
                        Text(
                            text = when {
                                isTransfer -> "Internal Transfer (Non-Outflow)"
                                isCommitted -> "Committed (Essentials)"
                                else -> "Discretionary (Lifestyle)"
                            },
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = when {
                                    isTransfer -> Color(0xFF2563EB)
                                    isCommitted -> FigmaTerracotta
                                    else -> FigmaDarkText
                                }
                            )
                        )
                    }

                    HorizontalDivider(thickness = 0.8.dp, color = FigmaDivider)

                    // Expense Nature
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Expense Type",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = FigmaMutedBrown
                        )
                        Text(
                            text = when {
                                isTransfer -> "Internal Cash Movement"
                                expense.isRecurring -> "Recurring Obligation"
                                else -> "One-Time Outflow"
                            },
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = FigmaDarkText
                        )
                    }

                    if (!expense.notes.isNullOrBlank()) {
                        HorizontalDivider(thickness = 0.8.dp, color = FigmaDivider)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Notes",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = FigmaMutedBrown
                            )
                            Text(
                                text = expense.notes,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Normal),
                                color = FigmaDarkText
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons: Delete on bottom-left, Edit on bottom-right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Bottom-Left: Delete
                OutlinedButton(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onDelete()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFD32F2F).copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFD32F2F)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Delete",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                // Bottom-Right: Edit
                Button(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onEdit()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FigmaTerracotta,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Edit",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

/**
 * Modern ergonomic ModalBottomSheet for viewing, toggling, and managing subscriptions.
 * Replaces the static AlertDialog with smooth gesture dismiss, annualized cost breakdown,
 * auto-debit indicators, and tactile action buttons.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FigmaSubscriptionDetailSheet(
    subscription: SubscriptionEntity,
    onDismiss: () -> Unit,
    onToggleStatus: (newStatus: String) -> Unit,
    onMarkAsPaid: () -> Unit
) {
    val haptics = LocalHapticFeedback.current
    val isCancelled = subscription.status.equals("CANCELLED", ignoreCase = true)

    val icon: ImageVector = when {
        subscription.name.contains("Rent", ignoreCase = true) || subscription.name.contains("House", ignoreCase = true) -> Icons.Default.Home
        subscription.name.contains("Gym", ignoreCase = true) || subscription.name.contains("Fitness", ignoreCase = true) -> Icons.Default.FitnessCenter
        subscription.name.contains("SaaS", ignoreCase = true) || subscription.name.contains("Cloud", ignoreCase = true) || subscription.name.contains("Stream", ignoreCase = true) -> Icons.Default.Cloud
        else -> Icons.Default.Receipt
    }

    val accountLabel = when (subscription.paymentAccount.uppercase()) {
        "CARD_PRIMARY" -> "Card Primary"
        "ACCOUNT_SECONDARY" -> "Secondary Bank Account"
        "CASH" -> "Cash"
        else -> subscription.paymentAccount.replace("_", " ")
    }

    val diff = subscription.nextBillingMillis - System.currentTimeMillis()
    val daysRemaining = (diff / (24 * 3600 * 1000)).coerceAtLeast(0)
    val nextDateFormatted = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.US).format(Date(subscription.nextBillingMillis))

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
                .navigationBarsPadding()
        ) {
            // Header: Icon + Title + Status Pill + Close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(if (isCancelled) Color(0xFFF0EFEB) else FigmaIconCircleBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = subscription.name,
                            tint = if (isCancelled) FigmaMutedSlate else FigmaDarkText,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = subscription.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = FigmaDarkText
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isCancelled) Color(0xFFF1F3F4) else Color(0xFFE6F4EA))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (isCancelled) "PAUSED" else "ACTIVE",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = if (isCancelled) FigmaMutedSlate else Color(0xFF137333)
                                )
                            }
                            Text(
                                text = "•  $accountLabel",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = FigmaMutedBrown
                            )
                        }
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = FigmaMutedSlate
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Cost summary cards (Monthly & Annualized Outflow)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = FigmaCardBg),
                    border = BorderStroke(1.dp, FigmaBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "MONTHLY",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = FigmaMutedBrown
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = CurrencyFormatter.formatLkr(subscription.amountLkr),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = FigmaDarkText
                        )
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = FigmaCardBg),
                    border = BorderStroke(1.dp, FigmaBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "ANNUALIZED",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = FigmaMutedBrown
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = CurrencyFormatter.formatLkr(subscription.amountLkr * 12),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = FigmaTerracotta
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Schedule & Renewal details card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, FigmaBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Next Renewal",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = FigmaMutedBrown
                        )
                        Text(
                            text = if (daysRemaining == 0L) "Today" else "In $daysRemaining days",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (daysRemaining <= 3L && !isCancelled) FigmaTerracotta else FigmaDarkText
                            )
                        )
                    }

                    HorizontalDivider(thickness = 0.8.dp, color = FigmaDivider)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Renewal Date",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = FigmaMutedBrown
                        )
                        Text(
                            text = nextDateFormatted,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = FigmaDarkText
                        )
                    }

                    HorizontalDivider(thickness = 0.8.dp, color = FigmaDivider)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Auto-Debit",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = FigmaMutedBrown
                        )
                        Text(
                            text = if (subscription.paymentAccount != "CASH") "Auto-charged via Bank" else "Manual Payment",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = if (subscription.paymentAccount != "CASH") Color(0xFF1B6B38) else FigmaMutedSlate
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onMarkAsPaid()
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, FigmaBorder)
                ) {
                    Text(
                        text = "Mark Paid",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = FigmaDarkText
                    )
                }

                Button(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        val newStatus = if (isCancelled) "ACTIVE" else "CANCELLED"
                        onToggleStatus(newStatus)
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCancelled) FigmaTerracotta else MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(
                        text = if (isCancelled) "Resume Sub" else "Pause / Cancel",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }
        }
    }
}

/**
 * Auto-debit Alert Insight Banner matching Figma node 2006:168
 */
@Composable
fun FigmaAutoDebitAlertBanner(
    upcomingAmount: Double,
    upcomingName: String,
    onManageClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F6F0)),
        border = BorderStroke(1.dp, FigmaBorder),
        modifier = modifier.fillMaxWidth()
    ) {
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
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(FigmaIconCircleBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = "Alert",
                        tint = FigmaTerracotta,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = "Auto-debit alert",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = FigmaDarkText
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Ensure ${CurrencyFormatter.formatLkr(upcomingAmount)} is buffered by Friday",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.5.sp
                        ),
                        color = FigmaMutedBrown
                    )
                }
            }

            TextButton(
                onClick = onManageClick,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Manage",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = FigmaTerracotta
                )
            }
        }
    }
}

/**
 * Dynamic Shock Insight Banner matching InTrack Figma Design Language:
 * Displays when discretionary dining/coffee spending exceeds threshold,
 * showing the exact percentage of the user's active goal (e.g. MacBook Pro M4 Fund) monthly requirement.
 */
@Composable
fun FigmaShockInsightBanner(
    diningCoffeeTotal: Double,
    goalRunRate: Double,
    goalTitle: String,
    modifier: Modifier = Modifier
) {
    val percentage = if (goalRunRate > 0) ((diningCoffeeTotal / goalRunRate) * 100).toInt() else 0

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED)),
        border = BorderStroke(1.5.dp, Color(0xFFFDBA74)),
        modifier = modifier.fillMaxWidth()
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "⚠️", fontSize = 16.sp)
                    Text(
                        text = "LIFESTYLE IMPACT ALERT",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = Color(0xFFC2410C)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color(0xFFFFEDD5))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "$percentage% of Goal Pace",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color(0xFFC2410C)
                    )
                }
            }

            Text(
                text = "High Lifestyle Outflow: You've spent ${CurrencyFormatter.formatLkr(diningCoffeeTotal)} on Dining & Coffee this month. This accounts for $percentage% of your monthly $goalTitle requirement.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 18.sp
                ),
                color = Color(0xFF7C2D12)
            )

            // Progress bar showing how much of the monthly goal run-rate has been diverted into dining/coffee
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                val progressFraction = (diningCoffeeTotal / goalRunRate).toFloat().coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFED7AA))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = progressFraction)
                            .height(6.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEA580C))
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Dining Outflow: ${CurrencyFormatter.formatLkr(diningCoffeeTotal)}",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = Color(0xFF9A3412)
                    )
                    Text(
                        text = "Monthly Goal Pace: ${CurrencyFormatter.formatLkr(goalRunRate)}",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = Color(0xFF9A3412)
                    )
                }
            }
        }
    }
}
