package com.example.intrack.ui.screens.goal

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.AutoGraph
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CurrencyBitcoin
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.LaptopMac
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.intrack.data.model.GoalContributionEntity
import com.example.intrack.data.model.GoalEntity
import com.example.intrack.ui.common.EmptyStateView
import com.example.intrack.ui.common.UiState
import com.example.intrack.ui.components.WhatIfCalculatorModal
import com.example.intrack.ui.theme.*
import com.example.intrack.util.CurrencyFormatter
import com.example.intrack.util.DateUtils
import kotlinx.coroutines.launch

/**
 * Goal Page (Goal Details Screen) matching Figma Node 2026:1550 / 2026:1549:
 * - Top Bar: "Goal Details", Back Button, Delete Action
 * - Main Goal Card (2026:1559): Category icon, Goal Title, Centered Hero Amount, Target caption, Saved / Time pills, Progress bar
 * - Primary Action CTA: "Deposit Funds" (opens AddGoalValueSheet)
 * - Quick Metrics Grid (2026:1578): MONTHLY PACE & TARGET DATE side-by-side cards
 * - Savings Pace Analysis Card (Required vs Actual pace & deficit/surplus)
 * - Milestones Stepper & What-If Calculator Simulator
 * - Deposit & Windfall History Timeline
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalDetailScreen(
    goalId: String,
    onNavigateBack: () -> Unit,
    viewModel: GoalViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val goalsState by viewModel.goalsState.collectAsState()
    val contributionsMap by viewModel.contributionsState.collectAsState()
    val currentGoalsList = (goalsState as? UiState.Success<List<GoalEntity>>)?.data ?: emptyList()
    val currentGoal = currentGoalsList.find { it.id == goalId } ?: currentGoalsList.firstOrNull() ?: GoalEntity()
    val contributions = contributionsMap[goalId]?.takeIf { it.isNotEmpty() }
        ?: currentGoal.contributions.takeIf { it.isNotEmpty() }
        ?: emptyList()

    LaunchedEffect(goalId) {
        if (goalId.isNotBlank()) {
            viewModel.loadContributions(goalId)
        }
    }

    var showWhatIfModal by remember { mutableStateOf(false) }
    var showDepositSheet by remember { mutableStateOf(false) }
    var showDeleteGoalConfirm by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            InTrackTopBar(
                title = "Goal Details",
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = FigmaDarkText,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteGoalConfirm = true }) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = "Delete Goal",
                            tint = FigmaTerracotta,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            )
        },
        containerColor = FigmaBg,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = modifier
    ) { innerPadding ->
        when (goalsState) {
            is UiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = FigmaTerracotta)
                }
            }
            is UiState.Error -> {
                val errorMsg = (goalsState as UiState.Error).message
                EmptyStateView(
                    title = "Error Loading Goal",
                    message = errorMsg,
                    actionLabel = "Retry",
                    onActionClick = {
                        val currentUid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: "guest_user"
                        viewModel.loadGoals(currentUid)
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            is UiState.Empty -> {
                EmptyStateView(
                    title = "Goal Not Found",
                    message = "The requested financial goal target does not exist.",
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
                val animatedProgress by animateFloatAsState(
                    targetValue = progress,
                    animationSpec = tween(500),
                    label = "goalProgress"
                )
                val isCompleted = goal.isCompleted || progress >= 1f
                val now = System.currentTimeMillis()
                val daysLeft = DateUtils.daysBetween(now, goal.targetDateMillis).coerceAtLeast(0L)
                val percentageInt = (progress * 100).toInt()

                val remainingToSave = (goal.targetAmountLkr - goal.currentSavedLkr).coerceAtLeast(0.0)
                val monthsRemaining = (daysLeft / 30.4375).coerceAtLeast(0.1)

                val dynamicRequiredMonthlyPace = if (isCompleted || remainingToSave <= 0.0) {
                    0.0
                } else {
                    remainingToSave / monthsRemaining
                }

                val elapsedMillis = (now - goal.createdAtMillis).coerceAtLeast(1L)
                val elapsedDays = (elapsedMillis / (24L * 60 * 60 * 1000L)).coerceAtLeast(1L)
                val elapsedMonths = (elapsedDays / 30.4375).coerceAtLeast(0.25)
                val actualMonthlySavingsRate = (goal.currentSavedLkr / elapsedMonths)

                val isPaceOnTrack = isCompleted || (actualMonthlySavingsRate >= dynamicRequiredMonthlyPace)
                val paceDeficit = (dynamicRequiredMonthlyPace - actualMonthlySavingsRate).coerceAtLeast(0.0)
                val paceSurplus = (actualMonthlySavingsRate - dynamicRequiredMonthlyPace).coerceAtLeast(0.0)

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(4.dp))

                        // --- 1. Main Goal Hero Card (Figma node 2026:1559) ---
                        InTrackCard(
                            shape = InTrackCardShape,
                            elevation = 0.5.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 22.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Top Row: Category Pill & Status Badge
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(999.dp))
                                            .background(FigmaSoftPeach)
                                            .padding(horizontal = 10.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = GoalCategoryHelper.getCategoryIcon(goal.category),
                                            contentDescription = null,
                                            tint = FigmaTerracotta,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = GoalCategoryHelper.getCategoryLabel(goal.category),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = FigmaTerracotta
                                        )
                                    }

                                    InTrackStatusBadge(
                                        text = if (isCompleted) "Completed 🎉" else "In Progress",
                                        containerColor = if (isCompleted) Color(0xFFE8F5E9) else Color(0xFFF4EEEA),
                                        contentColor = if (isCompleted) Color(0xFF2E7D32) else FigmaMutedBrown,
                                        fontSize = 11.5.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Heading 2: Goal Title (Figma node 2026:1564)
                                Text(
                                    text = goal.title,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = FigmaDarkText,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // Centered Hero Outflow / Saved Amount (Figma node 2026:1567)
                                InTrackHeroAmount(amountLkr = goal.currentSavedLkr)

                                Spacer(modifier = Modifier.height(6.dp))

                                // Target Caption & Remaining breakdown
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "Target: ${CurrencyFormatter.formatLkr(goal.targetAmountLkr)}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Normal
                                        ),
                                        color = FigmaMutedSlate
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(3.5.dp)
                                            .clip(CircleShape)
                                            .background(FigmaMutedSlate.copy(alpha = 0.5f))
                                    )
                                    Text(
                                        text = if (isCompleted) "Target Reached" else "Remaining: ${CurrencyFormatter.formatLkr(remainingToSave)}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = if (isCompleted) Color(0xFF2E7D32) else FigmaTerracotta
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Percentage & Time Left Badges (Figma node 2026:1571)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    InTrackStatusBadge(
                                        text = "$percentageInt% Saved",
                                        containerColor = FigmaSoftPeach,
                                        contentColor = FigmaTerracotta,
                                        fontSize = 11.5.sp
                                    )

                                    InTrackStatusBadge(
                                        text = if (isCompleted) "Done 🎉" else "${daysLeft}d left",
                                        containerColor = Color(0xFFF4EEEA),
                                        contentColor = if (isCompleted) FigmaTerracotta else FigmaMutedBrown,
                                        fontSize = 11.5.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Smooth Progress Bar (Figma node 2026:1576)
                                InTrackProgressBar(
                                    progress = progress,
                                    height = 8.dp,
                                    fillColor = FigmaTerracotta,
                                    trackColor = FigmaTrackBg
                                )
                            }
                        }
                    }

                    // --- Primary Action CTA Button: "Deposit Funds" ---
                    item {
                        InTrackPrimaryButton(
                            onClick = { showDepositSheet = true }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Savings,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Deposit Funds",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.White
                            )
                        }
                    }

                    // --- 2. Goal Specifications & Funding Card ---
                    item {
                        InTrackCard(
                            shape = InTrackMediumCardShape,
                            elevation = 0.5.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp)
                            ) {
                                Text(
                                    text = "Goal Specifications & Funding",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = FigmaDarkText
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                // 1. Goal Category
                                GoalSpecRow(
                                    icon = GoalCategoryHelper.getCategoryIcon(goal.category),
                                    iconTint = FigmaTerracotta,
                                    label = "Goal Category",
                                    value = GoalCategoryHelper.getCategoryLabel(goal.category),
                                    secondaryValue = "Category classification"
                                )

                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    color = FigmaDivider,
                                    thickness = 1.dp
                                )

                                // 2. Initial Deposit
                                GoalSpecRow(
                                    icon = Icons.Rounded.Savings,
                                    iconTint = FigmaLightOrange,
                                    label = "Initial Deposit",
                                    value = CurrencyFormatter.formatLkr(goal.initialDepositLkr),
                                    secondaryValue = "Seed deposit at creation"
                                )

                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    color = FigmaDivider,
                                    thickness = 1.dp
                                )

                                // 3. Primary Funding Source
                                GoalSpecRow(
                                    icon = GoalCategoryHelper.getFundingSourceIcon(goal.primaryFundingSource),
                                    iconTint = Color(0xFF2C3E50),
                                    label = "Primary Funding Source",
                                    value = GoalCategoryHelper.getFundingSourceTitle(goal.primaryFundingSource),
                                    secondaryValue = GoalCategoryHelper.getFundingSourceSubtitle(goal.primaryFundingSource)
                                )

                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    color = FigmaDivider,
                                    thickness = 1.dp
                                )

                                // 4. Monthly Pace Reminder (Interactive toggle)
                                GoalSpecRow(
                                    icon = GoalCategoryHelper.getPaceReminderIcon(goal.remindMonthlyPace),
                                    iconTint = if (goal.remindMonthlyPace) FigmaLightOrange else FigmaMutedSlate,
                                    label = "Monthly Pace Reminder",
                                    value = if (goal.remindMonthlyPace) "Active • Monthly pace reminder" else "Disabled • Reminders off",
                                    secondaryValue = if (goal.remindMonthlyPace) "Tap to pause reminders" else "Tap to enable reminders",
                                    trailing = {
                                        InTrackStatusBadge(
                                            text = if (goal.remindMonthlyPace) "Active" else "Off",
                                            containerColor = if (goal.remindMonthlyPace) FigmaSoftPeach else Color(0xFFF4EEEA),
                                            contentColor = if (goal.remindMonthlyPace) FigmaTerracotta else FigmaMutedSlate,
                                            fontSize = 11.sp
                                        )
                                    },
                                    onClick = {
                                        val updated = goal.copy(remindMonthlyPace = !goal.remindMonthlyPace)
                                        viewModel.updateGoal(updated)
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar(
                                                if (updated.remindMonthlyPace) "Pace reminders enabled!" else "Pace reminders paused."
                                            )
                                        }
                                    }
                                )

                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    color = FigmaDivider,
                                    thickness = 1.dp
                                )

                                // 5. Creation Date
                                GoalSpecRow(
                                    icon = Icons.Rounded.CalendarToday,
                                    iconTint = FigmaMutedBrown,
                                    label = "Created On",
                                    value = DateUtils.formatDate(goal.createdAtMillis),
                                    secondaryValue = "Original goal setup date"
                                )
                            }
                        }
                    }

                    // --- 3. Quick Metrics Grid (Figma node 2026:1578) ---
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            FigmaStatCard(
                                label = "REQUIRED PACE",
                                value = if (isCompleted) "Completed" else "${CurrencyFormatter.formatLkr(dynamicRequiredMonthlyPace)}/mo",
                                icon = Icons.Rounded.Speed,
                                modifier = Modifier.weight(1f)
                            )

                            FigmaStatCard(
                                label = "TARGET DATE",
                                value = DateUtils.formatDate(goal.targetDateMillis),
                                icon = Icons.Rounded.CalendarToday,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // --- 4. Dynamic Savings Pace Analysis Card (Scenario Core Implementation) ---
                    item {
                        InTrackCard(
                            shape = InTrackMediumCardShape,
                            elevation = 0.5.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Savings Pace Analysis",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = FigmaDarkText
                                    )

                                    InTrackStatusBadge(
                                        text = when {
                                            isCompleted -> "Target Reached 🎉"
                                            isPaceOnTrack -> "On Track"
                                            else -> "Pace Deficit"
                                        },
                                        containerColor = when {
                                            isCompleted || isPaceOnTrack -> Color(0xFFE8F5E9)
                                            else -> FigmaSoftPeach
                                        },
                                        contentColor = when {
                                            isCompleted || isPaceOnTrack -> Color(0xFF2E7D32)
                                            else -> FigmaTerracotta
                                        },
                                        fontSize = 11.5.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // 3-Metric Clean Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Actual Pace
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "CURRENT PACE",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                letterSpacing = 0.5.sp
                                            ),
                                            color = FigmaMutedSlate
                                        )
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text(
                                            text = "${CurrencyFormatter.formatLkr(actualMonthlySavingsRate)}/mo",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = FigmaDarkText,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    // Needed Pace
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "NEEDED PACE",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                letterSpacing = 0.5.sp
                                            ),
                                            color = FigmaMutedSlate
                                        )
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text(
                                            text = "${CurrencyFormatter.formatLkr(dynamicRequiredMonthlyPace)}/mo",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = FigmaDarkText,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    // Gap
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        horizontalAlignment = Alignment.End
                                    ) {
                                        Text(
                                            text = if (paceDeficit > 0) "MONTHLY DEFICIT" else "MONTHLY SURPLUS",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                letterSpacing = 0.5.sp
                                            ),
                                            color = if (paceDeficit > 0) FigmaTerracotta else Color(0xFF2E7D32)
                                        )
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text(
                                            text = if (paceDeficit > 0) {
                                                "- ${CurrencyFormatter.formatLkr(paceDeficit)}"
                                            } else {
                                                "+ ${CurrencyFormatter.formatLkr(paceSurplus)}"
                                            },
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = if (paceDeficit > 0) FigmaTerracotta else Color(0xFF2E7D32),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Minimalist explanatory caption
                                Text(
                                    text = when {
                                        isCompleted -> "Target reached! All goal savings satisfied."
                                        paceDeficit > 0 -> "Saving ${CurrencyFormatter.formatLkr(paceDeficit)}/mo below the pace needed to meet your 12-month target on time."
                                        else -> "Great progress! Your savings velocity is on schedule to reach target by ${DateUtils.formatDate(goal.targetDateMillis)}."
                                    },
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Normal
                                    ),
                                    color = if (paceDeficit > 0) FigmaMutedBrown else Color(0xFF2E7D32)
                                )
                            }
                        }
                    }

                    // --- 3. Milestones Stepper ---
                    item {
                        InTrackCard(
                            shape = InTrackMediumCardShape,
                            elevation = 0.5.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Milestones",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = FigmaDarkText
                                    )
                                    val achievedPercent = (progress * 100).toInt()
                                    Text(
                                        text = "$achievedPercent% Achieved",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = if (achievedPercent >= 100) FigmaTerracotta else FigmaTerracottaOrange
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                val milestones = listOf(
                                    0.25f to "25%",
                                    0.50f to "50%",
                                    0.75f to "75%",
                                    1.00f to "100%"
                                )

                                val lineFraction = when {
                                    progress <= 0.25f -> 0f
                                    progress < 0.50f -> (progress - 0.25f) / 0.25f * (1f / 3f)
                                    progress < 0.75f -> (1f / 3f) + (progress - 0.50f) / 0.25f * (1f / 3f)
                                    progress < 1.00f -> (2f / 3f) + (progress - 0.75f) / 0.25f * (1f / 3f)
                                    else -> 1f
                                }

                                Box(modifier = Modifier.fillMaxWidth()) {
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

                                        drawLine(
                                            color = Color(0xFFF2ECE6),
                                            start = Offset(startX, trackY),
                                            end = Offset(endX, trackY),
                                            strokeWidth = strokeW,
                                            cap = StrokeCap.Round
                                        )

                                        val trackLength = endX - startX
                                        val activeLength = trackLength * lineFraction.coerceIn(0f, 1f)
                                        if (activeLength > 0f) {
                                            drawLine(
                                                color = FigmaTerracotta,
                                                start = Offset(startX, trackY),
                                                end = Offset(startX + activeLength, trackY),
                                                strokeWidth = strokeW,
                                                cap = StrokeCap.Round
                                            )
                                        }
                                    }

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
                                                        .background(if (isAchieved) FigmaTerracotta else Color.White)
                                                        .border(
                                                            width = if (isAchieved) 0.dp else if (isNextTarget) 2.dp else 1.5.dp,
                                                            color = if (isAchieved) Color.Transparent
                                                            else if (isNextTarget) FigmaTerracotta
                                                            else FigmaBorder,
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
                                                                    if (isNextTarget) FigmaTerracotta
                                                                    else FigmaMutedSlate.copy(alpha = 0.4f)
                                                                )
                                                        )
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(6.dp))

                                                Text(
                                                    text = label,
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                                    fontWeight = if (isAchieved || isNextTarget) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (isAchieved) FigmaTerracotta
                                                    else if (isNextTarget) FigmaDarkText
                                                    else FigmaMutedSlate
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // --- 4. What-If Simulator Action ---
                    item {
                        OutlinedButton(
                            onClick = { showWhatIfModal = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.White,
                                contentColor = FigmaDarkText
                            ),
                            border = BorderStroke(1.dp, FigmaBorder)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AutoGraph,
                                contentDescription = null,
                                tint = FigmaLightOrange,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Simulate with What-If Calculator",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }

                    // --- 5. Deposit & Windfall Contribution History Section ---
                    item {
                        InTrackCard(
                            shape = InTrackMediumCardShape,
                            elevation = 0.5.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Deposit History",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = FigmaDarkText
                                    )

                                    InTrackStatusBadge(
                                        text = "${contributions.size} Deposits",
                                        containerColor = Color(0xFFF4EEEA),
                                        contentColor = FigmaMutedBrown,
                                        fontSize = 11.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                if (contributions.isEmpty()) {
                                    Text(
                                        text = "No deposits recorded yet. Use 'Deposit Funds' to allocate savings or sweep freelance windfalls.",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 12.sp
                                        ),
                                        color = FigmaMutedSlate
                                    )
                                } else {
                                    contributions.forEachIndexed { index, contrib ->
                                        val icon = when (contrib.sourceId) {
                                            "freelance_milestone" -> Icons.Rounded.LaptopMac
                                            "monthly_salary" -> Icons.Rounded.AccountBalance
                                            "adsense_usd" -> Icons.Rounded.Language
                                            "crypto_profit" -> Icons.Rounded.CurrencyBitcoin
                                            else -> Icons.Rounded.Savings
                                        }

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(32.dp)
                                                        .clip(CircleShape)
                                                        .background(FigmaSoftPeach),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = icon,
                                                        contentDescription = null,
                                                        tint = FigmaTerracotta,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }

                                                Column {
                                                    Text(
                                                        text = contrib.sourceTitle,
                                                        style = MaterialTheme.typography.bodyMedium.copy(
                                                            fontSize = 13.sp,
                                                            fontWeight = FontWeight.SemiBold
                                                        ),
                                                        color = FigmaDarkText,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    Text(
                                                        text = DateUtils.formatDate(contrib.timestampMillis) + if (!contrib.note.isNullOrBlank()) " • ${contrib.note}" else "",
                                                        style = MaterialTheme.typography.bodySmall.copy(
                                                            fontSize = 11.sp
                                                        ),
                                                        color = FigmaMutedSlate,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }

                                            Text(
                                                text = "+ ${CurrencyFormatter.formatLkr(contrib.amountLkr)}",
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                color = FigmaTerracottaOrange
                                            )
                                        }

                                        if (index < contributions.size - 1) {
                                            HorizontalDivider(
                                                modifier = Modifier.padding(vertical = 10.dp),
                                                color = FigmaDivider,
                                                thickness = 0.8.dp
                                            )
                                        }
                                    }
                                }
                            }
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

    // --- "Add a goal value" Bottom Sheet (Figma node 2026:1549) ---
    if (showDepositSheet) {
        AddGoalValueSheet(
            goal = currentGoal,
            contributions = contributions,
            onDismiss = { showDepositSheet = false },
            onConfirmDeposit = { amount, sourceId, sourceTitle, _ ->
                viewModel.depositToGoal(
                    amountLkr = amount,
                    goalId = currentGoal.id,
                    sourceId = sourceId,
                    sourceTitle = sourceTitle
                )
                showDepositSheet = false
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Deposited ${CurrencyFormatter.formatLkr(amount)} to ${currentGoal.title}!")
                }
            }
        )
    }

    if (showWhatIfModal) {
        WhatIfCalculatorModal(
            monthlyTargetRunRate = currentGoal.monthlyTargetRunRateLkr,
            onDismiss = { showWhatIfModal = false }
        )
    }

    if (showDeleteGoalConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteGoalConfirm = false },
            title = {
                Text(text = "Delete Goal", fontWeight = FontWeight.Bold, color = FigmaDarkText)
            },
            text = {
                Text(
                    text = "Are you sure you want to delete '${currentGoal.title}'? This action cannot be undone.",
                    color = FigmaMutedSlate
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteGoal(currentGoal.id)
                        showDeleteGoalConfirm = false
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FigmaTerracotta),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteGoalConfirm = false }) {
                    Text("Cancel", color = FigmaDarkText)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(18.dp)
        )
    }
}

@Composable
private fun FigmaStatCard(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    InTrackCard(
        shape = RoundedCornerShape(18.dp),
        elevation = 0.5.dp,
        modifier = modifier
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
                        letterSpacing = 0.5.sp
                    ),
                    color = FigmaMutedSlate
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = FigmaTerracotta,
                    modifier = Modifier.size(15.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = FigmaDarkText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun GoalSpecRow(
    icon: ImageVector,
    iconTint: Color,
    label: String,
    value: String,
    secondaryValue: String? = null,
    trailing: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(FigmaIconCircleBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(19.dp)
                )
            }

            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = FigmaMutedSlate
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = FigmaDarkText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (secondaryValue != null) {
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        text = secondaryValue,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Normal
                        ),
                        color = FigmaMutedBrown.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        if (trailing != null) {
            Spacer(modifier = Modifier.width(8.dp))
            trailing()
        }
    }
}

