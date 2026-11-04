package com.example.intrack.data.repository

import android.util.Log
import com.example.intrack.data.local.GoalDao
import com.example.intrack.data.model.GoalContributionEntity
import com.example.intrack.data.model.GoalEntity
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
}

@Singleton
class GoalRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore,
    private val goalDao: GoalDao?
) : GoalRepository {

    constructor(db: FirebaseFirestore = FirestoreService.db) : this(db, null)

    override fun getGoals(userId: String): Flow<List<GoalEntity>> = callbackFlow {
        if (userId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val cacheJob = launch {
            goalDao?.getGoals(userId)?.collect { cached ->
                if (cached.isNotEmpty()) {
                    trySend(cached)
                }
            }
        }

        val collection = db.collection("users").document(userId).collection("goals")
        val query = collection.orderBy("targetDateMillis", Query.Direction.ASCENDING)

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w("GoalRepository", "Error getting goals from Firestore: ${error.message}")
                if (goalDao == null) {
                    trySend(emptyList())
                }
                return@addSnapshotListener
            }
            val list = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(GoalEntity::class.java)?.copy(id = doc.id, userId = userId)
            } ?: emptyList()

            if (list.isNotEmpty()) {
                trySend(list)
                launch {
                    goalDao?.replaceAllForUser(userId, list)
                }
            } else if (goalDao == null) {
                trySend(emptyList())
            }
        }
        awaitClose {
            cacheJob.cancel()
            listener.remove()
        }
    }

    override fun getMacBookGoal(userId: String): Flow<GoalEntity> = callbackFlow {
        if (userId.isBlank()) {
            trySend(GoalEntity())
            close()
            return@callbackFlow
        }

        val cacheJob = launch {
            goalDao?.getGoals(userId)?.collect { cached ->
                val target = cached.find { it.id == "macbook_m4_goal" } ?: cached.firstOrNull()
                if (target != null) {
                    trySend(target)
                }
            }
        }

        val collection = db.collection("users").document(userId).collection("goals")

        val listener = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w("GoalRepository", "Error getting primary goal: ${error.message}")
                return@addSnapshotListener
            }
            val goals = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(GoalEntity::class.java)?.copy(id = doc.id, userId = userId)
            } ?: emptyList()

            val targetGoal = goals.find { it.id == "macbook_m4_goal" }
                ?: goals.firstOrNull()

            if (targetGoal != null) {
                trySend(targetGoal)
                launch {
                    goalDao?.upsert(targetGoal)
                }
            } else {
                trySend(GoalEntity(id = "", userId = userId, title = "", targetAmountLkr = 0.0, currentSavedLkr = 0.0))
            }
        }
        awaitClose {
            cacheJob.cancel()
            listener.remove()
        }
    }

    override suspend fun addGoal(goal: GoalEntity): Result<Unit> {
        if (goal.userId.isBlank()) {
            return Result.failure(IllegalArgumentException("Authenticated userId is required"))
        }
        return try {
            val collection = db.collection("users").document(goal.userId).collection("goals")
            val docRef = if (goal.id.isNotBlank()) collection.document(goal.id) else collection.document()
            val savedGoal = goal.copy(id = docRef.id)
            goalDao?.upsert(savedGoal)
            docRef.set(savedGoal).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateGoal(goal: GoalEntity): Result<Unit> {
        if (goal.userId.isBlank() || goal.id.isBlank()) {
            return Result.failure(IllegalArgumentException("Authenticated userId and goalId are required"))
        }
        return try {
            goalDao?.upsert(goal)
            val collection = db.collection("users").document(goal.userId).collection("goals")
            collection.document(goal.id).set(goal).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteGoal(userId: String, goalId: String): Result<Unit> {
        if (userId.isBlank() || goalId.isBlank()) {
            return Result.failure(IllegalArgumentException("Authenticated userId and goalId are required"))
        }
        return try {
            goalDao?.deleteById(goalId)
            val collection = db.collection("users").document(userId).collection("goals")
            collection.document(goalId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getGoalContributions(userId: String, goalId: String): Flow<List<GoalContributionEntity>> = callbackFlow {
        if (userId.isBlank() || goalId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val cacheJob = launch {
            val cachedGoal = goalDao?.getGoalById(goalId)
            if (cachedGoal != null && cachedGoal.contributions.isNotEmpty()) {
                trySend(cachedGoal.contributions.sortedByDescending { it.timestampMillis })
            }
        }

        val goalDocRef = db.collection("users").document(userId).collection("goals").document(goalId)
        val subcollection = goalDocRef.collection("contributions")
            .orderBy("timestampMillis", Query.Direction.DESCENDING)

        val goalListener = goalDocRef.addSnapshotListener { snapshot, _ ->
            val goal = snapshot?.toObject(GoalEntity::class.java)
            if (goal != null && goal.contributions.isNotEmpty()) {
                trySend(goal.contributions.sortedByDescending { it.timestampMillis })
            }
        }

        val subcollectionListener = subcollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w("GoalRepository", "Contributions subcollection query error: ${error.message}")
                return@addSnapshotListener
            }
            val list = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(GoalContributionEntity::class.java)?.copy(id = doc.id)
            }?.sortedByDescending { it.timestampMillis } ?: emptyList()

            trySend(list)
        }

        awaitClose {
            cacheJob.cancel()
            goalListener.remove()
            subcollectionListener.remove()
        }
    }

    override suspend fun addGoalContribution(
        userId: String,
        contribution: GoalContributionEntity
    ): Result<Unit> {
        if (userId.isBlank() || contribution.goalId.isBlank()) {
            return Result.failure(IllegalArgumentException("Authenticated userId and goalId are required"))
        }
        return try {
            val goalCollection = db.collection("users").document(userId).collection("goals")
            val goalDocRef = goalCollection.document(contribution.goalId)
            val contribCollection = goalDocRef.collection("contributions")
            val contribDocRef = if (contribution.id.isNotBlank()) {
                contribCollection.document(contribution.id)
            } else {
                contribCollection.document()
            }
            val finalContribution = contribution.copy(id = contribDocRef.id, userId = userId)

            val cachedGoal = goalDao?.getGoalById(contribution.goalId)
            if (cachedGoal != null) {
                val newSaved = cachedGoal.currentSavedLkr + contribution.amountLkr
                val updatedContributions = listOf(finalContribution) + cachedGoal.contributions.filter { it.id != finalContribution.id }
                goalDao?.upsert(
                    cachedGoal.copy(
                        currentSavedLkr = newSaved,
                        isCompleted = newSaved >= cachedGoal.targetAmountLkr,
                        contributions = updatedContributions
                    )
                )
            }

            db.runTransaction { transaction ->
                val snapshot = transaction.get(goalDocRef)
                val currentGoal = snapshot.toObject(GoalEntity::class.java)
                    ?: GoalEntity(id = contribution.goalId, userId = userId)

                val newSaved = currentGoal.currentSavedLkr + contribution.amountLkr
                val existingContributions = currentGoal.contributions.filter { it.id != finalContribution.id }
                val updatedContributions = listOf(finalContribution) + existingContributions
                val updatedGoal = currentGoal.copy(
                    userId = userId,
                    currentSavedLkr = newSaved,
                    isCompleted = newSaved >= currentGoal.targetAmountLkr,
                    contributions = updatedContributions
                )
                transaction.set(goalDocRef, updatedGoal)
            }.await()

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
}
