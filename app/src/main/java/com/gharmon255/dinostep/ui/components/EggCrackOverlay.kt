package com.gharmon255.dinostep.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun EggCrackOverlay(
    crackLevel: Int,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 2.dp,
) {
    if (crackLevel <= 0) {
        return
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val color = Color.Black.copy(alpha = 0.5f + crackLevel * 0.08f)
        val stroke = strokeWidth.toPx()
        val center = Offset(size.width / 2f, size.height / 2f)

        fun crack(from: Offset, to: Offset) {
            drawLine(
                color = color,
                start = from,
                end = to,
                strokeWidth = stroke,
                cap = StrokeCap.Round,
            )
        }

        if (crackLevel >= 1) {
            crack(
                Offset(center.x - size.width * 0.08f, center.y - size.height * 0.18f),
                Offset(center.x + size.width * 0.12f, center.y + size.height * 0.1f),
            )
        }
        if (crackLevel >= 2) {
            crack(
                Offset(center.x + size.width * 0.1f, center.y - size.height * 0.2f),
                Offset(center.x - size.width * 0.06f, center.y + size.height * 0.16f),
            )
            crack(
                Offset(center.x - size.width * 0.18f, center.y + size.height * 0.04f),
                Offset(center.x + size.width * 0.04f, center.y + size.height * 0.22f),
            )
        }
        if (crackLevel >= 3) {
            crack(
                Offset(center.x, center.y - size.height * 0.28f),
                Offset(center.x - size.width * 0.14f, center.y + size.height * 0.08f),
            )
            crack(
                Offset(center.x + size.width * 0.16f, center.y - size.height * 0.06f),
                Offset(center.x + size.width * 0.02f, center.y + size.height * 0.28f),
            )
        }
    }
}
