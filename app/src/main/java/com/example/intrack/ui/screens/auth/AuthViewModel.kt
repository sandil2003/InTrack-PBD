package com.example.intrack.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.intrack.data.repository.AuthRepository
import com.example.intrack.data.repository.AuthRepositoryImpl
import com.example.intrack.ui.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepositoryImpl()
) : ViewModel() {

    private val _authState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val authState: StateFlow<UiState<String>> = _authState

    init {
        checkUser()
    }

    private fun checkUser() {
        val currentUser = authRepository.getCurrentUser()
        if (currentUser != null) {
            _authState.value = UiState.Success(currentUser.uid)
        } else {
            signInGuest()
        }
    }

    fun signInGuest() {
        viewModelScope.launch {
            _authState.value = UiState.Loading
            val result = authRepository.signInAnonymously()
            result.fold(
                onSuccess = { user ->
                    _authState.value = UiState.Success(user.uid)
                },
                onFailure = { throwable ->
                    // Fall back to offline guest session if Firebase Anonymous Auth is not enabled in Console
                    _authState.value = UiState.Error(
                        message = throwable.message ?: "Sign in failed",
                        throwable = throwable
                    )
                }
            )
        }
    }

    fun continueOffline() {
        _authState.value = UiState.Success("guest_user")
    }
}
