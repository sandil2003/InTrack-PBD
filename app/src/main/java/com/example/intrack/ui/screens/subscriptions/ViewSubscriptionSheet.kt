package com.example.intrack.ui.screens.subscriptions

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LiveTv
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.intrack.data.model.SubscriptionEntity
import com.example.intrack.ui.theme.PlusJakartaSansFontFamily
import com.example.intrack.util.CurrencyFormatter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Full screen version of "View subscription" matching Figma frame 2012:618.
 */
@Composable
fun ViewSubscriptionScreen(
    subscription: SubscriptionEntity,
    onNavigateBack: () -> Unit,
    onEditSubscription: (SubscriptionEntity) -> Unit,
    onDeleteSubscription: (SubscriptionEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F3EF))
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp)
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(Color.White)
        ) {
            ViewSubscriptionContent(
                subscription = subscription,
                onDismiss = onNavigateBack,
                onEdit = { onEditSubscription(subscription) },
                onDelete = { onDeleteSubscription(subscription) }
            )
        }
    }
}

/**
 * Modal Bottom Sheet version of "View subscription" matching Figma frame 2012:685.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewSubscriptionBottomSheet(
    subscription: SubscriptionEntity,
    onDismiss: () -> Unit,
    onEdit: (SubscriptionEntity) -> Unit,
    onDelete: (SubscriptionEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        containerColor = Color.White,
        dragHandle = null,
        modifier = modifier.imePadding()
    ) {
        ViewSubscriptionContent(
            subscription = subscription,
            onDismiss = onDismiss,
            onEdit = { onEdit(subscription) },
            onDelete = { onDelete(subscription) }
        )
    }
}

/**
 * Complete UI and UX matching Figma node 2012:685 ("View subscription").
 */
