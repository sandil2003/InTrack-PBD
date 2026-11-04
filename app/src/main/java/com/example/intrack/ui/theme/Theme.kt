package com.example.intrack.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = BrandMidnightNavy,
    onPrimary = Color.White,
    primaryContainer = BrandWarmYellow,
    onPrimaryContainer = BrandMidnightNavy,
    secondary = BrandAzureBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE5F1FD),
    onSecondaryContainer = BrandMidnightNavy,
    tertiary = BrandEmeraldGreen,
    onTertiary = Color.White,
    background = AppBackground,
    onBackground = BrandMidnightNavy,
    surface = AppSurface,
    onSurface = BrandMidnightNavy,
    surfaceVariant = AppSurfaceVariant,
    onSurfaceVariant = Color(0xFF53607E),
    outline = AppBorderSubtle,
    outlineVariant = Color(0xFFE2E8F0),
    error = ExpenseNegative,
    errorContainer = ExpenseNegativeContainer,
    onErrorContainer = Color(0xFF991B1B)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF81A1FF),
    onPrimary = Color(0xFF070B28),
    primaryContainer = Color(0xFF1B225A),
    onPrimaryContainer = Color(0xFFEBF3FE),
    secondary = BrandWarmYellow,
    onSecondary = Color(0xFF070B28),
    secondaryContainer = Color(0xFF383000),
    onSecondaryContainer = BrandWarmYellow,
    tertiary = BrandEmeraldGreen,
    onTertiary = Color(0xFF070B28),
    background = DarkBackground,
    onBackground = Color(0xFFF8FAFC),
    surface = DarkSurface,
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = DarkBorderSubtle,
    outlineVariant = Color(0xFF283278),
    error = ExpenseNegative,
    errorContainer = Color(0xFF450A0A),
    onErrorContainer = Color(0xFFFCA5A5)
)

private val LightFinancialColors = FinancialColorScheme(
    income = IncomePositive,
    incomeContainer = IncomePositiveContainer,
    expense = ExpenseNegative,
    expenseContainer = ExpenseNegativeContainer,
    subscription = SubscriptionIndigo,
    subscriptionContainer = SubscriptionIndigoContainer,
    goal = GoalSunshine,
    goalContainer = GoalSunshineContainer
)

private val DarkFinancialColors = FinancialColorScheme(
    income = Color(0xFF34D399),
    incomeContainer = Color(0xFF064E3B),
    expense = Color(0xFFFB7185),
    expenseContainer = Color(0xFF4C0519),
    subscription = Color(0xFF3B92FF),
    subscriptionContainer = Color(0xFF122C5C),
    goal = BrandWarmYellow,
    goalContainer = Color(0xFF383000)
)

enum class AppThemeMode(val title: String) {
    SYSTEM("Mobile"),
    LIGHT("Light"),
    DARK("Dark")
}

@Composable
fun InTrackTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val financialColors = if (darkTheme) DarkFinancialColors else LightFinancialColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context as? Activity
            if (activity != null) {
                val window = activity.window
                window.statusBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(
        LocalFinancialColors provides financialColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
