package com.example.intrack.ui.navigation

sealed class Screen(val route: String, val title: String) {
    object Login : Screen("login", "Login")
    object Dashboard : Screen("dashboard", "Dashboard")
    object IncomeHub : Screen("income_hub", "Income Hub")
    object ExpenseAnalytics : Screen("expense_analytics", "Expense Analytics")
    object SubscriptionAudit : Screen("subscription_audit", "Subscriptions")
    object GoalTracker : Screen("goal_tracker", "Goals & Targets")
    object AllTransactions : Screen("all_transactions", "All Transactions")
    object GoalDetail : Screen("goal_detail/{goalId}", "Goal Details") {
        const val navRoute = "goal_detail/{goalId}"
        fun createRoute(goalId: String): String = "goal_detail/$goalId"
    }
}
