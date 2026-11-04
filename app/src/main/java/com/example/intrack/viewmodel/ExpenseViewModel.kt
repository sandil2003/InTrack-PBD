package com.example.intrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.intrack.data.model.ExpenseEntity
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
import com.example.intrack.ui.screens.expenses.CategorySpendingItem
import com.example.intrack.ui.screens.expenses.calculateCategoryBreakdown
import com.example.intrack.util.DateUtils
import com.example.intrack.util.KavinduScenarioSeeder
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ExpenseAnalyticsSummary(
    val selectedMonth: String = DateUtils.getRecentMonthsList(6).firstOrNull() ?: DateUtils.getCurrentMonthName(),
    val selectedTypeFilter: String = "ALL",
    val selectedCategoryFilter: String = "ALL",
    val selectedAccountFilter: String = "ALL",
    val monthExpenses: List<ExpenseEntity> = emptyList(),
    val outflowExpenses: List<ExpenseEntity> = emptyList(),
    val totalExpenseLkr: Double = 0.0,
    val committedLkr: Double = 0.0,
    val discretionaryLkr: Double = 0.0,
    val cardPrimaryTotalLkr: Double = 0.0,
    val secondaryAccountTotalLkr: Double = 0.0,
    val cashTotalLkr: Double = 0.0,
    val categoryBreakdownItems: List<CategorySpendingItem> = emptyList(),
    val filteredExpenses: List<ExpenseEntity> = emptyList(),
    val groupedExpenses: Map<String, List<ExpenseEntity>> = emptyMap()
)

