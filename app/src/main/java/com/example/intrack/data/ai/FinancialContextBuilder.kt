package com.example.intrack.data.ai

import com.example.intrack.data.model.ExpenseEntity
import com.example.intrack.data.model.GoalEntity
import com.example.intrack.data.model.IncomeEntity
import com.example.intrack.data.model.SubscriptionEntity
import com.example.intrack.util.CurrencyFormatter
import com.example.intrack.util.DateUtils
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Pure Kotlin aggregator that converts live InTrack entities into a compact,
 * privacy-conscious [FinancialContextSnapshot] and system prompt grounding block.
 */
object FinancialContextBuilder {

    fun buildSnapshot(
        userFirstName: String,
        baseCurrency: String,
        incomes: List<IncomeEntity>,
        expenses: List<ExpenseEntity>,
        goals: List<GoalEntity>,
        subscriptions: List<SubscriptionEntity>,
        nowMillis: Long = System.currentTimeMillis(),
        currentMonthMatcher: (Long) -> Boolean = { DateUtils.isCurrentMonth(it) },
        monthLabel: String = DateUtils.getCurrentMonthName()
    ): FinancialContextSnapshot {
        val currentMonthPaidIncomes = incomes.filter {
            it.status.equals("PAID", ignoreCase = true) && currentMonthMatcher(it.dateReceivedMillis)
        }
        val monthlyIncomeLkr = currentMonthPaidIncomes.sumOf { it.realizedAmountLkr }
        val incomeByChannel = currentMonthPaidIncomes
            .groupBy { it.channel.uppercase(Locale.ROOT) }
            .mapValues { (_, list) -> list.sumOf { it.realizedAmountLkr } }
            .toList()
            .sortedByDescending { it.second }
            .toMap()

        val overdueIncomes = incomes.filter { it.status.equals("OVERDUE", ignoreCase = true) }
        val pendingIncomes = incomes.filter { it.status.equals("INVOICED", ignoreCase = true) }

        val currentMonthExpenses = expenses.filter {
            !it.isInternalTransfer() && currentMonthMatcher(it.dateSpentMillis)
        }
        val monthlyExpenseLkr = currentMonthExpenses.sumOf { it.amountLkr }
        val committedExpenseLkr = currentMonthExpenses.filter { it.isCommittedExpense() }.sumOf { it.amountLkr }
        val discretionaryExpenseLkr = currentMonthExpenses.filter { it.isDiscretionaryExpense() }.sumOf { it.amountLkr }
        val discretionarySharePercent = if (monthlyExpenseLkr > 0.0) {
            ((discretionaryExpenseLkr / monthlyExpenseLkr) * 100.0).roundToInt().coerceIn(0, 100)
        } else {
            0
        }

        val topCategories = currentMonthExpenses
            .groupBy { it.category.uppercase(Locale.ROOT) }
            .mapValues { (_, items) -> items.sumOf { it.amountLkr } }
            .toList()
            .sortedByDescending { it.second }
            .take(5)

        val goalSnapshots = goals
            .sortedWith(compareBy<GoalEntity> { it.isCompleted }.thenBy { it.targetDateMillis })
            .map { goal ->
                val pct = if (goal.targetAmountLkr > 0.0) {
                    ((goal.currentSavedLkr / goal.targetAmountLkr) * 100.0).roundToInt().coerceIn(0, 100)
                } else {
                    0
                }
                GoalSnapshot(
                    title = goal.title,
                    targetAmountLkr = goal.targetAmountLkr,
                    currentSavedLkr = goal.currentSavedLkr,
                    monthlyTargetRunRateLkr = goal.monthlyTargetRunRateLkr,
                    progressPercent = pct,
                    daysRemaining = DateUtils.daysBetween(nowMillis, goal.targetDateMillis),
                    isCompleted = goal.isCompleted
                )
            }

        val activeSubs = subscriptions.filter { !it.status.equals("CANCELLED", ignoreCase = true) }
        val estimatedMonthlySubsLkr = activeSubs.sumOf { normalizeMonthlySubscriptionLkr(it) }
        val flaggedSubs = activeSubs
            .filter { it.isFlaggedForReview || it.status.equals("UNUSED_FLAGGED", ignoreCase = true) }
            .map {
                SubscriptionSnapshot(
                    name = it.name,
                    amountLkr = it.amountLkr,
                    billingCycle = it.billingCycle,
                    status = it.status,
                    isFlagged = true
                )
            }

        return FinancialContextSnapshot(
            userFirstName = userFirstName.ifBlank { "there" },
            monthLabel = monthLabel,
            currencyCode = CurrencyFormatter.getCurrencyCode(baseCurrency),
            monthlyIncomeLkr = monthlyIncomeLkr,
            monthlyExpenseLkr = monthlyExpenseLkr,
            netMonthlyBalanceLkr = monthlyIncomeLkr - monthlyExpenseLkr,
            committedExpenseLkr = committedExpenseLkr,
            discretionaryExpenseLkr = discretionaryExpenseLkr,
            discretionarySharePercent = discretionarySharePercent,
            incomeByChannel = incomeByChannel,
            overdueInvoicesCount = overdueIncomes.size,
            overdueInvoicesTotalLkr = overdueIncomes.sumOf { it.realizedAmountLkr },
            pendingInvoicesCount = pendingIncomes.size,
            pendingInvoicesTotalLkr = pendingIncomes.sumOf { it.realizedAmountLkr },
            topCategories = topCategories,
            goals = goalSnapshots,
            activeSubscriptionsCount = activeSubs.size,
            estimatedMonthlySubscriptionsLkr = estimatedMonthlySubsLkr,
            flaggedSubscriptions = flaggedSubs
        )
    }

