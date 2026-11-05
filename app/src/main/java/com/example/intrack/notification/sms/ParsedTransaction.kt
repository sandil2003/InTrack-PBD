package com.example.intrack.notification.sms

/**
 * Structured model representing a financial transaction extracted from
 * an SMS or an app notification (e.g. PickMe, UberEats, Commercial Bank, Sampath, HNB).
 */
data class ParsedTransaction(
    val rawText: String,
    val senderOrSource: String,
    val merchantName: String,
    val amountLkr: Double,
    val category: String, // "TRANSPORT", "DINING_OUT", "UTILITIES", "MISC", "ATM_WITHDRAWAL"
    val commitmentType: String, // "DISCRETIONARY", "COMMITTED", "TRANSFER"
    val paymentAccount: String, // "CARD_PRIMARY", "ACCOUNT_SECONDARY", "CASH"
    val typeLabel: String, // "ride", "food delivery", "groceries", "utility bill", "card payment"
    val timestampMillis: Long = System.currentTimeMillis()
)
