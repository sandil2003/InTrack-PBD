package com.example.intrack.ui.screens.goal

import com.example.intrack.viewmodel.GoalViewModel
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.TrackChanges
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
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
import kotlinx.coroutines.launch

/**
 * Financial Goals Page matching InTrack Sunset Tangerine Design System:
 * - Application Header with Profile Menu (Settings / LogOut) & Subtitle
 * - Section 1: Total savings overview card with Centered Hero Amount, dual-tone progress, and 3-pillar breakdown
 * - Section 2: Nav - Goal status filter pills (All Goals, In Progress, Completed)
 * - Section 3: Detailed items list with category icons, progress tracks, timeline badges, and clear metrics
 * - Section 4: Friendly empty state when filter yields zero results
 * - Floating Action Button to create a new goal
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
    var depositGoal by remember { mutableStateOf<GoalEntity?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = FigmaBg,
        topBar = {
            InTrackTopBar(
                title = "Goals",
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
                contentDescription = "New Financial Goal"
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
                        icon = Icons.Rounded.TrackChanges,
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
                    val overallRatio = if (totalTarget > 0.0) (totalSaved / totalTarget).toFloat().coerceIn(0f, 1f) else 0f

                    val completedCount = goals.count { it.isCompleted || (it.targetAmountLkr > 0.0 && it.currentSavedLkr >= it.targetAmountLkr) }
                    val inProgressCount = goals.size - completedCount

                    val filteredGoals = goals.filter { g ->
                        val isDone = g.isCompleted || (g.targetAmountLkr > 0.0 && g.currentSavedLkr >= g.targetAmountLkr)
                        when (selectedFilter) {
                            "ACTIVE", "IN_PROGRESS" -> !isDone
                            "DONE", "COMPLETED" -> isDone
                            else -> true
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // --- 1. Total Savings Overview Card ---
                        item {
                            Spacer(modifier = Modifier.height(2.dp))
                            FigmaGoalsOverviewCard(
                                totalSavedLkr = totalSaved,
                                totalTargetLkr = totalTarget,
                                overallRatio = overallRatio,
                                activeCount = inProgressCount,
                                completedCount = completedCount
                            )
                        }

                        // --- 2. Nav - Goal Status Filter Pills ---
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(
                                    "ALL" to "All",
                                    "ACTIVE" to "Active",
                                    "DONE" to "Done"
                                ).forEach { (key, label) ->
                                    InTrackFilterPill(
                                        label = label,
                                        isSelected = selectedFilter == key,
                                        onClick = { selectedFilter = key }
                                    )
                                }
                            }
                        }

                        // --- 3. Detailed Items List or Filter Empty State ---
                        if (filteredGoals.isEmpty()) {
                            item {
                                EmptyFilterGoalsCard(
                                    filter = selectedFilter,
                                    onResetFilter = { selectedFilter = "ALL" }
                                )
                            }
                        } else {
                            items(filteredGoals, key = { it.id }) { goal ->
                                FigmaGoalCard(
                                    goal = goal,
                                    onClick = { onNavigateToGoalDetail(goal.id) },
                                    onDepositClick = { depositGoal = goal }
                                )
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

    depositGoal?.let { targetGoal ->
        AddGoalValueSheet(
            goal = targetGoal,
            onDismiss = { depositGoal = null },
            onConfirmDeposit = { amount, sourceId, sourceTitle, _ ->
                viewModel.depositToGoal(
                    amountLkr = amount,
                    goalId = targetGoal.id,
                    sourceId = sourceId,
                    sourceTitle = sourceTitle
                )
                depositGoal = null
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Deposited ${CurrencyFormatter.formatLkr(amount)} to '${targetGoal.title}'!")
                }
            }
        )
    }
}

/**
 * Goal Overview Card matching InTrack Design System:
 * - Clean minimal hero card focusing on overall progress
 * - Large progress percentage (e.g. '68% Achieved')
 * - Active & Done goal count chips
 * - Smooth animated progress bar
 */
