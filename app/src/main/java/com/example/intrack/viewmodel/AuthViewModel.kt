package com.example.intrack.viewmodel

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.intrack.data.model.UserProfile
import com.example.intrack.data.repository.AuthRepository
import com.example.intrack.data.repository.AuthRepositoryImpl
import com.example.intrack.ui.common.UiState
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
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

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    constructor() : this(AuthRepositoryImpl())

    companion object {
        const val DEBUG_SHA1_FINGERPRINT = "41:22:92:CF:87:CF:84:BA:62:2B:2E:01:FF:06:F6:1D:A2:B9:B0:61"
        const val DEFAULT_WEB_CLIENT_ID = "527660795071-0i7jrn1s461bs3jdf3a8227aiblrmh37.apps.googleusercontent.com"
    }

    private val _authState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val authState: StateFlow<UiState<String>> = _authState

    private val _resetPasswordState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val resetPasswordState: StateFlow<UiState<String>> = _resetPasswordState

    private val _showGoogleSetupDialog = MutableStateFlow(false)
    val showGoogleSetupDialog: StateFlow<Boolean> = _showGoogleSetupDialog

    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile

    private val _userDisplayName = MutableStateFlow("")
    val userDisplayName: StateFlow<String> = _userDisplayName

    private val _userEmail = MutableStateFlow("")
    val userEmail: StateFlow<String> = _userEmail

    private val _userInitials = MutableStateFlow("U")
    val userInitials: StateFlow<String> = _userInitials

    private var profileListenerJob: Job? = null

    init {
        checkUser()
    }

    private fun applyProfileState(profile: UserProfile) {
        _userProfile.value = profile
        _userDisplayName.value = profile.displayName.ifBlank { profile.firstName }
        _userEmail.value = profile.email
        _userInitials.value = profile.initials
    }

    private fun refreshProfileInfo(explicitName: String? = null, explicitEmail: String? = null) {
        val currentUser = authRepository.getCurrentUser()
        if (currentUser == null) {
            profileListenerJob?.cancel()
            profileListenerJob = null
            _userProfile.value = UserProfile()
            _userDisplayName.value = ""
            _userEmail.value = ""
            _userInitials.value = "U"
            return
        }

        val name = explicitName?.takeIf { it.isNotBlank() }
            ?: authRepository.getCurrentUserDisplayName()
        val email = explicitEmail?.takeIf { it.isNotBlank() }
            ?: authRepository.getCurrentUserEmail()

        val immediateProfile = _userProfile.value.copy(
            uid = currentUser.uid,
            displayName = name,
            email = email
        )
        applyProfileState(immediateProfile)

        profileListenerJob?.cancel()
        profileListenerJob = viewModelScope.launch {
            authRepository.observeUserProfile(currentUser.uid).collect { firestoreProfile ->
                if (firestoreProfile != null) {
                    val mergedProfile = firestoreProfile.copy(
                        displayName = firestoreProfile.displayName.ifBlank { name },
                        email = firestoreProfile.email.ifBlank { email }
                    )
                    applyProfileState(mergedProfile)
                }
            }
        }
    }

    fun checkUser() {
        val currentUser = authRepository.getCurrentUser()
        refreshProfileInfo()
        if (currentUser != null && currentUser.uid.isNotBlank()) {
            _authState.value = UiState.Success(currentUser.uid)
        } else {
            _authState.value = UiState.Idle
        }
    }

    fun updatePreferences(
        baseCurrency: String = _userProfile.value.baseCurrency,
        billingCycle: String = _userProfile.value.billingCycle,
        weeklyStartDay: String = _userProfile.value.weeklyStartDay
    ) {
        val uid = authRepository.getCurrentUserId()
        val updated = _userProfile.value.copy(
            baseCurrency = baseCurrency,
            billingCycle = billingCycle,
            weeklyStartDay = weeklyStartDay
        )
        applyProfileState(updated)
        if (uid.isNotBlank()) {
            viewModelScope.launch {
                authRepository.updateUserPreferences(uid, baseCurrency, billingCycle, weeklyStartDay)
            }
        }
    }

    fun signOut() {
        profileListenerJob?.cancel()
        profileListenerJob = null
        authRepository.signOut()
        refreshProfileInfo()
        _authState.value = UiState.Idle
    }

    fun resetAuthState() {
        _authState.value = UiState.Idle
    }

    fun triggerGoogleSetupDialog() {
        _showGoogleSetupDialog.value = true
    }

    fun dismissGoogleSetupDialog() {
        _showGoogleSetupDialog.value = false
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
                    refreshProfileInfo(
                        explicitName = user.displayName,
                        explicitEmail = user.email ?: trimmedEmail
                    )
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
                    refreshProfileInfo(
                        explicitName = trimmedName,
                        explicitEmail = trimmedEmail
                    )
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

    fun signInWithGoogle(
        context: Context,
        onSuccess: () -> Unit = {}
    ) {
        val serverClientId = getWebClientId(context)
        if (serverClientId.isBlank()) {
            _showGoogleSetupDialog.value = true
            return
        }

        viewModelScope.launch {
            _authState.value = UiState.Loading
            try {
                val credentialManager = CredentialManager.create(context)
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(serverClientId)
                    .setAutoSelectEnabled(false)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(
                    request = request,
                    context = context
                )

                val credential = result.credential
                if (credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val idToken = googleIdTokenCredential.idToken
                    signInWithGoogleToken(idToken, onSuccess)
                } else {
                    _authState.value = UiState.Error("Unexpected credential type received.")
                }
            } catch (e: GetCredentialCancellationException) {
                _authState.value = UiState.Idle
            } catch (e: NoCredentialException) {
                _authState.value = UiState.Error("No Google accounts found on this device.")
            } catch (e: GetCredentialException) {
                val msg = e.message ?: "Google Sign-In failed."
                if (msg.contains("developer error", ignoreCase = true) ||
                    msg.contains("10:", ignoreCase = true) ||
                    msg.contains("12500", ignoreCase = true) ||
                    msg.contains("fingerprint", ignoreCase = true) ||
                    msg.contains("SHA-1", ignoreCase = true)
                ) {
                    _authState.value = UiState.Idle
                    _showGoogleSetupDialog.value = true
                } else {
                    _authState.value = UiState.Error(msg, e)
                }
            } catch (e: Exception) {
                val msg = e.message ?: "An unexpected error occurred during Google Sign-In."
                _authState.value = UiState.Error(msg, e)
            }
        }
    }

    fun signInWithGoogleToken(
        idToken: String,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _authState.value = UiState.Loading
            val authResult = authRepository.signInWithGoogle(idToken)
            authResult.fold(
                onSuccess = { user ->
                    refreshProfileInfo(
                        explicitName = user.displayName,
                        explicitEmail = user.email
                    )
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

    fun getWebClientId(context: Context): String {
        val defaultIdRes = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
        if (defaultIdRes != 0) {
            val id = context.getString(defaultIdRes)
            if (id.isNotBlank() && !id.contains("webclientid")) return id
        }
        val customIdRes = context.resources.getIdentifier("server_client_id", "string", context.packageName)
        if (customIdRes != 0) {
            val id = context.getString(customIdRes)
            if (id.isNotBlank() && !id.contains("webclientid")) return id
        }
        return DEFAULT_WEB_CLIENT_ID
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
