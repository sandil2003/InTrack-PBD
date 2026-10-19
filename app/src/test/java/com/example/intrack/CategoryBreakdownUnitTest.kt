package com.example.intrack

import com.example.intrack.data.model.ExpenseEntity
import com.example.intrack.ui.screens.expenses.calculateCategoryBreakdown
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CategoryBreakdownUnitTest {

    private val sampleExpenses = listOf(
        ExpenseEntity(
            id = "1",
            title = "Monthly House Rent",
            amountLkr = 34000.0,
            category = "RENT",
            paymentAccount = "ACCOUNT_SECONDARY"
        ),
        ExpenseEntity(
            id = "2",
            title = "Coffee & Casual Dining",
            amountLkr = 24000.0,
            category = "DINING_OUT",
            paymentAccount = "CARD_PRIMARY"
        ),
        ExpenseEntity(
            id = "3",
            title = "UberEats Takeout",
            amountLkr = 6000.0,
            category = "DINING_OUT",
            paymentAccount = "CARD_PRIMARY"
        ),
        ExpenseEntity(
            id = "4",
            title = "PickMe Night Rides",
            amountLkr = 6800.0,
            category = "TRANSPORT",
            paymentAccount = "CARD_PRIMARY"
        ),
        ExpenseEntity(
            id = "5",
            title = "Electricity & Water Bill",
            amountLkr = 9200.0,
            category = "UTILITIES",
            paymentAccount = "CARD_PRIMARY"
        )
    )

    @Test
    fun calculateCategoryBreakdown_aggregatesAndSortsDescending() {
        val breakdown = calculateCategoryBreakdown(sampleExpenses)

        // Expected:
        // RENT: 34000
        // DINING_OUT: 30000 (24000 + 6000)
        // UTILITIES: 9200
        // TRANSPORT: 6800
        // Total = 80000
        assertEquals(4, breakdown.size)
        assertEquals("RENT", breakdown[0].category)
        assertEquals(34000.0, breakdown[0].amountLkr, 0.001)
        assertEquals(42.5f, breakdown[0].percentage, 0.1f) // 34000 / 80000 * 100 = 42.5%

        assertEquals("DINING_OUT", breakdown[1].category)
        assertEquals(30000.0, breakdown[1].amountLkr, 0.001)
        assertEquals(37.5f, breakdown[1].percentage, 0.1f) // 30000 / 80000 * 100 = 37.5%

        assertEquals("UTILITIES", breakdown[2].category)
        assertEquals(9200.0, breakdown[2].amountLkr, 0.001)
        assertEquals(11.5f, breakdown[2].percentage, 0.1f)

        assertEquals("TRANSPORT", breakdown[3].category)
        assertEquals(6800.0, breakdown[3].amountLkr, 0.001)
        assertEquals(8.5f, breakdown[3].percentage, 0.1f)
    }

    @Test
    fun calculateCategoryBreakdown_topBleedCategoryIsRankedFirst() {
        // Test scenario where dining out is the highest spend
        val highDiningExpenses = listOf(
            ExpenseEntity(id = "1", title = "Coffee", amountLkr = 25000.0, category = "DINING_OUT"),
            ExpenseEntity(id = "2", title = "Transport", amountLkr = 5000.0, category = "TRANSPORT")
        )

        val breakdown = calculateCategoryBreakdown(highDiningExpenses)
        assertEquals("DINING_OUT", breakdown.first().category)
        assertEquals(25000.0, breakdown.first().amountLkr, 0.001)
        assertEquals(83.33f, breakdown.first().percentage, 0.1f)
    }

    @Test
    fun calculateCategoryBreakdown_emptyListReturnsEmpty() {
        val emptyBreakdown = calculateCategoryBreakdown(emptyList())
        assertTrue(emptyBreakdown.isEmpty())
    }

    @Test
    fun calculateCategoryBreakdown_singleCategoryReturns100Percent() {
        val single = listOf(ExpenseEntity(id = "1", title = "Groceries", amountLkr = 5000.0, category = "MISC"))
        val breakdown = calculateCategoryBreakdown(single)

        assertEquals(1, breakdown.size)
        assertEquals(100f, breakdown[0].percentage, 0.01f)
    }
}
