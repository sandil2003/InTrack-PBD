package com.example.intrack.viewmodel

import com.example.intrack.data.model.ExpenseEntity
import com.example.intrack.data.model.GoalEntity

data class DashboardData(
    val totalIncomeLkr: Double = 0.0,
    val totalExpenseLkr: Double = 0.0,
    val committedExpenseLkr: Double = 0.0,
    val discretionaryExpenseLkr: Double = 0.0,
    val goals: List<GoalEntity> = emptyList(),
    val macBookGoalProgress: GoalEntity = GoalEntity(),
    val recentTransactions: List<ExpenseEntity> = emptyList(),
    val shockInsightMessage: String = ""
)
