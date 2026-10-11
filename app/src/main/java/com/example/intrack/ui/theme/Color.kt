package com.example.intrack.ui.theme

import androidx.compose.ui.graphics.Color

// Primary Production Brand Colors - Unified Sunset Tangerine Theme
val BrandTangerine = Color(0xFFFF6422) // Electric Sunset Tangerine
val BrandTangerineDark = Color(0xFFFF7A3D) // Luminous Sunset Orange for dark mode
val BrandTangerineContainer = Color(0xFFFFEDD5) // Warm peach-tint container (Orange 100)
val BrandTangerineOnContainer = Color(0xFF9A3412) // Rich burnt rust (Orange 900)

// Neutral Surfaces (Light) - Warm Luxe Neo-Bank Canvas
val AppBackground = Color(0xFFFBF9F6) // Warm, luminous ivory/cream canvas
val AppSurface = Color(0xFFFFFFFF) // Pure white elevated cards
val AppSurfaceVariant = Color(0xFFF3ECE4) // Warm soft structure
val AppBorderSubtle = Color(0xFFE5DDD3) // Defined warm borders

// Neutral Surfaces (Dark) - Deep Obsidian Slate
val DarkBackground = Color(0xFF0C0F14) // Deep obsidian slate
val DarkSurface = Color(0xFF161A22) // Sleek elevated surface
val DarkSurfaceVariant = Color(0xFF242A36) // Distinct dark structure
val DarkBorderSubtle = Color(0xFF2D3544)

// Harmonious Theme Shades (Monochromatic Steps of the Single Theme)
val TangerineShade1 = BrandTangerine          // Primary
val TangerineShade2 = Color(0xFFEA580C)       // Sunset Terracotta
val TangerineShade3 = Color(0xFFFB923C)       // Sunset Amber
val TangerineShade4 = Color(0xFFFDBA74)       // Sunset Peach

// Status & Metric Indicators - Unified with Theme
val IncomePositive = BrandTangerine
val ExpenseNegative = Color(0xFF64748B) // Slate 500

// Essentials vs Lifestyle - Harmonious with Theme (Tangerine & Slate)
val EssentialsColor = BrandTangerine
val LifestyleColor = Color(0xFF64748B) // Slate 500

// Channel Accents - Unified Sunset Family
val SalaryBlue = BrandTangerine
val FreelancePurple = TangerineShade2
val AdSenseGreen = TangerineShade3
val CryptoOrange = TangerineShade4

// Compatibility aliases
val PrimaryGreen = BrandTangerine
val PrimaryContainerGreen = BrandTangerineContainer
val SecondaryGreen = Color(0xFF334155)
val GainGreen = IncomePositive
val LossRed = ExpenseNegative
