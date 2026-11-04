package com.example.intrack.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Standard 2-segment sliding pill switch with fluid spring animation
 * sliding the white indicator pill physically from left-to-right and right-to-left.
 */
@Composable
fun InTrackSegmentedSwitch(
    leftLabel: String,
    rightLabel: String,
    isLeftSelected: Boolean,
    onLeftSelect: () -> Unit,
    onRightSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = LocalHapticFeedback.current

    val alignmentBias by animateFloatAsState(
        targetValue = if (isLeftSelected) -1f else 1f,
        animationSpec = spring(
            stiffness = Spring.StiffnessMediumLow,
            dampingRatio = Spring.DampingRatioNoBouncy
        ),
        label = "switchPillAlignment"
    )

    val leftTextColor by animateColorAsState(
        targetValue = if (isLeftSelected) Color(0xFF121660) else Color(0xFF121660).copy(alpha = 0.65f),
        label = "switchLeftText"
    )
    val rightTextColor by animateColorAsState(
        targetValue = if (!isLeftSelected) Color(0xFF121660) else Color(0xFF121660).copy(alpha = 0.65f),
        label = "switchRightText"
    )

    // Darker Slate background without a border
    val trackBg = Color(0xFFE2E6F2)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(CircleShape)
            .background(trackBg)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        // Sliding Pure White Indicator Pill
        Box(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .fillMaxHeight()
                .align(BiasAlignment(alignmentBias, 0f))
                .clip(CircleShape)
                .background(Color.White)
        )

        // Labels Row over the sliding pill
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Tab ("Daily Transactions")
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .clickable {
                        if (!isLeftSelected) {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            onLeftSelect()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = leftLabel,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = 13.5.sp,
                        fontWeight = if (isLeftSelected) FontWeight.Bold else FontWeight.SemiBold
                    ),
                    color = leftTextColor,
                    maxLines = 1
                )
            }

            // Right Tab ("Subscriptions")
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .clickable {
                        if (isLeftSelected) {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            onRightSelect()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = rightLabel,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = 13.5.sp,
                        fontWeight = if (!isLeftSelected) FontWeight.Bold else FontWeight.SemiBold
                    ),
                    color = rightTextColor,
                    maxLines = 1
                )
            }
        }
    }
}
