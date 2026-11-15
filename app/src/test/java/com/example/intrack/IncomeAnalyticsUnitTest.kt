package com.example.intrack

import com.example.intrack.data.model.IncomeEntity
import com.example.intrack.viewmodel.IncomeAnalyticsCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IncomeAnalyticsUnitTest {

    @Test
    fun `computeRollingAverageLkr averages prior non-zero paid months and ignores unpaid`() {
        val recentMonths = listOf("November 2026", "October 2026", "September 2026", "August 2026")
        val incomes = listOf(
            IncomeEntity(id = "nov", realizedAmountLkr = 200_000.0, status = "PAID", dateReceivedMillis = 1L),
            IncomeEntity(id = "oct1", realizedAmountLkr = 120_000.0, status = "PAID", dateReceivedMillis = 2L),
            IncomeEntity(id = "oct_unpaid", realizedAmountLkr = 90_000.0, status = "INVOICED", dateReceivedMillis = 2L),
            IncomeEntity(id = "sep1", realizedAmountLkr = 180_000.0, status = "PAID", dateReceivedMillis = 3L)
            // August has 0 paid income -> excluded from non-zero rolling average
        )

        val avg = IncomeAnalyticsCalculator.computeRollingAverageLkr(
            allIncomes = incomes,
            selectedMonth = "November 2026",
            recentMonths = recentMonths,
            monthMatcher = { millis, month ->
                (millis == 1L && month == "November 2026") ||
                    (millis == 2L && month == "October 2026") ||
                    (millis == 3L && month == "September 2026")
            }
        )

        assertEquals(150_000.0, avg, 0.01)
    }

    @Test
    fun `computeRollingAverageLkr returns zero when no prior months have income`() {
        val recentMonths = listOf("November 2026", "October 2026")
        val avg = IncomeAnalyticsCalculator.computeRollingAverageLkr(
            allIncomes = emptyList(),
            selectedMonth = "November 2026",
            recentMonths = recentMonths
        )
        assertEquals(0.0, avg, 0.01)
    }

    @Test
    fun `buildChannelSlices sorts descending calculates shares and filters zero or negative channels`() {
        val slices = IncomeAnalyticsCalculator.buildChannelSlices(
            salaryTotalLkr = 120_000.0,
            salaryCount = 1,
            freelanceTotalLkr = 40_000.0,
            freelanceCount = 2,
            adsenseTotalLkr = 40_000.0,
            adsenseCount = 1,
            cryptoTotalLkr = -5_000.0,
            cryptoCount = 1
        )

        assertEquals(3, slices.size)
        assertEquals("SALARY", slices[0].channelKey)
        assertEquals(60f, slices[0].percentage, 0.01f)
        assertEquals(20f, slices[1].percentage, 0.01f)
        assertEquals(20f, slices[2].percentage, 0.01f)
    }

    @Test
    fun `buildChannelSlices returns empty list when all channels are zero`() {
        val slices = IncomeAnalyticsCalculator.buildChannelSlices(
            salaryTotalLkr = 0.0,
            salaryCount = 0,
            freelanceTotalLkr = 0.0,
            freelanceCount = 0,
            adsenseTotalLkr = 0.0,
            adsenseCount = 0,
            cryptoTotalLkr = 0.0,
            cryptoCount = 0
        )
        assertTrue(slices.isEmpty())
    }
}
