package com.gharmon255.dinostep.wear.ui.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Colors

private val WearColors = Colors(
    primary = androidx.compose.ui.graphics.Color(0xFFD0BCFF),
    secondary = androidx.compose.ui.graphics.Color(0xFFCCC2DC),
    background = androidx.compose.ui.graphics.Color(0xFF1C1B1F),
    surface = androidx.compose.ui.graphics.Color(0xFF1C1B1F),
    onPrimary = androidx.compose.ui.graphics.Color(0xFF381E72),
    onSecondary = androidx.compose.ui.graphics.Color(0xFF332D41),
    onBackground = androidx.compose.ui.graphics.Color(0xFFE6E1E5),
    onSurface = androidx.compose.ui.graphics.Color(0xFFE6E1E5),
)

@Composable
fun WearTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colors = WearColors,
        content = content,
    )
}
