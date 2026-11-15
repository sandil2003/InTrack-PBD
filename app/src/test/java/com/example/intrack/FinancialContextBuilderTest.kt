package com.example.intrack

import com.example.intrack.data.ai.FinancialContextBuilder
import com.example.intrack.data.model.ExpenseEntity
import com.example.intrack.data.model.GoalEntity
import com.example.intrack.data.model.IncomeEntity
import com.example.intrack.data.model.SubscriptionEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FinancialContextBuilderTest {

    @Test
    fun `buildSnapshot aggregates current month incomes expenses goals and subscriptions accurately`() {
        val now = 1_760_000_000_000L
        val incomes = listOf(
            IncomeEntity(
                id = "inc1",
                userId = "u1",
                channel = "SALARY",
                title = "Monthly Salary",
                realizedAmountLkr = 135_000.0,
                status = "PAID",
                dateReceivedMillis = now
            ),
            IncomeEntity(
                id = "inc2",
                userId = "u1",
                channel = "ADSENSE",
                title = "AdSense USD",
                realizedAmountLkr = 15_000.0,
                status = "PAID",
                dateReceivedMillis = now
            ),
            IncomeEntity(
                id = "inc3",
                userId = "u1",
                channel = "FREELANCE",
                title = "React SME Milestone",
                realizedAmountLkr = 45_000.0,
                status = "OVERDUE",
                dateReceivedMillis = now
            )
        )

        val expenses = listOf(
            ExpenseEntity(
                id = "exp1",
                userId = "u1",
                title = "Rent",
                amountLkr = 34_000.0,
                category = "RENT",
                commitmentType = "COMMITTED",
                dateSpentMillis = now
            ),
            ExpenseEntity(
                id = "exp2",
                userId = "u1",
                title = "PickMe Ride",
                amountLkr = 2_000.0,
                category = "TRANSPORT",
                commitmentType = "DISCRETIONARY",
                dateSpentMillis = now
            ),
            ExpenseEntity(
                id = "exp3",
                userId = "u1",
                title = "ATM Withdrawal",
                amountLkr = 10_000.0,
                category = "ATM_WITHDRAWAL",
                commitmentType = "TRANSFER",
                dateSpentMillis = now
            )
        )

        val goals = listOf(
            GoalEntity(
                id = "macbook_m4_goal",
                userId = "u1",
                title = "MacBook Pro M4 Fund",
                targetAmountLkr = 490_000.0,
                currentSavedLkr = 98_000.0,
                monthlyTargetRunRateLkr = 40_833.33,
                targetDateMillis = now + (30L * 24 * 60 * 60 * 1000)
            )
        )

        val subscriptions = listOf(
            SubscriptionEntity(
                id = "sub1",
                userId = "u1",
                name = "Gym Membership",
                amountLkr = 4_500.0,
                billingCycle = "MONTHLY",
                status = "ACTIVE",
                isFlaggedForReview = false
            ),
            SubscriptionEntity(
                id = "sub2",
                userId = "u1",
                name = "Unused SaaS",
                amountLkr = 2_200.0,
                billingCycle = "MONTHLY",
                status = "UNUSED_FLAGGED",
                isFlaggedForReview = true
            ),
            SubscriptionEntity(
                id = "sub3",
                userId = "u1",
                name = "Old Plan",
                amountLkr = 1_500.0,
                billingCycle = "MONTHLY",
                status = "CANCELLED"
            )
        )

        val snapshot = FinancialContextBuilder.buildSnapshot(
            userFirstName = "Kavindu",
            baseCurrency = "LKR (Rs) • Sri Lankan Rupee",
            incomes = incomes,
            expenses = expenses,
            goals = goals,
            subscriptions = subscriptions,
            nowMillis = now,
            currentMonthMatcher = { true },
            monthLabel = "November 2026"
        )

        assertEquals("Kavindu", snapshot.userFirstName)
        assertEquals(150_000.0, snapshot.monthlyIncomeLkr, 0.01)
        assertEquals(36_000.0, snapshot.monthlyExpenseLkr, 0.01)
        assertEquals(114_000.0, snapshot.netMonthlyBalanceLkr, 0.01)
        assertEquals(34_000.0, snapshot.committedExpenseLkr, 0.01)
        assertEquals(2_000.0, snapshot.discretionaryExpenseLkr, 0.01)
        assertEquals(1, snapshot.overdueInvoicesCount)
        assertEquals(45_000.0, snapshot.overdueInvoicesTotalLkr, 0.01)
        assertEquals(2, snapshot.activeSubscriptionsCount)
        assertEquals(6_700.0, snapshot.estimatedMonthlySubscriptionsLkr, 0.01)
        assertEquals(1, snapshot.flaggedSubscriptions.size)
        assertEquals(20, snapshot.primaryGoal?.progressPercent)

        val systemInstruction = FinancialContextBuilder.buildSystemInstruction(snapshot)
        assertTrue(systemInstruction.contains("Kavindu"))
        assertTrue(systemInstruction.contains("MacBook Pro M4 Fund"))
        assertTrue(systemInstruction.contains("Unused SaaS"))

        val prompts = FinancialContextBuilder.buildSuggestedPrompts(snapshot)
        assertTrue(prompts.any { it.contains("MacBook Pro M4 Fund") })
        assertTrue(prompts.any { it.contains("flagged subscriptions") })
        assertTrue(prompts.any { it.contains("overdue freelance invoices") })
    }
}
