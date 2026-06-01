package com.gharmon255.dinostep.wear.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Text
import com.gharmon255.dinostep.shared.visual.DrawableResourceResolver
import com.gharmon255.dinostep.wear.model.WearGrowthStage

@Composable
fun WearCreatureDrawableOrEmoji(
    creatureId: String,
    stage: WearGrowthStage,
    eggRarityName: String,
    emoji: String,
    imageSize: Dp,
    fontSizeSp: Int,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    val context = LocalContext.current
    val drawableId = when (stage) {
        WearGrowthStage.EGG -> DrawableResourceResolver.eggDrawableId(
            resources = context.resources,
            packageName = context.packageName,
            eggRarityName = eggRarityName,
        )
        else -> DrawableResourceResolver.stageDrawableId(
            resources = context.resources,
            packageName = context.packageName,
            creatureId = creatureId,
            stageName = stage.name,
        )
    }

    if (drawableId != 0) {
        Image(
            painter = painterResource(drawableId),
            contentDescription = contentDescription,
            modifier = modifier.size(imageSize),
            contentScale = ContentScale.Fit,
        )
    } else {
        Text(
            text = emoji,
            fontSize = fontSizeSp.sp,
            modifier = modifier,
        )
    }
}
