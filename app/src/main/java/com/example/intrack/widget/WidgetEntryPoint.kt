package com.example.intrack.widget

import com.example.intrack.data.local.ExpenseDao
import com.example.intrack.data.local.GoalDao
import com.example.intrack.data.local.InTrackDatabase
import com.example.intrack.data.local.IncomeDao
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun inTrackDatabase(): InTrackDatabase
    fun goalDao(): GoalDao
    fun expenseDao(): ExpenseDao
    fun incomeDao(): IncomeDao
}
