package com.example.intrack.data.ai

import java.util.UUID

enum class AiChatRole {
    USER,
    ASSISTANT
}

data class AiChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val role: AiChatRole,
    val text: String,
    val timestampMillis: Long = System.currentTimeMillis(),
    val isStreaming: Boolean = false,
    val isError: Boolean = false
)

data class GoalSnapshot(
    val title: String,
    val targetAmountLkr: Double,
    val currentSavedLkr: Double,
    val monthlyTargetRunRateLkr: Double,
    val progressPercent: Int,
    val daysRemaining: Long,
    val isCompleted: Boolean
)

data class SubscriptionSnapshot(
    val name: String,
    val amountLkr: Double,
    val billingCycle: String,
    val status: String,
    val isFlagged: Boolean
)

data class FinancialContextSnapshot(
    val userFirstName: String = "there",
    val monthLabel: String = "",
    val currencyCode: String = "LKR",
    val monthlyIncomeLkr: Double = 0.0,
    val monthlyExpenseLkr: Double = 0.0,
    val netMonthlyBalanceLkr: Double = 0.0,
    val committedExpenseLkr: Double = 0.0,
    val discretionaryExpenseLkr: Double = 0.0,
    val discretionarySharePercent: Int = 0,
    val incomeByChannel: Map<String, Double> = emptyMap(),
    val overdueInvoicesCount: Int = 0,
    val overdueInvoicesTotalLkr: Double = 0.0,
    val pendingInvoicesCount: Int = 0,
    val pendingInvoicesTotalLkr: Double = 0.0,
    val topCategories: List<Pair<String, Double>> = emptyList(),
    val goals: List<GoalSnapshot> = emptyList(),
    val activeSubscriptionsCount: Int = 0,
    val estimatedMonthlySubscriptionsLkr: Double = 0.0,
    val flaggedSubscriptions: List<SubscriptionSnapshot> = emptyList()
) {
    val primaryGoal: GoalSnapshot?
        get() = goals.firstOrNull { !it.isCompleted } ?: goals.firstOrNull()
}
