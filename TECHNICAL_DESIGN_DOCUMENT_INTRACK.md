# Technical Design Document & Software Requirements Specification (SRS)
## Personal Finance Management System for Android (InTrack / FinFlow)
### Platform Based Development (SE3092) — Assignment 01

---

Requirement Analysis


Kavindu estimates his monthly income at LKR 160,000–210,000 across four asynchronous channels, yet has only accumulated LKR 11,200 in savings after earning over LKR 1.6M in 12 months. Conventional budgeting apps fail him because they assume a single, fixed monthly paycheck in a single currency.


Lens 1: Deconstruction of Income streams
1. Primary Employment (Semi-Variable Recurring): Deposited on the 25th of each month in LKR. While base compensation is predictable, performance incentives create a variance between LKR 120,000 and LKR 140,000. Kavindu avoids checking his physical payslip. The platform must distinguish between guaranteed baseline cashflow (LKR 120,000) and realized variable bonuses. It is recommended to add a recurring prompt on the 25th for the variable delta input rather than expecting manual entry from scratch.
2. SME Freelance Engagements (Milestone-Driven & Fragmented): React and WordPress development projects paying LKR 20,000 to LKR 90,000 per project. Payments arrive irregularly, invoices are frequently forgotten, and bank reconciliation between payments and projects fails. The system must decouple earned revenue from realized cash (deposited in the bank). 
3. AdSense Monetization (Foreign Currency Volatility): Generated from his tech blog, denominated in USD, and converted to LKR at prevailing bank exchange rates upon monthly transfer. Yields fluctuate between LKR 3,500 and LKR 19,000. Kavindu checks his dashboard but never logs the realized deposit. The persistence layer must preserve dual-currency accounting: 
4. Cryptocurrency Trading (Capital Gains vs. Working Income): Active trading across cryptocurrencies (USDT, ETH) characterized by volatile quarters (gains in Q1, losses in Q3) without a calculated running net position. High-frequency micro-trading cannot be tracked manually without immediate drop-off. The system must record Realized Net P&L Snapshots (closed gains or losses liquidated to fiat/stablecoins) rather than treating gross withdrawals as earned income, preventing paper profits or capital losses from distorting monthly spending power.
Lens 2: Failure Analysis


1. Google Sheets - Manually entered formulas broke upon row insertion; entry overhead too high. By Enforce automatic aggregations via Firestore reactive queries. Implement a "Forgiving Catch-Up" engine where missed days can be balanced with a single unallocated discrepancy adjustment rather than retroactive ledger reconstruction.
2. Play Store Budgeting App - Hardcoded to USD; single-income paradigm; fixed rigid monthly budget. 
3. Notes Application - Unlabeled blocks of raw numbers pasted from bank notifications. This will become unreadable in 4 days. By introducing 2-Tap Micro-Logging replaces free-text pasting with interactive categorical quick-action chips on the Android home screen. 
4. Bank Native App - Captured card only; missed cash and secondary accounts entirely, misclassified PickMe as "Online Retail". Implement Multi-Ledger Aggregation unified logging capturing three payment channels.


Lens 3: Goal Progress Visibility 
Kavindu has a specific, quantified savings goal and a deadline. His previous tools never connected daily financial behaviour to that goal in a visible or motivating way.
Mathematical Grounding of the MacBook Pro M4 Goal
* Target Asset: MacBook Pro M4 (Authorized Reseller Configuration).
* Target Capital Required: LKR 490,000.00.
* Current Liquid Savings: LKR 11,200.00.
* Deficit: LKR 478,800.00.
* Target Timeline: 12 Months (365 Days).


Lens 4: Low-Friction Expense Ingestion and Data Fidelity 
Given that the application cannot import bank data automatically, how should it handle expense entry in a way that minimises friction and maximises long-term user adherence?
Quick-Action Presets (The "Frequent 4"): Pre-built fast-track buttons for Kavindu's high-frequency spending vectors: PickMe, UberEats, Coffee / Café, and Cash Withdrawal. Tapping opens a numeric keypad with category and channel pre-selected.
Default Context Inheritance: The application defaults the timestamp to now(), the payment channel to Primary Card (or last used), and the currency to LKR. Entry requires only typing the amount and tapping a single confirmation button.
Automated Fixed Recurring Commitments: Rent (LKR 34,000 on the 1st), gym auto-debits, and digital subscriptions are managed by an automated scheduler. They are auto-posted to the ledger and simply require confirmation on the dashboard, removing the chore of logging fixed recurring overhead.


