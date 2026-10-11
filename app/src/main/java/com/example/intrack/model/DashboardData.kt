package com.example.intrack.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.vector.ImageVector

data class Transaction(
    val id: String,
    val title: String,
    val category: String,
    val amount: Double,
    val date: String,
    val isExpense: Boolean,
    val icon: ImageVector
)

data class DashboardSummary(
    val totalBalance: Double,
    val totalIncome: Double,
    val totalExpenses: Double
)

data class DrawerMenuItem(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val badgeCount: Int? = null
)

object SampleData {
    fun getSummary(): DashboardSummary {
        return DashboardSummary(
            totalBalance = 12450.80,
            totalIncome = 5800.00,
            totalExpenses = 2349.20
        )
    }

    fun getTransactions(): List<Transaction> {
        return listOf(
            Transaction(
                id = "1",
                title = "Client Project Payout",
                category = "Income",
                amount = 2500.00,
                date = "Today, 2:30 PM",
                isExpense = false,
                icon = Icons.Default.Work
            ),
            Transaction(
                id = "2",
                title = "Grocery Shopping",
                category = "Food & Groceries",
                amount = 142.50,
                date = "Yesterday, 6:15 PM",
                isExpense = true,
                icon = Icons.Default.ShoppingCart
            ),
            Transaction(
                id = "3",
                title = "Monthly Rent",
                category = "Housing",
                amount = 1200.00,
                date = "Sep 28, 2026",
                isExpense = true,
                icon = Icons.Default.Home
            ),
            Transaction(
                id = "4",
                title = "Software Subscription",
                category = "Services",
                amount = 29.99,
                date = "Sep 26, 2026",
                isExpense = true,
                icon = Icons.Default.Code
            ),
            Transaction(
                id = "5",
                title = "Stock Dividend",
                category = "Investments",
                amount = 350.00,
                date = "Sep 25, 2026",
                isExpense = false,
                icon = Icons.AutoMirrored.Filled.TrendingUp
            ),
            Transaction(
                id = "6",
                title = "Coffee & Bakery",
                category = "Dining Out",
                amount = 18.25,
                date = "Sep 24, 2026",
                isExpense = true,
                icon = Icons.Default.LocalCafe
            )
        )
    }
}
