package com.example.intrack.ui.screens.goal

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoGraph
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Flight
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LaptopMac
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.intrack.data.model.GoalEntity
import com.example.intrack.ui.common.EmptyStateView
import com.example.intrack.ui.common.UiState
import com.example.intrack.ui.components.WhatIfCalculatorModal
import com.example.intrack.ui.theme.PrimaryGreen
import com.example.intrack.util.CurrencyFormatter
import com.example.intrack.util.DateUtils
import java.util.Locale
import kotlinx.coroutines.launch

import androidx.compose.material.icons.rounded.DeleteOutline

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalDetailScreen(
    goalId: String,
    onNavigateBack: () -> Unit,
    viewModel: GoalViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val goalsState by viewModel.goalsState.collectAsState()
    var showWhatIfModal by remember { mutableStateOf(false) }
    var showContributeDialog by remember { mutableStateOf(false) }
    var showDeleteGoalConfirm by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp),
                title = {
                    Text(
                        text = "Goal Details",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(start = 2.dp)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteGoalConfirm = true }) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = "Delete Goal",
                            tint = MaterialTheme.colorScheme.error
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
                    title = "Error Loading Goal",
                    message = errorMsg,
                    actionLabel = "Retry",
                    onActionClick = { viewModel.loadGoals("guest_user") },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            is UiState.Empty -> {
                EmptyStateView(
                    title = "Goal Not Found",
                    message = "The requested goal target does not exist.",
                    actionLabel = "Back to Goals",
                    onActionClick = onNavigateBack,
                    modifier = Modifier.padding(innerPadding)
                )
            }
            is UiState.Success -> {
                val goals = (goalsState as UiState.Success<List<GoalEntity>>).data
                val goal = goals.find { it.id == goalId } ?: goals.firstOrNull() ?: GoalEntity()

                val progress = if (goal.targetAmountLkr > 0) {
                    (goal.currentSavedLkr / goal.targetAmountLkr).toFloat().coerceIn(0f, 1f)
                } else 0f
                val isCompleted = goal.isCompleted || progress >= 1f
                val daysLeft = DateUtils.daysBetween(System.currentTimeMillis(), goal.targetDateMillis).coerceAtLeast(0L)
                val percentageInt = (progress * 100).toInt()
                val (goalIcon, goalTint) = getGoalIcon(goal.title)

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(6.dp))

                        // Hero Header Card (Clean, minimal, high contrast)
                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Category Icon
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(goalTint.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = goalIcon,
                                        contentDescription = null,
                                        tint = goalTint,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Title
                                Text(
                                    text = goal.title,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Saved Balance
                                Text(
                                    text = CurrencyFormatter.formatLkr(goal.currentSavedLkr),
                                    style = MaterialTheme.typography.headlineLarge.copy(
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = (-0.5).sp
                                    ),
                                    color = PrimaryGreen
                                )

                                Text(
                                    text = "Target: ${CurrencyFormatter.formatLkr(goal.targetAmountLkr)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(18.dp))

                                // Progress Bar
                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = PrimaryGreen,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // Percentage & Time Left Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "$percentageInt% Saved",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryGreen
                                    )

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(
                                                if (isCompleted) PrimaryGreen.copy(alpha = 0.15f)
                                                else MaterialTheme.colorScheme.surfaceVariant
                                            )
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = if (isCompleted) "Done 🎉" else "${daysLeft}d left",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isCompleted) PrimaryGreen else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Primary Deposit Button
                    item {
                        Button(
                            onClick = { showContributeDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Savings,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Deposit Funds",
                                style = MaterialTheme.typography.labelLarge.copy(fontSize = 15.sp),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Key Stats Row (2 clean side-by-side cards)
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatBox(
                                label = "MONTHLY PACE",
                                value = "${CurrencyFormatter.formatLkr(goal.monthlyTargetRunRateLkr)}/mo",
                                icon = Icons.Rounded.Speed,
                                modifier = Modifier.weight(1f)
                            )

                            StatBox(
                                label = "TARGET DATE",
                                value = DateUtils.formatDate(goal.targetDateMillis),
                                icon = Icons.Rounded.CalendarToday,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Minimal Milestone Stepper
                    item {
                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Milestones",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    val achievedPercent = (progress * 100).toInt()
                                    Text(
                                        text = "$achievedPercent% Achieved",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (achievedPercent >= 100) PrimaryGreen else MaterialTheme.colorScheme.primary
                                    )
                                }

                                Spacer(modifier = Modifier.height(18.dp))

                                val milestones = listOf(
                                    0.25f to "25%",
                                    0.50f to "50%",
                                    0.75f to "75%",
                                    1.00f to "100%"
                                )

                                val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
                                val primaryGreenColor = PrimaryGreen

                                // Piecewise progress fraction across the 4 milestones
                                val lineFraction = when {
                                    progress <= 0.25f -> 0f
                                    progress < 0.50f -> (progress - 0.25f) / 0.25f * (1f / 3f)
                                    progress < 0.75f -> (1f / 3f) + (progress - 0.50f) / 0.25f * (1f / 3f)
                                    progress < 1.00f -> (2f / 3f) + (progress - 0.75f) / 0.25f * (1f / 3f)
                                    else -> 1f
                                }

                                Box(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    // Connecting Track Line through the center of all milestone circles
                                    Canvas(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(32.dp)
                                    ) {
                                        val circleRadiusPx = 16.dp.toPx()
                                        val startX = circleRadiusPx
                                        val endX = size.width - circleRadiusPx
                                        val trackY = size.height / 2
                                        val strokeW = 4.dp.toPx()

                                        // Background Track
                                        drawLine(
                                            color = surfaceVariantColor,
                                            start = Offset(startX, trackY),
                                            end = Offset(endX, trackY),
                                            strokeWidth = strokeW,
                                            cap = StrokeCap.Round
                                        )

                                        // Active Progress Line
                                        val trackLength = endX - startX
                                        val activeLength = trackLength * lineFraction.coerceIn(0f, 1f)
                                        if (activeLength > 0f) {
                                            drawLine(
                                                color = primaryGreenColor,
                                                start = Offset(startX, trackY),
                                                end = Offset(startX + activeLength, trackY),
                                                strokeWidth = strokeW,
                                                cap = StrokeCap.Round
                                            )
                                        }
                                    }

                                    // Milestone Nodes Row
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        milestones.forEach { (ratio, label) ->
                                            val isAchieved = progress >= ratio
                                            val isNextTarget = !isAchieved && (ratio == 0.25f || progress >= (ratio - 0.25f))

                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(32.dp)
                                                        .clip(CircleShape)
                                                        .background(
                                                            if (isAchieved) PrimaryGreen
                                                            else MaterialTheme.colorScheme.surface
                                                        )
                                                        .border(
                                                            width = if (isAchieved) 0.dp else if (isNextTarget) 2.dp else 1.5.dp,
                                                            color = if (isAchieved) Color.Transparent
                                                            else if (isNextTarget) PrimaryGreen
                                                            else MaterialTheme.colorScheme.outlineVariant,
                                                            shape = CircleShape
                                                        ),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    if (isAchieved) {
                                                        Icon(
                                                            imageVector = Icons.Rounded.Check,
                                                            contentDescription = null,
                                                            tint = Color.White,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                    } else {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(8.dp)
                                                                .clip(CircleShape)
                                                                .background(
                                                                    if (isNextTarget) PrimaryGreen
                                                                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                                                )
                                                        )
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(6.dp))

                                                Text(
                                                    text = label,
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                                    fontWeight = if (isAchieved || isNextTarget) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (isAchieved) PrimaryGreen
                                                    else if (isNextTarget) MaterialTheme.colorScheme.onSurface
                                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Secondary What-If Simulator Action
                    item {
                        OutlinedButton(
                            onClick = { showWhatIfModal = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AutoGraph,
                                contentDescription = null,
                                tint = PrimaryGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Simulate with What-If Calculator",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
            else -> {}
        }
    }

    val goals = (goalsState as? UiState.Success<List<GoalEntity>>)?.data ?: emptyList()
    val currentGoal = goals.find { it.id == goalId } ?: goals.firstOrNull() ?: GoalEntity()

    if (showWhatIfModal) {
        WhatIfCalculatorModal(
            monthlyTargetRunRate = currentGoal.monthlyTargetRunRateLkr,
            onDismiss = { showWhatIfModal = false }
        )
    }

    if (showContributeDialog) {
        var depositText by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showContributeDialog = false },
            title = {
                Text(
                    text = "Deposit to ${currentGoal.title}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Current saved: ${CurrencyFormatter.formatLkr(currentGoal.currentSavedLkr)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = depositText,
                        onValueChange = { depositText = it },
                        label = { Text("Deposit Amount (LKR)") },
                        placeholder = { Text("e.g. 15000") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = depositText.toDoubleOrNull() ?: 0.0
                        if (amount > 0) {
                            viewModel.allocateWindfall(amount, currentGoal.id)
                            showContributeDialog = false
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Deposited ${CurrencyFormatter.formatLkr(amount)} into ${currentGoal.title}")
                            }
                        }
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Deposit", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showContributeDialog = false }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(18.dp)
        )
    }

    if (showDeleteGoalConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteGoalConfirm = false },
            title = {
                Text(text = "Delete Goal", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("Are you sure you want to delete '${currentGoal.title}'? This action cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteGoal(currentGoal.id)
                        showDeleteGoalConfirm = false
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteGoalConfirm = false }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(18.dp)
        )
    }
}

@Composable
private fun StatBox(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.4.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
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
