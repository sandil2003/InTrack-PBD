package com.example.intrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.intrack.data.model.ExpenseEntity
import com.example.intrack.data.model.GoalEntity
import com.example.intrack.data.model.SubscriptionEntity
import com.example.intrack.data.repository.AuthRepository
import com.example.intrack.data.repository.AuthRepositoryImpl
import com.example.intrack.data.repository.ExpenseRepository
import com.example.intrack.data.repository.ExpenseRepositoryImpl
import com.example.intrack.data.repository.GoalRepository
import com.example.intrack.data.repository.GoalRepositoryImpl
import com.example.intrack.data.repository.IncomeRepository
import com.example.intrack.data.repository.IncomeRepositoryImpl
import com.example.intrack.data.repository.SubscriptionRepository
import com.example.intrack.data.repository.SubscriptionRepositoryImpl
import com.example.intrack.ui.common.UiState
import com.example.intrack.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val incomeRepository: IncomeRepository,
    private val expenseRepository: ExpenseRepository,
    private val goalRepository: GoalRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    constructor(
        incomeRepository: IncomeRepository = IncomeRepositoryImpl(),
        expenseRepository: ExpenseRepository = ExpenseRepositoryImpl(),
        goalRepository: GoalRepository = GoalRepositoryImpl(),
        subscriptionRepository: SubscriptionRepository = SubscriptionRepositoryImpl()
    ) : this(incomeRepository, expenseRepository, goalRepository, subscriptionRepository, AuthRepositoryImpl())

    private val _uiState = MutableStateFlow<UiState<DashboardData>>(UiState.Loading)
    val uiState: StateFlow<UiState<DashboardData>> = _uiState

    private val _userGreetingName = MutableStateFlow("User")
    val userGreetingName: StateFlow<String> = _userGreetingName

    private val _userInitials = MutableStateFlow("U")
    val userInitials: StateFlow<String> = _userInitials

    private val _selectedCategoryFilter = MutableStateFlow("ALL")
    val selectedCategoryFilter: StateFlow<String> = _selectedCategoryFilter

    private val _selectedAccountFilter = MutableStateFlow("ALL")
    val selectedAccountFilter: StateFlow<String> = _selectedAccountFilter

    init {
        loadDashboard()
    }

    fun setCategoryFilter(category: String) {
        _selectedCategoryFilter.value = category
    }

    fun setAccountFilter(account: String) {
        _selectedAccountFilter.value = account
    }

    fun loadDashboard(userId: String = authRepository.getCurrentUserId()) {
        viewModelScope.launch {
            val fallbackName = authRepository.getCurrentUserDisplayName()
            if (fallbackName.isNotBlank()) {
                _userGreetingName.value = fallbackName.trim().split(" ").firstOrNull { it.isNotBlank() } ?: fallbackName
                _userInitials.value = fallbackName.trim().take(1).uppercase()
            }
            if (userId.isNotBlank()) {
                launch {
                    authRepository.observeUserProfile(userId).collect { profile ->
                        if (profile != null) {
                            _userGreetingName.value = profile.firstName
                            _userInitials.value = profile.initials
                        }
                    }
                }
            }
        }

        viewModelScope.launch {
            _uiState.value = UiState.Loading

            combine(
                incomeRepository.getIncomes(userId),
                expenseRepository.getExpenses(userId),
                goalRepository.getGoals(userId),
                _selectedCategoryFilter,
                _selectedAccountFilter
            ) { incomes, expenses, goals, categoryFilter, accountFilter ->
                val totalIncome = incomes.sumOf { it.realizedAmountLkr }
                val totalExpense = expenses.sumOf { it.amountLkr }
                val remainingBalance = totalIncome - totalExpense
                val totalFlow = (totalIncome + totalExpense).coerceAtLeast(1.0)
                val inflowFraction = (totalIncome / totalFlow).toFloat().coerceIn(0.1f, 0.9f)

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

                val filteredRecent = expenses.filter { expense ->
                    val matchesCategory = if (categoryFilter == "ALL") true else expense.category.equals(categoryFilter, ignoreCase = true)
                    val matchesAccount = if (accountFilter == "ALL") true else expense.paymentAccount.equals(accountFilter, ignoreCase = true)
                    matchesCategory && matchesAccount
                }

                val groupedRecent = filteredRecent.groupBy { DateUtils.formatWeeklyTimeTier(it.dateSpentMillis) }

                DashboardData(
                    totalIncomeLkr = totalIncome,
                    totalExpenseLkr = totalExpense,
                    remainingBalanceLkr = remainingBalance,
                    inflowFraction = inflowFraction,
                    committedExpenseLkr = committed,
                    discretionaryExpenseLkr = discretionary,
                    goals = goals,
                    macBookGoalProgress = primaryGoal,
                    recentTransactions = expenses,
                    filteredRecentTransactions = filteredRecent,
                    groupedRecentTransactions = groupedRecent,
                    selectedCategoryFilter = categoryFilter,
                    selectedAccountFilter = accountFilter,
                    shockInsightMessage = shockInsight
                )
            }.collect { data ->
                _uiState.value = UiState.Success(data)
            }
        }
    }

    fun logExpense(expense: ExpenseEntity) {
        val currentUid = expense.userId.ifBlank { authRepository.getCurrentUserId() }
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

    fun allocateWindfallToGoal(amountLkr: Double, goalId: String = "macbook_m4_goal") {
        val currentUid = authRepository.getCurrentUserId()
        viewModelScope.launch {
            goalRepository.allocateWindfallToGoal(currentUid, amountLkr, goalId)
        }
    }
}