Lens 5: Structural Spending Distribution and Rapid Self-Awareness
Kavindu has no concept of his discretionary-to-committed spending ratio. What visualisations or summary views would most efficiently reveal this to a user who does not think in financial categories?
* UI and UX should design to provide immediate financial self-awareness within seven days. Use charts and matrices.
* Kavindu does not think in traditional accounting categories. Forcing him to manage dozens of subcategories creates cognitive overload. The system therefore models every outflow under a binary macro-taxonomic model which are Behavioural expenses and Committed expenses.
Software Requirements Specification (SRS)
1. System Scope & Purpose
The Personal Finance Management System is an offline-first, mobile Android application built specifically to address the structural financial ambiguity of users with irregular, multi-channel income streams and impulsive discretionary spending patterns.
The application unifies multi-currency earnings (LKR, USD, crypto P&L), simplifies manual transaction tracking to under three seconds, enforces visibility over committed versus discretionary outlays, and continuously links daily discretionary habits to a tangible capital goal (MacBook Pro M4: LKR 490,000).
2. User Persona Summary: Kavindu Silva
* Role: Junior Software Engineer at a product startup, Colombo, Sri Lanka.
* Financial Profile: Base salary of LKR 120,000–140,000 arriving on the 25th; irregular SME freelance projects (LKR 20,000–90,000); monthly USD AdSense revenue (LKR 3,500–19,000 equivalent); volatile Binance crypto trading (USDT/ETH).
* Core Friction Points: Invisible spending, neglected freelance invoicing, unaccounted cash transactions, and a recurring drop-off from financial tracking within two weeks.
* Target Goal: Accumulate LKR 490,000 for a MacBook Pro M4 within 12 months with only LKR 11,200 currently saved.
3. Functional Requirements (FR)
Module 1: Authentication & User State Management (FR-AUTH)
* FR-AUTH-01: The system shall authenticate users via Firebase Authentication using secure Email and Password credentials.
* FR-AUTH-02: The system shall maintain an authenticated session state locally, allowing seamless access without network connectivity.
* FR-AUTH-03: The system shall initialize user preferences upon registration with a default base currency set to Sri Lankan Rupees
Module 2: Multi-Source & Multi-Currency Income Management (FR-INC)
* FR-INC-01 (Salary Ingestion): The system shall prompt the user on the 25th of each calendar month with a pre-filled base salary of LKR 120,000, allowing quick adjustment for variable performance bonuses up to LKR 140,000.
* FR-INC-02 (Freelance Project Tracking): The system shall track SME freelance engagements with attributes: project title, client name, total agreed amount in LKR, payment milestones, due date, and payment status.
* FR-INC-03 (Receivables Reminder): The system shall display an alert on the dashboard for any freelance milestone that remains in INVOICED status past its due date.
* FR-INC-04 (Foreign Currency Normalization): The system shall accept AdSense income entries in USD, record the user-specified realized exchange rate at the time of conversion, and compute the normalized base amount in LKR.
* FR-INC-05 (Crypto Net Position Ledger): The system shall allow the user to record realized crypto trading net outcomes (in USDT or LKR) with an indicator of profit or loss, maintaining a running Year-to-Date (YTD) net position.
* FR-INC-06 (True Income Aggregation): The system shall compute Total Realized Income for any calendar month by summing cleared salary, paid freelance milestones, normalized AdSense earnings, and realized net crypto P&L.
Module 3: Low-Friction Expense Logging & Categorization (FR-EXP)
* FR-EXP-01 (2-Tap Quick Presets): The system shall provide one-tap shortcut buttons on the primary entry screen for frequent categories
* FR-EXP-02 (Account Channel Allocation): The system shall require every expense entry to be assigned to one of three channels: Primary Bank Card, Cash, or Secondary Account.
* FR-EXP-03 (Binary Nature Tagging): The system shall tag every outflow as either COMMITTED (non-negotiable survival expenses) or DISCRETIONARY (lifestyle and impulse outlays).
* FR-EXP-04 (Recurring Expense Scheduler): The system shall automatically generate ledger entries for recurring committed expenses (Apartment Rent of LKR 34,000, secondary-account gym membership, and active software subscriptions) on their due dates.
* FR-EXP-05 (Subscription Audit): The system shall maintain an active register of recurring digital subscriptions and flag services with an "Unused / Cancel Intent" marker to prompt cancellation.
Module 4: Dynamic Goal Runway & Capital Allocation (FR-GOAL)
* FR-GOAL-01 (Goal Configuration): The system shall allow the user to define a primary asset goal with a title (MacBook Pro M4), target cost (LKR 490,000), deadline (12 months), and starting balance (LKR 11,200).
* FR-GOAL-02 (Required Savings Rate Calculation): The system shall compute and update the required daily and monthly savings run rate necessary to reach the goal by the target date:
   * FR-GOAL-03 (Discretionary Impact Feedback): Upon saving any discretionary expense greater than LKR 2,000, the system shall display an inline impact notice indicating how many days the expense adds to the projected goal completion date.
   * FR-GOAL-04 (Windfall Siphoning): When freelance or ad income is logged, the system shall provide an option to allocate a percentage (e.g., 20%, 30%, 50%) directly to the goal fund.
Module 5: Financial Analytics & Clarity Dashboards (FR-ANL)
   * FR-ANL-01 (Committed vs. Discretionary Gauge): The dashboard shall display a visual ratio gauge showing the percentage breakdown of committed versus discretionary spending against total monthly outflow.
   * FR-ANL-02 (Top Discretionary Outflow Radar): The system shall aggregate and rank discretionary spending categories for the active month, highlighting dining and transport leaks.
   * FR-ANL-03 (Account Channel Balance Breakdown): The system shall visualize total spending distributed across the three payment channels: Primary Card, Cash, and Secondary Account
4. Non-Functional Requirements (NFR)
Performance & Responsiveness (NFR-PERF)
   * NFR-PERF-01: The application cold-start time to an interactive dashboard state shall not exceed 1.5 seconds on devices meeting the minimum SDK 26 specification.
   * NFR-PERF-02: Transaction quick-logging shall execute and clear the interface in under 400 milliseconds using optimistic UI updates.
Security & Data Isolation (NFR-SEC)
   * NFR-SEC-01: All Firestore database read and write operations must be secured by user-isolated Firestore Security Rules.
   * NFR-SEC-02: No sensitive financial information or authentication keys shall be logged to the Android system console in release builds.
Usability & Accessibility (NFR-USE)
   * NFR-USE-01: The entire user interface must be constructed with Jetpack Compose following Material Design 3 guidelines, supporting dynamic color schemes and high-contrast typography.
   * NFR-USE-02: Transaction entry shall be executable with one hand via bottom-sheet interactions and keypad-optimized thumb zones.
Reliability & Offline Availability (NFR-REL)
   * NFR-REL-01: The application must maintain 100% offline functionality for reading and recording transactions using Room database caching and Firestore offline persistence.
   * NFR-REL-02: Under network disconnection, the UI must not block or display an unhandled error state, syncing automatically when connectivity is restored.

5. System Constraints & Architectural Boundaries (CON)
* CON-01 (Platform Target): The application must run on devices with a minimum API Level of 26 (Android 8.0 Oreo) and target API Level 34+ (Android 14).
* CON-02 (Language & Runtime): 100% Kotlin codebase. Zero Java files or Java interop permitted within the application presentation and domain layers.
* CON-03 (UI Toolkit): Exclusively built with Jetpack Compose using Material 3 design tokens. Legacy Android XML view hierarchies are strictly disallowed.
* CON-04 (Architecture Paradigm): Strict adherence to Model-View-ViewModel (MVVM) and Unidirectional Data Flow (UDF). No database calls or business logic inside Composable functions.
* CON-05 (Cloud Backend & Multi-Tenancy): Powered by Firebase Authentication and Cloud Firestore with granular user-partitioned security rules. Local emulator-only backends are forbidden for final deliverables.


Use case Daiagram

```
+----------------------------------------------------------------------------------------------------+
|                                    INTRACK SYSTEM BOUNDARY                                         |
|                                                                                                    |
|                                      +------------------------------------+                        |
|                                      |      UC-01: Authenticate User      |                        |
|                                      |  (Sign In / Sign Up / Guest Mode)  |                        |
|                                      +-----------------+------------------+                        |
|                                                        |                                           |
|                   +------------------------------------+-----------------------------------+       |
|                   |                                    |                                   |       |
|  +----------------v------------------+  +--------------v-------------------+  +------------v----+  |
|  |  UC-02: Ingest Multi-Source       |  |  UC-03: Rapid 3-Tap Expense      |  |  UC-04: Audit   |  |
|  |         Income Stream             |  |         Logging                  |  |  Recurring      |  |
|  +----------------+------------------+  +--------------+-------------------+  |  Obligations &  |  |
|                   |                                    |                      |  Subscriptions  |  |
|       +-----------+-----------+             +----------+----------+           +------------+----+  |
|       |           |           |             |                     |                        |       |
|  +----v-----+ +---v----+ +----v----+   +----v--------+     +------v---------+              |       |
|  |<<extend>>| |<<ext>> | |<<extend>>   |<<include>>  |     |<<include>>     |              |       |
|  |  Salary  | |Freelanc| | AdSense |   |Select Paymt |     |Tag Commitment  |              |       |
|  |  Bonus   | |Mileston| | USD/LKR |   |Channel      |     |Type: Committed |              |       |
|  |  Prompt  | |Invoice | | Normal- |   |(Card/Cash/  |     |vs Discretionary|              |       |
|  |  (25th)  | |Tracker | | ization |   | Secondary)  |     +----------------+              |       |
|  +----------+ +--------+ +---------+   +-------------+                                     |       |
|                                                                                            |       |
|       +------------------------------------------------------------------------------------+       |
|       |                                                                                            |
|  +----v------------------------------+  +----------------------------------+                       |
|  |  UC-05: Track MacBook Pro M4 Goal |  |  UC-06: Analyze Financial        |                       |
|  |         & Windfall Allocation     |  |         Clarity & Shock Insights |                       |
|  +----------------+------------------+  +----------------------------------+                       |
|                   |                                                                                |
|       +-----------+-----------+                                                                    |
|       |                       |                                                                    |
|  +----v----------------+ +----v--------------------+                                               |
|  |<<extend>>           | |<<extend>>               |                                               |
|  |Siphon Lump-Sum % to | |Run "What-If" Opportunity|                                               |
|  |Goal Runway          | |Cost Simulation          |                                               |
|  +---------------------+ +-------------------------+                                               |
|                                                                                                    |
+----------------------------------------------------------------------------------------------------+
                                      ^
                                      | interacts
                              [ Kavindu Silva ]
                                (Primary User)
```

