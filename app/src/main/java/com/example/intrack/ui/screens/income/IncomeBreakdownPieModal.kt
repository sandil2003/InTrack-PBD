package com.example.intrack.ui.screens.income

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
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.material.icons.rounded.CurrencyBitcoin
import androidx.compose.material.icons.rounded.Laptop
import androidx.compose.material.icons.rounded.Work
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
import com.example.intrack.viewmodel.IncomeAnalyticsCalculator
import com.example.intrack.viewmodel.IncomeChannelSlice
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.roundToInt

private data class ChannelVisualStyle(
    val color: Color,
    val icon: ImageVector
)

private fun visualStyleForChannel(channelKey: String): ChannelVisualStyle {
    return when (channelKey.uppercase()) {
        "SALARY" -> ChannelVisualStyle(BrandEmeraldGreen, Icons.Rounded.Work)
        "FREELANCE" -> ChannelVisualStyle(BrandAzureBlue, Icons.Rounded.Laptop)
        "ADSENSE" -> ChannelVisualStyle(BrandWarmYellow, Icons.Rounded.AttachMoney)
        "CRYPTO" -> ChannelVisualStyle(Color(0xFF7C4DFF), Icons.Rounded.CurrencyBitcoin)
        else -> ChannelVisualStyle(BrandMidnightNavy, Icons.AutoMirrored.Filled.TrendingUp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncomeBreakdownPieModal(
    monthLabel: String,
    totalInflowLkr: Double,
    salaryTotalLkr: Double,
    salaryCount: Int,
    freelanceTotalLkr: Double,
    freelanceCount: Int,
    adsenseTotalLkr: Double,
    adsenseCount: Int,
    cryptoTotalLkr: Double,
    cryptoCount: Int,
    onDismiss: () -> Unit,
    onSelectChannel: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        IncomeBreakdownPieContent(
            monthLabel = monthLabel,
            totalInflowLkr = totalInflowLkr,
            salaryTotalLkr = salaryTotalLkr,
            salaryCount = salaryCount,
            freelanceTotalLkr = freelanceTotalLkr,
            freelanceCount = freelanceCount,
            adsenseTotalLkr = adsenseTotalLkr,
            adsenseCount = adsenseCount,
            cryptoTotalLkr = cryptoTotalLkr,
            cryptoCount = cryptoCount,
            onDismiss = onDismiss,
            onSelectChannel = onSelectChannel
        )
    }
}

@Composable
internal fun IncomeBreakdownPieContent(
    monthLabel: String,
    totalInflowLkr: Double,
    salaryTotalLkr: Double,
    salaryCount: Int,
    freelanceTotalLkr: Double,
    freelanceCount: Int,
    adsenseTotalLkr: Double,
    adsenseCount: Int,
    cryptoTotalLkr: Double,
    cryptoCount: Int,
    onDismiss: () -> Unit,
    onSelectChannel: ((String) -> Unit)? = null
) {
    val slices = remember(
        salaryTotalLkr, salaryCount,
        freelanceTotalLkr, freelanceCount,
        adsenseTotalLkr, adsenseCount,
        cryptoTotalLkr, cryptoCount
    ) {
        IncomeAnalyticsCalculator.buildChannelSlices(
            salaryTotalLkr = salaryTotalLkr,
            salaryCount = salaryCount,
            freelanceTotalLkr = freelanceTotalLkr,
            freelanceCount = freelanceCount,
            adsenseTotalLkr = adsenseTotalLkr,
            adsenseCount = adsenseCount,
            cryptoTotalLkr = cryptoTotalLkr,
            cryptoCount = cryptoCount
        )
    }

    var focusedChannelKey by remember(slices) { mutableStateOf<String?>(null) }
    val focusedSlice = slices.find { it.channelKey == focusedChannelKey }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$monthLabel Breakdown",
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
                    text = "Total ${CurrencyFormatter.formatLkr(totalInflowLkr)}",
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

        if (slices.isEmpty()) {
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
                        text = "No realized income in $monthLabel",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            // Interactive Pie Chart + Focused Summary
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
                    IncomePieChartCanvas(
                        slices = slices,
                        focusedChannelKey = focusedChannelKey,
                        onSliceTapped = { tappedKey ->
                            focusedChannelKey = if (focusedChannelKey == tappedKey) null else tappedKey
                        }
                    )

                    val badgeText = if (focusedSlice != null) {
                        "${focusedSlice.label} · ${focusedSlice.percentage.roundToInt()}% · ${CurrencyFormatter.formatLkr(focusedSlice.amountLkr)}"
                    } else {
                        "${slices.size} active ${if (slices.size == 1) "channel" else "channels"} · Tap slice to focus"
                    }

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
                            color = BrandMidnightNavy,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Detailed Channel Breakdown Cards
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                slices.forEach { slice ->
                    val style = visualStyleForChannel(slice.channelKey)
                    val isFocused = focusedChannelKey == slice.channelKey
                    val pct = slice.percentage.roundToInt()

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isFocused) style.color.copy(alpha = 0.14f) else BrandSoftSlate
                        ),
                        border = if (isFocused) BorderStroke(1.dp, style.color) else BorderStroke(0.dp, Color.Transparent),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (onSelectChannel != null) {
                                    onSelectChannel(slice.channelKey)
                                    onDismiss()
                                } else {
                                    focusedChannelKey = if (isFocused) null else slice.channelKey
                                }
                            }
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
                                        imageVector = style.icon,
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
                                                .background(style.color)
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
                                        text = "${slice.entryCount} ${if (slice.entryCount == 1) "entry" else "entries"}",
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
    }
}

