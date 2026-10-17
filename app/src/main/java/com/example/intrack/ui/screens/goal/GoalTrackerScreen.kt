package com.example.intrack.ui.screens.goal

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoGraph
import androidx.compose.material.icons.rounded.BeachAccess
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Flight
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LaptopMac
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.TrackChanges
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import com.example.intrack.data.model.GoalEntity
import com.example.intrack.ui.common.EmptyStateView
import com.example.intrack.ui.common.UiState
import com.example.intrack.ui.theme.PrimaryGreen
import com.example.intrack.util.CurrencyFormatter
import com.example.intrack.util.DateUtils
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalTrackerScreen(
    onNavigateToGoalDetail: (String) -> Unit,
    onNavigateBack: () -> Unit = {},
    viewModel: GoalViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val goalsState by viewModel.goalsState.collectAsState()
    var selectedFilter by remember { mutableStateOf("ALL") }
    var showAddGoalDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp),
                title = {
                    Text(
                        text = "Financial Goals",
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddGoalDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "New Goal",
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        modifier = modifier
    ) { innerPadding ->
        when (goalsState) {
            is UiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is UiState.Error -> {
                val errorMsg = (goalsState as UiState.Error).message
                EmptyStateView(
                    title = "Error Loading Goals",
                    message = errorMsg,
                    actionLabel = "Retry",
                    onActionClick = { viewModel.loadGoals("guest_user") },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            is UiState.Empty -> {
                EmptyStateView(
                    title = "No Goals Created Yet",
                    message = "Set targets like a new laptop, emergency buffer, or vacation fund.",
                    actionLabel = "Create First Goal",
                    onActionClick = { showAddGoalDialog = true },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            is UiState.Success -> {
                val goals = (goalsState as UiState.Success<List<GoalEntity>>).data
                val totalSaved = goals.sumOf { it.currentSavedLkr }
                val totalTarget = goals.sumOf { it.targetAmountLkr }
                val overallRatio = if (totalTarget > 0) (totalSaved / totalTarget).toFloat().coerceIn(0f, 1f) else 0f
                val overallPercent = (overallRatio * 100).toInt()

                val completedCount = goals.count { it.isCompleted || (it.targetAmountLkr > 0 && it.currentSavedLkr >= it.targetAmountLkr) }
                val inProgressCount = goals.size - completedCount

                val filteredGoals = goals.filter { g ->
                    val isDone = g.isCompleted || (g.targetAmountLkr > 0 && g.currentSavedLkr >= g.targetAmountLkr)
                    when (selectedFilter) {
                        "IN_PROGRESS" -> !isDone
                        "COMPLETED" -> isDone
                        else -> true
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(22.dp)
                ) {
                    // Overall Goals Overview Card
                    item {
                        Spacer(modifier = Modifier.height(2.dp))
                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Text(
                                    text = "TOTAL SAVED ACROSS GOALS",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    letterSpacing = 0.6.sp
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = CurrencyFormatter.formatLkr(totalSaved),
                                    style = MaterialTheme.typography.headlineLarge.copy(
                                        fontSize = 30.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = (-0.5).sp
                                    ),
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                // Proportional Progress Bar (10.dp height, clip 5.dp)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(10.dp)
                                        .clip(RoundedCornerShape(5.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    if (overallRatio > 0f) {
                                        Box(
                                            modifier = Modifier
                                                .weight(overallRatio.coerceIn(0.01f, 1f))
                                                .fillMaxSize()
                                                .background(PrimaryGreen)
                                        )
                                    }
                                    if (1f - overallRatio > 0f) {
                                        Box(
                                            modifier = Modifier
                                                .weight((1f - overallRatio).coerceIn(0.01f, 1f))
                                                .fillMaxSize()
                                                .background(MaterialTheme.colorScheme.surfaceVariant)
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
                                                    .background(PrimaryGreen)
                                            )
                                            Text(
                                                text = "Saved Progress",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "$overallPercent% (${CurrencyFormatter.formatLkr(totalSaved)})",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Target (${goals.size} Active)",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = CurrencyFormatter.formatLkr(totalTarget),
                                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Status Filter Chips
                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(
                                    "ALL" to "All Goals (${goals.size})",
                                    "IN_PROGRESS" to "In Progress ($inProgressCount)",
                                    "COMPLETED" to "Completed ($completedCount)"
                                ).forEach { (key, label) ->
                                    val isSelected = selectedFilter == key
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { selectedFilter = key },
                                        label = {
                                            Text(
                                                text = label,
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
                                        )
                                    )
                                }
                            }

                            // Goals list directly under filter chips with 10dp spacing
                            if (filteredGoals.isEmpty()) {
                                EmptyStateView(
                                    title = "No Goals in this category",
                                    message = "Switch filters or create a new goal target."
                                )
                            } else {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    filteredGoals.forEach { goal ->
                                        GoalListItemCard(
                                            goal = goal,
                                            onClick = { onNavigateToGoalDetail(goal.id) }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(72.dp))
                    }
                }
            }
            else -> {}
        }
    }

    if (showAddGoalDialog) {
        CreateGoalDialog(
            onDismiss = { showAddGoalDialog = false },
            onSubmit = { newGoal ->
                viewModel.addGoal(newGoal)
                showAddGoalDialog = false
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Created goal '${newGoal.title}' (${CurrencyFormatter.formatLkr(newGoal.targetAmountLkr)})",
                        duration = SnackbarDuration.Short
                    )
                }
            }
        )
    }
}

@Composable
fun GoalListItemCard(
    goal: GoalEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = if (goal.targetAmountLkr > 0) {
        (goal.currentSavedLkr / goal.targetAmountLkr).toFloat().coerceIn(0f, 1f)
    } else 0f

    val isCompleted = goal.isCompleted || progress >= 1f
    val daysLeft = DateUtils.daysBetween(System.currentTimeMillis(), goal.targetDateMillis).coerceAtLeast(0L)
    val percentageInt = (progress * 100).toInt()
    val timeFormatted = formatTimeRemaining(daysLeft, isCompleted)
    val (icon, iconTint) = getGoalIcon(goal.title)

    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Icon + Title & Time pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(iconTint.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = goal.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Target: ${CurrencyFormatter.formatLkr(goal.targetAmountLkr)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
                    contentDescription = "View Details",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(14.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Progress Bar
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (isCompleted) PrimaryGreen else iconTint,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Metrics Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Saved ${CurrencyFormatter.formatLkr(goal.currentSavedLkr)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "$percentageInt%",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 19.sp,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = if (isCompleted) PrimaryGreen else iconTint
                )
            }
        }
    }
}

@Composable
fun CreateGoalDialog(
    onDismiss: () -> Unit,
    onSubmit: (GoalEntity) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var targetAmountText by remember { mutableStateOf("") }
    var initialDepositText by remember { mutableStateOf("0") }
    var monthsText by remember { mutableStateOf("12") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Create Financial Goal",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Goal Title") },
                    placeholder = { Text("e.g. MacBook Pro M4, Japan Trip") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = targetAmountText,
                    onValueChange = { targetAmountText = it },
                    label = { Text("Target Amount (LKR)") },
                    placeholder = { Text("490000.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = initialDepositText,
                    onValueChange = { initialDepositText = it },
                    label = { Text("Initial Saved (LKR)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = monthsText,
                    onValueChange = { monthsText = it },
                    label = { Text("Timeframe (Months)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val targetAmt = targetAmountText.toDoubleOrNull() ?: 0.0
                    val initialSaved = initialDepositText.toDoubleOrNull() ?: 0.0
                    val months = monthsText.toDoubleOrNull() ?: 12.0
                    if (title.isNotEmpty() && targetAmt > 0) {
                        val runRate = (targetAmt - initialSaved) / months.coerceAtLeast(1.0)
                        val targetDateMillis = System.currentTimeMillis() + (months * 30L * 24L * 60L * 60L * 1000L).toLong()
                        val newGoal = GoalEntity(
                            id = UUID.randomUUID().toString(),
                            title = title,
                            targetAmountLkr = targetAmt,
                            currentSavedLkr = initialSaved,
                            targetDateMillis = targetDateMillis,
                            monthlyTargetRunRateLkr = runRate
                        )
                        onSubmit(newGoal)
                    }
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Create Goal", style = MaterialTheme.typography.labelLarge)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", style = MaterialTheme.typography.labelLarge)
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

private fun formatTimeRemaining(daysLeft: Long, isCompleted: Boolean): String {
    if (isCompleted) return "Done 🎉"
    if (daysLeft <= 0) return "Due"
    if (daysLeft <= 30) return "${daysLeft}d left"

    val months = daysLeft / 30
    val days = daysLeft % 30

    return if (days > 0) {
        "${months}mo ${days}d left"
    } else {
        "${months}mo left"
    }
}

private fun getGoalIcon(title: String): Pair<ImageVector, Color> {
    val lower = title.lowercase(Locale.ROOT)
    val icon = when {
        lower.contains("macbook") || lower.contains("laptop") || lower.contains("pc") || lower.contains("tech") ->
            Icons.Rounded.LaptopMac
        lower.contains("car") || lower.contains("bike") || lower.contains("vehicle") || lower.contains("transport") ->
            Icons.Rounded.DirectionsCar
        lower.contains("emergency") || lower.contains("shield") || lower.contains("buffer") ->
            Icons.Rounded.Shield
        lower.contains("trip") || lower.contains("travel") || lower.contains("vacation") || lower.contains("holiday") ->
            Icons.Rounded.Flight
        lower.contains("home") || lower.contains("house") || lower.contains("rent") ->
            Icons.Rounded.Home
        lower.contains("study") || lower.contains("course") || lower.contains("education") || lower.contains("degree") ->
            Icons.Rounded.School
        lower.contains("savings") || lower.contains("fund") ->
            Icons.Rounded.Savings
        else ->
            Icons.Rounded.Flag
    }
    return Pair(icon, PrimaryGreen)
}
