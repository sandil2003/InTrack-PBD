package com.example.intrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.intrack.data.model.IncomeEntity
import com.example.intrack.data.repository.AuthRepository
import com.example.intrack.data.repository.AuthRepositoryImpl
import com.example.intrack.data.repository.IncomeRepository
import com.example.intrack.data.repository.IncomeRepositoryImpl
import com.example.intrack.ui.common.UiState
import com.example.intrack.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class IncomeHubSummary(
    val selectedMonth: String = DateUtils.getRecentMonthsList(6).firstOrNull() ?: DateUtils.getCurrentMonthName(),
    val selectedChannelFilter: String = "ALL",
    val allPendingInvoices: List<IncomeEntity> = emptyList(),
    val totalPendingReceivablesLkr: Double = 0.0,
    val monthIncomes: List<IncomeEntity> = emptyList(),
    val salaryIncomes: List<IncomeEntity> = emptyList(),
    val paidFreelanceInMonth: List<IncomeEntity> = emptyList(),
    val adsenseIncomes: List<IncomeEntity> = emptyList(),
    val cryptoIncomes: List<IncomeEntity> = emptyList(),
    val totalSalaryLkr: Double = 0.0,
    val totalFreelancePaidThisMonthLkr: Double = 0.0,
    val totalAdsenseLkr: Double = 0.0,
    val totalCryptoLkr: Double = 0.0,
    val totalMonthRealizedLkr: Double = 0.0,
    val rollingAverageLkr: Double = 120000.0,
    val monthPaidIncomes: List<IncomeEntity> = emptyList(),
    val groupedMonthIncomes: Map<String, List<IncomeEntity>> = emptyMap()
)

@HiltViewModel
class IncomeViewModel @Inject constructor(
    private val incomeRepository: IncomeRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    constructor(
        incomeRepository: IncomeRepository = IncomeRepositoryImpl()
    ) : this(incomeRepository, AuthRepositoryImpl())

    private val _incomesState = MutableStateFlow<UiState<List<IncomeEntity>>>(UiState.Loading)
    val incomesState: StateFlow<UiState<List<IncomeEntity>>> = _incomesState

    val recentMonths: List<String> = DateUtils.getRecentMonthsList(count = 6)

    private val _selectedMonth = MutableStateFlow(recentMonths.firstOrNull() ?: DateUtils.getCurrentMonthName())
    val selectedMonth: StateFlow<String> = _selectedMonth

    private val _selectedChannelFilter = MutableStateFlow("ALL")
    val selectedChannelFilter: StateFlow<String> = _selectedChannelFilter

    private val _hubSummary = MutableStateFlow(IncomeHubSummary())
    val hubSummary: StateFlow<IncomeHubSummary> = _hubSummary

    init {
        loadIncomes()
    }

    fun setSelectedMonth(month: String) {
        _selectedMonth.value = month
        _selectedChannelFilter.value = "ALL"
        recomputeHubSummary()
    }

    fun setChannelFilter(channel: String) {
        _selectedChannelFilter.value = channel
        recomputeHubSummary()
    }

    private fun recomputeHubSummary() {
        val allIncomes = (_incomesState.value as? UiState.Success<List<IncomeEntity>>)?.data ?: emptyList()
        val month = _selectedMonth.value
        val channelFilter = _selectedChannelFilter.value

        val allPendingInvoices = allIncomes.filter {
            it.channel == "FREELANCE" && (it.status == "INVOICED" || it.status == "OVERDUE")
        }.sortedByDescending { it.dateReceivedMillis }

        val totalPendingReceivablesLkr = allPendingInvoices.sumOf { it.realizedAmountLkr }

        val monthIncomes = allIncomes.filter {
            DateUtils.matchesMonth(it.dateReceivedMillis, month)
        }

        val salaryIncomes = monthIncomes.filter { it.channel == "SALARY" && it.status == "PAID" }
        val paidFreelanceInMonth = monthIncomes.filter { it.channel == "FREELANCE" && it.status == "PAID" }
        val adsenseIncomes = monthIncomes.filter { it.channel == "ADSENSE" && it.status == "PAID" }
        val cryptoIncomes = monthIncomes.filter { it.channel == "CRYPTO" && it.status == "PAID" }

        val totalSalary = salaryIncomes.sumOf { it.realizedAmountLkr }
        val totalFreelancePaidThisMonth = paidFreelanceInMonth.sumOf { it.realizedAmountLkr }
        val totalAdsense = adsenseIncomes.sumOf { it.realizedAmountLkr }
        val totalCrypto = cryptoIncomes.sumOf { it.realizedAmountLkr }

        val totalMonthRealized = totalSalary + totalFreelancePaidThisMonth + totalAdsense + totalCrypto

        val rollingAverage = if (allIncomes.isNotEmpty()) {
            val allPaid = allIncomes.filter { it.status == "PAID" }.sumOf { it.realizedAmountLkr }
            (allPaid / 3.0).coerceAtLeast(120000.0)
        } else {
            120000.0
        }

        val monthPaidIncomes = monthIncomes.filter {
            it.status == "PAID" && (if (channelFilter == "ALL") true else it.channel.equals(channelFilter, ignoreCase = true))
        }.sortedByDescending { it.dateReceivedMillis }

        val groupedMonthIncomes = monthPaidIncomes.groupBy {
            DateUtils.formatRelativeDateGroup(it.dateReceivedMillis, month)
        }

        _hubSummary.value = IncomeHubSummary(
            selectedMonth = month,
            selectedChannelFilter = channelFilter,
            allPendingInvoices = allPendingInvoices,
            totalPendingReceivablesLkr = totalPendingReceivablesLkr,
            monthIncomes = monthIncomes,
            salaryIncomes = salaryIncomes,
            paidFreelanceInMonth = paidFreelanceInMonth,
            adsenseIncomes = adsenseIncomes,
            cryptoIncomes = cryptoIncomes,
            totalSalaryLkr = totalSalary,
            totalFreelancePaidThisMonthLkr = totalFreelancePaidThisMonth,
            totalAdsenseLkr = totalAdsense,
            totalCryptoLkr = totalCrypto,
            totalMonthRealizedLkr = totalMonthRealized,
            rollingAverageLkr = rollingAverage,
            monthPaidIncomes = monthPaidIncomes,
            groupedMonthIncomes = groupedMonthIncomes
        )
    }

    fun loadIncomes(userId: String = authRepository.getCurrentUserId()) {
        viewModelScope.launch {
            _incomesState.value = UiState.Loading
            incomeRepository.getIncomes(userId).collect { list ->
                if (list.isEmpty()) {
                    _incomesState.value = UiState.Empty
                } else {
                    _incomesState.value = UiState.Success(list)
                }
                recomputeHubSummary()
            }
        }
    }

    fun addIncome(income: IncomeEntity) {
        val currentUid = income.userId.ifBlank { authRepository.getCurrentUserId() }
        val prepared = income.copy(userId = currentUid)
        viewModelScope.launch {
            incomeRepository.addIncome(prepared)
        }
    }

    fun updateIncome(income: IncomeEntity) {
        val currentUid = income.userId.ifBlank { authRepository.getCurrentUserId() }
        val prepared = income.copy(userId = currentUid)
        viewModelScope.launch {
            incomeRepository.updateIncome(prepared)
        }
    }

    fun deleteIncome(incomeId: String, userId: String? = null) {
        val currentUid = userId?.takeIf { it.isNotBlank() } ?: authRepository.getCurrentUserId()
        viewModelScope.launch {
            incomeRepository.deleteIncome(currentUid, incomeId)
        }
    }
}
