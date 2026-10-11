# FinFlow (Kavindu Finance System) — Android Studio Agent Execution Specification

> **Target Platform:** Android (API 26 to 34+)
>
> **Tech Stack:** 100% Kotlin, Jetpack Compose (Material 3), Navigation Compose, MVVM + Coroutines/StateFlow, Hilt DI, Firebase Auth & Cloud Firestore (with Offline Cache enabled).
>
> **Currency Base:** LKR (Sri Lankan Rupee) with USD dual-currency conversion.

---

## 1. System Constants & Domain Rules

* **Target Goal:** LKR 490,000.00 (MacBook Pro M4 Fund, 12-Month Target).
* **Baseline Saved:** LKR 11,200.00.
* **Monthly Savings Run-Rate Target:** LKR 40,833.33/month ($490,000 / 12$).
* **Income Channels (4 strictly distinct sources):**
  1. `SALARY`: Arrives 25th of each month (Base LKR 120,000 + variable Performance Bonus up to LKR 140,000).
  2. `FREELANCE`: Milestone-based (Status: `DRAFT`, `INVOICED`, `PAID`, `OVERDUE`). Tracks client follow-up reminder alerts.
  3. `ADSENSE`: Multi-currency. Logged in `USD` (\$), converted to `LKR` with prevailing exchange rate.
  4. `CRYPTO`: Realized fiat P&L tracker. Tracks `fiatInjected` vs `fiatWithdrawn` to compute net realized P&L (not speculative asset valuation).
* **Expense Classification:**
  * `COMMITTED`: Rent (LKR 34,000), Utilities, Gym auto-debit, Subscriptions, Essential Commute.
  * `DISCRETIONARY`: PickMe, UberEats, Dining out, Coffee & Snacks, Impulses.
  * `PAYMENT_SOURCE`: `CARD_PRIMARY`, `ACCOUNT_SECONDARY`, `CASH`.

---

## 2. Recommended Package & Clean Architecture Structure

Strict Unidirectional Data Flow (UDF). No database calls in Composables. No direct ViewModel instantiations inside UI.

```
com.example.finflow/
├── FinFlowApp.kt                     // Application class (@HiltAndroidApp)
├── di/
│   ├── AppModule.kt                   // Coroutine dispatchers, Firebase instances
│   └── RepositoryModule.kt            // Interface-to-Implementation bindings
├── data/
│   ├── model/
│   │   ├── IncomeEntity.kt
│   │   ├── ExpenseEntity.kt
│   │   ├── GoalEntity.kt
│   │   └── SubscriptionEntity.kt
│   ├── repository/
│   │   ├── AuthRepository.kt / AuthRepositoryImpl.kt
│   │   ├── IncomeRepository.kt / IncomeRepositoryImpl.kt
│   │   ├── ExpenseRepository.kt / ExpenseRepositoryImpl.kt
│   │   ├── GoalRepository.kt / GoalRepositoryImpl.kt
│   │   └── SubscriptionRepository.kt / SubscriptionRepositoryImpl.kt
│   └── remote/
│       └── FirestoreService.kt        // Cloud Firestore client with offline cache
├── ui/
│   ├── common/
│   │   ├── UiState.kt                 // Sealed interface (Loading, Success, Empty, Error)
│   │   ├── EmptyStateView.kt          // Zero-crash graceful fallback UI
│   │   └── ErrorSnackbar.kt
│   ├── theme/
│   │   ├── Color.kt
│   │   ├── Theme.kt
│   │   └── Type.kt
│   ├── navigation/
│   │   ├── Screen.kt                  // Sealed class routes
│   │   └── NavGraph.kt                // Navigation Compose single-activity host
│   ├── components/
│   │   ├── QuickExpenseBottomSheet.kt
│   │   ├── CommitmentRatioBar.kt
│   │   ├── GoalProgressCard.kt
│   │   └── MetricCard.kt
│   └── screens/
│       ├── auth/
│       │   ├── LoginScreen.kt
│       │   └── AuthViewModel.kt
│       ├── dashboard/
│       │   ├── DashboardScreen.kt
│       │   ├── DashboardViewModel.kt
│       │   └── DashboardUiState.kt
│       ├── income/
│       │   ├── IncomeHubScreen.kt
│       │   └── IncomeViewModel.kt
│       ├── expenses/
│       │   ├── ExpenseAnalyticsScreen.kt
│       │   └── ExpenseViewModel.kt
│       ├── subscriptions/
│       │   ├── SubscriptionAuditScreen.kt
│       │   └── SubscriptionViewModel.kt
│       └── goal/
│           ├── GoalTrackerScreen.kt
│           └── GoalViewModel.kt
└── util/
    ├── CurrencyFormatter.kt           // LKR and USD formatting
    └── DateUtils.kt
```

---

