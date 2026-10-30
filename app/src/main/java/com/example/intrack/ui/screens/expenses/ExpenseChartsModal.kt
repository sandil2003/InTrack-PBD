package com.example.intrack.ui.screens.expenses

import java.util.Locale
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.intrack.util.CurrencyFormatter

/**
   Figma-accurate Expense Breakdown Modal Card
   Displays Donut Chart & Hero Metric Card and Detailed Category Distribution Cards.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ExpenseChartsModal(
    monthLabel: String,
    totalExpenseLkr: Double,
    committedLkr: Double,
    discretionaryLkr: Double,
    cardPrimaryTotal: Double = 0.0,
    secondaryAccountTotal: Double = 0.0,
    cashTotal: Double = 0.0,
    categoryItems: List<CategorySpendingItem>,
    onDismiss: () -> Unit,
    onSelectCategory: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Palette synchronized with Figma InTrack theme
    val figmaScreenBg = Color(0xFFFFFFFF)
    val figmaBorder = Color(0x4DE3BEB6)
    val figmaTerracotta = Color(0xFFB52705)
    val figmaTrackBg = Color(0xFFE4E2DE)
    val figmaDarkText = Color(0xFF1B1C1A)
    val figmaMutedBrown = Color(0xFF6B5B53)
    val figmaMutedSlate = Color(0xFF585F6B)
    val figmaDonutBase = Color(0xFFEFEEEA)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = figmaScreenBg,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header: "Expense Breakdown" + Close Button (Figma node 2002:243)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Expense Breakdown",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    ),
                    color = figmaDarkText
                )

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        modifier = Modifier.size(20.dp),
                        tint = figmaDarkText
                    )
                }
            }

            // Section 1: Donut Chart & Hero Metric Card (Figma node 2002:256)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, figmaBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Upper Row: "OUTFLOW & MIX" + Budget Health Indicator Pill (Figma node 2002:258)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween, // Keeps layout rule for other elements
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "OUTFLOW & MIX",
                            modifier = Modifier.weight(1f), // Forces the text box to fill the Row's width
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.55.sp,
                                textAlign = TextAlign.Center // Centers the text characters inside that filled width
                            ),
                            color = figmaMutedBrown
                        )
                    }

                    // SVG Donut Chart with Centered Hero Stat
                    Box(
                        modifier = Modifier.size(210.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val strokeWidth = 22.dp.toPx()
                            val diameter = size.minDimension - strokeWidth
                            val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
                            val arcSize = Size(diameter, diameter)

                            // Background circular track
                            drawArc(
                                color = figmaDonutBase,
                                startAngle = 0f,
                                sweepAngle = 360f,
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcSize,
                                style = Stroke(width = strokeWidth)
                            )

                            // Multi-segment category arcs
                            if (totalExpenseLkr > 0 && categoryItems.isNotEmpty()) {
                                var startAngle = -90f
                                val totalPercentages = categoryItems.sumOf { it.percentage.toDouble() }
                                    .toFloat()
                                    .coerceAtLeast(1f)
                                val gapAngle = if (categoryItems.size > 1) 2.5f else 0f
                                val totalGaps = categoryItems.size * gapAngle
                                val availableAngle = 360f - totalGaps

                                categoryItems.forEach { item ->
                                    val sweep = ((item.percentage / totalPercentages) * availableAngle)
                                        .coerceAtLeast(1.5f)
                                    drawArc(
                                        color = item.color,
                                        startAngle = startAngle,
                                        sweepAngle = sweep,
                                        useCenter = false,
                                        topLeft = topLeft,
                                        size = arcSize,
                                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                    )
                                    startAngle += sweep + gapAngle
                                }
                            }
                        }

                        // Centered Hero Metric
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "TOTAL SPENT",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 0.5.sp
                                ),
                                color = figmaMutedBrown
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "LKR",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = figmaTerracotta,
                                    modifier = Modifier.padding(bottom = 2.dp)
                                )
                                Text(
                                    text = CurrencyFormatter.formatLkr(totalExpenseLkr).replace("LKR ", ""),
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = (-0.5).sp
                                    ),
                                    color = figmaDarkText
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = monthLabel,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = figmaMutedSlate
                            )
                        }
                    }

                    // Interactive Compact Legend Pills
                    if (categoryItems.isNotEmpty()) {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            categoryItems.forEach { item ->
                                Row(
                                    modifier = Modifier
                                        .padding(horizontal = 6.dp)
                                        .clickable {
                                            onSelectCategory?.invoke(item.category)
                                            onDismiss()
                                        },
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(item.color)
                                    )
                                    Text(
                                        text = item.displayName.split(" ", "&").firstOrNull() ?: item.displayName,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                        color = figmaMutedBrown
                                    )
                                    Text(
                                        text = "${item.percentage.toInt()}%",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = figmaDarkText
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Section 2: Category Distribution Detailed Cards
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Section Header Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CATEGORY DISTRIBUTION",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.55.sp
                        ),
                        color = figmaMutedBrown
                    )

                    Text(
                        text = "${categoryItems.size} Categories",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = figmaMutedSlate
                    )
                }

                // Cards for each category
                categoryItems.forEach { item ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, figmaBorder),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelectCategory?.invoke(item.category)
                                onDismiss()
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Left: Category icon circle + Title / Percentage badge + Subtitle
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(item.color.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.displayName,
                                        tint = item.color,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = item.displayName,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.SemiBold
                                            ),
                                            color = figmaDarkText,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    // Subtitle: Transaction count & nature
                                    Text(
                                        text = "${item.transactionCount} transactions",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                        color = figmaMutedSlate
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            // Right: Amount + Mini proportional progress bar
                            Column(
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "LKR ${CurrencyFormatter.formatLkr(item.amountLkr).replace("LKR ", "")}",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = (-0.3).sp
                                    ),
                                    color = figmaDarkText
                                )

                                // 64dp mini progress bar matching
                                Box(
                                    modifier = Modifier
                                        .width(64.dp)
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(9999.dp))
                                        .background(figmaTrackBg)
                                ) {
                                    val barRatio = (item.percentage / 100f).coerceIn(0.04f, 1f)
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(barRatio)
                                            .clip(RoundedCornerShape(9999.dp))
                                            .background(item.color)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Spacer ensuring the last card is fully visible with comfortable clearance
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}
