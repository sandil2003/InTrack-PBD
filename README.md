# InTrack — Personal Finance Management System for Android

> **Module:** SE3092 — Platform Based Development | Assignment 01  
> **Prepared For:** BSc (Hons) in Computer Science  
> **Student Scenario:** Kavindu Silva — The Ambitious Earner Without a System  
> **Technology Stack:** 100% Kotlin · Jetpack Compose (Material 3) · MVVM · Coroutines & StateFlow · Firebase Authentication & Cloud Firestore (Offline Persistent Cache)  
> **Platform Requirements:** Minimum SDK 26 (Android 8.0 Oreo), Target SDK 34+ (Android 14)

---

## 1. Project Overview & Scenario Translation

**InTrack** is an offline-first Android personal finance application engineered specifically to resolve the structural financial ambiguity of users with irregular, multi-channel income streams and impulsive discretionary spending patterns, as personified by **Kavindu Silva**.

### The Core Problem (Kavindu Silva)
* **Asynchronous Multi-Source Income:** Base salary with variable bonus (LKR 120,000–140,000 arriving on the 25th), irregular SME freelance projects (LKR 20,000–90,000), USD Google AdSense payouts ($LKR 3,500–19,000), and volatile Binance crypto trading (USDT/ETH).
* **Expense Invisibility:** Fragmentation across primary card, secondary bank account (with forgotten gym auto-debits and subscriptions), PickMe rides, UberEats, and unmonitored cash withdrawals.
* **MacBook Pro M4 Stagnation:** An urgent professional goal (LKR 490,000 capital requirement within 12 months) that remains emotionally present but financially stalled at LKR 11,200 after 12 months of earning over LKR 1.6M.
* **Tracking Drop-off:** Past failure across 4 tools (Google Sheets, Play Store app, Notes app, and bank app) due to high entry friction, rigid single-paycheck paradigms, and broken manual formulas.

### The InTrack Solution
1. **Multi-Channel Income Hub:** Distinct architectural tracking for salary bonus delta on the 25th, freelance milestones with overdue reminders, AdSense USD auto-normalization, and crypto fiat cashflow P&L.
2. **Rapid 3-Tap Micro-Logging:** An ergonomic bottom-sheet interface completing expense entries in under 3 seconds using "Frequent 4" preset chips (`PickMe`, `UberEats`, `Coffee / Cafe`, `Cash`).
3. **Binary Macro-Taxonomic Engine:** Classifies all spending into `COMMITTED` (non-negotiable survival) vs. `DISCRETIONARY` (lifestyle impulses), visualized via an executive split-color ratio gauge.
4. **Dynamic Goal Runway & Windfall Siphoning:** Calculates the exact required monthly savings run-rate (LKR 40,833.33/mo) and intercepts lump-sum freelance earnings with a siphoning slider (20%–50%) to lock funds into the MacBook fund.
5. **Shock-Factor Micro-Insights:** Dynamically correlates lifestyle spending leaks directly with goal deficits (e.g., *"LKR 24,000 spent on coffee & takeout equals 58% of your monthly MacBook quota!"*).
6. **Subscription & Secondary Account Auditor:** Proactively scans auto-debits from secondary accounts and flags unused/zombie subscriptions with cancel-intent triggers.

---

## 2. Architecture & Design Principles

InTrack strictly follows **Clean Architecture** and **Model-View-ViewModel (MVVM)** with **Unidirectional Data Flow (UDF)**:

```
+=============================================================================+
|                          PRESENTATION LAYER (UI)                            |
|  - Jetpack Compose Screens (DashboardScreen, IncomeHubScreen, etc.)        |
|  - Material Design 3 Theme & Plus Jakarta Sans Typography                   |
|  - Navigation Compose Single-Activity Architecture (NavGraph.kt)           |
+=============================================================================+
                                      │  ▲
                         User Events  │  │  StateFlow<UiState<T>>
                                      ▼  │
+=============================================================================+
|                             VIEWMODEL LAYER                                 |
|  - DashboardViewModel, IncomeViewModel, ExpenseViewModel, GoalViewModel    |
|  - Structured Coroutines Scope (viewModelScope on Dispatchers.IO)           |
|  - Reactive Multi-Stream Aggregation via Kotlin combine()                   |
+=============================================================================+
                                      │  ▲
                        Data Requests │  │  Kotlin Flow<List<Entity>>
                                      ▼  │
+=============================================================================+
|                            REPOSITORY LAYER                                 |
|  - Repository Interfaces (AuthRepository, IncomeRepository, etc.)           |
|  - Domain Business Rules, Dual-Currency Normalization, Milestone Auditing   |
+=============================================================================+
                                      │  ▲
                        CRUD Operations│ │  Snapshot Listeners
                                      ▼  │
+=============================================================================+
|                       DATA PERSISTENCE LAYER                                |
|  - Firebase Authentication (User Identity & Session Management)             |
|  - Cloud Firestore Remote Collections (/users, /incomes, /expenses, etc.)  |
|  - Cloud Firestore Persistent Disk Cache (Offline LRU Storage Engine)       |
+=============================================================================+
```

### Key Architectural Safeguards
* **Zero Business Logic in Composables:** Composable functions only render state and forward user events to ViewModels.
* **Zero Database Calls in UI:** Data access is strictly isolated behind Repository abstractions.
* **Zero-Crash Resilience:** All ViewModels expose state wrapped in a sealed `UiState<T>` hierarchy (`Idle`, `Loading`, `Success`, `Empty`, `Error`). Empty lists render informative fallback states rather than blank screens or null-pointer crashes.
* **100% Offline Persistence:** Firestore persistent cache allows reading, creating, and modifying data without internet connectivity, queuing mutations for automatic cloud synchronization.

---

## 3. Package & Directory Structure

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

## 4. Firebase Configuration & Security Rules

### Firebase Services Used
* **Firebase Authentication:** User signup and signin via email/password.
* **Cloud Firestore:** Document-based cloud database configured with persistent offline cache.

### Setting Up Firebase
1. Create a project on the [Firebase Console](https://console.firebase.google.com/).
2. Add an Android app with package name `com.example.intrack`.
3. Download `google-services.json` and place it in the `app/` directory.
4. Enable **Email/Password** provider under **Authentication -> Sign-in method**.
5. Create a Cloud Firestore database in production mode.
6. Deploy the included security rules:
   ```bash
   firebase deploy --only firestore:rules
   ```

### Firestore Security Rules Summary (`firestore.rules`)
* Strictly enforces user isolation: users can only read and write documents where `userId == request.auth.uid`.
* Includes guest sandbox authorization (`guest_user`) for demo evaluation and offline marker validation.
* Protects collections: `/users`, `/incomes`, `/expenses`, `/goals`, and `/subscriptions`.

---

## 5. Build & Execution Instructions

### Prerequisites
* **Android Studio:** Hedgehog (2023.1.1) or Ladybug (2024.2.1+)
* **JDK:** Version 17 (recommended: OpenJDK 17)
* **Android SDK:** Platforms API 26 through API 34 installed

### Building the Project
Clone the repository and open in Android Studio:
```bash
git clone <repository-url>
cd InTrack
```

To build a Debug APK via Gradle:
```bash
./gradlew assembleDebug
```
The resulting APK will be located at:
`app/build/outputs/apk/debug/app-debug.apk`

To build a Release APK:
```bash
./gradlew assembleRelease
```

### Running on Physical Device or Emulator
1. Enable **Developer Options** and **USB Debugging** on your Android 8.0+ device (or launch an AVD running API 26+).
2. Select the `app` run configuration in Android Studio.
3. Click **Run** (`Shift + F10`).
