package com.example.intrack.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = BrandTangerine,
    onPrimary = Color.White,
    primaryContainer = BrandTangerineContainer,
    onPrimaryContainer = BrandTangerineOnContainer,
    secondary = Color(0xFF334155),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF1ECE4),
    onSecondaryContainer = Color(0xFF1E293B),
    background = AppBackground,
    onBackground = Color(0xFF0F172A),
    surface = AppSurface,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = AppSurfaceVariant,
    onSurfaceVariant = Color(0xFF475569), // Slate 600: strong readable text
    outline = Color(0xFFCBD5E1), // Slate 400: clear visible borders
    outlineVariant = Color(0xFFE5DDD3), // Warm distinct dividers
    error = ExpenseNegative,
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF991B1B)
)

private val DarkColorScheme = darkColorScheme(
    primary = BrandTangerineDark,
    onPrimary = Color(0xFF431407),
    primaryContainer = Color(0xFF7C2D12),
    onPrimaryContainer = Color(0xFFFFEDD5),
    secondary = Color(0xFF94A3B8),
    onSecondary = Color(0xFF0F172A),
    secondaryContainer = Color(0xFF242A36),
    onSecondaryContainer = Color(0xFFF8FAFC),
    background = DarkBackground,
    onBackground = Color(0xFFF8FAFC),
    surface = DarkSurface,
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF475569),
    outlineVariant = DarkBorderSubtle,
    error = ExpenseNegative,
    errorContainer = Color(0xFF450A0A),
    onErrorContainer = Color(0xFFFCA5A5)
)

enum class AppThemeMode(val title: String) {
    SYSTEM("Mobile"),
    LIGHT("Light"),
    DARK("Dark")
}

@Composable
fun InTrackTheme(
    darkTheme: Boolean = false,
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
