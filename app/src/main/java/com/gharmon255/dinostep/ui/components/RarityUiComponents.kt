package com.gharmon255.dinostep.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gharmon255.dinostep.game.ActiveCreatureState
import com.gharmon255.dinostep.model.CreatureVisualMapper
import com.gharmon255.dinostep.model.EggRarity
import com.gharmon255.dinostep.model.GrowthStage
import com.gharmon255.dinostep.model.Rarity
import com.gharmon255.dinostep.model.StageVisual
import com.gharmon255.dinostep.model.toRarity
import com.gharmon255.dinostep.ui.theme.RarityColorSet
import com.gharmon255.dinostep.ui.theme.rarityColors

@Composable
fun RarityBadge(
    label: String,
    colors: RarityColorSet,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = colors.container,
        contentColor = colors.onContainer,
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
fun RarityBadge(
    rarity: Rarity,
    modifier: Modifier = Modifier,
) {
    RarityBadge(
        label = rarity.name,
        colors = rarityColors(rarity),
        modifier = modifier,
    )
}

@Composable
fun RarityBadge(
    eggRarity: EggRarity,
    modifier: Modifier = Modifier,
) {
    RarityBadge(
        label = eggRarity.name,
        colors = rarityColors(eggRarity),
        modifier = modifier,
    )
}

@Composable
private fun EggStageVisual(
    eggRarity: EggRarity,
    drawableId: Int,
    emoji: String,
    frameSize: Dp,
    imageSize: Dp,
    animateEgg: Boolean,
) {
    val colors = rarityColors(eggRarity)
    val showBaseGlow = eggRarity != EggRarity.COMMON

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            contentAlignment = Alignment.BottomCenter,
            modifier = Modifier.size(frameSize),
        ) {
            if (showBaseGlow) {
                Box(
                    modifier = Modifier
                        .size(width = frameSize * 0.72f, height = frameSize * 0.14f)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    colors.accent.copy(alpha = 0.42f),
                                    colors.accent.copy(alpha = 0.12f),
                                    colors.accent.copy(alpha = 0f),
                                ),
                            ),
                        ),
                )
            }

            val imageContent: @Composable () -> Unit = {
                StageCreatureImageOrEmoji(
                    drawableId = drawableId,
                    emoji = emoji,
                    imageSize = imageSize,
                    fontSizeSp = (frameSize.value * 0.38f).toInt().coerceIn(48, 80),
                    contentDescription = "${eggRarity.displayName} egg",
                )
            }

            if (animateEgg) {
                val infiniteTransition = rememberInfiniteTransition(label = "eggRock")
                val rotation by infiniteTransition.animateFloat(
                    initialValue = -6f,
                    targetValue = 6f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 700, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse,
                    ),
                    label = "eggRotation",
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .graphicsLayer { rotationZ = rotation },
                ) {
                    imageContent()
                }
            } else {
                Box(modifier = Modifier.align(Alignment.Center)) {
                    imageContent()
                }
            }
        }
    }
}

@Composable
fun CreatureStageVisual(
    activeCreature: ActiveCreatureState,
    modifier: Modifier = Modifier,
    frameSize: Dp = 180.dp,
    showSpeciesHint: Boolean = true,
) {
    val visual = CreatureVisualMapper.visualForActiveCreature(activeCreature)
    val stage = activeCreature.stage
    val eggRarity = activeCreature.eggRarity
    val displayRarity = if (activeCreature.isRevealed) {
        activeCreature.creature.rarity
    } else {
        null
    }
    val colors = rarityColors(displayRarity ?: eggRarity.toRarity())
    val baseEmojiSp = (frameSize.value * 0.42f * visual.stageScale).toInt().coerceIn(40, 110)
    val drawableId = CreatureStageDrawableResolve.resolveDrawableId(
        speciesId = activeCreature.creature.id,
        stage = stage,
        eggRarity = eggRarity,
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        when (stage) {
            GrowthStage.EGG -> {
                EggStageVisual(
                    eggRarity = eggRarity,
                    drawableId = drawableId,
                    emoji = visual.displayEmoji,
                    frameSize = frameSize,
                    imageSize = frameSize * 0.78f,
                    animateEgg = true,
                )
            }
            else -> StageDinoVisual(
                visual = visual,
                colors = colors,
                drawableId = drawableId,
                baseFontSizeSp = baseEmojiSp,
                bounce = stage == GrowthStage.BABY,
                frameSize = frameSize,
                showSpeciesHint = showSpeciesHint && activeCreature.isRevealed,
            )
        }
    }
}

