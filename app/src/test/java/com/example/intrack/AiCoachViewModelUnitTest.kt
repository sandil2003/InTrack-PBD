package com.example.intrack

import com.example.intrack.data.ai.AiChatRole
import com.example.intrack.data.ai.FinancialContextSnapshot
import com.example.intrack.data.model.ExpenseEntity
import com.example.intrack.data.model.GoalContributionEntity
import com.example.intrack.data.model.GoalEntity
import com.example.intrack.data.model.IncomeEntity
import com.example.intrack.data.model.SubscriptionEntity
import com.example.intrack.data.model.UserProfile
import com.example.intrack.data.repository.AiCoachRepository
import com.example.intrack.data.repository.AuthRepository
import com.example.intrack.data.repository.ExpenseRepository
import com.example.intrack.data.repository.GoalRepository
import com.example.intrack.data.repository.IncomeRepository
import com.example.intrack.data.repository.SubscriptionRepository
import com.example.intrack.viewmodel.AiCoachViewModel
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

private class FakeAiCoachRepository : AiCoachRepository {
    var chunksToEmit: List<String> = listOf("Hello ", "Kavindu! ", "Your savings look solid.")
    var errorToThrow: Throwable? = null
    var resetCalls: Int = 0
    var lastContextReceived: FinancialContextSnapshot? = null

    override fun sendMessageStream(
        userMessage: String,
        contextSnapshot: FinancialContextSnapshot
    ): Flow<String> = flow {
        lastContextReceived = contextSnapshot
        errorToThrow?.let { throw it }
        for (chunk in chunksToEmit) {
            emit(chunk)
        }
    }

    override fun resetSession() {
        resetCalls++
    }
}

private class StubIncomeRepo(private val items: List<IncomeEntity>) : IncomeRepository {
    override fun getIncomes(userId: String) = flowOf(items)
    override suspend fun addIncome(income: IncomeEntity) = Result.success(Unit)
    override suspend fun updateIncome(income: IncomeEntity) = Result.success(Unit)
    override suspend fun deleteIncome(userId: String, incomeId: String) = Result.success(Unit)
}

private class StubExpenseRepo(private val items: List<ExpenseEntity>) : ExpenseRepository {
    override fun getExpenses(userId: String) = flowOf(items)
    override suspend fun addExpense(expense: ExpenseEntity) = Result.success(Unit)
    override suspend fun updateExpense(expense: ExpenseEntity) = Result.success(Unit)
    override suspend fun deleteExpense(userId: String, expenseId: String) = Result.success(Unit)
}

private class StubGoalRepo(private val items: List<GoalEntity>) : GoalRepository {
    override fun getGoals(userId: String) = flowOf(items)
    override fun getMacBookGoal(userId: String) = flowOf(items.firstOrNull() ?: GoalEntity())
    override fun getGoalContributions(userId: String, goalId: String) = flowOf(emptyList<GoalContributionEntity>())
    override suspend fun addGoal(goal: GoalEntity) = Result.success(Unit)
    override suspend fun updateGoal(goal: GoalEntity) = Result.success(Unit)
    override suspend fun deleteGoal(userId: String, goalId: String) = Result.success(Unit)
    override suspend fun allocateWindfallToGoal(
        userId: String,
        amountLkr: Double,
        goalId: String,
        sourceTitle: String,
        sourceId: String,
        note: String?
    ) = Result.success(Unit)
    override suspend fun addGoalContribution(userId: String, contribution: GoalContributionEntity) = Result.success(Unit)
}

private class StubSubscriptionRepo(private val items: List<SubscriptionEntity>) : SubscriptionRepository {
    override fun getSubscriptions(userId: String) = flowOf(items)
    override suspend fun addSubscription(subscription: SubscriptionEntity) = Result.success(Unit)
    override suspend fun updateSubscription(subscription: SubscriptionEntity) = Result.success(Unit)
    override suspend fun deleteSubscription(userId: String, subscriptionId: String) = Result.success(Unit)
    override suspend fun updateSubscriptionStatus(userId: String, subscriptionId: String, newStatus: String) = Result.success(Unit)
    override suspend fun updateSubscriptionAutoPayment(userId: String, subscriptionId: String, isAutoPayment: Boolean) = Result.success(Unit)
}

