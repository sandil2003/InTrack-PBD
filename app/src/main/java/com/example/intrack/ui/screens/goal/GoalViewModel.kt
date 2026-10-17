package com.example.intrack.ui.screens.goal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.intrack.data.model.GoalEntity
import com.example.intrack.data.repository.GoalRepository
import com.example.intrack.data.repository.GoalRepositoryImpl
import com.example.intrack.ui.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GoalViewModel(
    private val goalRepository: GoalRepository = GoalRepositoryImpl()
) : ViewModel() {

    private val _goalsState = MutableStateFlow<UiState<List<GoalEntity>>>(UiState.Loading)
    val goalsState: StateFlow<UiState<List<GoalEntity>>> = _goalsState

    init {
        loadGoals("guest_user")
    }

    fun loadGoals(userId: String) {
        viewModelScope.launch {
            _goalsState.value = UiState.Loading
            goalRepository.getGoals(userId).collect { goals ->
                if (goals.isEmpty()) {
                    _goalsState.value = UiState.Empty
                } else {
                    _goalsState.value = UiState.Success(goals)
                }
            }
        }
    }

    fun addGoal(goal: GoalEntity) {
        viewModelScope.launch {
            goalRepository.addGoal(goal.copy(userId = "guest_user"))
        }
    }

    fun allocateWindfall(amountLkr: Double, goalId: String = "macbook_m4_goal") {
        viewModelScope.launch {
            goalRepository.allocateWindfallToGoal("guest_user", amountLkr, goalId)
        }
    }

    fun deleteGoal(goalId: String, userId: String = "guest_user") {
        viewModelScope.launch {
            goalRepository.deleteGoal(userId, goalId)
        }
    }
}
