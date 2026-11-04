package com.example.intrack.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ==============================================================================
// 1. Primary Production Brand Colors - Exact 4 Fintech Palette Hex Codes
// ==============================================================================
val BrandMidnightNavy = Color(0xFF121660) // Deep Midnight Navy (#121660) - Primary CTA Buttons, Top Bars, FABs & Titles
val BrandWarmYellow = Color(0xFFFFD13E)   // Warm Sun Yellow (#FFD13E) - Main Hero Cards, Active Pills & Highlights
val BrandEmeraldGreen = Color(0xFF3CAE8C) // Emerald Teal Green (#3CAE8C) - Feature Cards, Income Streams & Gains
val BrandAzureBlue = Color(0xFF0171DF)    // Azure Blue (#0171DF) - Secondary Feature Cards & Channel Accents

// Compatibility Brand Aliases
val BrandPitchBlack = BrandMidnightNavy
val BrandRoyalBlue = BrandAzureBlue
val BrandElectricLime = BrandWarmYellow
val BrandLavenderContainer = Color(0xFFE8EEFC) // Soft Azure/Navy Tint Container
val BrandSoftSlate = Color(0xFFECEFF8) // Soft Slate Gray Container

val BrandSlatePrimary = BrandMidnightNavy
val BrandSlatePrimaryDark = Color(0xFF81A1FF)
val BrandGold = BrandWarmYellow
val BrandGoldDark = Color(0xFFFFDC73)
val BrandGoldContainer = BrandWarmYellow
val BrandGoldOnContainer = BrandMidnightNavy

val BrandTangerine = BrandWarmYellow
val BrandTangerineDark = BrandGoldDark
val BrandTangerineContainer = BrandGoldContainer
val BrandTangerineOnContainer = BrandMidnightNavy
val BrandSunsetCoral = BrandMidnightNavy

// ==============================================================================
// 2. Neutral Surfaces (Light) - Off-White Soft Canvas & Crisp White Cards
// ==============================================================================
val AppBackground = Color(0xFFF7F9FC) // Off-White Soft Canvas
val AppSurface = Color(0xFFFFFFFF) // Crisp Pure White Elevated Cards
val AppSurfaceVariant = Color(0xFFECEFF8) // Soft Pearl/Navy Structure Container
val AppBorderSubtle = Color(0xFFE2E8F0) // Defined Pearl Slate Borders

// Neutral Surfaces (Dark) - Deep Obsidian Navy
val DarkBackground = Color(0xFF070B28) // Deep Obsidian Navy Canvas
val DarkSurface = Color(0xFF101642) // Sleek Elevated Dark Surface
val DarkSurfaceVariant = Color(0xFF1B225A) // Distinct Dark Container
val DarkBorderSubtle = Color(0xFF283278) // Dark Subtle Border

// Harmonious Monochromatic Steps
val NavyShade1 = BrandMidnightNavy
val NavyShade2 = Color(0xFF1B2082)
val NavyShade3 = Color(0xFF2B32B3)
val NavyShade4 = Color(0xFF424BD9)

val CobaltShade1 = NavyShade1
val CobaltShade2 = NavyShade2
val CobaltShade3 = NavyShade3
val CobaltShade4 = NavyShade4

val GoldShade1 = BrandWarmYellow
val GoldShade2 = Color(0xFFE5B82A)
val GoldShade3 = Color(0xFFFFDC73)
val GoldShade4 = Color(0xFFFFEAA8)

val TangerineShade1 = GoldShade1
val TangerineShade2 = GoldShade2
val TangerineShade3 = GoldShade3
val TangerineShade4 = GoldShade4

// ==============================================================================
// 3. Financial Semantic Colors (Fintech Hierarchy Alignment)
// ==============================================================================
val IncomePositive = BrandEmeraldGreen      // Emerald Teal Green (#3CAE8C) for Income / Cash-In
val IncomePositiveContainer = Color(0xFFE5F6F1)

val ExpenseNegative = Color(0xFFE11D48)     // Coral Crimson for Expense / Cash-Out
val ExpenseNegativeContainer = Color(0xFFFFE4E6)

val SubscriptionIndigo = BrandAzureBlue     // Azure Blue (#0171DF) for Subscriptions
val SubscriptionIndigoContainer = Color(0xFFE5F1FD)

val GoalSunshine = BrandWarmYellow          // Warm Sun Yellow (#FFD13E) for Goals
val GoalSunshineContainer = Color(0xFFFFF8D6)

// Category & Channel Accents
val EssentialsColor = BrandMidnightNavy
val LifestyleColor = Color(0xFF64748B) // Slate 500

val SalaryBlue = IncomePositive
val FreelancePurple = BrandAzureBlue
val AdSenseGreen = BrandEmeraldGreen
val CryptoOrange = BrandWarmYellow

// Compatibility Aliases
val PrimaryGreen = BrandMidnightNavy
val PrimaryContainerGreen = IncomePositiveContainer
val SecondaryGreen = Color(0xFF475569)
val GainGreen = IncomePositive
val LossRed = ExpenseNegative

// ==============================================================================
// 4. Custom Financial Extension for Material 3 ColorScheme
// ==============================================================================
data class FinancialColorScheme(
    val income: Color,
    val incomeContainer: Color,
    val expense: Color,
    val expenseContainer: Color,
    val subscription: Color,
    val subscriptionContainer: Color,
    val goal: Color,
    val goalContainer: Color
)

val LocalFinancialColors = staticCompositionLocalOf {
    FinancialColorScheme(
        income = IncomePositive,
        incomeContainer = IncomePositiveContainer,
        expense = ExpenseNegative,
        expenseContainer = ExpenseNegativeContainer,
        subscription = SubscriptionIndigo,
        subscriptionContainer = SubscriptionIndigoContainer,
        goal = GoalSunshine,
        goalContainer = GoalSunshineContainer
    )
}

val ColorScheme.financial: FinancialColorScheme
    @Composable
    @ReadOnlyComposable
    get() = LocalFinancialColors.current
