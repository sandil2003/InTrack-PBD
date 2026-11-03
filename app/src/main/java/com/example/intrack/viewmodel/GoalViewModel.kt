package com.example.intrack.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.intrack.data.model.GoalContributionEntity
import com.example.intrack.data.model.GoalEntity
import com.example.intrack.data.repository.AuthRepository
import com.example.intrack.data.repository.AuthRepositoryImpl
import com.example.intrack.data.repository.GoalRepository
import com.example.intrack.data.repository.GoalRepositoryImpl
import com.example.intrack.ui.common.UiState
import com.example.intrack.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

data class GoalPortfolioSummary(
    val selectedFilter: String = "ALL",
    val totalSavedLkr: Double = 0.0,
    val totalTargetLkr: Double = 0.0,
    val overallRatio: Float = 0f,
    val completedCount: Int = 0,
    val inProgressCount: Int = 0,
    val filteredGoals: List<GoalEntity> = emptyList(),
    val activeGoal: GoalEntity = GoalEntity(title = "MacBook Pro M4", monthlyTargetRunRateLkr = 40833.33)
)

data class GoalDetailMetrics(
    val goal: GoalEntity = GoalEntity(),
    val progress: Float = 0f,
    val isCompleted: Boolean = false,
    val daysLeft: Long = 0L,
    val percentageInt: Int = 0,
    val remainingToSave: Double = 0.0,
    val monthsRemaining: Double = 0.1,
    val dynamicRequiredMonthlyPace: Double = 0.0,
    val actualMonthlySavingsRate: Double = 0.0,
    val isPaceOnTrack: Boolean = true,
    val paceDeficit: Double = 0.0,
    val paceSurplus: Double = 0.0
)

