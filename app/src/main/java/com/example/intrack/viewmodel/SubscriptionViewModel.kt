package com.example.intrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.intrack.data.model.SubscriptionEntity
import com.example.intrack.data.repository.AuthRepository
import com.example.intrack.data.repository.AuthRepositoryImpl
import com.example.intrack.data.repository.SubscriptionRepository
import com.example.intrack.data.repository.SubscriptionRepositoryImpl
import com.example.intrack.ui.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class SubscriptionAuditSummary(
    val subscriptionFilter: String = "ALL",
    val activeSubs: List<SubscriptionEntity> = emptyList(),
    val totalActiveLkr: Double = 0.0,
    val secondaryTotalLkr: Double = 0.0,
    val primaryCardTotalLkr: Double = 0.0,
    val cashTotalLkr: Double = 0.0,
    val allCount: Int = 0,
    val dueWeekCount: Int = 0,
    val autoDebitCount: Int = 0,
    val filteredSubs: List<SubscriptionEntity> = emptyList(),
    val nextSub: SubscriptionEntity? = null,
    val nextInDays: Long = 0L
)

@HiltViewModel
class SubscriptionViewModel @Inject constructor(
    private val subscriptionRepository: SubscriptionRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    constructor(
        subscriptionRepository: SubscriptionRepository = SubscriptionRepositoryImpl()
    ) : this(subscriptionRepository, AuthRepositoryImpl())

    private val _subscriptionsState = MutableStateFlow<UiState<List<SubscriptionEntity>>>(UiState.Loading)
    val subscriptionsState: StateFlow<UiState<List<SubscriptionEntity>>> = _subscriptionsState

    private val _subscriptionFilter = MutableStateFlow("ALL")
    val subscriptionFilter: StateFlow<String> = _subscriptionFilter

    private val _auditSummary = MutableStateFlow(SubscriptionAuditSummary())
    val auditSummary: StateFlow<SubscriptionAuditSummary> = _auditSummary

    init {
        loadSubscriptions()
    }

    fun setSubscriptionFilter(filter: String) {
        _subscriptionFilter.value = filter
        recomputeAuditSummary()
    }

    private fun recomputeAuditSummary() {
        val subscriptions = (_subscriptionsState.value as? UiState.Success<List<SubscriptionEntity>>)?.data ?: emptyList()
        val filter = _subscriptionFilter.value
        val now = System.currentTimeMillis()

        val activeSubs = subscriptions.filter { it.status != "CANCELLED" }
        val totalActive = activeSubs.sumOf { it.amountLkr }
        val secondaryTotal = activeSubs.filter { it.paymentAccount.equals("ACCOUNT_SECONDARY", ignoreCase = true) }.sumOf { it.amountLkr }
        val primaryCardTotal = activeSubs.filter { it.paymentAccount.equals("CARD_PRIMARY", ignoreCase = true) }.sumOf { it.amountLkr }
        val cashTotal = activeSubs.filter { it.paymentAccount.equals("CASH", ignoreCase = true) }.sumOf { it.amountLkr }

        val allCount = subscriptions.size
        val dueWeekCount = subscriptions.count {
            val diff = it.nextBillingMillis - now
            diff in 0..(7L * 24 * 3600 * 1000)
        }
        val autoDebitCount = subscriptions.count {
            it.paymentAccount.equals("CARD_PRIMARY", ignoreCase = true) ||
                it.paymentAccount.equals("ACCOUNT_SECONDARY", ignoreCase = true)
        }

        val filteredSubs = when (filter) {
            "DUE_WEEK" -> subscriptions.filter {
                val diff = it.nextBillingMillis - now
                diff in 0..(7L * 24 * 3600 * 1000)
            }
            "AUTO_DEBIT" -> subscriptions.filter {
                it.paymentAccount.equals("CARD_PRIMARY", ignoreCase = true) ||
                    it.paymentAccount.equals("ACCOUNT_SECONDARY", ignoreCase = true)
            }
            else -> subscriptions
        }

        val nextSub = activeSubs.minByOrNull { it.nextBillingMillis }
        val nextInDays = if (nextSub != null) {
            val diff = nextSub.nextBillingMillis - now
            (diff / (24L * 3600L * 1000L)).coerceAtLeast(1L)
        } else {
            0L
        }

        _auditSummary.value = SubscriptionAuditSummary(
            subscriptionFilter = filter,
            activeSubs = activeSubs,
            totalActiveLkr = totalActive,
            secondaryTotalLkr = secondaryTotal,
            primaryCardTotalLkr = primaryCardTotal,
            cashTotalLkr = cashTotal,
            allCount = allCount,
            dueWeekCount = dueWeekCount,
            autoDebitCount = autoDebitCount,
            filteredSubs = filteredSubs,
            nextSub = nextSub,
            nextInDays = nextInDays
        )
    }

    fun loadSubscriptions(userId: String = authRepository.getCurrentUserId()) {
        viewModelScope.launch {
            _subscriptionsState.value = UiState.Loading
            subscriptionRepository.getSubscriptions(userId).collect { list ->
                if (list.isEmpty()) {
                    _subscriptionsState.value = UiState.Empty
                } else {
                    _subscriptionsState.value = UiState.Success(list)
                }
                recomputeAuditSummary()
            }
        }
    }

    fun addSubscription(subscription: SubscriptionEntity) {
        val currentUid = subscription.userId.ifBlank { authRepository.getCurrentUserId() }
        val prepared = subscription.copy(userId = currentUid)
        viewModelScope.launch {
            val currentState = _subscriptionsState.value
            if (currentState is UiState.Success) {
                _subscriptionsState.value = UiState.Success(listOf(prepared) + currentState.data)
            } else if (currentState is UiState.Empty) {
                _subscriptionsState.value = UiState.Success(listOf(prepared))
            }
            recomputeAuditSummary()
            subscriptionRepository.addSubscription(prepared)
        }
    }

    fun updateSubscription(subscription: SubscriptionEntity) {
        val currentUid = subscription.userId.ifBlank { authRepository.getCurrentUserId() }
        val updated = subscription.copy(userId = currentUid)
        viewModelScope.launch {
            val currentState = _subscriptionsState.value
            if (currentState is UiState.Success) {
                val updatedList = currentState.data.map { if (it.id == updated.id) updated else it }
                _subscriptionsState.value = UiState.Success(updatedList)
                recomputeAuditSummary()
            }
            subscriptionRepository.updateSubscription(updated)
        }
    }

    fun deleteSubscription(subscriptionId: String) {
        val currentUid = authRepository.getCurrentUserId()
        deleteSubscription(currentUid, subscriptionId)
    }

    fun deleteSubscription(userId: String, subscriptionId: String) {
        val currentUid = userId.ifBlank { authRepository.getCurrentUserId() }
        viewModelScope.launch {
            val currentState = _subscriptionsState.value
            if (currentState is UiState.Success) {
                val updatedList = currentState.data.filter { it.id != subscriptionId }
                if (updatedList.isEmpty()) {
                    _subscriptionsState.value = UiState.Empty
                } else {
                    _subscriptionsState.value = UiState.Success(updatedList)
                }
                recomputeAuditSummary()
            }
            subscriptionRepository.deleteSubscription(currentUid, subscriptionId)
        }
    }

    fun updateStatus(subscriptionId: String, newStatus: String) {
        val currentUid = authRepository.getCurrentUserId()
        viewModelScope.launch {
            val currentState = _subscriptionsState.value
            if (currentState is UiState.Success) {
                val updatedList = currentState.data.map { if (it.id == subscriptionId) it.copy(status = newStatus) else it }
                _subscriptionsState.value = UiState.Success(updatedList)
                recomputeAuditSummary()
            }
            subscriptionRepository.updateSubscriptionStatus(currentUid, subscriptionId, newStatus)
        }
    }

    fun updateAutoPayment(subscriptionId: String, isAutoPayment: Boolean) {
        val currentUid = authRepository.getCurrentUserId()
        viewModelScope.launch {
            val currentState = _subscriptionsState.value
            if (currentState is UiState.Success) {
                val updatedList = currentState.data.map { if (it.id == subscriptionId) it.copy(isAutoPayment = isAutoPayment) else it }
                _subscriptionsState.value = UiState.Success(updatedList)
                recomputeAuditSummary()
            }
            subscriptionRepository.updateSubscriptionAutoPayment(currentUid, subscriptionId, isAutoPayment)
        }
    }
}
