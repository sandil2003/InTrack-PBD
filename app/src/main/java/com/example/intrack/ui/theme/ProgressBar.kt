package com.example.intrack.ui.theme

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Standard capsule progress bar matching Figma hero and list item tracks.
 * Handles fractional bounds safely to prevent layout collapse.
 */
@Composable
fun InTrackProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    height: Dp = 8.dp,
    fillColor: Color = FigmaTerracotta,
    trackColor: Color = FigmaTrackBg,
    shape: Shape = InTrackPillShape,
    animate: Boolean = true
) {
    val clampedProgress = progress.coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = clampedProgress,
        animationSpec = tween(400),
        label = "inTrackProgressBar"
    )
    val currentProgress = if (animate) animatedProgress else clampedProgress

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(shape)
            .background(trackColor)
    ) {
        if (currentProgress > 0f) {
            Box(
                modifier = Modifier
                    .weight(currentProgress.coerceIn(0.001f, 1f))
                    .fillMaxHeight()
                    .background(fillColor)
            )
        }
        if (1f - currentProgress > 0f) {
            Box(
                modifier = Modifier
                    .weight((1f - currentProgress).coerceIn(0.001f, 1f))
                    .fillMaxHeight()
                    .background(trackColor)
            )
        }
    }
}
