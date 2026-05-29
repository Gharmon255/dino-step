package com.gharmon255.dinostep.wear.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Text
import com.gharmon255.dinostep.wear.model.WearGrowthStage

@Composable
fun CreatureVisual(
    stage: WearGrowthStage,
    emoji: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        when (stage) {
            WearGrowthStage.EGG -> EggVisual()
            WearGrowthStage.BABY -> BabyVisual(emoji = emoji)
            WearGrowthStage.JUVENILE -> DinoVisual(emoji = emoji, fontSizeSp = 18)
            WearGrowthStage.ADULT -> AdultVisual(emoji = emoji)
        }
    }
}

@Composable
private fun EggVisual() {
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

    Text(
        text = "🥚",
        fontSize = 18.sp,
        modifier = Modifier.graphicsLayer { rotationZ = rotation },
    )
}

@Composable
private fun BabyVisual(emoji: String) {
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

    Text(
        text = emoji,
        fontSize = 16.sp,
        modifier = Modifier.graphicsLayer { translationY = offsetY },
    )
}

@Composable
private fun DinoVisual(emoji: String, fontSizeSp: Int) {
    Text(
        text = emoji,
        fontSize = fontSizeSp.sp,
    )
}

@Composable
private fun AdultVisual(emoji: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = emoji,
            fontSize = 20.sp,
        )
        Text(
            text = "Grown",
            fontSize = 7.sp,
            textAlign = TextAlign.Center,
        )
    }
}
