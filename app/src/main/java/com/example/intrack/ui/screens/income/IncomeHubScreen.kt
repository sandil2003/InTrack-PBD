package com.example.intrack.ui.screens.income

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CurrencyBitcoin
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Laptop
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Work
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.intrack.data.model.CryptoHoldingEntity
import com.example.intrack.data.model.IncomeEntity
import com.example.intrack.ui.common.EmptyStateView
import com.example.intrack.ui.common.UiState
import com.example.intrack.ui.components.WindfallAllocatorModal
import com.example.intrack.ui.theme.BrandAzureBlue
import com.example.intrack.ui.theme.BrandEmeraldGreen
import com.example.intrack.ui.theme.BrandMidnightNavy
import com.example.intrack.ui.theme.BrandSoftSlate
import com.example.intrack.ui.theme.BrandTangerine
import com.example.intrack.ui.theme.BrandWarmYellow
import com.example.intrack.ui.theme.InTrackTheme
import com.example.intrack.ui.theme.InTrackTopBar
import com.example.intrack.ui.theme.PrimaryGreen
import com.example.intrack.util.CurrencyFormatter
import com.example.intrack.util.DateUtils
import com.example.intrack.viewmodel.IncomeHubSummary
import com.example.intrack.viewmodel.IncomeViewModel
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncomeHubScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToGoal: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToProfile: () -> Unit = onNavigateToSettings,
    onSignOut: () -> Unit = {},
    viewModel: IncomeViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val incomesState by viewModel.incomesState.collectAsState()
    val hubSummary by viewModel.hubSummary.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val selectedChannelFilter by viewModel.selectedChannelFilter.collectAsState()
    val recentMonths = viewModel.recentMonths
    val cryptoRates by viewModel.cryptoRatesState.collectAsState()
    val cryptoHoldings by viewModel.cryptoHoldingsState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    IncomeHubContent(
        uiState = incomesState,
        summary = hubSummary,
        cryptoHoldings = cryptoHoldings,
        cryptoRates = cryptoRates,
        selectedMonth = selectedMonth,
        selectedFilter = selectedChannelFilter,
        recentMonths = recentMonths,
        onSelectMonth = { viewModel.setSelectedMonth(it) },
        onSelectFilter = { viewModel.setChannelFilter(it) },
        onAddSalary = { base, bonus, monthName ->
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
            if (bonus <= 0) {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Logged $monthName salary (+${CurrencyFormatter.formatLkr(total)})")
                }
            }
        },
        onAddAdSense = { usd, rate, realizedLkr, monthName ->
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
        },
        onAddFreelance = { title, client, amount, isPending ->
            val status = if (isPending) "INVOICED" else "PAID"
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
            if (isPending) {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Created pending invoice for $client (${CurrencyFormatter.formatLkr(amount)})")
                }
            }
        },
        onSettleInvoice = { invoice ->
            val settledInvoice = invoice.copy(
                status = "PAID",
                dateReceivedMillis = System.currentTimeMillis()
            )
            viewModel.updateIncome(settledInvoice)
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = "Settled invoice '${invoice.title}' (+${CurrencyFormatter.formatLkr(invoice.realizedAmountLkr)})",
                    duration = SnackbarDuration.Short
                )
            }
        },
        onSendInvoiceReminder = { invoice ->
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = "Invoice reminder sent to ${invoice.clientName ?: "client"} for '${invoice.title}'!",
                    duration = SnackbarDuration.Short
                )
            }
        },
        onInvestCrypto = { coinSymbol, investedLkr ->
            viewModel.investInCoin(coinSymbol, investedLkr)
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Invested ${CurrencyFormatter.formatLkr(investedLkr)} into $coinSymbol!")
            }
        },
        onPayoutCrypto = { coinSymbol, payoutLkr ->
            viewModel.payoutCoinGains(coinSymbol, payoutLkr)
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Cashed out ${CurrencyFormatter.formatLkr(payoutLkr)} from $coinSymbol!")
            }
        },
        onAllocateWindfall = { income, allocatedLkr ->
            if (income != null) {
                viewModel.allocateWindfallToGoal(income, allocatedLkr)
            }
            coroutineScope.launch {
                val result = snackbarHostState.showSnackbar(
                    message = "Allocated ${CurrencyFormatter.formatLkr(allocatedLkr)} to goal!",
                    actionLabel = "View Goal",
                    duration = SnackbarDuration.Long
                )
                if (result == SnackbarResult.ActionPerformed) {
                    onNavigateToGoal()
                }
            }
        },
        onNavigateToSettings = onNavigateToSettings,
        onNavigateToProfile = onNavigateToProfile,
        onSignOut = onSignOut,
        onRefresh = { viewModel.loadIncomes() },
        snackbarHostState = snackbarHostState,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncomeHubContent(
    uiState: UiState<List<IncomeEntity>>,
    summary: IncomeHubSummary,
    cryptoHoldings: List<CryptoHoldingEntity>,
    cryptoRates: Map<String, Double>,
    selectedMonth: String,
    selectedFilter: String,
    recentMonths: List<String>,
    onSelectMonth: (String) -> Unit,
    onSelectFilter: (String) -> Unit,
    onAddSalary: (Double, Double, String) -> Unit,
    onAddAdSense: (Double, Double, Double, String) -> Unit,
    onAddFreelance: (String, String, Double, Boolean) -> Unit,
    onSettleInvoice: (IncomeEntity) -> Unit,
    onSendInvoiceReminder: (IncomeEntity) -> Unit,
    onInvestCrypto: (String, Double) -> Unit,
    onPayoutCrypto: (String, Double) -> Unit,
    onAllocateWindfall: (IncomeEntity?, Double) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onSignOut: () -> Unit,
    onRefresh: () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    modifier: Modifier = Modifier
) {
    var showChannelPickerModal by remember { mutableStateOf(false) }
    var activeModalChannel by remember { mutableStateOf<String?>(null) }
    var showInvestModal by remember { mutableStateOf(false) }
    var showPayoutModal by remember { mutableStateOf(false) }
    var showCryptoDetailSheet by remember { mutableStateOf(false) }
    var pendingWindfallAmount by remember { mutableDoubleStateOf(0.0) }
    var pendingWindfallIncome by remember { mutableStateOf<IncomeEntity?>(null) }
    var showWindfallModal by remember { mutableStateOf(false) }
    var showMonthPicker by remember { mutableStateOf(false) }
    var isFabExpanded by remember { mutableStateOf(false) }
    var showActivityFilters by remember { mutableStateOf(false) }
    var channelFilterExpanded by remember { mutableStateOf(false) }
    var showPendingInvoicesSheet by remember { mutableStateOf(false) }
    var showIncomeChartsModal by remember { mutableStateOf(false) }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            InTrackTopBar(
                title = "Income",
                onNavigateToSettings = onNavigateToSettings,
                onNavigateToProfile = onNavigateToProfile,
                onSignOut = onSignOut,
                showProfileMenu = true,
                actions = {
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
                            tint = BrandMidnightNavy
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                }
            )
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AnimatedVisibility(
                    visible = isFabExpanded,
                    enter = fadeIn(tween(180)) + slideInVertically(animationSpec = tween(200), initialOffsetY = { it / 2 }),
                    exit = fadeOut(tween(150)) + slideOutVertically(animationSpec = tween(150), targetOffsetY = { it / 2 })
                ) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        IncomeSpeedDialItem(
                            label = "Salary",
                            icon = Icons.Rounded.Work,
                            onClick = {
                                isFabExpanded = false
                                activeModalChannel = "SALARY"
                            }
                        )
                        IncomeSpeedDialItem(
                            label = "Freelance",
                            icon = Icons.Rounded.Laptop,
                            onClick = {
                                isFabExpanded = false
                                activeModalChannel = "FREELANCE"
                            }
                        )
                        IncomeSpeedDialItem(
                            label = "AdSense",
                            icon = Icons.Rounded.AttachMoney,
                            onClick = {
                                isFabExpanded = false
                                activeModalChannel = "ADSENSE"
                            }
                        )
                        IncomeSpeedDialItem(
                            label = "Crypto",
                            icon = Icons.Rounded.CurrencyBitcoin,
                            onClick = {
                                isFabExpanded = false
                                activeModalChannel = "CRYPTO"
                            }
                        )
                    }
                }

                val rotationAngle by animateFloatAsState(
                    targetValue = if (isFabExpanded) 45f else 0f,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "fabRotation"
                )

                FloatingActionButton(
                    onClick = { isFabExpanded = !isFabExpanded },
                    containerColor = BrandMidnightNavy,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = if (isFabExpanded) "Close options" else "Add income",
                        modifier = Modifier
                            .size(24.dp)
                            .graphicsLayer { rotationZ = rotationAngle }
                    )
                }
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState) {
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
                        title = "Error Loading Incomes",
                        message = uiState.message,
                        onActionClick = onRefresh
                    )
                }
                is UiState.Empty -> {
                    EmptyStateView(
                        title = "No income records",
                        message = "Log your first income stream to get started.",
                        actionLabel = "Add Income",
                        onActionClick = { isFabExpanded = true }
                    )
                }
                is UiState.Success -> {
                    val allPendingInvoices = summary.allPendingInvoices
                    val totalPendingReceivablesLkr = summary.totalPendingReceivablesLkr
                    val totalMonthRealized = summary.totalMonthRealizedLkr
                    val rollingAverage = summary.rollingAverageLkr
                    val monthPaidIncomes = summary.monthPaidIncomes
                    val groupedMonthIncomes = summary.groupedMonthIncomes

                    if (isLandscape) {
                        // 2-Column Responsive Landscape Layout
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Left Column: Metrics & Summary Cards (45% width)
                            Column(
                                modifier = Modifier
                                    .weight(0.45f)
                                    .fillMaxHeight()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Spacer(modifier = Modifier.height(4.dp))
                                TotalInflowBreakdownCard(
                                    monthLabel = selectedMonth,
                                    totalInflowLkr = totalMonthRealized,
                                    rollingAvgLkr = rollingAverage,
                                    onOpenCharts = { showIncomeChartsModal = true }
                                )

                                if (allPendingInvoices.isNotEmpty()) {
                                    PendingReceivablesBanner(
                                        pendingCount = allPendingInvoices.size,
                                        totalPendingLkr = totalPendingReceivablesLkr,
                                        onClick = { showPendingInvoicesSheet = true }
                                    )
                                }

                                CryptoYtdNetPositionCard(
                                    holdings = cryptoHoldings,
                                    liveRates = cryptoRates,
                                    ytdNetLkr = summary.cryptoYtdNetPositionLkr,
                                    onCardClick = { showCryptoDetailSheet = true },
                                    onInvestClick = { showInvestModal = true },
                                    onPayoutClick = { showPayoutModal = true }
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                            }

                            // Right Column: Activities Feed & Filter (55% width)
                            LazyColumn(
                                modifier = Modifier
                                    .weight(0.55f)
                                    .fillMaxHeight(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                item {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    ActivitiesHeader(
                                        selectedMonth = selectedMonth,
                                        selectedFilter = selectedFilter,
                                        showFilterDropdown = showActivityFilters,
                                        onToggleFilterDropdown = { showActivityFilters = !showActivityFilters },
                                        filterExpanded = channelFilterExpanded,
                                        onFilterExpandedChange = { channelFilterExpanded = it },
                                        onSelectFilter = {
                                            onSelectFilter(it)
                                            channelFilterExpanded = false
                                        }
                                    )
                                }

                                if (monthPaidIncomes.isEmpty()) {
                                    item {
                                        EmptyStateView(
                                            title = "No activity in $selectedMonth",
                                            message = "No income logged for this period."
                                        )
                                    }
                                } else {
                                    groupedMonthIncomes.forEach { (weekTier, incomesInGroup) ->
                                        val showGroupHeader = !(groupedMonthIncomes.size == 1 && weekTier == "This Week")
                                        if (showGroupHeader) {
                                            item(key = "header_$weekTier") {
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                                    modifier = Modifier.padding(vertical = 4.dp)
                                                ) {
                                                    Text(
                                                        text = weekTier,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                    )
                                                }
                                            }
                                        }

                                        items(incomesInGroup, key = { it.id }) { income ->
                                            InflowHistoryRow(income = income)
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
                        // Portrait: Clean Single-Column Layout
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item {
                                Spacer(modifier = Modifier.height(4.dp))
                                TotalInflowBreakdownCard(
                                    monthLabel = selectedMonth,
                                    totalInflowLkr = totalMonthRealized,
                                    rollingAvgLkr = rollingAverage,
                                    onOpenCharts = { showIncomeChartsModal = true }
                                )
                            }

                            if (allPendingInvoices.isNotEmpty()) {
                                item {
                                    PendingReceivablesBanner(
                                        pendingCount = allPendingInvoices.size,
                                        totalPendingLkr = totalPendingReceivablesLkr,
                                        onClick = { showPendingInvoicesSheet = true }
                                    )
                                }
                            }

                            item {
                                CryptoYtdNetPositionCard(
                                    holdings = cryptoHoldings,
                                    liveRates = cryptoRates,
                                    ytdNetLkr = summary.cryptoYtdNetPositionLkr,
                                    onCardClick = { showCryptoDetailSheet = true },
                                    onInvestClick = { showInvestModal = true },
                                    onPayoutClick = { showPayoutModal = true }
                                )
                            }

                            item {
                                ActivitiesHeader(
                                    selectedMonth = selectedMonth,
                                    selectedFilter = selectedFilter,
                                    showFilterDropdown = showActivityFilters,
                                    onToggleFilterDropdown = { showActivityFilters = !showActivityFilters },
                                    filterExpanded = channelFilterExpanded,
                                    onFilterExpandedChange = { channelFilterExpanded = it },
                                    onSelectFilter = {
                                        onSelectFilter(it)
                                        channelFilterExpanded = false
                                    }
                                )
                            }

                            if (monthPaidIncomes.isEmpty()) {
                                item {
                                    EmptyStateView(
                                        title = "No activity in $selectedMonth",
                                        message = "No income logged for this period."
                                    )
                                }
                            } else {
                                groupedMonthIncomes.forEach { (weekTier, incomesInGroup) ->
                                    val showGroupHeader = !(groupedMonthIncomes.size == 1 && weekTier == "This Week")
                                    if (showGroupHeader) {
                                        item(key = "header_$weekTier") {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = MaterialTheme.colorScheme.surfaceVariant,
                                                modifier = Modifier.padding(vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = weekTier,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                    }

                                    items(incomesInGroup, key = { it.id }) { income ->
                                        InflowHistoryRow(income = income)
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

                    if (showPendingInvoicesSheet) {
                        PendingInvoicesBottomSheet(
                            allPendingInvoices = allPendingInvoices,
                            totalPendingReceivablesLkr = totalPendingReceivablesLkr,
                            onDismiss = { showPendingInvoicesSheet = false },
                            onSendReminder = onSendInvoiceReminder,
                            onMarkAsPaid = { invoice ->
                                showPendingInvoicesSheet = false
                                onSettleInvoice(invoice)
                                pendingWindfallAmount = invoice.realizedAmountLkr
                                pendingWindfallIncome = invoice.copy(status = "PAID")
                                showWindfallModal = true
                            }
                        )
                    }

                    if (showIncomeChartsModal) {
                        IncomeBreakdownPieModal(
                            monthLabel = selectedMonth,
                            totalInflowLkr = totalMonthRealized,
                            salaryTotalLkr = summary.totalSalaryLkr,
                            salaryCount = summary.salaryIncomes.size,
                            freelanceTotalLkr = summary.totalFreelancePaidThisMonthLkr,
                            freelanceCount = summary.paidFreelanceInMonth.size,
                            adsenseTotalLkr = summary.totalAdsenseLkr,
                            adsenseCount = summary.adsenseIncomes.size,
                            cryptoTotalLkr = summary.totalCryptoLkr,
                            cryptoCount = summary.cryptoIncomes.size,
                            onDismiss = { showIncomeChartsModal = false },
                            onSelectChannel = { channel ->
                                onSelectFilter(channel)
                                showActivityFilters = true
                            }
                        )
                    }
                }
                else -> {}
            }
        }
    }

    // Month Picker Modal Bottom Sheet
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
                    .padding(bottom = 32.dp),
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
                    activeModalChannel = null
                    onAddSalary(base, bonus, monthName)
                    if (bonus > 0) {
                        pendingWindfallAmount = bonus
                        pendingWindfallIncome = IncomeEntity(
                            title = "$monthName Salary Bonus",
                            channel = "SALARY",
                            originalAmount = bonus,
                            realizedAmountLkr = bonus,
                            status = "PAID"
                        )
                        showWindfallModal = true
                    }
                }
            )
        }
        "ADSENSE" -> {
            MonthlyAdSenseDialog(
                defaultMonthName = selectedMonth,
                onDismiss = { activeModalChannel = null },
                onSubmit = { usd, rate, realizedLkr, monthName ->
                    activeModalChannel = null
                    onAddAdSense(usd, rate, realizedLkr, monthName)
                    pendingWindfallAmount = realizedLkr
                    pendingWindfallIncome = IncomeEntity(
                        title = "$monthName AdSense Payout ($$usd)",
                        channel = "ADSENSE",
                        originalAmount = usd,
                        originalCurrency = "USD",
                        exchangeRate = rate,
                        realizedAmountLkr = realizedLkr,
                        status = "PAID"
                    )
                    showWindfallModal = true
                }
            )
        }
        "FREELANCE" -> {
            FreelanceMilestoneDialog(
                onDismiss = { activeModalChannel = null },
                onSubmit = { title, client, amount, isPending ->
                    activeModalChannel = null
                    onAddFreelance(title, client, amount, isPending)
                    if (!isPending) {
                        pendingWindfallAmount = amount
                        pendingWindfallIncome = IncomeEntity(
                            title = title,
                            channel = "FREELANCE",
                            clientName = client,
                            originalAmount = amount,
                            originalCurrency = "LKR",
                            realizedAmountLkr = amount,
                            status = "PAID"
                        )
                        showWindfallModal = true
                    }
                }
            )
        }
        "CRYPTO" -> {
            CryptoInvestModal(
                liveRates = cryptoRates,
                onDismiss = { activeModalChannel = null },
                onConfirmInvest = { coinSymbol, investedLkr ->
                    activeModalChannel = null
                    onInvestCrypto(coinSymbol, investedLkr)
                }
            )
        }
    }

    if (showInvestModal) {
        CryptoInvestModal(
            liveRates = cryptoRates,
            onDismiss = { showInvestModal = false },
            onConfirmInvest = { coinSymbol, investedLkr ->
                showInvestModal = false
                onInvestCrypto(coinSymbol, investedLkr)
            }
        )
    }

    if (showPayoutModal) {
        CryptoPayoutModal(
            holdings = cryptoHoldings,
            liveRates = cryptoRates,
            onDismiss = { showPayoutModal = false },
            onConfirmPayout = { coinSymbol, payoutLkr ->
                showPayoutModal = false
                onPayoutCrypto(coinSymbol, payoutLkr)
                pendingWindfallAmount = payoutLkr
                pendingWindfallIncome = IncomeEntity(
                    title = "Crypto Realized Gain ($coinSymbol)",
                    channel = "CRYPTO",
                    originalAmount = payoutLkr,
                    realizedAmountLkr = payoutLkr,
                    status = "PAID"
                )
                showWindfallModal = true
            }
        )
    }

    if (showCryptoDetailSheet) {
        CryptoPortfolioDetailBottomSheet(
            holdings = cryptoHoldings,
            liveRates = cryptoRates,
            ytdNetLkr = summary.cryptoYtdNetPositionLkr,
            onDismiss = { showCryptoDetailSheet = false },
            onInvestClick = { showInvestModal = true },
            onPayoutClick = { showPayoutModal = true }
        )
    }

    if (showWindfallModal && pendingWindfallAmount > 0) {
        WindfallAllocatorModal(
            windfallAmountLkr = pendingWindfallAmount,
            onConfirmAllocation = { allocatedLkr ->
                showWindfallModal = false
                onAllocateWindfall(pendingWindfallIncome, allocatedLkr)
            },
            onDismiss = { showWindfallModal = false }
        )
    }
}

// -----------------------------------------------------------------------------------------
// COMPOSABLE SUB-COMPONENTS
// -----------------------------------------------------------------------------------------

@Composable
fun IncomeSpeedDialItem(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    val navyText = BrandMidnightNavy
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.clickable { onClick() }
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = navyText,
            tonalElevation = 2.dp
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }

        SmallFloatingActionButton(
            onClick = onClick,
            containerColor = navyText,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier.size(42.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun TotalInflowBreakdownCard(
    monthLabel: String,
    totalInflowLkr: Double,
    rollingAvgLkr: Double,
    onOpenCharts: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val hasRollingBaseline = rollingAvgLkr > 0.0
    val diff = totalInflowLkr - rollingAvgLkr
    val diffPct = if (hasRollingBaseline) ((diff / rollingAvgLkr) * 100).toInt() else 0
    val isAhead = diff >= 0
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
            // Title + Top-Right Analytics Pie Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$monthLabel Inflow",
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
                        contentDescription = "View income breakdown",
                        tint = navyText,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Hero Amount Row
            val currentSymbol = CurrencyFormatter.getCurrencySymbol()
            val formattedInflowAmount = CurrencyFormatter.formatLkr(totalInflowLkr).replace(currentSymbol, "").trim()

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
                    text = formattedInflowAmount,
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

                // Rolling 3-Month Average Run-Rate Comparison
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
                        Text(
                            text = "${if (isAhead) "▲ +" else "▼ "}$diffPct% vs 3M avg (${CurrencyFormatter.formatLkr(rollingAvgLkr)})",
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

@Composable
fun PendingReceivablesBanner(
    pendingCount: Int,
    totalPendingLkr: Double,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = BrandEmeraldGreen),
        border = BorderStroke(0.dp, Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Pending Invoices",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "${CurrencyFormatter.formatLkr(totalPendingLkr)} outstanding",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "$pendingCount pending",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Open Invoices",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivitiesHeader(
    selectedMonth: String,
    selectedFilter: String,
    showFilterDropdown: Boolean,
    onToggleFilterDropdown: () -> Unit,
    filterExpanded: Boolean,
    onFilterExpandedChange: (Boolean) -> Unit,
    onSelectFilter: (String) -> Unit
) {
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
                color = BrandMidnightNavy
            )

            val hasActiveFilter = selectedFilter != "ALL"

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (showFilterDropdown || hasActiveFilter) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent)
                    .clickable { onToggleFilterDropdown() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Tune,
                    contentDescription = "Filter Activity",
                    tint = if (showFilterDropdown || hasActiveFilter) BrandTangerine else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        if (showFilterDropdown) {
            Spacer(modifier = Modifier.height(8.dp))
            val channelOptions = listOf(
                "ALL" to "All",
                "SALARY" to "Salary",
                "FREELANCE" to "Freelance",
                "ADSENSE" to "AdSense",
                "CRYPTO" to "Crypto"
            )
            val selectedLabel = channelOptions.find { it.first == selectedFilter }?.second ?: "All"

            Box(modifier = Modifier.fillMaxWidth()) {
                ExposedDropdownMenuBox(
                    expanded = filterExpanded,
                    onExpandedChange = onFilterExpandedChange,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = selectedLabel,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Icon(
                                imageVector = Icons.Rounded.ArrowDropDown,
                                contentDescription = "Dropdown",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    ExposedDropdownMenu(
                        expanded = filterExpanded,
                        onDismissRequest = { onFilterExpandedChange(false) },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                    ) {
                        channelOptions.forEach { (key, label) ->
                            val isSelected = selectedFilter == key
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) BrandTangerine else MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                onClick = { onSelectFilter(key) },
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
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
    onMarkAsPaid: () -> Unit
) {
    var isCardExpanded by remember { mutableStateOf(false) }

    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isCardExpanded = !isCardExpanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = invoice.clientName ?: "Client",
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

                Text(
                    text = CurrencyFormatter.formatLkr(invoice.realizedAmountLkr),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            AnimatedVisibility(visible = isCardExpanded) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        thickness = 0.8.dp
                    )
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
                                .height(40.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(imageVector = Icons.Rounded.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Remind", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = onMarkAsPaid,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Mark Paid", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InflowHistoryRow(
    income: IncomeEntity,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val channelIcon = when (income.channel.uppercase()) {
        "SALARY" -> Icons.Rounded.Work
        "FREELANCE" -> Icons.Rounded.Laptop
        "ADSENSE" -> Icons.Rounded.AttachMoney
        "CRYPTO" -> Icons.Rounded.CurrencyBitcoin
        else -> Icons.AutoMirrored.Filled.TrendingUp
    }

    val channelLabel = when (income.channel.uppercase()) {
        "SALARY" -> "Salary"
        "FREELANCE" -> "Freelance"
        "ADSENSE" -> "AdSense"
        "CRYPTO" -> "Crypto"
        else -> income.channel.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
    }

    val (chanBg, chanText) = when (income.channel.uppercase()) {
        "SALARY" -> Pair(Color(0xFFE5F6F1), Color(0xFF3CAE8C))
        "FREELANCE" -> Pair(Color(0xFFE5F1FD), Color(0xFF0171DF))
        "ADSENSE" -> Pair(Color(0xFFFFF7DB), BrandMidnightNavy)
        "CRYPTO" -> Pair(BrandSoftSlate, BrandMidnightNavy)
        else -> Pair(BrandSoftSlate, BrandMidnightNavy)
    }

    val formattedDate = DateUtils.formatDate(income.dateReceivedMillis)

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
                    imageVector = channelIcon,
                    contentDescription = income.channel,
                    tint = BrandMidnightNavy,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = income.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = BrandMidnightNavy,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(chanBg)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = channelLabel,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                            color = chanText
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
            text = "+${CurrencyFormatter.formatLkr(income.realizedAmountLkr)}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = PrimaryGreen,
            maxLines = 1,
            softWrap = false
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PendingInvoicesBottomSheet(
    allPendingInvoices: List<IncomeEntity>,
    totalPendingReceivablesLkr: Double,
    onDismiss: () -> Unit,
    onSendReminder: (IncomeEntity) -> Unit,
    onMarkAsPaid: (IncomeEntity) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
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
                    Text(
                        text = "Pending Invoices",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    if (allPendingInvoices.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(BrandTangerine.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${allPendingInvoices.size} unpaid",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = BrandTangerine
                            )
                        }
                    }
                }

                TextButton(onClick = onDismiss) {
                    Text("Done", fontWeight = FontWeight.Bold)
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            if (allPendingInvoices.isNotEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    allPendingInvoices.forEach { invoice ->
                        PendingInvoiceCard(
                            invoice = invoice,
                            onSendReminder = { onSendReminder(invoice) },
                            onMarkAsPaid = { onMarkAsPaid(invoice) }
                        )
                    }
                }
            } else {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = PrimaryGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "All invoices settled",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChannelPickerModal(
    targetMonth: String,
    onDismiss: () -> Unit,
    onSelectChannel: (String) -> Unit
) {
    val navyText = BrandMidnightNavy
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text("Add Income", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = navyText)
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = targetMonth,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                ChannelPickItem(
                    title = "Salary & Bonus",
                    subtitle = "Monthly base salary and bonus",
                    icon = Icons.Rounded.Work,
                    tint = navyText,
                    onClick = { onSelectChannel("SALARY") }
                )
                ChannelPickItem(
                    title = "Google AdSense",
                    subtitle = "USD payout with currency conversion",
                    icon = Icons.Rounded.AttachMoney,
                    tint = navyText,
                    onClick = { onSelectChannel("ADSENSE") }
                )
                ChannelPickItem(
                    title = "Freelance Invoice",
                    subtitle = "Client milestones and invoices",
                    icon = Icons.Rounded.Laptop,
                    tint = navyText,
                    onClick = { onSelectChannel("FREELANCE") }
                )
                ChannelPickItem(
                    title = "Crypto Gains",
                    subtitle = "Realized crypto trading gains",
                    icon = Icons.Rounded.CurrencyBitcoin,
                    tint = navyText,
                    onClick = { onSelectChannel("CRYPTO") }
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = navyText, fontWeight = FontWeight.Bold) }
        },
        shape = RoundedCornerShape(20.dp)
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
    val navyText = BrandMidnightNavy
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = BrandSoftSlate),
        border = BorderStroke(0.dp, Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = title, tint = navyText, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = navyText)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = navyText.copy(alpha = 0.7f))
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
    val navyText = BrandMidnightNavy

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text("Add Salary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = navyText)
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = monthName,
                    onValueChange = { monthName = it },
                    label = { Text("Month") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = navyText, focusedLabelColor = navyText),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = baseText,
                    onValueChange = { baseText = it },
                    label = { Text("Base Salary (LKR)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = navyText, focusedLabelColor = navyText),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = bonusText,
                    onValueChange = { bonusText = it },
                    label = { Text("Bonus (LKR)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = navyText, focusedLabelColor = navyText),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(navyText.copy(alpha = 0.08f))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Total", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = navyText)
                    Text(CurrencyFormatter.formatLkr(total), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = navyText)
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
                colors = ButtonDefaults.buttonColors(containerColor = navyText),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Save", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = navyText, fontWeight = FontWeight.Bold) }
        },
        shape = RoundedCornerShape(20.dp)
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
    val navyText = BrandMidnightNavy

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text("Add AdSense Payout", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = navyText)
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = monthName,
                    onValueChange = { monthName = it },
                    label = { Text("Month") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = navyText, focusedLabelColor = navyText),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = usdText,
                    onValueChange = { usdText = it },
                    label = { Text("Amount (USD)") },
                    placeholder = { Text("150.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = navyText, focusedLabelColor = navyText),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = rateText,
                    onValueChange = { rateText = it },
                    label = { Text("Exchange Rate (LKR/USD)") },
                    placeholder = { Text("305.0") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = navyText, focusedLabelColor = navyText),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(navyText.copy(alpha = 0.08f))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Total (LKR)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = navyText)
                    Text(CurrencyFormatter.formatLkr(realizedLkr), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = navyText)
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
                colors = ButtonDefaults.buttonColors(containerColor = navyText),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Save", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = navyText, fontWeight = FontWeight.Bold) }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun FreelanceMilestoneDialog(
    onDismiss: () -> Unit,
    onSubmit: (title: String, client: String, amount: Double, isPending: Boolean) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var client by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var isPendingInvoice by remember { mutableStateOf(true) }
    val navyText = BrandMidnightNavy

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text("Add Freelance Invoice", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = navyText)
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = client,
                    onValueChange = { client = it },
                    label = { Text("Client") },
                    placeholder = { Text("e.g. Acme Corp") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = navyText, focusedLabelColor = navyText),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Project Title") },
                    placeholder = { Text("e.g. Website Design") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = navyText, focusedLabelColor = navyText),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount (LKR)") },
                    placeholder = { Text("45000.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = navyText, focusedLabelColor = navyText),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = isPendingInvoice,
                        onClick = { isPendingInvoice = true },
                        label = { Text("Pending") },
                        shape = RoundedCornerShape(8.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = navyText,
                            selectedLabelColor = Color.White,
                            containerColor = BrandSoftSlate,
                            labelColor = navyText
                        )
                    )
                    FilterChip(
                        selected = !isPendingInvoice,
                        onClick = { isPendingInvoice = false },
                        label = { Text("Paid") },
                        shape = RoundedCornerShape(8.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = navyText,
                            selectedLabelColor = Color.White,
                            containerColor = BrandSoftSlate,
                            labelColor = navyText
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    if (title.isNotEmpty() && amt > 0) {
                        onSubmit(title, client.ifEmpty { "Client" }, amt, isPendingInvoice)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = navyText),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(if (isPendingInvoice) "Create Invoice" else "Save Payment", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = navyText, fontWeight = FontWeight.Bold) }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun CryptoYtdNetPositionCard(
    holdings: List<CryptoHoldingEntity>,
    liveRates: Map<String, Double>,
    ytdNetLkr: Double,
    onCardClick: () -> Unit,
    onInvestClick: () -> Unit,
    onPayoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val navyText = BrandMidnightNavy

    val totalLiveValuationLkr = holdings.sumOf { h ->
        val rate = liveRates[h.coinSymbol.uppercase()] ?: h.avgBuyPriceLkr
        h.coinQuantity * rate
    }

    val totalInvestedLkr = holdings.sumOf { it.investedLkr }
    val totalUnrealizedPnlLkr = totalLiveValuationLkr - totalInvestedLkr
    val isPnlPositive = totalUnrealizedPnlLkr >= 0

    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCardClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = BrandSoftSlate),
        border = BorderStroke(0.dp, Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Crypto Portfolio",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = navyText
                    )
                    Text(
                        text = "Live market valuation",
                        style = MaterialTheme.typography.labelSmall,
                        color = navyText.copy(alpha = 0.7f)
                    )
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "View Details",
                    tint = navyText.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Portfolio Value",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = navyText.copy(alpha = 0.7f)
                    )
                    Text(
                        text = CurrencyFormatter.formatLkr(totalLiveValuationLkr),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = navyText
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isPnlPositive) Color(0xFFE5F6F1) else Color(0xFFFFEBEE))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isPnlPositive) "+${CurrencyFormatter.formatLkr(totalUnrealizedPnlLkr)}" else "-${CurrencyFormatter.formatLkr(-totalUnrealizedPnlLkr)}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isPnlPositive) BrandEmeraldGreen else Color(0xFFD32F2F)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onInvestClick,
                    colors = ButtonDefaults.buttonColors(containerColor = navyText),
                    shape = RoundedCornerShape(10.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                ) {
                    Icon(imageVector = Icons.Rounded.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Invest", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Button(
                    onClick = onPayoutClick,
                    colors = ButtonDefaults.buttonColors(containerColor = BrandAzureBlue),
                    shape = RoundedCornerShape(10.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Payout", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CryptoPortfolioDetailBottomSheet(
    holdings: List<CryptoHoldingEntity>,
    liveRates: Map<String, Double>,
    ytdNetLkr: Double,
    onDismiss: () -> Unit,
    onInvestClick: () -> Unit,
    onPayoutClick: () -> Unit
) {
    val navyText = BrandMidnightNavy
    val totalLiveValuationLkr = holdings.sumOf { h ->
        val rate = liveRates[h.coinSymbol.uppercase()] ?: h.avgBuyPriceLkr
        h.coinQuantity * rate
    }
    val totalInvestedLkr = holdings.sumOf { it.investedLkr }
    val totalUnrealizedPnlLkr = totalLiveValuationLkr - totalInvestedLkr
    val isPnlPositive = totalUnrealizedPnlLkr >= 0

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
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
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(navyText.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CurrencyBitcoin,
                            contentDescription = null,
                            tint = navyText,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "Crypto Holdings",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = navyText
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = navyText)
                }
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BrandSoftSlate),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Total Value",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = navyText.copy(alpha = 0.65f)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = CurrencyFormatter.formatLkr(totalLiveValuationLkr),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = navyText
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isPnlPositive) Color(0xFFE5F6F1) else Color(0xFFFFEBEE))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isPnlPositive) "+${CurrencyFormatter.formatLkr(totalUnrealizedPnlLkr)}" else "-${CurrencyFormatter.formatLkr(-totalUnrealizedPnlLkr)}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isPnlPositive) BrandEmeraldGreen else Color(0xFFD32F2F)
                            )
                        }
                    }

                    HorizontalDivider(color = navyText.copy(alpha = 0.12f), thickness = 0.8.dp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Invested",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = navyText.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = CurrencyFormatter.formatLkr(totalInvestedLkr),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = navyText
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "YTD P&L",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = navyText.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = CurrencyFormatter.formatLkr(ytdNetLkr),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = navyText
                            )
                        }
                    }
                }
            }

            Text(
                text = "Holdings (${holdings.size})",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = navyText
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                holdings.forEach { holding ->
                    val currentRate = liveRates[holding.coinSymbol.uppercase()] ?: holding.avgBuyPriceLkr
                    val currentValuation = holding.coinQuantity * currentRate
                    val holdingPnl = currentValuation - holding.investedLkr
                    val isPositive = holdingPnl >= 0

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = BrandSoftSlate),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color.White),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = holding.coinSymbol.take(3).uppercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = navyText
                                    )
                                }

                                Column(modifier = Modifier.weight(1f, fill = false)) {
                                    Text(
                                        text = "${holding.coinName} (${holding.coinSymbol.uppercase()})",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = navyText,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "Qty: ${String.format(Locale.US, "%.4f", holding.coinQuantity)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = navyText.copy(alpha = 0.8f),
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "Rate: ${CurrencyFormatter.formatLkr(currentRate)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = navyText.copy(alpha = 0.65f),
                                        maxLines = 1
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = CurrencyFormatter.formatLkr(currentValuation),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = navyText,
                                    maxLines = 1,
                                    softWrap = false
                                )
                                Text(
                                    text = if (isPositive) "+${CurrencyFormatter.formatLkr(holdingPnl)}" else "-${CurrencyFormatter.formatLkr(-holdingPnl)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPositive) BrandEmeraldGreen else Color(0xFFD32F2F),
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        onDismiss()
                        onInvestClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = navyText),
                    shape = RoundedCornerShape(10.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                ) {
                    Text("+ Invest", fontWeight = FontWeight.Bold, color = Color.White)
                }

                Button(
                    onClick = {
                        onDismiss()
                        onPayoutClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandAzureBlue),
                    shape = RoundedCornerShape(10.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                ) {
                    Text("Payout", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CryptoInvestModal(
    liveRates: Map<String, Double>,
    onDismiss: () -> Unit,
    onConfirmInvest: (coinSymbol: String, investedLkr: Double) -> Unit
) {
    val coins = listOf("USDT", "ETH", "BTC", "SOL", "BNB", "ADA", "XRP", "DOGE")
    var selectedCoin by remember { mutableStateOf("ETH") }
    var amountInputText by remember { mutableStateOf("50000") }
    val navyText = BrandMidnightNavy

    val currentLiveRate = liveRates[selectedCoin.uppercase()] ?: when (selectedCoin.uppercase()) {
        "USDT" -> 332.40
        "ETH" -> 765516.00
        "BTC" -> 26450000.00
        "SOL" -> 51800.00
        "BNB" -> 186200.00
        "ADA" -> 142.50
        "XRP" -> 182.00
        else -> 48.20
    }

    val investedLkr = amountInputText.toDoubleOrNull() ?: 0.0
    val calculatedCoinQty = if (currentLiveRate > 0) investedLkr / currentLiveRate else 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text("Invest in Crypto", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = navyText)
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Select Coin", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = navyText)

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(coins) { coin ->
                        val isSelected = selectedCoin.equals(coin, ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCoin = coin },
                            label = { Text(coin) },
                            shape = RoundedCornerShape(8.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = navyText,
                                selectedLabelColor = Color.White,
                                containerColor = BrandSoftSlate,
                                labelColor = navyText
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = amountInputText,
                    onValueChange = { amountInputText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Amount (LKR)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = navyText, focusedLabelColor = navyText),
                    modifier = Modifier.fillMaxWidth()
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(BrandSoftSlate)
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "Rate: 1 $selectedCoin = ${CurrencyFormatter.formatLkr(currentLiveRate)}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = navyText
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Est. Quantity: ${String.format(Locale.US, "%.6f", calculatedCoinQty)} $selectedCoin",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = BrandAzureBlue
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (investedLkr > 0) {
                        onConfirmInvest(selectedCoin, investedLkr)
                    }
                },
                enabled = investedLkr > 0,
                colors = ButtonDefaults.buttonColors(containerColor = navyText),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Confirm", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = navyText, fontWeight = FontWeight.Bold) }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CryptoPayoutModal(
    holdings: List<CryptoHoldingEntity>,
    liveRates: Map<String, Double>,
    onDismiss: () -> Unit,
    onConfirmPayout: (coinSymbol: String, payoutLkr: Double) -> Unit
) {
    val activeCoins = holdings.map { it.coinSymbol.uppercase() }.ifEmpty { listOf("ETH", "USDT") }
    var selectedCoin by remember { mutableStateOf(activeCoins.firstOrNull() ?: "ETH") }
    var payoutInputText by remember { mutableStateOf("25000") }
    val navyText = BrandMidnightNavy

    val currentHolding = holdings.find { it.coinSymbol.equals(selectedCoin, ignoreCase = true) }
    val currentRate = liveRates[selectedCoin.uppercase()] ?: currentHolding?.avgBuyPriceLkr ?: 332.40
    val maxLkrValue = (currentHolding?.coinQuantity ?: 0.0) * currentRate

    val payoutLkr = payoutInputText.toDoubleOrNull() ?: 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text("Crypto Payout", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = navyText)
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Select Coin", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = navyText)

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(activeCoins) { coin ->
                        val isSelected = selectedCoin.equals(coin, ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCoin = coin },
                            label = { Text(coin) },
                            shape = RoundedCornerShape(8.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = navyText,
                                selectedLabelColor = Color.White,
                                containerColor = BrandSoftSlate,
                                labelColor = navyText
                            )
                        )
                    }
                }

                if (currentHolding != null) {
                    Text(
                        text = "Holding: ${String.format(Locale.US, "%.4f", currentHolding.coinQuantity)} $selectedCoin (${CurrencyFormatter.formatLkr(maxLkrValue)})",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = BrandAzureBlue
                    )
                }

                OutlinedTextField(
                    value = payoutInputText,
                    onValueChange = { payoutInputText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Payout Amount (LKR)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = navyText, focusedLabelColor = navyText),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (payoutLkr > 0) {
                        onConfirmPayout(selectedCoin, payoutLkr)
                    }
                },
                enabled = payoutLkr > 0,
                colors = ButtonDefaults.buttonColors(containerColor = BrandAzureBlue),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Confirm", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = navyText, fontWeight = FontWeight.Bold) }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

// -----------------------------------------------------------------------------------------
// COMPOSE PREVIEWS
// -----------------------------------------------------------------------------------------

private fun getSampleSummary(): IncomeHubSummary {
    val sampleIncomes = listOf(
        IncomeEntity(
            id = "1",
            title = "October Base Salary",
            channel = "SALARY",
            realizedAmountLkr = 120000.0,
            status = "PAID",
            dateReceivedMillis = System.currentTimeMillis() - 86400000L * 2
        ),
        IncomeEntity(
            id = "2",
            title = "E-Commerce App Milestone",
            channel = "FREELANCE",
            clientName = "Acme Corp",
            realizedAmountLkr = 45000.0,
            status = "PAID",
            dateReceivedMillis = System.currentTimeMillis() - 86400000L * 5
        ),
        IncomeEntity(
            id = "3",
            title = "Google AdSense Payout",
            channel = "ADSENSE",
            realizedAmountLkr = 64050.0,
            status = "PAID",
            dateReceivedMillis = System.currentTimeMillis() - 86400000L * 10
        ),
        IncomeEntity(
            id = "4",
            title = "ETH Staking Reward Payout",
            channel = "CRYPTO",
            realizedAmountLkr = 28500.0,
            status = "PAID",
            dateReceivedMillis = System.currentTimeMillis() - 86400000L * 15
        )
    )

    val samplePendingInvoices = listOf(
        IncomeEntity(
            id = "p1",
            title = "Inventory Admin Dashboard",
            clientName = "Nexus Ltd",
            channel = "FREELANCE",
            realizedAmountLkr = 55000.0,
            status = "INVOICED"
        ),
        IncomeEntity(
            id = "p2",
            title = "Mobile App Bug Fixes",
            clientName = "Apex Studio",
            channel = "FREELANCE",
            realizedAmountLkr = 25000.0,
            status = "INVOICED"
        )
    )

    return IncomeHubSummary(
        selectedMonth = "October 2026",
        selectedChannelFilter = "ALL",
        allPendingInvoices = samplePendingInvoices,
        totalPendingReceivablesLkr = 80000.0,
        monthIncomes = sampleIncomes,
        salaryIncomes = sampleIncomes.filter { it.channel == "SALARY" },
        paidFreelanceInMonth = sampleIncomes.filter { it.channel == "FREELANCE" },
        adsenseIncomes = sampleIncomes.filter { it.channel == "ADSENSE" },
        cryptoIncomes = sampleIncomes.filter { it.channel == "CRYPTO" },
        totalSalaryLkr = 120000.0,
        totalFreelancePaidThisMonthLkr = 45000.0,
        totalAdsenseLkr = 64050.0,
        totalCryptoLkr = 28500.0,
        totalMonthRealizedLkr = 257550.0,
        rollingAverageLkr = 230000.0,
        monthPaidIncomes = sampleIncomes,
        groupedMonthIncomes = mapOf(
            "This Week" to sampleIncomes.take(2),
            "Last Week" to sampleIncomes.drop(2)
        ),
        cryptoYtdNetPositionLkr = 62400.0
    )
}

private fun getSampleHoldings(): List<CryptoHoldingEntity> {
    return listOf(
        CryptoHoldingEntity(
            id = "h1",
            coinSymbol = "ETH",
            coinName = "Ethereum",
            coinQuantity = 0.525,
            investedLkr = 350000.0,
            avgBuyPriceLkr = 666666.0
        ),
        CryptoHoldingEntity(
            id = "h2",
            coinSymbol = "USDT",
            coinName = "Tether USD",
            coinQuantity = 450.0,
            investedLkr = 148500.0,
            avgBuyPriceLkr = 330.0
        ),
        CryptoHoldingEntity(
            id = "h3",
            coinSymbol = "SOL",
            coinName = "Solana",
            coinQuantity = 3.2,
            investedLkr = 145000.0,
            avgBuyPriceLkr = 45312.0
        )
    )
}

private fun getSampleRates(): Map<String, Double> {
    return mapOf(
        "ETH" to 765516.0,
        "USDT" to 332.40,
        "SOL" to 51800.0,
        "BTC" to 26450000.0
    )
}

@Preview(name = "Income Hub - Portrait Light", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun IncomeHubPreviewPortraitLight() {
    InTrackTheme {
        Surface {
            IncomeHubContent(
                uiState = UiState.Success(getSampleSummary().monthIncomes),
                summary = getSampleSummary(),
                cryptoHoldings = getSampleHoldings(),
                cryptoRates = getSampleRates(),
                selectedMonth = "October 2026",
                selectedFilter = "ALL",
                recentMonths = listOf("October 2026", "September 2026", "August 2026", "July 2026", "June 2026", "May 2026"),
                onSelectMonth = {},
                onSelectFilter = {},
                onAddSalary = { _, _, _ -> },
                onAddAdSense = { _, _, _, _ -> },
                onAddFreelance = { _, _, _, _ -> },
                onSettleInvoice = {},
                onSendInvoiceReminder = {},
                onInvestCrypto = { _, _ -> },
                onPayoutCrypto = { _, _ -> },
                onAllocateWindfall = { _, _ -> },
                onNavigateToSettings = {},
                onNavigateToProfile = {},
                onSignOut = {},
                onRefresh = {}
            )
        }
    }
}

@Preview(name = "Income Hub - Portrait Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, widthDp = 390, heightDp = 844)
@Composable
private fun IncomeHubPreviewPortraitDark() {
    InTrackTheme(darkTheme = true) {
        Surface {
            IncomeHubContent(
                uiState = UiState.Success(getSampleSummary().monthIncomes),
                summary = getSampleSummary(),
                cryptoHoldings = getSampleHoldings(),
                cryptoRates = getSampleRates(),
                selectedMonth = "October 2026",
                selectedFilter = "ALL",
                recentMonths = listOf("October 2026", "September 2026", "August 2026", "July 2026", "June 2026", "May 2026"),
                onSelectMonth = {},
                onSelectFilter = {},
                onAddSalary = { _, _, _ -> },
                onAddAdSense = { _, _, _, _ -> },
                onAddFreelance = { _, _, _, _ -> },
                onSettleInvoice = {},
                onSendInvoiceReminder = {},
                onInvestCrypto = { _, _ -> },
                onPayoutCrypto = { _, _ -> },
                onAllocateWindfall = { _, _ -> },
                onNavigateToSettings = {},
                onNavigateToProfile = {},
                onSignOut = {},
                onRefresh = {}
            )
        }
    }
}

@Preview(name = "Income Hub - Landscape", showBackground = true, widthDp = 844, heightDp = 390)
@Composable
private fun IncomeHubPreviewLandscape() {
    InTrackTheme {
        Surface {
            IncomeHubContent(
                uiState = UiState.Success(getSampleSummary().monthIncomes),
                summary = getSampleSummary(),
                cryptoHoldings = getSampleHoldings(),
                cryptoRates = getSampleRates(),
                selectedMonth = "October 2026",
                selectedFilter = "ALL",
                recentMonths = listOf("October 2026", "September 2026", "August 2026", "July 2026", "June 2026", "May 2026"),
                onSelectMonth = {},
                onSelectFilter = {},
                onAddSalary = { _, _, _ -> },
                onAddAdSense = { _, _, _, _ -> },
                onAddFreelance = { _, _, _, _ -> },
                onSettleInvoice = {},
                onSendInvoiceReminder = {},
                onInvestCrypto = { _, _ -> },
                onPayoutCrypto = { _, _ -> },
                onAllocateWindfall = { _, _ -> },
                onNavigateToSettings = {},
                onNavigateToProfile = {},
                onSignOut = {},
                onRefresh = {}
            )
        }
    }
}

@Preview(name = "Hero Inflow Card", showBackground = true, widthDp = 360)
@Composable
private fun HeroInflowCardPreview() {
    InTrackTheme {
        TotalInflowBreakdownCard(
            monthLabel = "October 2026",
            totalInflowLkr = 257550.0,
            rollingAvgLkr = 230000.0,
            onOpenCharts = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
