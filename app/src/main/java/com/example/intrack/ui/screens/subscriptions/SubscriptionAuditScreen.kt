package com.example.intrack.ui.screens.subscriptions

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
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
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.intrack.data.model.SubscriptionEntity
import com.example.intrack.ui.common.EmptyStateView
import com.example.intrack.ui.common.UiState
import com.example.intrack.ui.theme.TangerineShade1
import com.example.intrack.ui.theme.TangerineShade2
import com.example.intrack.ui.theme.TangerineShade3
import com.example.intrack.util.CurrencyFormatter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionAuditScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: SubscriptionViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val subscriptionsState by viewModel.subscriptionsState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var activeActionSub by remember { mutableStateOf<SubscriptionEntity?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp),
                title = {
                    Text(
                        text = "Subscriptions",
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
                    contentDescription = "Add Subscription",
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        modifier = modifier
    ) { innerPadding ->
        when (subscriptionsState) {
            is UiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            }
            is UiState.Error -> {
                val errorMsg = (subscriptionsState as UiState.Error).message
                EmptyStateView(
                    title = "Error Loading Subscriptions",
                    message = errorMsg,
                    actionLabel = "Retry",
                    onActionClick = { viewModel.loadSubscriptions("guest_user") },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            is UiState.Empty -> {
                EmptyStateView(
                    title = "No Subscriptions Found",
                    message = "No fixed obligations or auto-debits recorded.",
                    actionLabel = "Add Subscription",
                    onActionClick = { showAddDialog = true },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            is UiState.Success -> {
                val subscriptions = (subscriptionsState as UiState.Success<List<SubscriptionEntity>>).data
                val activeSubs = subscriptions.filter { it.status != "CANCELLED" }
                val totalActive = activeSubs.sumOf { it.amountLkr }
                val secondaryTotal = activeSubs.filter { it.paymentAccount == "ACCOUNT_SECONDARY" }.sumOf { it.amountLkr }
                val primaryCardTotal = activeSubs.filter { it.paymentAccount == "CARD_PRIMARY" }.sumOf { it.amountLkr }
                val cashTotal = activeSubs.filter { it.paymentAccount == "CASH" }.sumOf { it.amountLkr }

                val filteredList = subscriptions

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(22.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        RentAndRecurringCard(
                            totalRecurringLkr = totalActive,
                            secondaryAccountTotal = secondaryTotal,
                            primaryCardTotal = primaryCardTotal,
                            cashTotal = cashTotal
                        )
                    }

                    item {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Recurring Charges",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            if (filteredList.isEmpty()) {
                                EmptyStateView(
                                    title = "No items in this filter",
                                    message = "All subscriptions in this category are up to date."
                                )
                            } else {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    filteredList.forEach { subscription ->
                                        SubscriptionItemCard(
                                            subscription = subscription,
                                            onClick = { activeActionSub = subscription }
                                        )
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

    if (showAddDialog) {
        AddSubscriptionDialog(
            onDismiss = { showAddDialog = false },
            onSubmit = { newSub ->
                viewModel.addSubscription(newSub)
                showAddDialog = false
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Added recurring cost '${newSub.name}' (${CurrencyFormatter.formatLkr(newSub.amountLkr)}/mo)")
                }
            }
        )
    }

    if (activeActionSub != null) {
        val sub = activeActionSub!!
        AlertDialog(
            onDismissRequest = { activeActionSub = null },
            title = {
                Text(text = sub.name, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        text = "Cost: ${CurrencyFormatter.formatLkr(sub.amountLkr)} / month",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Payment Account: ${sub.paymentAccount.replace("_", " ")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Status: ${sub.status.lowercase().replaceFirstChar { it.uppercase() }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                if (sub.status != "CANCELLED") {
                    Button(
                        onClick = {
                            viewModel.updateStatus(sub.id, "CANCELLED")
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Cancelled ${sub.name}! Saved ${CurrencyFormatter.formatLkr(sub.amountLkr)}/month.")
                            }
                            activeActionSub = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Cancel Subscription")
                    }
                } else {
                    Button(
                        onClick = {
                            viewModel.updateStatus(sub.id, "ACTIVE")
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Re-activated ${sub.name}")
                            }
                            activeActionSub = null
                        }
                    ) {
                        Text("Keep Active")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { activeActionSub = null }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun RentAndRecurringCard(
    totalRecurringLkr: Double,
    secondaryAccountTotal: Double,
    primaryCardTotal: Double,
    cashTotal: Double = 0.0
) {
    val total = if (totalRecurringLkr > 0) totalRecurringLkr else 1.0
    val secondaryRatio = (secondaryAccountTotal / total).toFloat().coerceIn(0f, 1f)
    val primaryRatio = (primaryCardTotal / total).toFloat().coerceIn(0f, 1f)
    val cashRatio = (cashTotal / total).toFloat().coerceIn(0f, 1f)

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "MONTHLY COMMITMENTS",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.6.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = CurrencyFormatter.formatLkr(totalRecurringLkr),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp
                ),
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Multi-channel account distribution bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (secondaryRatio > 0f) {
                    Box(
                        modifier = Modifier
                            .weight(secondaryRatio.coerceAtLeast(0.01f))
                            .fillMaxSize()
                            .background(TangerineShade1)
                    )
                }
                if (primaryRatio > 0f) {
                    Box(
                        modifier = Modifier
                            .weight(primaryRatio.coerceAtLeast(0.01f))
                            .fillMaxSize()
                            .background(TangerineShade2)
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

            // Numbers & Legend (title and cost in two lines)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
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
                                    .background(TangerineShade1)
                            )
                            Text(
                                text = "Secondary Account",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = CurrencyFormatter.formatLkr(secondaryAccountTotal),
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
                            text = CurrencyFormatter.formatLkr(primaryCardTotal),
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                if (cashRatio > 0f) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
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
                                        .background(TangerineShade3)
                                )
                                Text(
                                    text = "Cash",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = CurrencyFormatter.formatLkr(cashTotal),
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SubscriptionItemRow(
    subscription: SubscriptionEntity,
    onCancelClick: () -> Unit = {},
    onKeepClick: () -> Unit = {}
) {
    SubscriptionItemCard(
        subscription = subscription,
        onClick = {}
    )
}

@Composable
fun SubscriptionItemCard(
    subscription: SubscriptionEntity,
    onClick: () -> Unit = {}
) {
    val (badgeBg, badgeText, accountLabel) = when (subscription.paymentAccount.uppercase()) {
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
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            "Cash"
        )
        else -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            subscription.paymentAccount.replace("_", " ")
        )
    }

    OutlinedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
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
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = subscription.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
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
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = CurrencyFormatter.formatLkr(subscription.amountLkr),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "/ month",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSubscriptionDialog(
    onDismiss: () -> Unit,
    onSubmit: (SubscriptionEntity) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var isFlagged by remember { mutableStateOf(false) }
    var accountIndex by remember { mutableIntStateOf(1) } // 0: CARD_PRIMARY, 1: ACCOUNT_SECONDARY, 2: CASH

    val accounts = listOf("Card", "Secondary", "Cash")
    val accountKeys = listOf("CARD_PRIMARY", "ACCOUNT_SECONDARY", "CASH")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "New Subscription",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    placeholder = { Text("e.g. Gym, SaaS, Netflix") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount (LKR)") },
                    placeholder = { Text("34000.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Account",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    accounts.forEachIndexed { index, label ->
                        SegmentedButton(
                            selected = accountIndex == index,
                            onClick = { accountIndex = index },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = accounts.size)
                        ) { Text(label, style = MaterialTheme.typography.labelSmall) }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isFlagged, onCheckedChange = { isFlagged = it })
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Flag for review", style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    if (name.isNotEmpty() && amt > 0) {
                        onSubmit(
                            SubscriptionEntity(
                                name = name,
                                amountLkr = amt,
                                paymentAccount = accountKeys[accountIndex],
                                isFlaggedForReview = isFlagged,
                                status = "ACTIVE"
                            )
                        )
                    }
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Save", style = MaterialTheme.typography.labelLarge)
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text("Cancel", style = MaterialTheme.typography.labelLarge)
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}
