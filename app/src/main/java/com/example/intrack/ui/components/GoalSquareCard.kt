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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Flight
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LaptopMac
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Shield
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
import com.example.intrack.ui.theme.BrandSoftSlate
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

    val percentageInt = (progress * 100).toInt()
    val (icon, _) = getGoalIcon(goal.title)
    val navyText = Color(0xFF121660)

    OutlinedCard(
        modifier = modifier
            .width(148.dp)
            .height(118.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = BrandSoftSlate),
        border = BorderStroke(0.dp, Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Row: Icon in White Circle Badge + Percentage on Top Right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = navyText,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Text(
                    text = "$percentageInt%",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = navyText
                )
            }

            // Goal Title
            Text(
                text = goal.title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontSize = 13.5.sp,
                    lineHeight = 17.sp
                ),
                fontWeight = FontWeight.Bold,
                color = navyText,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun AddGoalSquareCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val navyText = Color(0xFF121660)

    OutlinedCard(
        modifier = modifier
            .width(135.dp)
            .height(118.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = BrandSoftSlate),
        border = BorderStroke(0.dp, Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Add Goal",
                    tint = navyText,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "New Goal",
                style = MaterialTheme.typography.titleSmall.copy(fontSize = 13.5.sp),
                fontWeight = FontWeight.Bold,
                color = navyText
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "Set a target",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                color = navyText.copy(alpha = 0.7f)
            )
        }
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
    return Pair(icon, Color(0xFF121660))
}
