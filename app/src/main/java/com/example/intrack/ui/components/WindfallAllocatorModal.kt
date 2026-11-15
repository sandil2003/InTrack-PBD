package com.example.intrack.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.intrack.ui.theme.BrandMidnightNavy
import com.example.intrack.ui.theme.BrandSoftSlate
import com.example.intrack.ui.theme.InTrackTheme
import com.example.intrack.util.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WindfallAllocatorModal(
    windfallAmountLkr: Double,
    onConfirmAllocation: (allocatedLkr: Double) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var allocationPercentage by remember { mutableFloatStateOf(0.5f) }

    val allocatedLkr = windfallAmountLkr * allocationPercentage
    val remainingToSpendLkr = windfallAmountLkr - allocatedLkr
    val navyText = BrandMidnightNavy

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Savings,
                    contentDescription = null,
                    tint = navyText,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Allocate Windfall",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = navyText
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BrandSoftSlate),
                border = BorderStroke(0.dp, Color.Transparent),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${(allocationPercentage * 100).toInt()}% to Goal",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = navyText
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = CurrencyFormatter.formatLkr(allocatedLkr),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = navyText
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Slider(
                value = allocationPercentage,
                onValueChange = { allocationPercentage = it },
                valueRange = 0.1f..1.0f,
                steps = 8,
                colors = SliderDefaults.colors(
                    thumbColor = navyText,
                    activeTrackColor = navyText,
                    inactiveTrackColor = navyText.copy(alpha = 0.2f)
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("10%", style = MaterialTheme.typography.labelSmall, color = navyText)
                Text("50%", style = MaterialTheme.typography.labelSmall, color = navyText, fontWeight = FontWeight.SemiBold)
                Text("100%", style = MaterialTheme.typography.labelSmall, color = navyText)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(navyText.copy(alpha = 0.08f))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Remaining cash",
                    style = MaterialTheme.typography.bodyMedium,
                    color = navyText
                )
                Text(
                    text = CurrencyFormatter.formatLkr(remainingToSpendLkr),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = navyText
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (allocatedLkr > 0) {
                        onConfirmAllocation(allocatedLkr)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = navyText),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    text = "Allocate ${CurrencyFormatter.formatLkr(allocatedLkr)}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Dismiss", color = navyText, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Preview(name = "Windfall Allocator Modal Light", showBackground = true)
@Preview(name = "Windfall Allocator Modal Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun WindfallAllocatorModalPreview() {
    InTrackTheme {
        Surface {
            WindfallAllocatorModal(
                windfallAmountLkr = 50000.0,
                onConfirmAllocation = {},
                onDismiss = {}
            )
        }
    }
}
