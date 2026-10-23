package com.example.intrack.data.repository

import com.example.intrack.data.model.IncomeEntity
import com.example.intrack.data.remote.FirestoreService
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

interface IncomeRepository {
    fun getIncomes(userId: String): Flow<List<IncomeEntity>>
    suspend fun addIncome(income: IncomeEntity): Result<Unit>
    suspend fun updateIncome(income: IncomeEntity): Result<Unit>
    suspend fun deleteIncome(userId: String, incomeId: String): Result<Unit>
}

class IncomeRepositoryImpl(
    private val db: FirebaseFirestore = FirestoreService.db
) : IncomeRepository {

    override fun getIncomes(userId: String): Flow<List<IncomeEntity>> = callbackFlow {
        val collection = if (userId.isNotEmpty()) {
            db.collection("users").document(userId).collection("incomes")
        } else {
            db.collection("incomes")
        }

        var isSeeding = false

        val listener = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val list = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(IncomeEntity::class.java)?.copy(id = doc.id)
            } ?: emptyList()

            // Default seed if empty
            if (list.isEmpty() && !isSeeding) {
                isSeeding = true
                val sampleList = getInitialSampleIncomes(userId)
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

    override suspend fun addIncome(income: IncomeEntity): Result<Unit> {
        return try {
            val collection = if (income.userId.isNotEmpty()) {
                db.collection("users").document(income.userId).collection("incomes")
            } else {
                db.collection("incomes")
            }
            val docRef = if (income.id.isNotBlank()) collection.document(income.id) else collection.document()
            docRef.set(income.copy(id = docRef.id)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateIncome(income: IncomeEntity): Result<Unit> {
        return try {
            val collection = if (income.userId.isNotEmpty()) {
                db.collection("users").document(income.userId).collection("incomes")
            } else {
                db.collection("incomes")
            }
            if (income.id.isNotEmpty()) {
                collection.document(income.id).set(income).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteIncome(userId: String, incomeId: String): Result<Unit> {
        return try {
            val collection = if (userId.isNotEmpty()) {
                db.collection("users").document(userId).collection("incomes")
            } else {
                db.collection("incomes")
            }
            collection.document(incomeId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getInitialSampleIncomes(userId: String): List<IncomeEntity> {
        return listOf(
            // Current Month (Month 0)
            IncomeEntity(
                id = "inc_curr_1",
                userId = userId,
                channel = "SALARY",
                title = "Monthly Base Salary & Bonus",
                originalAmount = 135000.0,
                originalCurrency = "LKR",
                realizedAmountLkr = 135000.0,
                status = "PAID",
                dateReceivedMillis = com.example.intrack.util.DateUtils.getMillisForMonthOffset(0, 25)
            ),
            IncomeEntity(
                id = "inc_curr_2",
                userId = userId,
                channel = "FREELANCE",
                title = "React E-Commerce Milestone",
                originalAmount = 45000.0,
                originalCurrency = "LKR",
                realizedAmountLkr = 45000.0,
                status = "INVOICED",
                clientName = "Apex Labs",
                dateReceivedMillis = com.example.intrack.util.DateUtils.getMillisForMonthOffset(0, 18)
            ),
            IncomeEntity(
                id = "inc_curr_3",
                userId = userId,
                channel = "ADSENSE",
                title = "Tech Blog AdSense Payout ($150)",
                originalAmount = 150.0,
                originalCurrency = "USD",
                exchangeRate = 305.0,
                realizedAmountLkr = 45750.0,
                status = "PAID",
                dateReceivedMillis = com.example.intrack.util.DateUtils.getMillisForMonthOffset(0, 12)
            ),
            IncomeEntity(
                id = "inc_curr_4",
                userId = userId,
                channel = "CRYPTO",
                title = "Realized Binance P&L",
                originalAmount = 22000.0,
                originalCurrency = "LKR",
                realizedAmountLkr = 22000.0,
                status = "PAID",
                dateReceivedMillis = com.example.intrack.util.DateUtils.getMillisForMonthOffset(0, 8)
            ),

            // Previous Month (Month 1)
            IncomeEntity(
                id = "inc_prev_1",
                userId = userId,
                channel = "SALARY",
                title = "Monthly Base Salary",
                originalAmount = 120000.0,
                originalCurrency = "LKR",
                realizedAmountLkr = 120000.0,
                status = "PAID",
                dateReceivedMillis = com.example.intrack.util.DateUtils.getMillisForMonthOffset(1, 25)
            ),
            IncomeEntity(
                id = "inc_prev_2",
                userId = userId,
                channel = "FREELANCE",
                title = "WordPress SME Portal",
                originalAmount = 35000.0,
                originalCurrency = "LKR",
                realizedAmountLkr = 35000.0,
                status = "PAID",
                clientName = "Ceylon Organics",
                dateReceivedMillis = com.example.intrack.util.DateUtils.getMillisForMonthOffset(1, 16)
            ),
            IncomeEntity(
                id = "inc_prev_3",
                userId = userId,
                channel = "ADSENSE",
                title = "Tech Blog AdSense Payout ($95)",
                originalAmount = 95.0,
                originalCurrency = "USD",
                exchangeRate = 303.0,
                realizedAmountLkr = 28785.0,
                status = "PAID",
                dateReceivedMillis = com.example.intrack.util.DateUtils.getMillisForMonthOffset(1, 12)
            ),
            IncomeEntity(
                id = "inc_prev_4",
                userId = userId,
                channel = "CRYPTO",
                title = "Realized Binance P&L",
                originalAmount = 15000.0,
                originalCurrency = "LKR",
                realizedAmountLkr = 15000.0,
                status = "PAID",
                dateReceivedMillis = com.example.intrack.util.DateUtils.getMillisForMonthOffset(1, 5)
            ),

            // 2 Months Ago (Month 2)
            IncomeEntity(
                id = "inc_prev2_1",
                userId = userId,
                channel = "SALARY",
                title = "Monthly Base Salary & Bonus",
                originalAmount = 140000.0,
                originalCurrency = "LKR",
                realizedAmountLkr = 140000.0,
                status = "PAID",
                dateReceivedMillis = com.example.intrack.util.DateUtils.getMillisForMonthOffset(2, 25)
            ),
            IncomeEntity(
                id = "inc_prev2_2",
                userId = userId,
                channel = "FREELANCE",
                title = "Mobile UI Consultation",
                originalAmount = 25000.0,
                originalCurrency = "LKR",
                realizedAmountLkr = 25000.0,
                status = "PAID",
                clientName = "Studio K",
                dateReceivedMillis = com.example.intrack.util.DateUtils.getMillisForMonthOffset(2, 14)
            ),
            IncomeEntity(
                id = "inc_prev2_3",
                userId = userId,
                channel = "ADSENSE",
                title = "Tech Blog AdSense Payout ($60)",
                originalAmount = 60.0,
                originalCurrency = "USD",
                exchangeRate = 302.0,
                realizedAmountLkr = 18120.0,
                status = "PAID",
                dateReceivedMillis = com.example.intrack.util.DateUtils.getMillisForMonthOffset(2, 12)
            ),
            IncomeEntity(
                id = "inc_prev2_4",
                userId = userId,
                channel = "CRYPTO",
                title = "Realized Binance P&L",
                originalAmount = 12000.0,
                originalCurrency = "LKR",
                realizedAmountLkr = 12000.0,
                status = "PAID",
                dateReceivedMillis = com.example.intrack.util.DateUtils.getMillisForMonthOffset(2, 6)
            )
        )
    }
}

