package com.example.intrack.data.repository

import android.util.Log
import com.example.intrack.data.model.GoalContributionEntity
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
    fun getGoalContributions(userId: String, goalId: String): Flow<List<GoalContributionEntity>>
    suspend fun addGoal(goal: GoalEntity): Result<Unit>
    suspend fun updateGoal(goal: GoalEntity): Result<Unit>
    suspend fun deleteGoal(userId: String, goalId: String): Result<Unit>
    suspend fun allocateWindfallToGoal(
        userId: String,
        amountLkr: Double,
        goalId: String = "macbook_m4_goal",
        sourceTitle: String = "Windfall Allocation",
        sourceId: String = "manual",
        note: String? = null
    ): Result<Unit>
    suspend fun addGoalContribution(
        userId: String,
        contribution: GoalContributionEntity
    ): Result<Unit>
    fun getDefaultSampleContributions(userId: String, goalId: String): List<GoalContributionEntity>
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
                Log.w("GoalRepository", "Error getting goals from Firestore: ${error.message}")
                trySend(getDefaultSampleGoals(userId))
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
                } catch (e: Exception) {
                    Log.w("GoalRepository", "Failed to seed sample goals: ${e.message}")
                }
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
                Log.w("GoalRepository", "Error getting MacBook goal: ${error.message}")
                trySend(getDefaultSampleGoals(userId).first())
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

    override fun getGoalContributions(userId: String, goalId: String): Flow<List<GoalContributionEntity>> = callbackFlow {
        val goalDocRef = if (userId.isNotEmpty()) {
            db.collection("users").document(userId).collection("goals").document(goalId)
        } else {
            db.collection("goals").document(goalId)
        }
        val subcollection = goalDocRef.collection("contributions")

        var isSeeding = false

        // 1. Listen to parent goal document for inline contributions
        val goalListener = goalDocRef.addSnapshotListener { snapshot, _ ->
            val goal = snapshot?.toObject(GoalEntity::class.java)
            if (goal != null && goal.contributions.isNotEmpty()) {
                trySend(goal.contributions.sortedByDescending { it.timestampMillis })
            }
        }

        // 2. Also listen to subcollection if permissions allow
        val subcollectionListener = subcollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w("GoalRepository", "Contributions subcollection not accessible: ${error.message}")
                // Do NOT call close(error). Provide fallback sample contributions
                val sampleList = getDefaultSampleContributions(userId, goalId)
                trySend(sampleList)
                return@addSnapshotListener
            }
            val list = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(GoalContributionEntity::class.java)?.copy(id = doc.id)
            }?.sortedByDescending { it.timestampMillis } ?: emptyList()

            if (list.isEmpty() && !isSeeding) {
                isSeeding = true
                val sampleList = getDefaultSampleContributions(userId, goalId)
                trySend(sampleList)
                if (sampleList.isNotEmpty()) {
                    try {
                        val batch = db.batch()
                        sampleList.forEach { sampleItem ->
                            val docRef = subcollection.document(sampleItem.id)
                            batch.set(docRef, sampleItem)
                        }
                        batch.commit()
                    } catch (_: Exception) {}
                }
            } else if (list.isNotEmpty()) {
                trySend(list)
            }
        }

        awaitClose {
            goalListener.remove()
            subcollectionListener.remove()
        }
    }

    override suspend fun addGoalContribution(
        userId: String,
        contribution: GoalContributionEntity
    ): Result<Unit> {
        return try {
            val goalCollection = if (userId.isNotEmpty()) {
                db.collection("users").document(userId).collection("goals")
            } else {
                db.collection("goals")
            }
            val goalDocRef = goalCollection.document(contribution.goalId)
            val contribCollection = goalDocRef.collection("contributions")
            val contribDocRef = if (contribution.id.isNotBlank()) {
                contribCollection.document(contribution.id)
            } else {
                contribCollection.document()
            }
            val finalContribution = contribution.copy(id = contribDocRef.id)

            db.runTransaction { transaction ->
                val snapshot = transaction.get(goalDocRef)
                val currentGoal = snapshot.toObject(GoalEntity::class.java)
                    ?: getDefaultSampleGoals(userId).find { it.id == contribution.goalId }
                    ?: GoalEntity(id = contribution.goalId, userId = userId)

                val newSaved = currentGoal.currentSavedLkr + contribution.amountLkr
                val existingContributions = currentGoal.contributions.filter { it.id != finalContribution.id }
                val updatedContributions = listOf(finalContribution) + existingContributions
                val updatedGoal = currentGoal.copy(
                    currentSavedLkr = newSaved,
                    isCompleted = newSaved >= currentGoal.targetAmountLkr,
                    contributions = updatedContributions
                )
                transaction.set(goalDocRef, updatedGoal)
            }.await()

            // Also try saving to subcollection if Firestore rules permit it, but ignore failure if not permitted
            try {
                contribDocRef.set(finalContribution).await()
            } catch (e: Exception) {
                Log.w("GoalRepository", "Optional subcollection save skipped: ${e.message}")
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("GoalRepository", "Error adding goal contribution: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun allocateWindfallToGoal(
        userId: String,
        amountLkr: Double,
        goalId: String,
        sourceTitle: String,
        sourceId: String,
        note: String?
    ): Result<Unit> {
        val contribution = GoalContributionEntity(
            id = java.util.UUID.randomUUID().toString(),
            goalId = goalId,
            userId = userId,
            amountLkr = amountLkr,
            sourceTitle = sourceTitle,
            sourceId = sourceId,
            timestampMillis = System.currentTimeMillis(),
            note = note
        )
        return addGoalContribution(userId, contribution)
    }

    override fun getDefaultSampleContributions(userId: String, goalId: String): List<GoalContributionEntity> {
        val now = System.currentTimeMillis()
        return when (goalId) {
            "macbook_m4_goal" -> listOf(
                GoalContributionEntity(
                    id = "contrib_mac_1",
                    goalId = goalId,
                    userId = userId,
                    amountLkr = 10000.0,
                    sourceTitle = "Initial Deposit (Card Primary)",
                    sourceId = "CARD_PRIMARY",
                    timestampMillis = now - (14L * 24 * 60 * 60 * 1000),
                    note = "Initial seed deposit"
                ),
                GoalContributionEntity(
                    id = "contrib_mac_2",
                    goalId = goalId,
                    userId = userId,
                    amountLkr = 1200.0,
                    sourceTitle = "React SME Milestone Sweep",
                    sourceId = "freelance_milestone",
                    timestampMillis = now - (5L * 24 * 60 * 60 * 1000),
                    note = "Direct allocation from freelance milestone"
                )
            )
            "emergency_fund" -> listOf(
                GoalContributionEntity(
                    id = "contrib_emg_1",
                    goalId = goalId,
                    userId = userId,
                    amountLkr = 50000.0,
                    sourceTitle = "Emergency Seed Deposit",
                    sourceId = "ACCOUNT_SECONDARY",
                    timestampMillis = now - (30L * 24 * 60 * 60 * 1000)
                ),
                GoalContributionEntity(
                    id = "contrib_emg_2",
                    goalId = goalId,
                    userId = userId,
                    amountLkr = 35000.0,
                    sourceTitle = "Monthly Salary Sweep",
                    sourceId = "monthly_salary",
                    timestampMillis = now - (10L * 24 * 60 * 60 * 1000)
                )
            )
            "trip_fund" -> listOf(
                GoalContributionEntity(
                    id = "contrib_trip_1",
                    goalId = goalId,
                    userId = userId,
                    amountLkr = 20000.0,
                    sourceTitle = "Trip Fund Initial",
                    sourceId = "CARD_PRIMARY",
                    timestampMillis = now - (20L * 24 * 60 * 60 * 1000)
                ),
                GoalContributionEntity(
                    id = "contrib_trip_2",
                    goalId = goalId,
                    userId = userId,
                    amountLkr = 22000.0,
                    sourceTitle = "AdSense USD Transfer Allocation",
                    sourceId = "adsense_usd",
                    timestampMillis = now - (8L * 24 * 60 * 60 * 1000)
                )
            )
            else -> listOf(
                GoalContributionEntity(
                    id = "contrib_def_1",
                    goalId = goalId,
                    userId = userId,
                    amountLkr = 5000.0,
                    sourceTitle = "Initial Deposit",
                    sourceId = "manual",
                    timestampMillis = now - (7L * 24 * 60 * 60 * 1000),
                    note = "Goal allocation"
                )
            )
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
                createdAtMillis = now - (14L * 24 * 60 * 60 * 1000),
                contributions = getDefaultSampleContributions(userId, "macbook_m4_goal")
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
                createdAtMillis = now - (30L * 24 * 60 * 60 * 1000),
                contributions = getDefaultSampleContributions(userId, "emergency_fund")
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
                createdAtMillis = now - (20L * 24 * 60 * 60 * 1000),
                contributions = getDefaultSampleContributions(userId, "trip_fund")
            )
        )
    }
}
