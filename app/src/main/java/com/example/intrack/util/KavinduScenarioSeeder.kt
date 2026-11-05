package com.example.intrack.util

import com.example.intrack.data.model.ExpenseEntity
import com.example.intrack.data.model.GoalContributionEntity
import com.example.intrack.data.model.GoalEntity
import com.example.intrack.data.model.IncomeEntity
import com.example.intrack.data.model.SubscriptionEntity
import com.example.intrack.data.repository.ExpenseRepository
import com.example.intrack.data.repository.GoalRepository
import com.example.intrack.data.repository.IncomeRepository
import com.example.intrack.data.repository.SubscriptionRepository
import java.util.Calendar

object KavinduScenarioSeeder {

    private fun getDynamicCurrentMonthDate(day: Int, hour: Int = 12): Long {
        val cal = Calendar.getInstance()
        val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        cal.set(Calendar.DAY_OF_MONTH, day.coerceIn(1, maxDays))
        cal.set(Calendar.HOUR_OF_DAY, hour)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun getSeedGoals(userId: String, now: Long = System.currentTimeMillis()): List<GoalEntity> {
        val monthStart = getDynamicCurrentMonthDate(1)
        return listOf(
            GoalEntity(
                id = "macbook_m4_goal",
                userId = userId,
                title = "MacBook Pro M4 Fund",
                targetAmountLkr = 490000.0,
                currentSavedLkr = 112000.0,
                targetDateMillis = monthStart + (365L * 24 * 60 * 60 * 1000L),
                monthlyTargetRunRateLkr = 40833.33,
                initialDepositLkr = 50000.0,
                isCompleted = false,
                createdAtMillis = monthStart - (30L * 24 * 60 * 60 * 1000L)
            ),
            GoalEntity(
                id = "emergency_fund_goal",
                userId = userId,
                title = "6-Month Emergency Buffer",
                targetAmountLkr = 600000.0,
                currentSavedLkr = 204000.0,
                targetDateMillis = monthStart + (180L * 24 * 60 * 60 * 1000L),
                monthlyTargetRunRateLkr = 66000.00,
                initialDepositLkr = 100000.0,
                isCompleted = false,
                createdAtMillis = monthStart - (60L * 24 * 60 * 60 * 1000L)
            ),
            GoalEntity(
                id = "travel_conference_goal",
                userId = userId,
                title = "Tech Conference & Travel",
                targetAmountLkr = 150000.0,
                currentSavedLkr = 34500.0,
                targetDateMillis = monthStart + (232L * 24 * 60 * 60 * 1000L),
                monthlyTargetRunRateLkr = 15000.00,
                initialDepositLkr = 20000.0,
                isCompleted = false,
                createdAtMillis = monthStart - (15L * 24 * 60 * 60 * 1000L)
            )
        )
    }

    fun getSeedSubscriptions(userId: String = "kavindu_demo", now: Long = System.currentTimeMillis()): List<SubscriptionEntity> {
        val nextBilling = getDynamicCurrentMonthDate(15)
        return listOf(
            SubscriptionEntity(
                id = "sub_rent",
                userId = userId,
                name = "Monthly House Rent",
                amountLkr = 34000.0,
                billingCycle = "Monthly",
                paymentAccount = "ACCOUNT_SECONDARY",
                category = "HOUSING",
                status = "ACTIVE",
                isAutoPayment = false,
                nextBillingMillis = nextBilling
            ),
            SubscriptionEntity(
                id = "sub_gym",
                userId = userId,
                name = "Gym Membership (Secondary Auto-Debit)",
                amountLkr = 4500.0,
                billingCycle = "Monthly",
                paymentAccount = "ACCOUNT_SECONDARY",
                category = "FITNESS",
                status = "ACTIVE",
                isAutoPayment = true,
                nextBillingMillis = getDynamicCurrentMonthDate(12)
            ),
            SubscriptionEntity(
                id = "sub_zombie_saas",
                userId = userId,
                name = "Cloud SaaS Streaming Plan",
                amountLkr = 2200.0,
                billingCycle = "Monthly",
                paymentAccount = "CARD_PRIMARY",
                category = "ENTERTAINMENT",
                status = "UNUSED_FLAGGED",
                isFlaggedForReview = true,
                isAutoPayment = true,
                nextBillingMillis = getDynamicCurrentMonthDate(8)
            ),
            SubscriptionEntity(
                id = "sub_zombie_cloud",
                userId = userId,
                name = "Pro Dev Cloud Server / Utility",
                amountLkr = 1800.0,
                billingCycle = "Monthly",
                paymentAccount = "CARD_PRIMARY",
                category = "SOFTWARE",
                status = "UNUSED_FLAGGED",
                isFlaggedForReview = true,
                isAutoPayment = true,
                nextBillingMillis = getDynamicCurrentMonthDate(10)
            )
        )
    }

    fun getSeedIncomes(
        userId: String = "kavindu_demo",
        now: Long = System.currentTimeMillis(),
        currentYear: Int = Calendar.getInstance().get(Calendar.YEAR)
    ): List<IncomeEntity> {
        val day1 = getDynamicCurrentMonthDate(1, 9)
        val day2 = getDynamicCurrentMonthDate(2, 14)
        val day3 = getDynamicCurrentMonthDate(3, 11)
        val day4 = getDynamicCurrentMonthDate(4, 16)

        return listOf(
            IncomeEntity(
                id = "inc_salary_curr",
                userId = userId,
                channel = "SALARY",
                title = "Monthly Base Salary & Performance Bonus",
                originalAmount = 140000.0,
                originalCurrency = "LKR",
                realizedAmountLkr = 140000.0,
                status = "PAID",
                dateReceivedMillis = day1
            ),
            IncomeEntity(
                id = "inc_freelance_paid",
                userId = userId,
                channel = "FREELANCE",
                title = "WordPress SME Web Portal",
                clientName = "Ceylon Organics",
                originalAmount = 45000.0,
                originalCurrency = "LKR",
                realizedAmountLkr = 45000.0,
                status = "PAID",
                dateReceivedMillis = day2
            ),
            IncomeEntity(
                id = "inc_freelance_overdue",
                userId = userId,
                channel = "FREELANCE",
                title = "React E-Commerce Redesign (Milestone 2)",
                clientName = "Studio K Retailers",
                originalAmount = 60000.0,
                originalCurrency = "LKR",
                realizedAmountLkr = 60000.0,
                status = "OVERDUE",
                invoiceSentMillis = day1,
                dateReceivedMillis = day4
            ),
            IncomeEntity(
                id = "inc_adsense_curr",
                userId = userId,
                channel = "ADSENSE",
                title = "Tech Blog AdSense Payout (\$210 USD)",
                originalAmount = 210.0,
                originalCurrency = "USD",
                exchangeRate = 305.0,
                realizedAmountLkr = 64050.0,
                status = "PAID",
                dateReceivedMillis = day3
            ),
            IncomeEntity(
                id = "inc_crypto_gain",
                userId = userId,
                channel = "CRYPTO",
                title = "Binance USDT/ETH Realized Gain",
                originalAmount = 28500.0,
                originalCurrency = "LKR",
                realizedAmountLkr = 28500.0,
                status = "PAID",
                dateReceivedMillis = day2
            )
        )
    }

    fun getSeedExpenses(userId: String = "kavindu_demo", now: Long = System.currentTimeMillis()): List<ExpenseEntity> {
        val day1 = getDynamicCurrentMonthDate(1, 10)
        val day2 = getDynamicCurrentMonthDate(2, 12)
        val day3 = getDynamicCurrentMonthDate(3, 15)

        return listOf(
            ExpenseEntity(
                id = "exp_rent_curr",
                userId = userId,
                title = "Monthly House Rent",
                amountLkr = 34000.0,
                category = "RENT",
                paymentAccount = "ACCOUNT_SECONDARY",
                commitmentType = "COMMITTED",
                dateSpentMillis = day1
            ),
            ExpenseEntity(
                id = "exp_wifi_curr",
                userId = userId,
                title = "Dialog Fiber Broadband",
                amountLkr = 5800.0,
                category = "UTILITIES",
                paymentAccount = "CARD_PRIMARY",
                commitmentType = "COMMITTED",
                dateSpentMillis = day1
            ),
            ExpenseEntity(
                id = "exp_gym_curr",
                userId = userId,
                title = "Gym Membership (Secondary Auto-Debit)",
                amountLkr = 4500.0,
                category = "FITNESS",
                paymentAccount = "ACCOUNT_SECONDARY",
                commitmentType = "COMMITTED",
                dateSpentMillis = day2
            ),
            ExpenseEntity(
                id = "exp_groceries",
                userId = userId,
                title = "Keells Supermarket Essentials",
                amountLkr = 18500.0,
                category = "MISC",
                paymentAccount = "CARD_PRIMARY",
                commitmentType = "COMMITTED",
                dateSpentMillis = day2
            ),
            ExpenseEntity(
                id = "exp_tech_conf",
                userId = userId,
                title = "DevCon Ticket Registration",
                amountLkr = 12500.0,
                category = "TECH",
                paymentAccount = "CARD_PRIMARY",
                commitmentType = "DISCRETIONARY",
                dateSpentMillis = day3
            ),
            ExpenseEntity(
                id = "exp_coffee_1",
                userId = userId,
                title = "Specialty Espresso & Croissant",
                amountLkr = 1650.0,
                category = "DINING_OUT",
                paymentAccount = "CASH",
                commitmentType = "DISCRETIONARY",
                dateSpentMillis = day3
            ),
            ExpenseEntity(
                id = "exp_coffee_2",
                userId = userId,
                title = "Work Session Café Latte & Muffin",
                amountLkr = 1850.0,
                category = "DINING_OUT",
                paymentAccount = "CARD_PRIMARY",
                commitmentType = "DISCRETIONARY",
                dateSpentMillis = day3
            ),
            ExpenseEntity(
                id = "exp_coffee_3",
                userId = userId,
                title = "Afternoon Cold Brew & Pastry",
                amountLkr = 1500.0,
                category = "DINING_OUT",
                paymentAccount = "CASH",
                commitmentType = "DISCRETIONARY",
                dateSpentMillis = day2
            ),
            ExpenseEntity(
                id = "exp_coffee_4",
                userId = userId,
                title = "Artisan Pour Over Coffee",
                amountLkr = 1400.0,
                category = "DINING_OUT",
                paymentAccount = "CASH",
                commitmentType = "DISCRETIONARY",
                dateSpentMillis = day1
            ),
            ExpenseEntity(
                id = "exp_coffee_5",
                userId = userId,
                title = "Client Café Meeting & Snacks",
                amountLkr = 3200.0,
                category = "DINING_OUT",
                paymentAccount = "CARD_PRIMARY",
                commitmentType = "DISCRETIONARY",
                dateSpentMillis = day2
            ),
            ExpenseEntity(
                id = "exp_ubereats_curr",
                userId = userId,
                title = "UberEats Late Night Dinner",
                amountLkr = 7400.0,
                category = "DINING_OUT",
                paymentAccount = "CARD_PRIMARY",
                commitmentType = "DISCRETIONARY",
                dateSpentMillis = day3
            ),
            ExpenseEntity(
                id = "exp_atm_transfer",
                userId = userId,
                title = "ATM Cash Withdrawal",
                amountLkr = 15000.0,
                category = "ATM_WITHDRAWAL",
                paymentAccount = "CASH",
                commitmentType = "TRANSFER",
                dateSpentMillis = day2
            )
        )
    }

    suspend fun seedCompleteScenario(
        userId: String,
        incomeRepository: IncomeRepository,
        expenseRepository: ExpenseRepository,
        goalRepository: GoalRepository,
        subscriptionRepository: SubscriptionRepository
    ): Result<Unit> {
        if (userId.isBlank()) {
            return Result.failure(IllegalArgumentException("Authenticated user ID required for seeding"))
        }

        return try {
            val now = System.currentTimeMillis()
            val cal = Calendar.getInstance()
            val currentYear = cal.get(Calendar.YEAR)

            // 1. Goals
            val goals = getSeedGoals(userId, now)
            goals.forEach { goalRepository.addGoal(it) }

            val macBookGoal = goals.first()
            goalRepository.addGoalContribution(
                userId = userId,
                contribution = GoalContributionEntity(
                    id = "nov_deposit_1",
                    goalId = macBookGoal.id,
                    userId = userId,
                    amountLkr = 25000.0,
                    sourceTitle = "Salary Allocation",
                    sourceId = "salary",
                    timestampMillis = getDynamicCurrentMonthDate(2),
                    note = "Monthly goal contribution from salary"
                )
            )

            // 2. Subscriptions
            val subscriptions = getSeedSubscriptions(userId, now)
            subscriptions.forEach { subscriptionRepository.addSubscription(it) }

            // 3. Multi-Channel Incomes
            val incomes = getSeedIncomes(userId, now, currentYear)
            incomes.forEach { incomeRepository.addIncome(it) }

            // 4. Expenses
            val expenses = getSeedExpenses(userId, now)
            expenses.forEach { expenseRepository.addExpense(it) }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
