package com.example.intrack.data.repository

import com.example.intrack.data.local.ExpenseDao
import com.example.intrack.data.model.ExpenseEntity
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

interface ExpenseRepository {
    fun getExpenses(userId: String): Flow<List<ExpenseEntity>>
    suspend fun addExpense(expense: ExpenseEntity): Result<Unit>
    suspend fun updateExpense(expense: ExpenseEntity): Result<Unit>
    suspend fun deleteExpense(userId: String, expenseId: String): Result<Unit>
}

@Singleton
class ExpenseRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore,
    private val expenseDao: ExpenseDao?
) : ExpenseRepository {

    constructor(db: FirebaseFirestore = FirestoreService.db) : this(db, null)

    override fun getExpenses(userId: String): Flow<List<ExpenseEntity>> = callbackFlow {
        if (userId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val cacheJob = launch {
            expenseDao?.getExpenses(userId)?.collect { cached ->
                trySend(cached)
            }
        }

        val collection = db.collection("users").document(userId).collection("expenses")
        val query = collection.orderBy("dateSpentMillis", Query.Direction.DESCENDING)

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                if (expenseDao == null) {
                    trySend(emptyList())
                }
                return@addSnapshotListener
            }
            val list = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(ExpenseEntity::class.java)?.copy(id = doc.id, userId = userId)
            } ?: emptyList()

            if (expenseDao != null) {
                launch {
                    expenseDao.replaceAllForUser(userId, list)
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

    override suspend fun addExpense(expense: ExpenseEntity): Result<Unit> {
        if (expense.userId.isBlank()) {
            return Result.failure(IllegalArgumentException("Authenticated userId is required"))
        }
        return try {
            val collection = db.collection("users").document(expense.userId).collection("expenses")
            val docRef = if (expense.id.isNotBlank()) collection.document(expense.id) else collection.document()
            val savedExpense = expense.copy(id = docRef.id)
            expenseDao?.upsert(savedExpense)
            docRef.set(savedExpense).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateExpense(expense: ExpenseEntity): Result<Unit> {
        if (expense.userId.isBlank() || expense.id.isBlank()) {
            return Result.failure(IllegalArgumentException("Authenticated userId and expenseId are required"))
        }
        return try {
            expenseDao?.upsert(expense)
            val collection = db.collection("users").document(expense.userId).collection("expenses")
            collection.document(expense.id).set(expense).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteExpense(userId: String, expenseId: String): Result<Unit> {
        if (userId.isBlank() || expenseId.isBlank()) {
            return Result.failure(IllegalArgumentException("Authenticated userId and expenseId are required"))
        }
        return try {
            expenseDao?.deleteById(expenseId)
            val collection = db.collection("users").document(userId).collection("expenses")
            collection.document(expenseId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
