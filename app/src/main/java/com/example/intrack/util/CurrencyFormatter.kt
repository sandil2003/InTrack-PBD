package com.example.intrack.util

import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {
    private const val DEFAULT_USD_TO_LKR_RATE = 305.0

    fun formatLkr(amount: Double): String {
        val formatter = NumberFormat.getNumberInstance(Locale.US).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
        return "LKR ${formatter.format(amount)}"
    }

    fun formatUsd(amount: Double): String {
        val formatter = NumberFormat.getNumberInstance(Locale.US).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
        return "$${formatter.format(amount)}"
    }

    fun convertUsdToLkr(usdAmount: Double, exchangeRate: Double = DEFAULT_USD_TO_LKR_RATE): Double {
        return usdAmount * exchangeRate
    }
}