@Composable
private fun IncomePieChartCanvas(
    slices: List<IncomeChannelSlice>,
    focusedChannelKey: String?,
    onSliceTapped: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var playIntro by remember { mutableStateOf(false) }
    LaunchedEffect(slices) { playIntro = true }

    val sweepProgress by animateFloatAsState(
        targetValue = if (playIntro) 1f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "incomePieSweep"
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

                    // Angle in degrees starting at top (-90 deg) clockwise [0..360)
                    var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat() + 90f
                    if (angle < 0f) angle += 360f

                    val hit = sliceAngles.firstOrNull { (_, start, sweep) ->
                        val normStart = (start + 90f).let { if (it < 0f) it + 360f else it }
                        angle >= normStart && angle <= normStart + sweep
                    }
                    hit?.first?.let { onSliceTapped(it.channelKey) }
                }
            }
    ) {
        val basePadding = 10.dp.toPx()
        val maxSize = size.minDimension - (basePadding * 2f)
        val separatorWidth = if (slices.size > 1) 2.5.dp.toPx() else 0f

        sliceAngles.forEach { (slice, startAngle, sweepAngle) ->
            val isFocused = focusedChannelKey == slice.channelKey
            val expandOffset = if (isFocused) 6.dp.toPx() else 0f
            val arcDiameter = maxSize + (expandOffset * 2f)
            val topLeft = Offset(
                x = (size.width - arcDiameter) / 2f,
                y = (size.height - arcDiameter) / 2f
            )
            val arcSize = Size(arcDiameter, arcDiameter)
            val animatedSweep = (sweepAngle * sweepProgress).coerceAtLeast(0f)
            val style = visualStyleForChannel(slice.channelKey)
            val alpha = if (focusedChannelKey == null || isFocused) 1f else 0.42f

            drawArc(
                color = style.color.copy(alpha = alpha),
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

@Preview(name = "Income Breakdown Pie - Light", showBackground = true, widthDp = 390)
@Composable
private fun IncomeBreakdownPiePreviewLight() {
    InTrackTheme {
        Surface {
            IncomeBreakdownPieContent(
                monthLabel = "October 2026",
                totalInflowLkr = 257550.0,
                salaryTotalLkr = 120000.0,
                salaryCount = 1,
                freelanceTotalLkr = 45000.0,
                freelanceCount = 2,
                adsenseTotalLkr = 64050.0,
                adsenseCount = 1,
                cryptoTotalLkr = 28500.0,
                cryptoCount = 1,
                onDismiss = {}
            )
        }
    }
}

@Preview(name = "Income Breakdown Pie - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, widthDp = 390)
@Composable
private fun IncomeBreakdownPiePreviewDark() {
    InTrackTheme(darkTheme = true) {
        Surface {
            IncomeBreakdownPieContent(
                monthLabel = "October 2026",
                totalInflowLkr = 257550.0,
                salaryTotalLkr = 120000.0,
                salaryCount = 1,
                freelanceTotalLkr = 45000.0,
                freelanceCount = 2,
                adsenseTotalLkr = 64050.0,
                adsenseCount = 1,
                cryptoTotalLkr = 28500.0,
                cryptoCount = 1,
                onDismiss = {}
            )
        }
    }
}

@Preview(name = "Income Breakdown Pie - Empty", showBackground = true, widthDp = 390)
@Composable
private fun IncomeBreakdownPiePreviewEmpty() {
    InTrackTheme {
        Surface {
            IncomeBreakdownPieContent(
                monthLabel = "November 2026",
                totalInflowLkr = 0.0,
                salaryTotalLkr = 0.0,
                salaryCount = 0,
                freelanceTotalLkr = 0.0,
                freelanceCount = 0,
                adsenseTotalLkr = 0.0,
                adsenseCount = 0,
                cryptoTotalLkr = 0.0,
                cryptoCount = 0,
                onDismiss = {}
            )
        }
    }
}
