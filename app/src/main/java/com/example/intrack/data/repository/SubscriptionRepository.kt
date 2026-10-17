package com.example.intrack.data.repository

import com.example.intrack.data.model.SubscriptionEntity
import com.example.intrack.data.remote.FirestoreService
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

interface SubscriptionRepository {
    fun getSubscriptions(userId: String): Flow<List<SubscriptionEntity>>
    suspend fun addSubscription(subscription: SubscriptionEntity): Result<Unit>
    suspend fun updateSubscriptionStatus(userId: String, subscriptionId: String, newStatus: String): Result<Unit>
}

class SubscriptionRepositoryImpl(
    private val db: FirebaseFirestore = FirestoreService.db
) : SubscriptionRepository {

    override fun getSubscriptions(userId: String): Flow<List<SubscriptionEntity>> = callbackFlow {
        val collection = if (userId.isNotEmpty()) {
            db.collection("users").document(userId).collection("subscriptions")
        } else {
            db.collection("subscriptions")
        }

        val listener = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val list = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(SubscriptionEntity::class.java)?.copy(id = doc.id)
            } ?: emptyList()

            if (list.isEmpty()) {
                trySend(getInitialSampleSubscriptions(userId))
            } else {
                trySend(list)
            }
        }
        awaitClose { listener.remove() }
    }

    override suspend fun addSubscription(subscription: SubscriptionEntity): Result<Unit> {
        return try {
            val collection = if (subscription.userId.isNotEmpty()) {
                db.collection("users").document(subscription.userId).collection("subscriptions")
            } else {
                db.collection("subscriptions")
            }
            collection.add(subscription).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateSubscriptionStatus(
        userId: String,
        subscriptionId: String,
        newStatus: String
    ): Result<Unit> {
        return try {
            val collection = if (userId.isNotEmpty()) {
                db.collection("users").document(userId).collection("subscriptions")
            } else {
                db.collection("subscriptions")
            }
            collection.document(subscriptionId).update("status", newStatus).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Sample subscriptions list
    private fun getInitialSampleSubscriptions(userId: String): List<SubscriptionEntity> {
        return listOf(
            SubscriptionEntity(
                id = "sub_1",
                userId = userId,
                name = "House Rent Baseline",
                amountLkr = 34000.0,
                billingCycle = "MONTHLY",
                paymentAccount = "ACCOUNT_SECONDARY",
                status = "ACTIVE"
            ),
            SubscriptionEntity(
                id = "sub_2",
                userId = userId,
                name = "Gym Membership (Secondary Auto-Debit)",
                amountLkr = 4500.0,
                billingCycle = "MONTHLY",
                paymentAccount = "ACCOUNT_SECONDARY",
                status = "ACTIVE"
            ),
            SubscriptionEntity(
                id = "sub_3",
                userId = userId,
                name = "Cloud SaaS Streaming Plan",
                amountLkr = 2200.0,
                billingCycle = "MONTHLY",
                paymentAccount = "CARD_PRIMARY",
                status = "UNUSED_FLAGGED",
                isFlaggedForReview = true
            )
        )
    }
}