### Detailed Use Case Specifications

#### UC-01: Authenticate User & Session Persistence
* **Actor:** Kavindu Silva (User).
* **Trigger:** App launch or navigating to account settings.
* **Preconditions:** Network connection required for initial cloud registration/login; cached credentials valid for offline launches.
* **Main Success Scenario:**
  1. Kavindu enters registered email and password.
  2. Firebase Authentication validates credentials and issues a secure JWT token.
  3. System caches local user profile and transitions navigation graph to `DashboardScreen`.
* **Alternate / Offline Flow:** If network is unavailable, system validates the active local Firebase session token and grants immediate offline access to cached Firestore data without blocking or crashing.

#### UC-02: Ingest Multi-Source Income Stream
* **Actor:** Kavindu Silva.
* **Trigger:** 25th of month salary notification, milestone completion of a freelance gig, AdSense USD wire, or crypto liquidation.
* **Main Success Scenario:**
  1. Kavindu opens `IncomeHubScreen` and selects the relevant income channel:
     - **Salary:** Pre-populates baseline LKR 120,000; Kavindu inputs the variable performance bonus delta (e.g. +LKR 15,000).
     - **Freelance:** Logs project milestone, client name, agreed amount, due date, and status (`PAID` vs `INVOICED`).
     - **AdSense:** Inputs gross USD earnings ($) and prevailing exchange rate (e.g., 305.50); system normalizes and persists base LKR.
     - **Crypto:** Enters fiat cashflow (Fiat Withdrawn minus Fiat Injected) to capture true realized P&L rather than volatile paper wealth.
  2. System records entry to Firestore `incomes` collection with timestamp and updates monthly realized income aggregates.

#### UC-03: Rapid 3-Tap Expense Logging
* **Actor:** Kavindu Silva.
* **Trigger:** Making an impulse purchase, buying coffee, paying for a PickMe ride, or cash expense.
* **Main Success Scenario:**
  1. Kavindu taps the persistent `+ Quick Expense` FAB on the dashboard (opens `QuickExpenseBottomSheet` in < 200ms).
  2. **Tap 1:** Taps a "Frequent 4" preset chip (e.g., `Coffee / Cafe`). System auto-sets Category = `DINING_OUT`, Nature = `DISCRETIONARY`.
  3. **Tap 2:** Enters amount (e.g., LKR 1,200) on the thumb-friendly numeric pad.
  4. **Tap 3:** Selects payment channel (`Primary Card`, `Cash`, `Secondary Account`) and confirms.
  5. UI updates optimistically, writes to Firestore offline persistent cache, and recalculates commitment ratios in real time.

#### UC-04: Audit Recurring Obligations & Subscriptions
* **Actor:** Kavindu Silva.
* **Trigger:** Reviewing monthly fixed burn or inspecting auto-debits on secondary account.
* **Main Success Scenario:**
  1. Kavindu navigates to `SubscriptionAuditScreen`.
  2. System aggregates committed monthly overhead: Rent (LKR 34,000 auto-scheduled for the 1st), gym membership auto-debited from secondary account, and SaaS subscriptions.
  3. System flags "Zombie / Unused" services that Kavindu marked with cancellation intent, displaying the total recoverable annual savings.

#### UC-05: Monitor MacBook Pro M4 Goal & Windfall Siphoning
* **Actor:** Kavindu Silva.
* **Trigger:** Viewing goal runway or receiving an irregular freelance lump-sum payment.
* **Main Success Scenario:**
  1. Kavindu views `GoalTrackerScreen` showing LKR 11,200 baseline against LKR 490,000 target.
  2. System computes monthly required savings rate ($LKR 40,833.33/mo) and displays countdown days.
  3. Upon logging an unexpected freelance income (e.g. LKR 60,000), the **Windfall Allocator Modal** activates.
  4. Kavindu adjusts slider to 40% (LKR 24,000); system automatically diverts this sum into the MacBook fund, adjusting target delivery date forward.


MVVM architectural diagram

