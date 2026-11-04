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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Check
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
import androidx.hilt.navigation.compose.hiltViewModel
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

    var showChannelPickerModal by remember { mutableStateOf(false) }
    var activeModalChannel by remember { mutableStateOf<String?>(null) }
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
                title = "Income Streams",
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
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurface
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
                        verticalArrangement = Arrangement.spacedBy(16.dp)
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
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.White.copy(alpha = 0.2f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.Description,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }

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

                        // Crypto YTD Net Position (Q1 gain vs Q3 loss tracker)
                        item {
                            CryptoYtdNetPositionCard(
                                ytdNetLkr = hubSummary.cryptoYtdNetPositionLkr,
                                q1PnlLkr = hubSummary.cryptoQ1PnlLkr,
                                q2PnlLkr = hubSummary.cryptoQ2PnlLkr,
                                q3PnlLkr = hubSummary.cryptoQ3PnlLkr,
                                q4PnlLkr = hubSummary.cryptoQ4PnlLkr,
                                totalGainsLkr = hubSummary.cryptoYtdTotalGainsLkr,
                                totalLossesLkr = hubSummary.cryptoYtdTotalLossesLkr,
                                onLogCryptoClick = { activeModalChannel = "CRYPTO" }
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
                        pendingWindfallIncome = IncomeEntity(title = "Crypto Realized P&L", channel = "CRYPTO", originalAmount = netPnl, originalCurrency = "LKR", realizedAmountLkr = netPnl, status = "PAID")
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
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.clickable { onClick() }
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 2.dp
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
            )
        }

        SmallFloatingActionButton(
            onClick = onClick,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = BrandTangerine,
            shape = CircleShape,
            modifier = Modifier.size(42.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
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

    val animatedSalary by animateFloatAsState(
        targetValue = salaryFraction,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "animatedSalary"
    )
    val animatedFreelance by animateFloatAsState(
        targetValue = freelanceFraction,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "animatedFreelance"
    )
    val animatedAdsense by animateFloatAsState(
        targetValue = adsenseFraction,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "animatedAdsense"
    )
    val animatedCrypto by animateFloatAsState(
        targetValue = cryptoFraction,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "animatedCrypto"
    )

    val darkTextColor = MaterialTheme.colorScheme.onSurface
    val mutedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val hairlineColor = MaterialTheme.colorScheme.outlineVariant

    val rollingAvg = if (rollingAvgLkr > 0) rollingAvgLkr else 120000.0
    val diff = totalInflowLkr - rollingAvg
    val diffPct = if (rollingAvg > 0) ((diff / rollingAvg) * 100).toInt() else 0
    val isAhead = diff >= 0

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
            // 1. Top Row: Title + Inflow Trending Icon Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${monthLabel.uppercase()} REALIZED INFLOW",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp
                    ),
                    color = Color(0xFF121660),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF121660).copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                        contentDescription = "Realized Inflow",
                        tint = Color(0xFF121660),
                        modifier = Modifier.size(18.dp)
                    )
                }
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
                    color = Color(0xFF121660),
                    modifier = Modifier.alignByBaseline()
                )
                Text(
                    text = CurrencyFormatter.formatLkr(totalInflowLkr).replace("LKR", "").trim(),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    ),
                    color = Color(0xFF121660),
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
                        .background(Color(0xFF121660).copy(alpha = 0.12f))
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
                            color = Color(0xFF121660)
                        )
                        Text(
                            text = "vs. 3-Mo Rolling Avg (${CurrencyFormatter.formatLkr(rollingAvg)})",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = Color(0xFF121660)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Multi-segment Capsule Progress Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(9999.dp))
                    .background(Color(0xFF121660).copy(alpha = 0.2f))
            ) {
                if (totalInflowLkr > 0 && animatedSalary > 0f) {
                    Box(
                        modifier = Modifier
                            .weight(animatedSalary.coerceIn(0.01f, 0.99f))
                            .fillMaxHeight()
                            .background(Color(0xFF121660))
                    )
                }
                if (totalInflowLkr > 0 && animatedFreelance > 0f) {
                    Box(
                        modifier = Modifier
                            .weight(animatedFreelance.coerceIn(0.01f, 0.99f))
                            .fillMaxHeight()
                            .background(Color(0xFF0171DF))
                    )
                }
                if (totalInflowLkr > 0 && animatedAdsense > 0f) {
                    Box(
                        modifier = Modifier
                            .weight(animatedAdsense.coerceIn(0.01f, 0.99f))
                            .fillMaxHeight()
                            .background(Color(0xFF3CAE8C))
                    )
                }
                if (totalInflowLkr > 0 && animatedCrypto > 0f) {
                    Box(
                        modifier = Modifier
                            .weight(animatedCrypto.coerceIn(0.01f, 0.99f))
                            .fillMaxHeight()
                            .background(Color(0xFFFFD13E))
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 5. Multi-Column Channel Legend Breakdown
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    LegendNumberItem(color = Color(0xFF121660), label = "Salary", amount = salaryTotal, modifier = Modifier.weight(1f))
                    LegendNumberItem(color = Color(0xFF0171DF), label = "Freelance", amount = freelanceTotal, modifier = Modifier.weight(1f))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    LegendNumberItem(color = Color(0xFF3CAE8C), label = "AdSense", amount = adsenseTotal, modifier = Modifier.weight(1f))
                    LegendNumberItem(color = Color(0xFFFFD13E), label = "Crypto", amount = cryptoTotal, modifier = Modifier.weight(1f))
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
                color = Color(0xFF121660),
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
            color = Color(0xFF121660),
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
        "FREELANCE" -> if (!income.clientName.isNullOrEmpty()) "Client: ${income.clientName}" else "Freelance"
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

@Composable
fun CryptoYtdNetPositionCard(
    ytdNetLkr: Double,
    q1PnlLkr: Double,
    q2PnlLkr: Double,
    q3PnlLkr: Double,
    q4PnlLkr: Double,
    totalGainsLkr: Double,
    totalLossesLkr: Double,
    onLogCryptoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isNetPositive = ytdNetLkr >= 0
    val netColor = if (isNetPositive) PrimaryGreen else MaterialTheme.colorScheme.error

    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = BrandSoftSlate),
        border = BorderStroke(0.dp, Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
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
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF121660).copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CurrencyBitcoin,
                            contentDescription = null,
                            tint = Color(0xFF121660),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Crypto YTD Net Position",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            ),
                            color = Color(0xFF121660)
                        )
                        Text(
                            text = "Binance Realized P&L Ledger",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = Color(0xFF121660).copy(alpha = 0.8f)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF121660),
                    modifier = Modifier.clickable { onLogCryptoClick() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Log P&L",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            color = Color.White
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(netColor.copy(alpha = 0.08f))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "TOTAL YTD REALIZED EARNINGS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (isNetPositive) "+${CurrencyFormatter.formatLkr(ytdNetLkr)}" else "-${CurrencyFormatter.formatLkr(-ytdNetLkr)}",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp
                        ),
                        color = netColor
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(netColor.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = if (isNetPositive) "NET GAIN" else "NET LOSS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = netColor
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "Q1 Gain",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (q1PnlLkr >= 0) "+${CurrencyFormatter.formatLkr(q1PnlLkr)}" else "-${CurrencyFormatter.formatLkr(-q1PnlLkr)}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp
                        ),
                        color = if (q1PnlLkr >= 0) PrimaryGreen else MaterialTheme.colorScheme.error
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "Q3 Loss",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (q3PnlLkr >= 0) "+${CurrencyFormatter.formatLkr(q3PnlLkr)}" else "-${CurrencyFormatter.formatLkr(-q3PnlLkr)}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp
                        ),
                        color = if (q3PnlLkr >= 0) PrimaryGreen else MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}