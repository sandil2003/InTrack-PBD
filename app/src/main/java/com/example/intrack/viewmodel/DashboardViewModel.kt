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
import com.example.intrack.util.KavinduScenarioSeeder
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
                if (userId.isNotBlank() &&
                    incomes.none { DateUtils.isCurrentMonth(it.dateReceivedMillis) } &&
                    expenses.none { DateUtils.isCurrentMonth(it.dateSpentMillis) }
                ) {
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

                // 1. Current Month Inflow: strictly PAID income received in the current month (excludes unpaid/invoiced/overdue & past months)
                val currentMonthPaidIncomes = incomes.filter {
                    it.status.equals("PAID", ignoreCase = true) && DateUtils.isCurrentMonth(it.dateReceivedMillis)
                }
                val totalIncome = currentMonthPaidIncomes.sumOf { it.realizedAmountLkr }

                // 2. Current Month Outflow: strictly non-transfer expenses spent in the current month (excludes internal ATM withdrawals/transfers & past months)
                val currentMonthExpenses = expenses.filter {
                    !it.isInternalTransfer() && DateUtils.isCurrentMonth(it.dateSpentMillis)
                }
                val totalExpense = currentMonthExpenses.sumOf { it.amountLkr }
                val remainingBalance = totalIncome - totalExpense
                val totalFlow = (totalIncome + totalExpense).coerceAtLeast(1.0)
                val inflowFraction = if (totalFlow > 0.0) {
                    (totalIncome / totalFlow).toFloat().coerceIn(0.01f, 0.99f)
                } else {
                    0.5f
                }

                // 3. Current month committed vs discretionary breakdown
                val committed = currentMonthExpenses.filter { it.isCommittedExpense() }.sumOf { it.amountLkr }
                val discretionary = currentMonthExpenses.filter { it.isDiscretionaryExpense() }.sumOf { it.amountLkr }

                // 4. Dining out shock factor (current month)
                val diningOutExpense = currentMonthExpenses.filter { it.category.equals("DINING_OUT", ignoreCase = true) }.sumOf { it.amountLkr }
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

                // 5. Catch-Up mode detection: check elapsed days since last logged expense
                val latestExpenseMillis = expenses.maxOfOrNull { it.dateSpentMillis } ?: 0L
                val daysSinceLastExpense = if (latestExpenseMillis > 0L) {
                    ((System.currentTimeMillis() - latestExpenseMillis) / (24L * 3600L * 1000L)).coerceAtLeast(0L)
                } else {
                    0L
                }
                val isCatchUpModeNeeded = daysSinceLastExpense >= 5L

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
                    shockInsightMessage = shockInsight,
                    daysSinceLastExpense = daysSinceLastExpense,
                    isCatchUpModeNeeded = isCatchUpModeNeeded
                )
            }.collect { data ->
                _uiState.value = UiState.Success(data)
            }
        }
    }

    fun logBulkCatchUpExpense(amountLkr: Double, note: String = "Estimated catch-up for missed logging period") {
        val currentUid = authRepository.getCurrentUserId()
        val expense = ExpenseEntity(
            id = UUID.randomUUID().toString(),
            userId = currentUid,
            title = "Catch-Up Logging Estimate",
            amountLkr = amountLkr,
            category = "MISC",
            paymentAccount = "CARD_PRIMARY",
            commitmentType = "DISCRETIONARY",
            dateSpentMillis = System.currentTimeMillis(),
            notes = note
        )
        viewModelScope.launch {
            expenseRepository.addExpense(expense)
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
