package com.example.intrack.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// ==============================================================================
// Unified InTrack Figma Design Tokens (matching Expenses & Goals specs)
// ==============================================================================

// Adaptive Material 3 Semantic Accessors (Dark & Light Mode aware)
val InTrackCanvasBg: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.background
val InTrackCardBg: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.surface
val InTrackDarkText: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onSurface
val InTrackMutedText: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onSurfaceVariant
val InTrackBorder: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.outlineVariant
val InTrackDivider: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.outlineVariant

// Background & Canvas Surfaces (Adaptive for Light & Dark Mode)
val FigmaBg: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.background
val FigmaCanvasBg: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.background
val FigmaCardBg: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.surface
val FigmaIconCircleBg: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.surfaceContainerHighest

// Typography & Content Colors
val FigmaDarkText: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onSurface
val FigmaMutedSlate: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onSurfaceVariant
val FigmaMutedBrown: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onSurfaceVariant
val FigmaNoteBrown: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onSurfaceVariant

// Brand & Accent Colors - Unified Sunset Tangerine Theme
val FigmaTerracotta: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.primary
val FigmaTerracottaOrange: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.primary
val FigmaLightOrange: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.primary
val FigmaButtonOrange: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.primary
val FigmaSoftPeach: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.primaryContainer
val FigmaPeachBar = Color(0xFFFDBA74)
val FigmaSoftPillActive: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.primaryContainer
val FigmaActiveText: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onPrimaryContainer

// Borders, Dividers & Component Backdrops
val FigmaBorder: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.outlineVariant
val FigmaBorderActive: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.primary
val FigmaDivider: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.outlineVariant
val FigmaTrackBg: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.surfaceContainerHighest
val FigmaSwitchBg: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.surfaceContainerHighest
val FigmaLifestyleBar = Color(0xFFC0C7D5)

// Standard Application Corner Shapes
val InTrackCardShape = RoundedCornerShape(24.dp)
val InTrackMediumCardShape = RoundedCornerShape(20.dp)
val InTrackSmallCardShape = RoundedCornerShape(16.dp)
val InTrackPillShape = RoundedCornerShape(999.dp)
