package com.example.intrack

import com.example.intrack.data.model.UserProfile
import com.example.intrack.data.repository.AuthRepository
import com.example.intrack.ui.common.UiState
import com.example.intrack.viewmodel.AuthViewModel
import com.example.intrack.viewmodel.PasswordStrengthLevel
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FakeAuthRepository : AuthRepository {
    var mockCurrentUser: FirebaseUser? = null
    var shouldFail: Boolean = false
    val profileFlow = MutableStateFlow<UserProfile?>(null)

    override fun getCurrentUser(): FirebaseUser? = mockCurrentUser

    override fun observeUserProfile(userId: String): Flow<UserProfile?> = profileFlow

    override suspend fun updateUserPreferences(
        userId: String,
        baseCurrency: String,
        billingCycle: String,
        weeklyStartDay: String
    ): Result<Unit> {
        val current = profileFlow.value ?: UserProfile(uid = userId)
        profileFlow.value = current.copy(
            baseCurrency = baseCurrency,
            billingCycle = billingCycle,
            weeklyStartDay = weeklyStartDay
        )
        return Result.success(Unit)
    }

    override suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser> {
        return if (shouldFail) {
            Result.failure(Exception("Invalid credentials"))
        } else {
            val user = createMockUser("user_123", email, "Test User")
            mockCurrentUser = user
            profileFlow.value = UserProfile(uid = "user_123", displayName = "Test User", email = email)
            Result.success(user)
        }
    }

    override suspend fun signUpWithEmail(name: String, email: String, password: String): Result<FirebaseUser> {
        return if (shouldFail) {
            Result.failure(Exception("Email in use"))
        } else {
            val user = createMockUser("new_user_456", email, name)
            mockCurrentUser = user
            profileFlow.value = UserProfile(uid = "new_user_456", displayName = name, email = email)
            Result.success(user)
        }
    }

    override suspend fun signInWithGoogle(idToken: String): Result<FirebaseUser> {
        return if (shouldFail) {
            Result.failure(Exception("Google Sign-In failed"))
        } else {
            val user = createMockUser("google_user_789", "google@example.com", "Google User")
            mockCurrentUser = user
            profileFlow.value = UserProfile(uid = "google_user_789", displayName = "Google User", email = "google@example.com")
            Result.success(user)
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return if (shouldFail) Result.failure(Exception("Network error"))
        else Result.success(Unit)
    }

    override fun signOut() {
        mockCurrentUser = null
        profileFlow.value = null
    }

    fun createMockUser(uid: String, email: String?, displayName: String? = null): FirebaseUser {
        val user = org.mockito.Mockito.mock(FirebaseUser::class.java)
        org.mockito.Mockito.`when`(user.uid).thenReturn(uid)
        org.mockito.Mockito.`when`(user.email).thenReturn(email)
        org.mockito.Mockito.`when`(user.displayName).thenReturn(displayName)
        return user
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelUnitTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var fakeRepo: FakeAuthRepository
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepo = FakeAuthRepository()
        viewModel = AuthViewModel(fakeRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `password strength calculator evaluates empty password`() {
        val strength = viewModel.calculatePasswordStrength("")
        assertEquals(PasswordStrengthLevel.EMPTY, strength)
    }

    @Test
    fun `password strength calculator evaluates short weak password`() {
        val strength = viewModel.calculatePasswordStrength("123")
        assertEquals(PasswordStrengthLevel.WEAK, strength)
    }

    @Test
    fun `password strength calculator evaluates fair password`() {
        val strength = viewModel.calculatePasswordStrength("abcdefgh")
        assertEquals(PasswordStrengthLevel.FAIR, strength)
    }

    @Test
    fun `password strength calculator evaluates strong password`() {
        val strength = viewModel.calculatePasswordStrength("InTrack2026!")
        assertEquals(PasswordStrengthLevel.STRONG, strength)
    }

    @Test
    fun `signUpWithEmail sets error when name is empty`() {
        viewModel.signUpWithEmail("", "test@example.com", "Secret123!", isTermsAccepted = true)
        assertTrue(viewModel.authState.value is UiState.Error)
        assertEquals("Please enter your full name.", (viewModel.authState.value as UiState.Error).message)
    }

    @Test
    fun `signUpWithEmail sets error when email is empty`() {
        viewModel.signUpWithEmail("Test User", "", "Secret123!", isTermsAccepted = true)
        assertTrue(viewModel.authState.value is UiState.Error)
        assertEquals("Please enter your email address.", (viewModel.authState.value as UiState.Error).message)
    }

    @Test
    fun `signUpWithEmail sets error when email format is invalid`() {
        viewModel.signUpWithEmail("Test User", "invalid-email-format", "Secret123!", isTermsAccepted = true)
        assertTrue(viewModel.authState.value is UiState.Error)
        assertEquals("Please enter a valid email address.", (viewModel.authState.value as UiState.Error).message)
    }

    @Test
    fun `signUpWithEmail sets error when password is under 6 characters`() {
        viewModel.signUpWithEmail("Test User", "test@example.com", "12345", isTermsAccepted = true)
        assertTrue(viewModel.authState.value is UiState.Error)
        assertEquals("Password must be at least 6 characters.", (viewModel.authState.value as UiState.Error).message)
    }

    @Test
    fun `signUpWithEmail sets error when terms not accepted`() {
        viewModel.signUpWithEmail("Test User", "test@example.com", "Secret123!", isTermsAccepted = false)
        assertTrue(viewModel.authState.value is UiState.Error)
        assertEquals("Please accept the Terms of Service & Privacy Policy.", (viewModel.authState.value as UiState.Error).message)
    }

    @Test
    fun `signInWithEmail sets error when email is empty`() {
        viewModel.signInWithEmail("", "Secret123!")
        assertTrue(viewModel.authState.value is UiState.Error)
        assertEquals("Please enter your email and password.", (viewModel.authState.value as UiState.Error).message)
    }

    @Test
    fun `signInWithEmail sets error when password is empty`() {
        viewModel.signInWithEmail("test@example.com", "")
        assertTrue(viewModel.authState.value is UiState.Error)
        assertEquals("Please enter your email and password.", (viewModel.authState.value as UiState.Error).message)
    }

    @Test
    fun `signInWithEmail sets error when email format is invalid`() {
        viewModel.signInWithEmail("notanemail", "Secret123!")
        assertTrue(viewModel.authState.value is UiState.Error)
        assertEquals("Please enter a valid email address.", (viewModel.authState.value as UiState.Error).message)
    }

    @Test
    fun `unauthenticated repository returns empty userId instead of hardcoded guest_user`() {
        fakeRepo.mockCurrentUser = null
        assertEquals("", fakeRepo.getCurrentUserId())
        assertEquals("", fakeRepo.getCurrentUserDisplayName())
        assertEquals("", fakeRepo.getCurrentUserEmail())
    }

    @Test
    fun `signOut resets authState to Idle and clears profile state`() {
        viewModel.signInWithEmail("user@example.com", "Password123!")
        assertTrue(viewModel.authState.value is UiState.Success)
        assertEquals("Test User", viewModel.userDisplayName.value)

        viewModel.signOut()
        assertEquals(UiState.Idle, viewModel.authState.value)
        assertEquals("", viewModel.userDisplayName.value)
        assertEquals("", viewModel.userEmail.value)
    }

    @Test
    fun `clearError resets Error state back to Idle`() {
        viewModel.signInWithEmail("", "")
        assertTrue(viewModel.authState.value is UiState.Error)

        viewModel.clearError()
        assertEquals(UiState.Idle, viewModel.authState.value)
    }

    @Test
    fun `triggerGoogleSetupDialog and dismissGoogleSetupDialog control dialog state`() {
        assertEquals(false, viewModel.showGoogleSetupDialog.value)

        viewModel.triggerGoogleSetupDialog()
        assertEquals(true, viewModel.showGoogleSetupDialog.value)

        viewModel.dismissGoogleSetupDialog()
        assertEquals(false, viewModel.showGoogleSetupDialog.value)
    }

    @Test
    fun `checkUser transitions to Success when currentUser exists`() {
        fakeRepo.mockCurrentUser = fakeRepo.createMockUser("active_uid_999", "user@intrack.io", "Active Member")
        viewModel.checkUser()

        assertTrue(viewModel.authState.value is UiState.Success)
        assertEquals("active_uid_999", (viewModel.authState.value as UiState.Success<String>).data)
        assertEquals("Active Member", viewModel.userDisplayName.value)
        assertEquals("AM", viewModel.userInitials.value)
    }

    @Test
    fun `checkUser transitions to Idle when currentUser is null`() {
        fakeRepo.mockCurrentUser = null
        viewModel.checkUser()

        assertEquals(UiState.Idle, viewModel.authState.value)
    }

    @Test
    fun `resetAuthState resets success state to Idle`() {
        viewModel.signInWithEmail("user@example.com", "Password123!")
        assertTrue(viewModel.authState.value is UiState.Success)

        viewModel.resetAuthState()
        assertEquals(UiState.Idle, viewModel.authState.value)
    }

    @Test
    fun `default web client id matches Firebase production client id`() {
        assertEquals(
            "527660795071-0i7jrn1s461bs3jdf3a8227aiblrmh37.apps.googleusercontent.com",
            AuthViewModel.DEFAULT_WEB_CLIENT_ID
        )
    }

    @Test
    fun `signInWithGoogleToken sets success on valid token`() {
        fakeRepo.shouldFail = false
        var callbackTriggered = false
        viewModel.signInWithGoogleToken("test_valid_id_token") {
            callbackTriggered = true
        }

        assertTrue(callbackTriggered)
        assertTrue(viewModel.authState.value is UiState.Success)
        assertEquals("google_user_789", (viewModel.authState.value as UiState.Success<String>).data)
        assertEquals("Google User", viewModel.userDisplayName.value)
    }

    @Test
    fun `signInWithGoogleToken sets error on invalid token`() {
        fakeRepo.shouldFail = true
        viewModel.signInWithGoogleToken("test_invalid_id_token")

        assertTrue(viewModel.authState.value is UiState.Error)
        assertEquals("Google Sign-In failed", (viewModel.authState.value as UiState.Error).message)
    }

    @Test
    fun `signInWithEmail executes successfully with valid credentials`() {
        fakeRepo.shouldFail = false
        var successCallback = false
        viewModel.signInWithEmail("user@example.com", "Password123!") {
            successCallback = true
        }

        assertTrue(successCallback)
        assertTrue(viewModel.authState.value is UiState.Success)
        assertEquals("user_123", (viewModel.authState.value as UiState.Success<String>).data)
    }

    @Test
    fun `signInWithEmail maps failure message on invalid credentials`() {
        fakeRepo.shouldFail = true
        viewModel.signInWithEmail("user@example.com", "WrongPassword!")

        assertTrue(viewModel.authState.value is UiState.Error)
        assertEquals("Invalid credentials", (viewModel.authState.value as UiState.Error).message)
    }

    @Test
    fun `signUpWithEmail executes successfully and updates user profile state`() {
        fakeRepo.shouldFail = false
        var successCallback = false
        viewModel.signUpWithEmail("Nimal Perera", "nimal@example.com", "SecurePass123!", isTermsAccepted = true) {
            successCallback = true
        }

        assertTrue(successCallback)
        assertTrue(viewModel.authState.value is UiState.Success)
        assertEquals("new_user_456", (viewModel.authState.value as UiState.Success<String>).data)
        assertEquals("Nimal Perera", viewModel.userDisplayName.value)
        assertEquals("nimal@example.com", viewModel.userEmail.value)
        assertEquals("NP", viewModel.userInitials.value)
    }

    @Test
    fun `signUpWithEmail maps failure on duplicate email collision`() {
        fakeRepo.shouldFail = true
        viewModel.signUpWithEmail("Nimal Perera", "nimal@example.com", "SecurePass123!", isTermsAccepted = true)

        assertTrue(viewModel.authState.value is UiState.Error)
        assertEquals("Email in use", (viewModel.authState.value as UiState.Error).message)
    }

    @Test
    fun `sendPasswordReset succeeds with valid email`() {
        fakeRepo.shouldFail = false
        var sentCallback = false
        viewModel.sendPasswordReset("user@example.com") {
            sentCallback = true
        }

        assertTrue(sentCallback)
        assertTrue(viewModel.resetPasswordState.value is UiState.Success)
        assertEquals(
            "Password reset instructions sent to user@example.com",
            (viewModel.resetPasswordState.value as UiState.Success<String>).data
        )
    }

    @Test
    fun `sendPasswordReset fails with invalid email format`() {
        viewModel.sendPasswordReset("not-an-email")

        assertTrue(viewModel.resetPasswordState.value is UiState.Error)
        assertEquals(
            "Please enter a valid email to receive reset instructions.",
            (viewModel.resetPasswordState.value as UiState.Error).message
        )
    }

    @Test
    fun `updatePreferences persists currency and cycle to userProfile`() {
        viewModel.signInWithEmail("user@example.com", "Password123!")
        viewModel.updatePreferences(
            baseCurrency = "USD ($) • US Dollar",
            billingCycle = "1st of Every Month",
            weeklyStartDay = "Sunday"
        )
        assertEquals("USD ($) • US Dollar", viewModel.userProfile.value.baseCurrency)
        assertEquals("1st of Every Month", viewModel.userProfile.value.billingCycle)
        assertEquals("Sunday", viewModel.userProfile.value.weeklyStartDay)
    }
}
