package com.example.intrack.data.repository

import com.example.intrack.data.model.ExpenseEntity
import com.example.intrack.data.remote.FirestoreService
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import com.example.intrack.util.DateUtils

interface ExpenseRepository {
    fun getExpenses(userId: String): Flow<List<ExpenseEntity>>
    suspend fun addExpense(expense: ExpenseEntity): Result<Unit>
    suspend fun updateExpense(expense: ExpenseEntity): Result<Unit>
    suspend fun deleteExpense(userId: String, expenseId: String): Result<Unit>
}

class ExpenseRepositoryImpl(
    private val db: FirebaseFirestore = FirestoreService.db
) : ExpenseRepository {

    private var isSeeding = false

    override fun getExpenses(userId: String): Flow<List<ExpenseEntity>> = callbackFlow {
        val collection = if (userId.isNotEmpty()) {
            db.collection("users").document(userId).collection("expenses")
        } else {
            db.collection("expenses")
        }

        val listener = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val list = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(ExpenseEntity::class.java)?.copy(id = doc.id)
            } ?: emptyList()

            if (list.isEmpty() && !isSeeding) {
                isSeeding = true
                val sampleList = getInitialSampleExpenses(userId)
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

    override suspend fun addExpense(expense: ExpenseEntity): Result<Unit> {
        return try {
            val collection = if (expense.userId.isNotEmpty()) {
                db.collection("users").document(expense.userId).collection("expenses")
            } else {
                db.collection("expenses")
            }
            val docRef = if (expense.id.isNotBlank()) collection.document(expense.id) else collection.document()
            docRef.set(expense.copy(id = docRef.id)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateExpense(expense: ExpenseEntity): Result<Unit> {
        return try {
            val collection = if (expense.userId.isNotEmpty()) {
                db.collection("users").document(expense.userId).collection("expenses")
            } else {
                db.collection("expenses")
            }
            collection.document(expense.id).set(expense).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteExpense(userId: String, expenseId: String): Result<Unit> {
        return try {
            val collection = if (userId.isNotEmpty()) {
                db.collection("users").document(userId).collection("expenses")
            } else {
                db.collection("expenses")
            }
            collection.document(expenseId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Sample expenses list
    private fun getInitialSampleExpenses(userId: String): List<ExpenseEntity> {
        return listOf(
            // Current Month (Month Offset 0)
            ExpenseEntity(
                id = "exp_curr_1",
                userId = userId,
                title = "Monthly House Rent",
                amountLkr = 34000.0,
                category = "RENT",
                paymentAccount = "ACCOUNT_SECONDARY",
                commitmentType = "COMMITTED",
                dateSpentMillis = DateUtils.getMillisForMonthOffset(0, 28)
            ),
            ExpenseEntity(
                id = "exp_curr_atm",
                userId = userId,
                title = "ATM Cash Withdrawal",
                amountLkr = 15000.0,
                category = "MISC",
                paymentAccount = "CASH",
                commitmentType = "DISCRETIONARY",
                dateSpentMillis = DateUtils.getMillisForMonthOffset(0, 25)
            ),
            ExpenseEntity(
                id = "exp_curr_2",
                userId = userId,
                title = "UberEats Dinner",
                amountLkr = 2850.0,
                category = "DINING_OUT",
                paymentAccount = "CARD_PRIMARY",
                commitmentType = "DISCRETIONARY",
                dateSpentMillis = DateUtils.getMillisForMonthOffset(0, 22)
            ),
            ExpenseEntity(
                id = "exp_curr_3",
                userId = userId,
                title = "Coffee & Bakery",
                amountLkr = 1450.0,
                category = "DINING_OUT",
                paymentAccount = "CASH",
                commitmentType = "DISCRETIONARY",
                dateSpentMillis = DateUtils.getMillisForMonthOffset(0, 19)
            ),
            ExpenseEntity(
                id = "exp_curr_4",
                userId = userId,
                title = "Electricity & Water Bill",
                amountLkr = 8500.0,
                category = "UTILITIES",
                paymentAccount = "CARD_PRIMARY",
                commitmentType = "COMMITTED",
                dateSpentMillis = DateUtils.getMillisForMonthOffset(0, 15)
            ),
            ExpenseEntity(
                id = "exp_curr_cafe",
                userId = userId,
                title = "Café Java Ceylon (Cash)",
                amountLkr = 2400.0,
                category = "DINING_OUT",
                paymentAccount = "CASH",
                commitmentType = "DISCRETIONARY",
                dateSpentMillis = DateUtils.getMillisForMonthOffset(0, 12)
            ),
            ExpenseEntity(
                id = "exp_curr_5",
                userId = userId,
                title = "PickMe Office Commute",
                amountLkr = 1850.0,
                category = "TRANSPORT",
                paymentAccount = "CARD_PRIMARY",
                commitmentType = "COMMITTED",
                dateSpentMillis = DateUtils.getMillisForMonthOffset(0, 10)
            ),
            ExpenseEntity(
                id = "exp_curr_6",
                userId = userId,
                title = "Gym Auto-Debit",
                amountLkr = 4500.0,
                category = "SUBSCRIPTION",
                paymentAccount = "ACCOUNT_SECONDARY",
                commitmentType = "COMMITTED",
                dateSpentMillis = DateUtils.getMillisForMonthOffset(0, 5)
            ),

            // Previous Month (Month Offset 1) - directly from the assignment scenario
            ExpenseEntity(
                id = "exp_prev_1",
                userId = userId,
                title = "Monthly House Rent",
                amountLkr = 34000.0,
                category = "RENT",
                paymentAccount = "ACCOUNT_SECONDARY",
                commitmentType = "COMMITTED",
                dateSpentMillis = DateUtils.getMillisForMonthOffset(1, 28)
            ),
            ExpenseEntity(
                id = "exp_prev_atm",
                userId = userId,
                title = "Commercial Bank ATM Cash",
                amountLkr = 20000.0,
                category = "MISC",
                paymentAccount = "CASH",
                commitmentType = "DISCRETIONARY",
                dateSpentMillis = DateUtils.getMillisForMonthOffset(1, 26)
            ),
            ExpenseEntity(
                id = "exp_prev_2",
                userId = userId,
                title = "Coffee & Casual Dining",
                amountLkr = 24000.0,
                category = "DINING_OUT",
                paymentAccount = "CARD_PRIMARY",
                commitmentType = "DISCRETIONARY",
                dateSpentMillis = DateUtils.getMillisForMonthOffset(1, 24)
            ),
            ExpenseEntity(
                id = "exp_prev_3",
                userId = userId,
                title = "PickMe Night Rides",
                amountLkr = 6800.0,
                category = "TRANSPORT",
                paymentAccount = "CARD_PRIMARY",
                commitmentType = "DISCRETIONARY",
                dateSpentMillis = DateUtils.getMillisForMonthOffset(1, 20)
            ),
            ExpenseEntity(
                id = "exp_prev_cafe_cash",
                userId = userId,
                title = "Corner Bakery & Espresso (Cash)",
                amountLkr = 4200.0,
                category = "DINING_OUT",
                paymentAccount = "CASH",
                commitmentType = "DISCRETIONARY",
                dateSpentMillis = DateUtils.getMillisForMonthOffset(1, 18)
            ),
            ExpenseEntity(
                id = "exp_prev_4",
                userId = userId,
                title = "Utility Bills & Fiber WiFi",
                amountLkr = 9200.0,
                category = "UTILITIES",
                paymentAccount = "CARD_PRIMARY",
                commitmentType = "COMMITTED",
                dateSpentMillis = DateUtils.getMillisForMonthOffset(1, 15)
            ),
            ExpenseEntity(
                id = "exp_prev_5",
                userId = userId,
                title = "Unused Cloud & Media Subs",
                amountLkr = 3500.0,
                category = "SUBSCRIPTION",
                paymentAccount = "CARD_PRIMARY",
                commitmentType = "DISCRETIONARY",
                dateSpentMillis = DateUtils.getMillisForMonthOffset(1, 8)
            ),
            ExpenseEntity(
                id = "exp_prev_6",
                userId = userId,
                title = "Gym Auto-Debit",
                amountLkr = 4500.0,
                category = "SUBSCRIPTION",
                paymentAccount = "ACCOUNT_SECONDARY",
                commitmentType = "COMMITTED",
                dateSpentMillis = DateUtils.getMillisForMonthOffset(1, 5)
            ),

            // 2 Months Ago (Month Offset 2)
            ExpenseEntity(
                id = "exp_hist_1",
                userId = userId,
                title = "Monthly House Rent",
                amountLkr = 34000.0,
                category = "RENT",
                paymentAccount = "ACCOUNT_SECONDARY",
                commitmentType = "COMMITTED",
                dateSpentMillis = DateUtils.getMillisForMonthOffset(2, 28)
            ),
            ExpenseEntity(
                id = "exp_hist_atm",
                userId = userId,
                title = "ATM Cash Withdrawal",
                amountLkr = 15000.0,
                category = "MISC",
                paymentAccount = "CASH",
                commitmentType = "DISCRETIONARY",
                dateSpentMillis = DateUtils.getMillisForMonthOffset(2, 25)
            ),
            ExpenseEntity(
                id = "exp_hist_2",
                userId = userId,
                title = "UberEats & Takeout",
                amountLkr = 14500.0,
                category = "DINING_OUT",
                paymentAccount = "CARD_PRIMARY",
                commitmentType = "DISCRETIONARY",
                dateSpentMillis = DateUtils.getMillisForMonthOffset(2, 21)
            ),
            ExpenseEntity(
                id = "exp_hist_3",
                userId = userId,
                title = "PickMe Office Rides",
                amountLkr = 4200.0,
                category = "TRANSPORT",
                paymentAccount = "CARD_PRIMARY",
                commitmentType = "COMMITTED",
                dateSpentMillis = DateUtils.getMillisForMonthOffset(2, 14)
            ),
            ExpenseEntity(
                id = "exp_hist_cafe",
                userId = userId,
                title = "Artisan Café Cash Payment",
                amountLkr = 3800.0,
                category = "DINING_OUT",
                paymentAccount = "CASH",
                commitmentType = "DISCRETIONARY",
                dateSpentMillis = DateUtils.getMillisForMonthOffset(2, 12)
            ),
            ExpenseEntity(
                id = "exp_hist_4",
                userId = userId,
                title = "Electricity & Water Bill",
                amountLkr = 7800.0,
                category = "UTILITIES",
                paymentAccount = "CARD_PRIMARY",
                commitmentType = "COMMITTED",
                dateSpentMillis = DateUtils.getMillisForMonthOffset(2, 11)
            ),
            ExpenseEntity(
                id = "exp_hist_5",
                userId = userId,
                title = "Gym Auto-Debit",
                amountLkr = 4500.0,
                category = "SUBSCRIPTION",
                paymentAccount = "ACCOUNT_SECONDARY",
                commitmentType = "COMMITTED",
                dateSpentMillis = DateUtils.getMillisForMonthOffset(2, 5)
            )
        )
    }
}