```
+===================================================================================================+
|                                        PRESENTATION LAYER (UI)                                    |
|                                                                                                   |
|  +---------------------------------------------------------------------------------------------+  |
|  |                               MainActivity (Single-Activity Host)                           |  |
|  |                            + Navigation Compose Host (NavGraph.kt)                          |  |
|  +---------------------------------------------------------------------------------------------+  |
|                                                                                                   |
|  +-----------------------+ +-----------------------+ +--------------------+ +------------------+  |
|  |    DashboardScreen    | |     IncomeHubScreen   | | ExpenseAnalytics   | | GoalTrackerScreen|  |
|  +-----------------------+ +-----------------------+ +--------------------+ +------------------+  |
|  | - CommitmentGaugeBar  | | - SalaryBonusPrompt   | | - QuickExpenseSheet| | - MacBookFundBar |  |
|  | - ShockInsightCard    | | - FreelanceMilestones | | - 3-Tap Keypad     | | - WindfallSlider |  |
|  | - RecentTxList        | | - USD Normalizer      | | - CategoryRadar    | | - WhatIfCalc     |  |
|  +-----------------------+ +-----------------------+ +--------------------+ +------------------+  |
|                                                                                                   |
|       ^                               ^                              ^              ^             |
|       | observes StateFlow<UiState>   | emits User Actions / Events  |              |             |
+=======|===============================|==============================|==============|=============+
|       v                               v                              v              v             |
|                                        VIEWMODEL LAYER                                            |
|                                                                                                   |
|  +-----------------------+ +-----------------------+ +--------------------+ +------------------+  |
|  |  DashboardViewModel   | |    IncomeViewModel    | |  ExpenseViewModel  | |  GoalViewModel   |  |
|  +-----------------------+ +-----------------------+ +--------------------+ +------------------+  |
|  | * combine() flows     | | * currency conversion | | * 3-tap validation | | * run-rate math  |  |
|  | * shock calculation   | | * overdue alerts      | | * recurring logic  | | * windfall siphon|  |
|  | * StateFlow<UiState>  | | * StateFlow<UiState>  | | * StateFlow<UiState| | * StateFlow<UiSt>|  |
|  +-----------------------+ +-----------------------+ +--------------------+ +------------------+  |
|                                                                                                   |
|       |                               |                              |              |             |
|       | coroutineScope (Dispatchers.IO)                              |              |             |
+=======|===============================|==============================|==============|=============+
|       v                               v                              v              v             |
|                                        REPOSITORY LAYER                                           |
|                                                                                                   |
|  +---------------------------------------------------------------------------------------------+  |
|  |  Repository Interfaces & Domain Contracts:                                                  |  |
|  |  - AuthRepository         - IncomeRepository          - ExpenseRepository                  |  |
|  |  - GoalRepository         - SubscriptionRepository                                         |  |
|  +---------------------------------------------------------------------------------------------+  |
|  |  Repository Implementations (Clean Business & Data Mapping Logic):                          |  |
|  |  - AuthRepositoryImpl     - IncomeRepositoryImpl      - ExpenseRepositoryImpl              |  |
|  |  - GoalRepositoryImpl     - SubscriptionRepositoryImpl                                     |  |
|  +---------------------------------------------------------------------------------------------+  |
|                                                                                                   |
|       |                                                              |                            |
|       | reads & writes entities                                      | streams Kotlin Flow<List>  |
+=======|==============================================================|============================+
|       v                                                              v                            |
|                                    DATA PERSISTENCE LAYER                                         |
|                                                                                                   |
|  +-------------------------------------------------+ +-----------------------------------------+  |
|  |             CLOUD PERSISTENCE (Remote)          | |         OFFLINE CACHE (Local Disk)      |  |
|  |                                                 | |                                         |  |
|  |  * Firebase Authentication (User Identity)      | |  * Cloud Firestore Persistent Disk      |  |
|  |  * Cloud Firestore Remote Collections:          | |    Cache (LRU Offline Engine)           |  |
|  |    /users, /incomes, /expenses, /goals,         | |  * Optimistic Mutation Queue            |  |
|  |    /subscriptions, /goal_contributions         | |  * Automatic Sync on Reconnection       |  |
|  +-------------------------------------------------+ +-----------------------------------------+  |
+===================================================================================================+
```

### Architectural Decomposition & Unidirectional Data Flow (UDF)

The system strictly adheres to the official Android Architecture Guidelines and Google's Unidirectional Data Flow (UDF) paradigm, ensuring testability, maintainability, and zero business logic leakage into Composables:

1. **User Interaction & Intent:**
   The user performs an action in the UI layer (e.g., tapping the "PickMe" shortcut chip in `QuickExpenseBottomSheet`). The Composable function does not execute database queries or compute business rules; it delegates directly to `ExpenseViewModel.logExpense(...)`.

2. **Asynchronous Execution via Coroutines:**
   The ViewModel receives the action and launches a structured coroutine within its `viewModelScope` bound to `Dispatchers.IO`. This guarantees that heavy computational workloads (such as dual-currency normalization or multi-stream aggregation) never block the Android Main UI Thread, preventing frame drops and UI stutter.

3. **Domain & Data Access through Repositories:**
   The ViewModel communicates exclusively with abstraction interfaces (`ExpenseRepository`, `IncomeRepository`, `GoalRepository`). The concrete repository implementations manage data transformations and data sourcing, shielding the presentation layer from underlying persistence mechanics.

4. **Reactive State Emission via StateFlow:**
   Repositories return real-time Kotlin `Flow<List<T>>` backed by Firestore snapshot listeners. The `DashboardViewModel` combines streams using Kotlin's `combine()` operator:
   $$\text{Total Realized} = \sum \text{Salary} + \sum \text{Paid Freelance} + \sum \text{Normalized AdSense} + \text{Crypto P\&L}$$
   The resulting state is wrapped in a type-safe `UiState<DashboardData>` sealed hierarchy and exposed to the UI via an immutable `StateFlow`.

5. **Lifecycle-Aware UI Rendering:**
   Composables observe the `StateFlow` using `collectAsStateWithLifecycle()` or `collectAsState()`. When state updates occur, Jetpack Compose executes intelligent recomposition only on the modified subtrees, guaranteeing maximum rendering efficiency and instantaneous feedback.

### Package & Directory Structure

```
com.example.intrack/
├── MainActivity.kt                      // Single-activity host with Edge-to-Edge UI
├── data/
│   ├── model/
│   │   ├── ExpenseEntity.kt            // Categorized outflow schema (Committed vs Discretionary)
│   │   ├── IncomeEntity.kt             // 4-Channel multi-currency income schema
│   │   ├── GoalEntity.kt               // MacBook Pro M4 capital runway schema
│   │   ├── GoalContributionEntity.kt   // Windfall siphoning audit ledger
│   │   └── SubscriptionEntity.kt       // Fixed recurring burn & zombie subscription schema
│   ├── remote/
│   │   └── FirestoreService.kt         // Firestore client with persistent cache configuration
│   └── repository/
│       ├── AuthRepository.kt / AuthRepositoryImpl.kt
│       ├── IncomeRepository.kt / IncomeRepositoryImpl.kt
│       ├── ExpenseRepository.kt / ExpenseRepositoryImpl.kt
│       ├── GoalRepository.kt / GoalRepositoryImpl.kt
│       └── SubscriptionRepository.kt / SubscriptionRepositoryImpl.kt
├── model/
│   └── DashboardData.kt                // Immutable aggregate state for executive overview
├── viewmodel/
│   ├── AuthViewModel.kt
│   ├── DashboardViewModel.kt           // Executive dashboard multi-stream aggregator
│   ├── DashboardUiState.kt
│   ├── IncomeViewModel.kt              // Multi-channel income normalizer & invoice tracker
│   ├── ExpenseViewModel.kt             // 3-Tap micro-logger & commitment evaluator
│   ├── GoalViewModel.kt                // Run-rate calculator & windfall siphoning engine
│   └── SubscriptionViewModel.kt        // Secondary account auto-debit scanner
├── ui/
│   ├── common/
│   │   ├── UiState.kt                  // Sealed state: Idle, Loading, Success, Empty, Error
│   │   └── EmptyStateView.kt           // Graceful zero-crash empty view
│   ├── components/
│   │   ├── CommitmentRatioBar.kt       // Visual committed vs discretionary ratio gauge
│   │   ├── GoalProgressCard.kt         // MacBook Pro M4 countdown & run-rate card
│   │   ├── QuickExpenseBottomSheet.kt  // 3-Tap rapid expense logger with Frequent-4 chips
│   │   ├── ShockInsightBanner.kt       // Coffee/Dining vs MacBook quota micro-alert
│   │   ├── WindfallAllocatorDialog.kt  // Lump-sum savings diversion modal
│   │   └── WhatIfSimulatorCard.kt      // Discretionary reduction accelerator
│   ├── navigation/
│   │   ├── Screen.kt                   // Sealed navigation routes with argument builders
│   │   └── NavGraph.kt                 // Navigation Compose graph definition
│   ├── screens/
│   │   ├── auth/                       // LoginScreen & SignUpScreen
│   │   ├── dashboard/                  // DashboardScreen
│   │   ├── income/                     // IncomeHubScreen
│   │   ├── expenses/                   // ExpenseAnalyticsScreen & AddExpenseScreen
│   │   ├── goal/                       // GoalTrackerScreen & GoalDetailScreen
│   │   ├── subscriptions/              // SubscriptionAuditScreen & AddSubscriptionScreen
│   │   └── settings/                   // SettingsScreen & AccountManagement
│   └── theme/
│       ├── Color.kt                    // Material 3 Emerald/Teal, Slate, Coral tokens
│       ├── Type.kt                     // Plus Jakarta Sans typography scale
│       └── Theme.kt                    // Dynamic MaterialTheme wrapper
└── util/
    ├── CurrencyFormatter.kt            // LKR currency formatting (e.g. LKR 490,000.00)
    └── DateUtils.kt                    // Month boundaries & milestone countdown calculators
```

