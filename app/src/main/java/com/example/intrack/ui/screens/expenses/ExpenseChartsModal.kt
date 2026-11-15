package com.example.intrack.ui.screens.expenses

import android.content.res.Configuration
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.intrack.ui.theme.BrandAzureBlue
import com.example.intrack.ui.theme.BrandEmeraldGreen
import com.example.intrack.ui.theme.BrandMidnightNavy
import com.example.intrack.ui.theme.BrandSoftSlate
import com.example.intrack.ui.theme.BrandWarmYellow
import com.example.intrack.ui.theme.InTrackTheme
import com.example.intrack.util.CurrencyFormatter
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.roundToInt

private data class BreakdownPieSlice(
    val key: String,
    val label: String,
    val amountLkr: Double,
    val percentage: Float,
    val color: Color,
    val icon: ImageVector,
    val countLabel: String
)

@OptIn(ExperimentalMaterial3Api::class)
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
    onSelectCommitmentType: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        ExpenseBreakdownPieContent(
            monthLabel = monthLabel,
            totalExpenseLkr = totalExpenseLkr,
            committedLkr = committedLkr,
            discretionaryLkr = discretionaryLkr,
            categoryItems = categoryItems,
            onDismiss = onDismiss,
            onSelectCategory = onSelectCategory,
            onSelectCommitmentType = onSelectCommitmentType
        )
    }
}