## 3. Data Models & Firestore Document Schemas

### 3.1 Income Model (`incomes`)
```kotlin
data class IncomeEntity(
    val id: String = "",
    val userId: String = "",
    val channel: String = "SALARY", // SALARY, FREELANCE, ADSENSE, CRYPTO
    val title: String = "",
    val originalAmount: Double = 0.0,
    val originalCurrency: String = "LKR", // LKR, USD
    val exchangeRate: Double = 1.0,
    val realizedAmountLkr: Double = 0.0, // originalAmount * exchangeRate
    val dateReceivedMillis: Long = System.currentTimeMillis(),
    val status: String = "PAID", // PAID, INVOICED, OVERDUE, DRAFT
    val clientName: String? = null,
    val invoiceSentMillis: Long? = null,
    val allocatedToGoalLkr: Double = 0.0
)
```

### 3.2 Expense Model (`expenses`)
```kotlin
data class ExpenseEntity(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val amountLkr: Double = 0.0,
    val category: String = "DINING_OUT", // TRANSPORT, DINING_OUT, RENT, SUBSCRIPTION, UTILITIES, MISC
    val paymentAccount: String = "CARD_PRIMARY", // CARD_PRIMARY, ACCOUNT_SECONDARY, CASH
    val commitmentType: String = "DISCRETIONARY", // COMMITTED, DISCRETIONARY
    val dateSpentMillis: Long = System.currentTimeMillis(),
    val isRecurring: Boolean = false,
    val notes: String? = null
)
```

### 3.3 Goal Model (`goals`)
```kotlin
data class GoalEntity(
    val id: String = "",
    val userId: String = "",
    val title: String = "MacBook Pro M4 Fund",
    val targetAmountLkr: Double = 490000.0,
    val currentSavedLkr: Double = 11200.0,
    val targetDateMillis: Long = System.currentTimeMillis() + (365L * 24 * 60 * 60 * 1000),
    val monthlyTargetRunRateLkr: Double = 40833.33,
    val isCompleted: Boolean = false
)
```

### 3.4 Subscription & Recurring Cost Model (`subscriptions`)
*Addresses Kavindu's hidden recurring leaks: Rent (LKR 34,000), gym membership auto-debited from secondary account, and unused subscriptions.*
```kotlin
data class SubscriptionEntity(
    val id: String = "",
    val userId: String = "",
    val name: String = "", // e.g., "Gym Membership", "SaaS Cloud Plan"
    val amountLkr: Double = 0.0,
    val billingCycle: String = "MONTHLY", // MONTHLY, ANNUAL
    val paymentAccount: String = "ACCOUNT_SECONDARY", // CARD_PRIMARY, ACCOUNT_SECONDARY, CASH
    val status: String = "ACTIVE", // ACTIVE, UNUSED_FLAGGED, CANCELLED
    val nextBillingMillis: Long = System.currentTimeMillis(),
    val isFlaggedForReview: Boolean = false // Set true if Kavindu intended to cancel
)
```

---

## 4. UI State Architecture & Error/Offline Handling

To strictly comply with the SLIIT rubric (*"No unhandled exceptions, no blank screens on network failure, no crashes on empty datasets"*), all ViewModels must expose a reactive `UiState` via `StateFlow`.

### 4.1 Generic State Wrapper (`UiState.kt`)
```kotlin
sealed interface UiState<out T> {
    object Idle : UiState<Nothing>
    object Loading : UiState<Nothing>
    object Empty : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String, val throwable: Throwable? = null) : UiState<Nothing>
}
```

### 4.2 Dashboard State Contract (`DashboardUiState.kt`)
```kotlin
data class DashboardData(
    val totalIncomeLkr: Double = 0.0,
    val totalExpenseLkr: Double = 0.0,
    val committedExpenseLkr: Double = 0.0,
    val discretionaryExpenseLkr: Double = 0.0,
    val macBookGoalProgress: GoalEntity = GoalEntity(),
    val recentTransactions: List<ExpenseEntity> = emptyList(),
    val shockInsightMessage: String = ""
)
```

### 4.3 Firestore Offline Persistence Setup (`FirestoreService.kt`)
Must initialize Cloud Firestore with local cache settings so reads and writes function seamlessly offline without crashing when network connectivity drops:
```kotlin
val firestoreSettings = firestoreSettings {
    setLocalCacheSettings(persistentCacheSettings {
        // Automatically caches data to device disk for offline reading & writing
    })
}
Firebase.firestore.firestoreSettings = firestoreSettings
```

---

## 5. Dependency Injection Configuration (Hilt)

### 5.1 Application Class (`FinFlowApp.kt`)
```kotlin
@HiltAndroidApp
class FinFlowApp : Application()
```

