package com.example.intrack.data.model

data class UserProfile(
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String = "",
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
}
