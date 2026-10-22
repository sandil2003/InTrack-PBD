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

import com.example.intrack.data.model.SubscriptionEntity
import com.example.intrack.data.repository.SubscriptionRepository
import com.example.intrack.data.repository.SubscriptionRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import java.util.UUID

class ExpenseViewModel(
    private val expenseRepository: ExpenseRepository = ExpenseRepositoryImpl(),
    private val subscriptionRepository: SubscriptionRepository = SubscriptionRepositoryImpl()
) : ViewModel() {

    private val _expensesState = MutableStateFlow<UiState<List<ExpenseEntity>>>(UiState.Loading)
    val expensesState: StateFlow<UiState<List<ExpenseEntity>>> = _expensesState

    init {
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: "guest_user"
        loadExpenses(currentUid)
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
        val currentUid = if (expense.userId.isNotBlank()) expense.userId else (FirebaseAuth.getInstance().currentUser?.uid ?: "guest_user")
        val preparedExpense = expense.copy(userId = currentUid)
        viewModelScope.launch {
            expenseRepository.addExpense(preparedExpense)
            if (preparedExpense.isRecurring) {
                val sub = SubscriptionEntity(
                    id = UUID.randomUUID().toString(),
                    userId = currentUid,
                    name = preparedExpense.title,
                    amountLkr = preparedExpense.amountLkr,
                    billingCycle = "MONTHLY",
                    paymentAccount = preparedExpense.paymentAccount,
                    status = "ACTIVE",
                    nextBillingMillis = preparedExpense.dateSpentMillis + 30L * 24 * 60 * 60 * 1000L,
                    isAutoPayment = true
                )
                subscriptionRepository.addSubscription(sub)
            }
        }
    }

    fun updateExpense(expense: ExpenseEntity) {
        val currentUid = if (expense.userId.isNotBlank()) expense.userId else (FirebaseAuth.getInstance().currentUser?.uid ?: "guest_user")
        val prepared = expense.copy(userId = currentUid)
        viewModelScope.launch {
            val currentState = _expensesState.value
            if (currentState is UiState.Success) {
                val updatedList = currentState.data.map { if (it.id == prepared.id) prepared else it }
                _expensesState.value = UiState.Success(updatedList)
            }
            expenseRepository.updateExpense(prepared)
        }
    }

    fun deleteExpense(userId: String, expenseId: String) {
        val currentUid = if (userId.isNotBlank()) userId else (FirebaseAuth.getInstance().currentUser?.uid ?: "guest_user")
        viewModelScope.launch {
            val currentState = _expensesState.value
            if (currentState is UiState.Success) {
                val updatedList = currentState.data.filter { it.id != expenseId }
                if (updatedList.isEmpty()) {
                    _expensesState.value = UiState.Empty
                } else {
                    _expensesState.value = UiState.Success(updatedList)
                }
            }
            expenseRepository.deleteExpense(currentUid, expenseId)
        }
    }
}
