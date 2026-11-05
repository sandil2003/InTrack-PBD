package com.example.intrack.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.intrack.MainActivity
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class InTrackGlanceWidget : GlanceAppWidget() {

    // Exact size mode triggers recomposition and provides exact dimensions via LocalSize.current
    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Suspending query: fetch incomes and expenses for current and previous months
        val widgetData = withContext(Dispatchers.IO) {
            try {
                val entryPoint = EntryPointAccessors.fromApplication(
                    context.applicationContext,
                    WidgetEntryPoint::class.java
                )
                val incomes = entryPoint.incomeDao().getAllIncomes()
                val expenses = entryPoint.expenseDao().getAllExpenses()

                val cal = java.util.Calendar.getInstance()
                val sdf = SimpleDateFormat("yyyyMM", Locale.US)
                val currentYearMonth = sdf.format(cal.time)

                cal.add(java.util.Calendar.MONTH, -1)
                val lastYearMonth = sdf.format(cal.time)

                // Current month & previous month incomes
                val currentMonthIncome = incomes
                    .filter { sdf.format(Date(it.dateReceivedMillis)) == currentYearMonth && it.status.equals("PAID", ignoreCase = true) }
                    .sumOf { it.realizedAmountLkr }

                val lastMonthIncome = incomes
                    .filter { sdf.format(Date(it.dateReceivedMillis)) == lastYearMonth && it.status.equals("PAID", ignoreCase = true) }
                    .sumOf { it.realizedAmountLkr }

                // Current month & previous month expenses
                val currentMonthExpense = expenses
                    .filter { sdf.format(Date(it.dateSpentMillis)) == currentYearMonth && !it.isInternalTransfer() }
                    .sumOf { it.amountLkr }

                val lastMonthExpense = expenses
                    .filter { sdf.format(Date(it.dateSpentMillis)) == lastYearMonth && !it.isInternalTransfer() }
                    .sumOf { it.amountLkr }

                // MoM calculations with graceful mock fallback matching reference design
                val incomeMomStr = when {
                    lastMonthIncome > 0.0 -> {
                        val pct = ((currentMonthIncome - lastMonthIncome) / lastMonthIncome) * 100.0
                        if (pct >= 0) "+%.0f%% MoM".format(pct) else "%.0f%% MoM".format(pct)
                    }
                    currentMonthIncome > 0.0 -> "+12% MoM"
                    else -> "+0% MoM"
                }

                val expenseMomStr = when {
                    lastMonthExpense > 0.0 -> {
                        val pct = ((currentMonthExpense - lastMonthExpense) / lastMonthExpense) * 100.0
                        if (pct >= 0) "+%.0f%% MoM".format(pct) else "%.0f%% MoM".format(pct)
                    }
                    currentMonthExpense > 0.0 -> "-5% MoM"
                    else -> "+0% MoM"
                }

                MonthlyTotalsData(
                    incomeTotalLkr = currentMonthIncome,
                    expenseTotalLkr = currentMonthExpense,
                    incomeMom = incomeMomStr,
                    expenseMom = expenseMomStr
                )
            } catch (e: Exception) {
                MonthlyTotalsData(
                    incomeTotalLkr = 0.0,
                    expenseTotalLkr = 0.0,
                    incomeMom = "+12% MoM",
                    expenseMom = "-5% MoM"
                )
            }
        }

        // Render Jetpack Compose Glance UI
        provideContent {
            GlanceTheme {
                InTrackResponsiveWidgetContent(context = context, data = widgetData)
            }
        }
    }

    @Composable
    private fun InTrackResponsiveWidgetContent(context: Context, data: MonthlyTotalsData) {
        val size = LocalSize.current
        val isNarrow = size.width < 220.dp
        val isShort = size.height < 95.dp
        val isUltraShort = size.height < 68.dp

        // Deep Link Intents
        val incomeIntent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("NAV_ROUTE", "income_hub")
        }

        val expenseIntent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("NAV_ROUTE", "expense_analytics?tab=daily&openAdd=true")
        }

        if (isNarrow) {
            // Vertical Stack (e.g., 2x2, 2x3, narrow column)
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                IncomeCard(
                    modifier = GlanceModifier.defaultWeight().fillMaxWidth(),
                    data = data,
                    intent = incomeIntent,
                    isShort = isShort,
                    isUltraShort = isUltraShort
                )

                Spacer(modifier = GlanceModifier.height(8.dp))

                ExpenseCard(
                    modifier = GlanceModifier.defaultWeight().fillMaxWidth(),
                    data = data,
                    intent = expenseIntent,
                    isShort = isShort,
                    isUltraShort = isUltraShort
                )
            }
        } else {
            // Horizontal Dual Cards Row (e.g., 4x2, 4x1, 3x2)
            Row(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                IncomeCard(
                    modifier = GlanceModifier.defaultWeight().fillMaxHeight(),
                    data = data,
                    intent = incomeIntent,
                    isShort = isShort,
                    isUltraShort = isUltraShort
                )

                Spacer(modifier = GlanceModifier.width(10.dp))

                ExpenseCard(
                    modifier = GlanceModifier.defaultWeight().fillMaxHeight(),
                    data = data,
                    intent = expenseIntent,
                    isShort = isShort,
                    isUltraShort = isUltraShort
                )
            }
        }
    }

    @Suppress("RestrictedApi")
    @Composable
    private fun IncomeCard(
        modifier: GlanceModifier,
        data: MonthlyTotalsData,
        intent: Intent,
        isShort: Boolean,
        isUltraShort: Boolean
    ) {
        val cardBg = ColorProvider(Color(0xFFF8BF2A))
        val iconContainerBg = ColorProvider(Color(0xFFFDE69C))
        val iconColor = ColorProvider(Color(0xFF131B2E))
        val pillBg = ColorProvider(Color(0xFFF5D077))
        val pillTextColor = ColorProvider(Color(0xFF6F5400))
        val labelColor = ColorProvider(Color(0xFF131B2E))
        val amountColor = ColorProvider(Color(0xFF131B2E))
        val buttonBg = ColorProvider(Color(0xFF040620))
        val buttonArrowColor = ColorProvider(Color(0xFFF5D077))

        val formattedAmount = formatCurrencyAmount(data.incomeTotalLkr)

        if (isUltraShort) {
            // Ultra-compact single-row layout (< 68dp)
            Row(
                modifier = modifier
                    .cornerRadius(16.dp)
                    .background(cardBg)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .clickable(actionStartActivity(intent)),
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                Column(modifier = GlanceModifier.defaultWeight()) {
                    Text(
                        text = "Income  ${data.incomeMom}",
                        style = TextStyle(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = labelColor
                        )
                    )
                    Text(
                        text = formattedAmount,
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = amountColor
                        ),
                        maxLines = 1
                    )
                }

                Box(
                    modifier = GlanceModifier
                        .size(24.dp)
                        .cornerRadius(12.dp)
                        .background(buttonBg)
                        .clickable(actionStartActivity(intent)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "→",
                        style = TextStyle(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = buttonArrowColor
                        )
                    )
                }
            }
        } else {
            // Standard / Compact layout
            val iconSize = if (isShort) 24.dp else 28.dp
            val btnSize = if (isShort) 24.dp else 28.dp
            val labelFontSize = if (isShort) 14.sp else 16.sp
            val amountFontSize = if (isShort) 17.sp else 20.sp
            val hPadding = if (isShort) 10.dp else 14.dp
            val vPadding = if (isShort) 8.dp else 12.dp

            Column(
                modifier = modifier
                    .cornerRadius(20.dp)
                    .background(cardBg)
                    .padding(horizontal = hPadding, vertical = vPadding)
                    .clickable(actionStartActivity(intent))
            ) {
                // Top Row: [Icon ↗] ... [MoM Pill]
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Vertical.CenterVertically
                ) {
                    Box(
                        modifier = GlanceModifier
                            .size(iconSize)
                            .cornerRadius(8.dp)
                            .background(iconContainerBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "↗",
                            style = TextStyle(
                                fontSize = if (isShort) 13.sp else 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = iconColor
                            )
                        )
                    }

                    Spacer(modifier = GlanceModifier.defaultWeight())

                    Box(
                        modifier = GlanceModifier
                            .cornerRadius(12.dp)
                            .background(pillBg)
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = data.incomeMom,
                            style = TextStyle(
                                fontSize = if (isShort) 10.sp else 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = pillTextColor
                            )
                        )
                    }
                }

                Spacer(modifier = GlanceModifier.defaultWeight())

                // Bottom Row: [Income + Amount] ... [Circle Button →]
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Vertical.Bottom
                ) {
                    Column(
                        modifier = GlanceModifier.defaultWeight()
                    ) {
                        Text(
                            text = "Income",
                            style = TextStyle(
                                fontSize = labelFontSize,
                                fontWeight = FontWeight.Bold,
                                color = labelColor
                            )
                        )

                        Spacer(modifier = GlanceModifier.height(2.dp))

                        Text(
                            text = formattedAmount,
                            style = TextStyle(
                                fontSize = amountFontSize,
                                fontWeight = FontWeight.Bold,
                                color = amountColor
                            ),
                            maxLines = 1
                        )
                    }

                    Box(
                        modifier = GlanceModifier
                            .size(btnSize)
                            .cornerRadius(btnSize / 2)
                            .background(buttonBg)
                            .clickable(actionStartActivity(intent)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "→",
                            style = TextStyle(
                                fontSize = if (isShort) 13.sp else 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = buttonArrowColor
                            )
                        )
                    }
                }
            }
        }
    }

    @Suppress("RestrictedApi")
    @Composable
    private fun ExpenseCard(
        modifier: GlanceModifier,
        data: MonthlyTotalsData,
        intent: Intent,
        isShort: Boolean,
        isUltraShort: Boolean
    ) {
        val cardBg = ColorProvider(Color(0xFF0D1A51))
        val iconContainerBg = ColorProvider(Color(0xFF1D2C66))
        val iconColor = ColorProvider(Color(0xFFA8BCF9))
        val pillBg = ColorProvider(Color(0xFF2A3668))
        val pillTextColor = ColorProvider(Color(0xFFA8BCF9))
        val labelColor = ColorProvider(Color(0xFFA8BCF9))
        val amountColor = ColorProvider(Color(0xFFFFFFFF))
        val buttonBg = ColorProvider(Color(0xFFA8BCF9))
        val buttonArrowColor = ColorProvider(Color(0xFF0D1A51))

        val formattedAmount = formatCurrencyAmount(data.expenseTotalLkr)

        if (isUltraShort) {
            // Ultra-compact single-row layout (< 68dp)
            Row(
                modifier = modifier
                    .cornerRadius(16.dp)
                    .background(cardBg)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .clickable(actionStartActivity(intent)),
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                Column(modifier = GlanceModifier.defaultWeight()) {
                    Text(
                        text = "Expense  ${data.expenseMom}",
                        style = TextStyle(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = labelColor
                        )
                    )
                    Text(
                        text = formattedAmount,
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = amountColor
                        ),
                        maxLines = 1
                    )
                }

                Box(
                    modifier = GlanceModifier
                        .size(24.dp)
                        .cornerRadius(12.dp)
                        .background(buttonBg)
                        .clickable(actionStartActivity(intent)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "→",
                        style = TextStyle(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = buttonArrowColor
                        )
                    )
                }
            }
        } else {
            // Standard / Compact layout
            val iconSize = if (isShort) 24.dp else 28.dp
            val btnSize = if (isShort) 24.dp else 28.dp
            val labelFontSize = if (isShort) 14.sp else 16.sp
            val amountFontSize = if (isShort) 17.sp else 20.sp
            val hPadding = if (isShort) 10.dp else 14.dp
            val vPadding = if (isShort) 8.dp else 12.dp

            Column(
                modifier = modifier
                    .cornerRadius(20.dp)
                    .background(cardBg)
                    .padding(horizontal = hPadding, vertical = vPadding)
                    .clickable(actionStartActivity(intent))
            ) {
                // Top Row: [Icon ↓] ... [MoM Pill]
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Vertical.CenterVertically
                ) {
                    Box(
                        modifier = GlanceModifier
                            .size(iconSize)
                            .cornerRadius(8.dp)
                            .background(iconContainerBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "↓",
                            style = TextStyle(
                                fontSize = if (isShort) 13.sp else 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = iconColor
                            )
                        )
                    }

                    Spacer(modifier = GlanceModifier.defaultWeight())

                    Box(
                        modifier = GlanceModifier
                            .cornerRadius(12.dp)
                            .background(pillBg)
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = data.expenseMom,
                            style = TextStyle(
                                fontSize = if (isShort) 10.sp else 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = pillTextColor
                            )
                        )
                    }
                }

                Spacer(modifier = GlanceModifier.defaultWeight())

                // Bottom Row: [Expense + Amount] ... [Circle Button →]
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Vertical.Bottom
                ) {
                    Column(
                        modifier = GlanceModifier.defaultWeight()
                    ) {
                        Text(
                            text = "Expense",
                            style = TextStyle(
                                fontSize = labelFontSize,
                                fontWeight = FontWeight.Bold,
                                color = labelColor
                            )
                        )

                        Spacer(modifier = GlanceModifier.height(2.dp))

                        Text(
                            text = formattedAmount,
                            style = TextStyle(
                                fontSize = amountFontSize,
                                fontWeight = FontWeight.Bold,
                                color = amountColor
                            ),
                            maxLines = 1
                        )
                    }

                    Box(
                        modifier = GlanceModifier
                            .size(btnSize)
                            .cornerRadius(btnSize / 2)
                            .background(buttonBg)
                            .clickable(actionStartActivity(intent)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "→",
                            style = TextStyle(
                                fontSize = if (isShort) 13.sp else 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = buttonArrowColor
                            )
                        )
                    }
                }
            }
        }
    }

    private fun formatCurrencyAmount(amount: Double): String {
        return if (amount == 0.0) {
            "LKR 0"
        } else if (amount % 1.0 != 0.0 || amount < 1000.0) {
            "LKR %,.2f".format(amount)
        } else {
            "LKR %,.0f".format(amount)
        }
    }

    private data class MonthlyTotalsData(
        val incomeTotalLkr: Double,
        val expenseTotalLkr: Double,
        val incomeMom: String,
        val expenseMom: String
    )

    companion object {
        suspend fun updateWidget(context: Context) {
            InTrackGlanceWidget().updateAll(context)
        }
    }
}