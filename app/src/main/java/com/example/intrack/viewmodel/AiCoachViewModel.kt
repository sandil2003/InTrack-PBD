package com.example.intrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.intrack.data.ai.AiChatMessage
import com.example.intrack.data.ai.AiChatRole
import com.example.intrack.data.ai.FinancialContextBuilder
import com.example.intrack.data.ai.FinancialContextSnapshot
import com.example.intrack.data.repository.AiCoachRepository
import com.example.intrack.data.repository.AuthRepository
import com.example.intrack.data.repository.ExpenseRepository
import com.example.intrack.data.repository.GoalRepository
import com.example.intrack.data.repository.IncomeRepository
import com.example.intrack.data.repository.SubscriptionRepository
import com.example.intrack.util.CurrencyFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AiCoachUiState(
    val messages: List<AiChatMessage> = emptyList(),
    val isGenerating: Boolean = false,
    val contextSnapshot: FinancialContextSnapshot = FinancialContextSnapshot(),
    val suggestedPrompts: List<String> = FinancialContextBuilder.buildSuggestedPrompts(FinancialContextSnapshot()),
    val lastFailedPrompt: String? = null
)

@HiltViewModel
class AiCoachViewModel @Inject constructor(
    private val incomeRepository: IncomeRepository,
    private val expenseRepository: ExpenseRepository,
    private val goalRepository: GoalRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val authRepository: AuthRepository,
    private val aiCoachRepository: AiCoachRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiCoachUiState())
    val uiState: StateFlow<AiCoachUiState> = _uiState.asStateFlow()

    private var contextJob: Job? = null
    private var generationJob: Job? = null

    init {
        observeContext()
    }

    fun observeContext(userId: String = authRepository.getCurrentUserId()) {
        contextJob?.cancel()
        val fallbackName = authRepository.getCurrentUserDisplayName()
            .trim()
            .split(" ")
            .firstOrNull { it.isNotBlank() }
            ?: "there"

        if (userId.isBlank()) {
            val emptySnapshot = FinancialContextSnapshot(userFirstName = fallbackName)
            _uiState.update {
                it.copy(
                    contextSnapshot = emptySnapshot,
                    suggestedPrompts = FinancialContextBuilder.buildSuggestedPrompts(emptySnapshot)
                )
            }
            return
        }

        contextJob = viewModelScope.launch {
            val profileFlow = authRepository.observeUserProfile(userId).onStart { emit(null) }

            combine(
                incomeRepository.getIncomes(userId).onStart { emit(emptyList()) },
                expenseRepository.getExpenses(userId).onStart { emit(emptyList()) },
                goalRepository.getGoals(userId).onStart { emit(emptyList()) },
                subscriptionRepository.getSubscriptions(userId).onStart { emit(emptyList()) },
                profileFlow
            ) { incomes, expenses, goals, subscriptions, profile ->
                val firstName = profile?.firstName?.takeIf { it.isNotBlank() } ?: fallbackName
                val baseCurrency = profile?.baseCurrency?.takeIf { it.isNotBlank() }
                    ?: CurrencyFormatter.activeBaseCurrency
                FinancialContextBuilder.buildSnapshot(
                    userFirstName = firstName,
                    baseCurrency = baseCurrency,
                    incomes = incomes,
                    expenses = expenses,
                    goals = goals,
                    subscriptions = subscriptions
                )
            }.collect { snapshot ->
                _uiState.update { state ->
                    state.copy(
                        contextSnapshot = snapshot,
                        suggestedPrompts = FinancialContextBuilder.buildSuggestedPrompts(snapshot)
                    )
                }
            }
        }
    }

    fun sendMessage(rawPrompt: String) {
        val prompt = rawPrompt.trim()
        if (prompt.isEmpty() || _uiState.value.isGenerating) return

        val userMessage = AiChatMessage(
            id = UUID.randomUUID().toString(),
            role = AiChatRole.USER,
            text = prompt
        )
        val assistantMessageId = UUID.randomUUID().toString()
        val assistantPlaceholder = AiChatMessage(
            id = assistantMessageId,
            role = AiChatRole.ASSISTANT,
            text = "",
            isStreaming = true
        )

        _uiState.update { state ->
            state.copy(
                messages = state.messages + userMessage + assistantPlaceholder,
                isGenerating = true,
                lastFailedPrompt = null
            )
        }

        val snapshot = _uiState.value.contextSnapshot
        generationJob?.cancel()
        generationJob = viewModelScope.launch {
            val buffer = StringBuilder()
            try {
                aiCoachRepository.sendMessageStream(prompt, snapshot).collect { chunk ->
                    buffer.append(chunk)
                    val currentText = buffer.toString()
                    _uiState.update { state ->
                        state.copy(
                            messages = state.messages.map { msg ->
                                if (msg.id == assistantMessageId) {
                                    msg.copy(text = currentText, isStreaming = true, isError = false)
                                } else {
                                    msg
                                }
                            }
                        )
                    }
                }
                val finalText = buffer.toString().ifBlank {
                    "I couldn't generate a response for that prompt. Please try rephrasing your question."
                }
                _uiState.update { state ->
                    state.copy(
                        isGenerating = false,
                        messages = state.messages.map { msg ->
                            if (msg.id == assistantMessageId) {
                                msg.copy(text = finalText, isStreaming = false, isError = false)
                            } else {
                                msg
                            }
                        }
                    )
                }
            } catch (e: Exception) {
                val errorText = e.message?.takeIf { it.isNotBlank() }
                    ?: "Something went wrong while contacting Gemini."
                _uiState.update { state ->
                    state.copy(
                        isGenerating = false,
                        lastFailedPrompt = prompt,
                        messages = state.messages.map { msg ->
                            if (msg.id == assistantMessageId) {
                                msg.copy(
                                    text = errorText,
                                    isStreaming = false,
                                    isError = true
                                )
                            } else {
                                msg
                            }
                        }
                    )
                }
            }
        }
    }

    fun retryLastMessage() {
        val failedPrompt = _uiState.value.lastFailedPrompt ?: return
        _uiState.update { state ->
            val trimmed = if (state.messages.lastOrNull()?.isError == true) {
                state.messages.dropLast(2)
            } else {
                state.messages
            }
            state.copy(messages = trimmed, lastFailedPrompt = null)
        }
        sendMessage(failedPrompt)
    }

    fun clearConversation() {
        generationJob?.cancel()
        aiCoachRepository.resetSession()
        _uiState.update { state ->
            state.copy(
                messages = emptyList(),
                isGenerating = false,
                lastFailedPrompt = null
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        contextJob?.cancel()
        generationJob?.cancel()
    }
}
