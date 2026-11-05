package com.example.intrack.notification.sms

import java.util.Locale
import java.util.regex.Pattern

object TransactionParser {

    private val OTP_PATTERN = Pattern.compile(
        "\\b(otp|verification code|one time password|security code|auth code|password is)\\b",
        Pattern.CASE_INSENSITIVE
    )

    private val CREDIT_PATTERN = Pattern.compile(
        "\\b(credited(?: with| to)?|credit of|received lkr|refund of|salary(?: deposit| credited)?|deposited|deposit of)\\b",
        Pattern.CASE_INSENSITIVE
    )

    // Regex to match currency and amounts: e.g., "LKR 850", "LKR 850.00", "LKR 1,250.50", "Rs. 850"
    private val AMOUNT_PATTERN = Pattern.compile(
        "(?:lkr|rs\\.?|රු\\.?)\\s*([0-9]{1,3}(?:,[0-9]{3})*(?:\\.[0-9]{1,2})?|[0-9]+(?:\\.[0-9]{1,2})?)",
        Pattern.CASE_INSENSITIVE
    )

    // Secondary amount pattern: e.g. "for 850.00 LKR" or "fare: 850.00"
    private val AMOUNT_FALLBACK_PATTERN = Pattern.compile(
        "(?:fare(?: is)?|total(?: fare)?|paid|debited|amount|txn of|spent)\\s*(?:is|of|:)?\\s*(?:lkr|rs\\.?)?\\s*([0-9]{1,3}(?:,[0-9]{3})*(?:\\.[0-9]{1,2})?|[0-9]+(?:\\.[0-9]{1,2})?)",
        Pattern.CASE_INSENSITIVE
    )

    /**
     * Parses an incoming SMS or notification message from supported banks and ride-hailing services.
     *
     * @param sender The SMS sender address (e.g. "COMBANK", "SAMPATH", "HNB", "PickMe", "Uber")
     *               or the posting package name (e.g. "com.pickme.passenger", "com.ubercab").
     * @param messageBody The textual content of the notification or SMS.
     * @return [ParsedTransaction] if successfully detected as an expense, or null if ignored.
     */
    fun parse(sender: String?, messageBody: String?): ParsedTransaction? {
        if (messageBody.isNullOrBlank()) return null
        val lowerBody = messageBody.lowercase(Locale.ROOT)
        val lowerSender = (sender ?: "").lowercase(Locale.ROOT)

        // Ignore security OTPs and incoming credits/refunds
        if (OTP_PATTERN.matcher(lowerBody).find()) return null
        if (CREDIT_PATTERN.matcher(lowerBody).find() && !lowerBody.contains("debited") && !lowerBody.contains("paid")) {
            return null
        }

        // Determine source identity
        val isComBank = lowerSender.contains("combank") || lowerSender.contains("combk") ||
                lowerSender.contains("commercial bank") || lowerBody.contains("combank") || lowerBody.contains("combk")
        val isSampath = lowerSender.contains("sampath") || lowerBody.contains("sampath")
        val isHnb = lowerSender.contains("hnb") || lowerBody.contains("hnb") || lowerSender.contains("hatton")
        val isPickMe = lowerSender.contains("pickme") || lowerBody.contains("pickme")
        val isUber = lowerSender.contains("uber") || lowerBody.contains("uber")

        // Must be from a recognized financial or ride-hailing source
        if (!isComBank && !isSampath && !isHnb && !isPickMe && !isUber) {
            // Check for generic Sri Lankan debit pattern
            val hasDebitKeywords = lowerBody.contains("debited") || lowerBody.contains("paid lkr") ||
                    lowerBody.contains("card ending") || lowerBody.contains("txn of")
            if (!hasDebitKeywords) {
                return null
            }
        }

        // Extract amount
        val amount = extractAmount(messageBody) ?: return null
        if (amount <= 0.0) return null

        // Determine merchant, category, commitment, and account
        return classifyTransaction(
            sender = sender ?: "Bank Alert",
            body = messageBody,
            lowerBody = lowerBody,
            lowerSender = lowerSender,
            amount = amount,
            isComBank = isComBank,
            isSampath = isSampath,
            isHnb = isHnb,
            isPickMe = isPickMe,
            isUber = isUber
        )
    }

