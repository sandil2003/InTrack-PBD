package com.example.intrack.ui.screens.income

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CurrencyBitcoin
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Laptop
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.intrack.data.model.IncomeEntity
import com.example.intrack.ui.common.EmptyStateView
import com.example.intrack.ui.common.UiState
import com.example.intrack.ui.components.WindfallAllocatorModal
import com.example.intrack.ui.theme.BrandTangerine
import com.example.intrack.ui.theme.IncomePositive
import com.example.intrack.ui.theme.PrimaryGreen
import com.example.intrack.ui.theme.TangerineShade1
import com.example.intrack.ui.theme.TangerineShade2
import com.example.intrack.ui.theme.TangerineShade3
import com.example.intrack.ui.theme.TangerineShade4
import com.example.intrack.util.CurrencyFormatter
import com.example.intrack.util.DateUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncomeHubScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToGoal: () -> Unit = {},
    viewModel: IncomeViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val incomesState by viewModel.incomesState.collectAsState()
    val recentMonths = remember { DateUtils.getRecentMonthsList(count = 6) }
    var selectedMonth by remember { mutableStateOf(recentMonths.firstOrNull() ?: DateUtils.getCurrentMonthName()) }

    var showChannelPickerModal by remember { mutableStateOf(false) }
    var activeModalChannel by remember { mutableStateOf<String?>(null) }
    var pendingWindfallAmount by remember { mutableDoubleStateOf(0.0) }
    var showWindfallModal by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp),
                title = {
                    Text(
                        text = "Income Streams",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(start = 4.dp)
                    )
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
        when (incomesState) {
            is UiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }
            is UiState.Error -> {
                val errorMsg = (incomesState as UiState.Error).message
                EmptyStateView(
                    title = "Error Loading Incomes",
                    message = errorMsg,
                    onActionClick = {
                        val currentUid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: "guest_user"
                        viewModel.loadIncomes(currentUid)
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            is UiState.Empty -> {
                EmptyStateView(
                    title = "No Income Streams Found",
                    message = "You have no income records yet. Log your first stream to start tracking.",
                    actionLabel = "Log Stream",
                    onActionClick = { showChannelPickerModal = true },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            is UiState.Success -> {
                val allIncomes = (incomesState as UiState.Success<List<IncomeEntity>>).data

                // 1. All Active Pending Invoices (Cross-Month Receivables)
                val allPendingInvoices = allIncomes.filter {
                    it.channel == "FREELANCE" && (it.status == "INVOICED" || it.status == "OVERDUE")
                }.sortedByDescending { it.dateReceivedMillis }

                val totalPendingReceivablesLkr = allPendingInvoices.sumOf { it.realizedAmountLkr }

                // 2. Filter by the currently selected month chip
                val monthIncomes = allIncomes.filter {
                    DateUtils.matchesMonth(it.dateReceivedMillis, selectedMonth)
                }

                // Month-Specific Stream Breakdowns (Only Paid Realized Amounts)
                val salaryIncomes = monthIncomes.filter { it.channel == "SALARY" && it.status == "PAID" }
                val paidFreelanceInMonth = monthIncomes.filter { it.channel == "FREELANCE" && it.status == "PAID" }
                val adsenseIncomes = monthIncomes.filter { it.channel == "ADSENSE" && it.status == "PAID" }
                val cryptoIncomes = monthIncomes.filter { it.channel == "CRYPTO" && it.status == "PAID" }

                val totalSalary = salaryIncomes.sumOf { it.realizedAmountLkr }
                val totalFreelancePaidThisMonth = paidFreelanceInMonth.sumOf { it.realizedAmountLkr }
                val totalAdsense = adsenseIncomes.sumOf { it.realizedAmountLkr }
                val totalCrypto = cryptoIncomes.sumOf { it.realizedAmountLkr }

                val totalMonthRealized = totalSalary + totalFreelancePaidThisMonth + totalAdsense + totalCrypto

                // 3-Month Rolling Average across paid history
                val rollingAverage = if (allIncomes.isNotEmpty()) {
                    val allPaid = allIncomes.filter { it.status == "PAID" }.sumOf { it.realizedAmountLkr }
                    (allPaid / 3.0).coerceAtLeast(120000.0)
                } else 120000.0

                // Freelance paid history for selected month
                val freelancePaidHistory = paidFreelanceInMonth.sortedByDescending { it.dateReceivedMillis }

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

                    // Month Realized Inflow Hero Card & Distribution Bar
                    item {
                        TotalInflowBreakdownCard(
                            monthLabel = selectedMonth,
                            totalInflowLkr = totalMonthRealized,
                            rollingAvgLkr = rollingAverage,
                            salaryTotal = totalSalary,
                            freelanceTotal = totalFreelancePaidThisMonth,
                            adsenseTotal = totalAdsense,
                            cryptoTotal = totalCrypto
                        )
                    }

                    // 4 Channel Overview Tiles for Selected Month
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "$selectedMonth Channels",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                ChannelOverviewTile(
                                    title = "Salary",
                                    amountText = CurrencyFormatter.formatLkr(totalSalary),
                                    accentColor = BrandTangerine,
                                    icon = Icons.Rounded.Work,
                                    isLogged = totalSalary > 0,
                                    modifier = Modifier.weight(1f),
                                    onClick = { activeModalChannel = "SALARY" }
                                )
                                ChannelOverviewTile(
                                    title = "Freelance",
                                    amountText = CurrencyFormatter.formatLkr(totalFreelancePaidThisMonth),
                                    accentColor = BrandTangerine,
                                    icon = Icons.Rounded.Laptop,
                                    isLogged = totalFreelancePaidThisMonth > 0,
                                    modifier = Modifier.weight(1f),
                                    onClick = { activeModalChannel = "FREELANCE" }
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                ChannelOverviewTile(
                                    title = "Google AdSense",
                                    amountText = CurrencyFormatter.formatLkr(totalAdsense),
                                    accentColor = BrandTangerine,
                                    icon = Icons.Rounded.AttachMoney,
                                    isLogged = totalAdsense > 0,
                                    modifier = Modifier.weight(1f),
                                    onClick = { activeModalChannel = "ADSENSE" }
                                )
                                ChannelOverviewTile(
                                    title = "Crypto Trading",
                                    amountText = if (totalCrypto >= 0) "+${CurrencyFormatter.formatLkr(totalCrypto)}" else "-${CurrencyFormatter.formatLkr(-totalCrypto)}",
                                    accentColor = BrandTangerine,
                                    icon = Icons.Rounded.CurrencyBitcoin,
                                    isLogged = totalCrypto != 0.0,
                                    modifier = Modifier.weight(1f),
                                    onClick = { activeModalChannel = "CRYPTO" }
                                )
                            }
                        }
                    }

                    // PENDING INVOICES & RECEIVABLES SECTION (Cross-Month Tracker)
                    item {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Pending Client Invoices",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                if (allPendingInvoices.isNotEmpty()) {
                                    Text(
                                        text = "${allPendingInvoices.size} Unpaid • ${CurrencyFormatter.formatLkr(totalPendingReceivablesLkr)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            if (allPendingInvoices.isNotEmpty()) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    allPendingInvoices.forEach { invoice ->
                                        PendingInvoiceCard(
                                            invoice = invoice,
                                            onSendReminder = {
                                                coroutineScope.launch {
                                                    snackbarHostState.showSnackbar(
                                                        message = "Invoice reminder sent to ${invoice.clientName ?: "client"} for '${invoice.title}'!",
                                                        duration = SnackbarDuration.Short
                                                    )
                                                }
                                            },
                                            onMarkAsPaid = {
                                                val settledInvoice = invoice.copy(
                                                    status = "PAID",
                                                    dateReceivedMillis = System.currentTimeMillis()
                                                )
                                                viewModel.updateIncome(settledInvoice)
                                                pendingWindfallAmount = settledInvoice.realizedAmountLkr
                                                showWindfallModal = true
                                                coroutineScope.launch {
                                                    snackbarHostState.showSnackbar(
                                                        message = "Settled invoice '${invoice.title}' (+${CurrencyFormatter.formatLkr(invoice.realizedAmountLkr)})",
                                                        duration = SnackbarDuration.Short
                                                    )
                                                }
                                            },
                                            onDeleteInvoice = {
                                                viewModel.deleteIncome(invoice.id)
                                                coroutineScope.launch {
                                                    snackbarHostState.showSnackbar(
                                                        message = "Deleted invoice '${invoice.title}'",
                                                        duration = SnackbarDuration.Short
                                                    )
                                                }
                                            }
                                        )
                                    }
                                }
                            } else {
                                OutlinedCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.CheckCircle,
                                            contentDescription = null,
                                            tint = PrimaryGreen,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = "All client invoices settled — no pending receivables.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // FREELANCE PAID HISTORY IN THIS MONTH
                    item {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Freelance Paid History in $selectedMonth",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            if (freelancePaidHistory.isEmpty()) {
                                EmptyStateView(
                                    title = "No freelance inflows for $selectedMonth",
                                    message = "No settled freelance invoices recorded in this month."
                                )
                            } else {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    freelancePaidHistory.forEachIndexed { index, income ->
                                        InflowHistoryRow(income = income)
                                        if (index < freelancePaidHistory.lastIndex) {
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
                        Spacer(modifier = Modifier.height(84.dp))
                    }
                }
            }
            else -> {}
        }
    }

    // Modal Dialogs for Monthly / Channel Inflows
    if (showChannelPickerModal) {
        ChannelPickerModal(
            targetMonth = selectedMonth,
            onDismiss = { showChannelPickerModal = false },
            onSelectChannel = { channelKey ->
                showChannelPickerModal = false
                activeModalChannel = channelKey
            }
        )
    }

    when (activeModalChannel) {
        "SALARY" -> {
            MonthlySalaryDialog(
                defaultMonthName = selectedMonth,
                onDismiss = { activeModalChannel = null },
                onSubmit = { base, bonus, monthName ->
                    val total = base + bonus
                    viewModel.addIncome(
                        IncomeEntity(
                            title = "$monthName Salary (Base + Bonus)",
                            channel = "SALARY",
                            originalAmount = total,
                            originalCurrency = "LKR",
                            realizedAmountLkr = total,
                            status = "PAID"
                        )
                    )
                    activeModalChannel = null
                    if (bonus > 0) {
                        pendingWindfallAmount = bonus
                        showWindfallModal = true
                    } else {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Logged $monthName salary (+${CurrencyFormatter.formatLkr(total)})")
                        }
                    }
                }
            )
        }
        "ADSENSE" -> {
            MonthlyAdSenseDialog(
                defaultMonthName = selectedMonth,
                onDismiss = { activeModalChannel = null },
                onSubmit = { usd, rate, realizedLkr, monthName ->
                    viewModel.addIncome(
                        IncomeEntity(
                            title = "$monthName AdSense Payout ($$usd)",
                            channel = "ADSENSE",
                            originalAmount = usd,
                            originalCurrency = "USD",
                            exchangeRate = rate,
                            realizedAmountLkr = realizedLkr,
                            status = "PAID"
                        )
                    )
                    activeModalChannel = null
                    pendingWindfallAmount = realizedLkr
                    showWindfallModal = true
                }
            )
        }
        "FREELANCE" -> {
            FreelanceMilestoneDialog(
                onDismiss = { activeModalChannel = null },
                onSubmit = { title, client, amount, status ->
                    val newIncome = IncomeEntity(
                        title = title,
                        channel = "FREELANCE",
                        clientName = client,
                        originalAmount = amount,
                        originalCurrency = "LKR",
                        realizedAmountLkr = amount,
                        status = status,
                        dateReceivedMillis = System.currentTimeMillis()
                    )
                    viewModel.addIncome(newIncome)
                    activeModalChannel = null
                    if (status == "PAID") {
                        pendingWindfallAmount = amount
                        showWindfallModal = true
                    } else {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Created pending invoice for $client (${CurrencyFormatter.formatLkr(amount)})")
                        }
                    }
                }
            )
        }
        "CRYPTO" -> {
            CryptoPnlDialog(
                onDismiss = { activeModalChannel = null },
                onSubmit = { capital, withdraw, netPnl ->
                    viewModel.addIncome(
                        IncomeEntity(
                            title = "Crypto Realized P&L (${if (netPnl >= 0) "+${CurrencyFormatter.formatLkr(netPnl)}" else "-${CurrencyFormatter.formatLkr(-netPnl)}"})",
                            channel = "CRYPTO",
                            originalAmount = netPnl,
                            originalCurrency = "LKR",
                            realizedAmountLkr = netPnl,
                            status = "PAID"
                        )
                    )
                    activeModalChannel = null
                    if (netPnl > 0) {
                        pendingWindfallAmount = netPnl
                        showWindfallModal = true
                    } else {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Logged crypto net position (${CurrencyFormatter.formatLkr(netPnl)})")
                        }
                    }
                }
            )
        }
    }

    if (showWindfallModal && pendingWindfallAmount > 0) {
        WindfallAllocatorModal(
            windfallAmountLkr = pendingWindfallAmount,
            onConfirmAllocation = { allocatedLkr ->
                showWindfallModal = false
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Diverted ${CurrencyFormatter.formatLkr(allocatedLkr)} directly to MacBook Pro M4 Fund! 🎉",
                        actionLabel = "View Goal",
                        duration = SnackbarDuration.Long
                    )
                }
            },
            onDismiss = { showWindfallModal = false }
        )
    }
}

