package com.example.intrack.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.BeachAccess
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Flight
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LaptopMac
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.TrackChanges
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.intrack.data.model.GoalEntity
import com.example.intrack.ui.theme.PrimaryGreen
import com.example.intrack.util.DateUtils
import java.util.Locale

@Composable
fun GoalSquareCard(
    goal: GoalEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = if (goal.targetAmountLkr > 0) {
        (goal.currentSavedLkr / goal.targetAmountLkr).toFloat().coerceIn(0f, 1f)
    } else 0f

    val isCompleted = goal.isCompleted || progress >= 1f
    val daysLeft = DateUtils.daysBetween(System.currentTimeMillis(), goal.targetDateMillis).coerceAtLeast(0L)
    val percentageInt = (progress * 100).toInt()
    val timeFormatted = formatTimeRemaining(daysLeft, isCompleted)

    val (icon, iconTint) = getGoalIcon(goal.title)

    OutlinedCard(
        modifier = modifier
            .width(148.dp)
            .height(152.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Row: Bare larger icon (no bg box)
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(32.dp)
            )

            // Goal Title
            Text(
                text = goal.title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontSize = 13.sp,
                    lineHeight = 16.5.sp
                ),
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Percentage Display aligned to bottom right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "$percentageInt%",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = PrimaryGreen
                )
            }
        }
    }
}

@Composable
fun AddGoalSquareCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        modifier = modifier
            .width(135.dp)
            .height(152.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = "Add Goal",
                tint = PrimaryGreen,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "New Goal",
                style = MaterialTheme.typography.titleSmall.copy(fontSize = 13.sp),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "Set a target",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Formats days remaining into months and days if longer than 30 days.
 * Examples:
 * - 365 days -> "12mo 5d"
 * - 60 days  -> "2mo"
 * - 45 days  -> "1mo 15d"
 * - 24 days  -> "24d"
 * - isCompleted -> "Done"
 */
private fun formatTimeRemaining(daysLeft: Long, isCompleted: Boolean): String {
    if (isCompleted) return "Done"
    if (daysLeft <= 0) return "Due"
    if (daysLeft <= 30) return "${daysLeft}d"

    val months = daysLeft / 30
    val days = daysLeft % 30

    return if (days > 0) {
        "${months}mo ${days}d"
    } else {
        "${months}mo"
    }
}

private fun getGoalIcon(title: String): Pair<ImageVector, Color> {
    val lower = title.lowercase(Locale.ROOT)
    val icon = when {
        lower.contains("macbook") || lower.contains("laptop") || lower.contains("pc") || lower.contains("tech") ->
            Icons.Rounded.LaptopMac
        lower.contains("car") || lower.contains("bike") || lower.contains("vehicle") || lower.contains("transport") ->
            Icons.Rounded.DirectionsCar
        lower.contains("emergency") || lower.contains("shield") || lower.contains("buffer") ->
            Icons.Rounded.Shield
        lower.contains("trip") || lower.contains("travel") || lower.contains("vacation") || lower.contains("holiday") ->
            Icons.Rounded.Flight
        lower.contains("home") || lower.contains("house") || lower.contains("rent") ->
            Icons.Rounded.Home
        lower.contains("study") || lower.contains("course") || lower.contains("education") || lower.contains("degree") ->
            Icons.Rounded.School
        lower.contains("savings") || lower.contains("fund") ->
            Icons.Rounded.Savings
        else ->
            Icons.Rounded.Flag
    }
    return Pair(icon, PrimaryGreen)
}
