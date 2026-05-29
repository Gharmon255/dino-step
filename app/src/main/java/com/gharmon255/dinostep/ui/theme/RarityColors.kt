package com.gharmon255.dinostep.ui.theme

import androidx.compose.ui.graphics.Color
import com.gharmon255.dinostep.model.EggRarity
import com.gharmon255.dinostep.model.Rarity
import com.gharmon255.dinostep.shared.visual.RarityTheme

data class RarityColorSet(
    val accent: Color,
    val container: Color,
    val onContainer: Color,
    val border: Color,
)

fun Long.toComposeColor(): Color = Color(this)

fun rarityColors(rarity: Rarity): RarityColorSet = rarityColorsByName(rarity.name)

fun rarityColors(eggRarity: EggRarity): RarityColorSet = rarityColorsByName(eggRarity.name)

fun rarityColorsByName(rarityName: String): RarityColorSet {
    val accent = RarityTheme.accentArgbForName(rarityName).toComposeColor()
    return RarityColorSet(
        accent = accent,
        container = accent.copy(alpha = 0.22f),
        onContainer = accent.copy(alpha = 0.95f),
        border = accent.copy(alpha = 0.75f),
    )
}
