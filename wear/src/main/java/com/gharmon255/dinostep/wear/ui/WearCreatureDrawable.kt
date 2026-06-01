package com.gharmon255.dinostep.wear.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Text
import com.gharmon255.dinostep.shared.visual.DrawableCreatureResolver
import com.gharmon255.dinostep.wear.model.WearGrowthStage

@Composable
fun WearCreatureDrawableOrEmoji(
    speciesId: String,
    stage: WearGrowthStage,
    eggRarityName: String,
    emoji: String,
    imageSize: Dp,
    fontSizeSp: Int,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    val resources = LocalContext.current.resources
    val packageName = LocalContext.current.packageName
    val drawableId = remember(speciesId, stage, eggRarityName, packageName) {
        when (stage) {
            WearGrowthStage.EGG -> DrawableCreatureResolver.eggDrawableId(
                resources = resources,
                packageName = packageName,
                eggRarityName = eggRarityName,
            )
            else -> DrawableCreatureResolver.stageDrawableId(
                resources = resources,
                packageName = packageName,
                speciesId = speciesId,
                stageName = stage.name,
            )
        }
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
