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

    fun getSeedGoals(userId: String, now: Long = System.currentTimeMillis()): List<GoalEntity> {
        return listOf(
            GoalEntity(
                id = "macbook_m4_goal",
                userId = userId,
                title = "MacBook Pro M4 Fund",
                targetAmountLkr = 490000.0,
                currentSavedLkr = 11200.0,
                targetDateMillis = now + (365L * 24 * 60 * 60 * 1000L),
                monthlyTargetRunRateLkr = 40833.33,
                initialDepositLkr = 11200.0,
                isCompleted = false,
                createdAtMillis = now - (30L * 24 * 60 * 60 * 1000L)
            )
        )
    }

    fun getSeedSubscriptions(userId: String = "kavindu_demo", now: Long = System.currentTimeMillis()): List<SubscriptionEntity> {
        return listOf(
            // Committed Fixed: Rent LKR 34,000 on Secondary Account
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
                nextBillingMillis = now + 14L * 24 * 3600 * 1000L
            ),
            // Committed Fixed: Gym Membership Auto-Debit on Secondary Account
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
                nextBillingMillis = now + 9L * 24 * 3600 * 1000L
            ),
            // Zombie Subscription 1: Cloud SaaS Video Streaming (Intended to cancel 6 months ago)
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
                nextBillingMillis = now + 4L * 24 * 3600 * 1000L
            ),
            // Zombie Subscription 2: Pro Dev Server / Cloud Utility (Intended to cancel 6 months ago)
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
                nextBillingMillis = now + 6L * 24 * 3600 * 1000L
            )
        )
    }

    fun getSeedIncomes(
        userId: String = "kavindu_demo",
        now: Long = System.currentTimeMillis(),
        currentYear: Int = Calendar.getInstance().get(Calendar.YEAR)
    ): List<IncomeEntity> {
        fun getMillisInQuarter(monthIndex0: Int, day: Int): Long {
            val c = Calendar.getInstance()
            c.set(Calendar.YEAR, currentYear)
            c.set(Calendar.MONTH, monthIndex0)
            c.set(Calendar.DAY_OF_MONTH, day)
            c.set(Calendar.HOUR_OF_DAY, 11)
            return c.timeInMillis
        }

        return listOf(
            // Channel 1: Salary (Current Month base 120,000 + bonus 20,000)
            IncomeEntity(
                id = "inc_salary_curr",
                userId = userId,
                channel = "SALARY",
                title = "Monthly Base Salary & Performance Bonus",
                originalAmount = 140000.0,
                originalCurrency = "LKR",
                realizedAmountLkr = 140000.0,
                status = "PAID",
                dateReceivedMillis = now - 2L * 24 * 3600 * 1000L
            ),
            // Channel 2: Freelance Paid milestone
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
                dateReceivedMillis = now - 7L * 24 * 3600 * 1000L
            ),
            // Channel 2: Freelance Pending / Overdue Invoice (Triggers follow-up alert!)
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
                invoiceSentMillis = now - 19L * 24 * 3600 * 1000L,
                dateReceivedMillis = now - 19L * 24 * 3600 * 1000L
            ),
            // Channel 3: Google AdSense USD payout converted to LKR
            IncomeEntity(
                id = "inc_adsense_curr",
                userId = userId,
                channel = "ADSENSE",
                title = "Tech Blog AdSense Payout (\$62)",
                originalAmount = 62.0,
                originalCurrency = "USD",
                exchangeRate = 304.5,
                realizedAmountLkr = 18879.0,
                status = "PAID",
                dateReceivedMillis = now - 11L * 24 * 3600 * 1000L
            ),
            // Channel 4: Crypto Q1 Significant Gain (Binance USDT/ETH)
            IncomeEntity(
                id = "inc_crypto_q1_gain",
                userId = userId,
                channel = "CRYPTO",
                title = "Binance USDT/ETH Realized Gain (Q1)",
                originalAmount = 45000.0,
                originalCurrency = "LKR",
                realizedAmountLkr = 45000.0,
                status = "PAID",
                dateReceivedMillis = getMillisInQuarter(1, 18) // February (Q1)
            ),
            // Channel 4: Crypto Q3 Material Loss (Binance Altcoin drop)
            IncomeEntity(
                id = "inc_crypto_q3_loss",
                userId = userId,
                channel = "CRYPTO",
                title = "Altcoin Realized Trading Loss (Q3)",
                originalAmount = -23000.0,
                originalCurrency = "LKR",
                realizedAmountLkr = -23000.0,
                status = "PAID",
                dateReceivedMillis = getMillisInQuarter(7, 22) // August (Q3)
            )
        )
    }

    fun getSeedExpenses(userId: String = "kavindu_demo", now: Long = System.currentTimeMillis()): List<ExpenseEntity> {
        return listOf(
            // Committed: Rent LKR 34,000 (Current month)
            ExpenseEntity(
                id = "exp_rent_curr",
                userId = userId,
                title = "Monthly House Rent",
                amountLkr = 34000.0,
                category = "RENT",
                paymentAccount = "ACCOUNT_SECONDARY",
                commitmentType = "COMMITTED",
                dateSpentMillis = now - 5L * 24 * 3600 * 1000L
            ),
            // Committed: Gym Auto-Debit LKR 4,500 (Secondary Account)
            ExpenseEntity(
                id = "exp_gym_curr",
                userId = userId,
                title = "Gym Membership (Secondary Auto-Debit)",
                amountLkr = 4500.0,
                category = "FITNESS",
                paymentAccount = "ACCOUNT_SECONDARY",
                commitmentType = "COMMITTED",
                dateSpentMillis = now - 12L * 24 * 3600 * 1000L
            ),
            // Committed: Fiber Home Broadband
            ExpenseEntity(
                id = "exp_wifi_curr",
                userId = userId,
                title = "Dialog Fiber Broadband",
                amountLkr = 5800.0,
                category = "UTILITIES",
                paymentAccount = "CARD_PRIMARY",
                commitmentType = "COMMITTED",
                dateSpentMillis = now - 6L * 24 * 3600 * 1000L
            ),
            // DISCRETIONARY HABIT LEAK: Coffee & Pastry Shock (Kavindu spends LKR 24,000/mo on café visits)
            ExpenseEntity(
                id = "exp_coffee_1",
                userId = userId,
                title = "Specialty Espresso & Croissant",
                amountLkr = 1650.0,
                category = "DINING_OUT",
                paymentAccount = "CASH",
                commitmentType = "DISCRETIONARY",
                dateSpentMillis = now - 1L * 24 * 3600 * 1000L
            ),
            ExpenseEntity(
                id = "exp_coffee_2",
                userId = userId,
                title = "Work Session Café Latte & Muffin",
                amountLkr = 1850.0,
                category = "DINING_OUT",
                paymentAccount = "CARD_PRIMARY",
                commitmentType = "DISCRETIONARY",
                dateSpentMillis = now - 3L * 24 * 3600 * 1000L
            ),
            ExpenseEntity(
                id = "exp_coffee_3",
                userId = userId,
                title = "Afternoon Cold Brew & Pastry",
                amountLkr = 1500.0,
                category = "DINING_OUT",
                paymentAccount = "CASH",
                commitmentType = "DISCRETIONARY",
                dateSpentMillis = now - 4L * 24 * 3600 * 1000L
            ),
            ExpenseEntity(
                id = "exp_coffee_4",
                userId = userId,
                title = "Artisan Pour Over Coffee",
                amountLkr = 1400.0,
                category = "DINING_OUT",
                paymentAccount = "CASH",
                commitmentType = "DISCRETIONARY",
                dateSpentMillis = now - 7L * 24 * 3600 * 1000L
            ),
            ExpenseEntity(
                id = "exp_coffee_5",
                userId = userId,
                title = "Client Café Meeting & Snacks",
                amountLkr = 3200.0,
                category = "DINING_OUT",
                paymentAccount = "CARD_PRIMARY",
                commitmentType = "DISCRETIONARY",
                dateSpentMillis = now - 9L * 24 * 3600 * 1000L
            ),
            ExpenseEntity(
                id = "exp_ubereats_curr",
                userId = userId,
                title = "UberEats Late Night Meals",
                amountLkr = 7400.0,
                category = "DINING_OUT",
                paymentAccount = "CARD_PRIMARY",
                commitmentType = "DISCRETIONARY",
                dateSpentMillis = now - 8L * 24 * 3600 * 1000L
            ),
            // INTERNAL TRANSFER: ATM Cash Withdrawal LKR 15,000 (Must NOT be counted as an expense!)
            ExpenseEntity(
                id = "exp_atm_transfer",
                userId = userId,
                title = "ATM Cash Withdrawal",
                amountLkr = 15000.0,
                category = "ATM_WITHDRAWAL",
                paymentAccount = "CASH",
                commitmentType = "TRANSFER",
                dateSpentMillis = now - 10L * 24 * 3600 * 1000L
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
                    id = "initial_contrib",
                    goalId = macBookGoal.id,
                    userId = userId,
                    amountLkr = 11200.0,
                    sourceTitle = "Initial Savings Baseline",
                    sourceId = "seed",
                    timestampMillis = macBookGoal.createdAtMillis,
                    note = "Saved after 12 months of earning"
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
