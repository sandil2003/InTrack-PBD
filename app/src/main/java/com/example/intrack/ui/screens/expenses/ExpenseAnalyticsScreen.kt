package com.example.intrack.ui.screens.expenses

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.intrack.data.model.ExpenseEntity
import com.example.intrack.ui.common.EmptyStateView
import com.example.intrack.ui.common.UiState
import com.example.intrack.util.CurrencyFormatter
import com.example.intrack.util.DateUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Design tokens extracted directly from Figma (InTrack Design System)
private val FigmaCanvasBg = Color(0xFFFAF9F5)
private val FigmaDarkText = Color(0xFF1B1C1A)
private val FigmaMutedBrown = Color(0xFF6B5B53)
private val FigmaMutedSlate = Color(0xFF585F6B)
private val FigmaTerracotta = Color(0xFFB52705)
private val FigmaSoftPillActive = Color(0xFFF5DED4)
private val FigmaActiveText = Color(0xFF5B0D00)
private val FigmaCardBg = Color(0xFFF5F3EF)
private val FigmaIconCircleBg = Color(0xFFEFECE5)
private val FigmaBorder = Color(0x4DE3BEB6)
private val FigmaBorderActive = Color(0x80E3BEB6)
private val FigmaDivider = Color(0x33E3BEB6)
private val FigmaLifestyleBar = Color(0xFFC0C7D5)
private val FigmaTrackBg = Color(0xFFE4E2DE)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseAnalyticsScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: ExpenseViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val expensesState by viewModel.expensesState.collectAsState()
    val recentMonths = remember { DateUtils.getRecentMonthsList(count = 6) }
    var selectedMonth by remember { mutableStateOf(recentMonths.firstOrNull() ?: DateUtils.getCurrentMonthName()) }
    var selectedTypeFilter by remember { mutableStateOf("ALL") }
    var selectedCategoryFilter by remember { mutableStateOf("ALL") }
    var selectedAccountFilter by remember { mutableStateOf("ALL") }
    var showAddDialog by remember { mutableStateOf(false) }
    var showChartsModal by remember { mutableStateOf(false) }
    var showMonthPicker by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = FigmaCanvasBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Expense Analytics",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        ),
                        color = FigmaDarkText,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                },
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

                    // Button - Profile (Figma: 36x36 circular button)
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .clickable { /* Profile info */ },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonOutline,
                            contentDescription = "Profile",
                            modifier = Modifier.size(18.dp),
                            tint = FigmaDarkText
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = FigmaCanvasBg
                )
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            // Minimal Floating Action Button (Figma: 48x48 circular button, Terracotta red)
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = FigmaTerracotta,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .padding(bottom = 16.dp, end = 4.dp)
                    .size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add transaction",
                    modifier = Modifier.size(18.dp)
                )
            }
        },
        modifier = modifier
    ) { innerPadding ->
        if (showAddDialog) {
            AddExpenseDialog(
                onDismiss = { showAddDialog = false },
                onSubmit = { newExpense ->
                    viewModel.addExpense(newExpense.copy(userId = "guest_user"))
                    showAddDialog = false
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

        when (expensesState) {
            is UiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
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
                    onActionClick = { viewModel.loadExpenses("guest_user") },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            is UiState.Empty -> {
                EmptyStateView(
                    title = "No Expense Records Found",
                    message = "No expense data available to display analytics.",
                    actionLabel = "Log Expense",
                    onActionClick = { showAddDialog = true },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            is UiState.Success -> {
                val allExpenses = (expensesState as UiState.Success<List<ExpenseEntity>>).data

                // Filter expenses by the selected month
                val monthExpenses = allExpenses.filter {
                    DateUtils.matchesMonth(it.dateSpentMillis, selectedMonth)
                }

                val totalExpense = monthExpenses.sumOf { it.amountLkr }
                val committed = monthExpenses.filter { it.commitmentType == "COMMITTED" }.sumOf { it.amountLkr }
                val discretionary = monthExpenses.filter { it.commitmentType == "DISCRETIONARY" }.sumOf { it.amountLkr }

                val cardPrimaryTotal = monthExpenses.filter { it.paymentAccount.equals("CARD_PRIMARY", ignoreCase = true) }.sumOf { it.amountLkr }
                val secondaryAccountTotal = monthExpenses.filter { it.paymentAccount.equals("ACCOUNT_SECONDARY", ignoreCase = true) }.sumOf { it.amountLkr }
                val cashTotal = monthExpenses.filter { it.paymentAccount.equals("CASH", ignoreCase = true) }.sumOf { it.amountLkr }

                val categoryBreakdownItems = remember(monthExpenses) {
                    calculateCategoryBreakdown(monthExpenses)
                }

                val filteredExpenses = monthExpenses.filter { expense ->
                    val matchesType = when (selectedTypeFilter) {
                        "COMMITTED" -> expense.commitmentType == "COMMITTED"
                        "DISCRETIONARY" -> expense.commitmentType == "DISCRETIONARY"
                        else -> true
                    }
                    val matchesCategory = when (selectedCategoryFilter) {
                        "ALL" -> true
                        else -> expense.category.equals(selectedCategoryFilter, ignoreCase = true)
                    }
                    val matchesAccount = when (selectedAccountFilter) {
                        "ALL" -> true
                        else -> expense.paymentAccount.equals(selectedAccountFilter, ignoreCase = true)
                    }
                    matchesType && matchesCategory && matchesAccount
                }.sortedByDescending { it.dateSpentMillis }

                // Group transactions by date for Section - Streamlined Grouped Transactions Feed
                val groupedExpenses = remember(filteredExpenses) {
                    filteredExpenses.groupBy { formatRelativeDateGroup(it.dateSpentMillis) }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
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
                                        "UTILITIES" to "Utilities"
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

                    // Item 4: Transactions Feed (grouped by date with Figma rounded card)
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
                                FigmaTransactionCard(expense = expense)
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
}

/**
 * Modern Soft Hero Card matching Figma node 2001:69
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
    val total = committedLkr + discretionaryLkr
    val committedRatio = if (total > 0) (committedLkr / total).toFloat().coerceIn(0.01f, 0.99f) else 0.5f
    val discretionaryRatio = if (total > 0) (discretionaryLkr / total).toFloat().coerceIn(0.01f, 0.99f) else 0.5f

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = FigmaCardBg),
        border = BorderStroke(1.dp, FigmaBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Upper row: Outflow info on left, Charts button on right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${monthLabel.uppercase()} TOTAL OUTFLOW",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.55.sp
                        ),
                        color = FigmaMutedBrown
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "LKR",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = FigmaTerracotta,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                        Text(
                            text = CurrencyFormatter.formatLkr(totalExpenseLkr).replace("LKR ", ""),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-0.6).sp
                            ),
                            color = FigmaDarkText
                        )
                    }
                }

                // Button - View charts (Figma node 2001:76: 32x32 circular button)
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, FigmaBorder, CircleShape)
                        .clickable(onClick = onOpenCharts),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = "View charts",
                        tint = FigmaTerracotta,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Subtle divider line
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 1.dp,
                color = FigmaDivider
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 2-tone progress bar (6dp height)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(9999.dp))
                    .background(FigmaTrackBg)
            ) {
                if (total > 0) {
                    Box(
                        modifier = Modifier
                            .weight(committedRatio)
                            .fillMaxSize()
                            .background(FigmaTerracotta)
                    )
                    Box(
                        modifier = Modifier
                            .weight(discretionaryRatio)
                            .fillMaxSize()
                            .background(FigmaLifestyleBar)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Inline breakdown row (Figma node 2001:83)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Essentials
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(FigmaTerracotta)
                    )
                    Text(
                        text = "Essentials",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = FigmaMutedBrown
                    )
                    Text(
                        text = CurrencyFormatter.formatLkr(committedLkr).replace("LKR ", ""),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = FigmaDarkText
                    )
                }

                // Lifestyle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(FigmaLifestyleBar)
                    )
                    Text(
                        text = "Lifestyle",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = FigmaMutedBrown
                    )
                    Text(
                        text = CurrencyFormatter.formatLkr(discretionaryLkr).replace("LKR ", ""),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = FigmaDarkText
                    )
                }
            }
        }
    }
}

/**
 * Filter Pill matching standard application filter chips across pages
 */
@Composable
fun FigmaFilterPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp),
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        },
        shape = RoundedCornerShape(10.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
            containerColor = MaterialTheme.colorScheme.surface,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = isSelected,
            borderColor = MaterialTheme.colorScheme.outlineVariant,
            selectedBorderColor = MaterialTheme.colorScheme.primary
        ),
        modifier = modifier
    )
}

/**
 * Transaction Card matching Figma node 2001:136 (Background+Border+Shadow)
 */
@Composable
fun FigmaTransactionCard(
    expense: ExpenseEntity,
    modifier: Modifier = Modifier
) {
    val categoryIcon: ImageVector = when {
        expense.title.contains("ATM", ignoreCase = true) || (expense.paymentAccount.equals("CASH", ignoreCase = true) && expense.category.equals("MISC", ignoreCase = true)) -> Icons.Default.Payments
        expense.category.uppercase() == "TRANSPORT" -> Icons.Default.DirectionsCar
        expense.category.uppercase() == "DINING_OUT" -> Icons.Default.LocalCafe
        expense.category.uppercase() == "RENT" -> Icons.Default.Home
        expense.category.uppercase() == "MISC" -> Icons.Default.ShoppingCart
        else -> Icons.Default.Receipt
    }

    val accountLabel = when (expense.paymentAccount.uppercase()) {
        "CARD_PRIMARY" -> "Card Primary"
        "ACCOUNT_SECONDARY" -> "Bank Primary"
        "CASH" -> "Cash"
        else -> expense.paymentAccount.replace("_", " ")
    }

    val displayCategory = when (expense.category.uppercase()) {
        "DINING_OUT" -> "Dining"
        "TRANSPORT" -> "Transport"
        "RENT" -> "Housing"
        "SUBSCRIPTION" -> "Subscription"
        "UTILITIES" -> "Utilities"
        "MISC" -> "Groceries"
        else -> expense.category
    }

    val isCommitted = expense.commitmentType.equals("COMMITTED", ignoreCase = true)

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, FigmaBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Icon + Title & Account/Time Subtitle
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
                        imageVector = categoryIcon,
                        contentDescription = expense.category,
                        tint = FigmaDarkText,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

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
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Mini account badge (Figma node 2001:145)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(FigmaIconCircleBg)
                                .padding(horizontal = 6.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = accountLabel,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = FigmaMutedBrown
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
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Right: Amount + Category (Figma node 2001:149)
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "- LKR ${CurrencyFormatter.formatLkr(expense.amountLkr).replace("LKR ", "")}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.35).sp
                    ),
                    color = FigmaDarkText
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (isCommitted) "Essentials" else displayCategory,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = if (isCommitted) FontWeight.SemiBold else FontWeight.Medium
                    ),
                    color = if (isCommitted) FigmaTerracotta else FigmaMutedSlate
                )
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
