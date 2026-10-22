package com.example.intrack.ui.theme

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Standard primary CTA button matching the Expenses Edit Transaction button style:
 * - Light Orange container (FigmaLightOrange / #FF5C38)
 * - White content color
 * - 52dp standard height with rounded pill geometry (28dp)
 */
@Composable
fun InTrackPrimaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(28.dp),
    height: Dp = 52.dp,
    containerColor: Color = FigmaLightOrange,
    contentColor: Color = Color.White,
    elevation: ButtonElevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp),
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.5f),
            disabledContentColor = contentColor.copy(alpha = 0.7f)
        ),
        elevation = elevation,
        modifier = modifier
            .fillMaxWidth()
            .height(height),
        content = content
    )
}