    private fun extractAmount(text: String): Double? {
        val matcher = AMOUNT_PATTERN.matcher(text)
        if (matcher.find()) {
            val amountStr = matcher.group(1)?.replace(",", "")
            val parsed = amountStr?.toDoubleOrNull()
            if (parsed != null && parsed > 0.0) return parsed
        }

        val fallbackMatcher = AMOUNT_FALLBACK_PATTERN.matcher(text)
        if (fallbackMatcher.find()) {
            val amountStr = fallbackMatcher.group(1)?.replace(",", "")
            val parsed = amountStr?.toDoubleOrNull()
            if (parsed != null && parsed > 0.0) return parsed
        }

        return null
    }

    private fun classifyTransaction(
        sender: String,
        body: String,
        lowerBody: String,
        lowerSender: String,
        amount: Double,
        isComBank: Boolean,
        isSampath: Boolean,
        isHnb: Boolean,
        isPickMe: Boolean,
        isUber: Boolean
    ): ParsedTransaction {
        val now = System.currentTimeMillis()

        // 1. PickMe Food vs PickMe Ride
        if (isPickMe || lowerBody.contains("pickme")) {
            val isFood = lowerBody.contains("food") || lowerBody.contains("restaurant") ||
                    lowerBody.contains("burger") || lowerBody.contains("meal") || lowerBody.contains("order")
            return if (isFood) {
                ParsedTransaction(
                    rawText = body,
                    senderOrSource = sender,
                    merchantName = "PickMe Food",
                    amountLkr = amount,
                    category = "DINING_OUT",
                    commitmentType = "DISCRETIONARY",
                    paymentAccount = "CARD_PRIMARY",
                    typeLabel = "food delivery",
                    timestampMillis = now
                )
            } else {
                ParsedTransaction(
                    rawText = body,
                    senderOrSource = sender,
                    merchantName = "PickMe",
                    amountLkr = amount,
                    category = "TRANSPORT",
                    commitmentType = "DISCRETIONARY",
                    paymentAccount = "CARD_PRIMARY",
                    typeLabel = "ride",
                    timestampMillis = now
                )
            }
        }

        // 2. Uber Eats vs Uber Ride
        if (isUber || lowerBody.contains("uber")) {
            val isEats = lowerBody.contains("eats") || lowerBody.contains("food") || lowerBody.contains("order")
            return if (isEats) {
                ParsedTransaction(
                    rawText = body,
                    senderOrSource = sender,
                    merchantName = "Uber Eats",
                    amountLkr = amount,
                    category = "DINING_OUT",
                    commitmentType = "DISCRETIONARY",
                    paymentAccount = "CARD_PRIMARY",
                    typeLabel = "food delivery",
                    timestampMillis = now
                )
            } else {
                ParsedTransaction(
                    rawText = body,
                    senderOrSource = sender,
                    merchantName = "Uber",
                    amountLkr = amount,
                    category = "TRANSPORT",
                    commitmentType = "DISCRETIONARY",
                    paymentAccount = "CARD_PRIMARY",
                    typeLabel = "ride",
                    timestampMillis = now
                )
            }
        }

        // 3. Bank Alert (Commercial Bank, Sampath Bank, HNB)
        val bankName = when {
            isComBank -> "Commercial Bank"
            isSampath -> "Sampath Bank"
            isHnb -> "HNB"
            else -> "Bank"
        }

        // Check for specific Sri Lankan merchants embedded in bank debits
        when {
            lowerBody.contains("pickme") -> {
                return ParsedTransaction(
                    rawText = body,
                    senderOrSource = bankName,
                    merchantName = "PickMe",
                    amountLkr = amount,
                    category = "TRANSPORT",
                    commitmentType = "DISCRETIONARY",
                    paymentAccount = "CARD_PRIMARY",
                    typeLabel = "ride",
                    timestampMillis = now
                )
            }
            lowerBody.contains("ubereats") || lowerBody.contains("uber eats") -> {
                return ParsedTransaction(
                    rawText = body,
                    senderOrSource = bankName,
                    merchantName = "Uber Eats",
                    amountLkr = amount,
                    category = "DINING_OUT",
                    commitmentType = "DISCRETIONARY",
                    paymentAccount = "CARD_PRIMARY",
                    typeLabel = "food delivery",
                    timestampMillis = now
                )
            }
            lowerBody.contains("keells") || lowerBody.contains("cargills") ||
                    lowerBody.contains("glomark") || lowerBody.contains("supermarket") ||
                    lowerBody.contains("spar") || lowerBody.contains("arpico") -> {
                val store = when {
                    lowerBody.contains("keells") -> "Keells Super"
                    lowerBody.contains("cargills") -> "Cargills Food City"
                    lowerBody.contains("glomark") -> "Softlogic GLOMARK"
                    lowerBody.contains("spar") -> "SPAR Supermarket"
                    lowerBody.contains("arpico") -> "Arpico Supercentre"
                    else -> "Groceries"
                }
                return ParsedTransaction(
                    rawText = body,
                    senderOrSource = bankName,
                    merchantName = store,
                    amountLkr = amount,
                    category = "MISC",
                    commitmentType = "COMMITTED",
                    paymentAccount = "CARD_PRIMARY",
                    typeLabel = "groceries",
                    timestampMillis = now
                )
            }
            lowerBody.contains("dialog") || lowerBody.contains("slt") ||
                    lowerBody.contains("mobitel") || lowerBody.contains("ceb") ||
                    lowerBody.contains("water board") -> {
                val utility = when {
                    lowerBody.contains("dialog") -> "Dialog Broadband/Mobile"
                    lowerBody.contains("slt") -> "SLT-Mobitel Fibre"
                    lowerBody.contains("ceb") -> "CEB Electricity"
                    else -> "Utility Bill"
                }
                return ParsedTransaction(
                    rawText = body,
                    senderOrSource = bankName,
                    merchantName = utility,
                    amountLkr = amount,
                    category = "UTILITIES",
                    commitmentType = "COMMITTED",
                    paymentAccount = "CARD_PRIMARY",
                    typeLabel = "utility bill",
                    timestampMillis = now
                )
            }
            lowerBody.contains("atm") || lowerBody.contains("cash withdrawal") -> {
                return ParsedTransaction(
                    rawText = body,
                    senderOrSource = bankName,
                    merchantName = "ATM Cash Withdrawal",
                    amountLkr = amount,
                    category = "ATM_WITHDRAWAL",
                    commitmentType = "TRANSFER",
                    paymentAccount = "CASH",
                    typeLabel = "cash withdrawal",
                    timestampMillis = now
                )
            }
            else -> {
                // Extract merchant name if mentioned after "at " or "to "
                val merchant = extractMerchantAfterPreposition(body) ?: "$bankName Card"
                return ParsedTransaction(
                    rawText = body,
                    senderOrSource = bankName,
                    merchantName = merchant,
                    amountLkr = amount,
                    category = "MISC",
                    commitmentType = "DISCRETIONARY",
                    paymentAccount = "CARD_PRIMARY",
                    typeLabel = "card transaction",
                    timestampMillis = now
                )
            }
        }
    }

    private fun extractMerchantAfterPreposition(text: String): String? {
        val pattern = Pattern.compile(
            "(?:at|to|for)\\s+([A-Za-z0-9&'._\\- ]{3,25})(?:\\s+on|\\s+with|\\s+ref|\\s+bal|\\.|\$)",
            Pattern.CASE_INSENSITIVE
        )
        val matcher = pattern.matcher(text)
        if (matcher.find()) {
            val candidate = matcher.group(1)?.trim()
            if (!candidate.isNullOrBlank() && !candidate.equals("card", ignoreCase = true) &&
                !candidate.equals("a/c", ignoreCase = true) && !candidate.contains("ending", ignoreCase = true)
            ) {
                return candidate
            }
        }
        return null
    }
}
