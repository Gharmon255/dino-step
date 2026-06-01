package com.gharmon255.dinostep.wear.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Text
import com.gharmon255.dinostep.wear.model.WearGrowthStage

@Composable
fun CreatureVisual(
    stage: WearGrowthStage,
    creatureId: String,
    eggRarityName: String,
    emoji: String,
    accentColor: Color,
    speciesShortLabel: String = "",
    stageScale: Float = 1f,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        when (stage) {
            WearGrowthStage.EGG -> EggVisual(
                accentColor = accentColor,
                creatureId = creatureId,
                eggRarityName = eggRarityName,
                emoji = emoji,
            )
            WearGrowthStage.BABY -> SpeciesVisual(
                creatureId = creatureId,
                eggRarityName = eggRarityName,
                stage = stage,
                emoji = emoji,
                accentColor = accentColor,
                speciesShortLabel = speciesShortLabel,
                baseFontSizeSp = 24,
                stageScale = stageScale,
                bounce = true,
            )
            WearGrowthStage.JUVENILE -> SpeciesVisual(
                creatureId = creatureId,
                eggRarityName = eggRarityName,
                stage = stage,
                emoji = emoji,
                accentColor = accentColor,
                speciesShortLabel = speciesShortLabel,
                baseFontSizeSp = 28,
                stageScale = stageScale,
                bounce = false,
            )
            WearGrowthStage.ADULT -> SpeciesVisual(
                creatureId = creatureId,
                eggRarityName = eggRarityName,
                stage = stage,
                emoji = emoji,
                accentColor = accentColor,
                speciesShortLabel = speciesShortLabel,
                baseFontSizeSp = 32,
                stageScale = stageScale,
                bounce = false,
            )
        }
    }
}

@Composable
private fun EggVisual(
    accentColor: Color,
    creatureId: String,
    eggRarityName: String,
    emoji: String,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wearEggRock")
    val rotation by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "wearEggRotation",
    )

    Box(contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.35f))
                .border(2.dp, accentColor, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Box(modifier = Modifier.graphicsLayer { rotationZ = rotation }) {
                WearCreatureDrawableOrEmoji(
                    creatureId = creatureId,
                    stage = WearGrowthStage.EGG,
                    eggRarityName = eggRarityName,
                    emoji = emoji,
                    imageSize = 32.dp,
                    fontSizeSp = 26,
                )
            }
        }
    }
}

@Composable
private fun SpeciesVisual(
    creatureId: String,
    eggRarityName: String,
    stage: WearGrowthStage,
    emoji: String,
    accentColor: Color,
    speciesShortLabel: String,
    baseFontSizeSp: Int,
    stageScale: Float,
    bounce: Boolean,
) {
    val scaledSp = (baseFontSizeSp * stageScale).toInt().coerceIn(18, 36)
    val imageSize = ((24f * stageScale).coerceIn(18f, 34f)).dp
    val content: @Composable () -> Unit = {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            WearCreatureDrawableOrEmoji(
                creatureId = creatureId,
                stage = stage,
                eggRarityName = eggRarityName,
                emoji = emoji,
                imageSize = imageSize,
                fontSizeSp = scaledSp,
            )
            if (speciesShortLabel.isNotBlank()) {
                Text(
                    text = speciesShortLabel,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                )
            }
        }
    }

    if (bounce) {
        val infiniteTransition = rememberInfiniteTransition(label = "wearBabyBounce")
        val offsetY by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = -4f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 450, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "wearBabyOffset",
        )
        Box(modifier = Modifier.graphicsLayer { translationY = offsetY }) {
            content()
        }
    } else {
        content()
    }
}
