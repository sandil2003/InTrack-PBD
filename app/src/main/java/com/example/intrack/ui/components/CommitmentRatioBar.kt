package com.example.intrack.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.intrack.util.CurrencyFormatter

@Composable
fun CommitmentRatioBar(
    committedLkr: Double,
    discretionaryLkr: Double,
    modifier: Modifier = Modifier
) {
    val total = committedLkr + discretionaryLkr
    val committedRatio = if (total > 0) (committedLkr / total).toFloat().coerceIn(0.01f, 0.99f) else 0.5f
    val discretionaryRatio = if (total > 0) (discretionaryLkr / total).toFloat().coerceIn(0.01f, 0.99f) else 0.5f

    val committedPct = if (total > 0) ((committedLkr / total) * 100).toInt() else 0
    val discretionaryPct = if (total > 0) (100 - committedPct) else 0

    val navyText = Color(0xFF121660)

    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = Color(0xFFFFD13E)),
        border = BorderStroke(0.dp, Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Two-tone continuous segmented bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(navyText.copy(alpha = 0.2f))
            ) {
                if (total > 0) {
                    Box(
                        modifier = Modifier
                            .weight(committedRatio)
                            .height(10.dp)
                            .background(navyText)
                    )
                    Box(
                        modifier = Modifier
                            .weight(discretionaryRatio)
                            .height(10.dp)
                            .background(navyText.copy(alpha = 0.5f))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .background(navyText.copy(alpha = 0.2f))
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(navyText)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Essentials ($committedPct%)",
                            style = MaterialTheme.typography.labelSmall,
                            color = navyText
                        )
                        Text(
                            text = CurrencyFormatter.formatLkr(committedLkr),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = navyText
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(navyText.copy(alpha = 0.5f))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Lifestyle ($discretionaryPct%)",
                            style = MaterialTheme.typography.labelSmall,
                            color = navyText
                        )
                        Text(
                            text = CurrencyFormatter.formatLkr(discretionaryLkr),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = navyText
                        )
                    }
                }
            }
        }
    }
}
