package com.example.intrack.ui.screens.income

import com.example.intrack.viewmodel.IncomeViewModel
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CurrencyBitcoin
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Laptop
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.ArrowDropDown
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
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.border
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import com.example.intrack.ui.theme.*
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
import androidx.compose.foundation.lazy.items
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.intrack.data.model.CryptoHoldingEntity
import com.example.intrack.data.model.IncomeEntity
import com.example.intrack.ui.common.EmptyStateView
import com.example.intrack.ui.common.UiState
import com.example.intrack.ui.components.WindfallAllocatorModal
import com.example.intrack.ui.theme.BrandTangerine
import com.example.intrack.ui.theme.InTrackTopBar
import com.example.intrack.ui.theme.IncomePositive
import com.example.intrack.ui.theme.PrimaryGreen
import com.example.intrack.ui.theme.TangerineShade1
import com.example.intrack.ui.theme.TangerineShade2
import com.example.intrack.ui.theme.TangerineShade3
import com.example.intrack.ui.theme.TangerineShade4
import com.example.intrack.util.CurrencyFormatter
import com.example.intrack.util.DateUtils
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
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
    var isPendingInvoicesSectionExpanded by remember { mutableStateOf(false) }
    var channelFilterExpanded by remember { mutableStateOf(false) }
    var showActivityFilters by remember { mutableStateOf(false) }
    var showPendingInvoicesSheet by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            InTrackTopBar(
                title = "Incomes",
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
                            contentDescription = "Select Date / Month",
                            modifier = Modifier.size(22.dp),
                            tint = Color(0xFF121660)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                }
            )
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Mini Speed Dial Action Buttons (visible when expanded)
                AnimatedVisibility(
                    visible = isFabExpanded,
                    enter = fadeIn(tween(180)) + slideInVertically(animationSpec = tween(200), initialOffsetY = { it / 2 }),
                    exit = fadeOut(tween(150)) + slideOutVertically(animationSpec = tween(150), targetOffsetY = { it / 2 })
                ) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        // 1. Salary
                        IncomeSpeedDialItem(
                            label = "Salary & Bonus",
                            icon = Icons.Rounded.Work,
                            onClick = {
                                isFabExpanded = false
                                activeModalChannel = "SALARY"
                            }
                        )

                        // 2. Freelance
                        IncomeSpeedDialItem(
                            label = "Freelance Invoice",
                            icon = Icons.Rounded.Laptop,
                            onClick = {
                                isFabExpanded = false
                                activeModalChannel = "FREELANCE"
                            }
                        )

                        // 3. AdSense
                        IncomeSpeedDialItem(
                            label = "Google AdSense",
                            icon = Icons.Rounded.AttachMoney,
                            onClick = {
                                isFabExpanded = false
                                activeModalChannel = "ADSENSE"
                            }
                        )

                        // 4. Crypto
                        IncomeSpeedDialItem(
                            label = "Crypto Realized P&L",
                            icon = Icons.Rounded.CurrencyBitcoin,
                            onClick = {
                                isFabExpanded = false
                                activeModalChannel = "CRYPTO"
                            }
                        )
                    }
                }

                // Main Floating Action Button
                val rotationAngle by animateFloatAsState(
                    targetValue = if (isFabExpanded) 45f else 0f,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "fabRotation"
                )

                FloatingActionButton(
                    onClick = { isFabExpanded = !isFabExpanded },
                    containerColor = Color(0xFF121660),
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = if (isFabExpanded) "Close income options" else "Open income options",
                        modifier = Modifier
                            .size(24.dp)
                            .graphicsLayer { rotationZ = rotationAngle }
                    )
                }
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = Color.White,
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (incomesState) {
                is UiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator() }
                }
                is UiState.Error -> {
                    val errorMsg = (incomesState as UiState.Error).message
                    EmptyStateView(
                        title = "Error Loading Incomes",
                        message = errorMsg,
                        onActionClick = {
                            viewModel.loadIncomes()
                        }
                    )
                }
                is UiState.Empty -> {
                    EmptyStateView(
                        title = "No Income Streams Found",
                        message = "You have no income records yet. Use the + FAB to log your first stream.",
                        actionLabel = "Log Stream",
                        onActionClick = { isFabExpanded = true }
                    )
                }
                is UiState.Success -> {
                    val allPendingInvoices = hubSummary.allPendingInvoices
                    val totalPendingReceivablesLkr = hubSummary.totalPendingReceivablesLkr
                    val totalSalary = hubSummary.totalSalaryLkr
                    val totalFreelancePaidThisMonth = hubSummary.totalFreelancePaidThisMonthLkr
                    val totalAdsense = hubSummary.totalAdsenseLkr
                    val totalCrypto = hubSummary.totalCryptoLkr
                    val totalMonthRealized = hubSummary.totalMonthRealizedLkr
                    val rollingAverage = hubSummary.rollingAverageLkr
                    val monthPaidIncomes = hubSummary.monthPaidIncomes
                    val groupedMonthIncomes = hubSummary.groupedMonthIncomes
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Month Realized Inflow Hero Card & Distribution Bar
                        item {
                            Spacer(modifier = Modifier.height(2.dp))
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

                        // Item 2: Slim Receivables Summary Banner (Option 2 - when pending invoices exist)
                        if (allPendingInvoices.isNotEmpty()) {
                            item {
                                OutlinedCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showPendingInvoicesSheet = true },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.outlinedCardColors(containerColor = Color(0xFF3CAE8C)),
                                    border = BorderStroke(0.dp, Color.Transparent),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 13.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Column {
                                                Text(
                                                    text = "Client Receivables",
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontSize = 13.5.sp,
                                                        fontWeight = FontWeight.Bold
                                                    ),
                                                    color = Color.White
                                                )
                                                Text(
                                                    text = "Tap to manage unpaid invoices",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Medium
                                                    ),
                                                    color = Color.White.copy(alpha = 0.85f)
                                                )
                                            }
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(CircleShape)
                                                    .background(Color.White.copy(alpha = 0.2f))
                                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                                            ) {
                                                Text(
                                                    text = if (allPendingInvoices.size == 1) "1 Pending" else "${allPendingInvoices.size} Pending",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold
                                                    ),
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
                        }

                        // Crypto Multi-Coin Portfolio & Realized P&L Card
                        item {
                            CryptoYtdNetPositionCard(
                                holdings = cryptoHoldings,
                                liveRates = cryptoRates,
                                ytdNetLkr = hubSummary.cryptoYtdNetPositionLkr,
                                onCardClick = { showCryptoDetailSheet = true },
                                onInvestClick = { showInvestModal = true },
                                onPayoutClick = { showPayoutModal = true }
                            )
                        }

                        // ACTIVITIES SECTION HEADER & FEED
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
                                        text = "$selectedMonth Activities",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = Color(0xFF121660)
                                    )

                                    val hasActiveChannelFilter = selectedChannelFilter != "ALL"

                                    Box(
                                        modifier = Modifier
                                            .size(30.dp)
                                            .clip(CircleShape)
                                            .background(if (showActivityFilters || hasActiveChannelFilter) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent)
                                            .clickable { showActivityFilters = !showActivityFilters },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Tune,
                                            contentDescription = "Toggle Filters",
                                            tint = if (showActivityFilters || hasActiveChannelFilter) BrandTangerine else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                if (showActivityFilters) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    val channelOptions = listOf(
                                        "ALL" to "All Inflow Streams",
                                        "SALARY" to "Salary Only",
                                        "FREELANCE" to "Freelance Payments",
                                        "ADSENSE" to "AdSense Inflows",
                                        "CRYPTO" to "Crypto Realized P&L"
                                    )
                                    val selectedLabel = channelOptions.find { it.first == selectedChannelFilter }?.second ?: "All Inflow Streams"

                                    Box(modifier = Modifier.fillMaxWidth()) {
                                        ExposedDropdownMenuBox(
                                            expanded = channelFilterExpanded,
                                            onExpandedChange = { channelFilterExpanded = !channelFilterExpanded },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(MaterialTheme.colorScheme.surface)
                                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(
                                                        text = selectedLabel,
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
                                                expanded = channelFilterExpanded,
                                                onDismissRequest = { channelFilterExpanded = false },
                                                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                                            ) {
                                                channelOptions.forEach { (key, label) ->
                                                    val isSelected = selectedChannelFilter == key
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
                                                            viewModel.setChannelFilter(key)
                                                            channelFilterExpanded = false
                                                        },
                                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                if (monthPaidIncomes.isEmpty()) {
                                    EmptyStateView(
                                        title = "No inflows logged for $selectedMonth",
                                        message = "Use the + FAB to log salary, freelance, AdSense, or crypto."
                                    )
                                } else {
                                    groupedMonthIncomes.forEach { (weekTier, incomesInGroup) ->
                                        val showGroupHeader = !(groupedMonthIncomes.size == 1 && weekTier == "This Week")
                                        if (showGroupHeader) {
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
                                                        .background(Color(0xFF0171DF))
                                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                                ) {
                                                    Text(
                                                        text = weekTier,
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontSize = 11.5.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            letterSpacing = 0.3.sp
                                                        ),
                                                        color = Color.White
                                                    )
                                                }
                                            }
                                        }

                                        incomesInGroup.forEachIndexed { index, income ->
                                            InflowHistoryRow(income = income)
                                            if (index < incomesInGroup.lastIndex) {
                                                HorizontalDivider(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 4.dp),
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
                            Spacer(modifier = Modifier.height(84.dp))
                        }
                    }

                    if (showPendingInvoicesSheet) {
                        PendingInvoicesBottomSheet(
                            allPendingInvoices = allPendingInvoices,
                            totalPendingReceivablesLkr = totalPendingReceivablesLkr,
                            onDismiss = { showPendingInvoicesSheet = false },
                            onSendReminder = { invoice ->
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "Invoice reminder sent to ${invoice.clientName ?: "client"} for '${invoice.title}'!",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            },
                            onMarkAsPaid = { invoice ->
                                showPendingInvoicesSheet = false
                                val settledInvoice = invoice.copy(
                                    status = "PAID",
                                    dateReceivedMillis = System.currentTimeMillis()
                                )
                                viewModel.updateIncome(settledInvoice)
                                pendingWindfallAmount = settledInvoice.realizedAmountLkr
                                pendingWindfallIncome = settledInvoice
                                showWindfallModal = true
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "Settled invoice '${invoice.title}' (+${CurrencyFormatter.formatLkr(invoice.realizedAmountLkr)})",
                                        duration = SnackbarDuration.Short
                                    )
                                }
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
                                viewModel.setSelectedMonth(month)
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
                        pendingWindfallIncome = IncomeEntity(title = "$monthName Salary Bonus", channel = "SALARY", originalAmount = bonus, realizedAmountLkr = bonus, status = "PAID")
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
                    pendingWindfallIncome = IncomeEntity(title = "$monthName AdSense Payout ($$usd)", channel = "ADSENSE", originalAmount = usd, originalCurrency = "USD", exchangeRate = rate, realizedAmountLkr = realizedLkr, status = "PAID")
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
                        pendingWindfallIncome = newIncome
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
            CryptoInvestModal(
                liveRates = cryptoRates,
                onDismiss = { activeModalChannel = null },
                onConfirmInvest = { coinSymbol, investedLkr ->
                    viewModel.investInCoin(coinSymbol, investedLkr)
                    activeModalChannel = null
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Invested ${CurrencyFormatter.formatLkr(investedLkr)} into $coinSymbol! Live market tracking active 🚀")
                    }
                }
            )
        }
    }

    if (showInvestModal) {
        CryptoInvestModal(
            liveRates = cryptoRates,
            onDismiss = { showInvestModal = false },
            onConfirmInvest = { coinSymbol, investedLkr ->
                viewModel.investInCoin(coinSymbol, investedLkr)
                showInvestModal = false
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Invested ${CurrencyFormatter.formatLkr(investedLkr)} into $coinSymbol! Live market tracking active 🚀")
                }
            }
        )
    }

    if (showPayoutModal) {
        CryptoPayoutModal(
            holdings = cryptoHoldings,
            liveRates = cryptoRates,
            onDismiss = { showPayoutModal = false },
            onConfirmPayout = { coinSymbol, payoutLkr ->
                viewModel.payoutCoinGains(coinSymbol, payoutLkr)
                showPayoutModal = false
                pendingWindfallAmount = payoutLkr
                showWindfallModal = true
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Cashed out ${CurrencyFormatter.formatLkr(payoutLkr)} from $coinSymbol!")
                }
            }
        )
    }

    if (showCryptoDetailSheet) {
        CryptoPortfolioDetailBottomSheet(
            holdings = cryptoHoldings,
            liveRates = cryptoRates,
            ytdNetLkr = hubSummary.cryptoYtdNetPositionLkr,
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

private fun formatRelativeIncomeDateGroup(timeMillis: Long, monthName: String = "Month"): String {
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
        else -> "Earlier in $monthName"
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
    val navyText = Color(0xFF121660)
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
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
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
    totalOutflowLkr: Double = 89450.0,
    rollingAvgLkr: Double,
    salaryTotal: Double,
    freelanceTotal: Double,
    adsenseTotal: Double,
    cryptoTotal: Double
) {
    val totalFlow = (totalInflowLkr + totalOutflowLkr).coerceAtLeast(1.0)
    val inflowFraction = (totalInflowLkr / totalFlow).toFloat().coerceIn(0f, 1f)
    val outflowFraction = (totalOutflowLkr / totalFlow).toFloat().coerceIn(0f, 1f)

    val animatedInflow by animateFloatAsState(
        targetValue = inflowFraction,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "animatedInflow"
    )
    val animatedOutflow by animateFloatAsState(
        targetValue = outflowFraction,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "animatedOutflow"
    )

    val rollingAvg = if (rollingAvgLkr > 0) rollingAvgLkr else 120000.0
    val diff = totalInflowLkr - rollingAvg
    val diffPct = if (rollingAvg > 0) ((diff / rollingAvg) * 100).toInt() else 0
    val isAhead = diff >= 0

    val navyText = Color(0xFF121660)
    val azureBlue = Color(0xFF0171DF)

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = BrandWarmYellow),
        border = BorderStroke(0.dp, Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            // 1. Top Row: Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${monthLabel.uppercase()} REALIZED INFLOW",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = navyText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Hero Amount Row
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
                    color = navyText,
                    modifier = Modifier.alignByBaseline()
                )
                Text(
                    text = CurrencyFormatter.formatLkr(totalInflowLkr).replace("LKR", "").trim(),
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

            Spacer(modifier = Modifier.height(10.dp))

            // 2b. Rolling 3-Month Average Run-Rate Comparison
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(navyText.copy(alpha = 0.12f))
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text(
                            text = if (isAhead) "▲ +$diffPct%" else "▼ $diffPct%",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp
                            ),
                            color = navyText
                        )
                        Text(
                            text = "vs. 3-Mo Rolling Avg (${CurrencyFormatter.formatLkr(rollingAvg)})",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = navyText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Dual-tone Progress Bar (Inflow vs Outflow Ratio)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(9999.dp))
                    .background(navyText.copy(alpha = 0.15f))
            ) {
                if (totalInflowLkr > 0 && animatedInflow > 0f) {
                    Box(
                        modifier = Modifier
                            .weight(animatedInflow.coerceIn(0.01f, 0.99f))
                            .fillMaxHeight()
                            .background(navyText)
                    )
                }
                if (totalOutflowLkr > 0 && animatedOutflow > 0f) {
                    Box(
                        modifier = Modifier
                            .weight(animatedOutflow.coerceIn(0.01f, 0.99f))
                            .fillMaxHeight()
                            .background(azureBlue)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Inflow vs Outflow Legend Breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Column 1: Inflow
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
                                .background(navyText)
                        )
                        Text(
                            text = "Inflow",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = navyText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = CurrencyFormatter.formatLkr(totalInflowLkr),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.3).sp
                        ),
                        color = navyText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Column 2: Outflow
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
                                .background(azureBlue)
                        )
                        Text(
                            text = "Outflow",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = navyText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = CurrencyFormatter.formatLkr(totalOutflowLkr),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.3).sp
                        ),
                        color = navyText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
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
    textColor: Color = Color.White,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
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
                    .background(color)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = CurrencyFormatter.formatLkr(amount),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.3).sp
            ),
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun ChannelOverviewTile(
    title: String,
    subtitle: String,
    accentColor: Color,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
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

            // Right: Title & CTA Button / Subtitle
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
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
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = accentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
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

                Text(
                    text = CurrencyFormatter.formatLkr(invoice.realizedAmountLkr),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            AnimatedVisibility(visible = isCardExpanded) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
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
        "CRYPTO" -> "Crypto P&L"
        else -> income.channel.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
    }

    val (chanBg, chanText) = when (income.channel.uppercase()) {
        "SALARY" -> Pair(Color(0xFFE5F6F1), Color(0xFF3CAE8C))
        "FREELANCE" -> Pair(Color(0xFFE5F1FD), Color(0xFF0171DF))
        "ADSENSE" -> Pair(Color(0xFFFFF7DB), Color(0xFF121660))
        "CRYPTO" -> Pair(BrandSoftSlate, Color(0xFF121660))
        else -> Pair(BrandSoftSlate, Color(0xFF121660))
    }

    val formattedDate = DateUtils.formatDate(income.dateReceivedMillis)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp),
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
                    .background(BrandSoftSlate),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = channelIcon,
                    contentDescription = income.channel,
                    tint = Color(0xFF121660),
                    modifier = Modifier.size(19.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = income.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.5.sp,
                        lineHeight = 17.sp
                    ),
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF121660),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(chanBg)
                            .padding(horizontal = 6.dp, vertical = 1.5.dp)
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

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = "+${CurrencyFormatter.formatLkr(income.realizedAmountLkr)}",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            ),
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
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Row
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
                        text = "Pending Client Invoices",
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
                                text = "${allPendingInvoices.size} Unpaid",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = BrandTangerine
                            )
                        }
                    }
                }

                TextButton(onClick = onDismiss) {
                    Text("Done", fontWeight = FontWeight.Bold)
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Cards List
            if (allPendingInvoices.isNotEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
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
    val navyText = Color(0xFF121660)
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Text("Log Income Stream", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = navyText)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Target Month: $targetMonth", style = MaterialTheme.typography.bodySmall, color = navyText, fontWeight = FontWeight.Bold)

                ChannelPickItem(
                    title = "Monthly Salary & Bonus",
                    subtitle = "Track base salary (120k+) & monthly bonus",
                    icon = Icons.Rounded.Work,
                    tint = navyText,
                    onClick = { onSelectChannel("SALARY") }
                )
                ChannelPickItem(
                    title = "Monthly Google AdSense",
                    subtitle = "Log USD payout with auto-currency conversion",
                    icon = Icons.Rounded.AttachMoney,
                    tint = navyText,
                    onClick = { onSelectChannel("ADSENSE") }
                )
                ChannelPickItem(
                    title = "Freelance Project Milestone / Invoice",
                    subtitle = "Track SME web builds, invoices & client payments",
                    icon = Icons.Rounded.Laptop,
                    tint = navyText,
                    onClick = { onSelectChannel("FREELANCE") }
                )
                ChannelPickItem(
                    title = "Crypto Realized P&L",
                    subtitle = "Record net realized trading gains or withdrawals",
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
    val navyText = Color(0xFF121660)
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
            Spacer(modifier = Modifier.width(10.dp))
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
    val navyText = Color(0xFF121660)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Text("Log Monthly Salary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = navyText)
        },
        text = {
            Column {
                Text("Month Cycle", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = navyText)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = monthName,
                    onValueChange = { monthName = it },
                    label = { Text("Target Month") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = navyText, focusedLabelColor = navyText),
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
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = navyText, focusedLabelColor = navyText),
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
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = navyText, focusedLabelColor = navyText),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(navyText.copy(alpha = 0.12f))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Total Realized", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, color = navyText)
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
                Text("Save Monthly Salary", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = navyText, fontWeight = FontWeight.Bold) }
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
    val navyText = Color(0xFF121660)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Text("Log Monthly AdSense (USD)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = navyText)
        },
        text = {
            Column {
                OutlinedTextField(
                    value = monthName,
                    onValueChange = { monthName = it },
                    label = { Text("Payout Month") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = navyText, focusedLabelColor = navyText),
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
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = navyText, focusedLabelColor = navyText),
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
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = navyText, focusedLabelColor = navyText),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(navyText.copy(alpha = 0.12f))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Converted LKR", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, color = navyText)
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
                Text("Save AdSense Inflow", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = navyText, fontWeight = FontWeight.Bold) }
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
    val navyText = Color(0xFF121660)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Text("Freelance Project / Invoice", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = navyText)
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
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = navyText, focusedLabelColor = navyText),
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
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = navyText, focusedLabelColor = navyText),
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
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = navyText, focusedLabelColor = navyText),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text("Invoice Status", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = navyText)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = isPendingInvoice,
                        onClick = { isPendingInvoice = true },
                        label = { Text("Pending Invoice (Unpaid)") },
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
                        label = { Text("Already Received (Paid)") },
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
                        onSubmit(title, client.ifEmpty { "Direct Client" }, amt, if (isPendingInvoice) "INVOICED" else "PAID")
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = navyText),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(if (isPendingInvoice) "Create Pending Invoice" else "Save Paid Milestone", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = navyText, fontWeight = FontWeight.Bold) }
        },
        shape = RoundedCornerShape(16.dp)
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
    val navyText = Color(0xFF121660)

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
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Row: Title & Chevron
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Crypto Portfolio & P&L",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        color = navyText
                    )
                    Text(
                        text = "Binance & CoinGecko Live Market",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = navyText.copy(alpha = 0.7f)
                    )
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "View Details",
                    tint = navyText.copy(alpha = 0.6f),
                    modifier = Modifier.size(22.dp)
                )
            }

            // Portfolio Value Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "LIVE PORTFOLIO VALUATION",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                        color = navyText.copy(alpha = 0.7f)
                    )
                    Text(
                        text = CurrencyFormatter.formatLkr(totalLiveValuationLkr),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold, fontSize = 21.sp),
                        color = navyText
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isPnlPositive) Color(0xFFE5F6F1) else Color(0xFFFFEBEE))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = if (isPnlPositive) "+${CurrencyFormatter.formatLkr(totalUnrealizedPnlLkr)}" else "-${CurrencyFormatter.formatLkr(-totalUnrealizedPnlLkr)}",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                        color = if (isPnlPositive) Color(0xFF3CAE8C) else Color(0xFFD32F2F)
                    )
                }
            }

            // Bottom 50/50 Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // + Invest Button (50%)
                Button(
                    onClick = onInvestClick,
                    colors = ButtonDefaults.buttonColors(containerColor = navyText),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                ) {
                    Icon(imageVector = Icons.Rounded.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Invest", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, fontSize = 13.5.sp), color = Color.White)
                }

                // Payout Button (50%)
                Button(
                    onClick = onPayoutClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0171DF)),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Payout", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, fontSize = 13.5.sp), color = Color.White)
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
    val navyText = Color(0xFF121660)
    val totalLiveValuationLkr = holdings.sumOf { h ->
        val rate = liveRates[h.coinSymbol.uppercase()] ?: h.avgBuyPriceLkr
        h.coinQuantity * rate
    }
    val totalInvestedLkr = holdings.sumOf { it.investedLkr }
    val totalUnrealizedPnlLkr = totalLiveValuationLkr - totalInvestedLkr
    val isPnlPositive = totalUnrealizedPnlLkr >= 0

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
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
                        text = "Crypto Portfolio Holdings",
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                        color = navyText
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = navyText)
                }
            }

            // Live Portfolio Valuation Card
            Card(
                shape = RoundedCornerShape(18.dp),
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
                                text = "TOTAL LIVE PORTFOLIO VALUE",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                                color = navyText.copy(alpha = 0.65f)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = CurrencyFormatter.formatLkr(totalLiveValuationLkr),
                                style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp, fontWeight = FontWeight.ExtraBold),
                                color = navyText
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isPnlPositive) Color(0xFFE5F6F1) else Color(0xFFFFEBEE))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = if (isPnlPositive) "+${CurrencyFormatter.formatLkr(totalUnrealizedPnlLkr)}" else "-${CurrencyFormatter.formatLkr(-totalUnrealizedPnlLkr)}",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                                color = if (isPnlPositive) Color(0xFF3CAE8C) else Color(0xFFD32F2F)
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
                                text = "INVESTED BASIS",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.4.sp),
                                color = navyText.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = CurrencyFormatter.formatLkr(totalInvestedLkr),
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
                                color = navyText
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "YTD REALIZED P&L",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.4.sp),
                                color = navyText.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = CurrencyFormatter.formatLkr(ytdNetLkr),
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
                                color = navyText
                            )
                        }
                    }
                }
            }

            Text(
                text = "INDIVIDUAL COIN HOLDINGS (${holdings.size})",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp),
                color = navyText
            )

            // Individual Coin Holding Cards List
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                holdings.forEach { holding ->
                    val currentRate = liveRates[holding.coinSymbol.uppercase()] ?: holding.avgBuyPriceLkr
                    val currentValuation = holding.coinQuantity * currentRate
                    val holdingPnl = currentValuation - holding.investedLkr
                    val isPositive = holdingPnl >= 0

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = BrandSoftSlate),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 14.dp),
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
                                        .background(Color.White),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = holding.coinSymbol.take(3).uppercase(),
                                        style = MaterialTheme.typography.titleSmall.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold),
                                        color = navyText
                                    )
                                }

                                Column(modifier = Modifier.weight(1f, fill = false)) {
                                    Text(
                                        text = "${holding.coinName} (${holding.coinSymbol.uppercase()})",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold),
                                        color = navyText,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Qty: ${String.format(Locale.US, "%.4f", holding.coinQuantity)}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp, fontWeight = FontWeight.Medium),
                                        color = navyText.copy(alpha = 0.8f),
                                        maxLines = 1
                                    )
                                    Spacer(modifier = Modifier.height(1.dp))
                                    Text(
                                        text = "Rate: ${CurrencyFormatter.formatLkr(currentRate)}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = navyText.copy(alpha = 0.65f),
                                        maxLines = 1
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = CurrencyFormatter.formatLkr(currentValuation),
                                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.5.sp, fontWeight = FontWeight.Bold),
                                    color = navyText,
                                    maxLines = 1,
                                    softWrap = false
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (isPositive) "+${CurrencyFormatter.formatLkr(holdingPnl)}" else "-${CurrencyFormatter.formatLkr(-holdingPnl)}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                                    color = if (isPositive) Color(0xFF3CAE8C) else Color(0xFFD32F2F),
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action CTAs inside drawer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        onDismiss()
                        onInvestClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = navyText),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text("+ Invest in Coin", fontWeight = FontWeight.Bold, color = Color.White)
                }

                Button(
                    onClick = {
                        onDismiss()
                        onPayoutClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0171DF)),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text("Payout Gains", fontWeight = FontWeight.Bold, color = Color.White)
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
    val navyText = Color(0xFF121660)

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
        containerColor = Color.White,
        title = {
            Text("Invest in Crypto Coin", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = navyText)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                    label = { Text("Invested LKR Sum") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
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
                            text = "LIVE RATE: 1 $selectedCoin = ${CurrencyFormatter.formatLkr(currentLiveRate)}",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp, fontWeight = FontWeight.Bold),
                            color = navyText
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Auto-Calculated Coin Quantity: ${String.format(Locale.US, "%.6f", calculatedCoinQty)} $selectedCoin",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold),
                            color = Color(0xFF0171DF)
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
                Text("Confirm Investment", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = navyText, fontWeight = FontWeight.Bold) }
        },
        shape = RoundedCornerShape(16.dp)
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
    val navyText = Color(0xFF121660)

    val currentHolding = holdings.find { it.coinSymbol.equals(selectedCoin, ignoreCase = true) }
    val currentRate = liveRates[selectedCoin.uppercase()] ?: currentHolding?.avgBuyPriceLkr ?: 332.40
    val maxLkrValue = (currentHolding?.coinQuantity ?: 0.0) * currentRate

    val payoutLkr = payoutInputText.toDoubleOrNull() ?: 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Text("Payout & Realize Gains", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = navyText)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Select Coin to Payout", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = navyText)

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
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold),
                        color = Color(0xFF0171DF)
                    )
                }

                OutlinedTextField(
                    value = payoutInputText,
                    onValueChange = { payoutInputText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("LKR Payout Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
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
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0171DF)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Confirm Payout", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = navyText, fontWeight = FontWeight.Bold) }
        },
        shape = RoundedCornerShape(16.dp)
    )
}