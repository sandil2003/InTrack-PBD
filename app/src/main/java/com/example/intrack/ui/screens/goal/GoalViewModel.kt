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

import com.google.firebase.auth.FirebaseAuth

class GoalViewModel(
    private val goalRepository: GoalRepository = GoalRepositoryImpl()
) : ViewModel() {

    private val _goalsState = MutableStateFlow<UiState<List<GoalEntity>>>(UiState.Loading)
    val goalsState: StateFlow<UiState<List<GoalEntity>>> = _goalsState

    init {
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: "guest_user"
        loadGoals(currentUid)
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
        val currentUid = if (goal.userId.isNotBlank()) goal.userId else (FirebaseAuth.getInstance().currentUser?.uid ?: "guest_user")
        val preparedGoal = goal.copy(userId = currentUid)
        viewModelScope.launch {
            goalRepository.addGoal(preparedGoal)
        }
    }

    fun updateGoal(goal: GoalEntity) {
        val currentUid = if (goal.userId.isNotBlank()) goal.userId else (FirebaseAuth.getInstance().currentUser?.uid ?: "guest_user")
        val preparedGoal = goal.copy(userId = currentUid)
        viewModelScope.launch {
            val currentState = _goalsState.value
            if (currentState is UiState.Success) {
                val updatedList = currentState.data.map { if (it.id == preparedGoal.id) preparedGoal else it }
                _goalsState.value = UiState.Success(updatedList)
            }
            goalRepository.updateGoal(preparedGoal)
        }
    }

    fun allocateWindfall(amountLkr: Double, goalId: String = "macbook_m4_goal", userId: String? = null) {
        val currentUid = userId ?: (FirebaseAuth.getInstance().currentUser?.uid ?: "guest_user")
        viewModelScope.launch {
            goalRepository.allocateWindfallToGoal(currentUid, amountLkr, goalId)
        }
    }

    fun deleteGoal(goalId: String, userId: String? = null) {
        val currentUid = userId ?: (FirebaseAuth.getInstance().currentUser?.uid ?: "guest_user")
        viewModelScope.launch {
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
        }
    }
}
