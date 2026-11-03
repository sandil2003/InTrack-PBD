package com.example.intrack.viewmodel

import com.example.intrack.data.model.ExpenseEntity
import com.example.intrack.data.model.GoalEntity

data class DashboardData(
    val totalIncomeLkr: Double = 0.0,
    val totalExpenseLkr: Double = 0.0,
    val remainingBalanceLkr: Double = 0.0,
    val inflowFraction: Float = 0.5f,
    val committedExpenseLkr: Double = 0.0,
    val discretionaryExpenseLkr: Double = 0.0,
    val goals: List<GoalEntity> = emptyList(),
    val macBookGoalProgress: GoalEntity = GoalEntity(),
    val recentTransactions: List<ExpenseEntity> = emptyList(),
    val filteredRecentTransactions: List<ExpenseEntity> = emptyList(),
    val groupedRecentTransactions: Map<String, List<ExpenseEntity>> = emptyMap(),
    val selectedCategoryFilter: String = "ALL",
    val selectedAccountFilter: String = "ALL",
    val shockInsightMessage: String = ""
)
