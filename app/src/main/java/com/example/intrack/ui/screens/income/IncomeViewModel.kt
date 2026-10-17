package com.example.intrack.ui.screens.income

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.intrack.data.model.IncomeEntity
import com.example.intrack.data.repository.IncomeRepository
import com.example.intrack.data.repository.IncomeRepositoryImpl
import com.example.intrack.ui.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class IncomeViewModel(
    private val incomeRepository: IncomeRepository = IncomeRepositoryImpl()
) : ViewModel() {

    private val _incomesState = MutableStateFlow<UiState<List<IncomeEntity>>>(UiState.Loading)
    val incomesState: StateFlow<UiState<List<IncomeEntity>>> = _incomesState

    init {
        loadIncomes("guest_user")
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
        viewModelScope.launch {
            incomeRepository.addIncome(income)
        }
    }

    fun updateIncome(income: IncomeEntity) {
        viewModelScope.launch {
            incomeRepository.updateIncome(income)
        }
    }

    fun deleteIncome(incomeId: String, userId: String = "guest_user") {
        viewModelScope.launch {
            incomeRepository.deleteIncome(userId, incomeId)
        }
    }
}
