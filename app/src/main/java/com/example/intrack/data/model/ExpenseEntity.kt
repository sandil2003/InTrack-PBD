package com.example.intrack.data.model

data class ExpenseEntity(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val amountLkr: Double = 0.0,
    val category: String = "DINING_OUT", // Transport, Dining-in, Dining-out, Rent, Subscription, Utilities, Misc
    val paymentAccount: String = "CARD_PRIMARY", // CARD_PRIMARY, ACCOUNT_SECONDARY, CASH
    val commitmentType: String = "DISCRETIONARY", // COMMITTED, DISCRETIONARY
    val dateSpentMillis: Long = System.currentTimeMillis(),
    val isRecurring: Boolean = false,
    val notes: String? = null
)
