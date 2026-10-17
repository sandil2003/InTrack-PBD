package com.example.intrack.ui.screens.transactions

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.intrack.data.model.ExpenseEntity
import com.example.intrack.data.model.IncomeEntity
import com.example.intrack.data.repository.ExpenseRepository
import com.example.intrack.data.repository.ExpenseRepositoryImpl
import com.example.intrack.data.repository.IncomeRepository
import com.example.intrack.data.repository.IncomeRepositoryImpl
import com.example.intrack.ui.common.EmptyStateView
import com.example.intrack.ui.common.UiState
import com.example.intrack.ui.theme.ExpenseNegative
import com.example.intrack.ui.theme.IncomePositive
import com.example.intrack.ui.theme.PrimaryGreen
import com.example.intrack.ui.theme.TangerineShade2
import com.example.intrack.ui.theme.TangerineShade3
import com.example.intrack.util.CurrencyFormatter
import com.example.intrack.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class AllTransactionsData(
    val incomes: List<IncomeEntity> = emptyList(),
    val expenses: List<ExpenseEntity> = emptyList()
)

class AllTransactionsViewModel(
    private val incomeRepository: IncomeRepository = IncomeRepositoryImpl(),
    private val expenseRepository: ExpenseRepository = ExpenseRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<AllTransactionsData>>(UiState.Loading)
    val uiState: StateFlow<UiState<AllTransactionsData>> = _uiState

    init {
        loadData("guest_user")
    }

    fun loadData(userId: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            combine(
                incomeRepository.getIncomes(userId),
                expenseRepository.getExpenses(userId)
            ) { incomes, expenses ->
                AllTransactionsData(incomes = incomes, expenses = expenses)
            }.collect { data ->
                _uiState.value = UiState.Success(data)
            }
        }
    }
}

