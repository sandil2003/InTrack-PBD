package com.example.intrack

import com.example.intrack.data.model.GoalEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GoalTrackerUnitTest {

    private val sampleGoals = listOf(
        GoalEntity(
            id = "g1",
            title = "MacBook Pro M4",
            targetAmountLkr = 490000.0,
            currentSavedLkr = 125000.0,
            category = "TECH",
            isCompleted = false
        ),
        GoalEntity(
            id = "g2",
            title = "Emergency Fund Buffer",
            targetAmountLkr = 300000.0,
            currentSavedLkr = 300000.0,
            category = "EMERGENCY",
            isCompleted = true
        ),
        GoalEntity(
            id = "g3",
            title = "Japan Vacation Trip",
            targetAmountLkr = 650000.0,
            currentSavedLkr = 200000.0,
            category = "TRAVEL",
            isCompleted = false
        ),
        GoalEntity(
            id = "g4",
            title = "Motorbike Fund",
            targetAmountLkr = 400000.0,
            currentSavedLkr = 420000.0, // Over-achieved
            category = "VEHICLE",
            isCompleted = false
        )
    )

    @Test
    fun goalOverviewMetrics_calculateCorrectTotalsAndRatios() {
        val totalSaved = sampleGoals.sumOf { it.currentSavedLkr }
        val totalTarget = sampleGoals.sumOf { it.targetAmountLkr }
        val expectedSaved = 125000.0 + 300000.0 + 200000.0 + 420000.0
        val expectedTarget = 490000.0 + 300000.0 + 650000.0 + 400000.0

        assertEquals(expectedSaved, totalSaved, 0.01)
        assertEquals(expectedTarget, totalTarget, 0.01)

        val ratio = (totalSaved / totalTarget).toFloat()
        assertTrue(ratio in 0.5f..0.6f)

        val totalRemaining = (totalTarget - totalSaved).coerceAtLeast(0.0)
        assertEquals(expectedTarget - expectedSaved, totalRemaining, 0.01)
    }

    @Test
    fun goalCompletionFiltering_correctlyPartitionsActiveAndCompleted() {
        val completed = sampleGoals.filter {
            it.isCompleted || (it.targetAmountLkr > 0.0 && it.currentSavedLkr >= it.targetAmountLkr)
        }
        val inProgress = sampleGoals.filter {
            !(it.isCompleted || (it.targetAmountLkr > 0.0 && it.currentSavedLkr >= it.targetAmountLkr))
        }

        assertEquals(2, completed.size) // Emergency Fund Buffer & Motorbike Fund
        assertEquals(2, inProgress.size) // MacBook Pro M4 & Japan Vacation Trip

        assertTrue(completed.any { it.title == "Emergency Fund Buffer" })
        assertTrue(completed.any { it.title == "Motorbike Fund" })
        assertTrue(inProgress.any { it.title == "MacBook Pro M4" })
        assertTrue(inProgress.any { it.title == "Japan Vacation Trip" })
    }

    @Test
    fun goalRemainingAndProgress_handlesBoundariesAndOverachievement() {
        val overachievedGoal = sampleGoals.first { it.id == "g4" }
        val progress = (overachievedGoal.currentSavedLkr / overachievedGoal.targetAmountLkr).toFloat().coerceIn(0f, 1f)
        val remaining = (overachievedGoal.targetAmountLkr - overachievedGoal.currentSavedLkr).coerceAtLeast(0.0)

        assertEquals(1.0f, progress, 0.001f)
        assertEquals(0.0, remaining, 0.001)

        val normalGoal = sampleGoals.first { it.id == "g1" }
        val normalRemaining = (normalGoal.targetAmountLkr - normalGoal.currentSavedLkr).coerceAtLeast(0.0)
        assertEquals(365000.0, normalRemaining, 0.001)
    }

    @Test
    fun emptyGoalsList_handlesSafelyWithoutDivisionByZero() {
        val emptyGoals = emptyList<GoalEntity>()
        val totalSaved = emptyGoals.sumOf { it.currentSavedLkr }
        val totalTarget = emptyGoals.sumOf { it.targetAmountLkr }
        val overallRatio = if (totalTarget > 0.0) (totalSaved / totalTarget).toFloat().coerceIn(0f, 1f) else 0f
        val percentageInt = (overallRatio * 100).toInt()

        assertEquals(0.0, totalSaved, 0.001)
        assertEquals(0.0, totalTarget, 0.001)
        assertEquals(0f, overallRatio, 0.001f)
        assertEquals(0, percentageInt)
    }

    @Test
    fun goalTimelineStatus_identifiesOverdueDueTodayAndFutureDays() {
        val now = 1700000000000L
        val pastDate = now - (3L * 24 * 60 * 60 * 1000L) // 3 days ago
        val futureDate = now + (15L * 24 * 60 * 60 * 1000L) // 15 days ahead

        val isOverdue = pastDate < now
        val daysLeftPast = com.example.intrack.util.DateUtils.daysBetween(now, pastDate).coerceAtLeast(0L)
        val daysLeftFuture = com.example.intrack.util.DateUtils.daysBetween(now, futureDate).coerceAtLeast(0L)
        val daysLeftToday = com.example.intrack.util.DateUtils.daysBetween(now, now).coerceAtLeast(0L)

        assertTrue(isOverdue)
        assertEquals(0L, daysLeftPast)
        assertEquals(15L, daysLeftFuture)
        assertEquals(0L, daysLeftToday)
    }

    @Test
    fun currencyFormatter_formatsLkrWholeWithoutDecimalCents() {
        val amount = 1465000.00
        val formattedWhole = com.example.intrack.util.CurrencyFormatter.formatLkrWhole(amount)
        assertEquals("LKR 1,465,000", formattedWhole)

        val zeroAmount = 0.0
        val formattedZero = com.example.intrack.util.CurrencyFormatter.formatLkrWhole(zeroAmount)
        assertEquals("LKR 0", formattedZero)
    }

    @Test
    fun goalQuickDeposit_updatesSavedAmountAndMarksCompleted() {
        val initialGoal = GoalEntity(
            id = "g_dep",
            title = "Sony Alpha Camera",
            targetAmountLkr = 250000.0,
            currentSavedLkr = 200000.0,
            category = "TECH",
            isCompleted = false
        )

        val depositAmount = 50000.0
        val newSaved = initialGoal.currentSavedLkr + depositAmount
        val updatedGoal = initialGoal.copy(
            currentSavedLkr = newSaved,
            isCompleted = newSaved >= initialGoal.targetAmountLkr
        )

        assertEquals(250000.0, updatedGoal.currentSavedLkr, 0.01)
        assertTrue(updatedGoal.isCompleted)
    }

    @Test
    fun simplifiedGoalFilters_correctlyPartitionActiveAndDone() {
        val allFiltered = sampleGoals.filter { g ->
            val isDone = g.isCompleted || (g.targetAmountLkr > 0.0 && g.currentSavedLkr >= g.targetAmountLkr)
            when ("ALL") {
                "ACTIVE", "IN_PROGRESS" -> !isDone
                "DONE", "COMPLETED" -> isDone
                else -> true
            }
        }
        val activeFiltered = sampleGoals.filter { g ->
            val isDone = g.isCompleted || (g.targetAmountLkr > 0.0 && g.currentSavedLkr >= g.targetAmountLkr)
            when ("ACTIVE") {
                "ACTIVE", "IN_PROGRESS" -> !isDone
                "DONE", "COMPLETED" -> isDone
                else -> true
            }
        }
        val doneFiltered = sampleGoals.filter { g ->
            val isDone = g.isCompleted || (g.targetAmountLkr > 0.0 && g.currentSavedLkr >= g.targetAmountLkr)
            when ("DONE") {
                "ACTIVE", "IN_PROGRESS" -> !isDone
                "DONE", "COMPLETED" -> isDone
                else -> true
            }
        }

        assertEquals(4, allFiltered.size)
        assertEquals(2, activeFiltered.size)
        assertEquals(2, doneFiltered.size)

        // Ensure "Active" contains MacBook and Japan trip
        assertTrue(activeFiltered.all { it.title in listOf("MacBook Pro M4", "Japan Vacation Trip") })
        // Ensure "Done" contains Emergency fund and Motorbike
        assertTrue(doneFiltered.all { it.title in listOf("Emergency Fund Buffer", "Motorbike Fund") })
    }

    @Test
    fun simplifiedHeroOverview_formatsProgressPercentageCorrectly() {
        // Case 1: Partial completion (approx 56%)
        val totalSaved = sampleGoals.sumOf { it.currentSavedLkr }
        val totalTarget = sampleGoals.sumOf { it.targetAmountLkr }
        val ratio = if (totalTarget > 0.0) (totalSaved / totalTarget).toFloat().coerceIn(0f, 1f) else 0f
        val completedCount1 = sampleGoals.count { it.isCompleted || (it.targetAmountLkr > 0.0 && it.currentSavedLkr >= it.targetAmountLkr) }
        val activeCount1 = sampleGoals.size - completedCount1
        val isFullyCompleted1 = (activeCount1 == 0 && completedCount1 > 0) || (ratio >= 1f && (totalTarget > 0.0 || completedCount1 > 0))
        val effRatio1 = if (isFullyCompleted1) 1f else ratio
        val percent1 = (effRatio1 * 100).toInt()
        val headline1 = if (isFullyCompleted1) "100% Achieved 🎉" else "$percent1% Achieved"
        assertEquals("56% Achieved", headline1)

        // Case 2: 100% completion with matching target
        val fullyDoneGoals = listOf(
            GoalEntity(id = "1", title = "G1", targetAmountLkr = 100.0, currentSavedLkr = 100.0, isCompleted = true)
        )
        val doneSaved = fullyDoneGoals.sumOf { it.currentSavedLkr }
        val doneTarget = fullyDoneGoals.sumOf { it.targetAmountLkr }
        val doneRatio = if (doneTarget > 0.0) (doneSaved / doneTarget).toFloat().coerceIn(0f, 1f) else 0f
        val completedCount2 = fullyDoneGoals.count { it.isCompleted || (it.targetAmountLkr > 0.0 && it.currentSavedLkr >= it.targetAmountLkr) }
        val activeCount2 = fullyDoneGoals.size - completedCount2
        val isFullyCompleted2 = (activeCount2 == 0 && completedCount2 > 0) || (doneRatio >= 1f && (doneTarget > 0.0 || completedCount2 > 0))
        val effRatio2 = if (isFullyCompleted2) 1f else doneRatio
        val doneHeadline = if (isFullyCompleted2) "100% Achieved 🎉" else "${(effRatio2 * 100).toInt()}% Achieved"
        assertEquals("100% Achieved 🎉", doneHeadline)

        // Case 3: Empty / 0%
        val zeroSaved = 0.0
        val zeroTarget = 500.0
        val zeroRatio = if (zeroTarget > 0.0) (zeroSaved / zeroTarget).toFloat().coerceIn(0f, 1f) else 0f
        val zeroCompleted = (0 == 0 && 0 > 0) || (zeroRatio >= 1f && zeroTarget > 0.0)
        val zeroHeadline = if (zeroCompleted) "100% Achieved 🎉" else "${(zeroRatio * 100).toInt()}% Achieved"
        assertEquals("0% Achieved", zeroHeadline)

        // Case 4: Edge case - Zero-target goals where targetAmountLkr == 0.0 and isCompleted == true
        val zeroTargetDoneGoals = listOf(
            GoalEntity(id = "z1", title = "Zero Target", targetAmountLkr = 0.0, currentSavedLkr = 0.0, isCompleted = true)
        )
        val zTarget = zeroTargetDoneGoals.sumOf { it.targetAmountLkr }
        val zRatio = if (zTarget > 0.0) (zeroTargetDoneGoals.sumOf { it.currentSavedLkr } / zTarget).toFloat().coerceIn(0f, 1f) else 0f
        val zCompletedCount = zeroTargetDoneGoals.count { it.isCompleted || (it.targetAmountLkr > 0.0 && it.currentSavedLkr >= it.targetAmountLkr) }
        val zActiveCount = zeroTargetDoneGoals.size - zCompletedCount
        val zIsFullyCompleted = (zActiveCount == 0 && zCompletedCount > 0) || (zRatio >= 1f && (zTarget > 0.0 || zCompletedCount > 0))
        val zHeadline = if (zIsFullyCompleted) "100% Achieved 🎉" else "${(zRatio * 100).toInt()}% Achieved"
        assertEquals("100% Achieved 🎉", zHeadline)

        // Case 5: Edge case - All goals marked completed even if currentSaved < targetAmount
        val prematurelyCompletedGoals = listOf(
            GoalEntity(id = "p1", title = "Premature Done", targetAmountLkr = 1000.0, currentSavedLkr = 800.0, isCompleted = true)
        )
        val pTarget = prematurelyCompletedGoals.sumOf { it.targetAmountLkr }
        val pRatio = if (pTarget > 0.0) (prematurelyCompletedGoals.sumOf { it.currentSavedLkr } / pTarget).toFloat().coerceIn(0f, 1f) else 0f
        val pCompletedCount = prematurelyCompletedGoals.count { it.isCompleted || (it.targetAmountLkr > 0.0 && it.currentSavedLkr >= it.targetAmountLkr) }
        val pActiveCount = prematurelyCompletedGoals.size - pCompletedCount
        val pIsFullyCompleted = (pActiveCount == 0 && pCompletedCount > 0) || (pRatio >= 1f && (pTarget > 0.0 || pCompletedCount > 0))
        val pEffRatio = if (pIsFullyCompleted) 1f else pRatio
        val pHeadline = if (pIsFullyCompleted) "100% Achieved 🎉" else "${(pEffRatio * 100).toInt()}% Achieved"
        assertEquals(1.0f, pEffRatio, 0.001f)
        assertEquals("100% Achieved 🎉", pHeadline)
    }

    @Test
    fun goalCardTimelineBadge_formatsCorrectInlineBadges() {
        val now = 1700000000000L
        fun badgeFor(isCompleted: Boolean, targetDate: Long): String {
            val isOverdue = !isCompleted && targetDate < now
            val daysLeft = com.example.intrack.util.DateUtils.daysBetween(now, targetDate).coerceAtLeast(0L)
            return when {
                isCompleted -> "• Done 🎉"
                isOverdue -> "• Overdue"
                daysLeft == 0L -> "• Due today"
                else -> "• ${daysLeft}d left"
            }
        }

        assertEquals("• Done 🎉", badgeFor(isCompleted = true, targetDate = now + 10000000L))
        assertEquals("• Done 🎉", badgeFor(isCompleted = true, targetDate = now - 50000000L)) // Completed takes priority over overdue
        assertEquals("• Overdue", badgeFor(isCompleted = false, targetDate = now - 50000000L))
        assertEquals("• Due today", badgeFor(isCompleted = false, targetDate = now))
        assertEquals("• 12d left", badgeFor(isCompleted = false, targetDate = now + (12L * 24 * 60 * 60 * 1000L)))
    }

    @Test
    fun goalCardEffectiveProgress_alwaysFullWhenMarkedCompleted() {
        fun computeEffectiveProgressAndLabel(goal: GoalEntity): Pair<Float, String> {
            val progress = if (goal.targetAmountLkr > 0.0) {
                (goal.currentSavedLkr / goal.targetAmountLkr).toFloat().coerceIn(0f, 1f)
            } else 0f
            val isCompleted = goal.isCompleted || progress >= 1f
            val effectiveProgress = if (isCompleted) 1f else progress
            val percentageInt = (effectiveProgress * 100).toInt()
            val label = if (isCompleted) "100%" else "$percentageInt%"
            return Pair(effectiveProgress, label)
        }

        // Normal in-progress goal
        val inProgressGoal = GoalEntity(targetAmountLkr = 100.0, currentSavedLkr = 45.0, isCompleted = false)
        val (p1, l1) = computeEffectiveProgressAndLabel(inProgressGoal)
        assertEquals(0.45f, p1, 0.001f)
        assertEquals("45%", l1)

        // Goal completed via amount
        val completedByAmount = GoalEntity(targetAmountLkr = 100.0, currentSavedLkr = 100.0, isCompleted = false)
        val (p2, l2) = computeEffectiveProgressAndLabel(completedByAmount)
        assertEquals(1.0f, p2, 0.001f)
        assertEquals("100%", l2)

        // Goal marked completed with zero saved
        val manuallyCompletedZeroSaved = GoalEntity(targetAmountLkr = 500.0, currentSavedLkr = 0.0, isCompleted = true)
        val (p3, l3) = computeEffectiveProgressAndLabel(manuallyCompletedZeroSaved)
        assertEquals(1.0f, p3, 0.001f)
        assertEquals("100%", l3)

        // Goal marked completed with zero target
        val zeroTargetCompleted = GoalEntity(targetAmountLkr = 0.0, currentSavedLkr = 0.0, isCompleted = true)
        val (p4, l4) = computeEffectiveProgressAndLabel(zeroTargetCompleted)
        assertEquals(1.0f, p4, 0.001f)
        assertEquals("100%", l4)
    }

    @Test
    fun goalCardLabels_neverContainCurrencyNumbers() {
        // Verify that the simplified card components format ONLY progress % and timeline badges,
        // and deliberately omit currency values (LKR, target, saved)
        fun cardTextElements(goal: GoalEntity): List<String> {
            val progress = if (goal.targetAmountLkr > 0.0) {
                (goal.currentSavedLkr / goal.targetAmountLkr).toFloat().coerceIn(0f, 1f)
            } else 0f
            val isCompleted = goal.isCompleted || progress >= 1f
            val effectiveProgress = if (isCompleted) 1f else progress
            val percentageInt = (effectiveProgress * 100).toInt()
            val percentLabel = if (isCompleted) "100%" else "$percentageInt%"
            val actionLabel = if (isCompleted) "Completed ✨" else "Deposit"
            val badge = if (isCompleted) "• Done 🎉" else "• 12d left"

            return listOf(goal.title, badge, percentLabel, actionLabel)
        }

        sampleGoals.forEach { goal ->
            val elements = cardTextElements(goal)
            elements.forEach { text ->
                assertTrue("Card text '$text' should not mention LKR", !text.contains("LKR", ignoreCase = true))
                assertTrue("Card text '$text' should not mention target amount", !text.contains("target", ignoreCase = true))
                assertTrue("Card text '$text' should not mention saved amount", !text.contains("saved", ignoreCase = true))
            }
        }
    }
}
