package com.example.intrack.ui.screens.goal

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.automirrored.rounded.ExitToApp
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.intrack.data.model.GoalEntity
import com.example.intrack.ui.common.EmptyStateView
import com.example.intrack.ui.common.UiState
import com.example.intrack.ui.theme.*
import com.example.intrack.util.CurrencyFormatter
import com.example.intrack.util.DateUtils
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.launch

/**
 * Financial Goals Page matching Figma Node 2026:1227:
 * - Application Header with Profile Menu (Settings / LogOut)
 * - Section 1: Total savings overview card (2026:1248) with Centered Hero Amount & Dual-Tone progress track
 * - Section 2: Nav - Goal status filters (2026:1272) (All Goals, In Progress, Completed)
 * - Section 3: Detailed items list (2026:1279) with clean cards, progress bars, and run-rates
 * - FAB to create a new goal
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalTrackerScreen(
    onNavigateToGoalDetail: (String) -> Unit,
    onNavigateToAddGoal: (() -> Unit)? = null,
    onNavigateBack: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onSignOut: () -> Unit = {},
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
        containerColor = FigmaBg,
        topBar = {
            InTrackTopBar(
                title = "Financial Goals",
                onNavigateToSettings = onNavigateToSettings,
                onSignOut = onSignOut
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            InTrackAddFab(
                onClick = {
                    if (onNavigateToAddGoal != null) {
                        onNavigateToAddGoal()
                    } else {
                        showAddGoalDialog = true
                    }
                },
                contentDescription = "New Goal"
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            when (goalsState) {
                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = FigmaTerracotta)
                    }
                }
                is UiState.Error -> {
                    val errorMsg = (goalsState as UiState.Error).message
                    EmptyStateView(
                        title = "Error Loading Goals",
                        message = errorMsg,
                        actionLabel = "Retry",
                        onActionClick = {
                            val currentUid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: "guest_user"
                            viewModel.loadGoals(currentUid)
                        }
                    )
                }
                is UiState.Empty -> {
                    EmptyStateView(
                        title = "No Goals Created Yet",
                        message = "Set targets like a new laptop, emergency buffer, or vacation fund.",
                        actionLabel = "Create First Goal",
                        onActionClick = {
                            if (onNavigateToAddGoal != null) {
                                onNavigateToAddGoal()
                            } else {
                                showAddGoalDialog = true
                            }
                        }
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
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // --- 1. Total Savings Overview Card (Figma node 2026:1248) ---
                        item {
                            Spacer(modifier = Modifier.height(2.dp))
                            FigmaGoalsOverviewCard(
                                totalSavedLkr = totalSaved,
                                totalTargetLkr = totalTarget,
                                overallRatio = overallRatio,
                                activeCount = goals.size
                            )
                        }

                        // --- 2. Nav - Goal Status Filter Pills (Figma node 2026:1272) ---
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(
                                    "ALL" to "All Goals (${goals.size})",
                                    "IN_PROGRESS" to "In Progress ($inProgressCount)",
                                    "COMPLETED" to "Completed ($completedCount)"
                                ).forEach { (key, label) ->
                                    InTrackFilterPill(
                                        label = label,
                                        isSelected = selectedFilter == key,
                                        onClick = { selectedFilter = key }
                                    )
                                }
                            }
                        }

                        // --- 3. Detailed Items List (Figma node 2026:1279) ---
                        items(filteredGoals, key = { it.id }) { goal ->
                            FigmaGoalCard(
                                goal = goal,
                                onClick = { onNavigateToGoalDetail(goal.id) }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(60.dp))
                        }
                    }
                }
                else -> {}
            }
        }
    }

    if (showAddGoalDialog) {
        AddGoalBottomSheet(
            onDismiss = { showAddGoalDialog = false },
            onSubmit = { newGoal ->
                val currentUid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: "guest_user"
                viewModel.addGoal(newGoal.copy(userId = currentUid))
                showAddGoalDialog = false
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Created financial goal '${newGoal.title}'!")
                }
            }
        )
    }
}

/**
 * Total Savings Overview Card matching Figma node 2026:1248:
 * - Header: "TOTAL SAVED ACROSS GOALS"
 * - Centered Hero Saved Amount: LKR in Terracotta, Amount in bold dark text
 * - Dual-Tone progress capsule bar (Terracotta + Peach)
 * - Metric Details Row (Saved Progress & Target Metric)
 */
@Composable
fun FigmaGoalsOverviewCard(
    totalSavedLkr: Double,
    totalTargetLkr: Double,
    overallRatio: Float,
    activeCount: Int,
    modifier: Modifier = Modifier
) {
    val percentageInt = (overallRatio * 100).toInt()

    InTrackCard(
        shape = InTrackCardShape,
        elevation = 0.5.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            // 1. Top Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TOTAL SAVED ACROSS GOALS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp
                    ),
                    color = FigmaMutedBrown
                )
                InTrackStatusBadge(
                    text = "$percentageInt% Saved",
                    containerColor = FigmaSoftPeach,
                    contentColor = FigmaTerracotta
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Centered Large Hero Amount (Matching Figma & Daily Transactions header value)
            InTrackHeroAmount(amountLkr = totalSavedLkr)

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Master Progress Track
            InTrackProgressBar(
                progress = overallRatio,
                height = 8.dp,
                fillColor = FigmaTerracotta,
                trackColor = FigmaTrackBg
            )

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider(thickness = 0.8.dp, color = FigmaDivider)

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Metric Details Row (Saved Progress & Target Metric)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Left Column: Saved Progress
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
                                .background(FigmaTerracotta)
                        )
                        Text(
                            text = "Saved Progress",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = FigmaMutedBrown
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$percentageInt% (${CurrencyFormatter.formatLkr(totalSavedLkr)})",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.3).sp
                        ),
                        color = FigmaDarkText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Right Column: Target Metric
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
                                .background(FigmaPeachBar)
                        )
                        Text(
                            text = "Target ($activeCount Active)",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = FigmaMutedBrown
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = CurrencyFormatter.formatLkr(totalTargetLkr),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.3).sp
                        ),
                        color = FigmaDarkText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * Goal Card item matching Figma Node 2026:1279
 * Clean, responsive presentation with category icon removed matching Expenses page styling.
 */
@Composable
fun FigmaGoalCard(
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

    InTrackCard(
        shape = InTrackMediumCardShape,
        elevation = 0.5.dp,
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp)
        ) {
            // Header Row: Title & Badges/Chevron (No Category Icon)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = goal.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = FigmaDarkText,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
                    contentDescription = "View Details",
                    tint = FigmaMutedSlate.copy(alpha = 0.45f),
                    modifier = Modifier.size(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Progress Bar (Dark Orange / Terracotta matching Expenses page)
            InTrackProgressBar(
                progress = progress,
                height = 6.dp,
                fillColor = FigmaTerracotta,
                trackColor = FigmaTrackBg
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Metrics Row: Saved amount on left in smaller text size, percentage on right (right-aligned)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Saved ${CurrencyFormatter.formatLkr(goal.currentSavedLkr)}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = FigmaMutedSlate,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "$percentageInt%",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = FigmaTerracotta,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}

@Composable
fun FigmaCreateGoalDialog(
    onDismiss: () -> Unit,
    onSubmit: (GoalEntity) -> Unit
) {
    AddGoalBottomSheet(
        onDismiss = onDismiss,
        onSubmit = onSubmit
    )
}