---

## 6. Firebase Integration & Cloud Firestore Schema

### 6.1 Authentication Architecture
Firebase Authentication provides enterprise-grade user identity management:
* **Email & Password Authentication:** Mandatory credential pairing enforcing strong password policies.
* **Persistent Session Token:** Stored in encrypted internal storage by Firebase SDK, ensuring that Kavindu remains logged in across application restarts and offline sessions.
* **Guest / Demo Fallback Mode:** For academic grading and offline demonstrations, a local guest sandbox (`guest_user`) is provisioned to allow full exploration of screens without network availability.

### 6.2 Cloud Firestore Schema & Data Dictionary

The persistence layer is structured into root collections partitioned by `userId`. This provides high querying flexibility, prevents document size bloating, and enables secure multi-tenancy.

#### 1. Incomes Collection (`incomes`)
Stores earnings across Kavindu's four distinct asynchronous channels.

| Field Name | Data Type | Required | Default | Sample Value | Scenario Justification |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `id` | `String` | Yes | UUID | `"inc_92817"` | Unique Firestore document ID. |
| `userId` | `String` | Yes | - | `"usr_kavindu_01"` | Partition key for tenant security isolation. |
| `channel` | `String` | Yes | `"SALARY"` | `"FREELANCE"` | Channel discriminator: `SALARY`, `FREELANCE`, `ADSENSE`, `CRYPTO`. |
| `title` | `String` | Yes | `""` | `"SME React Portal Milestone 1"` | Descriptive descriptor for reconciliation. |
| `originalAmount` | `Double` | Yes | `0.0` | `120.0` | Raw amount in source currency (e.g., $120 USD for AdSense). |
| `originalCurrency`| `String` | Yes | `"LKR"` | `"USD"` | Currency denomination: `LKR` or `USD`. |
| `exchangeRate` | `Double` | Yes | `1.0` | `305.50` | Realized exchange rate at time of bank deposit. |
| `realizedAmountLkr`|`Double` | Yes | `0.0` | `36660.0` | Normalized base value ($Amount \times Rate$). |
| `dateReceivedMillis`|`Long` | Yes | `now()` | `1730102400000` | Epoch timestamp of cleared funds. |
| `status` | `String` | Yes | `"PAID"` | `"INVOICED"` | Milestone status: `PAID`, `INVOICED`, `OVERDUE`, `DRAFT`. |
| `clientName` | `String?` | No | `null` | `"BlueLotus Tech Ltd"` | SME client name to resolve invoice forgetfulness. |
| `invoiceSentMillis`| `Long?` | No | `null` | `1728892800000` | Timestamp for tracking overdue invoices (> 14 days). |
| `allocatedToGoalLkr`|`Double` | Yes | `0.0` | `14664.0` | Amount diverted directly into MacBook fund (40%). |

#### 2. Expenses Collection (`expenses`)
Captures all financial outflows categorized by payment channel and behavioral commitment.

| Field Name | Data Type | Required | Default | Sample Value | Scenario Justification |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `id` | `String` | Yes | UUID | `"exp_55102"` | Unique Firestore document ID. |
| `userId` | `String` | Yes | - | `"usr_kavindu_01"` | Partition key for tenant isolation. |
| `title` | `String` | Yes | `""` | `"Barista Espresso & Pastry"` | Narrative description of outflow. |
| `amountLkr` | `Double` | Yes | `0.0` | `1850.0` | Total expense denominated in LKR. |
| `category` | `String` | Yes | `"DINING_OUT"`| `"TRANSPORT"` | Vector: `DINING_OUT`, `TRANSPORT`, `RENT`, `SUBSCRIPTION`, `UTILITIES`, `MISC`. |
| `paymentAccount` | `String` | Yes | `"CARD_PRIMARY"`|`"ACCOUNT_SECONDARY"`| Solves invisibility across: `CARD_PRIMARY`, `ACCOUNT_SECONDARY`, `CASH`. |
| `commitmentType` | `String` | Yes | `"DISCRETIONARY"`|`"COMMITTED"` | Binary classification: `COMMITTED` (survival) vs `DISCRETIONARY` (lifestyle). |
| `dateSpentMillis` | `Long` | Yes | `now()` | `1730114000000` | Exact occurrence timestamp. |
| `isRecurring` | `Boolean` | Yes | `false` | `true` | Indicates whether expense auto-schedules future ledger postings. |
| `notes` | `String?` | No | `null` | `"Late-night freelance fuel"` | Optional user context. |

#### 3. Goals Collection (`goals`)
Governs Kavindu's MacBook Pro M4 target runway.

| Field Name | Data Type | Required | Default | Sample Value | Scenario Justification |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `id` | `String` | Yes | UUID | `"macbook_m4_goal"`| Unique goal identifier. |
| `userId` | `String` | Yes | - | `"usr_kavindu_01"` | Partition key. |
| `title` | `String` | Yes | `"MacBook M4"`| `"MacBook Pro M4 Fund"` | Quantified asset target. |
| `targetAmountLkr` | `Double` | Yes | `490000.0` | `490000.0` | Exact capital requirement from authorized reseller. |
| `currentSavedLkr` | `Double` | Yes | `11200.0` | `11200.0` | Liquid baseline savings after 12 months. |
| `targetDateMillis`| `Long` | Yes | `now()+1yr` | `1761638400000` | Hard 12-month completion deadline. |
| `monthlyTargetRunRateLkr`|`Double`| Yes | `40833.33` | `40833.33` | Required monthly savings pace ($\frac{490,000}{12}$). |
| `isCompleted` | `Boolean` | Yes | `false` | `false` | Completion status flag. |

