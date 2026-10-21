package com.example.intrack.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Standard status pill badge (e.g., "Done 🎉", "360d left", "25% Saved", "Essentials", "Card Primary").
 */
@Composable
fun InTrackStatusBadge(
    text: String,
    modifier: Modifier = Modifier,
    containerColor: Color = FigmaSoftPeach,
    contentColor: Color = FigmaTerracotta,
    fontSize: TextUnit = 11.sp,
    fontWeight: FontWeight = FontWeight.SemiBold,
    shape: Shape = InTrackPillShape
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(containerColor)
            .padding(horizontal = 9.dp, vertical = 3.5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = fontSize,
                fontWeight = fontWeight
            ),
            color = contentColor,
            maxLines = 1,
            softWrap = false
        )
    }
}