@Composable
fun FigmaGoalsOverviewCard(
    totalSavedLkr: Double = 0.0,
    totalTargetLkr: Double = 0.0,
    overallRatio: Float,
    activeCount: Int,
    completedCount: Int = 0,
    modifier: Modifier = Modifier
) {
    val isFullyCompleted = (activeCount == 0 && completedCount > 0) || (overallRatio >= 1f && (totalTargetLkr > 0.0 || completedCount > 0))
    val effectiveRatio = if (isFullyCompleted) 1f else overallRatio.coerceIn(0f, 1f)
    val percentageInt = (effectiveRatio * 100).toInt()

    InTrackCard(
        shape = InTrackCardShape,
        elevation = 0.5.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            // 1. Top Header Row: Track icon + "OVERALL PROGRESS" label, and Active / Done count chips
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
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(FigmaSoftPeach),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.TrackChanges,
                            contentDescription = null,
                            tint = FigmaTerracotta,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Text(
                        text = "OVERALL PROGRESS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = FigmaMutedBrown
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    InTrackStatusBadge(
                        text = "$activeCount Active",
                        containerColor = FigmaSoftPeach,
                        contentColor = FigmaTerracotta,
                        fontSize = 11.sp
                    )
                    InTrackStatusBadge(
                        text = "$completedCount Done",
                        containerColor = Color(0xFFE8F5E9),
                        contentColor = Color(0xFF2E7D32),
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Large Progress Percentage
            Text(
                text = if (isFullyCompleted) "100% Achieved 🎉" else "$percentageInt% Achieved",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                ),
                color = FigmaDarkText
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Master Progress Track with smooth animation
            InTrackProgressBar(
                progress = effectiveRatio,
                height = 8.dp,
                fillColor = if (isFullyCompleted) Color(0xFF2E7D32) else FigmaTerracotta,
                trackColor = FigmaTrackBg,
                animate = true
            )
        }
    }
}

/**
 * Goal Card item matching InTrack design system:
 * - Clean inline row: Category icon, Goal title, timeline badge
 * - Smooth animated progress bar
 * - Below progress bar: completion percentage on the left, compact interactive + Deposit pill button on the right (or Completed ✨ if done)
 * - Tapping card opens GoalDetailScreen
 */
@Composable
fun FigmaGoalCard(
    goal: GoalEntity,
    onClick: () -> Unit,
    onDepositClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val progress = if (goal.targetAmountLkr > 0.0) {
        (goal.currentSavedLkr / goal.targetAmountLkr).toFloat().coerceIn(0f, 1f)
    } else 0f
    val isCompleted = goal.isCompleted || progress >= 1f
    val effectiveProgress = if (isCompleted) 1f else progress
    val percentageInt = (effectiveProgress * 100).toInt()

    val now = System.currentTimeMillis()
    val isOverdue = !isCompleted && goal.targetDateMillis < now
    val daysLeft = DateUtils.daysBetween(now, goal.targetDateMillis).coerceAtLeast(0L)

    val categoryIcon = GoalCategoryHelper.getCategoryIcon(goal.category)
    val categoryLabel = GoalCategoryHelper.getCategoryLabel(goal.category)

    val (badgeText, badgeColor, badgeWeight) = when {
        isCompleted -> Triple("• Done 🎉", Color(0xFF2E7D32), FontWeight.SemiBold)
        isOverdue -> Triple("• Overdue", FigmaTerracotta, FontWeight.Bold)
        daysLeft == 0L -> Triple("• Due today", FigmaTerracotta, FontWeight.Bold)
        else -> Triple("• ${daysLeft}d left", FigmaMutedSlate, FontWeight.Normal)
    }

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
            // Clean inline row: Category icon, Goal title, timeline badge & Chevron
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Category Icon Circle
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (isCompleted) Color(0xFFE8F5E9) else FigmaSoftPeach),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = categoryIcon,
                            contentDescription = categoryLabel,
                            tint = if (isCompleted) Color(0xFF2E7D32) else FigmaTerracotta,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    Text(
                        text = goal.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = FigmaDarkText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    // Timeline badge with guaranteed single-line layout
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.5.sp,
                            fontWeight = badgeWeight
                        ),
                        color = badgeColor,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
                    contentDescription = "View Details",
                    tint = FigmaMutedSlate.copy(alpha = 0.35f),
                    modifier = Modifier.size(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Smooth animated progress bar (100% full when completed)
            InTrackProgressBar(
                progress = effectiveProgress,
                height = 6.dp,
                fillColor = if (isCompleted) Color(0xFF2E7D32) else FigmaTerracotta,
                trackColor = FigmaTrackBg,
                animate = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Row: Completion % on left, compact interactive + Deposit pill button on right (or Completed ✨ if done)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isCompleted) "100%" else "$percentageInt%",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (isCompleted) Color(0xFF2E7D32) else FigmaTerracotta,
                    maxLines = 1,
                    softWrap = false
                )

                if (!isCompleted && onDepositClick != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(FigmaSoftPeach)
                            .clickable(
                                role = Role.Button,
                                onClickLabel = "Deposit to ${goal.title}",
                                onClick = onDepositClick
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = null,
                                tint = FigmaTerracotta,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "Deposit",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = FigmaTerracotta,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                } else if (isCompleted) {
                    Text(
                        text = "Completed ✨",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color(0xFF2E7D32),
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}

/**
 * Clean empty state card when selected filter has no matching goals.
 */
@Composable
private fun EmptyFilterGoalsCard(
    filter: String,
    onResetFilter: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDoneFilter = filter == "DONE" || filter == "COMPLETED"
    InTrackCard(
        shape = InTrackMediumCardShape,
        elevation = 0.5.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp, horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(FigmaSoftPeach),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isDoneFilter) Icons.Rounded.TrackChanges else Icons.Rounded.Savings,
                    contentDescription = null,
                    tint = FigmaTerracotta,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = if (isDoneFilter) "No Completed Goals Yet" else "No Goals in Progress",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = FigmaDarkText,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (isDoneFilter) {
                    "Keep saving toward your active targets! Once reached, completed goals will show here."
                } else {
                    "All your goals are completed! Create a new financial goal to keep building wealth."
                },
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                ),
                color = FigmaMutedSlate,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))

            TextButton(
                onClick = onResetFilter,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "View All Goals",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = FigmaTerracotta
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
