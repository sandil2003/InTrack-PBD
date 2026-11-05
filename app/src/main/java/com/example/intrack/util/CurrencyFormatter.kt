package com.example.intrack.util

import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {
    private const val DEFAULT_USD_TO_LKR_RATE = 305.0

    var activeBaseCurrency: String = "LKR (Rs) • Sri Lankan Rupee"

    fun getLkrExchangeRate(baseCurrencyString: String? = activeBaseCurrency): Double {
        if (baseCurrencyString.isNullOrBlank()) return 1.0
        return when {
            baseCurrencyString.contains("USD", ignoreCase = true) -> 305.0
            baseCurrencyString.contains("EUR", ignoreCase = true) -> 330.0
            baseCurrencyString.contains("GBP", ignoreCase = true) -> 390.0
            baseCurrencyString.contains("INR", ignoreCase = true) -> 3.65
            baseCurrencyString.contains("AED", ignoreCase = true) -> 83.0
            baseCurrencyString.contains("AUD", ignoreCase = true) -> 200.0
            baseCurrencyString.contains("CAD", ignoreCase = true) -> 220.0
            baseCurrencyString.contains("SGD", ignoreCase = true) -> 230.0
            baseCurrencyString.contains("JPY", ignoreCase = true) -> 2.05
            else -> 1.0
        }
    }

    fun getCurrencyCode(baseCurrencyString: String? = activeBaseCurrency): String {
        if (baseCurrencyString.isNullOrBlank()) return "LKR"
        val code = baseCurrencyString.substringBefore(" ").substringBefore("(").trim().uppercase()
        return if (code.isNotBlank()) code else "LKR"
    }

    fun getCurrencySymbol(baseCurrencyString: String? = activeBaseCurrency): String {
        if (baseCurrencyString.isNullOrBlank()) return "LKR"
        return when {
            baseCurrencyString.contains("USD", ignoreCase = true) -> "$"
            baseCurrencyString.contains("EUR", ignoreCase = true) -> "€"
            baseCurrencyString.contains("GBP", ignoreCase = true) -> "£"
            baseCurrencyString.contains("INR", ignoreCase = true) -> "₹"
            baseCurrencyString.contains("JPY", ignoreCase = true) -> "¥"
            baseCurrencyString.contains("AED", ignoreCase = true) -> "AED"
            baseCurrencyString.contains("AUD", ignoreCase = true) -> "A$"
            baseCurrencyString.contains("CAD", ignoreCase = true) -> "C$"
            baseCurrencyString.contains("SGD", ignoreCase = true) -> "S$"
            else -> "LKR"
        }
    }

    fun convertFromLkr(amountLkr: Double, baseCurrencyString: String? = activeBaseCurrency): Double {
        val rate = getLkrExchangeRate(baseCurrencyString)
        return if (rate > 0) amountLkr / rate else amountLkr
    }

    fun formatLkr(amount: Double): String {
        return formatCurrency(amount, activeBaseCurrency)
    }

    fun formatCurrency(amountLkr: Double, baseCurrencyString: String? = activeBaseCurrency): String {
        val convertedAmount = convertFromLkr(amountLkr, baseCurrencyString)
        val symbol = getCurrencySymbol(baseCurrencyString)
        val formatter = NumberFormat.getNumberInstance(Locale.US).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
        return if (symbol == "LKR" || symbol.length > 2) "$symbol ${formatter.format(convertedAmount)}" else "$symbol${formatter.format(convertedAmount)}"
    }

    fun formatLkrWhole(amountLkr: Double): String {
        val convertedAmount = convertFromLkr(amountLkr, activeBaseCurrency)
        val symbol = getCurrencySymbol(activeBaseCurrency)
        val formatter = NumberFormat.getNumberInstance(Locale.US).apply {
            maximumFractionDigits = 0
        }
        return if (symbol == "LKR" || symbol.length > 2) "$symbol ${formatter.format(convertedAmount)}" else "$symbol${formatter.format(convertedAmount)}"
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
