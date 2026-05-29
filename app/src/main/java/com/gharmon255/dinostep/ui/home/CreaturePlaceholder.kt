package com.gharmon255.dinostep.ui.home

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gharmon255.dinostep.model.GrowthStage

@Composable
fun CreaturePlaceholder(
    stage: GrowthStage,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(160.dp)
            .size(160.dp),
        contentAlignment = Alignment.Center,
    ) {
        when (stage) {
            GrowthStage.EGG -> EggAnimation()
            GrowthStage.BABY -> BabyAnimation()
            GrowthStage.JUVENILE -> DinoPlaceholder(fontSizeSp = 96)
            GrowthStage.ADULT -> DinoPlaceholder(fontSizeSp = 120)
        }
    }
}

@Composable
private fun EggAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "eggRock")
    val rotation by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "eggRotation",
    )

    Text(
        text = "🥚",
        fontSize = 96.sp,
        modifier = Modifier.graphicsLayer { rotationZ = rotation },
    )
}

@Composable
private fun BabyAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "babyBounce")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -28f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "babyOffset",
    )

    Text(
        text = "🦖",
        fontSize = 72.sp,
        modifier = Modifier.graphicsLayer {
            translationY = offsetY
        },
    )
}

@Composable
private fun DinoPlaceholder(fontSizeSp: Int) {
    Text(
        text = "🦕",
        fontSize = fontSizeSp.sp,
    )
}
