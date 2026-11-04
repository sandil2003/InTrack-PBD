package com.example.intrack.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// ==============================================================================
// Unified InTrack Design Tokens - Exact 4 Hex Codes Fintech Visual Hierarchy
// ==============================================================================

// Adaptive Material 3 Semantic Accessors
val InTrackCanvasBg: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.background
val InTrackCardBg: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.surface
val InTrackDarkText: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onSurface
val InTrackMutedText: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onSurfaceVariant
val InTrackBorder: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.outlineVariant
val InTrackDivider: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.outlineVariant

// Background & Canvas Surfaces
val FigmaBg: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.background
val FigmaCanvasBg: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.background
val FigmaCardBg: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.surface
val FigmaIconCircleBg: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.surfaceVariant

// Typography & Content Colors
val FigmaDarkText: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onSurface
val FigmaMutedSlate: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onSurfaceVariant
val FigmaMutedBrown: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onSurfaceVariant
val FigmaNoteBrown: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onSurfaceVariant

// Brand & Accent Colors - Exact Mockup Mapping
// Hero Cards & Progress Fills -> #FFD13E Warm Sun Yellow
val FigmaTerracotta: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.primaryContainer
val FigmaTerracottaOrange: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.primaryContainer
val FigmaPeachBar: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.primaryContainer

// Primary CTA Buttons & FABs -> #121660 Deep Midnight Navy
val FigmaLightOrange: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.primary
val FigmaButtonOrange: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.primary

// Active Filter Pills & Badges -> #FFD13E Warm Sun Yellow with #121660 Deep Navy Text
val FigmaSoftPeach: Color @Composable @ReadOnlyComposable get() = Color(0xFFFFF8D6)
val FigmaSoftPillActive: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.primaryContainer
val FigmaActiveText: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onPrimaryContainer

// Borders, Dividers & Component Backdrops
val FigmaBorder: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.outlineVariant
val FigmaBorderActive: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.primary
val FigmaDivider: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.outlineVariant
val FigmaTrackBg: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.surfaceVariant
val FigmaSwitchBg: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.surfaceVariant
val FigmaLifestyleBar = Color(0xFF94A3B8)

// Standard Application Corner Shapes
val InTrackCardShape = RoundedCornerShape(24.dp)
val InTrackMediumCardShape = RoundedCornerShape(20.dp)
val InTrackSmallCardShape = RoundedCornerShape(16.dp)
val InTrackPillShape = RoundedCornerShape(999.dp)
