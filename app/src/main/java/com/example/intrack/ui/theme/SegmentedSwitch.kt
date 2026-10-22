package com.example.intrack.ui.theme

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Standard 2-segment sliding pill switch matching Figma node 2006:55 with tactile haptic response.
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

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(9999.dp))
            .background(FigmaSwitchBg)
            .border(1.dp, FigmaBorder, RoundedCornerShape(9999.dp))
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Tab
            val leftBgColor by animateColorAsState(
                targetValue = if (isLeftSelected) Color.White else Color.Transparent,
                animationSpec = tween(200),
                label = "switchLeftBg"
            )
            val leftTextColor by animateColorAsState(
                targetValue = if (isLeftSelected) FigmaTerracotta else FigmaMutedSlate,
                animationSpec = tween(200),
                label = "switchLeftText"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(9999.dp))
                    .background(leftBgColor)
                    .then(
                        if (isLeftSelected) Modifier.border(1.dp, FigmaBorderActive, RoundedCornerShape(9999.dp))
                        else Modifier
                    )
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

            // Right Tab
            val isRightSelected = !isLeftSelected
            val rightBgColor by animateColorAsState(
                targetValue = if (isRightSelected) Color.White else Color.Transparent,
                animationSpec = tween(200),
                label = "switchRightBg"
            )
            val rightTextColor by animateColorAsState(
                targetValue = if (isRightSelected) FigmaTerracotta else FigmaMutedSlate,
                animationSpec = tween(200),
                label = "switchRightText"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(9999.dp))
                    .background(rightBgColor)
                    .then(
                        if (isRightSelected) Modifier.border(1.dp, FigmaBorderActive, RoundedCornerShape(9999.dp))
                        else Modifier
                    )
                    .clickable {
                        if (!isRightSelected) {
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
                        fontWeight = if (isRightSelected) FontWeight.Bold else FontWeight.SemiBold
                    ),
                    color = rightTextColor,
                    maxLines = 1
                )
            }
        }
    }
}
