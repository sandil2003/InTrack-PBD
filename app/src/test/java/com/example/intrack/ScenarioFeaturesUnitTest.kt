package com.example.intrack

import com.example.intrack.data.model.ExpenseEntity
import com.example.intrack.data.model.GoalEntity
import com.example.intrack.data.model.IncomeEntity
import com.example.intrack.data.model.SubscriptionEntity
import com.example.intrack.util.DateUtils
import com.example.intrack.util.FinancialExportService
import com.example.intrack.util.KavinduScenarioSeeder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class ScenarioFeaturesUnitTest {

    @Test
    fun testCashFlowStrictlyExcludesTransfersAndUnpaidInvoices() {
        val now = System.currentTimeMillis()
        val twoMonthsAgo = Calendar.getInstance().apply { add(Calendar.MONTH, -2) }.timeInMillis

        val incomes = listOf(
            IncomeEntity(
                id = "inc_salary",
                title = "Primary Salary",
                channel = "SALARY",
                realizedAmountLkr = 175000.0,
                status = "PAID",
                dateReceivedMillis = now
            ),
            IncomeEntity(
                id = "inc_unpaid_invoice",
                title = "Freelance UI Project",
                channel = "FREELANCE",
                realizedAmountLkr = 65000.0,
                status = "INVOICED", // Not paid!
                dateReceivedMillis = now
            ),
            IncomeEntity(
                id = "inc_overdue_invoice",
                title = "Old Client Web Design",
                channel = "FREELANCE",
                realizedAmountLkr = 45000.0,
                status = "OVERDUE", // Not paid!
                dateReceivedMillis = now
            ),
            IncomeEntity(
                id = "inc_past_month",
                title = "Past Month Freelance",
                channel = "FREELANCE",
                realizedAmountLkr = 50000.0,
                status = "PAID",
                dateReceivedMillis = twoMonthsAgo // Previous month!
            )
        )

        val expenses = listOf(
            ExpenseEntity(
                id = "exp_rent",
                title = "House Rent",
                amountLkr = 34000.0,
                category = "RENT",
                paymentAccount = "ACCOUNT_SECONDARY",
                commitmentType = "COMMITTED",
                dateSpentMillis = now
            ),
            ExpenseEntity(
                id = "exp_atm",
                title = "ATM Cash Withdrawal",
                amountLkr = 15000.0,
                category = "ATM_WITHDRAWAL", // Internal transfer!
                paymentAccount = "CASH",
                commitmentType = "TRANSFER",
                dateSpentMillis = now
            ),
            ExpenseEntity(
                id = "exp_transfer",
                title = "Transfer to Savings",
                amountLkr = 20000.0,
                category = "TRANSFER", // Internal transfer!
                paymentAccount = "CARD_PRIMARY",
                commitmentType = "TRANSFER",
                dateSpentMillis = now
            ),
            ExpenseEntity(
                id = "exp_gym",
                title = "Gym Membership",
                amountLkr = 4500.0,
                category = "FITNESS",
                paymentAccount = "ACCOUNT_SECONDARY",
                commitmentType = "COMMITTED",
                dateSpentMillis = now
            ),
            ExpenseEntity(
                id = "exp_past_month",
                title = "Past Month Rent",
                amountLkr = 34000.0,
                category = "RENT",
                paymentAccount = "ACCOUNT_SECONDARY",
                commitmentType = "COMMITTED",
                dateSpentMillis = twoMonthsAgo
            )
        )

        // Rule: Only current month PAID incomes
        val realizedInflow = incomes.filter {
            it.status == "PAID" && DateUtils.isCurrentMonth(it.dateReceivedMillis)
        }.sumOf { it.realizedAmountLkr }

        // Rule: Only current month NON-TRANSFER expenses
        val realizedOutflow = expenses.filter {
            !it.isInternalTransfer() && DateUtils.isCurrentMonth(it.dateSpentMillis)
        }.sumOf { it.amountLkr }

        val netCashFlow = realizedInflow - realizedOutflow

        // Inflow should ONLY be the current month salary: 175,000 LKR
        assertEquals(175000.0, realizedInflow, 0.01)

        // Outflow should ONLY be Rent (34,000) + Gym (4,500) = 38,500 LKR
        // ATM withdrawal (15,000) and Transfer (20,000) must be excluded to avoid double counting!
        assertEquals(38500.0, realizedOutflow, 0.01)

        // Net cash flow
        assertEquals(175000.0 - 38500.0, netCashFlow, 0.01)
    }

    @Test
    fun testCryptoYtdNetPositionCalculation() {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)

        fun getMillisInQuarter(monthIndex0: Int): Long {
            val c = Calendar.getInstance()
            c.set(Calendar.YEAR, currentYear)
            c.set(Calendar.MONTH, monthIndex0)
            c.set(Calendar.DAY_OF_MONTH, 15)
            return c.timeInMillis
        }

        val incomes = listOf(
            IncomeEntity(
                id = "inc_crypto_q1",
                title = "Binance USDT/ETH Gain",
                channel = "CRYPTO",
                realizedAmountLkr = 45000.0, // Q1 Gain
                status = "PAID",
                dateReceivedMillis = getMillisInQuarter(1) // Feb
            ),
            IncomeEntity(
                id = "inc_crypto_q3",
                title = "Binance Altcoin Loss",
                channel = "CRYPTO",
                realizedAmountLkr = -23000.0, // Q3 Loss
                status = "PAID",
                dateReceivedMillis = getMillisInQuarter(7) // Aug
            ),
            IncomeEntity(
                id = "inc_freelance",
                title = "Mobile UI Consultation",
                channel = "FREELANCE",
                realizedAmountLkr = 80000.0,
                status = "PAID",
                dateReceivedMillis = System.currentTimeMillis()
            )
        )

        val cal = Calendar.getInstance()
        val allPaidCrypto = incomes.filter { it.channel.equals("CRYPTO", ignoreCase = true) && it.status.equals("PAID", ignoreCase = true) }

        var q1Pnl = 0.0
        var q3Pnl = 0.0
        var ytdGains = 0.0
        var ytdLosses = 0.0

        allPaidCrypto.forEach { trade ->
            cal.timeInMillis = trade.dateReceivedMillis
            if (cal.get(Calendar.YEAR) == currentYear) {
                val amount = trade.realizedAmountLkr
                when (cal.get(Calendar.MONTH)) {
                    in 0..2 -> q1Pnl += amount
                    in 6..8 -> q3Pnl += amount
                }
                if (amount >= 0) {
                    ytdGains += amount
                } else {
                    ytdLosses += Math.abs(amount)
                }
            }
        }

        val netPosition = q1Pnl + q3Pnl

        assertEquals(45000.0, q1Pnl, 0.01)
        assertEquals(-23000.0, q3Pnl, 0.01)
        assertEquals(45000.0, ytdGains, 0.01)
        assertEquals(23000.0, ytdLosses, 0.01)
        assertEquals(22000.0, netPosition, 0.01)
    }

    @Test
    fun testZombieSubscriptionsSeededAndFlagged() {
        val subscriptions = KavinduScenarioSeeder.getSeedSubscriptions()
        val zombieSubscriptions = subscriptions.filter { it.isFlaggedForReview }

        // Must seed exactly 2 zombie subscriptions that Kavindu intended to cancel 6 months ago
        assertEquals(2, zombieSubscriptions.size)

        val sub1 = zombieSubscriptions.find { it.id == "sub_zombie_saas" }
        val sub2 = zombieSubscriptions.find { it.id == "sub_zombie_cloud" }

        assertTrue(sub1 != null)
        assertTrue(sub2 != null)
        assertEquals(2200.0, sub1!!.amountLkr, 0.01)
        assertEquals(1800.0, sub2!!.amountLkr, 0.01)
        assertEquals("UNUSED_FLAGGED", sub1.status)
        assertEquals("UNUSED_FLAGGED", sub2.status)

        val totalZombieDrain = zombieSubscriptions.sumOf { it.amountLkr }
        assertEquals(4000.0, totalZombieDrain, 0.01)
        val annualLeak = totalZombieDrain * 12
        assertEquals(48000.0, annualLeak, 0.01)
    }

    @Test
    fun testWindfallAllocationReducesRemainingAndUpdatesGoal() {
        val windfall = IncomeEntity(
            id = "inc_bonus",
            title = "Annual Performance Bonus",
            channel = "SALARY",
            realizedAmountLkr = 75000.0,
            status = "PAID",
            allocatedToGoalLkr = 0.0
        )

        val targetGoal = GoalEntity(
            id = "goal_macbook",
            title = "MacBook Pro M4",
            targetAmountLkr = 490000.0,
            currentSavedLkr = 125000.0,
            category = "TECH"
        )

        val allocationAmount = 50000.0
        val updatedGoal = targetGoal.copy(
            currentSavedLkr = targetGoal.currentSavedLkr + allocationAmount
        )
        val updatedWindfall = windfall.copy(
            allocatedToGoalLkr = allocationAmount
        )

        assertEquals(175000.0, updatedGoal.currentSavedLkr, 0.01)
        assertEquals(50000.0, updatedWindfall.allocatedToGoalLkr, 0.01)
        assertEquals(25000.0, updatedWindfall.realizedAmountLkr - updatedWindfall.allocatedToGoalLkr, 0.01)
    }

    @Test
    fun testCatchUpModeLapseDetection() {
        val twentyDaysAgo = System.currentTimeMillis() - (20L * 24 * 60 * 60 * 1000)
        val daysSinceLastExpense = (System.currentTimeMillis() - twentyDaysAgo) / (1000 * 60 * 60 * 24)

        val isCatchUpModeNeeded = daysSinceLastExpense >= 14
        assertTrue("Catch-up mode should trigger after 14+ days of no logging", isCatchUpModeNeeded)

        val yesterday = System.currentTimeMillis() - (1L * 24 * 60 * 60 * 1000)
        val daysRecent = (System.currentTimeMillis() - yesterday) / (1000 * 60 * 60 * 24)
        val isCatchUpRecent = daysRecent >= 14
        assertFalse("Catch-up mode should NOT trigger when user logged yesterday", isCatchUpRecent)
    }

    @Test
    fun testCsvExportFormatIncludesHeadersAndEscapesFields() {
        val incomes = listOf(
            IncomeEntity(
                id = "inc_1",
                title = "Bonus, Special",
                channel = "SALARY",
                originalCurrency = "LKR",
                originalAmount = 50000.0,
                exchangeRate = 1.0,
                realizedAmountLkr = 50000.0,
                status = "PAID"
            )
        )
        val expenses = listOf(
            ExpenseEntity(
                id = "exp_1",
                title = "Coffee \"Specialty\" Roast",
                amountLkr = 1500.0,
                category = "DINING_OUT",
                paymentAccount = "CASH",
                commitmentType = "DISCRETIONARY"
            )
        )
        val subscriptions = listOf(
            SubscriptionEntity(
                id = "sub_1",
                name = "Cloud Server",
                amountLkr = 1800.0,
                billingCycle = "MONTHLY",
                paymentAccount = "CARD_PRIMARY",
                status = "UNUSED_FLAGGED",
                isFlaggedForReview = true
            )
        )
        val goals = listOf(
            GoalEntity(
                id = "goal_1",
                title = "MacBook Pro M4",
                targetAmountLkr = 490000.0,
                currentSavedLkr = 175000.0
            )
        )

        val csv = FinancialExportService.buildCsvContent(incomes, expenses, subscriptions, goals)

        // Verify CSV header sections
        assertTrue(csv.contains("InTrack Financial Management System - Exported Statement"))
        assertTrue(csv.contains("--- INCOMES ---"))
        assertTrue(csv.contains("--- EXPENSES ---"))
        assertTrue(csv.contains("--- RECURRING OBLIGATIONS & SUBSCRIPTIONS ---"))
        assertTrue(csv.contains("--- SAVINGS GOALS ---"))

        // Verify escaping: Commas quoted, quotes doubled
        assertTrue("Values with commas must be quoted", csv.contains("\"Bonus, Special\""))
        assertTrue("Values with quotes must be escaped with double quotes", csv.contains("\"Coffee \"\"Specialty\"\" Roast\""))
        assertTrue(csv.contains("490000.0"))
        assertTrue(csv.contains("1800.0"))
    }
}
