package com.gharmon255.dinostep.wear.ui

import android.util.Log
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
    stageDrawableKey: String = "",
) {
    val resources = LocalContext.current.resources
    val packageName = LocalContext.current.packageName
    val safeSpeciesId = speciesId.trim()
    val safeEmoji = emoji.ifBlank { "🦕" }
    val safeDrawableKey = stageDrawableKey.trim()
    val drawableId = remember(safeSpeciesId, stage, eggRarityName, safeDrawableKey, packageName) {
        when (stage) {
            WearGrowthStage.EGG -> DrawableCreatureResolver.eggDrawableId(
                resources = resources,
                packageName = packageName,
                eggRarityName = eggRarityName.ifBlank { "COMMON" },
            )
            else -> DrawableCreatureResolver.stageDrawableIdFromSync(
                resources = resources,
                packageName = packageName,
                speciesId = safeSpeciesId,
                stageName = stage.name,
                stageDrawableKey = safeDrawableKey,
            )
        }
    }

    if (drawableId == 0 && stage != WearGrowthStage.EGG) {
        Log.w(
            TAG,
            "Wear art fallback to emoji: speciesId=$safeSpeciesId stage=$stage " +
                "drawableKey=${safeDrawableKey.ifBlank { "—" }}",
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
            text = safeEmoji,
            fontSize = fontSizeSp.sp,
            modifier = modifier,
        )
    }
}

private const val TAG = "WearCreatureArt"