@Composable
fun ViewSubscriptionContent(
    subscription: SubscriptionEntity,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categoryIcon: ImageVector = when (subscription.category.uppercase()) {
        "ENTERTAINMENT" -> Icons.Rounded.LiveTv
        "HOUSING" -> Icons.Rounded.Home
        "HEALTH" -> Icons.Rounded.FitnessCenter
        "TECH" -> Icons.Rounded.Devices
        else -> when {
            subscription.name.contains("gym", ignoreCase = true) || subscription.name.contains("fitness", ignoreCase = true) -> Icons.Rounded.FitnessCenter
            subscription.name.contains("rent", ignoreCase = true) || subscription.name.contains("house", ignoreCase = true) -> Icons.Rounded.Home
            subscription.name.contains("stream", ignoreCase = true) || subscription.name.contains("netflix", ignoreCase = true) -> Icons.Rounded.LiveTv
            else -> Icons.Rounded.Category
        }
    }

    val categoryDisplayName = when (subscription.category.uppercase()) {
        "ENTERTAINMENT" -> "Entertainment"
        "HOUSING" -> "Housing"
        "HEALTH" -> "Health & Wellness"
        "TECH" -> "Software & Tech"
        else -> when {
            subscription.name.contains("gym", ignoreCase = true) -> "Health & Wellness"
            subscription.name.contains("rent", ignoreCase = true) -> "Housing & Utilities"
            else -> subscription.category.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
        }
    }

    val paymentSourceLabel = when (subscription.paymentAccount.uppercase()) {
        "CARD_PRIMARY" -> "Card Primary (••4242)"
        "ACCOUNT_SECONDARY" -> "Secondary Current (••8890)"
        "CASH" -> "Cash Wallet"
        else -> subscription.paymentAccount.replace("_", " ")
    }

    val billingDayString = remember(subscription.nextBillingMillis) {
        val cal = Calendar.getInstance().apply { timeInMillis = subscription.nextBillingMillis }
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val suffix = when (day) {
            1, 21, 31 -> "st"
            2, 22 -> "nd"
            3, 23 -> "rd"
            else -> "th"
        }
        "$day$suffix of month"
    }

    val cycleLabel = when (subscription.billingCycle.uppercase()) {
        "YEARLY", "ANNUAL" -> "year"
        "WEEKLY" -> "week"
        "QUARTERLY" -> "quarter"
        else -> "month"
    }

    val totalPaid = if (subscription.totalPaidLkr > 0) {
        subscription.totalPaidLkr
    } else {
        subscription.amountLkr * subscription.billingCyclesCount.coerceAtLeast(1)
    }

    val haptics = LocalHapticFeedback.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
    ) {
        // Top Swipe Drag Handle Pill (48x6dp, #E4E2DE)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(22.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .width(48.dp)
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE4E2DE))
            )
        }

        // Top Navigation Row: Category Chip Indicator + Close Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Chip Indicator (Pill, fill #F5DED4, border 1dp #E3BEB6)
            Row(
                modifier = Modifier
                    .height(28.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF5DED4))
                    .border(1.dp, Color(0xFFE3BEB6), CircleShape)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = categoryIcon,
                    contentDescription = null,
                    tint = Color(0xFFB52705),
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = categoryDisplayName,
                    fontFamily = PlusJakartaSansFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF5B0D00)
                )
            }

            // Close modal button (36x36dp circle, fill #F5F3EF)
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF5F3EF))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onDismiss() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Close",
                    tint = Color(0xFF585F6B),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // SECTION 1: Subscription Title & Provider Info
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Rounded square badge (56x56dp, fill #F5DED4, border 1dp #E3BEB6, cr 16dp)
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF5DED4).copy(alpha = 0.7f))
                    .border(1.dp, Color(0xFFE3BEB6), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = categoryIcon,
                    contentDescription = subscription.name,
                    tint = Color(0xFFB52705),
                    modifier = Modifier.size(26.dp)
                )
            }

            Text(
                text = subscription.name,
                fontFamily = PlusJakartaSansFontFamily,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B1C1A),
                maxLines = 2
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SECTION 2: Billing & Commitment Highlight Card
        // Background #F5F3EF (alpha 0.7), border 1dp #E3BEB6, cr 16dp, padding 16dp
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFF5F3EF).copy(alpha = 0.7f))
                .border(1.dp, Color(0xFFE3BEB6), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recurring Billing Amount",
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF585F6B)
                    )

                    Text(
                        text = "Fixed Commitment",
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFB52705)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "LKR",
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B1C1A),
                        modifier = Modifier.padding(bottom = 3.dp)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = String.format(Locale.US, "%,.2f", subscription.amountLkr),
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B1C1A)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "/ $cycleLabel",
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF585F6B),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SECTION 3: Detailed Subscription Information List Card
        // Background #FFFFFF, border 1dp #E3BEB6, cr 16dp
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFE3BEB6), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Row 1: Frequency
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF5F3EF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CalendarToday,
                                contentDescription = null,
                                tint = Color(0xFF585F6B),
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Billing Frequency",
                                fontFamily = PlusJakartaSansFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color(0xFF585F6B)
                            )
                            Text(
                                text = subscription.billingCycle.lowercase().replaceFirstChar { it.uppercase() },
                                fontFamily = PlusJakartaSansFontFamily,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1B1C1A)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFF5F3EF))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = billingDayString,
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF585F6B)
                        )
                    }
                }

                HorizontalDivider(color = Color(0xFFE3BEB6).copy(alpha = 0.5f), thickness = 0.8.dp)

                // Row 2: Payment Source
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF5F3EF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (subscription.paymentAccount == "CARD_PRIMARY") Icons.Rounded.CreditCard else Icons.Rounded.AccountBalance,
                                contentDescription = null,
                                tint = Color(0xFF585F6B),
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Payment Source",
                                fontFamily = PlusJakartaSansFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color(0xFF585F6B)
                            )
                            Text(
                                text = paymentSourceLabel,
                                fontFamily = PlusJakartaSansFontFamily,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1B1C1A)
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFB52705))
                        )
                        Text(
                            text = "Verified",
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF585F6B)
                        )
                    }
                }

                HorizontalDivider(color = Color(0xFFE3BEB6).copy(alpha = 0.5f), thickness = 0.8.dp)

                // Row 3: Auto-Debit Status
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF5F3EF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Sync,
                                contentDescription = null,
                                tint = Color(0xFF585F6B),
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Auto-Debit Safeguard",
                                fontFamily = PlusJakartaSansFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color(0xFF585F6B)
                            )
                            Text(
                                text = if (subscription.isAutoPayment) "Enabled" else "Manual",
                                fontFamily = PlusJakartaSansFontFamily,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1B1C1A)
                            )
                        }
                    }

                    Text(
                        text = if (subscription.isAutoPayment) "72h advance reminder active" else "Reminders only",
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF585F6B)
                    )
                }

                HorizontalDivider(color = Color(0xFFE3BEB6).copy(alpha = 0.5f), thickness = 0.8.dp)

                // Row 4: Category
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF5F3EF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = categoryIcon,
                                contentDescription = null,
                                tint = Color(0xFF585F6B),
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Expense Category",
                                fontFamily = PlusJakartaSansFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color(0xFF585F6B)
                            )
                            Text(
                                text = categoryDisplayName,
                                fontFamily = PlusJakartaSansFontFamily,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1B1C1A)
                            )
                        }
                    }

                    Text(
                        text = "Lifestyle / Personal",
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF585F6B)
                    )
                }

                HorizontalDivider(color = Color(0xFFE3BEB6).copy(alpha = 0.5f), thickness = 0.8.dp)

                // Row 5: Total Paid to Date
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF5F3EF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ReceiptLong,
                                contentDescription = null,
                                tint = Color(0xFF585F6B),
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Total Paid to Date",
                                fontFamily = PlusJakartaSansFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color(0xFF585F6B)
                            )
                            Text(
                                text = "LKR " + String.format(Locale.US, "%,.2f", totalPaid),
                                fontFamily = PlusJakartaSansFontFamily,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1B1C1A)
                            )
                        }
                    }

                    Text(
                        text = "${subscription.billingCyclesCount.coerceAtLeast(1)} billing cycles",
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF585F6B)
                    )
                }

                // Row 6: Terms & Notice (background #F5F3EF)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF5F3EF).copy(alpha = 0.4f))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Info,
                            contentDescription = "Notice",
                            tint = Color(0xFF585F6B),
                            modifier = Modifier
                                .size(16.dp)
                                .padding(top = 2.dp)
                        )
                        Text(
                            text = "Cancellation terms: Cancel anytime before renewal date without cancellation fees. Standing auto-debit terminates immediately upon cancellation.",
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color(0xFF585F6B),
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION 4: Contextual Actions Bottom Group
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Primary Edit Subscription Button (Terracotta Pill, fill #FF5C38)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFF5C38))
                    .clickable {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onEdit()
                    },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Edit Subscription",
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Destructive Cancel & Delete Subscription Button (Outlined White Pill, text #B52705)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE3BEB6), CircleShape)
                    .clickable {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onDelete()
                    },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = null,
                        tint = Color(0xFFB52705),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Cancel & Delete Subscription",
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFB52705)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
