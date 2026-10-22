package com.example.intrack.data.repository

import com.example.intrack.data.model.GoalEntity
import com.example.intrack.data.remote.FirestoreService
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

interface GoalRepository {
    fun getGoals(userId: String): Flow<List<GoalEntity>>
    fun getMacBookGoal(userId: String): Flow<GoalEntity>
    suspend fun addGoal(goal: GoalEntity): Result<Unit>
    suspend fun updateGoal(goal: GoalEntity): Result<Unit>
    suspend fun deleteGoal(userId: String, goalId: String): Result<Unit>
    suspend fun allocateWindfallToGoal(userId: String, amountLkr: Double, goalId: String = "macbook_m4_goal"): Result<Unit>
}

class GoalRepositoryImpl(
    private val db: FirebaseFirestore = FirestoreService.db
) : GoalRepository {

    override fun getGoals(userId: String): Flow<List<GoalEntity>> = callbackFlow {
        val collection = if (userId.isNotEmpty()) {
            db.collection("users").document(userId).collection("goals")
        } else {
            db.collection("goals")
        }

        var isSeeding = false

        val listener = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val list = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(GoalEntity::class.java)?.copy(id = doc.id)
            } ?: emptyList()

            if (list.isEmpty() && !isSeeding) {
                isSeeding = true
                val sampleList = getDefaultSampleGoals(userId)
                trySend(sampleList)
                try {
                    val batch = db.batch()
                    sampleList.forEach { sampleItem ->
                        val docRef = collection.document(sampleItem.id)
                        batch.set(docRef, sampleItem)
                    }
                    batch.commit()
                } catch (_: Exception) {}
            } else {
                trySend(list)
            }
        }
        awaitClose { listener.remove() }
    }

    override fun getMacBookGoal(userId: String): Flow<GoalEntity> = callbackFlow {
        val collection = if (userId.isNotEmpty()) {
            db.collection("users").document(userId).collection("goals")
        } else {
            db.collection("goals")
        }

        val listener = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val goals = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(GoalEntity::class.java)?.copy(id = doc.id)
            } ?: emptyList()

            val targetGoal = goals.find { it.id == "macbook_m4_goal" }
                ?: goals.firstOrNull()
                ?: getDefaultSampleGoals(userId).first()

            trySend(targetGoal)
        }
        awaitClose { listener.remove() }
    }

    override suspend fun addGoal(goal: GoalEntity): Result<Unit> {
        return try {
            val collection = if (goal.userId.isNotEmpty()) {
                db.collection("users").document(goal.userId).collection("goals")
            } else {
                db.collection("goals")
            }
            val docRef = if (goal.id.isNotBlank()) collection.document(goal.id) else collection.document()
            docRef.set(goal.copy(id = docRef.id)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateGoal(goal: GoalEntity): Result<Unit> {
        return try {
            val collection = if (goal.userId.isNotEmpty()) {
                db.collection("users").document(goal.userId).collection("goals")
            } else {
                db.collection("goals")
            }
            collection.document(goal.id).set(goal).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteGoal(userId: String, goalId: String): Result<Unit> {
        return try {
            val collection = if (userId.isNotEmpty()) {
                db.collection("users").document(userId).collection("goals")
            } else {
                db.collection("goals")
            }
            collection.document(goalId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun allocateWindfallToGoal(
        userId: String,
        amountLkr: Double,
        goalId: String
    ): Result<Unit> {
        return try {
            val collection = if (userId.isNotEmpty()) {
                db.collection("users").document(userId).collection("goals")
            } else {
                db.collection("goals")
            }
            val docRef = collection.document(goalId)
            db.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                val currentGoal = snapshot.toObject(GoalEntity::class.java)
                    ?: getDefaultSampleGoals(userId).find { it.id == goalId }
                    ?: GoalEntity(id = goalId, userId = userId)

                val newSaved = currentGoal.currentSavedLkr + amountLkr
                val updatedGoal = currentGoal.copy(
                    currentSavedLkr = newSaved,
                    isCompleted = newSaved >= currentGoal.targetAmountLkr
                )
                transaction.set(docRef, updatedGoal)
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getDefaultSampleGoals(userId: String): List<GoalEntity> {
        val now = System.currentTimeMillis()
        return listOf(
            GoalEntity(
                id = "macbook_m4_goal",
                userId = userId,
                title = "MacBook Pro M4 Fund",
                targetAmountLkr = 490000.0,
                currentSavedLkr = 11200.0,
                targetDateMillis = now + (365L * 24 * 60 * 60 * 1000),
                monthlyTargetRunRateLkr = 40833.33,
                category = "TECH",
                initialDepositLkr = 10000.0,
                primaryFundingSource = "CARD_PRIMARY",
                remindMonthlyPace = true,
                createdAtMillis = now - (14L * 24 * 60 * 60 * 1000)
            ),
            GoalEntity(
                id = "emergency_fund",
                userId = userId,
                title = "6-Month Emergency Fund",
                targetAmountLkr = 250000.0,
                currentSavedLkr = 85000.0,
                targetDateMillis = now + (180L * 24 * 60 * 60 * 1000),
                monthlyTargetRunRateLkr = 25000.0,
                category = "EMERGENCY",
                initialDepositLkr = 50000.0,
                primaryFundingSource = "ACCOUNT_SECONDARY",
                remindMonthlyPace = true,
                createdAtMillis = now - (30L * 24 * 60 * 60 * 1000)
            ),
            GoalEntity(
                id = "trip_fund",
                userId = userId,
                title = "Tech Conference & Travel",
                targetAmountLkr = 180000.0,
                currentSavedLkr = 42000.0,
                targetDateMillis = now + (240L * 24 * 60 * 60 * 1000),
                monthlyTargetRunRateLkr = 15000.0,
                category = "TRAVEL",
                initialDepositLkr = 20000.0,
                primaryFundingSource = "CARD_PRIMARY",
                remindMonthlyPace = false,
                createdAtMillis = now - (20L * 24 * 60 * 60 * 1000)
            )
        )
    }
}
