package com.example.intrack.ui.screens.expenses

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import com.example.intrack.ui.theme.EssentialsColor
import com.example.intrack.ui.theme.LifestyleColor
import com.example.intrack.ui.theme.TangerineShade1
import com.example.intrack.ui.theme.TangerineShade2
import com.example.intrack.ui.theme.TangerineShade3
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.rounded.Add
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.intrack.data.model.ExpenseEntity
import com.example.intrack.ui.common.EmptyStateView
import com.example.intrack.ui.common.UiState
import com.example.intrack.util.CurrencyFormatter
import com.example.intrack.util.DateUtils

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

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp),
                title = {
                    Text(
                        text = "Expense Analytics",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(start = 4.dp)
                    )
                },
                windowInsets = androidx.compose.material3.TopAppBarDefaults.windowInsets,
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Log Expense",
                    modifier = Modifier.size(24.dp)
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

        when (expensesState) {
            is UiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
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

                // Filter expenses by the selected month chip
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

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(22.dp)
                ) {
                    // Month Selector Chips (Left side is most recent)
                    item {
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            recentMonths.forEach { monthName ->
                                val isSelected = selectedMonth == monthName
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        selectedMonth = monthName
                                        selectedTypeFilter = "ALL"
                                        selectedCategoryFilter = "ALL"
                                        selectedAccountFilter = "ALL"
                                    },
                                    label = {
                                        Text(
                                            text = monthName,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    },
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
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        }
                    }

                    item {
                        TotalExpenseCard(
                            monthLabel = selectedMonth,
                            totalExpenseLkr = totalExpense,
                            committedLkr = committed,
                            discretionaryLkr = discretionary
                        )
                    }

                    item {
                        PaymentSourceDistributionCard(
                            monthLabel = selectedMonth,
                            totalExpenseLkr = totalExpense,
                            cardPrimaryTotal = cardPrimaryTotal,
                            secondaryAccountTotal = secondaryAccountTotal,
                            cashTotal = cashTotal
                        )
                    }

                    item {
                        CategoryBreakdownCard(
                            items = categoryBreakdownItems,
                            totalExpenseLkr = totalExpense,
                            selectedCategory = selectedCategoryFilter,
                            onSelectCategory = { catKey ->
                                selectedCategoryFilter = if (selectedCategoryFilter.equals(catKey, ignoreCase = true)) "ALL" else catKey
                            }
                        )
                    }

                    // Unified Transactions Section Header & Controls
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Header Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Transactions",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "${filteredExpenses.size} items",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // 1. Spending Nature Switcher (Essentials vs Lifestyle)
                            SingleChoiceSegmentedButtonRow(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                listOf(
                                    "ALL" to "All",
                                    "COMMITTED" to "Essentials",
                                    "DISCRETIONARY" to "Lifestyle"
                                ).forEachIndexed { index, (key, label) ->
                                    SegmentedButton(
                                        selected = selectedTypeFilter == key,
                                        onClick = { selectedTypeFilter = key },
                                        shape = SegmentedButtonDefaults.itemShape(index = index, count = 3),
                                        colors = SegmentedButtonDefaults.colors(
                                            activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                            activeContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                            inactiveContainerColor = MaterialTheme.colorScheme.surface,
                                            inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    ) {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = if (selectedTypeFilter == key) FontWeight.Bold else FontWeight.Medium
                                            )
                                        )
                                    }
                                }
                            }

                            // 2. Payment Account & Cash Filter Pills
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(
                                    "ALL" to "All Accounts",
                                    "CARD_PRIMARY" to "💳 Card Primary",
                                    "ACCOUNT_SECONDARY" to "🏦 Secondary",
                                    "CASH" to "💵 Cash"
                                ).forEach { (key, label) ->
                                    val isSelected = selectedAccountFilter == key
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { selectedAccountFilter = key },
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
                                        )
                                    )
                                }
                            }

                            // 3. Inline Category Filter Pills
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(
                                    "ALL" to "All Categories",
                                    "DINING_OUT" to "Dining",
                                    "TRANSPORT" to "Transport",
                                    "RENT" to "Housing",
                                    "SUBSCRIPTION" to "Subscriptions",
                                    "UTILITIES" to "Utilities",
                                    "MISC" to "Groceries"
                                ).forEach { (key, label) ->
                                    val isSelected = selectedCategoryFilter == key
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { selectedCategoryFilter = key },
                                        label = {
                                            Text(
                                                text = label,
                                                style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp),
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                            )
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                            containerColor = MaterialTheme.colorScheme.surface,
                                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(
                                            enabled = true,
                                            selected = isSelected,
                                            borderColor = MaterialTheme.colorScheme.outlineVariant,
                                            selectedBorderColor = MaterialTheme.colorScheme.secondary
                                        )
                                    )
                                }
                            }

                            // 3. Transactions List directly under filters with 10dp spacing
                            if (filteredExpenses.isEmpty()) {
                                EmptyStateView(
                                    title = "No matching expenses",
                                    message = "No transactions found with current filters."
                                )
                            } else {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    filteredExpenses.forEachIndexed { index, expense ->
                                        ExpenseDetailRow(expense = expense)
                                        if (index < filteredExpenses.lastIndex) {
                                            HorizontalDivider(
                                                modifier = Modifier.fillMaxWidth(),
                                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                                thickness = 1.dp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
            else -> {}
        }
    }
}

@Composable
fun TotalExpenseCard(
    monthLabel: String,
    totalExpenseLkr: Double,
    committedLkr: Double,
    discretionaryLkr: Double
) {
    val total = committedLkr + discretionaryLkr
    val committedRatio = if (total > 0) (committedLkr / total).toFloat().coerceIn(0.01f, 0.99f) else 0.5f
    val discretionaryRatio = if (total > 0) (discretionaryLkr / total).toFloat().coerceIn(0.01f, 0.99f) else 0.5f

    val committedPct = if (total > 0) ((committedLkr / total) * 100).toInt() else 0
    val discretionaryPct = if (total > 0) (100 - committedPct) else 0

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "${monthLabel.uppercase()} OUTFLOW",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.6.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = CurrencyFormatter.formatLkr(totalExpenseLkr),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp
                ),
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Two-tone continuous segmented bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (total > 0) {
                    Box(
                        modifier = Modifier
                            .weight(committedRatio)
                            .fillMaxSize()
                            .background(EssentialsColor)
                    )
                    Box(
                        modifier = Modifier
                            .weight(discretionaryRatio)
                            .fillMaxSize()
                            .background(LifestyleColor)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Numbers & Legend (title and cost in two lines)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(EssentialsColor)
                        )
                        Text(
                            text = "Essentials",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${CurrencyFormatter.formatLkr(committedLkr)} ($committedPct%)",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(LifestyleColor)
                        )
                        Text(
                            text = "Lifestyle",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${CurrencyFormatter.formatLkr(discretionaryLkr)} ($discretionaryPct%)",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun PaymentSourceDistributionCard(
    monthLabel: String,
    totalExpenseLkr: Double,
    cardPrimaryTotal: Double,
    secondaryAccountTotal: Double,
    cashTotal: Double
) {
    val total = if (totalExpenseLkr > 0) totalExpenseLkr else 1.0
    val cardRatio = (cardPrimaryTotal / total).toFloat().coerceIn(0f, 1f)
    val secondaryRatio = (secondaryAccountTotal / total).toFloat().coerceIn(0f, 1f)
    val cashRatio = (cashTotal / total).toFloat().coerceIn(0f, 1f)

    val cardPct = if (totalExpenseLkr > 0) ((cardPrimaryTotal / total) * 100).toInt() else 0
    val secondaryPct = if (totalExpenseLkr > 0) ((secondaryAccountTotal / total) * 100).toInt() else 0
    val cashPct = if (totalExpenseLkr > 0) ((cashTotal / total) * 100).toInt() else 0

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PAYMENT SOURCES & CASH TRACKING",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp
                )
                if (cashTotal > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(TangerineShade3.copy(alpha = 0.25f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Cash Monitored",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                            color = TangerineShade2
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Multi-segment horizontal distribution bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (cardRatio > 0f) {
                    Box(
                        modifier = Modifier
                            .weight(cardRatio.coerceAtLeast(0.01f))
                            .fillMaxSize()
                            .background(TangerineShade2)
                    )
                }
                if (secondaryRatio > 0f) {
                    Box(
                        modifier = Modifier
                            .weight(secondaryRatio.coerceAtLeast(0.01f))
                            .fillMaxSize()
                            .background(TangerineShade1)
                    )
                }
                if (cashRatio > 0f) {
                    Box(
                        modifier = Modifier
                            .weight(cashRatio.coerceAtLeast(0.01f))
                            .fillMaxSize()
                            .background(TangerineShade3)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Three column stats: Card Primary, Secondary Account, Cash
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(TangerineShade2)
                        )
                        Text(
                            text = "Card Primary",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = CurrencyFormatter.formatLkr(cardPrimaryTotal),
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.5.sp, fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$cardPct% of outflow",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(TangerineShade1)
                        )
                        Text(
                            text = "Secondary",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = CurrencyFormatter.formatLkr(secondaryAccountTotal),
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.5.sp, fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$secondaryPct% of outflow",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(TangerineShade3)
                        )
                        Text(
                            text = "Cash (ATM)",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = CurrencyFormatter.formatLkr(cashTotal),
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.5.sp, fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$cashPct% of outflow",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun ExpenseDetailRow(expense: ExpenseEntity) {
    val categoryIcon: ImageVector = when {
        expense.title.contains("ATM", ignoreCase = true) -> Icons.Default.Payments
        expense.category.uppercase() == "TRANSPORT" -> Icons.Default.DirectionsCar
        expense.category.uppercase() == "DINING_OUT" -> Icons.Default.LocalCafe
        expense.category.uppercase() == "RENT" -> Icons.Default.Home
        expense.category.uppercase() == "MISC" && expense.paymentAccount.uppercase() == "CASH" -> Icons.Default.Payments
        expense.category.uppercase() == "MISC" -> Icons.Default.ShoppingCart
        else -> Icons.Default.Receipt
    }

    val (badgeBg, badgeText, accountLabel) = when (expense.paymentAccount.uppercase()) {
        "CARD_PRIMARY" -> Triple(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
            MaterialTheme.colorScheme.onPrimaryContainer,
            "Card Primary"
        )
        "ACCOUNT_SECONDARY" -> Triple(
            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f),
            MaterialTheme.colorScheme.onSecondaryContainer,
            "Secondary Account"
        )
        "CASH" -> Triple(
            TangerineShade3.copy(alpha = 0.25f),
            TangerineShade2,
            "Cash"
        )
        else -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            expense.paymentAccount.replace("_", " ")
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = categoryIcon,
                    contentDescription = expense.category,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(verticalArrangement = Arrangement.Center) {
                Text(
                    text = expense.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(badgeBg)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = accountLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = badgeText
                        )
                    }

                    Text(
                        text = "•  ${DateUtils.formatDate(expense.dateSpentMillis)}",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
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
            color = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
fun ExpenseDetailCard(expense: ExpenseEntity) {
    ExpenseDetailRow(expense = expense)
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
