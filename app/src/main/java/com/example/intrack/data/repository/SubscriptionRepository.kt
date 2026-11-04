package com.example.intrack.data.repository

import com.example.intrack.data.local.SubscriptionDao
import com.example.intrack.data.model.SubscriptionEntity
import com.example.intrack.data.remote.FirestoreService
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
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
        if (userId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val cacheJob = launch {
            subscriptionDao?.getSubscriptions(userId)?.collect { cached ->
                trySend(cached)
            }
        }

        val collection = db.collection("users").document(userId).collection("subscriptions")
        val query = collection.orderBy("nextBillingMillis", Query.Direction.ASCENDING)

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                if (subscriptionDao == null) {
                    trySend(emptyList())
                }
                return@addSnapshotListener
            }
            val list = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(SubscriptionEntity::class.java)?.copy(id = doc.id, userId = userId)
            } ?: emptyList()

            if (subscriptionDao != null) {
                launch {
                    subscriptionDao.replaceAllForUser(userId, list)
                }
            } else {
                trySend(list)
            }
        }
        awaitClose {
            cacheJob.cancel()
            listener.remove()
        }
    }

    override suspend fun addSubscription(subscription: SubscriptionEntity): Result<Unit> {
        if (subscription.userId.isBlank()) {
            return Result.failure(IllegalArgumentException("Authenticated userId is required"))
        }
        return try {
            val collection = db.collection("users").document(subscription.userId).collection("subscriptions")
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
        if (subscription.userId.isBlank() || subscription.id.isBlank()) {
            return Result.failure(IllegalArgumentException("Authenticated userId and subscriptionId are required"))
        }
        return try {
            subscriptionDao?.upsert(subscription)
            val collection = db.collection("users").document(subscription.userId).collection("subscriptions")
            collection.document(subscription.id).set(subscription).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteSubscription(userId: String, subscriptionId: String): Result<Unit> {
        if (userId.isBlank() || subscriptionId.isBlank()) {
            return Result.failure(IllegalArgumentException("Authenticated userId and subscriptionId are required"))
        }
        return try {
            subscriptionDao?.deleteById(subscriptionId)
            val collection = db.collection("users").document(userId).collection("subscriptions")
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
        if (userId.isBlank() || subscriptionId.isBlank()) {
            return Result.failure(IllegalArgumentException("Authenticated userId and subscriptionId are required"))
        }
        return try {
            val collection = db.collection("users").document(userId).collection("subscriptions")
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
        if (userId.isBlank() || subscriptionId.isBlank()) {
            return Result.failure(IllegalArgumentException("Authenticated userId and subscriptionId are required"))
        }
        return try {
            val collection = db.collection("users").document(userId).collection("subscriptions")
            collection.document(subscriptionId).update("isAutoPayment", isAutoPayment).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