private class StubAuthRepo : AuthRepository {
    override fun getCurrentUser(): FirebaseUser? = null
    override fun getCurrentUserId(): String = "user_1"
    override fun getCurrentUserDisplayName(): String = "Kavindu Silva"
    override fun observeUserProfile(userId: String): Flow<UserProfile?> =
        MutableStateFlow(UserProfile(uid = userId, displayName = "Kavindu Silva", baseCurrency = "LKR"))
    override suspend fun updateUserPreferences(userId: String, baseCurrency: String, billingCycle: String, weeklyStartDay: String) = Result.success(Unit)
    override suspend fun updateUserProfileDetails(userId: String, displayName: String, phoneNumber: String, bio: String) = Result.success(Unit)
    override suspend fun signInWithEmail(email: String, password: String) = Result.failure<FirebaseUser>(UnsupportedOperationException())
    override suspend fun signUpWithEmail(name: String, email: String, password: String) = Result.failure<FirebaseUser>(UnsupportedOperationException())
    override suspend fun signInWithGoogle(idToken: String) = Result.failure<FirebaseUser>(UnsupportedOperationException())
    override suspend fun sendPasswordResetEmail(email: String) = Result.success(Unit)
    override fun signOut() {}
}

@OptIn(ExperimentalCoroutinesApi::class)
class AiCoachViewModelUnitTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var fakeAiRepo: FakeAiCoachRepository
    private lateinit var viewModel: AiCoachViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeAiRepo = FakeAiCoachRepository()
        viewModel = AiCoachViewModel(
            incomeRepository = StubIncomeRepo(
                listOf(
                    IncomeEntity(
                        id = "i1",
                        userId = "user_1",
                        channel = "SALARY",
                        realizedAmountLkr = 130_000.0,
                        status = "PAID",
                        dateReceivedMillis = System.currentTimeMillis()
                    )
                )
            ),
            expenseRepository = StubExpenseRepo(
                listOf(
                    ExpenseEntity(
                        id = "e1",
                        userId = "user_1",
                        title = "Rent",
                        amountLkr = 34_000.0,
                        category = "RENT",
                        commitmentType = "COMMITTED",
                        dateSpentMillis = System.currentTimeMillis()
                    )
                )
            ),
            goalRepository = StubGoalRepo(listOf(GoalEntity(userId = "user_1"))),
            subscriptionRepository = StubSubscriptionRepo(emptyList()),
            authRepository = StubAuthRepo(),
            aiCoachRepository = fakeAiRepo
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `sendMessage streams assistant response and grounds live context`() {
        viewModel.sendMessage("How am I doing this month?")

        val state = viewModel.uiState.value
        assertFalse(state.isGenerating)
        assertEquals(2, state.messages.size)
        assertEquals(AiChatRole.USER, state.messages[0].role)
        assertEquals("How am I doing this month?", state.messages[0].text)
        assertEquals(AiChatRole.ASSISTANT, state.messages[1].role)
        assertEquals("Hello Kavindu! Your savings look solid.", state.messages[1].text)
        assertFalse(state.messages[1].isStreaming)
        assertEquals(130_000.0, fakeAiRepo.lastContextReceived?.monthlyIncomeLkr ?: 0.0, 0.01)
    }

    @Test
    fun `sendMessage ignores blank input`() {
        viewModel.sendMessage("   ")
        assertTrue(viewModel.uiState.value.messages.isEmpty())
    }

    @Test
    fun `sendMessage surfaces error and retryLastMessage recovers`() {
        fakeAiRepo.errorToThrow = IllegalStateException("Network timeout")
        viewModel.sendMessage("Audit my subscriptions")

        val errorState = viewModel.uiState.value
        assertFalse(errorState.isGenerating)
        assertEquals(2, errorState.messages.size)
        assertTrue(errorState.messages[1].isError)
        assertEquals("Network timeout", errorState.messages[1].text)
        assertEquals("Audit my subscriptions", errorState.lastFailedPrompt)

        fakeAiRepo.errorToThrow = null
        viewModel.retryLastMessage()

        val recoveredState = viewModel.uiState.value
        assertEquals(2, recoveredState.messages.size)
        assertFalse(recoveredState.messages[1].isError)
        assertEquals("Hello Kavindu! Your savings look solid.", recoveredState.messages[1].text)
    }

    @Test
    fun `clearConversation resets messages and AI session`() {
        viewModel.sendMessage("Hello")
        assertEquals(2, viewModel.uiState.value.messages.size)

        viewModel.clearConversation()
        assertTrue(viewModel.uiState.value.messages.isEmpty())
        assertEquals(1, fakeAiRepo.resetCalls)
    }
}
