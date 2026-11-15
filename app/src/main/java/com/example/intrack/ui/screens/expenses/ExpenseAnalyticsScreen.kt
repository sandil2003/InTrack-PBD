package com.example.intrack.ui.screens.expenses

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.intrack.data.model.ExpenseEntity
import com.example.intrack.data.model.GoalEntity
import com.example.intrack.data.model.SubscriptionEntity
import com.example.intrack.ui.common.EmptyStateView
import com.example.intrack.ui.common.UiState
import com.example.intrack.ui.components.ProfileSettingsBottomSheet
import com.example.intrack.ui.screens.subscriptions.AddSubscriptionDialog
import com.example.intrack.ui.screens.subscriptions.ViewSubscriptionBottomSheet
import com.example.intrack.ui.theme.AppThemeMode
import com.example.intrack.ui.theme.BrandAzureBlue
import com.example.intrack.ui.theme.BrandMidnightNavy
import com.example.intrack.ui.theme.BrandSoftSlate
import com.example.intrack.ui.theme.BrandTangerine
import com.example.intrack.ui.theme.BrandWarmYellow
import com.example.intrack.ui.theme.InTrackAddFab
import com.example.intrack.ui.theme.InTrackSegmentedSwitch
import com.example.intrack.ui.theme.InTrackTheme
import com.example.intrack.ui.theme.InTrackTopBar
import com.example.intrack.util.CurrencyFormatter
import com.example.intrack.util.DateUtils
import com.example.intrack.viewmodel.ExpenseAnalyticsSummary
import com.example.intrack.viewmodel.ExpenseViewModel
import com.example.intrack.viewmodel.GoalViewModel
import com.example.intrack.viewmodel.SubscriptionAuditSummary
import com.example.intrack.viewmodel.SubscriptionViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ExpenseViewTab {
    DAILY_TRANSACTIONS,
    SUBSCRIPTIONS
}