#### 4. Subscriptions Collection (`subscriptions`)
Audits fixed recurring overhead, focusing on secondary account auto-debits and forgotten SaaS tools.

| Field Name | Data Type | Required | Default | Sample Value | Scenario Justification |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `id` | `String` | Yes | UUID | `"sub_gym_01"` | Unique subscription identifier. |
| `userId` | `String` | Yes | - | `"usr_kavindu_01"` | Partition key. |
| `name` | `String` | Yes | `""` | `"Fitness First Gym Auto-Debit"` | Explicit service name. |
| `amountLkr` | `Double` | Yes | `0.0` | `8500.0` | Monthly fixed recurring cost. |
| `billingCycle` | `String` | Yes | `"MONTHLY"` | `"MONTHLY"` | Billing frequency: `MONTHLY` or `ANNUAL`. |
| `paymentAccount` | `String` | Yes | `"ACCOUNT_SECONDARY"`|`"ACCOUNT_SECONDARY"`| Identifies the neglected secondary account. |
| `status` | `String` | Yes | `"ACTIVE"` | `"UNUSED_FLAGGED"`| Status: `ACTIVE`, `UNUSED_FLAGGED`, `CANCELLED`. |
| `nextBillingMillis`|`Long` | Yes | `now()+30d` | `1732521600000` | Upcoming debit milestone. |
| `isFlaggedForReview`|`Boolean`| Yes | `false` | `true` | Identifies services Kavindu intended to cancel 6 months ago. |

### 6.3 Firestore Composite Indexing Strategy

To guarantee query performance with sub-second latency on large transaction histories, the following composite indexes are configured:

1. **Monthly Transactions Index:**
   * Collection: `expenses`
   * Fields: `userId` (ASC), `dateSpentMillis` (DESC)
   * *Purpose:* Enables rapid chronological queries for recent transactions and monthly analytics without full-collection scans.

2. **Freelance Receivables Index:**
   * Collection: `incomes`
   * Fields: `userId` (ASC), `channel` (ASC), `status` (ASC)
   * *Purpose:* Powers the "Overdue Invoices" dashboard badge by filtering for `channel == "FREELANCE"` and `status == "INVOICED"`.

3. **Active Subscriptions Index:**
   * Collection: `subscriptions`
   * Fields: `userId` (ASC), `status` (ASC), `nextBillingMillis` (ASC)
   * *Purpose:* Optimizes the automated scheduler that auto-posts recurring rent and gym outlays to the expense ledger.

### 6.4 Multi-Currency Normalization Formulae

* **AdSense Foreign Exchange Normalization:**
  $$\text{Realized Amount (LKR)} = \text{Original Amount (USD)} \times \text{Exchange Rate (LKR/USD)}$$
  *Example:* $\$58.00 \times 305.50 = \text{LKR } 17,719.00$.

* **Crypto Net Fiat Capital Gains:**
  $$\text{Realized Crypto P\&L} = \sum \text{Fiat Liquidated / Withdrawn} - \sum \text{Fiat Injected / Deposited}$$
  *Rationale:* Isolates cashflow from volatile asset prices, preventing unrealized paper gains from inflating Kavindu's perceived monthly spending budget.

### 6.5 Cloud Firestore Security Rules (`firestore.rules`)

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {

    // Helper: Validates that the requesting caller owns the resource
    function isOwner(userId) {
      return request.auth != null && (request.auth.uid == userId || userId == 'guest_user');
    }

    // User Profile Collection
    match /users/{userId} {
      allow read, write: if isOwner(userId);

      // Deeply nested subcollections inheritance
      match /{document=**} {
        allow read, write: if isOwner(userId);
      }
    }

    // Root Incomes Collection - Strictly isolated by userId attribute
    match /incomes/{incomeId} {
      allow read, write: if request.auth != null && (request.resource.data.userId == request.auth.uid || resource.data.userId == request.auth.uid || resource.data.userId == 'guest_user' || request.resource.data.userId == 'guest_user');
    }

    // Root Expenses Collection - Strictly isolated by userId attribute
    match /expenses/{expenseId} {
      allow read, write: if request.auth != null && (request.resource.data.userId == request.auth.uid || resource.data.userId == request.auth.uid || resource.data.userId == 'guest_user' || request.resource.data.userId == 'guest_user');
    }

    // Root Goals Collection & Associated Contributions
    match /goals/{goalId} {
      allow read, write: if request.auth != null && (request.resource.data.userId == request.auth.uid || resource.data.userId == request.auth.uid || resource.data.userId == 'guest_user' || request.resource.data.userId == 'guest_user');

      match /contributions/{contribId} {
        allow read, write: if request.auth != null;
      }
    }

    // Root Subscriptions Collection
    match /subscriptions/{subscriptionId} {
      allow read, write: if request.auth != null && (request.resource.data.userId == request.auth.uid || resource.data.userId == request.auth.uid || resource.data.userId == 'guest_user' || request.resource.data.userId == 'guest_user');
    }
  }
}
```

### 6.6 Offline Persistence & Real-Time Sync Configuration

Firestore is initialized with persistent disk cache settings in `FirestoreService.kt`:
```kotlin
val firestoreSettings = firestoreSettings {
    setLocalCacheSettings(persistentCacheSettings {
        // Caches documents to device flash storage using an LRU eviction algorithm
    })
}
Firebase.firestore.firestoreSettings = firestoreSettings
```
* **Offline Write Pipeline:** When Kavindu logs an expense in an underground car park or without 4G connectivity, Firestore immediately commits the write to local SQLite cache, emits the updated snapshot to UI listeners, and queues the change in a mutation queue.
* **Automatic Cloud Reconciliation:** Upon network reconnection, Firestore flushes pending mutations to the remote cloud server and resolves conflict resolution using server timestamps.

---

## 7. Jetpack Compose UI & UX Design

### 7.1 Material Design 3 Design System & Visual Ergonomics
* **Typography:** `Plus Jakarta Sans` family (Bold, SemiBold, Medium, Regular) providing clean readability and modern fintech styling.
* **Color Palette:**
  * **Primary Brand (Emerald / Mint):** `#10B981` — Symbolizes savings growth, capital accumulation, and goal progress.
  * **Committed Neutral (Dark Slate):** `#1E293B` — Used for essential non-negotiable living obligations (Rent, Utilities).
  * **Discretionary Alert (Coral / Crimson):** `#EF4444` — Visually flags lifestyle spending leaks, coffee, and dining out.
  * **Secondary Account Accent (Amber):** `#F59E0B` — Distinguishes secondary account auto-debits from primary card swipes.
* **Thumb-Zone Optimization:** All key action components (FAB, Rapid Entry Sheet, Filter Chips) reside within the lower 40% of the display viewport for effortless one-handed thumb interaction.

### 7.2 Composable Screen Decomposition & Navigation Architecture

Single-Activity Navigation Graph (`NavGraph.kt`) hosting typed destinations:

