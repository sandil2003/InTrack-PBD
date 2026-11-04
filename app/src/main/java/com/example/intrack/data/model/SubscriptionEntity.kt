package com.example.intrack.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.PropertyName

@Entity(tableName = "subscriptions")
data class SubscriptionEntity(
    @PrimaryKey
    val id: String = "",
    val userId: String = "",
    val name: String = "", // e.g. Rent, Gym Membership, SaaS Cloud Plan, Netflix Premium
    val amountLkr: Double = 0.0,
    val billingCycle: String = "MONTHLY", // Monthly, Annual, Weekly, Quarterly
    val paymentAccount: String = "CARD_PRIMARY", // CARD_PRIMARY, ACCOUNT_SECONDARY, CASH
    val category: String = "ENTERTAINMENT", // ENTERTAINMENT, HOUSING, HEALTH, TECH
    val status: String = "ACTIVE", // ACTIVE, UNUSED_FLAGGED, CANCELLED
    val nextBillingMillis: Long = System.currentTimeMillis() + 9L * 24 * 60 * 60 * 1000L,
    val totalPaidLkr: Double = 0.0,
    val billingCyclesCount: Int = 1,
    @get:PropertyName("isFlaggedForReview")
    val isFlaggedForReview: Boolean = false,
    @get:PropertyName("isAutoPayment")
    val isAutoPayment: Boolean = false
)
