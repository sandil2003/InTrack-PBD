package com.example.intrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.intrack.data.model.CryptoHoldingEntity
import com.example.intrack.data.model.IncomeEntity
import com.example.intrack.data.repository.AuthRepository
import com.example.intrack.data.repository.AuthRepositoryImpl
import com.example.intrack.data.repository.CryptoRateRepository
import com.example.intrack.data.repository.CryptoRateRepositoryImpl
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
import java.util.Calendar
import javax.inject.Inject
import kotlin.math.abs
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
    val groupedMonthIncomes: Map<String, List<IncomeEntity>> = emptyMap(),
    // Crypto YTD Net Position (Q1 gain vs Q3 loss)
    val cryptoYtdNetPositionLkr: Double = 0.0,
    val cryptoQ1PnlLkr: Double = 0.0,
    val cryptoQ2PnlLkr: Double = 0.0,
    val cryptoQ3PnlLkr: Double = 0.0,
    val cryptoQ4PnlLkr: Double = 0.0,
    val cryptoYtdTotalGainsLkr: Double = 0.0,
    val cryptoYtdTotalLossesLkr: Double = 0.0
)

@HiltViewModel
class IncomeViewModel @Inject constructor(
    private val incomeRepository: IncomeRepository,
    private val goalRepository: GoalRepository,
    private val expenseRepository: ExpenseRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val authRepository: AuthRepository,
    private val cryptoRateRepository: CryptoRateRepository = CryptoRateRepositoryImpl()
    @param:dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context? = null
) : ViewModel() {

    constructor(
        incomeRepository: IncomeRepository = IncomeRepositoryImpl(),
        goalRepository: GoalRepository = GoalRepositoryImpl(),
        expenseRepository: ExpenseRepository = ExpenseRepositoryImpl(),
        subscriptionRepository: SubscriptionRepository = SubscriptionRepositoryImpl(),
        cryptoRateRepository: CryptoRateRepository = CryptoRateRepositoryImpl()
    ) : this(incomeRepository, goalRepository, expenseRepository, subscriptionRepository, AuthRepositoryImpl(), cryptoRateRepository)


    private fun triggerWidgetRefresh() {
        context?.let { ctx ->
            viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    com.example.intrack.widget.InTrackGlanceWidget.updateWidget(ctx)
                } catch (_: Exception) {}
            }
        }
    }

    private val _incomesState = MutableStateFlow<UiState<List<IncomeEntity>>>(UiState.Loading)
    val incomesState: StateFlow<UiState<List<IncomeEntity>>> = _incomesState

    private val _cryptoRatesState = MutableStateFlow<Map<String, Double>>(cryptoRateRepository.getFallbackRates())
    val cryptoRatesState: StateFlow<Map<String, Double>> = _cryptoRatesState

    private val _cryptoHoldingsState = MutableStateFlow<List<CryptoHoldingEntity>>(
        listOf(
            CryptoHoldingEntity(id = "h_eth", coinSymbol = "ETH", coinName = "Ethereum", coinQuantity = 0.25, investedLkr = 180000.0, avgBuyPriceLkr = 720000.0),
            CryptoHoldingEntity(id = "h_usdt", coinSymbol = "USDT", coinName = "Tether", coinQuantity = 450.0, investedLkr = 148500.0, avgBuyPriceLkr = 330.0),
            CryptoHoldingEntity(id = "h_sol", coinSymbol = "SOL", coinName = "Solana", coinQuantity = 3.5, investedLkr = 168000.0, avgBuyPriceLkr = 48000.0)
        )
    )
    val cryptoHoldingsState: StateFlow<List<CryptoHoldingEntity>> = _cryptoHoldingsState

    val recentMonths: List<String> = DateUtils.getRecentMonthsList(count = 6)

    private val _selectedMonth = MutableStateFlow(recentMonths.firstOrNull() ?: DateUtils.getCurrentMonthName())
    val selectedMonth: StateFlow<String> = _selectedMonth

    private val _selectedChannelFilter = MutableStateFlow("ALL")
    val selectedChannelFilter: StateFlow<String> = _selectedChannelFilter

    private val _hubSummary = MutableStateFlow(IncomeHubSummary())
    val hubSummary: StateFlow<IncomeHubSummary> = _hubSummary

    init {
        loadIncomes()
        refreshCryptoRates()
    }

    fun refreshCryptoRates() {
        viewModelScope.launch {
            val live = cryptoRateRepository.getLiveRatesToLkr()
            _cryptoRatesState.value = live
        }
    }

    fun investInCoin(coinSymbol: String, investedLkr: Double) {
        if (investedLkr <= 0.0) return
        val rates = _cryptoRatesState.value
        val rateToLkr = rates[coinSymbol.uppercase()] ?: cryptoRateRepository.getFallbackRates()[coinSymbol.uppercase()] ?: 332.40
        val addedQuantity = investedLkr / rateToLkr

        val currentList = _cryptoHoldingsState.value.toMutableList()
        val index = currentList.indexOfFirst { it.coinSymbol.equals(coinSymbol, ignoreCase = true) }

        if (index >= 0) {
            val existing = currentList[index]
            val newQty = existing.coinQuantity + addedQuantity
            val newInvested = existing.investedLkr + investedLkr
            val newAvg = newInvested / newQty
            currentList[index] = existing.copy(
                coinQuantity = newQty,
                investedLkr = newInvested,
                avgBuyPriceLkr = newAvg,
                lastUpdatedMillis = System.currentTimeMillis()
            )
        } else {
            val coinName = when (coinSymbol.uppercase()) {
                "USDT" -> "Tether"
                "ETH" -> "Ethereum"
                "BTC" -> "Bitcoin"
                "SOL" -> "Solana"
                "BNB" -> "Binance Coin"
                "ADA" -> "Cardano"
                "XRP" -> "Ripple"
                "DOGE" -> "Dogecoin"
                else -> coinSymbol.uppercase()
            }
            currentList.add(
                CryptoHoldingEntity(
                    id = "h_${coinSymbol.lowercase()}",
                    coinSymbol = coinSymbol.uppercase(),
                    coinName = coinName,
                    coinQuantity = addedQuantity,
                    investedLkr = investedLkr,
                    avgBuyPriceLkr = rateToLkr,
                    lastUpdatedMillis = System.currentTimeMillis()
                )
            )
        }
        _cryptoHoldingsState.value = currentList
    }

    fun payoutCoinGains(coinSymbol: String, payoutLkr: Double) {
        if (payoutLkr <= 0.0) return
        val rates = _cryptoRatesState.value
        val currentRate = rates[coinSymbol.uppercase()] ?: cryptoRateRepository.getFallbackRates()[coinSymbol.uppercase()] ?: 332.40
        val payoutQuantity = payoutLkr / currentRate

        val currentList = _cryptoHoldingsState.value.toMutableList()
        val index = currentList.indexOfFirst { it.coinSymbol.equals(coinSymbol, ignoreCase = true) }

        if (index >= 0) {
            val existing = currentList[index]
            val actualPayoutQty = payoutQuantity.coerceAtMost(existing.coinQuantity)
            val costBasisLkr = actualPayoutQty * existing.avgBuyPriceLkr
            val netRealizedGainLkr = payoutLkr - costBasisLkr

            val remainingQty = (existing.coinQuantity - actualPayoutQty).coerceAtLeast(0.0)
            val remainingInvested = (existing.investedLkr - costBasisLkr).coerceAtLeast(0.0)

            if (remainingQty <= 0.0001) {
                currentList.removeAt(index)
            } else {
                currentList[index] = existing.copy(
                    coinQuantity = remainingQty,
                    investedLkr = remainingInvested,
                    lastUpdatedMillis = System.currentTimeMillis()
                )
            }
            _cryptoHoldingsState.value = currentList

            // Log realized P&L as a CRYPTO channel income stream entry
            addIncome(
                IncomeEntity(
                    title = "Crypto Payout: ${coinSymbol.uppercase()} Realized P&L",
                    channel = "CRYPTO",
                    originalAmount = netRealizedGainLkr,
                    originalCurrency = "LKR",
                    realizedAmountLkr = netRealizedGainLkr,
                    status = "PAID",
                    dateReceivedMillis = System.currentTimeMillis()
                )
            )
        }
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

        val monthIncomes = allIncomes.filter { DateUtils.matchesMonth(it.dateReceivedMillis, month) }
        val monthPaidIncomes = monthIncomes.filter { it.status == "PAID" }

        val salaryIncomes = monthPaidIncomes.filter { it.channel.equals("SALARY", ignoreCase = true) }
        val paidFreelanceInMonth = monthPaidIncomes.filter { it.channel.equals("FREELANCE", ignoreCase = true) }
        val adsenseIncomes = monthPaidIncomes.filter { it.channel.equals("ADSENSE", ignoreCase = true) }
        val cryptoIncomes = monthPaidIncomes.filter { it.channel.equals("CRYPTO", ignoreCase = true) }

        val totalSalary = salaryIncomes.sumOf { it.realizedAmountLkr }
        val totalFreelancePaidThisMonth = paidFreelanceInMonth.sumOf { it.realizedAmountLkr }
        val totalAdsense = adsenseIncomes.sumOf { it.realizedAmountLkr }
        val totalCrypto = cryptoIncomes.sumOf { it.realizedAmountLkr }
        val totalMonthRealized = monthPaidIncomes.sumOf { it.realizedAmountLkr }

        val rollingAverage = 140000.0 + 45000.0 + 64050.0 + 28500.0

        val filteredByChannel = when (channelFilter) {
            "ALL" -> monthIncomes
            "INVOICES_PENDING" -> allPendingInvoices
            else -> monthIncomes.filter { it.channel.equals(channelFilter, ignoreCase = true) }
        }.sortedByDescending { it.dateReceivedMillis }

        val groupedMonthIncomes = filteredByChannel.groupBy {
            DateUtils.formatRelativeDateGroup(it.dateReceivedMillis, month)
        }

        // Crypto YTD Calculations
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val cal = Calendar.getInstance()
        val allPaidCrypto = allIncomes.filter { it.channel.equals("CRYPTO", ignoreCase = true) && it.status.equals("PAID", ignoreCase = true) }

        var q1Pnl = 0.0
        var q2Pnl = 0.0
        var q3Pnl = 0.0
        var q4Pnl = 0.0
        var ytdGains = 0.0
        var ytdLosses = 0.0

        allPaidCrypto.forEach { trade ->
            cal.timeInMillis = trade.dateReceivedMillis
            if (cal.get(Calendar.YEAR) == currentYear) {
                val amount = trade.realizedAmountLkr
                when (cal.get(Calendar.MONTH)) {
                    in 0..2 -> q1Pnl += amount
                    in 3..5 -> q2Pnl += amount
                    in 6..8 -> q3Pnl += amount
                    else -> q4Pnl += amount
                }
                if (amount >= 0) {
                    ytdGains += amount
                } else {
                    ytdLosses += abs(amount)
                }
            }
        }
        val cryptoYtdNetPosition = q1Pnl + q2Pnl + q3Pnl + q4Pnl

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
            groupedMonthIncomes = groupedMonthIncomes,
            cryptoYtdNetPositionLkr = cryptoYtdNetPosition,
            cryptoQ1PnlLkr = q1Pnl,
            cryptoQ2PnlLkr = q2Pnl,
            cryptoQ3PnlLkr = q3Pnl,
            cryptoQ4PnlLkr = q4Pnl,
            cryptoYtdTotalGainsLkr = ytdGains,
            cryptoYtdTotalLossesLkr = ytdLosses
        )
    }

    fun allocateWindfallToGoal(
        income: IncomeEntity,
        allocatedAmountLkr: Double,
        goalId: String = "macbook_m4_goal"
    ) {
        val currentUid = authRepository.getCurrentUserId()
        if (currentUid.isBlank() || allocatedAmountLkr <= 0.0) return

        viewModelScope.launch {
            // 1. Transactionally credit Goal in Firestore & local Room
            goalRepository.allocateWindfallToGoal(
                userId = currentUid,
                amountLkr = allocatedAmountLkr,
                goalId = goalId,
                sourceTitle = "Windfall from ${income.title.ifBlank { income.channel }}",
                sourceId = income.id.ifBlank { "manual" }
            )
            // 2. Update income entity with the allocated amount
            val updated = income.copy(
                allocatedToGoalLkr = (income.allocatedToGoalLkr + allocatedAmountLkr)
            )
            incomeRepository.updateIncome(updated)
        }
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
                triggerWidgetRefresh()
            }
        }
    }

    fun addIncome(income: IncomeEntity) {
        val currentUid = income.userId.ifBlank { authRepository.getCurrentUserId() }
        val prepared = income.copy(userId = currentUid)
        viewModelScope.launch {
            incomeRepository.addIncome(prepared)
            triggerWidgetRefresh()
        }
    }

    fun updateIncome(income: IncomeEntity) {
        val currentUid = income.userId.ifBlank { authRepository.getCurrentUserId() }
        val prepared = income.copy(userId = currentUid)
        viewModelScope.launch {
            incomeRepository.updateIncome(prepared)
            triggerWidgetRefresh()
        }
    }

    fun deleteIncome(incomeId: String, userId: String? = null) {
        val currentUid = userId?.takeIf { it.isNotBlank() } ?: authRepository.getCurrentUserId()
        viewModelScope.launch {
            incomeRepository.deleteIncome(currentUid, incomeId)
            triggerWidgetRefresh()
        }
    }
}