@HiltViewModel
class GoalViewModel @Inject constructor(
    private val goalRepository: GoalRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    constructor(
        goalRepository: GoalRepository = GoalRepositoryImpl()
    ) : this(goalRepository, AuthRepositoryImpl())

    private val _goalsState = MutableStateFlow<UiState<List<GoalEntity>>>(UiState.Loading)
    val goalsState: StateFlow<UiState<List<GoalEntity>>> = _goalsState

    private val _contributionsState = MutableStateFlow<Map<String, List<GoalContributionEntity>>>(emptyMap())
    val contributionsState: StateFlow<Map<String, List<GoalContributionEntity>>> = _contributionsState

    private val _selectedFilter = MutableStateFlow("ALL")
    val selectedFilter: StateFlow<String> = _selectedFilter

    private val _portfolioSummary = MutableStateFlow(GoalPortfolioSummary())
    val portfolioSummary: StateFlow<GoalPortfolioSummary> = _portfolioSummary

    init {
        loadGoals()
    }

    fun setSelectedFilter(filter: String) {
        _selectedFilter.value = filter
        recomputePortfolioSummary()
    }

    private fun recomputePortfolioSummary() {
        val goals = (_goalsState.value as? UiState.Success<List<GoalEntity>>)?.data ?: emptyList()
        val filter = _selectedFilter.value

        val totalSaved = goals.sumOf { it.currentSavedLkr }
        val totalTarget = goals.sumOf { it.targetAmountLkr }
        val overallRatio = if (totalTarget > 0.0) (totalSaved / totalTarget).toFloat().coerceIn(0f, 1f) else 0f

        val completedCount = goals.count { it.isCompleted || (it.targetAmountLkr > 0.0 && it.currentSavedLkr >= it.targetAmountLkr) }
        val inProgressCount = goals.size - completedCount

        val filteredGoals = goals.filter { g ->
            val isDone = g.isCompleted || (g.targetAmountLkr > 0.0 && g.currentSavedLkr >= g.targetAmountLkr)
            when (filter) {
                "ACTIVE", "IN_PROGRESS" -> !isDone
                "DONE", "COMPLETED" -> isDone
                else -> true
            }
        }

        val activeGoal = goals.firstOrNull { !it.isCompleted }
            ?: goals.firstOrNull()
            ?: GoalEntity(title = "MacBook Pro M4", monthlyTargetRunRateLkr = 40833.33)

        _portfolioSummary.value = GoalPortfolioSummary(
            selectedFilter = filter,
            totalSavedLkr = totalSaved,
            totalTargetLkr = totalTarget,
            overallRatio = overallRatio,
            completedCount = completedCount,
            inProgressCount = inProgressCount,
            filteredGoals = filteredGoals,
            activeGoal = activeGoal
        )
    }

    fun calculateGoalDetailMetrics(goalId: String, now: Long = System.currentTimeMillis()): GoalDetailMetrics {
        val goals = (_goalsState.value as? UiState.Success<List<GoalEntity>>)?.data ?: emptyList()
        val goal = goals.find { it.id == goalId } ?: goals.firstOrNull() ?: GoalEntity()

        val progress = if (goal.targetAmountLkr > 0) {
            (goal.currentSavedLkr / goal.targetAmountLkr).toFloat().coerceIn(0f, 1f)
        } else 0f
        val isCompleted = goal.isCompleted || progress >= 1f
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
        val actualMonthlySavingsRate = goal.currentSavedLkr / elapsedMonths

        val isPaceOnTrack = isCompleted || (actualMonthlySavingsRate >= dynamicRequiredMonthlyPace)
        val paceDeficit = (dynamicRequiredMonthlyPace - actualMonthlySavingsRate).coerceAtLeast(0.0)
        val paceSurplus = (actualMonthlySavingsRate - dynamicRequiredMonthlyPace).coerceAtLeast(0.0)

        return GoalDetailMetrics(
            goal = goal,
            progress = progress,
            isCompleted = isCompleted,
            daysLeft = daysLeft,
            percentageInt = percentageInt,
            remainingToSave = remainingToSave,
            monthsRemaining = monthsRemaining,
            dynamicRequiredMonthlyPace = dynamicRequiredMonthlyPace,
            actualMonthlySavingsRate = actualMonthlySavingsRate,
            isPaceOnTrack = isPaceOnTrack,
            paceDeficit = paceDeficit,
            paceSurplus = paceSurplus
        )
    }

    fun loadGoals(userId: String = authRepository.getCurrentUserId()) {
        viewModelScope.launch {
            _goalsState.value = UiState.Loading
            try {
                goalRepository.getGoals(userId)
                    .catch { e ->
                        Log.e("GoalViewModel", "Error loading goals: ${e.message}", e)
                        _goalsState.value = UiState.Error(e.message ?: "Failed to load goals")
                    }
                    .collect { goals ->
                        if (goals.isEmpty()) {
                            _goalsState.value = UiState.Empty
                        } else {
                            _goalsState.value = UiState.Success(goals)
                        }
                        recomputePortfolioSummary()
                    }
            } catch (e: Exception) {
                Log.e("GoalViewModel", "Exception in loadGoals: ${e.message}", e)
                _goalsState.value = UiState.Error(e.message ?: "Failed to load goals")
            }
        }
    }

    fun loadContributions(goalId: String, userId: String? = null) {
        val currentUid = userId?.takeIf { it.isNotBlank() } ?: authRepository.getCurrentUserId()
        viewModelScope.launch {
            try {
                goalRepository.getGoalContributions(currentUid, goalId)
                    .catch { e ->
                        Log.e("GoalViewModel", "Error loading contributions: ${e.message}", e)
                        val fallback = goalRepository.getDefaultSampleContributions(currentUid, goalId)
                        _contributionsState.value = _contributionsState.value + (goalId to fallback)
                    }
                    .collect { list ->
                        _contributionsState.value = _contributionsState.value + (goalId to list)
                    }
            } catch (e: Exception) {
                Log.e("GoalViewModel", "Exception in loadContributions: ${e.message}", e)
                val fallback = goalRepository.getDefaultSampleContributions(currentUid, goalId)
                _contributionsState.value = _contributionsState.value + (goalId to fallback)
            }
        }
    }

    fun addGoal(goal: GoalEntity) {
        val currentUid = goal.userId.ifBlank { authRepository.getCurrentUserId() }
        val preparedGoal = goal.copy(userId = currentUid)
        viewModelScope.launch {
            try {
                val currentState = _goalsState.value
                if (currentState is UiState.Success) {
                    _goalsState.value = UiState.Success(currentState.data + preparedGoal)
                } else {
                    _goalsState.value = UiState.Success(listOf(preparedGoal))
                }
                recomputePortfolioSummary()
                goalRepository.addGoal(preparedGoal)
            } catch (e: Exception) {
                Log.e("GoalViewModel", "Error adding goal: ${e.message}", e)
            }
        }
    }

    fun updateGoal(goal: GoalEntity) {
        val currentUid = goal.userId.ifBlank { authRepository.getCurrentUserId() }
        val preparedGoal = goal.copy(userId = currentUid)
        viewModelScope.launch {
            try {
                val currentState = _goalsState.value
                if (currentState is UiState.Success) {
                    val updatedList = currentState.data.map { if (it.id == preparedGoal.id) preparedGoal else it }
                    _goalsState.value = UiState.Success(updatedList)
                    recomputePortfolioSummary()
                }
                goalRepository.updateGoal(preparedGoal)
            } catch (e: Exception) {
                Log.e("GoalViewModel", "Error updating goal: ${e.message}", e)
            }
        }
    }

    fun depositToGoal(
        amountLkr: Double,
        goalId: String = "macbook_m4_goal",
        sourceTitle: String = "Direct Contribution",
        sourceId: String = "manual",
        note: String? = null,
        userId: String? = null
    ) {
        val currentUid = userId?.takeIf { it.isNotBlank() } ?: authRepository.getCurrentUserId()
        viewModelScope.launch {
            try {
                val contribution = GoalContributionEntity(
                    id = UUID.randomUUID().toString(),
                    goalId = goalId,
                    userId = currentUid,
                    amountLkr = amountLkr,
                    sourceTitle = sourceTitle,
                    sourceId = sourceId,
                    timestampMillis = System.currentTimeMillis(),
                    note = note
                )

                // Optimistically update contributions state
                val currentContribs = _contributionsState.value[goalId] ?: emptyList()
                _contributionsState.value = _contributionsState.value + (goalId to (listOf(contribution) + currentContribs))

                // Optimistically update goals state
                val currentGoalsState = _goalsState.value
                if (currentGoalsState is UiState.Success) {
                    val updatedGoals = currentGoalsState.data.map { g ->
                        if (g.id == goalId) {
                            val newSaved = g.currentSavedLkr + amountLkr
                            g.copy(
                                currentSavedLkr = newSaved,
                                isCompleted = newSaved >= g.targetAmountLkr,
                                contributions = listOf(contribution) + g.contributions
                            )
                        } else g
                    }
                    _goalsState.value = UiState.Success(updatedGoals)
                    recomputePortfolioSummary()
                }

                goalRepository.addGoalContribution(currentUid, contribution)
            } catch (e: Exception) {
                Log.e("GoalViewModel", "Error depositing to goal: ${e.message}", e)
            }
        }
    }

    fun allocateWindfall(amountLkr: Double, goalId: String = "macbook_m4_goal", userId: String? = null) {
        depositToGoal(
            amountLkr = amountLkr,
            goalId = goalId,
            sourceTitle = "Windfall Allocation",
            sourceId = "manual",
            userId = userId
        )
    }

    fun deleteGoal(goalId: String, userId: String? = null) {
        val currentUid = userId?.takeIf { it.isNotBlank() } ?: authRepository.getCurrentUserId()
        viewModelScope.launch {
            try {
                val currentState = _goalsState.value
                if (currentState is UiState.Success) {
                    val updatedList = currentState.data.filter { it.id != goalId }
                    if (updatedList.isEmpty()) {
                        _goalsState.value = UiState.Empty
                    } else {
                        _goalsState.value = UiState.Success(updatedList)
                    }
                    recomputePortfolioSummary()
                }
                goalRepository.deleteGoal(currentUid, goalId)
            } catch (e: Exception) {
                Log.e("GoalViewModel", "Error deleting goal: ${e.message}", e)
            }
        }
    }
}