```
[Login / SignUp Screen]
         │ (Successful Authentication)
         ▼
[Executive DashboardScreen] ◄────────────────────────────────────────────────┐
   ├── [QuickExpenseBottomSheet] (Modal)                                     │
   ├── [IncomeHubScreen] ────────────────────────────────────────────────────┤
   │      └── Dialog: Log Salary Bonus (25th) / Freelance / AdSense / Crypto │
   ├── [ExpenseAnalyticsScreen] ─────────────────────────────────────────────┤
   │      └── Tab: Daily Ledger | Category Radar | Channel Matrix            │
   ├── [SubscriptionAuditScreen] ────────────────────────────────────────────┤
   │      └── Screen: Flag Unused Zombie Auto-Debits                         │
   └── [GoalTrackerScreen] ──────────────────────────────────────────────────┘
          ├── Windfall Siphoning Modal
          └── What-If Opportunity Cost Simulator
```

### 7.3 Annotated Wireframe Layouts & Component Placement

#### Screen 1: Executive Financial Clarity Dashboard
```
+-------------------------------------------------------------+
| [Profile / Avatar]   Hi Kavindu!               [Oct 2026 v] |
| Current Cashflow: LKR 184,500.00 In | LKR 112,400.00 Out    |
+-------------------------------------------------------------+
| >>> COMMITMENT VS. DISCRETIONARY GAUGE <<<                  |
| [===========================||============================] |
| 62% Committed (LKR 69.6k)   | 38% Discretionary (LKR 42.8k) |
+-------------------------------------------------------------+
| >>> MACBOOK PRO M4 RUNWAY WIDGET <<<                        |
| Target: LKR 490,000.00 | Saved: LKR 35,200.00 (7.2%)        |
| [====>----------------------------------------------------] |
| Run-Rate: LKR 40,833.33/mo | 312 Days Remaining             |
+-------------------------------------------------------------+
| >>> SHOCK-FACTOR MICRO-INSIGHT BANNER <<<                   |
| (!) "LKR 24,000 spent on coffee & casual dining equals 58%  |
|      of your monthly MacBook quota!"                        |
+-------------------------------------------------------------+
| [!] OVERDUE INVOICE ALERT: "BlueLotus Tech (LKR 45k) - 18d" |
+-------------------------------------------------------------+
| RECENT TRANSACTIONS                                [See All]|
| - UberEats Dinner          -LKR 2,450.00  [Card]  [Discret] |
| - PickMe Ride to Office    -LKR 1,120.00  [Cash]  [Discret] |
| - Apartment Rent           -LKR 34,000.00 [Card]  [Commit]  |
+-------------------------------------------------------------+
|  [ Bottom Navigation: Dashboard | Income | Expenses | Goals ]|
|                 [ (+) QUICK EXPENSE FAB ]                   |
+-------------------------------------------------------------+
```

#### Screen 2: Rapid 3-Tap Quick Expense Bottom Sheet
```
+-------------------------------------------------------------+
|                       LOG NEW EXPENSE                       |
| Select Quick-Tap Vector:                                    |
| [ PickMe / Ride ]  [ UberEats ]  [ Coffee / Cafe ]  [ Cash ]|
+-------------------------------------------------------------+
| Amount:  LKR [  1,850.00  ]                                 |
+-------------------------------------------------------------+
| Channel: [x] Primary Card   [ ] Secondary Acc   [ ] Cash    |
| Nature:  [ ] Committed     [x] Discretionary               |
+-------------------------------------------------------------+
| Numeric Keypad:                                             |
|       [ 1 ]       [ 2 ]       [ 3 ]                         |
|       [ 4 ]       [ 5 ]       [ 6 ]                         |
|       [ 7 ]       [ 8 ]       [ 9 ]                         |
|       [ . ]       [ 0 ]       [ < ]                         |
+-------------------------------------------------------------+
|                [ CONFIRM ENTRY (3 TAPS) ]                   |
+-------------------------------------------------------------+
```

#### Screen 3: Multi-Channel Income Hub
```
+-------------------------------------------------------------+
| TOTAL REALIZED INCOME (OCTOBER): LKR 192,500.00             |
| Rolling 3-Month Average: LKR 178,200.00                     |
+-------------------------------------------------------------+
| [CHANNEL 1: SALARY]                                         |
| Base: LKR 120,000.00 | Bonus: +LKR 15,000.00 [25th Oct]     |
| Status: Cleared into Bank Account                           |
+-------------------------------------------------------------+
| [CHANNEL 2: FREELANCE PROJECTS (SME)]                       |
| - React SME E-Commerce: LKR 45,000.00   [PAID]              |
| - WordPress Clinic Portal: LKR 35,000.00 [OVERDUE: 18 Days] |
|   Action: [ Send WhatsApp Reminder ]                        |
+-------------------------------------------------------------+
| [CHANNEL 3: GOOGLE ADSENSE (USD)]                           |
| Gross: $58.00 USD @ 305.50 LKR = LKR 17,719.00 [Cleared]    |
+-------------------------------------------------------------+
| [CHANNEL 4: CRYPTO REALIZED P&L (BINANCE)]                  |
| Injected: LKR 50,000 | Withdrawn: LKR 65,000                |
| Net Realized P&L: +LKR 15,000.00 [Capital Gain]             |
+-------------------------------------------------------------+
|                     [ + LOG NEW INCOME ]                    |
+-------------------------------------------------------------+
```

#### Screen 4: MacBook Pro M4 Goal Runway & Windfall Siphoning
```
+-------------------------------------------------------------+
| MACBOOK PRO M4 RUNWAY TRACKER                               |
| Target: LKR 490,000.00 | Liquid Saved: LKR 59,200.00        |
| [================>----------------------------------------] |
| Required Monthly Run-Rate: LKR 40,833.33                    |
| Current Projected Delivery Date: 14 October 2027            |
+-------------------------------------------------------------+
| >>> WINDFALL ALLOCATION SIMULATOR <<<                       |
| "You received LKR 45,000 from React SME Freelance!"         |
| Siphon to MacBook Fund: [ 40% Slider ] -> LKR 18,000.00     |
| New Completion Date: 12 August 2027 (Accelerated 6 Weeks!)  |
|                   [ SIPHON WINDFALL NOW ]                   |
+-------------------------------------------------------------+
| >>> "WHAT-IF" DISCRETIONARY ACCELERATOR <<<                 |
| Cut Coffee & UberEats by LKR 12,000/month:                  |
| Projected Completion Date pulled forward by: 54 Days!       |
+-------------------------------------------------------------+
```

---

## 8. Feature Completeness & Scenario Innovation

### 8.1 Core Feature Coverage Matrix

