package com.example.intrack.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.intrack.data.model.ExpenseEntity
import com.example.intrack.data.model.GoalEntity
import com.example.intrack.data.repository.ExpenseRepository
import com.example.intrack.data.repository.ExpenseRepositoryImpl
import com.example.intrack.data.repository.GoalRepository
import com.example.intrack.data.repository.GoalRepositoryImpl
import com.example.intrack.data.repository.IncomeRepository
import com.example.intrack.data.repository.IncomeRepositoryImpl
import com.example.intrack.ui.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val incomeRepository: IncomeRepository = IncomeRepositoryImpl(),
    private val expenseRepository: ExpenseRepository = ExpenseRepositoryImpl(),
    private val goalRepository: GoalRepository = GoalRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<DashboardData>>(UiState.Loading)
    val uiState: StateFlow<UiState<DashboardData>> = _uiState

    init {
        loadDashboard("guest_user")
    }

    fun loadDashboard(userId: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading

            combine(
                incomeRepository.getIncomes(userId),
                expenseRepository.getExpenses(userId),
                goalRepository.getGoals(userId)
            ) { incomes, expenses, goals ->
                val totalIncome = incomes.sumOf { it.realizedAmountLkr }
                val totalExpense = expenses.sumOf { it.amountLkr }
                val committed = expenses.filter { it.commitmentType == "COMMITTED" }.sumOf { it.amountLkr }
                val discretionary = expenses.filter { it.commitmentType == "DISCRETIONARY" }.sumOf { it.amountLkr }

                val diningOutExpense = expenses.filter { it.category == "DINING_OUT" }.sumOf { it.amountLkr }
                val macBookMonthlyQuota = 40833.33
                val diningPercentage = if (macBookMonthlyQuota > 0) ((diningOutExpense / macBookMonthlyQuota) * 100).toInt() else 0

                val primaryGoal = goals.find { it.id == "macbook_m4_goal" }
                    ?: goals.firstOrNull()
                    ?: GoalEntity()

                val shockInsight = if (diningPercentage > 0) {
                    "LKR ${diningOutExpense.toInt()} spent on coffee & takeout this month equals ${diningPercentage}% of your monthly MacBook quota."
                } else {
                    "Keep discretionary spending low to reach your financial goals faster!"
                }

                DashboardData(
                    totalIncomeLkr = totalIncome,
                    totalExpenseLkr = totalExpense,
                    committedExpenseLkr = committed,
                    discretionaryExpenseLkr = discretionary,
                    goals = goals,
                    macBookGoalProgress = primaryGoal,
                    recentTransactions = expenses,
                    shockInsightMessage = shockInsight
                )
            }.collect { data ->
                _uiState.value = UiState.Success(data)
            }
        }
    }

    fun logExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            expenseRepository.addExpense(expense)
        }
    }

    fun allocateWindfallToGoal(amountLkr: Double, goalId: String = "macbook_m4_goal") {
        viewModelScope.launch {
            goalRepository.allocateWindfallToGoal("guest_user", amountLkr, goalId)
        }
    }
}
