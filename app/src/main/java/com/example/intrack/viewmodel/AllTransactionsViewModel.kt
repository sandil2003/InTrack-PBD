package com.example.intrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.intrack.data.model.ExpenseEntity
import com.example.intrack.data.model.IncomeEntity
import com.example.intrack.data.repository.AuthRepository
import com.example.intrack.data.repository.AuthRepositoryImpl
import com.example.intrack.data.repository.ExpenseRepository
import com.example.intrack.data.repository.ExpenseRepositoryImpl
import com.example.intrack.data.repository.IncomeRepository
import com.example.intrack.data.repository.IncomeRepositoryImpl
import com.example.intrack.ui.common.UiState
import com.example.intrack.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class UnifiedTransaction(
    val id: String,
    val title: String,
    val amount: Double,
    val isIncome: Boolean,
    val timestampMillis: Long,
    val categoryOrChannel: String,
    val accountOrClient: String,
    val paymentAccount: String? = null
)

data class AllTransactionsData(
    val incomes: List<IncomeEntity> = emptyList(),
    val expenses: List<ExpenseEntity> = emptyList(),
    val selectedMonth: String = DateUtils.getRecentMonthsList(6).firstOrNull() ?: DateUtils.getCurrentMonthName(),
    val selectedTypeFilter: String = "ALL",
    val selectedAccountFilter: String = "ALL",
    val monthlyIncomes: List<IncomeEntity> = emptyList(),
    val monthlyExpenses: List<ExpenseEntity> = emptyList(),
    val monthInflowLkr: Double = 0.0,
    val monthOutflowLkr: Double = 0.0,
    val netFlowLkr: Double = 0.0,
    val inflowRatio: Float = 0.5f,
    val outflowRatio: Float = 0.5f,
    val allUnified: List<UnifiedTransaction> = emptyList(),
    val filteredTransactions: List<UnifiedTransaction> = emptyList()
)

@HiltViewModel
class AllTransactionsViewModel @Inject constructor(
    private val incomeRepository: IncomeRepository,
    private val expenseRepository: ExpenseRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    constructor(
        incomeRepository: IncomeRepository = IncomeRepositoryImpl(),
        expenseRepository: ExpenseRepository = ExpenseRepositoryImpl()
    ) : this(incomeRepository, expenseRepository, AuthRepositoryImpl())

    private val _uiState = MutableStateFlow<UiState<AllTransactionsData>>(UiState.Loading)
    val uiState: StateFlow<UiState<AllTransactionsData>> = _uiState

    val recentMonths: List<String> = DateUtils.getRecentMonthsList(count = 6)

    private val _selectedMonth = MutableStateFlow(recentMonths.firstOrNull() ?: DateUtils.getCurrentMonthName())
    val selectedMonth: StateFlow<String> = _selectedMonth

    private val _selectedTypeFilter = MutableStateFlow("ALL")
    val selectedTypeFilter: StateFlow<String> = _selectedTypeFilter

    private val _selectedAccountFilter = MutableStateFlow("ALL")
    val selectedAccountFilter: StateFlow<String> = _selectedAccountFilter

    init {
        loadData()
    }

    fun setSelectedMonth(month: String) {
        _selectedMonth.value = month
        _selectedAccountFilter.value = "ALL"
    }

    fun setTypeFilter(typeFilter: String) {
        _selectedTypeFilter.value = typeFilter
        if (typeFilter == "INCOME") {
            _selectedAccountFilter.value = "ALL"
        }
    }

    fun setAccountFilter(accountFilter: String) {
        _selectedAccountFilter.value = accountFilter
    }

    fun loadData(userId: String = authRepository.getCurrentUserId()) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            combine(
                incomeRepository.getIncomes(userId),
                expenseRepository.getExpenses(userId),
                _selectedMonth,
                _selectedTypeFilter,
                _selectedAccountFilter
            ) { incomes, expenses, month, typeFilter, accountFilter ->
                val monthlyIncomes = incomes.filter { DateUtils.matchesMonth(it.dateReceivedMillis, month) }
                val monthlyExpenses = expenses.filter { DateUtils.matchesMonth(it.dateSpentMillis, month) }

                val monthInflow = monthlyIncomes.sumOf { it.realizedAmountLkr }
                val monthOutflow = monthlyExpenses.sumOf { it.amountLkr }
                val netFlow = monthInflow - monthOutflow

                val totalFlow = (monthInflow + monthOutflow).coerceAtLeast(1.0)
                val inflowRatio = (monthInflow / totalFlow).toFloat().coerceIn(0.05f, 0.95f)
                val outflowRatio = (1f - inflowRatio).coerceIn(0.05f, 0.95f)

                val unifiedList = mutableListOf<UnifiedTransaction>()
                monthlyIncomes.forEach { inc ->
                    unifiedList.add(
                        UnifiedTransaction(
                            id = inc.id,
                            title = inc.title,
                            amount = inc.realizedAmountLkr,
                            isIncome = true,
                            timestampMillis = inc.dateReceivedMillis,
                            categoryOrChannel = inc.channel,
                            accountOrClient = inc.clientName ?: inc.channel.lowercase().replaceFirstChar { it.uppercase() }
                        )
                    )
                }
                monthlyExpenses.forEach { exp ->
                    unifiedList.add(
                        UnifiedTransaction(
                            id = exp.id,
                            title = exp.title,
                            amount = exp.amountLkr,
                            isIncome = false,
                            timestampMillis = exp.dateSpentMillis,
                            categoryOrChannel = exp.category,
                            accountOrClient = when (exp.paymentAccount.uppercase()) {
                                "CARD_PRIMARY" -> "Card Primary"
                                "ACCOUNT_SECONDARY" -> "Secondary Account"
                                "CASH" -> "Cash"
                                else -> exp.paymentAccount.replace("_", " ")
                            },
                            paymentAccount = exp.paymentAccount.uppercase()
                        )
                    )
                }
                val allUnified = unifiedList.sortedByDescending { it.timestampMillis }

                val filteredTransactions = allUnified.filter { tx ->
                    val matchesType = when (typeFilter) {
                        "EXPENSES" -> !tx.isIncome
                        "INCOME" -> tx.isIncome
                        else -> true
                    }
                    val matchesAccount = when (accountFilter) {
                        "ALL" -> true
                        "CASH" -> !tx.isIncome && tx.paymentAccount == "CASH"
                        "CARD_PRIMARY" -> !tx.isIncome && tx.paymentAccount == "CARD_PRIMARY"
                        "ACCOUNT_SECONDARY" -> !tx.isIncome && tx.paymentAccount == "ACCOUNT_SECONDARY"
                        else -> true
                    }
                    matchesType && matchesAccount
                }

                AllTransactionsData(
                    incomes = incomes,
                    expenses = expenses,
                    selectedMonth = month,
                    selectedTypeFilter = typeFilter,
                    selectedAccountFilter = accountFilter,
                    monthlyIncomes = monthlyIncomes,
                    monthlyExpenses = monthlyExpenses,
                    monthInflowLkr = monthInflow,
                    monthOutflowLkr = monthOutflow,
                    netFlowLkr = netFlow,
                    inflowRatio = inflowRatio,
                    outflowRatio = outflowRatio,
                    allUnified = allUnified,
                    filteredTransactions = filteredTransactions
                )
            }.collect { data ->
                _uiState.value = UiState.Success(data)
            }
        }
    }
}
