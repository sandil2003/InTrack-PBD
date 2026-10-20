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
        val currentUid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: "guest_user"
        loadSubscriptions(currentUid)
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
        val currentUid = if (subscription.userId.isNotBlank()) subscription.userId else (com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: "guest_user")
        val prepared = subscription.copy(userId = currentUid)
        viewModelScope.launch {
            val currentState = _subscriptionsState.value
            if (currentState is UiState.Success) {
                _subscriptionsState.value = UiState.Success(listOf(prepared) + currentState.data)
            } else if (currentState is UiState.Empty) {
                _subscriptionsState.value = UiState.Success(listOf(prepared))
            }
            subscriptionRepository.addSubscription(prepared)
        }
    }

    fun updateSubscription(subscription: SubscriptionEntity) {
        val currentUid = if (subscription.userId.isNotBlank()) subscription.userId else (com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: "guest_user")
        val updated = subscription.copy(userId = currentUid)
        viewModelScope.launch {
            val currentState = _subscriptionsState.value
            if (currentState is UiState.Success) {
                val updatedList = currentState.data.map { if (it.id == updated.id) updated else it }
                _subscriptionsState.value = UiState.Success(updatedList)
            }
            subscriptionRepository.updateSubscription(updated)
        }
    }

    fun deleteSubscription(subscriptionId: String) {
        val currentUid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: "guest_user"
        deleteSubscription(currentUid, subscriptionId)
    }

    fun deleteSubscription(userId: String, subscriptionId: String) {
        val currentUid = if (userId.isNotBlank()) userId else (com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: "guest_user")
        viewModelScope.launch {
            val currentState = _subscriptionsState.value
            if (currentState is UiState.Success) {
                val updatedList = currentState.data.filter { it.id != subscriptionId }
                if (updatedList.isEmpty()) {
                    _subscriptionsState.value = UiState.Empty
                } else {
                    _subscriptionsState.value = UiState.Success(updatedList)
                }
            }
            subscriptionRepository.deleteSubscription(currentUid, subscriptionId)
        }
    }

    fun updateStatus(subscriptionId: String, newStatus: String) {
        val currentUid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: "guest_user"
        viewModelScope.launch {
            val currentState = _subscriptionsState.value
            if (currentState is UiState.Success) {
                val updatedList = currentState.data.map { if (it.id == subscriptionId) it.copy(status = newStatus) else it }
                _subscriptionsState.value = UiState.Success(updatedList)
            }
            subscriptionRepository.updateSubscriptionStatus(currentUid, subscriptionId, newStatus)
        }
    }

    fun updateAutoPayment(subscriptionId: String, isAutoPayment: Boolean) {
        val currentUid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: "guest_user"
        viewModelScope.launch {
            val currentState = _subscriptionsState.value
            if (currentState is UiState.Success) {
                val updatedList = currentState.data.map { if (it.id == subscriptionId) it.copy(isAutoPayment = isAutoPayment) else it }
                _subscriptionsState.value = UiState.Success(updatedList)
            }
            subscriptionRepository.updateSubscriptionAutoPayment(currentUid, subscriptionId, isAutoPayment)
        }
    }
}