@Composable
fun ExpenseAnalyticsScreen(
    initialTab: String = "daily",
    openAddModal: Boolean = false,
    onNavigateBack: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToProfile: () -> Unit = onNavigateToSettings,
    themeMode: AppThemeMode = AppThemeMode.LIGHT,
    onThemeModeChange: (AppThemeMode) -> Unit = {},
    onSignOut: () -> Unit = {},
    viewModel: ExpenseViewModel = hiltViewModel(),
    subscriptionViewModel: SubscriptionViewModel = hiltViewModel(),
    goalViewModel: GoalViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val expensesState by viewModel.expensesState.collectAsState()
    val analyticsSummary by viewModel.analyticsSummary.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val selectedTypeFilter by viewModel.selectedTypeFilter.collectAsState()
    val selectedCategoryFilter by viewModel.selectedCategoryFilter.collectAsState()
    val selectedAccountFilter by viewModel.selectedAccountFilter.collectAsState()

    val subscriptionsState by subscriptionViewModel.subscriptionsState.collectAsState()
    val auditSummary by subscriptionViewModel.auditSummary.collectAsState()

    val goalPortfolio by goalViewModel.portfolioSummary.collectAsState()
    val activeGoal = goalPortfolio.activeGoal

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    ExpenseAnalyticsContent(
        initialTab = initialTab,
        openAddModal = openAddModal,
        expensesState = expensesState,
        analyticsSummary = analyticsSummary,
        subscriptionsState = subscriptionsState,
        auditSummary = auditSummary,
        activeGoal = activeGoal,
        selectedMonth = selectedMonth,
        selectedTypeFilter = selectedTypeFilter,
        selectedCategoryFilter = selectedCategoryFilter,
        selectedAccountFilter = selectedAccountFilter,
        recentMonths = viewModel.recentMonths,
        themeMode = themeMode,
        onThemeModeChange = onThemeModeChange,
        onSelectMonth = { viewModel.setSelectedMonth(it) },
        onSelectAccountFilter = { viewModel.setAccountFilter(it) },
        onSelectTypeAndCategory = { typeFilter, catFilter ->
            viewModel.setTypeAndCategoryFilter(typeFilter, catFilter)
        },
        onToggleCategoryFilter = { viewModel.toggleCategoryFilter(it) },
        onClearFilters = { viewModel.clearFilters() },
        onAddExpense = { newExpense ->
            viewModel.addExpense(newExpense)
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Added ${newExpense.title}")
            }
        },
        onUpdateExpense = { updatedExpense ->
            viewModel.updateExpense(updatedExpense)
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Updated ${updatedExpense.title}")
            }
        },
        onDeleteExpense = { exp ->
            viewModel.deleteExpense(exp.userId, exp.id)
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Deleted ${exp.title}")
            }
        },
        onRefreshExpenses = { viewModel.loadExpenses() },
        onAddSubscription = { newSub ->
            subscriptionViewModel.addSubscription(newSub)
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Added ${newSub.name}")
            }
        },
        onUpdateSubscription = { updatedSub ->
            subscriptionViewModel.updateSubscription(updatedSub)
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Updated ${updatedSub.name}")
            }
        },
        onDeleteSubscription = { subToDelete ->
            subscriptionViewModel.deleteSubscription(subToDelete.userId, subToDelete.id)
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Deleted ${subToDelete.name}")
            }
        },
        onRefreshSubscriptions = { subscriptionViewModel.loadSubscriptions() },
        onNavigateToSettings = onNavigateToSettings,
        onNavigateToProfile = onNavigateToProfile,
        onSignOut = onSignOut,
        snackbarHostState = snackbarHostState,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseAnalyticsContent(
    initialTab: String = "daily",
    openAddModal: Boolean = false,
    expensesState: UiState<List<ExpenseEntity>>,
    analyticsSummary: ExpenseAnalyticsSummary,
    subscriptionsState: UiState<List<SubscriptionEntity>>,
    auditSummary: SubscriptionAuditSummary,
    activeGoal: GoalEntity,
    selectedMonth: String,
    selectedTypeFilter: String,
    selectedCategoryFilter: String,
    selectedAccountFilter: String,
    recentMonths: List<String>,
    themeMode: AppThemeMode = AppThemeMode.LIGHT,
    onThemeModeChange: (AppThemeMode) -> Unit = {},
    onSelectMonth: (String) -> Unit,
    onSelectAccountFilter: (String) -> Unit,
    onSelectTypeAndCategory: (String, String) -> Unit,
    onToggleCategoryFilter: (String) -> Unit,
    onClearFilters: () -> Unit,
    onAddExpense: (ExpenseEntity) -> Unit,
    onUpdateExpense: (ExpenseEntity) -> Unit,
    onDeleteExpense: (ExpenseEntity) -> Unit,
    onRefreshExpenses: () -> Unit,
    onAddSubscription: (SubscriptionEntity) -> Unit,
    onUpdateSubscription: (SubscriptionEntity) -> Unit,
    onDeleteSubscription: (SubscriptionEntity) -> Unit,
    onRefreshSubscriptions: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onSignOut: () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    modifier: Modifier = Modifier
) {
    val initialViewTab = if (initialTab.equals("subscriptions", ignoreCase = true)) {
        ExpenseViewTab.SUBSCRIPTIONS
    } else {
        ExpenseViewTab.DAILY_TRANSACTIONS
    }
    var currentTab by remember(initialTab) { mutableStateOf(initialViewTab) }

    val hapticFeedback = LocalHapticFeedback.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    var accountFilterExpanded by remember { mutableStateOf(false) }
    var categoryFilterExpanded by remember { mutableStateOf(false) }
    var showActivityFilters by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showChartsModal by remember { mutableStateOf(false) }
    var showSubscriptionChartsModal by remember { mutableStateOf(false) }
    var showMonthPicker by remember { mutableStateOf(false) }
    var showProfileSheet by remember { mutableStateOf(false) }

    var selectedExpense by remember { mutableStateOf<ExpenseEntity?>(null) }
    var expenseToEdit by remember { mutableStateOf<ExpenseEntity?>(null) }
    var expenseToDelete by remember { mutableStateOf<ExpenseEntity?>(null) }

    var showAddSubscriptionDialog by remember { mutableStateOf(false) }
    var activeActionSub by remember { mutableStateOf<SubscriptionEntity?>(null) }
    var subscriptionToEdit by remember { mutableStateOf<SubscriptionEntity?>(null) }

    LaunchedEffect(openAddModal) {
        if (openAddModal) {
            if (currentTab == ExpenseViewTab.DAILY_TRANSACTIONS) {
                showAddDialog = true
            } else {
                showAddSubscriptionDialog = true
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            InTrackTopBar(
                title = "Expenses",
                onNavigateToSettings = onNavigateToSettings,
                onNavigateToProfile = onNavigateToProfile,
                onSignOut = onSignOut,
                showProfileMenu = true,
                actions = {
                    if (currentTab == ExpenseViewTab.DAILY_TRANSACTIONS) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .clickable { showMonthPicker = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Select Month",
                                modifier = Modifier.size(22.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }
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
                contentDescription = if (currentTab == ExpenseViewTab.DAILY_TRANSACTIONS) "Add expense" else "Add subscription"
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
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
                        DailyExpensesTabContent(
                            expensesState = expensesState,
                            summary = analyticsSummary,
                            selectedMonth = selectedMonth,
                            selectedTypeFilter = selectedTypeFilter,
                            selectedCategoryFilter = selectedCategoryFilter,
                            selectedAccountFilter = selectedAccountFilter,
                            isLandscape = isLandscape,
                            showActivityFilters = showActivityFilters,
                            accountFilterExpanded = accountFilterExpanded,
                            categoryFilterExpanded = categoryFilterExpanded,
                            onToggleFilters = { showActivityFilters = !showActivityFilters },
                            onAccountExpandedChange = { accountFilterExpanded = it },
                            onCategoryExpandedChange = { categoryFilterExpanded = it },
                            onSelectAccountFilter = {
                                onSelectAccountFilter(it)
                                accountFilterExpanded = false
                            },
                            onSelectTypeAndCategory = { typeF, catF ->
                                onSelectTypeAndCategory(typeF, catF)
                                categoryFilterExpanded = false
                            },
                            onClearFilters = onClearFilters,
                            onOpenCharts = { showChartsModal = true },
                            onExpenseClick = { expense ->
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                selectedExpense = expense
                            },
                            onAddClick = { showAddDialog = true },
                            onRefresh = onRefreshExpenses
                        )

                        if (showChartsModal && expensesState is UiState.Success) {
                            ExpenseChartsModal(
                                monthLabel = selectedMonth,
                                totalExpenseLkr = analyticsSummary.totalExpenseLkr,
                                committedLkr = analyticsSummary.committedLkr,
                                discretionaryLkr = analyticsSummary.discretionaryLkr,
                                cardPrimaryTotal = analyticsSummary.cardPrimaryTotalLkr,
                                secondaryAccountTotal = analyticsSummary.secondaryAccountTotalLkr,
                                cashTotal = analyticsSummary.cashTotalLkr,
                                categoryItems = analyticsSummary.categoryBreakdownItems,
                                onDismiss = { showChartsModal = false },
                                onSelectCategory = { catKey ->
                                    onToggleCategoryFilter(catKey)
                                },
                                onSelectCommitmentType = { tierKey ->
                                    onSelectTypeAndCategory(tierKey, "ALL")
                                }
                            )
                        }
                    }

                    ExpenseViewTab.SUBSCRIPTIONS -> {
                        SubscriptionsTabContent(
                            subscriptionsState = subscriptionsState,
                            auditSummary = auditSummary,
                            isLandscape = isLandscape,
                            onOpenCharts = { showSubscriptionChartsModal = true },
                            onSubscriptionClick = { sub ->
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                activeActionSub = sub
                            },
                            onAddClick = { showAddSubscriptionDialog = true },
                            onRefresh = onRefreshSubscriptions
                        )

                        if (showSubscriptionChartsModal && subscriptionsState is UiState.Success) {
                            SubscriptionBreakdownPieModal(
                                totalRecurringLkr = auditSummary.totalActiveLkr,
                                secondaryAccountTotal = auditSummary.secondaryTotalLkr,
                                primaryCardTotal = auditSummary.primaryCardTotalLkr,
                                cashTotal = auditSummary.cashTotalLkr,
                                onDismiss = { showSubscriptionChartsModal = false }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddExpenseDialog(
            onDismiss = { showAddDialog = false },
            onSubmit = { newExpense ->
                onAddExpense(newExpense)
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
                selectedExpense = null
                expenseToEdit = exp
            },
            onDelete = {
                selectedExpense = null
                expenseToDelete = exp
            }
        )
    }

    if (expenseToEdit != null) {
        EditExpenseSheet(
            expense = expenseToEdit!!,
            onDismiss = { expenseToEdit = null },
            onUpdateExpense = { updatedExpense ->
                onUpdateExpense(updatedExpense)
                expenseToEdit = null
            },
            onDeleteExpense = { toDelete ->
                onDeleteExpense(toDelete)
                expenseToEdit = null
            }
        )
    }

    if (expenseToDelete != null) {
        val exp = expenseToDelete!!
        AlertDialog(
            onDismissRequest = { expenseToDelete = null },
            shape = RoundedCornerShape(20.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    text = "Delete expense?",
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = "Remove '${exp.title}'?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteExpense(exp)
                        expenseToDelete = null
                        selectedExpense = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onError)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { expenseToDelete = null },
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurface)
                }
            }
        )
    }

    if (showAddSubscriptionDialog) {
        AddSubscriptionDialog(
            onDismiss = { showAddSubscriptionDialog = false },
            onSubmit = { newSub ->
                onAddSubscription(newSub)
                showAddSubscriptionDialog = false
            }
        )
    }

    if (subscriptionToEdit != null) {
        AddSubscriptionDialog(
            initialSubscription = subscriptionToEdit,
            onDismiss = { subscriptionToEdit = null },
            onSubmit = { updatedSub ->
                onUpdateSubscription(updatedSub)
                subscriptionToEdit = null
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
                onDeleteSubscription(subToDelete)
                activeActionSub = null
            }
        )
    }

    if (showMonthPicker) {
        ModalBottomSheet(
            onDismissRequest = { showMonthPicker = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Select Month",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                recentMonths.forEach { month ->
                    val isSelected = month == selectedMonth
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                            .clickable {
                                onSelectMonth(month)
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
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                        )
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
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
                onSignOut()
            },
            onNavigateToProfile = onNavigateToProfile,
            onDismiss = { showProfileSheet = false }
        )
    }
}

@Composable
private fun DailyExpensesTabContent(
    expensesState: UiState<List<ExpenseEntity>>,
    summary: ExpenseAnalyticsSummary,
    selectedMonth: String,
    selectedTypeFilter: String,
    selectedCategoryFilter: String,
    selectedAccountFilter: String,
    isLandscape: Boolean,
    showActivityFilters: Boolean,
    accountFilterExpanded: Boolean,
    categoryFilterExpanded: Boolean,
    onToggleFilters: () -> Unit,
    onAccountExpandedChange: (Boolean) -> Unit,
    onCategoryExpandedChange: (Boolean) -> Unit,
    onSelectAccountFilter: (String) -> Unit,
    onSelectTypeAndCategory: (String, String) -> Unit,
    onClearFilters: () -> Unit,
    onOpenCharts: () -> Unit,
    onExpenseClick: (ExpenseEntity) -> Unit,
    onAddClick: () -> Unit,
    onRefresh: () -> Unit
) {
    when (expensesState) {
        is UiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = BrandMidnightNavy)
            }
        }

        is UiState.Error -> {
            EmptyStateView(
                title = "Load failed",
                message = expensesState.message,
                actionLabel = "Retry",
                onActionClick = onRefresh
            )
        }

        is UiState.Empty -> {
            EmptyStateView(
                title = "No expenses yet",
                message = "",
                actionLabel = "Add Expense",
                onActionClick = onAddClick
            )
        }

        is UiState.Success -> {
            val filteredExpenses = summary.filteredExpenses
            val groupedExpenses = summary.groupedExpenses

            if (isLandscape) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(0.45f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Spacer(modifier = Modifier.height(4.dp))
                        FigmaHeroOutflowCard(
                            monthLabel = selectedMonth,
                            totalExpenseLkr = summary.totalExpenseLkr,
                            rollingAvgLkr = summary.rollingAverageLkr,
                            onOpenCharts = onOpenCharts
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    LazyColumn(
                        modifier = Modifier
                            .weight(0.55f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(4.dp))
                            ExpenseActivityHeader(
                                selectedMonth = selectedMonth,
                                selectedTypeFilter = selectedTypeFilter,
                                selectedCategoryFilter = selectedCategoryFilter,
                                selectedAccountFilter = selectedAccountFilter,
                                showFilters = showActivityFilters,
                                accountFilterExpanded = accountFilterExpanded,
                                categoryFilterExpanded = categoryFilterExpanded,
                                onToggleFilters = onToggleFilters,
                                onAccountExpandedChange = onAccountExpandedChange,
                                onCategoryExpandedChange = onCategoryExpandedChange,
                                onSelectAccountFilter = onSelectAccountFilter,
                                onSelectTypeAndCategory = onSelectTypeAndCategory,
                                onClearFilters = onClearFilters
                            )
                        }

                        if (filteredExpenses.isEmpty()) {
                            item {
                                EmptyStateView(
                                    title = "No matches",
                                    message = ""
                                )
                            }
                        } else {
                            groupedExpenses.forEach { (weekTier, expensesInGroup) ->
                                val showGroupHeader = !(groupedExpenses.size == 1 && weekTier == "This Week")
                                if (showGroupHeader) {
                                    item(key = "exp_header_$weekTier") {
                                        WeekGroupPill(label = weekTier)
                                    }
                                }
                                items(expensesInGroup, key = { it.id }) { expense ->
                                    FlatTransactionRow(
                                        expense = expense,
                                        onClick = { onExpenseClick(expense) }
                                    )
                                    HorizontalDivider(
                                        modifier = Modifier.fillMaxWidth(),
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                        thickness = 0.8.dp
                                    )
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(88.dp))
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        FigmaHeroOutflowCard(
                            monthLabel = selectedMonth,
                            totalExpenseLkr = summary.totalExpenseLkr,
                            rollingAvgLkr = summary.rollingAverageLkr,
                            onOpenCharts = onOpenCharts
                        )
                    }

                    item {
                        ExpenseActivityHeader(
                            selectedMonth = selectedMonth,
                            selectedTypeFilter = selectedTypeFilter,
                            selectedCategoryFilter = selectedCategoryFilter,
                            selectedAccountFilter = selectedAccountFilter,
                            showFilters = showActivityFilters,
                            accountFilterExpanded = accountFilterExpanded,
                            categoryFilterExpanded = categoryFilterExpanded,
                            onToggleFilters = onToggleFilters,
                            onAccountExpandedChange = onAccountExpandedChange,
                            onCategoryExpandedChange = onCategoryExpandedChange,
                            onSelectAccountFilter = onSelectAccountFilter,
                            onSelectTypeAndCategory = onSelectTypeAndCategory,
                            onClearFilters = onClearFilters
                        )
                    }

                    if (filteredExpenses.isEmpty()) {
                        item {
                            EmptyStateView(
                                title = "No matches",
                                message = ""
                            )
                        }
                    } else {
                        groupedExpenses.forEach { (weekTier, expensesInGroup) ->
                            val showGroupHeader = !(groupedExpenses.size == 1 && weekTier == "This Week")
                            if (showGroupHeader) {
                                item(key = "exp_header_$weekTier") {
                                    WeekGroupPill(label = weekTier)
                                }
                            }
                            items(expensesInGroup, key = { it.id }) { expense ->
                                FlatTransactionRow(
                                    expense = expense,
                                    onClick = { onExpenseClick(expense) }
                                )
                                HorizontalDivider(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                    thickness = 0.8.dp
                                )
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(88.dp))
                    }
                }
            }
        }

        is UiState.Idle -> Unit
    }
}

@Composable
private fun SubscriptionsTabContent(
    subscriptionsState: UiState<List<SubscriptionEntity>>,
    auditSummary: SubscriptionAuditSummary,
    isLandscape: Boolean,
    onOpenCharts: () -> Unit,
    onSubscriptionClick: (SubscriptionEntity) -> Unit,
    onAddClick: () -> Unit,
    onRefresh: () -> Unit
) {
    when (subscriptionsState) {
        is UiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = BrandMidnightNavy)
            }
        }

        is UiState.Error -> {
            EmptyStateView(
                title = "Load failed",
                message = subscriptionsState.message,
                actionLabel = "Retry",
                onActionClick = onRefresh
            )
        }

        is UiState.Empty -> {
            EmptyStateView(
                title = "No subscriptions yet",
                message = "",
                actionLabel = "Add Subscription",
                onActionClick = onAddClick
            )
        }

        is UiState.Success -> {
            val filteredSubs = auditSummary.filteredSubs
            val activeCount = auditSummary.activeSubs.size
            val nextLabel = if (auditSummary.nextSub != null) "${auditSummary.nextInDays}d" else null

            if (isLandscape) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(0.45f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Spacer(modifier = Modifier.height(4.dp))
                        FigmaMonthlyCommitmentsCard(
                            totalRecurringLkr = auditSummary.totalActiveLkr,
                            activeCount = activeCount,
                            nextInDays = if (auditSummary.nextSub != null) auditSummary.nextInDays else null,
                            onOpenCharts = onOpenCharts
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    LazyColumn(
                        modifier = Modifier
                            .weight(0.55f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(4.dp))
                            RecurringSectionHeader(count = filteredSubs.size, nextLabel = nextLabel)
                        }

                        if (filteredSubs.isEmpty()) {
                            item {
                                EmptyStateView(title = "No matches", message = "")
                            }
                        } else {
                            items(filteredSubs, key = { it.id.ifEmpty { it.name } }) { sub ->
                                FigmaSubscriptionItemCard(
                                    subscription = sub,
                                    onClick = { onSubscriptionClick(sub) },
                                    modifier = Modifier.animateItem()
                                )
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(88.dp))
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        FigmaMonthlyCommitmentsCard(
                            totalRecurringLkr = auditSummary.totalActiveLkr,
                            activeCount = activeCount,
                            nextInDays = if (auditSummary.nextSub != null) auditSummary.nextInDays else null,
                            onOpenCharts = onOpenCharts
                        )
                    }

                    item {
                        RecurringSectionHeader(count = filteredSubs.size, nextLabel = nextLabel)
                    }

                    if (filteredSubs.isEmpty()) {
                        item {
                            EmptyStateView(title = "No matches", message = "")
                        }
                    } else {
                        items(filteredSubs, key = { it.id.ifEmpty { it.name } }) { sub ->
                            FigmaSubscriptionItemCard(
                                subscription = sub,
                                onClick = { onSubscriptionClick(sub) },
                                modifier = Modifier.animateItem()
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(88.dp))
                    }
                }
            }
        }

        is UiState.Idle -> Unit
    }
}

@Composable
private fun WeekGroupPill(label: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun RecurringSectionHeader(
    count: Int,
    nextLabel: String?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Recurring",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "($count)",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (nextLabel != null) {
            Text(
                text = "Next: $nextLabel",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpenseActivityHeader(
    selectedMonth: String,
    selectedTypeFilter: String,
    selectedCategoryFilter: String,
    selectedAccountFilter: String,
    showFilters: Boolean,
    accountFilterExpanded: Boolean,
    categoryFilterExpanded: Boolean,
    onToggleFilters: () -> Unit,
    onAccountExpandedChange: (Boolean) -> Unit,
    onCategoryExpandedChange: (Boolean) -> Unit,
    onSelectAccountFilter: (String) -> Unit,
    onSelectTypeAndCategory: (String, String) -> Unit,
    onClearFilters: () -> Unit
) {
    val hasActiveFilters = selectedCategoryFilter != "ALL" || selectedTypeFilter != "ALL" || selectedAccountFilter != "ALL"

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$selectedMonth Activity",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (hasActiveFilters) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { onClearFilters() }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Clear",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (showFilters || hasActiveFilters) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent)
                        .clickable { onToggleFilters() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Tune,
                        contentDescription = "Filter Activity",
                        tint = if (showFilters || hasActiveFilters) BrandTangerine else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        if (showFilters) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val accountOptions = listOf(
                    "ALL" to "All Accounts",
                    "CASH" to "Cash",
                    "CARD_PRIMARY" to "Card",
                    "ACCOUNT_SECONDARY" to "Bank"
                )
                val selectedAccLabel = accountOptions.find { it.first == selectedAccountFilter }?.second ?: "All Accounts"

                Box(modifier = Modifier.weight(1f)) {
                    ExposedDropdownMenuBox(
                        expanded = accountFilterExpanded,
                        onExpandedChange = onAccountExpandedChange,
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
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
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
                            onDismissRequest = { onAccountExpandedChange(false) },
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
                                    onClick = { onSelectAccountFilter(key) },
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                val categoryOptions = listOf(
                    ("ALL" to "ALL") to "All",
                    ("COMMITTED" to "ALL") to "Essentials",
                    ("DISCRETIONARY" to "ALL") to "Lifestyle",
                    ("ALL" to "MISC") to "Groceries",
                    ("ALL" to "DINING_OUT") to "Dining",
                    ("ALL" to "TRANSPORT") to "Transport",
                    ("ALL" to "RENT") to "Housing",
                    ("ALL" to "SUBSCRIPTION") to "Subscriptions",
                    ("ALL" to "UTILITIES") to "Utilities",
                    ("ALL" to "ATM_WITHDRAWAL") to "Transfers"
                )
                val currentCatKey = selectedTypeFilter to selectedCategoryFilter
                val selectedCatLabel = categoryOptions.find { it.first == currentCatKey }?.second ?: "All"

                Box(modifier = Modifier.weight(1f)) {
                    ExposedDropdownMenuBox(
                        expanded = categoryFilterExpanded,
                        onExpandedChange = onCategoryExpandedChange,
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
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
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
                            onDismissRequest = { onCategoryExpandedChange(false) },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                        ) {
                            categoryOptions.forEach { (typeAndCat, label) ->
                                val (tFilter, cFilter) = typeAndCat
                                val isSelected = selectedTypeFilter == tFilter && selectedCategoryFilter == cFilter
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
                                    onClick = { onSelectTypeAndCategory(tFilter, cFilter) },
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Minimal Hero Outflow Card matching TotalInflowBreakdownCard on the Income page:
 * - Warm yellow tonal container with 20dp corners
 * - Top Row: "$monthLabel Outflow" on left, circular PieChart analytics button on top right
 * - Centered hero outflow amount
 * - Optional compact 3-month rolling average pill when prior baseline exists
 */
@Composable
fun FigmaHeroOutflowCard(
    monthLabel: String,
    totalExpenseLkr: Double,
    rollingAvgLkr: Double = 0.0,
    onOpenCharts: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasRollingBaseline = rollingAvgLkr > 0.0
    val diff = totalExpenseLkr - rollingAvgLkr
    val diffPct = if (hasRollingBaseline) ((diff / rollingAvgLkr) * 100).toInt() else 0
    val isHigher = diff > 0
    val navyText = BrandMidnightNavy

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = BrandWarmYellow),
        border = BorderStroke(0.dp, Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$monthLabel Outflow",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = navyText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(navyText.copy(alpha = 0.12f))
                        .clickable { onOpenCharts() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PieChart,
                        contentDescription = "View expense breakdown",
                        tint = navyText,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val currentSymbol = CurrencyFormatter.getCurrencySymbol()
            val formattedOutflowAmount = CurrencyFormatter.formatLkr(totalExpenseLkr).replace(currentSymbol, "").trim()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(
                    space = 6.dp,
                    alignment = Alignment.CenterHorizontally
                ),
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = currentSymbol,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = navyText,
                    modifier = Modifier.alignByBaseline()
                )
                Text(
                    text = formattedOutflowAmount,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    ),
                    color = navyText,
                    maxLines = 1,
                    modifier = Modifier.alignByBaseline()
                )
            }

            if (hasRollingBaseline) {
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(navyText.copy(alpha = 0.12f))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        val arrow = if (isHigher) "▲ +" else "▼ "
                        Text(
                            text = "${arrow}$diffPct% vs 3M avg (${CurrencyFormatter.formatLkr(rollingAvgLkr)})",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = navyText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

/**
 * Minimal Monthly Commitments Card matching FigmaHeroOutflowCard and TotalInflowBreakdownCard.
 */
@Composable
fun FigmaMonthlyCommitmentsCard(
    totalRecurringLkr: Double,
    activeCount: Int = 0,
    nextInDays: Long? = null,
    onOpenCharts: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val navyText = BrandMidnightNavy

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = BrandWarmYellow),
        border = BorderStroke(0.dp, Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Monthly Commitments",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = navyText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(navyText.copy(alpha = 0.12f))
                        .clickable { onOpenCharts() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PieChart,
                        contentDescription = "View subscription breakdown",
                        tint = navyText,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val currentSymbol = CurrencyFormatter.getCurrencySymbol()
            val formattedTotal = CurrencyFormatter.formatLkr(totalRecurringLkr).replace(currentSymbol, "").trim()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(
                    space = 6.dp,
                    alignment = Alignment.CenterHorizontally
                ),
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = currentSymbol,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = navyText,
                    modifier = Modifier.alignByBaseline()
                )
                Text(
                    text = formattedTotal,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    ),
                    color = navyText,
                    maxLines = 1,
                    modifier = Modifier.alignByBaseline()
                )
            }

            if (activeCount > 0 || nextInDays != null) {
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    val pillText = buildString {
                        if (activeCount > 0) append("$activeCount active")
                        if (nextInDays != null) {
                            if (isNotEmpty()) append(" · ")
                            append("Next ${nextInDays}d")
                        }
                    }
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(navyText.copy(alpha = 0.12f))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = pillText,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = navyText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

/**
 * Flat Transaction List Row matching InflowHistoryRow spacing and visual clarity.
 */
@Composable
fun FlatTransactionRow(
    expense: ExpenseEntity,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isInternalTransfer = expense.isInternalTransfer()

    val categoryIcon = when {
        isInternalTransfer || expense.title.contains("ATM", ignoreCase = true) -> Icons.Default.Payments
        expense.category.uppercase() == "DINING_OUT" -> Icons.Default.LocalCafe
        expense.category.uppercase() == "TRANSPORT" -> Icons.Default.DirectionsCar
        expense.category.uppercase() == "RENT" -> Icons.Default.Home
        expense.category.uppercase() == "MISC" && expense.paymentAccount.uppercase() == "CASH" -> Icons.Default.Payments
        expense.category.uppercase() == "MISC" -> Icons.Default.ShoppingCart
        else -> Icons.Default.Receipt
    }

    val accountLabel = when {
        isInternalTransfer -> "Transfer"
        expense.paymentAccount.uppercase() == "CARD_PRIMARY" -> "Card"
        expense.paymentAccount.uppercase() == "ACCOUNT_SECONDARY" -> "Bank"
        expense.paymentAccount.uppercase() == "CASH" -> "Cash"
        else -> expense.paymentAccount.replace("_", " ")
    }

    val (accBg, accText) = when {
        isInternalTransfer -> Pair(Color(0xFFE5F1FD), BrandAzureBlue)
        expense.paymentAccount.uppercase() == "CASH" -> Pair(Color(0xFFFFF7DB), BrandMidnightNavy)
        expense.paymentAccount.uppercase() == "CARD_PRIMARY" -> Pair(Color(0xFFE5F1FD), BrandAzureBlue)
        expense.paymentAccount.uppercase() == "ACCOUNT_SECONDARY" -> Pair(BrandSoftSlate, BrandMidnightNavy)
        else -> Pair(BrandSoftSlate, BrandMidnightNavy)
    }

    val formattedDate = DateUtils.formatDate(expense.dateSpentMillis)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 8.dp),
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
                    .background(BrandSoftSlate),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = categoryIcon,
                    contentDescription = expense.category,
                    tint = BrandMidnightNavy,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = expense.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(accBg)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = accountLabel,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                            color = accText
                        )
                    }
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = if (isInternalTransfer) "⇄ ${CurrencyFormatter.formatLkr(expense.amountLkr)}" else "-${CurrencyFormatter.formatLkr(expense.amountLkr)}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = if (isInternalTransfer) BrandAzureBlue else MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
fun FigmaExpenseViewsSwitch(
    currentTab: ExpenseViewTab,
    onTabSelected: (ExpenseViewTab) -> Unit,
    modifier: Modifier = Modifier
) {
    InTrackSegmentedSwitch(
        leftLabel = "Daily",
        rightLabel = "Subscriptions",
        isLeftSelected = currentTab == ExpenseViewTab.DAILY_TRANSACTIONS,
        onLeftSelect = { onTabSelected(ExpenseViewTab.DAILY_TRANSACTIONS) },
        onRightSelect = { onTabSelected(ExpenseViewTab.SUBSCRIPTIONS) },
        modifier = modifier
    )
}

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
        colors = CardDefaults.cardColors(containerColor = BrandSoftSlate),
        border = BorderStroke(0.dp, Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
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
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = subscription.name,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = if (isCancelled) BrandMidnightNavy.copy(alpha = 0.5f) else BrandMidnightNavy,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (isCancelled) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.White)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Paused",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            fontWeight = FontWeight.SemiBold,
                            color = BrandMidnightNavy.copy(alpha = 0.65f)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = CurrencyFormatter.formatLkr(subscription.amountLkr),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = if (isCancelled) BrandMidnightNavy.copy(alpha = 0.5f) else BrandMidnightNavy,
                maxLines = 1,
                softWrap = false,
                textAlign = TextAlign.End
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FigmaTransactionDetailSheet(
    expense: ExpenseEntity,
    activeGoal: GoalEntity = GoalEntity(title = "Savings Goal", monthlyTargetRunRateLkr = 40833.33),
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
        "CARD_PRIMARY" -> "Card"
        "ACCOUNT_SECONDARY" -> "Bank"
        "CASH" -> "Cash"
        else -> expense.paymentAccount.replace("_", " ")
    }

    val displayCategory = when (expense.category.uppercase()) {
        "ATM_WITHDRAWAL" -> "Transfer"
        "DINING_OUT" -> "Dining"
        "TRANSPORT" -> "Transport"
        "RENT" -> "Housing"
        "SUBSCRIPTION" -> "Subscription"
        "UTILITIES" -> "Utilities"
        "MISC" -> "Groceries"
        else -> expense.category
    }

    val formattedDate = SimpleDateFormat("MMM d, yyyy · hh:mm a", Locale.US).format(Date(expense.dateSpentMillis))

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .navigationBarsPadding()
        ) {
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
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(BrandSoftSlate),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = categoryIcon,
                            contentDescription = expense.category,
                            tint = BrandMidnightNavy,
                            modifier = Modifier.size(20.dp)
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
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$displayCategory · $accountLabel",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Minimal Amount Summary Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isTransfer) Color(0xFFE5F1FD) else BrandSoftSlate
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                            text = if (isTransfer) "Transfer" else "Amount",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isTransfer) BrandAzureBlue else BrandMidnightNavy.copy(alpha = 0.7f)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (!isCommitted && !isTransfer) {
                                val goalRunRate = if (activeGoal.monthlyTargetRunRateLkr > 0) activeGoal.monthlyTargetRunRateLkr else 40833.33
                                val costPct = ((expense.amountLkr / goalRunRate) * 100).toInt()
                                val pctLabel = if (costPct > 0) "$costPct% of goal" else "<1% of goal"
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(999.dp))
                                        .background(Color.White)
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = pctLabel,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        fontWeight = FontWeight.SemiBold,
                                        color = BrandMidnightNavy
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(Color.White)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = when {
                                        isTransfer -> "Excluded"
                                        isCommitted -> "Essential"
                                        else -> "Lifestyle"
                                    },
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = if (isTransfer) BrandAzureBlue else BrandMidnightNavy
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isTransfer) "⇄ ${CurrencyFormatter.formatLkr(expense.amountLkr)}" else "-${CurrencyFormatter.formatLkr(expense.amountLkr)}",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        ),
                        color = if (isTransfer) BrandAzureBlue else BrandMidnightNavy
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Compact Metadata Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DetailMetaRow(label = "Date", value = formattedDate)
                    HorizontalDivider(thickness = 0.8.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    DetailMetaRow(label = "Category", value = displayCategory)
                    HorizontalDivider(thickness = 0.8.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    DetailMetaRow(label = "Account", value = accountLabel)
                    HorizontalDivider(thickness = 0.8.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    DetailMetaRow(
                        label = "Type",
                        value = when {
                            isTransfer -> "Transfer"
                            expense.isRecurring -> "Recurring"
                            else -> "One-time"
                        }
                    )
                    if (!expense.notes.isNullOrBlank()) {
                        HorizontalDivider(thickness = 0.8.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        DetailMetaRow(label = "Note", value = expense.notes)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onDelete()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
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

                Button(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onEdit()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandMidnightNavy,
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

@Composable
private fun DetailMetaRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// -----------------------------------------------------------------------------------------
// COMPOSE PREVIEWS
// -----------------------------------------------------------------------------------------

private fun sampleExpenseSummary(): ExpenseAnalyticsSummary {
    val now = System.currentTimeMillis()
    val sampleExpenses = listOf(
        ExpenseEntity(
            id = "e1",
            title = "House Rent",
            amountLkr = 45000.0,
            category = "RENT",
            paymentAccount = "ACCOUNT_SECONDARY",
            commitmentType = "COMMITTED",
            dateSpentMillis = now - 86_400_000L * 2
        ),
        ExpenseEntity(
            id = "e2",
            title = "Keells Groceries",
            amountLkr = 17000.0,
            category = "MISC",
            paymentAccount = "CARD_PRIMARY",
            commitmentType = "COMMITTED",
            dateSpentMillis = now - 86_400_000L * 3
        ),
        ExpenseEntity(
            id = "e3",
            title = "Cafe Latte & Lunch",
            amountLkr = 8500.0,
            category = "DINING_OUT",
            paymentAccount = "CASH",
            commitmentType = "DISCRETIONARY",
            dateSpentMillis = now - 86_400_000L * 9
        ),
        ExpenseEntity(
            id = "e4",
            title = "PickMe Rides",
            amountLkr = 6200.0,
            category = "TRANSPORT",
            paymentAccount = "CARD_PRIMARY",
            commitmentType = "DISCRETIONARY",
            dateSpentMillis = now - 86_400_000L * 10
        )
    )
    return ExpenseAnalyticsSummary(
        selectedMonth = "November 2026",
        monthExpenses = sampleExpenses,
        outflowExpenses = sampleExpenses,
        totalExpenseLkr = 76700.0,
        rollingAverageLkr = 84000.0,
        committedLkr = 62000.0,
        discretionaryLkr = 14700.0,
        cardPrimaryTotalLkr = 23200.0,
        secondaryAccountTotalLkr = 45000.0,
        cashTotalLkr = 8500.0,
        categoryBreakdownItems = calculateCategoryBreakdown(sampleExpenses),
        filteredExpenses = sampleExpenses,
        groupedExpenses = mapOf(
            "This Week" to sampleExpenses.take(2),
            "Last Week" to sampleExpenses.drop(2)
        )
    )
}

private fun sampleSubscriptionSummary(): SubscriptionAuditSummary {
    val subs = listOf(
        SubscriptionEntity(id = "s1", name = "Fiber Internet", amountLkr = 7500.0, paymentAccount = "CARD_PRIMARY"),
        SubscriptionEntity(id = "s2", name = "Cloud Storage", amountLkr = 3200.0, paymentAccount = "ACCOUNT_SECONDARY")
    )
    return SubscriptionAuditSummary(
        activeSubs = subs,
        totalActiveLkr = 10700.0,
        secondaryTotalLkr = 3200.0,
        primaryCardTotalLkr = 7500.0,
        allCount = 2,
        filteredSubs = subs,
        nextSub = subs.first(),
        nextInDays = 4L
    )
}

@Preview(name = "Expenses - Portrait Light", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun ExpenseAnalyticsPreviewPortraitLight() {
    val summary = sampleExpenseSummary()
    val subSummary = sampleSubscriptionSummary()
    InTrackTheme {
        Surface {
            ExpenseAnalyticsContent(
                expensesState = UiState.Success(summary.monthExpenses),
                analyticsSummary = summary,
                subscriptionsState = UiState.Success(subSummary.activeSubs),
                auditSummary = subSummary,
                activeGoal = GoalEntity(title = "Emergency Fund", monthlyTargetRunRateLkr = 40833.33),
                selectedMonth = "November 2026",
                selectedTypeFilter = "ALL",
                selectedCategoryFilter = "ALL",
                selectedAccountFilter = "ALL",
                recentMonths = listOf("November 2026", "October 2026"),
                onSelectMonth = {},
                onSelectAccountFilter = {},
                onSelectTypeAndCategory = { _, _ -> },
                onToggleCategoryFilter = {},
                onClearFilters = {},
                onAddExpense = {},
                onUpdateExpense = {},
                onDeleteExpense = {},
                onRefreshExpenses = {},
                onAddSubscription = {},
                onUpdateSubscription = {},
                onDeleteSubscription = {},
                onRefreshSubscriptions = {},
                onNavigateToSettings = {},
                onNavigateToProfile = {},
                onSignOut = {}
            )
        }
    }
}

@Preview(name = "Expenses - Portrait Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, widthDp = 390, heightDp = 844)
@Composable
private fun ExpenseAnalyticsPreviewPortraitDark() {
    val summary = sampleExpenseSummary()
    val subSummary = sampleSubscriptionSummary()
    InTrackTheme(darkTheme = true) {
        Surface {
            ExpenseAnalyticsContent(
                expensesState = UiState.Success(summary.monthExpenses),
                analyticsSummary = summary,
                subscriptionsState = UiState.Success(subSummary.activeSubs),
                auditSummary = subSummary,
                activeGoal = GoalEntity(title = "Emergency Fund", monthlyTargetRunRateLkr = 40833.33),
                selectedMonth = "November 2026",
                selectedTypeFilter = "ALL",
                selectedCategoryFilter = "ALL",
                selectedAccountFilter = "ALL",
                recentMonths = listOf("November 2026", "October 2026"),
                onSelectMonth = {},
                onSelectAccountFilter = {},
                onSelectTypeAndCategory = { _, _ -> },
                onToggleCategoryFilter = {},
                onClearFilters = {},
                onAddExpense = {},
                onUpdateExpense = {},
                onDeleteExpense = {},
                onRefreshExpenses = {},
                onAddSubscription = {},
                onUpdateSubscription = {},
                onDeleteSubscription = {},
                onRefreshSubscriptions = {},
                onNavigateToSettings = {},
                onNavigateToProfile = {},
                onSignOut = {}
            )
        }
    }
}

@Preview(name = "Expenses - Landscape", showBackground = true, widthDp = 844, heightDp = 390)
@Composable
private fun ExpenseAnalyticsPreviewLandscape() {
    val summary = sampleExpenseSummary()
    val subSummary = sampleSubscriptionSummary()
    InTrackTheme {
        Surface {
            ExpenseAnalyticsContent(
                expensesState = UiState.Success(summary.monthExpenses),
                analyticsSummary = summary,
                subscriptionsState = UiState.Success(subSummary.activeSubs),
                auditSummary = subSummary,
                activeGoal = GoalEntity(title = "Emergency Fund", monthlyTargetRunRateLkr = 40833.33),
                selectedMonth = "November 2026",
                selectedTypeFilter = "ALL",
                selectedCategoryFilter = "ALL",
                selectedAccountFilter = "ALL",
                recentMonths = listOf("November 2026", "October 2026"),
                onSelectMonth = {},
                onSelectAccountFilter = {},
                onSelectTypeAndCategory = { _, _ -> },
                onToggleCategoryFilter = {},
                onClearFilters = {},
                onAddExpense = {},
                onUpdateExpense = {},
                onDeleteExpense = {},
                onRefreshExpenses = {},
                onAddSubscription = {},
                onUpdateSubscription = {},
                onDeleteSubscription = {},
                onRefreshSubscriptions = {},
                onNavigateToSettings = {},
                onNavigateToProfile = {},
                onSignOut = {}
            )
        }
    }
}

@Preview(name = "Hero Outflow Card", showBackground = true, widthDp = 360)
@Composable
private fun HeroOutflowCardPreview() {
    InTrackTheme {
        FigmaHeroOutflowCard(
            monthLabel = "November 2026",
            totalExpenseLkr = 76700.0,
            rollingAvgLkr = 84000.0,
            onOpenCharts = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
