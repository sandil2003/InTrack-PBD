package com.example.intrack.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Standard Floating Action Button (+) matching the Expenses screen style:
 * - Circular geometry (CircleShape)
 * - Light Orange background (FigmaLightOrange / #FF5C38 matching Expenses edit button)
 * - White rounded plus icon (Icons.Rounded.Add)
 * - Standard positioning padding (16dp bottom, 4dp end)
 */
@Composable
fun InTrackAddFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String = "Add",
    containerColor: Color = FigmaLightOrange,
    contentColor: Color = Color.White,
    shape: Shape = CircleShape,
    iconSize: Dp = 24.dp,
    elevation: Dp = 3.dp
) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = containerColor,
        contentColor = contentColor,
        shape = shape,
        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = elevation),
        modifier = modifier.padding(bottom = 16.dp, end = 4.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.Add,
            contentDescription = contentDescription,
            modifier = Modifier.size(iconSize)
        )
    }
}

/**
 * Inline circular (+) icon button matching the Expenses page circular action style:
 * - 36dp circular clickable container
 * - Soft peach or customizable background
 * - Light orange rounded plus icon
 */
@Composable
fun InTrackAddIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
    iconSize: Dp = 18.dp,
    containerColor: Color = FigmaSoftPeach,
    iconTint: Color = FigmaLightOrange,
    contentDescription: String = "Add"
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(containerColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.Add,
            contentDescription = contentDescription,
            tint = iconTint,
            modifier = Modifier.size(iconSize)
        )
    }
}
