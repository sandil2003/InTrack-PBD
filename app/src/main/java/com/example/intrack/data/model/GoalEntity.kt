package com.example.intrack.data.model

data class GoalEntity(
    val id: String = "macbook_m4_goal",
    val userId: String = "",
    val title: String = "MacBook Pro M4 Fund",
    val targetAmountLkr: Double = 490000.0,
    val currentSavedLkr: Double = 11200.0,
    val targetDateMillis: Long = System.currentTimeMillis() + (365L * 24 * 60 * 60 * 1000),
    val monthlyTargetRunRateLkr: Double = 40833.33,
    val isCompleted: Boolean = false
)