@Composable
internal fun ExpenseBreakdownPieContent(
    monthLabel: String,
    totalExpenseLkr: Double,
    committedLkr: Double,
    discretionaryLkr: Double,
    categoryItems: List<CategorySpendingItem>,
    onDismiss: () -> Unit,
    onSelectCategory: ((String) -> Unit)? = null,
    onSelectCommitmentType: ((String) -> Unit)? = null
) {
    val slices = remember(categoryItems) {
        categoryItems.filter { it.amountLkr > 0.0 && it.percentage > 0f }.map { item ->
            BreakdownPieSlice(
                key = item.category,
                label = item.displayName,
                amountLkr = item.amountLkr,
                percentage = item.percentage,
                color = item.color,
                icon = item.icon,
                countLabel = "${item.transactionCount} ${if (item.transactionCount == 1) "item" else "items"}"
            )
        }
    }

    var focusedKey by remember(slices) { mutableStateOf<String?>(null) }
    val focusedSlice = slices.find { it.key == focusedKey }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ModalBreakdownHeader(
            title = "$monthLabel Breakdown",
            subtitle = "Total ${CurrencyFormatter.formatLkr(totalExpenseLkr)}",
            onDismiss = onDismiss
        )

        if (slices.isEmpty()) {
            EmptyPieBreakdownCard(message = "No expenses in $monthLabel")
        } else {
            InteractivePieChartCard(
                slices = slices,
                focusedKey = focusedKey,
                badgeText = if (focusedSlice != null) {
                    "${focusedSlice.label} · ${focusedSlice.percentage.roundToInt()}% · ${CurrencyFormatter.formatLkr(focusedSlice.amountLkr)}"
                } else {
                    "${slices.size} ${if (slices.size == 1) "category" else "categories"} · Tap slice"
                },
                onSliceTapped = { tappedKey ->
                    focusedKey = if (focusedKey == tappedKey) null else tappedKey
                }
            )

            // Compact Essentials vs Lifestyle split inside the modal
            if (committedLkr > 0.0 || discretionaryLkr > 0.0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CommitmentTierPill(
                        label = "Essentials",
                        amountLkr = committedLkr,
                        dotColor = BrandMidnightNavy,
                        onClick = {
                            onSelectCommitmentType?.invoke("COMMITTED")
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    )
                    CommitmentTierPill(
                        label = "Lifestyle",
                        amountLkr = discretionaryLkr,
                        dotColor = BrandAzureBlue,
                        onClick = {
                            onSelectCommitmentType?.invoke("DISCRETIONARY")
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            SliceBreakdownList(
                slices = slices,
                focusedKey = focusedKey,
                onSliceCardClick = { slice ->
                    if (onSelectCategory != null) {
                        onSelectCategory(slice.key)
                        onDismiss()
                    } else {
                        focusedKey = if (focusedKey == slice.key) null else slice.key
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionBreakdownPieModal(
    totalRecurringLkr: Double,
    secondaryAccountTotal: Double,
    primaryCardTotal: Double,
    cashTotal: Double,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        SubscriptionBreakdownPieContent(
            totalRecurringLkr = totalRecurringLkr,
            secondaryAccountTotal = secondaryAccountTotal,
            primaryCardTotal = primaryCardTotal,
            cashTotal = cashTotal,
            onDismiss = onDismiss
        )
    }
}

@Composable
internal fun SubscriptionBreakdownPieContent(
    totalRecurringLkr: Double,
    secondaryAccountTotal: Double,
    primaryCardTotal: Double,
    cashTotal: Double,
    onDismiss: () -> Unit
) {
    val slices = remember(secondaryAccountTotal, primaryCardTotal, cashTotal) {
        val raw = listOf(
            Triple("ACCOUNT_SECONDARY", "Bank", secondaryAccountTotal),
            Triple("CARD_PRIMARY", "Card", primaryCardTotal),
            Triple("CASH", "Cash", cashTotal)
        ).filter { it.third > 0.0 }
        val total = raw.sumOf { it.third }
        if (total <= 0.0) {
            emptyList()
        } else {
            raw.sortedByDescending { it.third }.map { (key, label, amount) ->
                val (color, icon) = when (key) {
                    "ACCOUNT_SECONDARY" -> BrandMidnightNavy to Icons.Rounded.AccountBalance
                    "CARD_PRIMARY" -> BrandAzureBlue to Icons.Rounded.CreditCard
                    else -> BrandEmeraldGreen to Icons.Rounded.Payments
                }
                BreakdownPieSlice(
                    key = key,
                    label = label,
                    amountLkr = amount,
                    percentage = ((amount / total) * 100.0).toFloat(),
                    color = color,
                    icon = icon,
                    countLabel = "Monthly"
                )
            }
        }
    }

    var focusedKey by remember(slices) { mutableStateOf<String?>(null) }
    val focusedSlice = slices.find { it.key == focusedKey }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ModalBreakdownHeader(
            title = "Subscriptions",
            subtitle = "Total ${CurrencyFormatter.formatLkr(totalRecurringLkr)}",
            onDismiss = onDismiss
        )

        if (slices.isEmpty()) {
            EmptyPieBreakdownCard(message = "No active subscriptions")
        } else {
            InteractivePieChartCard(
                slices = slices,
                focusedKey = focusedKey,
                badgeText = if (focusedSlice != null) {
                    "${focusedSlice.label} · ${focusedSlice.percentage.roundToInt()}% · ${CurrencyFormatter.formatLkr(focusedSlice.amountLkr)}"
                } else {
                    "${slices.size} ${if (slices.size == 1) "source" else "sources"} · Tap slice"
                },
                onSliceTapped = { tappedKey ->
                    focusedKey = if (focusedKey == tappedKey) null else tappedKey
                }
            )

            SliceBreakdownList(
                slices = slices,
                focusedKey = focusedKey,
                onSliceCardClick = { slice ->
                    focusedKey = if (focusedKey == slice.key) null else slice.key
                }
            )
        }
    }
}

@Composable
private fun ModalBreakdownHeader(
    title: String,
    subtitle: String,
    onDismiss: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.4).sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun EmptyPieBreakdownCard(message: String) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Canvas(modifier = Modifier.size(120.dp)) {
                drawCircle(
                    color = Color(0xFFDDE3F0),
                    style = Stroke(width = 16.dp.toPx())
                )
            }
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun InteractivePieChartCard(
    slices: List<BreakdownPieSlice>,
    focusedKey: String?,
    badgeText: String,
    onSliceTapped: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = BrandSoftSlate.copy(alpha = 0.65f)),
        border = BorderStroke(0.dp, Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AnimatedPieChartCanvas(
                slices = slices,
                focusedKey = focusedKey,
                onSliceTapped = onSliceTapped
            )

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = badgeText,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun CommitmentTierPill(
    label: String,
    amountLkr: Double,
    dotColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = BrandSoftSlate),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = BrandMidnightNavy.copy(alpha = 0.72f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = CurrencyFormatter.formatLkr(amountLkr),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = BrandMidnightNavy,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SliceBreakdownList(
    slices: List<BreakdownPieSlice>,
    focusedKey: String?,
    onSliceCardClick: (BreakdownPieSlice) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        slices.forEach { slice ->
            val isFocused = focusedKey == slice.key
            val pct = slice.percentage.roundToInt()

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isFocused) slice.color.copy(alpha = 0.14f) else BrandSoftSlate
                ),
                border = if (isFocused) BorderStroke(1.dp, slice.color) else BorderStroke(0.dp, Color.Transparent),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSliceCardClick(slice) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
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
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = slice.icon,
                                contentDescription = slice.label,
                                tint = BrandMidnightNavy,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(slice.color)
                                )
                                Text(
                                    text = slice.label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandMidnightNavy,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Text(
                                text = slice.countLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = BrandMidnightNavy.copy(alpha = 0.68f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = CurrencyFormatter.formatLkr(slice.amountLkr),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = BrandMidnightNavy,
                            maxLines = 1,
                            softWrap = false
                        )
                        Box(
                            modifier = Modifier
                                .padding(top = 2.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.White)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "$pct%",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                fontWeight = FontWeight.Bold,
                                color = BrandMidnightNavy
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AnimatedPieChartCanvas(
    slices: List<BreakdownPieSlice>,
    focusedKey: String?,
    onSliceTapped: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var playIntro by remember { mutableStateOf(false) }
    LaunchedEffect(slices) { playIntro = true }

    val sweepProgress by animateFloatAsState(
        targetValue = if (playIntro) 1f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "expensePieSweep"
    )

    val sliceAngles = remember(slices) {
        var cursor = -90f
        slices.map { slice ->
            val sweep = (slice.percentage / 100f) * 360f
            val start = cursor
            cursor += sweep
            Triple(slice, start, sweep)
        }
    }

    Canvas(
        modifier = modifier
            .size(184.dp)
            .pointerInput(sliceAngles) {
                detectTapGestures { offset ->
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val dx = offset.x - center.x
                    val dy = offset.y - center.y
                    val radius = minOf(size.width, size.height) / 2f
                    if (hypot(dx, dy) > radius) return@detectTapGestures

                    var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat() + 90f
                    if (angle < 0f) angle += 360f

                    val hit = sliceAngles.firstOrNull { (_, start, sweep) ->
                        val normStart = (start + 90f).let { if (it < 0f) it + 360f else it }
                        angle >= normStart && angle <= normStart + sweep
                    }
                    hit?.first?.let { onSliceTapped(it.key) }
                }
            }
    ) {
        val basePadding = 10.dp.toPx()
        val maxSize = size.minDimension - (basePadding * 2f)
        val separatorWidth = if (slices.size > 1) 2.5.dp.toPx() else 0f

        sliceAngles.forEach { (slice, startAngle, sweepAngle) ->
            val isFocused = focusedKey == slice.key
            val expandOffset = if (isFocused) 6.dp.toPx() else 0f
            val arcDiameter = maxSize + (expandOffset * 2f)
            val topLeft = Offset(
                x = (size.width - arcDiameter) / 2f,
                y = (size.height - arcDiameter) / 2f
            )
            val arcSize = Size(arcDiameter, arcDiameter)
            val animatedSweep = (sweepAngle * sweepProgress).coerceAtLeast(0f)
            val alpha = if (focusedKey == null || isFocused) 1f else 0.42f

            drawArc(
                color = slice.color.copy(alpha = alpha),
                startAngle = startAngle,
                sweepAngle = animatedSweep,
                useCenter = true,
                topLeft = topLeft,
                size = arcSize
            )

            if (separatorWidth > 0f && animatedSweep > 1f) {
                drawArc(
                    color = Color.White,
                    startAngle = startAngle,
                    sweepAngle = animatedSweep,
                    useCenter = true,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = separatorWidth)
                )
            }
        }
    }
}

private fun sampleCategoryItems(): List<CategorySpendingItem> = listOf(
    CategorySpendingItem("RENT", "Housing", 45000.0, 45f, BrandMidnightNavy, Icons.Default.ShoppingCart, 1),
    CategorySpendingItem("DINING_OUT", "Dining", 28000.0, 28f, BrandWarmYellow, Icons.Default.LocalCafe, 6),
    CategorySpendingItem("MISC", "Groceries", 17000.0, 17f, BrandEmeraldGreen, Icons.Default.ShoppingCart, 4),
    CategorySpendingItem("TRANSPORT", "Transport", 10000.0, 10f, BrandAzureBlue, Icons.Default.DirectionsCar, 3)
)

@Preview(name = "Expense Breakdown Pie - Light", showBackground = true, widthDp = 390)
@Composable
private fun ExpenseBreakdownPiePreviewLight() {
    InTrackTheme {
        Surface {
            ExpenseBreakdownPieContent(
                monthLabel = "November 2026",
                totalExpenseLkr = 100000.0,
                committedLkr = 62000.0,
                discretionaryLkr = 38000.0,
                categoryItems = sampleCategoryItems(),
                onDismiss = {}
            )
        }
    }
}

@Preview(name = "Expense Breakdown Pie - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, widthDp = 390)
@Composable
private fun ExpenseBreakdownPiePreviewDark() {
    InTrackTheme(darkTheme = true) {
        Surface {
            ExpenseBreakdownPieContent(
                monthLabel = "November 2026",
                totalExpenseLkr = 100000.0,
                committedLkr = 62000.0,
                discretionaryLkr = 38000.0,
                categoryItems = sampleCategoryItems(),
                onDismiss = {}
            )
        }
    }
}

@Preview(name = "Expense Breakdown Pie - Empty", showBackground = true, widthDp = 390)
@Composable
private fun ExpenseBreakdownPiePreviewEmpty() {
    InTrackTheme {
        Surface {
            ExpenseBreakdownPieContent(
                monthLabel = "November 2026",
                totalExpenseLkr = 0.0,
                committedLkr = 0.0,
                discretionaryLkr = 0.0,
                categoryItems = emptyList(),
                onDismiss = {}
            )
        }
    }
}

@Preview(name = "Subscription Breakdown Pie - Light", showBackground = true, widthDp = 390)
@Composable
private fun SubscriptionBreakdownPiePreviewLight() {
    InTrackTheme {
        Surface {
            SubscriptionBreakdownPieContent(
                totalRecurringLkr = 40700.0,
                secondaryAccountTotal = 25000.0,
                primaryCardTotal = 12700.0,
                cashTotal = 3000.0,
                onDismiss = {}
            )
        }
    }
}