data class UnifiedTransaction(
    val id: String,
    val title: String,
    val amount: Double,
    val isIncome: Boolean,
    val timestampMillis: Long,
    val categoryOrChannel: String,
    val accountOrClient: String,
    val paymentAccount: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllTransactionsScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: AllTransactionsViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val recentMonths = remember { DateUtils.getRecentMonthsList(count = 6) }
    var selectedMonth by remember { mutableStateOf(recentMonths.firstOrNull() ?: DateUtils.getCurrentMonthName()) }
    var selectedTypeFilter by remember { mutableStateOf("ALL") } // "ALL", "EXPENSES", "INCOME"
    var selectedAccountFilter by remember { mutableStateOf("ALL") } // "ALL", "CARD_PRIMARY", "ACCOUNT_SECONDARY", "CASH"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "All Transactions",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(start = 4.dp)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
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
                ) { CircularProgressIndicator() }
            }
            is UiState.Error -> {
                val errorMsg = (uiState as UiState.Error).message
                EmptyStateView(
                    title = "Error Loading Transactions",
                    message = errorMsg,
                    actionLabel = "Retry",
                    onActionClick = { viewModel.loadData("guest_user") },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            is UiState.Empty -> {
                EmptyStateView(
                    title = "No Transactions Found",
                    message = "No transactions recorded yet.",
                    modifier = Modifier.padding(innerPadding)
                )
            }
            is UiState.Success -> {
                val data = (uiState as UiState.Success<AllTransactionsData>).data

                // Monthly filtered incomes & expenses
                val monthlyIncomes = remember(data.incomes, selectedMonth) {
                    data.incomes.filter { DateUtils.matchesMonth(it.dateReceivedMillis, selectedMonth) }
                }
                val monthlyExpenses = remember(data.expenses, selectedMonth) {
                    data.expenses.filter { DateUtils.matchesMonth(it.dateSpentMillis, selectedMonth) }
                }

                val monthInflow = monthlyIncomes.sumOf { it.realizedAmountLkr }
                val monthOutflow = monthlyExpenses.sumOf { it.amountLkr }
                val netFlow = monthInflow - monthOutflow

                // Combine into unified list
                val allUnified = remember(monthlyIncomes, monthlyExpenses) {
                    val list = mutableListOf<UnifiedTransaction>()
                    monthlyIncomes.forEach { inc ->
                        list.add(
                            UnifiedTransaction(
                                id = inc.id,
                                title = inc.title,
                                amount = inc.realizedAmountLkr,
                                isIncome = true,
                                timestampMillis = inc.dateReceivedMillis,
                                categoryOrChannel = inc.channel,
                                accountOrClient = inc.clientName ?: inc.channel.lowercase().replaceFirstChar { it.uppercase() }
                            )
                        )
                    }
                    monthlyExpenses.forEach { exp ->
                        list.add(
                            UnifiedTransaction(
                                id = exp.id,
                                title = exp.title,
                                amount = exp.amountLkr,
                                isIncome = false,
                                timestampMillis = exp.dateSpentMillis,
                                categoryOrChannel = exp.category,
                                accountOrClient = when (exp.paymentAccount.uppercase()) {
                                    "CARD_PRIMARY" -> "Card Primary"
                                    "ACCOUNT_SECONDARY" -> "Secondary Account"
                                    "CASH" -> "Cash"
                                    else -> exp.paymentAccount.replace("_", " ")
                                },
                                paymentAccount = exp.paymentAccount.uppercase()
                            )
                        )
                    }
                    list.sortedByDescending { it.timestampMillis }
                }

                // Filter by type and account
                val filteredTransactions = remember(allUnified, selectedTypeFilter, selectedAccountFilter) {
                    allUnified.filter { tx ->
                        val matchesType = when (selectedTypeFilter) {
                            "EXPENSES" -> !tx.isIncome
                            "INCOME" -> tx.isIncome
                            else -> true
                        }
                        val matchesAccount = when (selectedAccountFilter) {
                            "ALL" -> true
                            "CASH" -> !tx.isIncome && tx.paymentAccount == "CASH"
                            "CARD_PRIMARY" -> !tx.isIncome && tx.paymentAccount == "CARD_PRIMARY"
                            "ACCOUNT_SECONDARY" -> !tx.isIncome && tx.paymentAccount == "ACCOUNT_SECONDARY"
                            else -> true
                        }
                        matchesType && matchesAccount
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(22.dp)
                ) {
                    // 1. Month Selector Tabs (Same style as Income & Expense views)
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

                    // 2. Main Month Summary Card (Unified style)
                    item {
                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Text(
                                    text = "${selectedMonth.uppercase()} NET ACTIVITY",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.6.sp
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = CurrencyFormatter.formatLkr(netFlow),
                                    style = MaterialTheme.typography.headlineLarge.copy(
                                        fontSize = 30.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = (-0.5).sp
                                    ),
                                    color = if (netFlow >= 0) IncomePositive else MaterialTheme.colorScheme.error
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                // Proportional Bar (Inflow vs Outflow)
                                val totalActivity = (monthInflow + monthOutflow).coerceAtLeast(1.0)
                                val inflowRatio = (monthInflow / totalActivity).toFloat().coerceIn(0f, 1f)
                                val outflowRatio = (monthOutflow / totalActivity).toFloat().coerceIn(0f, 1f)

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(10.dp)
                                        .clip(RoundedCornerShape(5.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                 ) {
                                     if (inflowRatio > 0f) {
                                         Box(
                                             modifier = Modifier
                                                 .weight(inflowRatio.coerceAtLeast(0.01f))
                                                 .fillMaxSize()
                                                 .background(IncomePositive)
                                         )
                                     }
                                     if (outflowRatio > 0f) {
                                         Box(
                                             modifier = Modifier
                                                 .weight(outflowRatio.coerceAtLeast(0.01f))
                                                 .fillMaxSize()
                                                 .background(ExpenseNegative)
                                         )
                                     }
                                 }

                                 Spacer(modifier = Modifier.height(12.dp))

                                 // Two-line Legend and Numbers
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
                                                     .background(IncomePositive)
                                             )
                                             Text(
                                                 text = "Total Inflow",
                                                 style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                                 color = MaterialTheme.colorScheme.onSurfaceVariant
                                             )
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "+${CurrencyFormatter.formatLkr(monthInflow)}",
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
                                                    .background(ExpenseNegative)
                                            )
                                            Text(
                                                text = "Total Outflow",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "-${CurrencyFormatter.formatLkr(monthOutflow)}",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 3. Type Switcher & Transaction Count
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
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
                                    text = "${filteredTransactions.size} items",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            val filterOptions = listOf("ALL" to "All", "EXPENSES" to "Outflow", "INCOME" to "Inflow")
                            SingleChoiceSegmentedButtonRow(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                filterOptions.forEachIndexed { index, (key, label) ->
                                    SegmentedButton(
                                        shape = SegmentedButtonDefaults.itemShape(index = index, count = filterOptions.size),
                                        onClick = { selectedTypeFilter = key },
                                        selected = selectedTypeFilter == key,
                                        label = {
                                            Text(
                                                text = label,
                                                fontWeight = if (selectedTypeFilter == key) FontWeight.Bold else FontWeight.Medium
                                            )
                                        }
                                    )
                                }
                            }

                            // Payment Account Filter Pills (Card, Secondary, Cash)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(
                                    "ALL" to "All Sources",
                                    "CARD_PRIMARY" to "💳 Card Primary",
                                    "ACCOUNT_SECONDARY" to "🏦 Secondary",
                                    "CASH" to "💵 Cash"
                                ).forEach { (key, label) ->
                                    val isSelected = selectedAccountFilter == key
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            selectedAccountFilter = key
                                            if (key != "ALL" && selectedTypeFilter == "INCOME") {
                                                selectedTypeFilter = "EXPENSES"
                                            }
                                        },
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

                            // 4. Transaction Items List (tight 10dp under filters)
                            if (filteredTransactions.isEmpty()) {
                                EmptyStateView(
                                    title = "No transactions found",
                                    message = "No records matching the filter in $selectedMonth."
                                )
                            } else {
                                OutlinedCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        filteredTransactions.forEachIndexed { index, tx ->
                                            UnifiedTransactionRow(transaction = tx)
                                            if (index < filteredTransactions.lastIndex) {
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
fun UnifiedTransactionRow(transaction: UnifiedTransaction) {
    val icon: ImageVector = if (transaction.isIncome) {
        Icons.AutoMirrored.Filled.TrendingUp
    } else {
        when {
            transaction.title.contains("ATM", ignoreCase = true) -> Icons.Default.Payments
            transaction.categoryOrChannel.uppercase() == "TRANSPORT" -> Icons.Default.DirectionsCar
            transaction.categoryOrChannel.uppercase() == "DINING_OUT" -> Icons.Default.LocalCafe
            transaction.categoryOrChannel.uppercase() == "RENT" -> Icons.Default.Home
            transaction.categoryOrChannel.uppercase() == "MISC" && transaction.paymentAccount == "CASH" -> Icons.Default.Payments
            transaction.categoryOrChannel.uppercase() == "MISC" -> Icons.Default.ShoppingCart
            else -> Icons.Default.Receipt
        }
    }

    val iconBg = if (transaction.isIncome) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val iconTint = if (transaction.isIncome) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 13.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (transaction.paymentAccount == "CASH") {
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
                            text = transaction.accountOrClient,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = DateUtils.formatDate(transaction.timestampMillis),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = if (transaction.isIncome) {
                "+${CurrencyFormatter.formatLkr(transaction.amount)}"
            } else {
                "-${CurrencyFormatter.formatLkr(transaction.amount)}"
            },
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = if (transaction.isIncome) IncomePositive else MaterialTheme.colorScheme.onSurface
        )
    }
}
