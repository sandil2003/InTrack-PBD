package com.example.intrack.ui.screens.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.intrack.data.model.ExpenseEntity
import com.example.intrack.data.repository.ExpenseRepository
import com.example.intrack.data.repository.ExpenseRepositoryImpl
import com.example.intrack.ui.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ExpenseViewModel(
    private val expenseRepository: ExpenseRepository = ExpenseRepositoryImpl()
) : ViewModel() {

    private val _expensesState = MutableStateFlow<UiState<List<ExpenseEntity>>>(UiState.Loading)
    val expensesState: StateFlow<UiState<List<ExpenseEntity>>> = _expensesState

    init {
        loadExpenses("guest_user")
    }

    fun loadExpenses(userId: String) {
        viewModelScope.launch {
            _expensesState.value = UiState.Loading
            expenseRepository.getExpenses(userId).collect { list ->
                if (list.isEmpty()) {
                    _expensesState.value = UiState.Empty
                } else {
                    _expensesState.value = UiState.Success(list)
                }
            }
        }
    }

    fun addExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            expenseRepository.addExpense(expense)
        }
    }
}