### 5.2 Dependency Modules (`AppModule.kt` & `RepositoryModule.kt`)
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance().apply {
        firestoreSettings = firestoreSettings {
            setLocalCacheSettings(persistentCacheSettings {})
        }
    }

    @Provides
    @Singleton
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindExpenseRepository(impl: ExpenseRepositoryImpl): ExpenseRepository

    @Binds
    @Singleton
    abstract fun bindIncomeRepository(impl: IncomeRepositoryImpl): IncomeRepository

    @Binds
    @Singleton
    abstract fun bindGoalRepository(impl: GoalRepositoryImpl): GoalRepository

    @Binds
    @Singleton
    abstract fun bindSubscriptionRepository(impl: SubscriptionRepositoryImpl): SubscriptionRepository
}
```

---

## 6. UI Screens & Composable Specifications

### 6.1 DashboardScreen (`DashboardScreen.kt`)
* **State Holder:** `DashboardViewModel` exposing `StateFlow<UiState<DashboardData>>`.
* **Components:**
  1. **Greeting & Timeframe Header:** Active month with total net inflow vs total spend.
  2. **Commitment vs. Discretionary Gauge:** Split-color visual bar showing ratio of committed essentials vs discretionary lifestyle spend.
  3. **MacBook Pro M4 Tracker Widget:** Linear/Circular progress indicator displaying `currentSavedLkr` vs `LKR 490,000`, days remaining, and on-track status badge.
  4. **Shock-Factor Micro-Insight Banner:** Dynamic alert (e.g., *"LKR 24,000 spent on coffee & takeout this month equals 58% of your monthly MacBook quota"*).
  5. **Quick Action Buttons:** `+ Log Expense` (triggers 3-tap bottom sheet) and `+ Log Income`.
  6. **Recent Transactions Feed:** Grouped items with account badges (`Card`, `Cash`, `Secondary`). Displays `EmptyStateView` if no entries exist.

### 6.2 Multi-Channel Income Hub (`IncomeHubScreen.kt`)
* **Top Metric:** Realized Income This Month vs. Rolling 3-Month Average.
* **Segmented Tabs / Channel Cards:**
  * **Salary:** Base + bonus dual input, locked to 25th of month.
  * **Freelance:** Milestone cards with status pill (`PAID`, `INVOICED`, `OVERDUE`). "Remind Client" action for invoices > 14 days.
  * **AdSense (USD):** USD amount input + exchange rate field $\rightarrow$ auto-computes and displays realized LKR.
  * **Crypto P&L:** Running tally card displaying `Fiat Deposited` vs `Fiat Withdrawn` $\rightarrow$ Net Realized P&L badge (Green for gain, Red for loss).

### 6.3 Rapid Expense Entry Sheet (`QuickExpenseBottomSheet.kt`)
* **Design Rule:** Completable in under 5 seconds (maximum 3 taps).
* **Presets:** Quick-tap chips: `PickMe / Ride`, `UberEats`, `Coffee / Cafe`, `Groceries`, `Rent`.
* **Account Selector:** `Card (Primary)`, `Secondary (Auto-debited)`, `Cash`.
* **Commitment Toggle:** Instant toggle between `Committed` (Fixed obligation) and `Discretionary` (Lifestyle choice).

### 6.4 Subscription & Recurring Auditor (`SubscriptionAuditScreen.kt`)
* **Rent & Fixed Outflow Counter:** Prominently tracks the LKR 34,000 rent + utilities baseline.
* **Secondary Account Scanner:** Lists recurring auto-debits (gym membership, streaming services).
* **Zombie Subscription Flagging:** Highlights subscriptions with "Cancel / Keep" decision triggers.

### 6.5 MacBook Pro M4 Goal & Windfall Allocator (`GoalTrackerScreen.kt`)
* **Run-Rate Visual:** Progress bar displaying LKR 11,200 baseline up to LKR 490,000.
* **Windfall Allocator Modal:** Triggered whenever irregular freelance or AdSense income is logged. Offers an allocation slider (e.g., 20% to 100%) to divert a chunk of the lump sum directly into the MacBook fund.
* **"What-If" Opportunity Cost Calculator:** Interactive slider showing how saving LKR $X$ on dining out pulls the target delivery date forward by $N$ weeks.

---

## 7. Firestore Security Rules (`firestore.rules`)

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    function isOwner(userId) {
      return request.auth != null && request.auth.uid == userId;
    }

    match /users/{userId} {
      allow read, write: if isOwner(userId);

      match /incomes/{incomeId} {
        allow read, write: if isOwner(userId);
      }
      match /expenses/{expenseId} {
        allow read, write: if isOwner(userId);
      }
      match /goals/{goalId} {
        allow read, write: if isOwner(userId);
      }
      match /subscriptions/{subscriptionId} {
        allow read, write: if isOwner(userId);
      }
    }
  }
}
```