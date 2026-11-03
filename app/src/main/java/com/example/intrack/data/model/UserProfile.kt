package com.example.intrack.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class UserProfile(
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val phoneNumber: String = "",
    val bio: String = "",
    val baseCurrency: String = "LKR (Rs) • Sri Lankan Rupee",
    val billingCycle: String = "25th of Every Month",
    val weeklyStartDay: String = "Monday",
    val createdAtMillis: Long = System.currentTimeMillis(),
    val updatedAtMillis: Long = System.currentTimeMillis()
) {
    val firstName: String
        get() = displayName.trim().split(" ").firstOrNull { it.isNotBlank() }
            ?: email.substringBefore("@").replaceFirstChar { it.uppercase() }
                .ifBlank { "User" }

    val initials: String
        get() {
            val parts = displayName.trim().split(" ").filter { it.isNotBlank() }
            return when {
                parts.size >= 2 -> "${parts[0].first().uppercaseChar()}${parts[1].first().uppercaseChar()}"
                parts.size == 1 -> parts[0].take(2).uppercase()
                email.isNotBlank() -> email.take(2).uppercase()
                else -> "U"
            }
        }

    val memberSinceFormatted: String
        get() = try {
            val sdf = SimpleDateFormat("MMM yyyy", Locale.getDefault())
            sdf.format(Date(if (createdAtMillis > 0) createdAtMillis else System.currentTimeMillis()))
        } catch (_: Exception) {
            "Recently"
        }
}
