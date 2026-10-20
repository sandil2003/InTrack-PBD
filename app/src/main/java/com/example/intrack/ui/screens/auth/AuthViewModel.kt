package com.example.intrack.ui.screens.auth

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.intrack.data.repository.AuthRepository
import com.example.intrack.data.repository.AuthRepositoryImpl
import com.example.intrack.ui.common.UiState
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

enum class PasswordStrengthLevel(val label: String, val activeSegments: Int, val color: Color) {
    EMPTY("", 0, Color(0xFFE5E0DB)),
    WEAK("Weak", 1, Color(0xFFEF4444)),
    FAIR("Fair", 2, Color(0xFFF59E0B)),
    GOOD("Good", 3, Color(0xFF10B981)),
    STRONG("Strong", 4, Color(0xFF10B981))
}

class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepositoryImpl()
) : ViewModel() {

    private val _authState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val authState: StateFlow<UiState<String>> = _authState

    private val _resetPasswordState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val resetPasswordState: StateFlow<UiState<String>> = _resetPasswordState

    init {
        checkUser()
    }

    private fun checkUser() {
        val currentUser = authRepository.getCurrentUser()
        if (currentUser != null) {
            _authState.value = UiState.Success(currentUser.uid)
        }
    }

    fun signInWithEmail(
        email: String,
        pass: String,
        onSuccess: () -> Unit = {}
    ) {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isEmpty() || pass.isEmpty()) {
            _authState.value = UiState.Error("Please enter your email and password.")
            return
        }
        if (!isValidEmail(trimmedEmail)) {
            _authState.value = UiState.Error("Please enter a valid email address.")
            return
        }

        viewModelScope.launch {
            _authState.value = UiState.Loading
            val result = authRepository.signInWithEmail(trimmedEmail, pass)
            result.fold(
                onSuccess = { user ->
                    _authState.value = UiState.Success(user.uid)
                    onSuccess()
                },
                onFailure = { throwable ->
                    val message = mapAuthException(throwable)
                    _authState.value = UiState.Error(message, throwable)
                }
            )
        }
    }

    fun signUpWithEmail(
        name: String,
        email: String,
        pass: String,
        isTermsAccepted: Boolean,
        onSuccess: () -> Unit = {}
    ) {
        val trimmedName = name.trim()
        val trimmedEmail = email.trim()

        if (trimmedName.isEmpty()) {
            _authState.value = UiState.Error("Please enter your full name.")
            return
        }
        if (trimmedEmail.isEmpty()) {
            _authState.value = UiState.Error("Please enter your email address.")
            return
        }
        if (!isValidEmail(trimmedEmail)) {
            _authState.value = UiState.Error("Please enter a valid email address.")
            return
        }
        if (pass.length < 6) {
            _authState.value = UiState.Error("Password must be at least 6 characters.")
            return
        }
        if (!isTermsAccepted) {
            _authState.value = UiState.Error("Please accept the Terms of Service & Privacy Policy.")
            return
        }

        viewModelScope.launch {
            _authState.value = UiState.Loading
            val result = authRepository.signUpWithEmail(trimmedName, trimmedEmail, pass)
            result.fold(
                onSuccess = { user ->
                    _authState.value = UiState.Success(user.uid)
                    onSuccess()
                },
                onFailure = { throwable ->
                    val message = mapAuthException(throwable)
                    _authState.value = UiState.Error(message, throwable)
                }
            )
        }
    }

    fun sendPasswordReset(email: String, onSent: () -> Unit = {}) {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isEmpty() || !isValidEmail(trimmedEmail)) {
            _resetPasswordState.value = UiState.Error("Please enter a valid email to receive reset instructions.")
            return
        }

        viewModelScope.launch {
            _resetPasswordState.value = UiState.Loading
            val result = authRepository.sendPasswordResetEmail(trimmedEmail)
            result.fold(
                onSuccess = {
                    _resetPasswordState.value = UiState.Success("Password reset instructions sent to $trimmedEmail")
                    onSent()
                },
                onFailure = { throwable ->
                    _resetPasswordState.value = UiState.Error(mapAuthException(throwable), throwable)
                }
            )
        }
    }

    fun signInGuest(onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _authState.value = UiState.Loading
            val result = authRepository.signInAnonymously()
            result.fold(
                onSuccess = { user ->
                    _authState.value = UiState.Success(user.uid)
                    onSuccess()
                },
                onFailure = { throwable ->
                    _authState.value = UiState.Error(
                        message = throwable.message ?: "Sign in failed",
                        throwable = throwable
                    )
                }
            )
        }
    }

    fun continueOffline(onSuccess: () -> Unit = {}) {
        _authState.value = UiState.Success("guest_user")
        onSuccess()
    }

    fun clearError() {
        if (_authState.value is UiState.Error) {
            _authState.value = UiState.Idle
        }
    }

    fun clearResetState() {
        _resetPasswordState.value = UiState.Idle
    }

    fun calculatePasswordStrength(password: String): PasswordStrengthLevel {
        if (password.isEmpty()) return PasswordStrengthLevel.EMPTY
        var score = 0
        if (password.length >= 6) score++
        if (password.length >= 8) score++
        if (password.any { it.isDigit() } || password.any { !it.isLetterOrDigit() }) score++
        if (password.any { it.isUpperCase() } && password.any { it.isLowerCase() } && password.length >= 10) score++

        return when (score) {
            1 -> PasswordStrengthLevel.WEAK
            2 -> PasswordStrengthLevel.FAIR
            3 -> PasswordStrengthLevel.GOOD
            4 -> PasswordStrengthLevel.STRONG
            else -> if (password.length < 6) PasswordStrengthLevel.WEAK else PasswordStrengthLevel.STRONG
        }
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        return emailRegex.matches(email)
    }

    private fun mapAuthException(throwable: Throwable): String {
        if (throwable is FirebaseAuthException) {
            when (throwable.errorCode) {
                "ERROR_OPERATION_NOT_ALLOWED", "OPERATION_NOT_ALLOWED" ->
                    return "Email/Password sign-in is disabled in your Firebase Console. Please enable Email/Password under Authentication > Sign-in method in Firebase Console."
                "ERROR_USER_DISABLED", "USER_DISABLED" ->
                    return "This account has been disabled. Please contact support."
                "ERROR_USER_NOT_FOUND", "USER_NOT_FOUND" ->
                    return "No account registered with this email."
                "ERROR_WRONG_PASSWORD", "WRONG_PASSWORD" ->
                    return "Incorrect password. Please try again."
                "ERROR_EMAIL_ALREADY_IN_USE", "EMAIL_ALREADY_IN_USE" ->
                    return "An account with this email already exists. Please sign in instead."
                "ERROR_WEAK_PASSWORD", "WEAK_PASSWORD" ->
                    return "Password is too weak. Please use at least 6 characters."
                "ERROR_INVALID_EMAIL", "INVALID_EMAIL" ->
                    return "The email address is badly formatted."
            }
        }

        return when (throwable) {
            is FirebaseAuthInvalidUserException -> "No account registered with this email."
            is FirebaseAuthInvalidCredentialsException -> "Invalid email or password. Please try again."
            is FirebaseAuthUserCollisionException -> "An account with this email already exists. Please sign in instead."
            is FirebaseAuthWeakPasswordException -> "Password is too weak. Please use at least 6 characters."
            else -> {
                val msg = throwable.localizedMessage ?: ""
                when {
                    msg.contains("The given sign-in provider is disabled", ignoreCase = true) ||
                    msg.contains("OPERATION_NOT_ALLOWED", ignoreCase = true) ||
                    msg.contains("provider is disabled", ignoreCase = true) ->
                        "Email/Password sign-in is disabled in your Firebase Console. Please enable Email/Password under Authentication > Sign-in method in Firebase Console."
                    msg.contains("network error", ignoreCase = true) ||
                    msg.contains("A network error", ignoreCase = true) ->
                        "Network error. Please check your internet connection."
                    msg.contains("already in use", ignoreCase = true) ->
                        "An account with this email already exists. Please sign in instead."
                    else -> throwable.localizedMessage ?: "Authentication failed. Please check your connection."
                }
            }
        }
    }
}
