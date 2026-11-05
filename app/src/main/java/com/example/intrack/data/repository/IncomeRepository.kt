package com.example.intrack.data.repository

import com.example.intrack.data.local.IncomeDao
import com.example.intrack.data.model.IncomeEntity
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

interface IncomeRepository {
    fun getIncomes(userId: String): Flow<List<IncomeEntity>>
    suspend fun addIncome(income: IncomeEntity): Result<Unit>
    suspend fun updateIncome(income: IncomeEntity): Result<Unit>
    suspend fun deleteIncome(userId: String, incomeId: String): Result<Unit>
}

@Singleton
class IncomeRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore,
    private val incomeDao: IncomeDao?
) : IncomeRepository {

    constructor(db: FirebaseFirestore = FirestoreService.db) : this(db, null)

    override fun getIncomes(userId: String): Flow<List<IncomeEntity>> = callbackFlow {
        if (userId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val cacheJob = launch {
            incomeDao?.getIncomes(userId)?.collect { cached ->
                trySend(cached)
            }
        }

        val collection = db.collection("users").document(userId).collection("incomes")
        val query = collection.orderBy("dateReceivedMillis", Query.Direction.DESCENDING)

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                if (incomeDao == null) {
                    trySend(emptyList())
                }
                return@addSnapshotListener
            }
            val list = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(IncomeEntity::class.java)?.copy(id = doc.id, userId = userId)
            } ?: emptyList()

            if (incomeDao != null) {
                launch {
                    incomeDao.replaceAllForUser(userId, list)
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

    override suspend fun addIncome(income: IncomeEntity): Result<Unit> {
        if (income.userId.isBlank()) {
            return Result.failure(IllegalArgumentException("Authenticated userId is required"))
        }
        return try {
            val collection = db.collection("users").document(income.userId).collection("incomes")
            val docRef = if (income.id.isNotBlank()) collection.document(income.id) else collection.document()
            val savedIncome = income.copy(id = docRef.id)
            incomeDao?.upsert(savedIncome)
            docRef.set(savedIncome).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateIncome(income: IncomeEntity): Result<Unit> {
        if (income.userId.isBlank() || income.id.isBlank()) {
            return Result.failure(IllegalArgumentException("Authenticated userId and incomeId are required"))
        }
        return try {
            incomeDao?.upsert(income)
            val collection = db.collection("users").document(income.userId).collection("incomes")
            collection.document(income.id).set(income).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteIncome(userId: String, incomeId: String): Result<Unit> {
        if (userId.isBlank() || incomeId.isBlank()) {
            return Result.failure(IllegalArgumentException("Authenticated userId and incomeId are required"))
        }
        return try {
            incomeDao?.deleteById(incomeId)
            val collection = db.collection("users").document(userId).collection("incomes")
            collection.document(incomeId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
