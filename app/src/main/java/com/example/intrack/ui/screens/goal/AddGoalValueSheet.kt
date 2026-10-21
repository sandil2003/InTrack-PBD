package com.example.intrack.ui.screens.goal

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CurrencyBitcoin
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.LaptopMac
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.intrack.data.model.GoalEntity
import com.example.intrack.ui.theme.*
import com.example.intrack.util.CurrencyFormatter

data class IncomeChannelOption(
    val id: String,
    val title: String,
    val availableLkr: Double,
    val note: String,
    val icon: ImageVector
)

data class ContributionRecord(
    val title: String,
    val amountLkr: Double,
    val icon: ImageVector
)

/**
 * Add a goal value (Deposit Modal Bottom Sheet)
 * Exactly matching Figma Node 2026:1549:
 * - Grab handle & Sheet Header: "Deposit to [Goal Title]", Subtitle, Close Button
 * - 1. Amount Input Display & Quick Increment Pill Chips (+5,000, +15,000, +25,000, Sweep Full)
 * - 2. Source-Linked Income Channel Selector (4 sources with active radio check)
 * - 3. Smart Rule Toggle: Freelance Windfall Sweep
 * - 4. Recent Contribution History Micro-Feed
 * - 5. Confirm Deposit CTA Button
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGoalValueSheet(
    goal: GoalEntity,
    onDismiss: () -> Unit,
    onConfirmDeposit: (amountLkr: Double, sourceId: String, autoSweep: Boolean) -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    var depositAmount by remember { mutableDoubleStateOf(25000.0) }
    var rawInputText by remember { mutableStateOf("25000.00") }
    var selectedSourceId by remember { mutableStateOf("freelance_milestone") }
    var autoSweepEnabled by remember { mutableStateOf(true) }

    val remainingToTarget = (goal.targetAmountLkr - goal.currentSavedLkr).coerceAtLeast(0.0)

    val incomeChannels = remember {
        listOf(
            IncomeChannelOption(
                id = "freelance_milestone",
                title = "Freelance Milestone Payout",
                availableLkr = 68500.0,
                note = "React SME",
                icon = Icons.Rounded.LaptopMac
            ),
            IncomeChannelOption(
                id = "monthly_salary",
                title = "Monthly Salary (Baseline)",
                availableLkr = 145000.0,
                note = "Primary",
                icon = Icons.Rounded.AccountBalance
            ),
            IncomeChannelOption(
                id = "adsense_usd",
                title = "AdSense USD Transfer",
                availableLkr = 28400.0,
                note = "$92.50",
                icon = Icons.Rounded.Language
            ),
            IncomeChannelOption(
                id = "crypto_profit",
                title = "Crypto / Investment Profit",
                availableLkr = 42000.0,
                note = "Binance P2P",
                icon = Icons.Rounded.CurrencyBitcoin
            )
        )
    }

    val recentContributions = remember {
        listOf(
            ContributionRecord("From React SME Project", 15000.0, Icons.Rounded.LaptopMac),
            ContributionRecord("From Monthly Salary Sweep", 10000.0, Icons.Rounded.AccountBalance),
            ContributionRecord("From AdSense Payout", 6200.0, Icons.Rounded.Language)
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 4.dp)
                    .size(width = 40.dp, height = 4.5.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color(0xFFE4E2DE))
            )
        },
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // --- Sheet Header (Figma node 2026:1600) ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(FigmaSoftPeach),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Savings,
                            contentDescription = null,
                            tint = FigmaTerracotta,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Deposit to ${goal.title}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = FigmaDarkText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Allocate regular savings or sweep freelance windfalls",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal
                            ),
                            color = FigmaMutedSlate,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(FigmaBg)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close",
                        tint = FigmaMutedSlate,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            HorizontalDivider(thickness = 0.8.dp, color = FigmaBorder)

            // --- 1. Amount Input Display & Quick Chips (Figma node 2026:1613) ---
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "ENTER DEPOSIT SUM",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp
                    ),
                    color = FigmaMutedSlate
                )

                // Large Amount Input Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(FigmaBg)
                        .border(1.dp, FigmaBorder, RoundedCornerShape(18.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "LKR",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = FigmaTerracotta,
                            modifier = Modifier.padding(end = 8.dp)
                        )

                        OutlinedTextField(
                            value = rawInputText,
                            onValueChange = { input ->
                                rawInputText = input
                                depositAmount = input.toDoubleOrNull() ?: 0.0
                            },
                            textStyle = MaterialTheme.typography.headlineMedium.copy(
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = FigmaDarkText
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            ),
                            modifier = Modifier.fillMaxWidth(0.85f)
                        )
                    }
                }

                // Quick Increment Pill Chips (Figma node 2026:1622)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(5000.0, 15000.0, 25000.0).forEach { inc ->
                        val formattedInc = "+${inc.toInt()}"
                        val isSelected = depositAmount == inc
                        InTrackFilterPill(
                            label = formattedInc,
                            isSelected = isSelected,
                            onClick = {
                                depositAmount = inc
                                rawInputText = String.format(java.util.Locale.US, "%.2f", inc)
                            }
                        )
                    }

                    // "Sweep Full" button chip
                    val isSweepSelected = depositAmount == remainingToTarget && remainingToTarget > 0
                    InTrackFilterPill(
                        label = "Sweep Full",
                        isSelected = isSweepSelected,
                        onClick = {
                            if (remainingToTarget > 0) {
                                depositAmount = remainingToTarget
                                rawInputText = String.format(java.util.Locale.US, "%.2f", remainingToTarget)
                            }
                        }
                    )
                }
            }

            // --- 2. Source-Linked Income Channel Selector (Figma node 2026:1633) ---
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SELECT SOURCE / INCOME CHANNEL",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = FigmaMutedSlate
                    )

                    InTrackStatusBadge(
                        text = "${incomeChannels.size} Sources",
                        containerColor = FigmaSoftPeach,
                        contentColor = FigmaTerracottaOrange,
                        fontSize = 10.5.sp
                    )
                }

                Text(
                    text = "Tagging your side-hustles accelerates goal momentum and tracks revenue origins.",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = FigmaMutedSlate
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Source Options Stack
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    incomeChannels.forEach { channel ->
                        val isSelected = selectedSourceId == channel.id
                        val cardBorder = if (isSelected) FigmaTerracotta else FigmaBorder
                        val cardBg = if (isSelected) Color.White else FigmaBg

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(cardBg)
                                .border(if (isSelected) 1.5.dp else 1.dp, cardBorder, RoundedCornerShape(16.dp))
                            .clickable { selectedSourceId = channel.id }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) FigmaSoftPeach else Color(0xFFEFEEEA)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = channel.icon,
                                        contentDescription = null,
                                        tint = if (isSelected) FigmaTerracotta else FigmaMutedSlate,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = channel.title,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontSize = 14.sp,
                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                                        ),
                                        color = FigmaDarkText,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "Available: ${CurrencyFormatter.formatLkr(channel.availableLkr)} • ${channel.note}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                                        color = FigmaMutedSlate,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(FigmaTerracotta),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = "Selected",
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .border(1.2.dp, FigmaBorder, CircleShape)
                                )
                            }
                        }
                    }
                }
            }

            // --- 3. Smart Rule Toggle: Freelance Windfall Sweep (Figma node 2026:1690) ---
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = FigmaSoftPeach),
                border = BorderStroke(1.dp, FigmaPeachBar.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = FigmaTerracotta,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Auto-Sweep Windfall Rule",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = FigmaDarkText
                            )

                            Switch(
                                checked = autoSweepEnabled,
                                onCheckedChange = { autoSweepEnabled = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = FigmaTerracotta,
                                    uncheckedThumbColor = FigmaMutedSlate,
                                    uncheckedTrackColor = Color(0xFFEFEEEA)
                                ),
                                modifier = Modifier.height(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Automatically route 30% of upcoming freelance milestone payments to ${goal.title}.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.5.sp,
                                lineHeight = 16.sp
                            ),
                            color = FigmaNoteBrown
                        )
                    }
                }
            }

            // --- 4. Recent Contribution History Micro-Feed (Figma node 2026:1704) ---
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RECENT CONTRIBUTIONS (${recentContributions.size})",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = FigmaMutedSlate
                    )

                    Text(
                        text = "View All",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = FigmaTerracottaOrange,
                        modifier = Modifier.clickable { /* View details */ }
                    )
                }

                recentContributions.forEach { record ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(FigmaBg)
                            .border(1.dp, FigmaBorder, RoundedCornerShape(14.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = record.icon,
                                    contentDescription = null,
                                    tint = FigmaTerracotta,
                                    modifier = Modifier.size(15.dp)
                                )
                            }

                            Text(
                                text = record.title,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = FigmaDarkText
                            )
                        }

                        Text(
                            text = "+ ${CurrencyFormatter.formatLkr(record.amountLkr)}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = FigmaTerracottaOrange
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // --- 5. Confirm Deposit CTA Button ---
            Button(
                onClick = {
                    if (depositAmount > 0) {
                        onConfirmDeposit(depositAmount, selectedSourceId, autoSweepEnabled)
                    }
                },
                enabled = depositAmount > 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FigmaTerracotta,
                    disabledContainerColor = FigmaTerracotta.copy(alpha = 0.5f)
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp)
            ) {
                Text(
                    text = "Confirm Deposit • ${CurrencyFormatter.formatLkr(depositAmount)}",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )
            }
        }
    }
}
