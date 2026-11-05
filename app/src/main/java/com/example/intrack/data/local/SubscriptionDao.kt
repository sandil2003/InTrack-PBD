package com.example.intrack.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.intrack.data.model.SubscriptionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SubscriptionDao {
    @Query("SELECT * FROM subscriptions WHERE userId = :userId")
    fun getSubscriptions(userId: String): Flow<List<SubscriptionEntity>>

    @Query("SELECT * FROM subscriptions WHERE userId = :userId")
    suspend fun getSubscriptionsForUser(userId: String): List<SubscriptionEntity>

    @Query("SELECT * FROM subscriptions")
    suspend fun getAllSubscriptions(): List<SubscriptionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(subscription: SubscriptionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(subscriptions: List<SubscriptionEntity>)

    @Query("DELETE FROM subscriptions WHERE id = :subscriptionId")
    suspend fun deleteById(subscriptionId: String)

    @Query("DELETE FROM subscriptions WHERE userId = :userId")
    suspend fun deleteAllForUser(userId: String)

    @Transaction
    suspend fun replaceAllForUser(userId: String, subscriptions: List<SubscriptionEntity>) {
        deleteAllForUser(userId)
        upsertAll(subscriptions)
    }
}
