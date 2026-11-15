package com.example.intrack

import com.example.intrack.data.model.ExpenseEntity
import com.example.intrack.ui.screens.expenses.calculateCategoryBreakdown
import com.example.intrack.viewmodel.ExpenseAnalyticsCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExpenseAnalyticsUnitTest {

    @Test
    fun `computeRollingAverageLkr averages prior non-zero months and excludes transfers`() {
        val recentMonths = listOf("November 2026", "October 2026", "September 2026", "August 2026")
        val expenses = listOf(
            ExpenseEntity(id = "nov", amountLkr = 90_000.0, category = "RENT", dateSpentMillis = 1L),
            ExpenseEntity(id = "oct1", amountLkr = 60_000.0, category = "RENT", dateSpentMillis = 2L),
            ExpenseEntity(id = "oct_atm", amountLkr = 20_000.0, category = "ATM_WITHDRAWAL", commitmentType = "TRANSFER", dateSpentMillis = 2L),
            ExpenseEntity(id = "sep1", amountLkr = 80_000.0, category = "DINING_OUT", dateSpentMillis = 3L)
            // August has 0 outflow -> excluded from non-zero rolling average
        )

        val avg = ExpenseAnalyticsCalculator.computeRollingAverageLkr(
            allExpenses = expenses,
            selectedMonth = "November 2026",
            recentMonths = recentMonths,
            monthMatcher = { millis, month ->
                (millis == 1L && month == "November 2026") ||
                    (millis == 2L && month == "October 2026") ||
                    (millis == 3L && month == "September 2026")
            }
        )

        assertEquals(70_000.0, avg, 0.01)
    }

    @Test
    fun `computeRollingAverageLkr returns zero when no prior outflow exists`() {
        val recentMonths = listOf("November 2026", "October 2026")
        val avg = ExpenseAnalyticsCalculator.computeRollingAverageLkr(
            allExpenses = emptyList(),
            selectedMonth = "November 2026",
            recentMonths = recentMonths
        )
        assertEquals(0.0, avg, 0.01)
    }

    @Test
    fun `calculateCategoryBreakdown excludes transfers and assigns concise labels with distinct colors`() {
        val expenses = listOf(
            ExpenseEntity(id = "1", title = "Rent", amountLkr = 40_000.0, category = "RENT"),
            ExpenseEntity(id = "2", title = "Internet", amountLkr = 10_000.0, category = "UTILITIES"),
            ExpenseEntity(id = "3", title = "ATM Cash", amountLkr = 15_000.0, category = "ATM_WITHDRAWAL", commitmentType = "TRANSFER")
        )

        val breakdown = calculateCategoryBreakdown(expenses)

        assertEquals(2, breakdown.size)
        assertEquals("Housing", breakdown[0].displayName)
        assertEquals("Utilities", breakdown[1].displayName)
        assertEquals(80f, breakdown[0].percentage, 0.01f)
        assertEquals(20f, breakdown[1].percentage, 0.01f)
        assertTrue(breakdown[0].color != breakdown[1].color)
    }
}
