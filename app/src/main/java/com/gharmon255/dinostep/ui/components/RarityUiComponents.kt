package com.gharmon255.dinostep.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gharmon255.dinostep.game.ActiveCreatureState
import com.gharmon255.dinostep.model.CreatureVisualMapper
import com.gharmon255.dinostep.model.EggRarity
import com.gharmon255.dinostep.model.GrowthStage
import com.gharmon255.dinostep.model.Rarity
import com.gharmon255.dinostep.model.toRarity
import com.gharmon255.dinostep.model.StageVisual
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
fun RarityEggFrame(
    eggRarity: EggRarity,
    modifier: Modifier = Modifier,
    size: Dp = 160.dp,
    animateEgg: Boolean = true,
    content: @Composable BoxScope.() -> Unit,
) {
    val colors = rarityColors(eggRarity)
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(colors.container)
            .border(width = 3.dp, color = colors.border, shape = CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        if (animateEgg) {
            val infiniteTransition = rememberInfiniteTransition(label = "eggRock")
            val rotation by infiniteTransition.animateFloat(
                initialValue = -8f,
                targetValue = 8f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 700, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "eggRotation",
            )
            Box(modifier = Modifier.graphicsLayer { rotationZ = rotation }) {
                content()
            }
        } else {
            content()
        }
    }
}

@Composable
fun CreatureStageVisual(
    activeCreature: ActiveCreatureState,
    modifier: Modifier = Modifier,
    frameSize: Dp = 180.dp,
) {
    val visual = CreatureVisualMapper.visualForActiveCreature(activeCreature)
    val stage = activeCreature.stage
    val eggRarity = activeCreature.eggRarity
    val displayRarity = if (activeCreature.isRevealed) {
        activeCreature.creature.rarity
    } else {
        null
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        when (stage) {
            GrowthStage.EGG -> {
                RarityEggFrame(
                    eggRarity = eggRarity,
                    size = frameSize,
                ) {
                    StageEmojiText(emoji = visual.placeholderEmoji, fontSizeSp = 72)
                }
            }
            GrowthStage.BABY -> StageDinoVisual(
                visual = visual,
                colors = rarityColors(displayRarity ?: eggRarity.toRarity()),
                fontSizeSp = 64,
                bounce = true,
                frameSize = frameSize,
            )
            GrowthStage.JUVENILE -> StageDinoVisual(
                visual = visual,
                colors = rarityColors(displayRarity ?: eggRarity.toRarity()),
                fontSizeSp = 88,
                bounce = false,
                frameSize = frameSize,
            )
            GrowthStage.ADULT -> StageDinoVisual(
                visual = visual,
                colors = rarityColors(displayRarity ?: eggRarity.toRarity()),
                fontSizeSp = 104,
                bounce = false,
                frameSize = frameSize,
            )
        }
    }
}

@Composable
private fun StageDinoVisual(
    visual: StageVisual,
    colors: RarityColorSet,
    fontSizeSp: Int,
    bounce: Boolean,
    frameSize: Dp,
) {
    Box(
        modifier = Modifier
            .size(frameSize)
            .clip(RoundedCornerShape(24.dp))
            .background(colors.container)
            .border(2.dp, colors.border, RoundedCornerShape(24.dp)),
        contentAlignment = Alignment.Center,
    ) {
        if (bounce) {
            val infiniteTransition = rememberInfiniteTransition(label = "babyBounce")
            val offsetY by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = -20f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 450, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "babyOffset",
            )
            Box(modifier = Modifier.graphicsLayer { translationY = offsetY }) {
                StageEmojiText(emoji = visual.placeholderEmoji, fontSizeSp = fontSizeSp)
            }
        } else {
            StageEmojiText(emoji = visual.placeholderEmoji, fontSizeSp = fontSizeSp)
        }
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
    progressPercent: Float,
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
            Text(
                text = "Progress ${progressPercent.toInt()}%",
                style = MaterialTheme.typography.labelLarge,
                color = colors.accent,
                modifier = Modifier.padding(top = 8.dp),
            )
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

@Composable
fun CollectionCreatureAvatar(
    emoji: String,
    rarity: Rarity,
    collected: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = rarityColors(rarity)
    Box(
        modifier = modifier
            .size(56.dp)
            .alpha(if (collected) 1f else 0.45f)
            .clip(CircleShape)
            .background(if (collected) colors.container else MaterialTheme.colorScheme.surfaceVariant)
            .border(
                width = if (collected) 2.dp else 1.dp,
                color = if (collected) colors.border else MaterialTheme.colorScheme.outline,
                shape = CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = emoji, fontSize = 28.sp)
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
