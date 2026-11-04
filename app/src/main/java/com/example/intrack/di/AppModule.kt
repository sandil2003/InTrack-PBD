package com.example.intrack.di

import android.content.Context
import androidx.room.Room
import com.example.intrack.data.local.ExpenseDao
import com.example.intrack.data.local.GoalDao
import com.example.intrack.data.local.InTrackDatabase
import com.example.intrack.data.local.IncomeDao
import com.example.intrack.data.local.SubscriptionDao
import com.example.intrack.data.remote.FirestoreService
import com.example.intrack.data.repository.AuthRepository
import com.example.intrack.data.repository.AuthRepositoryImpl
import com.example.intrack.data.repository.CryptoRateRepository
import com.example.intrack.data.repository.CryptoRateRepositoryImpl
import com.example.intrack.data.repository.ExpenseRepository
import com.example.intrack.data.repository.ExpenseRepositoryImpl
import com.example.intrack.data.repository.GoalRepository
import com.example.intrack.data.repository.GoalRepositoryImpl
import com.example.intrack.data.repository.IncomeRepository
import com.example.intrack.data.repository.IncomeRepositoryImpl
import com.example.intrack.data.repository.SubscriptionRepository
import com.example.intrack.data.repository.SubscriptionRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirestoreService.db

    @Provides
    @Singleton
    fun provideInTrackDatabase(
        @ApplicationContext context: Context
    ): InTrackDatabase {
        return Room.databaseBuilder(
            context,
            InTrackDatabase::class.java,
            "intrack_local_cache.db"
        )
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }

    @Provides
    @Singleton
    fun provideExpenseDao(database: InTrackDatabase): ExpenseDao = database.expenseDao()

    @Provides
    @Singleton
    fun provideIncomeDao(database: InTrackDatabase): IncomeDao = database.incomeDao()

    @Provides
    @Singleton
    fun provideGoalDao(database: InTrackDatabase): GoalDao = database.goalDao()

    @Provides
    @Singleton
    fun provideSubscriptionDao(database: InTrackDatabase): SubscriptionDao = database.subscriptionDao()

    @Provides
    @Singleton
    fun provideAuthRepository(
        auth: FirebaseAuth,
        db: FirebaseFirestore
    ): AuthRepository = AuthRepositoryImpl(auth, db)

    @Provides
    @Singleton
    fun provideExpenseRepository(
        db: FirebaseFirestore,
        expenseDao: ExpenseDao
    ): ExpenseRepository = ExpenseRepositoryImpl(db, expenseDao)

    @Provides
    @Singleton
    fun provideIncomeRepository(
        db: FirebaseFirestore,
        incomeDao: IncomeDao
    ): IncomeRepository = IncomeRepositoryImpl(db, incomeDao)

    @Provides
    @Singleton
    fun provideGoalRepository(
        db: FirebaseFirestore,
        goalDao: GoalDao
    ): GoalRepository = GoalRepositoryImpl(db, goalDao)

    @Provides
    @Singleton
    fun provideSubscriptionRepository(
        db: FirebaseFirestore,
        subscriptionDao: SubscriptionDao
    ): SubscriptionRepository = SubscriptionRepositoryImpl(db, subscriptionDao)

    @Provides
    @Singleton
    fun provideCryptoRateRepository(): CryptoRateRepository = CryptoRateRepositoryImpl()
    fun provideSecurityPreferences(
        @ApplicationContext context: Context
    ): com.example.intrack.data.local.SecurityPreferences = com.example.intrack.data.local.SecurityPreferences(context)

    @Provides
    @Singleton
    fun provideBiometricAuthManager(): com.example.intrack.util.BiometricAuthManager = com.example.intrack.util.BiometricAuthManager()
}
