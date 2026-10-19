package com.example.intrack.ui.screens.subscriptions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.intrack.data.model.SubscriptionEntity
import com.example.intrack.data.repository.SubscriptionRepository
import com.example.intrack.data.repository.SubscriptionRepositoryImpl
import com.example.intrack.ui.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SubscriptionViewModel(
    private val subscriptionRepository: SubscriptionRepository = SubscriptionRepositoryImpl()
) : ViewModel() {

    private val _subscriptionsState = MutableStateFlow<UiState<List<SubscriptionEntity>>>(UiState.Loading)
    val subscriptionsState: StateFlow<UiState<List<SubscriptionEntity>>> = _subscriptionsState

    init {
        loadSubscriptions("guest_user")
    }

    fun loadSubscriptions(userId: String) {
        viewModelScope.launch {
            _subscriptionsState.value = UiState.Loading
            subscriptionRepository.getSubscriptions(userId).collect { list ->
                if (list.isEmpty()) {
                    _subscriptionsState.value = UiState.Empty
                } else {
                    _subscriptionsState.value = UiState.Success(list)
                }
            }
        }
    }

    fun addSubscription(subscription: SubscriptionEntity) {
        viewModelScope.launch {
            subscriptionRepository.addSubscription(subscription.copy(userId = "guest_user"))
        }
    }

    fun updateStatus(subscriptionId: String, newStatus: String) {
        viewModelScope.launch {
            subscriptionRepository.updateSubscriptionStatus("guest_user", subscriptionId, newStatus)
        }
    }

    fun updateAutoPayment(subscriptionId: String, isAutoPayment: Boolean) {
        viewModelScope.launch {
            subscriptionRepository.updateSubscriptionAutoPayment("guest_user", subscriptionId, isAutoPayment)
        }
    }
}
