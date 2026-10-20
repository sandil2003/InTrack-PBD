package com.example.intrack

import com.example.intrack.data.repository.AuthRepository
import com.example.intrack.ui.common.UiState
import com.example.intrack.ui.screens.auth.AuthViewModel
import com.example.intrack.ui.screens.auth.PasswordStrengthLevel
import com.google.firebase.auth.FirebaseUser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FakeAuthRepository : AuthRepository {
    var mockCurrentUser: FirebaseUser? = null
    var shouldFail: Boolean = false

    override fun getCurrentUser(): FirebaseUser? = mockCurrentUser

    override suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser> {
        return if (shouldFail) Result.failure(Exception("Invalid credentials"))
        else Result.success(createMockUser("user_123", email))
    }

    override suspend fun signUpWithEmail(name: String, email: String, password: String): Result<FirebaseUser> {
        return if (shouldFail) Result.failure(Exception("Email in use"))
        else Result.success(createMockUser("new_user_456", email))
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return if (shouldFail) Result.failure(Exception("Network error"))
        else Result.success(Unit)
    }

    override suspend fun signInAnonymously(): Result<FirebaseUser> {
        return Result.success(createMockUser("guest_uid", null))
    }

    override fun signOut() {
        mockCurrentUser = null
    }

    private fun createMockUser(uid: String, email: String?): FirebaseUser {
        // Simple proxy / stub for testing
        return java.lang.reflect.Proxy.newProxyInstance(
            FirebaseUser::class.java.classLoader,
            arrayOf(FirebaseUser::class.java)
        ) { _, method, _ ->
            when (method.name) {
                "getUid" -> uid
                "getEmail" -> email
                else -> null
            }
        } as FirebaseUser
    }
}

class AuthViewModelUnitTest {

    private lateinit var fakeRepo: FakeAuthRepository
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setup() {
        fakeRepo = FakeAuthRepository()
        viewModel = AuthViewModel(fakeRepo)
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
    fun `signUpWithEmail sets error when terms not accepted`() {
        viewModel.signUpWithEmail("Alex Morgan", "test@example.com", "Secret123!", isTermsAccepted = false)
        assertTrue(viewModel.authState.value is UiState.Error)
        assertEquals("Please accept the Terms of Service & Privacy Policy.", (viewModel.authState.value as UiState.Error).message)
    }

    @Test
    fun `continueOffline transitions to success with guest_user`() {
        var callbackTriggered = false
        viewModel.continueOffline {
            callbackTriggered = true
        }
        assertTrue(callbackTriggered)
        assertTrue(viewModel.authState.value is UiState.Success)
        assertEquals("guest_user", (viewModel.authState.value as UiState.Success<String>).data)
    }
}
