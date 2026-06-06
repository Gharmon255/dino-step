package com.gharmon255.dinostep.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.gharmon255.dinostep.model.Rarity

private fun screenTintOpacity(rarity: Rarity, darkTheme: Boolean): Float {
    val base = when (rarity) {
        Rarity.COMMON -> 0.06f
        Rarity.UNCOMMON -> 0.10f
        Rarity.RARE -> 0.16f
        Rarity.EPIC -> 0.20f
        Rarity.LEGENDARY -> 0.24f
    }
    return if (darkTheme) base + 0.06f else base
}

/** Ambient rarity wash behind Home — egg tint before hatch, creature tint after (iOS parity). */
@Composable
fun RarityScreenBackground(
    rarity: Rarity,
    modifier: Modifier = Modifier,
) {
    val colors = rarityColors(rarity)
    val darkTheme = isSystemInDarkTheme()
    val tint = screenTintOpacity(rarity, darkTheme)
    val baseColor = if (darkTheme) Color(0xFF121716) else MaterialTheme.colorScheme.background

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(baseColor),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to colors.accent.copy(alpha = tint),
                        0.45f to colors.accent.copy(alpha = tint * 0.55f),
                        0.72f to Color.Transparent,
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            colors.accent.copy(alpha = tint * 0.85f),
                            Color.Transparent,
                        ),
                        center = Offset(0.5f, 0f),
                        radius = 900f,
                    ),
                ),
        )
    }
}
