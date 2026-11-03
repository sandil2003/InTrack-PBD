package com.example.intrack.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// ==============================================================================
// Unified InTrack Figma Design Tokens (matching Expenses & Goals specs)
// ==============================================================================

// Background & Canvas Surfaces
val FigmaBg = Color(0xFFFFFFFF)
val FigmaCanvasBg = FigmaBg
val FigmaCardBg = Color(0xFFFFFFFF)
val FigmaIconCircleBg = Color(0xFFEFECE5)

// Typography & Content Colors
val FigmaDarkText = Color(0xFF1B1C1A)
val FigmaMutedSlate = Color(0xFF585F6B)
val FigmaMutedBrown = Color(0xFF6B5E57)
val FigmaNoteBrown = Color(0xFF5B413B)

// Brand & Accent Colors - Unified Sunset Tangerine Theme
val FigmaTerracotta = BrandTangerine
val FigmaTerracottaOrange = Color(0xFFFF5C38)
val FigmaLightOrange = Color(0xFFFF5C38) // Light orange used in the Expenses edit transaction button (FigmaPrimary)
val FigmaButtonOrange = FigmaLightOrange
val FigmaSoftPeach = BrandTangerineContainer
val FigmaPeachBar = Color(0xFFFDBA74)
val FigmaSoftPillActive = BrandTangerineContainer
val FigmaActiveText = BrandTangerineOnContainer

// Borders, Dividers & Component Backdrops
val FigmaBorder = Color(0xFFEDE8E3)
val FigmaBorderActive = Color(0x80E3BEB6)
val FigmaDivider = Color(0xFFF2ECE6)
val FigmaTrackBg = Color(0xFFF2ECE6)
val FigmaSwitchBg = Color(0xFFEAE8E4)
val FigmaLifestyleBar = Color(0xFFC0C7D5)

// Standard Application Corner Shapes
val InTrackCardShape = RoundedCornerShape(24.dp)
val InTrackMediumCardShape = RoundedCornerShape(20.dp)
val InTrackSmallCardShape = RoundedCornerShape(16.dp)
val InTrackPillShape = RoundedCornerShape(999.dp)