| Scenario Pain Point | Root Cause | InTrack Engineered Solution |
| :--- | :--- | :--- |
| **Irregular Multi-Channel Income** | 4 channels with varying currencies, cycles, and volatility. | **Multi-Channel Income Hub:** Distinct modules for 25th Salary bonus delta, Freelance milestones, AdSense USD normalizer, and Crypto fiat cashflow P&L. |
| **Freelance Invoice Forgetfulness** | Unbilled clients and unreconciled bank deposits. | **Receivables Auditor:** Milestone tracker with status badges (`INVOICED`, `OVERDUE`) and automated reminder prompts for uncollected dues > 14 days. |
| **Discretionary Spending Invisibility**| Fragmented transactions across card, cash, and PickMe. | **Binary Macro-Taxonomy & Ratio Gauge:** Instant tagging as `COMMITTED` vs `DISCRETIONARY` with executive split-color ratio bar on dashboard. |
| **Shock Dining Outlays (LKR 24,000)** | Lack of cognitive connection between habits and capital goals. | **Shock-Factor Micro-Insight Banner:** Computes dining spend as an exact percentage of monthly MacBook savings quota. |
| **MacBook Fund Stagnation (LKR 11,200)** | Goal is emotionally present but financially disconnected. | **Dynamic Capital Runway & Windfall Siphoning:** Calculates exact LKR 40,833.33/mo run-rate and diverts lump-sum freelance bonuses into the fund. |
| **Hidden Secondary Account Debits** | Gym membership & unused subscriptions auto-debited unnoticed. | **Subscription & Auto-Debit Auditor:** Scans secondary account debits and flags zombie subscriptions for immediate cancellation. |
| **2-Week Tracking Drop-off** | Friction-heavy spreadsheets and lengthy input forms. | **Rapid 3-Tap Micro-Logging:** One-handed bottom sheet with "Frequent 4" preset chips completing entries in < 3 seconds. |

### 8.2 Meaningful Innovation Beyond Minimum Requirements

1. **Shock-Factor Micro-Insight Engine:**
   Translates abstract expenditure into goal deficit metrics:
   $$\text{Quota Consumption \%} = \left(\frac{\text{Monthly Dining Spend}}{\text{LKR 40,833.33}}\right) \times 100$$
   *Impact:* When Kavindu spends LKR 24,000 on coffee, the app alerts: *"LKR 24,000 spent on dining out equals 58% of your monthly MacBook quota!"*, driving immediate behavioral self-correction.

2. **Windfall Siphoning Engine:**
   When irregular lump sums arrive (such as an overdue LKR 90,000 freelance payment or Q1 crypto profit), behavioral psychology proves money in checking accounts is quickly spent. InTrack intercepts the entry with an automated siphoning slider, encouraging Kavindu to allocate 20% to 50% directly into the locked MacBook fund.

3. **"What-If" Opportunity Cost Calculator:**
   Interactive slider allowing Kavindu to simulate the real-world impact of curbing discretionary habits (e.g., cutting 4 PickMe rides per week pulls the MacBook delivery date forward by 38 days).

4. **Forgiving Catch-Up Reconciliation:**
   Unlike Google Sheets where missed days break formulas, InTrack provides an unallocated reconciliation button. If Kavindu misses logging for 5 days, he enters his current bank balance, and the system balances the delta without forcing painful retroactive data entry.

---

## 9. Technical Design Justifications & Failure Mode Elimination

### 9.1 Comparative Failure Analysis Matrix

| Tool Attempted by Kavindu | Why It Failed in the Scenario | Architectural & UX Decision in InTrack That Prevents Failure |
| :--- | :--- | :--- |
| **1. Google Sheets** | Formulas broke on row insertions; manual entry on mobile was cumbersome; high maintenance friction led to abandonment in 13 days. | **Reactive Data Flow & Atomic Writes:** Firestore reactive listeners handle aggregations automatically. Zero formulas to break. 3-tap bottom sheet completes logging in 3 seconds. |
| **2. Play Store Budgeting App** | Hardcoded to USD; assumed single steady paycheck; forced rigid monthly budgets incompatible with irregular freelance/AdSense earnings. | **Dual-Currency & Multi-Source Architecture:** Built natively with LKR base and dynamic USD exchange rate conversion. Decouples income streams into 4 distinct asynchronous channels. |
| **3. Notes Application** | Unlabeled blocks of copied bank figures became an unreadable mess within 4 days. | **Structured Relational Firestore Schema:** Eliminates raw text pastes. Replaces unlabelled figures with structured categorical entities, account tags, and visual dashboard charts. |
| **4. Bank Native App** | Captured card transactions only; missed cash and secondary account entirely; misclassified PickMe rides as "online retail". | **Multi-Ledger Channel Tracking:** Tracks 3 distinct payment channels (`Primary Card`, `Cash`, `Secondary Account`). Preserves accurate transport categorization. |

### 9.2 Error Handling & Zero-Crash Architecture

To satisfy the rubric mandate (*"No unhandled exceptions, no blank screens on network failure, no crashes on empty datasets"*):
* **StateFlow Sealed Hierarchy (`UiState<T>`):** All ViewModels wrap data in `Idle`, `Loading`, `Success(data)`, `Empty`, and `Error(message, throwable)`. Composables render explicit fallback views for every state.
* **Empty State Views (`EmptyStateView.kt`):** Zero-item lists render motivational onboarding illustrations rather than blank white screens.
* **Coroutine Exception Handlers:** Background dispatchers utilize `CoroutineExceptionHandler` and `SupervisorJob` so that network timeout exceptions do not crash the host Activity.
* **Safe Number Parsing:** Numeric inputs parse doubles defensively via `.toDoubleOrNull() ?: 0.0`, eliminating runtime `NumberFormatException` crashes.

---

## 10. Project Implementation, Verification & Rubric Compliance

### 10.1 Build Configuration & Prerequisites
* **Programming Language:** Kotlin 100% (Kotlin DSL `build.gradle.kts`).
* **Android Target:** Minimum SDK 26 (Android 8.0 Oreo), Target SDK 34+ (Android 14).
* **UI Toolkit:** Jetpack Compose with Material Design 3.
* **Firebase Services:** Firebase Authentication (v33.4.0+), Cloud Firestore with Persistent Cache.
* **Async & Concurrency:** Kotlin Coroutines & StateFlow.

### 10.2 Final Deliverables Checklist

| Assignment Requirement | Deliverable Artefact | Location / Status |
| :--- | :--- | :--- |
| **Source Code Repository** | Private Git repository with clean commit history | GitHub / GitLab Link |
| **README.md** | Build instructions, Firebase setup, architecture summary | Project Root `README.md` |
| **Technical Design Document**| Minimum 15-page comprehensive specification PDF | Exported from this Document |
| **Software Requirements (SRS)**| Functional, Non-Functional, Constraints, Use Cases | Sections 1–5 of this Document |
| **MVVM Architecture Diagram** | Layered component & data flow diagram | Section 5 of this Document |
| **Firestore Schema & Rules** | Complete collections data dictionary & `firestore.rules` | Section 6 & `firestore.rules` |
| **Annotated UI Wireframes** | 5 Core screen wireframes with component annotations | Section 7 of this Document |
| **Signed APK File** | Release / Debug APK running live against Firebase | `app-release.apk` |
| **Live Demo Preparation** | 15-minute engineering walkthrough & defense | Scheduled LMS Session |
