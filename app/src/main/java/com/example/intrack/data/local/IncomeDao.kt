package com.example.intrack.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.intrack.data.model.IncomeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IncomeDao {
    @Query("SELECT * FROM incomes WHERE userId = :userId ORDER BY dateReceivedMillis DESC")
    fun getIncomes(userId: String): Flow<List<IncomeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(income: IncomeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(incomes: List<IncomeEntity>)

    @Query("DELETE FROM incomes WHERE id = :incomeId")
    suspend fun deleteById(incomeId: String)

    @Query("DELETE FROM incomes WHERE userId = :userId")
    suspend fun deleteAllForUser(userId: String)

    @Transaction
    suspend fun replaceAllForUser(userId: String, incomes: List<IncomeEntity>) {
        deleteAllForUser(userId)
        upsertAll(incomes)
    }
}