    fun buildSystemInstruction(snapshot: FinancialContextSnapshot): String {
        val channelSummary = if (snapshot.incomeByChannel.isEmpty()) {
            "None recorded this month"
        } else {
            snapshot.incomeByChannel.entries.joinToString(", ") { (channel, amount) ->
                "$channel: ${formatLkr(amount)}"
            }
        }

        val categorySummary = if (snapshot.topCategories.isEmpty()) {
            "No expenses recorded this month"
        } else {
            snapshot.topCategories.joinToString(", ") { (category, amount) ->
                "$category: ${formatLkr(amount)}"
            }
        }

        val goalsSummary = if (snapshot.goals.isEmpty()) {
            "No active savings goals"
        } else {
            snapshot.goals.joinToString(" | ") { g ->
                val remaining = (g.targetAmountLkr - g.currentSavedLkr).coerceAtLeast(0.0)
                val statusLabel = if (g.isCompleted) "COMPLETED" else "${g.progressPercent}% (${formatLkr(g.currentSavedLkr)} / ${formatLkr(g.targetAmountLkr)}, remaining ${formatLkr(remaining)}, target run-rate ${formatLkr(g.monthlyTargetRunRateLkr)}/mo, ${g.daysRemaining} days left)"
                "${g.title}: $statusLabel"
            }
        }

        val flaggedSubsSummary = if (snapshot.flaggedSubscriptions.isEmpty()) {
            "None flagged"
        } else {
            snapshot.flaggedSubscriptions.joinToString(", ") { s ->
                "${s.name} (${formatLkr(s.amountLkr)}/${s.billingCycle.lowercase(Locale.ROOT)})"
            }
        }

        return """
            You are InTrack AI Coach, a practical, empathetic personal finance assistant built into the InTrack Android app for Sri Lankan professionals.
            Always ground your advice strictly in the user's live financial snapshot below. Never fabricate figures that are not present in the snapshot; if data is missing or zero, say so clearly and suggest logging it in InTrack.
            Format currency values clearly in ${snapshot.currencyCode} / LKR (e.g., LKR 12,500), keep responses concise, scannable (use short bullet points where helpful), and give actionable next steps.

            LIVE FINANCIAL SNAPSHOT (${snapshot.monthLabel})
            - User first name: ${snapshot.userFirstName}
            - Preferred display currency: ${snapshot.currencyCode} (all ledger amounts below are in LKR)
            - Current month paid income: ${formatLkr(snapshot.monthlyIncomeLkr)} ($channelSummary)
            - Unpaid freelance invoices: ${snapshot.overdueInvoicesCount} overdue (${formatLkr(snapshot.overdueInvoicesTotalLkr)}), ${snapshot.pendingInvoicesCount} invoiced/pending (${formatLkr(snapshot.pendingInvoicesTotalLkr)})
            - Current month outflow (excluding internal transfers): ${formatLkr(snapshot.monthlyExpenseLkr)}
            - Net monthly cashflow: ${formatLkr(snapshot.netMonthlyBalanceLkr)}
            - Committed vs Discretionary split: Committed ${formatLkr(snapshot.committedExpenseLkr)}, Discretionary ${formatLkr(snapshot.discretionaryExpenseLkr)} (${snapshot.discretionarySharePercent}% of outflow)
            - Top spending categories this month: $categorySummary
            - Savings goals: $goalsSummary
            - Subscriptions: ${snapshot.activeSubscriptionsCount} active (~${formatLkr(snapshot.estimatedMonthlySubscriptionsLkr)}/mo); Flagged for review: $flaggedSubsSummary
        """.trimIndent()
    }

    fun buildSuggestedPrompts(snapshot: FinancialContextSnapshot): List<String> {
        val prompts = mutableListOf<String>()
        val primaryGoal = snapshot.primaryGoal
        if (primaryGoal != null && !primaryGoal.isCompleted) {
            prompts.add("How can I hit my ${primaryGoal.title} run-rate this month?")
        } else {
            prompts.add("How is my monthly cashflow looking so far?")
        }
        if (snapshot.flaggedSubscriptions.isNotEmpty()) {
            prompts.add("Audit my flagged subscriptions and potential savings")
        } else {
            prompts.add("Where is most of my discretionary spending going?")
        }
        if (snapshot.overdueInvoicesCount > 0) {
            prompts.add("How do my overdue freelance invoices impact my cashflow?")
        } else {
            prompts.add("Can I afford a LKR 15,000 weekend outing without hurting my goals?")
        }
        prompts.add("Give me 3 concrete steps to increase my savings this week")
        return prompts.distinct().take(4)
    }

    private fun normalizeMonthlySubscriptionLkr(sub: SubscriptionEntity): Double {
        return when (sub.billingCycle.uppercase(Locale.ROOT)) {
            "WEEKLY" -> sub.amountLkr * 4.33
            "QUARTERLY" -> sub.amountLkr / 3.0
            "ANNUAL", "YEARLY" -> sub.amountLkr / 12.0
            else -> sub.amountLkr
        }
    }

    private fun formatLkr(amount: Double): String {
        return String.format(Locale.US, "LKR %,.2f", amount)
    }
}
