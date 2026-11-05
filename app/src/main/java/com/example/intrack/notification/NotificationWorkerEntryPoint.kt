package com.example.intrack.notification

import com.example.intrack.data.local.ExpenseDao
import com.example.intrack.data.local.GoalDao
import com.example.intrack.data.local.InTrackDatabase
import com.example.intrack.data.local.IncomeDao
import com.example.intrack.data.local.SubscriptionDao
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface NotificationWorkerEntryPoint {
    fun inTrackDatabase(): InTrackDatabase
    fun subscriptionDao(): SubscriptionDao
    fun expenseDao(): ExpenseDao
    fun incomeDao(): IncomeDao
    fun goalDao(): GoalDao
    fun notificationPreferences(): NotificationPreferences
    fun firebaseAuth(): FirebaseAuth
}
