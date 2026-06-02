package com.gharmon255.dinostep.wear.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material.Colors
import androidx.wear.compose.material.MaterialTheme

/** Watch palette: dark moss background, bright leaf primary, high-contrast text. */
private val WearColors = Colors(
    primary = Color(0xFF7DCE8A),
    secondary = Color(0xFFE8B84A),
    background = Color(0xFF1A231C),
    surface = Color(0xFF283229),
    onPrimary = Color(0xFF0D2812),
    onSecondary = Color(0xFF2A2008),
    onBackground = Color(0xFFE8EDE6),
    onSurface = Color(0xFFE8EDE6),
)

@Composable
fun WearTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colors = WearColors,
        content = content,
    )
}
