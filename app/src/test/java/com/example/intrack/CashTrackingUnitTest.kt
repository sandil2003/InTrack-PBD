package com.example.intrack

import com.example.intrack.data.model.ExpenseEntity
import com.example.intrack.ui.screens.transactions.UnifiedTransaction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CashTrackingUnitTest {

    private val sampleExpenses = listOf(
        ExpenseEntity(
            id = "1",
            title = "Monthly House Rent",
            amountLkr = 34000.0,
            category = "RENT",
            paymentAccount = "ACCOUNT_SECONDARY",
            commitmentType = "COMMITTED"
        ),
        ExpenseEntity(
            id = "2",
            title = "ATM Cash Withdrawal",
            amountLkr = 15000.0,
            category = "MISC",
            paymentAccount = "CASH",
            commitmentType = "DISCRETIONARY"
        ),
        ExpenseEntity(
            id = "3",
            title = "UberEats Dinner",
            amountLkr = 2850.0,
            category = "DINING_OUT",
            paymentAccount = "CARD_PRIMARY",
            commitmentType = "DISCRETIONARY"
        ),
        ExpenseEntity(
            id = "4",
            title = "Coffee & Bakery",
            amountLkr = 1450.0,
            category = "DINING_OUT",
            paymentAccount = "CASH",
            commitmentType = "DISCRETIONARY"
        ),
        ExpenseEntity(
            id = "5",
            title = "Electricity & Water Bill",
            amountLkr = 8500.0,
            category = "UTILITIES",
            paymentAccount = "CARD_PRIMARY",
            commitmentType = "COMMITTED"
        )
    )

    @Test
    fun cashExpenseFiltering_isolatesOnlyCashTransactions() {
        val cashOnly = sampleExpenses.filter { it.paymentAccount.equals("CASH", ignoreCase = true) }

        assertEquals(2, cashOnly.size)
        assertTrue(cashOnly.any { it.title == "ATM Cash Withdrawal" })
        assertTrue(cashOnly.any { it.title == "Coffee & Bakery" })
        assertTrue(cashOnly.none { it.paymentAccount != "CASH" })
    }

    @Test
    fun multiChannelAccountOutflow_correctlyCalculatesTotals() {
        val cardPrimaryTotal = sampleExpenses.filter { it.paymentAccount == "CARD_PRIMARY" }.sumOf { it.amountLkr }
        val secondaryAccountTotal = sampleExpenses.filter { it.paymentAccount == "ACCOUNT_SECONDARY" }.sumOf { it.amountLkr }
        val cashTotal = sampleExpenses.filter { it.paymentAccount == "CASH" }.sumOf { it.amountLkr }
        val totalOutflow = sampleExpenses.sumOf { it.amountLkr }

        assertEquals(11350.0, cardPrimaryTotal, 0.001) // 2850 + 8500
        assertEquals(34000.0, secondaryAccountTotal, 0.001)
        assertEquals(16450.0, cashTotal, 0.001) // 15000 + 1450
        assertEquals(61800.0, totalOutflow, 0.001)
    }

    @Test
    fun unifiedTransaction_retainsPaymentAccountAndFiltersCashCorrectly() {
        val unifiedList = sampleExpenses.map { exp ->
            UnifiedTransaction(
                id = exp.id,
                title = exp.title,
                amount = exp.amountLkr,
                isIncome = false,
                timestampMillis = exp.dateSpentMillis,
                categoryOrChannel = exp.category,
                accountOrClient = when (exp.paymentAccount.uppercase()) {
                    "CARD_PRIMARY" -> "Card Primary"
                    "ACCOUNT_SECONDARY" -> "Secondary Account"
                    "CASH" -> "Cash"
                    else -> exp.paymentAccount
                },
                paymentAccount = exp.paymentAccount.uppercase()
            )
        }

        val cashTransactions = unifiedList.filter { !it.isIncome && it.paymentAccount == "CASH" }
        assertEquals(2, cashTransactions.size)
        assertEquals("Cash", cashTransactions[0].accountOrClient)
        assertEquals("CASH", cashTransactions[0].paymentAccount)
    }
}