@HiltViewModel
class ExpenseViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val incomeRepository: IncomeRepository,
    private val goalRepository: GoalRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    constructor(
        expenseRepository: ExpenseRepository = ExpenseRepositoryImpl(),
        subscriptionRepository: SubscriptionRepository = SubscriptionRepositoryImpl(),
        incomeRepository: IncomeRepository = IncomeRepositoryImpl(),
        goalRepository: GoalRepository = GoalRepositoryImpl()
    ) : this(expenseRepository, subscriptionRepository, incomeRepository, goalRepository, AuthRepositoryImpl())

    private val _expensesState = MutableStateFlow<UiState<List<ExpenseEntity>>>(UiState.Loading)
    val expensesState: StateFlow<UiState<List<ExpenseEntity>>> = _expensesState

    val recentMonths: List<String> = DateUtils.getRecentMonthsList(count = 6)

    private val _selectedMonth = MutableStateFlow(recentMonths.firstOrNull() ?: DateUtils.getCurrentMonthName())
    val selectedMonth: StateFlow<String> = _selectedMonth

    private val _selectedTypeFilter = MutableStateFlow("ALL")
    val selectedTypeFilter: StateFlow<String> = _selectedTypeFilter

    private val _selectedCategoryFilter = MutableStateFlow("ALL")
    val selectedCategoryFilter: StateFlow<String> = _selectedCategoryFilter

    private val _selectedAccountFilter = MutableStateFlow("ALL")
    val selectedAccountFilter: StateFlow<String> = _selectedAccountFilter

    private val _analyticsSummary = MutableStateFlow(ExpenseAnalyticsSummary())
    val analyticsSummary: StateFlow<ExpenseAnalyticsSummary> = _analyticsSummary

    init {
        loadExpenses()
    }

    fun setSelectedMonth(month: String) {
        _selectedMonth.value = month
        _selectedTypeFilter.value = "ALL"
        _selectedCategoryFilter.value = "ALL"
        _selectedAccountFilter.value = "ALL"
        recomputeAnalytics()
    }

    fun setAccountFilter(account: String) {
        _selectedAccountFilter.value = account
        recomputeAnalytics()
    }

    fun setTypeAndCategoryFilter(typeFilter: String, categoryFilter: String) {
        _selectedTypeFilter.value = typeFilter
        _selectedCategoryFilter.value = categoryFilter
        recomputeAnalytics()
    }

    fun toggleCategoryFilter(categoryKey: String) {
        _selectedCategoryFilter.value =
            if (_selectedCategoryFilter.value.equals(categoryKey, ignoreCase = true)) "ALL" else categoryKey
        recomputeAnalytics()
    }

    private fun recomputeAnalytics() {
        val allExpenses = (_expensesState.value as? UiState.Success<List<ExpenseEntity>>)?.data ?: emptyList()
        val month = _selectedMonth.value
        val typeFilter = _selectedTypeFilter.value
        val categoryFilter = _selectedCategoryFilter.value
        val accountFilter = _selectedAccountFilter.value

        val monthExpenses = allExpenses.filter {
            DateUtils.matchesMonth(it.dateSpentMillis, month)
        }
        val outflowExpenses = monthExpenses.filter { !it.isInternalTransfer() }
        val totalExpense = outflowExpenses.sumOf { it.amountLkr }
        val committed = outflowExpenses.filter { it.isCommittedExpense() }.sumOf { it.amountLkr }
        val discretionary = outflowExpenses.filter { it.isDiscretionaryExpense() }.sumOf { it.amountLkr }

        val cardPrimaryTotal = monthExpenses.filter { it.paymentAccount.equals("CARD_PRIMARY", ignoreCase = true) }.sumOf { it.amountLkr }
        val secondaryAccountTotal = monthExpenses.filter { it.paymentAccount.equals("ACCOUNT_SECONDARY", ignoreCase = true) }.sumOf { it.amountLkr }
        val cashTotal = monthExpenses.filter { it.paymentAccount.equals("CASH", ignoreCase = true) }.sumOf { it.amountLkr }

        val categoryBreakdownItems = calculateCategoryBreakdown(monthExpenses)

        val filteredExpenses = monthExpenses.filter { expense ->
            val matchesType = when (typeFilter) {
                "COMMITTED" -> expense.isCommittedExpense()
                "DISCRETIONARY" -> expense.isDiscretionaryExpense()
                "TRANSFER" -> expense.isInternalTransfer()
                else -> true
            }
            val matchesCategory = when (categoryFilter) {
                "ALL" -> true
                "ATM_WITHDRAWAL" -> expense.isInternalTransfer() || expense.category.equals("ATM_WITHDRAWAL", ignoreCase = true)
                else -> expense.category.equals(categoryFilter, ignoreCase = true)
            }
            val matchesAccount = when (accountFilter) {
                "ALL" -> true
                else -> expense.paymentAccount.equals(accountFilter, ignoreCase = true)
            }
            matchesType && matchesCategory && matchesAccount
        }.sortedByDescending { it.dateSpentMillis }

        val groupedExpenses = filteredExpenses.groupBy {
            DateUtils.formatRelativeDateGroup(it.dateSpentMillis, month)
        }

        _analyticsSummary.value = ExpenseAnalyticsSummary(
            selectedMonth = month,
            selectedTypeFilter = typeFilter,
            selectedCategoryFilter = categoryFilter,
            selectedAccountFilter = accountFilter,
            monthExpenses = monthExpenses,
            outflowExpenses = outflowExpenses,
            totalExpenseLkr = totalExpense,
            committedLkr = committed,
            discretionaryLkr = discretionary,
            cardPrimaryTotalLkr = cardPrimaryTotal,
            secondaryAccountTotalLkr = secondaryAccountTotal,
            cashTotalLkr = cashTotal,
            categoryBreakdownItems = categoryBreakdownItems,
            filteredExpenses = filteredExpenses,
            groupedExpenses = groupedExpenses
        )
    }

    fun loadExpenses(userId: String = authRepository.getCurrentUserId()) {
        viewModelScope.launch {
            _expensesState.value = UiState.Loading
            expenseRepository.getExpenses(userId).collect { list ->
                if (list.isEmpty()) {
                    _expensesState.value = UiState.Empty
                    if (userId.isNotBlank()) {
                        viewModelScope.launch {
                            KavinduScenarioSeeder.seedCompleteScenario(
                                userId = userId,
                                incomeRepository = incomeRepository,
                                expenseRepository = expenseRepository,
                                goalRepository = goalRepository,
                                subscriptionRepository = subscriptionRepository
                            )
                        }
                    }
                } else {
                    _expensesState.value = UiState.Success(list)
                }
                recomputeAnalytics()
            }
        }
    }

    fun addExpense(expense: ExpenseEntity) {
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

    fun updateExpense(expense: ExpenseEntity) {
        val currentUid = expense.userId.ifBlank { authRepository.getCurrentUserId() }
        val prepared = expense.copy(userId = currentUid)
        viewModelScope.launch {
            val currentState = _expensesState.value
            if (currentState is UiState.Success) {
                val updatedList = currentState.data.map { if (it.id == prepared.id) prepared else it }
                _expensesState.value = UiState.Success(updatedList)
                recomputeAnalytics()
            }
            expenseRepository.updateExpense(prepared)
        }
    }

    fun deleteExpense(expenseId: String) {
        deleteExpense(authRepository.getCurrentUserId(), expenseId)
    }

    fun deleteExpense(userId: String, expenseId: String) {
        val currentUid = userId.ifBlank { authRepository.getCurrentUserId() }
        viewModelScope.launch {
            val currentState = _expensesState.value
            if (currentState is UiState.Success) {
                val updatedList = currentState.data.filter { it.id != expenseId }
                if (updatedList.isEmpty()) {
                    _expensesState.value = UiState.Empty
                } else {
                    _expensesState.value = UiState.Success(updatedList)
                }
                recomputeAnalytics()
            }
            expenseRepository.deleteExpense(currentUid, expenseId)
        }
    }
}
