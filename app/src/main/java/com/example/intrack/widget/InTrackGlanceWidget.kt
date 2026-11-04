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
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
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
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.intrack.MainActivity
import com.example.intrack.util.DateUtils
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class InTrackGlanceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Suspending query: fetch incomes and expenses for the current month only from local Room cache
        val widgetData = withContext(Dispatchers.IO) {
            try {
                val entryPoint = EntryPointAccessors.fromApplication(
                    context.applicationContext,
                    WidgetEntryPoint::class.java
                )
                val incomes = entryPoint.incomeDao().getAllIncomes()
                val expenses = entryPoint.expenseDao().getAllExpenses()

                // Calculate current month's cleared income total
                val monthIncome = incomes
                    .filter { DateUtils.isCurrentMonth(it.dateReceivedMillis) && it.status.equals("PAID", ignoreCase = true) }
                    .sumOf { it.realizedAmountLkr }

                // Calculate current month's non-transfer expense total
                val monthExpense = expenses
                    .filter { DateUtils.isCurrentMonth(it.dateSpentMillis) && !it.isInternalTransfer() }
                    .sumOf { it.amountLkr }

                MonthlyTotalsData(
                    incomeTotalLkr = monthIncome,
                    expenseTotalLkr = monthExpense
                )
            } catch (e: Exception) {
                MonthlyTotalsData(
                    incomeTotalLkr = 0.0,
                    expenseTotalLkr = 0.0
                )
            }
        }

        // Render 100% Jetpack Compose UI
        provideContent {
            GlanceTheme {
                InTrackDualCardWidgetContent(context = context, data = widgetData)
            }
        }
    }

    @Composable
    private fun InTrackDualCardWidgetContent(context: Context, data: MonthlyTotalsData) {
        // Income Card Palette (Subtle Mint Tint & Emerald)
        val incomeCardBg = ColorProvider(Color(0xFFF0FDF4))
        val incomeBadgeColor = ColorProvider(Color(0xFF059669))
        val incomeAmountColor = ColorProvider(Color(0xFF065F46))
        val incomeButtonBg = ColorProvider(Color(0xFF059669))
        val incomeButtonTextColor = ColorProvider(Color(0xFFFFFFFF))
        val subtitleColor = ColorProvider(Color(0xFF64748B))

        // Expense Card Palette (Subtle Rose Tint & Crimson)
        val expenseCardBg = ColorProvider(Color(0xFFFFF1F2))
        val expenseBadgeColor = ColorProvider(Color(0xFFE11D48))
        val expenseAmountColor = ColorProvider(Color(0xFF9F1239))
        val expenseButtonBg = ColorProvider(Color(0xFFE11D48))
        val expenseButtonTextColor = ColorProvider(Color(0xFFFFFFFF))

        // Deep Link Intents: Exclusively 2 buttons
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

        // Dual Floating Cards Row (Zero outer white background / borderless)
        Row(
            modifier = GlanceModifier
                .fillMaxSize(),
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            // ================= CARD 1: MONTHLY INCOME =================
            Column(
                modifier = GlanceModifier
                    .defaultWeight()
                    .fillMaxHeight()
                    .cornerRadius(16.dp)
                    .background(incomeCardBg)
                    .padding(12.dp)
                    .clickable(actionStartActivity(incomeIntent))
            ) {
                Text(
                    text = "↑ Income",
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = incomeBadgeColor
                    )
                )

                Spacer(modifier = GlanceModifier.defaultWeight())

                Text(
                    text = "LKR ${"%,.0f".format(data.incomeTotalLkr)}",
                    style = TextStyle(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = incomeAmountColor
                    ),
                    maxLines = 1
                )

                Text(
                    text = "This Month",
                    style = TextStyle(
                        fontSize = 10.sp,
                        color = subtitleColor
                    )
                )

                Spacer(modifier = GlanceModifier.height(8.dp))

                // Button 1: "+ Income"
                Box(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .height(34.dp)
                        .cornerRadius(10.dp)
                        .background(incomeButtonBg)
                        .clickable(actionStartActivity(incomeIntent)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+ Income",
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = incomeButtonTextColor
                        )
                    )
                }
            }

            Spacer(modifier = GlanceModifier.width(10.dp))

            // ================= CARD 2: MONTHLY EXPENSES =================
            Column(
                modifier = GlanceModifier
                    .defaultWeight()
                    .fillMaxHeight()
                    .cornerRadius(16.dp)
                    .background(expenseCardBg)
                    .padding(12.dp)
                    .clickable(actionStartActivity(expenseIntent))
            ) {
                Text(
                    text = "↓ Expenses",
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = expenseBadgeColor
                    )
                )

                Spacer(modifier = GlanceModifier.defaultWeight())

                Text(
                    text = "LKR ${"%,.0f".format(data.expenseTotalLkr)}",
                    style = TextStyle(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = expenseAmountColor
                    ),
                    maxLines = 1
                )

                Text(
                    text = "This Month",
                    style = TextStyle(
                        fontSize = 10.sp,
                        color = subtitleColor
                    )
                )

                Spacer(modifier = GlanceModifier.height(8.dp))

                // Button 2: "+ Expense"
                Box(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .height(34.dp)
                        .cornerRadius(10.dp)
                        .background(expenseButtonBg)
                        .clickable(actionStartActivity(expenseIntent)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+ Expense",
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = expenseButtonTextColor
                        )
                    )
                }
            }
        }
    }

    private data class MonthlyTotalsData(
        val incomeTotalLkr: Double,
        val expenseTotalLkr: Double
    )

    companion object {
        suspend fun updateWidget(context: Context) {
            InTrackGlanceWidget().updateAll(context)
        }
    }
}
