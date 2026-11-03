package com.example.intrack.data.repository

import com.example.intrack.data.local.SubscriptionDao
import com.example.intrack.data.model.SubscriptionEntity
import com.example.intrack.data.remote.FirestoreService
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

interface SubscriptionRepository {
    fun getSubscriptions(userId: String): Flow<List<SubscriptionEntity>>
    suspend fun addSubscription(subscription: SubscriptionEntity): Result<Unit>
    suspend fun updateSubscription(subscription: SubscriptionEntity): Result<Unit>
    suspend fun deleteSubscription(userId: String, subscriptionId: String): Result<Unit>
    suspend fun updateSubscriptionStatus(userId: String, subscriptionId: String, newStatus: String): Result<Unit>
    suspend fun updateSubscriptionAutoPayment(userId: String, subscriptionId: String, isAutoPayment: Boolean): Result<Unit>
}

@Singleton
class SubscriptionRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore,
    private val subscriptionDao: SubscriptionDao?
) : SubscriptionRepository {

    constructor(db: FirebaseFirestore = FirestoreService.db) : this(db, null)

    override fun getSubscriptions(userId: String): Flow<List<SubscriptionEntity>> = callbackFlow {
        val cacheJob = launch {
            subscriptionDao?.getSubscriptions(userId)?.collect { cached ->
                if (cached.isNotEmpty()) {
                    trySend(cached)
                }
            }
        }

        val collection = if (userId.isNotEmpty()) {
            db.collection("users").document(userId).collection("subscriptions")
        } else {
            db.collection("subscriptions")
        }

        val listener = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                if (subscriptionDao == null) {
                    close(error)
                }
                return@addSnapshotListener
            }
            val list = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(SubscriptionEntity::class.java)?.copy(id = doc.id, userId = userId.ifEmpty { doc.getString("userId") ?: "" })
            } ?: emptyList()

            if (list.isEmpty()) {
                val sampleList = getInitialSampleSubscriptions(userId)
                trySend(sampleList)
                launch {
                    subscriptionDao?.replaceAllForUser(userId, sampleList)
                }
            } else {
                trySend(list)
                launch {
                    subscriptionDao?.replaceAllForUser(userId, list)
                }
            }
        }
        awaitClose {
            cacheJob.cancel()
            listener.remove()
        }
    }

    override suspend fun addSubscription(subscription: SubscriptionEntity): Result<Unit> {
        return try {
            val collection = if (subscription.userId.isNotEmpty()) {
                db.collection("users").document(subscription.userId).collection("subscriptions")
            } else {
                db.collection("subscriptions")
            }
            val docRef = if (subscription.id.isNotBlank()) collection.document(subscription.id) else collection.document()
            val savedSub = subscription.copy(id = docRef.id)
            subscriptionDao?.upsert(savedSub)
            docRef.set(savedSub).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateSubscription(subscription: SubscriptionEntity): Result<Unit> {
        return try {
            subscriptionDao?.upsert(subscription)
            val collection = if (subscription.userId.isNotEmpty()) {
                db.collection("users").document(subscription.userId).collection("subscriptions")
            } else {
                db.collection("subscriptions")
            }
            collection.document(subscription.id).set(subscription).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteSubscription(userId: String, subscriptionId: String): Result<Unit> {
        return try {
            subscriptionDao?.deleteById(subscriptionId)
            val collection = if (userId.isNotEmpty()) {
                db.collection("users").document(userId).collection("subscriptions")
            } else {
                db.collection("subscriptions")
            }
            collection.document(subscriptionId).delete().await()
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

    override suspend fun updateSubscriptionAutoPayment(
        userId: String,
        subscriptionId: String,
        isAutoPayment: Boolean
    ): Result<Unit> {
        return try {
            val collection = if (userId.isNotEmpty()) {
                db.collection("users").document(userId).collection("subscriptions")
            } else {
                db.collection("subscriptions")
            }
            collection.document(subscriptionId).update("isAutoPayment", isAutoPayment).await()
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
                status = "ACTIVE",
                isAutoPayment = false
            ),
            SubscriptionEntity(
                id = "sub_2",
                userId = userId,
                name = "Gym Membership (Secondary Auto-Debit)",
                amountLkr = 4500.0,
                billingCycle = "MONTHLY",
                paymentAccount = "ACCOUNT_SECONDARY",
                status = "ACTIVE",
                isAutoPayment = true
            ),
            SubscriptionEntity(
                id = "sub_3",
                userId = userId,
                name = "Cloud SaaS Streaming Plan",
                amountLkr = 2200.0,
                billingCycle = "MONTHLY",
                paymentAccount = "CARD_PRIMARY",
                status = "UNUSED_FLAGGED",
                isFlaggedForReview = true,
                isAutoPayment = true
            )
        )
    }
}