@Composable
private fun StageCreatureImageOrEmoji(
    drawableId: Int,
    emoji: String,
    imageSize: Dp,
    fontSizeSp: Int,
    contentDescription: String?,
) {
    if (drawableId != 0) {
        Image(
            painter = painterResource(drawableId),
            contentDescription = contentDescription,
            modifier = Modifier.size(imageSize),
            contentScale = ContentScale.Fit,
        )
    } else {
        StageEmojiText(emoji = emoji, fontSizeSp = fontSizeSp)
    }
}

@Composable
private fun StageDinoVisual(
    visual: StageVisual,
    colors: RarityColorSet,
    drawableId: Int,
    baseFontSizeSp: Int,
    bounce: Boolean,
    frameSize: Dp,
    showSpeciesHint: Boolean,
) {
    val visualSize = frameSize * visual.stageScale.coerceAtLeast(0.7f)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        val creatureContent: @Composable () -> Unit = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                StageCreatureImageOrEmoji(
                    drawableId = drawableId,
                    emoji = visual.speciesEmoji,
                    imageSize = visualSize,
                    fontSizeSp = baseFontSizeSp,
                    contentDescription = visual.stageDetailLabel,
                )
                if (showSpeciesHint) {
                    Text(
                        text = visual.speciesShortLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = colors.accent,
                    )
                }
            }
        }
        if (bounce) {
            val infiniteTransition = rememberInfiniteTransition(label = "babyBounce")
            val offsetY by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = -16f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 450, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "babyOffset",
            )
            Box(modifier = Modifier.graphicsLayer { translationY = offsetY }) {
                creatureContent()
            }
        } else {
            creatureContent()
        }
        Text(
            text = visual.stageDetailLabel,
            style = MaterialTheme.typography.labelMedium,
            color = colors.accent,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun StageEmojiText(emoji: String, fontSizeSp: Int) {
    Text(text = emoji, fontSize = fontSizeSp.sp)
}

@Composable
fun GameCreatureCard(
    title: String,
    stage: GrowthStage,
    eggRarity: EggRarity,
    creatureRarity: Rarity?,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val accentRarity = creatureRarity ?: eggRarity.toRarity()
    val colors = rarityColors(accentRarity)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(2.dp, colors.border, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            RowWithBadges(
                eggRarity = eggRarity,
                creatureRarity = creatureRarity,
                stage = stage,
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
            )
            content()
        }
    }
}

@Composable
private fun RowWithBadges(
    eggRarity: EggRarity,
    creatureRarity: Rarity?,
    stage: GrowthStage,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (creatureRarity != null && stage != GrowthStage.EGG) {
            RarityBadge(rarity = creatureRarity)
        } else {
            RarityBadge(eggRarity = eggRarity)
        }
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Text(
                text = stage.name,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

/** Undiscovered roster entry: mystery styling only — no species drawable or name art. */
@Composable
fun LockedCollectionAvatar(
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 88.dp,
) {
    Column(
        modifier = modifier.size(size),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "?",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = "🔒",
            fontSize = 12.sp,
        )
    }
}

/**
 * Discovered collection entry: adult PNG via [DrawableCreatureResolver] when asset-backed,
 * else stage placeholder vector, else species emoji — never another species' art.
 */
@Composable
fun CollectionCreatureAvatar(
    visual: StageVisual,
    creatureId: String,
    rarity: Rarity,
    modifier: Modifier = Modifier,
    frameSize: androidx.compose.ui.unit.Dp = 96.dp,
    imageSize: androidx.compose.ui.unit.Dp = 80.dp,
) {
    val drawableId = CreatureStageDrawableResolve.resolveAdultDrawableId(speciesId = creatureId)
    val fallbackEmoji = visual.displayEmoji.ifBlank { visual.speciesEmoji }
    Box(
        modifier = modifier.size(frameSize),
        contentAlignment = Alignment.Center,
    ) {
        if (drawableId != 0) {
            Image(
                painter = painterResource(drawableId),
                contentDescription = visual.stageDetailLabel,
                modifier = Modifier.size(imageSize),
                contentScale = ContentScale.Fit,
            )
        } else {
            Text(text = fallbackEmoji, fontSize = 36.sp)
        }
    }
}

@Composable
fun RarityOutlinedButton(
    label: String,
    eggRarity: EggRarity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = rarityColors(eggRarity)
    androidx.compose.material3.OutlinedButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, colors.border),
    ) {
        Text(text = label, color = colors.accent)
    }
}
