package com.example.intrack.data.model

data class IncomeEntity(
    val id: String = "",
    val userId: String = "",
    val channel: String = "SALARY", // SALARY, FREELANCE, ADSENSE, CRYPTO
    val title: String = "",
    val originalAmount: Double = 0.0,
    val originalCurrency: String = "LKR", // LKR, USD
    val exchangeRate: Double = 1.0,
    val realizedAmountLkr: Double = 0.0, // originalAmount * exchangeRate
    val dateReceivedMillis: Long = System.currentTimeMillis(),
    val status: String = "PAID", // PAID, INVOICED, OVERDUE, DRAFT
    val clientName: String? = null,
    val invoiceSentMillis: Long? = null,
    val allocatedToGoalLkr: Double = 0.0
)
