package com.example.intrack.ui.theme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.intrack.util.CurrencyFormatter

/**
 * Standard centered hero currency display used across Expenses overview, Goals overview,
 * and Goal detail screens.
 * Aligns the currency prefix and numeric figures along their baseline.
 */
@Composable
fun InTrackHeroAmount(
    amountLkr: Double,
    modifier: Modifier = Modifier,
    prefix: String = CurrencyFormatter.getCurrencySymbol(),
    amountFontSize: TextUnit = 30.sp,
    prefixFontSize: TextUnit = 18.sp,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    prefixColor: Color = FigmaTerracotta,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Center
) {
    val activeSymbol = CurrencyFormatter.getCurrencySymbol()
    val fullFormatted = CurrencyFormatter.formatLkr(amountLkr)
    val numericFormatted = fullFormatted.replace(activeSymbol, "").trim()

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = Alignment.Bottom
    ) {
        Text(
            text = prefix,
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = prefixFontSize,
                fontWeight = FontWeight.Bold
            ),
            color = prefixColor,
            modifier = Modifier.alignByBaseline()
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = numericFormatted,
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = amountFontSize,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            ),
            color = textColor,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.alignByBaseline()
        )
    }
}