// -----------------------------------------------------------------------------------------
// COMPOSABLE SUB-COMPONENTS
// -----------------------------------------------------------------------------------------

@Composable
fun TotalInflowBreakdownCard(
    monthLabel: String,
    totalInflowLkr: Double,
    rollingAvgLkr: Double,
    salaryTotal: Double,
    freelanceTotal: Double,
    adsenseTotal: Double,
    cryptoTotal: Double
) {
    val total = if (totalInflowLkr > 0) totalInflowLkr else 1.0
    val salaryFraction = (salaryTotal / total).toFloat().coerceIn(0f, 1f)
    val freelanceFraction = (freelanceTotal / total).toFloat().coerceIn(0f, 1f)
    val adsenseFraction = (adsenseTotal / total).toFloat().coerceIn(0f, 1f)
    val cryptoFraction = (cryptoTotal / total).toFloat().coerceIn(0f, 1f)

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "${monthLabel.uppercase()} REALIZED INFLOW",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.6.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = CurrencyFormatter.formatLkr(totalInflowLkr),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp
                ),
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Multi-Channel Distribution Bar for selected month
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (salaryFraction > 0f) {
                    Box(
                        modifier = Modifier
                            .weight(salaryFraction.coerceAtLeast(0.01f))
                            .fillMaxSize()
                            .background(TangerineShade1)
                    )
                }
                if (freelanceFraction > 0f) {
                    Box(
                        modifier = Modifier
                            .weight(freelanceFraction.coerceAtLeast(0.01f))
                            .fillMaxSize()
                            .background(TangerineShade2)
                    )
                }
                if (adsenseFraction > 0f) {
                    Box(
                        modifier = Modifier
                            .weight(adsenseFraction.coerceAtLeast(0.01f))
                            .fillMaxSize()
                            .background(TangerineShade3)
                    )
                }
                if (cryptoFraction > 0f) {
                    Box(
                        modifier = Modifier
                            .weight(cryptoFraction.coerceAtLeast(0.01f))
                            .fillMaxSize()
                            .background(TangerineShade4)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Channel Legend with numbers (title and cost in two lines)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    LegendNumberItem(color = TangerineShade1, label = "Salary", amount = salaryTotal, modifier = Modifier.weight(1f))
                    LegendNumberItem(color = TangerineShade2, label = "Freelance", amount = freelanceTotal, modifier = Modifier.weight(1f))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    LegendNumberItem(color = TangerineShade3, label = "AdSense", amount = adsenseTotal, modifier = Modifier.weight(1f))
                    LegendNumberItem(color = TangerineShade4, label = "Crypto", amount = cryptoTotal, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun LegendNumberItem(
    color: Color,
    label: String,
    amount: Double,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = CurrencyFormatter.formatLkr(amount),
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun ChannelOverviewTile(
    title: String,
    amountText: String,
    accentColor: Color,
    icon: ImageVector,
    isLogged: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            1.dp,
            if (isLogged) accentColor.copy(alpha = 0.25f) else MaterialTheme.colorScheme.outlineVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Left: Icon container
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Right: Title & Price
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                if (isLogged) {
                    Text(
                        text = amountText,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.3).sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "Log Stream",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = accentColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PendingInvoiceCard(
    invoice: IncomeEntity,
    onSendReminder: () -> Unit,
    onMarkAsPaid: () -> Unit,
    onDeleteInvoice: (() -> Unit)? = null
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Description,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = invoice.clientName ?: "Direct Client",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = invoice.title,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = CurrencyFormatter.formatLkr(invoice.realizedAmountLkr),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (onDeleteInvoice != null) {
                        IconButton(
                            onClick = onDeleteInvoice,
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.DeleteOutline,
                                contentDescription = "Delete Invoice",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onSendReminder,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(imageVector = Icons.Rounded.NotificationsActive, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Remind Client", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onMarkAsPaid,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Mark Received", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun InflowHistoryRow(
    income: IncomeEntity
) {
    val (icon, tint) = when (income.channel) {
        "SALARY" -> Icons.Rounded.Work to BrandTangerine
        "FREELANCE" -> Icons.Rounded.Laptop to BrandTangerine
        "ADSENSE" -> Icons.Rounded.AttachMoney to BrandTangerine
        else -> Icons.Rounded.CurrencyBitcoin to BrandTangerine
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
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(tint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = income.channel,
                    tint = tint,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = income.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                val subtitle = if (income.channel == "FREELANCE" && !income.clientName.isNullOrEmpty()) {
                    "Client: ${income.clientName} • Paid ${DateUtils.formatDate(income.dateReceivedMillis)}"
                } else {
                    "${income.channel.lowercase().replaceFirstChar { it.uppercase() }} • ${DateUtils.formatDate(income.dateReceivedMillis)}"
                }
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = "+ ${CurrencyFormatter.formatLkr(income.realizedAmountLkr)}",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = PrimaryGreen
        )
    }
}

// -----------------------------------------------------------------------------------------
// INPUT MODAL DIALOGS (MONTHLY & CHANNEL-SPECIFIC)
// -----------------------------------------------------------------------------------------

@Composable
fun ChannelPickerModal(
    targetMonth: String,
    onDismiss: () -> Unit,
    onSelectChannel: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Log Income Stream", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Target Month: $targetMonth", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)

                ChannelPickItem(
                    title = "Monthly Salary & Bonus",
                    subtitle = "Track base salary (120k+) & monthly bonus",
                    icon = Icons.Rounded.Work,
                    tint = BrandTangerine,
                    onClick = { onSelectChannel("SALARY") }
                )
                ChannelPickItem(
                    title = "Monthly Google AdSense",
                    subtitle = "Log USD payout with auto-currency conversion",
                    icon = Icons.Rounded.AttachMoney,
                    tint = BrandTangerine,
                    onClick = { onSelectChannel("ADSENSE") }
                )
                ChannelPickItem(
                    title = "Freelance Project Milestone / Invoice",
                    subtitle = "Track SME web builds, invoices & client payments",
                    icon = Icons.Rounded.Laptop,
                    tint = BrandTangerine,
                    onClick = { onSelectChannel("FREELANCE") }
                )
                ChannelPickItem(
                    title = "Crypto Realized P&L",
                    subtitle = "Record net realized trading gains or withdrawals",
                    icon = Icons.Rounded.CurrencyBitcoin,
                    tint = BrandTangerine,
                    onClick = { onSelectChannel("CRYPTO") }
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        shape = RoundedCornerShape(18.dp)
    )
}

@Composable
fun ChannelPickItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    tint: Color,
    onClick: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(tint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = title, tint = tint, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun MonthlySalaryDialog(
    defaultMonthName: String,
    onDismiss: () -> Unit,
    onSubmit: (base: Double, bonus: Double, monthName: String) -> Unit
) {
    var monthName by remember { mutableStateOf(defaultMonthName) }
    var baseText by remember { mutableStateOf("120000") }
    var bonusText by remember { mutableStateOf("15000") }

    val base = baseText.toDoubleOrNull() ?: 120000.0
    val bonus = bonusText.toDoubleOrNull() ?: 0.0
    val total = base + bonus

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Log Monthly Salary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text("Month Cycle", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = monthName,
                    onValueChange = { monthName = it },
                    label = { Text("Target Month") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = baseText,
                    onValueChange = { baseText = it },
                    label = { Text("Base Salary (LKR)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = bonusText,
                    onValueChange = { bonusText = it },
                    label = { Text("Performance Bonus (LKR)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(BrandTangerine.copy(alpha = 0.12f))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Total Realized", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, color = BrandTangerine)
                    Text(CurrencyFormatter.formatLkr(total), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = BrandTangerine)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (base > 0) {
                        onSubmit(base, bonus, monthName)
                    }
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Save Monthly Salary", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun MonthlyAdSenseDialog(
    defaultMonthName: String,
    onDismiss: () -> Unit,
    onSubmit: (usd: Double, rate: Double, realizedLkr: Double, monthName: String) -> Unit
) {
    var monthName by remember { mutableStateOf(defaultMonthName) }
    var usdText by remember { mutableStateOf("150") }
    var rateText by remember { mutableStateOf("305.0") }

    val usd = usdText.toDoubleOrNull() ?: 0.0
    val rate = rateText.toDoubleOrNull() ?: 305.0
    val realizedLkr = usd * rate

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Log Monthly AdSense (USD)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                OutlinedTextField(
                    value = monthName,
                    onValueChange = { monthName = it },
                    label = { Text("Payout Month") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = usdText,
                    onValueChange = { usdText = it },
                    label = { Text("AdSense USD Amount ($)") },
                    placeholder = { Text("150.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = rateText,
                    onValueChange = { rateText = it },
                    label = { Text("Exchange Rate (LKR / USD)") },
                    placeholder = { Text("305.0") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(BrandTangerine.copy(alpha = 0.12f))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Converted LKR", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, color = BrandTangerine)
                    Text(CurrencyFormatter.formatLkr(realizedLkr), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = BrandTangerine)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (usd > 0 && rate > 0) {
                        onSubmit(usd, rate, realizedLkr, monthName)
                    }
                },
                enabled = usd > 0,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Save AdSense Inflow", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun FreelanceMilestoneDialog(
    onDismiss: () -> Unit,
    onSubmit: (title: String, client: String, amount: Double, status: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var client by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var isPendingInvoice by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Freelance Project / Invoice", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                OutlinedTextField(
                    value = client,
                    onValueChange = { client = it },
                    label = { Text("Client Name") },
                    placeholder = { Text("e.g. Apex Labs / SME Client") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Project / Milestone Title") },
                    placeholder = { Text("e.g. React E-Commerce Milestone") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Milestone Amount (LKR)") },
                    placeholder = { Text("45000.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text("Invoice Status", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = isPendingInvoice,
                        onClick = { isPendingInvoice = true },
                        label = { Text("Pending Invoice (Unpaid)") },
                        shape = RoundedCornerShape(8.dp)
                    )
                    FilterChip(
                        selected = !isPendingInvoice,
                        onClick = { isPendingInvoice = false },
                        label = { Text("Already Received (Paid)") },
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    if (title.isNotEmpty() && amt > 0) {
                        onSubmit(title, client.ifEmpty { "Direct Client" }, amt, if (isPendingInvoice) "INVOICED" else "PAID")
                    }
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(if (isPendingInvoice) "Create Pending Invoice" else "Save Paid Milestone", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun CryptoPnlDialog(
    onDismiss: () -> Unit,
    onSubmit: (capital: Double, withdraw: Double, netPnl: Double) -> Unit
) {
    var capitalText by remember { mutableStateOf("50000") }
    var withdrawText by remember { mutableStateOf("72000") }

    val capital = capitalText.toDoubleOrNull() ?: 0.0
    val withdraw = withdrawText.toDoubleOrNull() ?: 0.0
    val netPnl = withdraw - capital

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Log Crypto Realized P&L", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                OutlinedTextField(
                    value = capitalText,
                    onValueChange = { capitalText = it },
                    label = { Text("Invested Capital Basis (LKR)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = withdrawText,
                    onValueChange = { withdrawText = it },
                    label = { Text("Cashed Out / Valuation (LKR)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (netPnl >= 0) IncomePositive.copy(alpha = 0.12f) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Net Realized Gain/Loss", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                    Text(
                        text = if (netPnl >= 0) "+${CurrencyFormatter.formatLkr(netPnl)}" else "-${CurrencyFormatter.formatLkr(-netPnl)}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (netPnl >= 0) IncomePositive else MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (withdraw > 0) {
                        onSubmit(capital, withdraw, netPnl)
                    }
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Save Realized P&L", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        shape = RoundedCornerShape(16.dp)
    )
}
