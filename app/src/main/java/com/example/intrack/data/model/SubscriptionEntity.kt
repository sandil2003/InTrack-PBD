package com.example.intrack.data.model

import com.google.firebase.firestore.PropertyName

data class SubscriptionEntity(
    val id: String = "",
    val userId: String = "",
    val name: String = "", // e.g. Rent, Gym Membership, SaaS Cloud Plan
    val amountLkr: Double = 0.0,
    val billingCycle: String = "MONTHLY", // Monthly, Annual
    val paymentAccount: String = "ACCOUNT_SECONDARY", // Card primary, Account secondary, Cash
    val status: String = "ACTIVE", // Active, Unused flags, cancelled
    val nextBillingMillis: Long = System.currentTimeMillis(),
    @get:PropertyName("isFlaggedForReview")
    val isFlaggedForReview: Boolean = false,
    @get:PropertyName("isAutoPayment")
    val isAutoPayment: Boolean = false
)

