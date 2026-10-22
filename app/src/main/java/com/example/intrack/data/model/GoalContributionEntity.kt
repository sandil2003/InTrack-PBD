package com.example.intrack.data.model

data class GoalContributionEntity(
    val id: String = "",
    val goalId: String = "",
    val userId: String = "",
    val amountLkr: Double = 0.0,
    val sourceTitle: String = "Direct Contribution",
    val sourceId: String = "manual",
    val timestampMillis: Long = System.currentTimeMillis(),
    val note: String? = null
)
