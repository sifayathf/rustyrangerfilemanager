package com.rustyranger.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.rustyranger.app.ui.theme.TextMuted

@Composable
fun TreeConnectorLines(
    depth: Int,
    isLastChild: Boolean,
    ancestorIsLast: List<Boolean>,
    modifier: Modifier = Modifier,
    indentStep: Dp = 16.dp
) {
    if (depth <= 0) return

    val totalWidth = indentStep * depth
    val lineColor = TextMuted.copy(alpha = 0.35f)
    val strokeWidthPx = 1.5f

    Canvas(
        modifier = modifier
            .width(totalWidth)
            .fillMaxHeight()
    ) {
        val indentPx = indentStep.toPx()
        val halfHeight = size.height / 2f

        for (i in 0 until depth) {
            val startX = i * indentPx + indentPx / 2f
            val isCurrentLevel = (i == depth - 1)
            val parentIsLast = if (i < ancestorIsLast.size) ancestorIsLast[i] else false

            // Draw vertical ancestor guide lines
            if (!parentIsLast) {
                drawLine(
                    color = lineColor,
                    start = Offset(startX, 0f),
                    end = Offset(startX, if (isCurrentLevel && isLastChild) halfHeight else size.height),
                    strokeWidth = strokeWidthPx,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                )
            }

            // Draw horizontal branch connector line for current level
            if (isCurrentLevel) {
                drawLine(
                    color = lineColor,
                    start = Offset(startX, halfHeight),
                    end = Offset(size.width, halfHeight),
                    strokeWidth = strokeWidthPx,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                )
            }
        }
    }
}
