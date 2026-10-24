package com.example.intrack.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.intrack.data.model.GoalContributionEntity
import com.example.intrack.data.model.GoalEntity
import com.example.intrack.data.repository.GoalRepository
import com.example.intrack.data.repository.GoalRepositoryImpl
import com.example.intrack.ui.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

import com.google.firebase.auth.FirebaseAuth

class GoalViewModel(
    private val goalRepository: GoalRepository = GoalRepositoryImpl()
) : ViewModel() {

    private val _goalsState = MutableStateFlow<UiState<List<GoalEntity>>>(UiState.Loading)
    val goalsState: StateFlow<UiState<List<GoalEntity>>> = _goalsState

    private val _contributionsState = MutableStateFlow<Map<String, List<GoalContributionEntity>>>(emptyMap())
    val contributionsState: StateFlow<Map<String, List<GoalContributionEntity>>> = _contributionsState

    init {
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: "guest_user"
        loadGoals(currentUid)
    }

    fun loadGoals(userId: String) {
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
                    }
            } catch (e: Exception) {
                Log.e("GoalViewModel", "Exception in loadGoals: ${e.message}", e)
                _goalsState.value = UiState.Error(e.message ?: "Failed to load goals")
            }
        }
    }

    fun loadContributions(goalId: String, userId: String? = null) {
        val currentUid = userId ?: (FirebaseAuth.getInstance().currentUser?.uid ?: "guest_user")
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
        val currentUid = if (goal.userId.isNotBlank()) goal.userId else (FirebaseAuth.getInstance().currentUser?.uid ?: "guest_user")
        val preparedGoal = goal.copy(userId = currentUid)
        viewModelScope.launch {
            try {
                val currentState = _goalsState.value
                if (currentState is UiState.Success) {
                    _goalsState.value = UiState.Success(currentState.data + preparedGoal)
                } else {
                    _goalsState.value = UiState.Success(listOf(preparedGoal))
                }
                goalRepository.addGoal(preparedGoal)
            } catch (e: Exception) {
                Log.e("GoalViewModel", "Error adding goal: ${e.message}", e)
            }
        }
    }

    fun updateGoal(goal: GoalEntity) {
        val currentUid = if (goal.userId.isNotBlank()) goal.userId else (FirebaseAuth.getInstance().currentUser?.uid ?: "guest_user")
        val preparedGoal = goal.copy(userId = currentUid)
        viewModelScope.launch {
            try {
                val currentState = _goalsState.value
                if (currentState is UiState.Success) {
                    val updatedList = currentState.data.map { if (it.id == preparedGoal.id) preparedGoal else it }
                    _goalsState.value = UiState.Success(updatedList)
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
        val currentUid = userId ?: (FirebaseAuth.getInstance().currentUser?.uid ?: "guest_user")
        viewModelScope.launch {
            try {
                val contribution = GoalContributionEntity(
                    id = java.util.UUID.randomUUID().toString(),
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
        val currentUid = userId ?: (FirebaseAuth.getInstance().currentUser?.uid ?: "guest_user")
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
                }
                goalRepository.deleteGoal(currentUid, goalId)
            } catch (e: Exception) {
                Log.e("GoalViewModel", "Error deleting goal: ${e.message}", e)
            }
        }
    }
}
