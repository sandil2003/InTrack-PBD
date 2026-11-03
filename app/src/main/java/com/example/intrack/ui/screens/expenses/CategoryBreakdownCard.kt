package com.example.intrack.ui.screens.expenses

import java.util.Locale
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingCart
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.intrack.data.model.ExpenseEntity
import com.example.intrack.util.CurrencyFormatter

data class CategorySpendingItem(
    val category: String,
    val displayName: String,
    val amountLkr: Double,
    val percentage: Float,
    val color: Color,
    val icon: ImageVector,
    val transactionCount: Int = 1
)

fun calculateCategoryBreakdown(expenses: List<ExpenseEntity>): List<CategorySpendingItem> {
    val outflowOnly = expenses.filter { !it.isInternalTransfer() }
    if (outflowOnly.isEmpty()) return emptyList()

    val total = outflowOnly.sumOf { it.amountLkr }
    if (total <= 0.0) return emptyList()

    val grouped = outflowOnly.groupBy { it.category.uppercase() }

    return grouped.map { (catKey, list) ->
        val catTotal = list.sumOf { it.amountLkr }
        val pct = ((catTotal / total) * 100).toFloat()

        val (displayName, color, icon) = when (catKey) {
            "MISC" -> Triple("Groceries", Color(0xFFFF5C38), Icons.Default.ShoppingCart)
            "DINING_OUT" -> Triple("Bistro & Dining", Color(0xFFFFB299), Icons.Default.LocalCafe)
            "TRANSPORT" -> Triple("Commute & Transport", Color(0xFF2C3437), Icons.Default.DirectionsCar)
            "UTILITIES" -> Triple("Utilities & Others", Color(0xFF585F6B), Icons.Default.Receipt)
            "RENT" -> Triple("Housing & Rent", Color(0xFF475569), Icons.Default.Home)
            "SUBSCRIPTION" -> Triple("Subscriptions", Color(0xFF8B5CF6), Icons.Default.Receipt)
            else -> Triple(
                catKey.replace("_", " ").lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString() },
                Color(0xFF64748B),
                Icons.Default.Receipt
            )
        }

        CategorySpendingItem(
            category = catKey,
            displayName = displayName,
            amountLkr = catTotal,
            percentage = pct,
            color = color,
            icon = icon,
            transactionCount = list.size
        )
    }.sortedByDescending { it.amountLkr }
}

@Composable
fun CategoryBreakdownCard(
    items: List<CategorySpendingItem>,
    totalExpenseLkr: Double,
    selectedCategory: String = "ALL",
    onSelectCategory: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) return

    val topCategory = items.firstOrNull()

    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(0.dp, Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SPENDING BY CATEGORY",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp
                )

                if (topCategory != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(topCategory.color.copy(alpha = 0.15f))
                            .padding(horizontal = 7.dp, vertical = 2.5.dp)
                    ) {
                        Text(
                            text = "Top: ${topCategory.displayName} (${topCategory.percentage.toInt()}%)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = topCategory.color
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Continuous Proportional Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                items.forEach { item ->
                    val ratio = (item.percentage / 100f).coerceAtLeast(0.01f)
                    Box(
                        modifier = Modifier
                            .weight(ratio)
                            .fillMaxSize()
                            .background(item.color)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Ranked Category Percentage Rows
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items.forEach { item ->
                    val isSelected = selectedCategory.equals(item.category, ignoreCase = true)
                    CategoryProgressRow(
                        item = item,
                        isSelected = isSelected,
                        onClick = { onSelectCategory(item.category) }
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryProgressRow(
    item: CategorySpendingItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) item.color.copy(alpha = 0.10f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(item.color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        tint = item.color,
                        modifier = Modifier.size(15.dp)
                    )
                }

                Text(
                    text = item.displayName,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.5.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = CurrencyFormatter.formatLkr(item.amountLkr),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "(${item.percentage.toInt()}%)",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = item.color
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Horizontal percentage progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(2.5.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = (item.percentage / 100f).coerceIn(0.01f, 1f))
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(2.5.dp))
                    .background(item.color)
            )
        }
    }
}
