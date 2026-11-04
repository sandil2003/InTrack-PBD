package com.example.intrack.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.intrack.data.model.ExpenseEntity
import com.example.intrack.data.model.GoalEntity
import com.example.intrack.data.model.IncomeEntity
import com.example.intrack.data.model.SubscriptionEntity

@Database(
    entities = [
        ExpenseEntity::class,
        IncomeEntity::class,
        GoalEntity::class,
        SubscriptionEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class InTrackDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun incomeDao(): IncomeDao
    abstract fun goalDao(): GoalDao
    abstract fun subscriptionDao(): SubscriptionDao
}
