package com.example.intrack.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.intrack.data.model.GoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals WHERE userId = :userId ORDER BY createdAtMillis DESC")
    fun getGoals(userId: String): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE id = :goalId LIMIT 1")
    suspend fun getGoalById(goalId: String): GoalEntity?

    @Query("SELECT * FROM goals ORDER BY createdAtMillis DESC LIMIT 1")
    suspend fun getPrimaryGoal(): GoalEntity?

    @Query("SELECT * FROM goals ORDER BY createdAtMillis DESC")
    suspend fun getAllGoals(): List<GoalEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(goal: GoalEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(goals: List<GoalEntity>)

    @Query("DELETE FROM goals WHERE id = :goalId")
    suspend fun deleteById(goalId: String)

    @Query("DELETE FROM goals WHERE userId = :userId")
    suspend fun deleteAllForUser(userId: String)

    @Transaction
    suspend fun replaceAllForUser(userId: String, goals: List<GoalEntity>) {
        deleteAllForUser(userId)
        upsertAll(goals)
    }
}
