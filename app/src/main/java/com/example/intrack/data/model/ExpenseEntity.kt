package com.example.intrack.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val amountLkr: Double = 0.0,
    val category: String = "DINING_OUT", // Transport, Dining-in, Dining-out, Rent, Subscription, Utilities, Misc, ATM_WITHDRAWAL
    val paymentAccount: String = "CARD_PRIMARY", // CARD_PRIMARY, ACCOUNT_SECONDARY, CASH
    val commitmentType: String = "DISCRETIONARY", // COMMITTED, DISCRETIONARY, TRANSFER
    val dateSpentMillis: Long = System.currentTimeMillis(),
    val isRecurring: Boolean = false,
    val notes: String? = null
) {
    fun isInternalTransfer(): Boolean {
        return category.equals("ATM_WITHDRAWAL", ignoreCase = true) ||
                category.equals("TRANSFER", ignoreCase = true) ||
                commitmentType.equals("TRANSFER", ignoreCase = true) ||
                (title.contains("ATM", ignoreCase = true) && title.contains("Withdrawal", ignoreCase = true))
    }

    fun isCommittedExpense(): Boolean {
        return !isInternalTransfer() && commitmentType.equals("COMMITTED", ignoreCase = true)
    }

    fun isDiscretionaryExpense(): Boolean {
        return !isInternalTransfer() && !isCommittedExpense()
    }
}
