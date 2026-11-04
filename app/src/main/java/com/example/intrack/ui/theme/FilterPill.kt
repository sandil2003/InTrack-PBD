package com.example.intrack.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Filter Pill matching the Expenses & Financial Goals Figma specifications.
 * Features fluid animated state transitions, rounded pill geometry, and adaptive theme typography.
 */
@Composable
fun InTrackFilterPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = InTrackPillShape,
    selectedContainerColor: Color = Color(0xFFFFD13E),
    unselectedContainerColor: Color = Color(0xFFECEFF8),
    selectedContentColor: Color = Color(0xFF121660),
    unselectedContentColor: Color = Color(0xFF121660),
    selectedBorderColor: Color = Color.Transparent,
    unselectedBorderColor: Color = Color.Transparent
) {
    val pillBg by animateColorAsState(
        targetValue = if (isSelected) selectedContainerColor else unselectedContainerColor,
        label = "filterPillBg"
    )
    val pillTextColor by animateColorAsState(
        targetValue = if (isSelected) selectedContentColor else unselectedContentColor,
        label = "filterPillText"
    )
    val pillBorderColor = if (isSelected) selectedBorderColor else unselectedBorderColor

    Box(
        modifier = modifier
            .clip(shape)
            .background(pillBg)
            .border(1.dp, pillBorderColor, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            ),
            color = pillTextColor,
            maxLines = 1,
            softWrap = false
        )
    }
}

/**
 * Backward-compatible alias for existing callers.
 */
@Composable
fun FigmaFilterPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    InTrackFilterPill(
        label = label,
        isSelected = isSelected,
        onClick = onClick,
        modifier = modifier
    )
}
