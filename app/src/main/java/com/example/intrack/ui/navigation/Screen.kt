package com.example.intrack.ui.navigation

sealed class Screen(val route: String, val title: String) {
    object Login : Screen("login", "Sign In")
    object SignIn : Screen("login", "Sign In")
    object SignUp : Screen("sign_up", "Sign Up")
    object Dashboard : Screen("dashboard", "Dashboard")
    object IncomeHub : Screen("income_hub", "Income Hub")
    object ExpenseAnalytics : Screen("expense_analytics", "Expense Analytics") {
        const val navRoute = "expense_analytics?tab={tab}"
        fun createRoute(tab: String = "daily"): String = "expense_analytics?tab=$tab"
    }
    object SubscriptionAudit : Screen("subscription_audit", "Subscriptions")
    object GoalTracker : Screen("goal_tracker", "Goals & Targets")
    object AllTransactions : Screen("all_transactions", "All Transactions")
    object GoalDetail : Screen("goal_detail/{goalId}", "Goal Details") {
        const val navRoute = "goal_detail/{goalId}"
        fun createRoute(goalId: String): String = "goal_detail/$goalId"
    }
    object AddExpense : Screen("add_expense", "Add an expense") {
        const val navRoute = "add_expense?category={category}&amount={amount}&title={title}&commitmentType={commitmentType}&paymentAccount={paymentAccount}"
        fun createRoute(
            category: String = "DINING_OUT",
            amount: String = "",
            title: String = "",
            commitmentType: String = "DISCRETIONARY",
            paymentAccount: String = "CARD_PRIMARY"
        ): String {
            val encodedTitle = try {
                java.net.URLEncoder.encode(title, "UTF-8")
            } catch (_: Exception) {
                title
            }
            return "add_expense?category=$category&amount=$amount&title=$encodedTitle&commitmentType=$commitmentType&paymentAccount=$paymentAccount"
        }
    }
    object AddSubscription : Screen("add_subscription", "Add Subscription")
    object AddGoal : Screen("add_goal", "New Financial Goal")
    object Profile : Screen("profile", "My Profile")
    object Settings : Screen("settings", "Settings & Account")
    object EditExpense : Screen("edit_expense?expenseId={expenseId}", "Edit expense") {
        const val navRoute = "edit_expense?expenseId={expenseId}"
        fun createRoute(expenseId: String): String = "edit_expense?expenseId=$expenseId"
    }
}
