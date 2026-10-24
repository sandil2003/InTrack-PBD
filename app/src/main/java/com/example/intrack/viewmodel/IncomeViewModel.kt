package com.example.intrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.intrack.data.model.IncomeEntity
import com.example.intrack.data.repository.IncomeRepository
import com.example.intrack.data.repository.IncomeRepositoryImpl
import com.example.intrack.ui.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

import com.google.firebase.auth.FirebaseAuth

class IncomeViewModel(
    private val incomeRepository: IncomeRepository = IncomeRepositoryImpl()
) : ViewModel() {

    private val _incomesState = MutableStateFlow<UiState<List<IncomeEntity>>>(UiState.Loading)
    val incomesState: StateFlow<UiState<List<IncomeEntity>>> = _incomesState

    init {
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: "guest_user"
        loadIncomes(currentUid)
    }

    fun loadIncomes(userId: String) {
        viewModelScope.launch {
            _incomesState.value = UiState.Loading
            incomeRepository.getIncomes(userId).collect { list ->
                if (list.isEmpty()) {
                    _incomesState.value = UiState.Empty
                } else {
                    _incomesState.value = UiState.Success(list)
                }
            }
        }
    }

    fun addIncome(income: IncomeEntity) {
        val currentUid = if (income.userId.isNotBlank()) income.userId else (FirebaseAuth.getInstance().currentUser?.uid ?: "guest_user")
        val prepared = income.copy(userId = currentUid)
        viewModelScope.launch {
            incomeRepository.addIncome(prepared)
        }
    }

    fun updateIncome(income: IncomeEntity) {
        val currentUid = if (income.userId.isNotBlank()) income.userId else (FirebaseAuth.getInstance().currentUser?.uid ?: "guest_user")
        val prepared = income.copy(userId = currentUid)
        viewModelScope.launch {
            incomeRepository.updateIncome(prepared)
        }
    }

    fun deleteIncome(incomeId: String, userId: String? = null) {
        val currentUid = userId ?: (FirebaseAuth.getInstance().currentUser?.uid ?: "guest_user")
        viewModelScope.launch {
            incomeRepository.deleteIncome(currentUid, incomeId)
        }
    }
}
