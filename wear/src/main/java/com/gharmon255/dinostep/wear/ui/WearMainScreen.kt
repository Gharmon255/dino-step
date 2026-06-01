package com.gharmon255.dinostep.wear.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import com.gharmon255.dinostep.wear.model.WatchCreatureState
import com.gharmon255.dinostep.wear.model.WearGrowthStage
import java.text.NumberFormat
import java.util.Locale

/** Safe insets for round Samsung watches — extra bottom inset avoids bezel clipping. */
private val RoundHorizontalPadding = 22.dp
private val RoundTopPadding = 26.dp
private val RoundBottomPadding = 42.dp

private val ProgressRingSize = 88.dp
private val ProgressStrokeWidth = 5.dp
private val CreatureInRingSize = 50.dp

@Composable
fun WearMainScreen(
    state: WatchCreatureState,
    modifier: Modifier = Modifier,
) {
    val numberFormat = NumberFormat.getIntegerInstance(Locale.getDefault())
    // Ring = progress within current stage toward next milestone (not egg→adult lifetime).
    val progress = (state.progressPercent / 100f).coerceIn(0f, 1f)
    val stepsUntilNextLine = state.stepsUntilNextStageDisplay(numberFormat)

    Scaffold(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = RoundHorizontalPadding,
                    end = RoundHorizontalPadding,
                    top = RoundTopPadding,
                    bottom = RoundBottomPadding,
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            WatchHeaderSection(state = state)

            Spacer(modifier = Modifier.height(6.dp))

            ProgressRingSection(
                progress = progress,
                state = state,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${state.progressPercent.toInt()}%",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stepsUntilNextLine,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = state.accentColorArgb.toWearColor(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun WatchHeaderSection(state: WatchCreatureState) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = state.syncStatusMessage,
            fontSize = 11.sp,
            color = if (state.isFromPhone) {
                MaterialTheme.colors.primary
            } else {
                MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
            },
            maxLines = 1,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = state.displayName,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = state.stageLabel,
            fontSize = 11.sp,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.8f),
            maxLines = 1,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ProgressRingSection(
    progress: Float,
    state: WatchCreatureState,
) {
    val accentColor = state.accentColorArgb.toWearColor()
    Box(
        modifier = Modifier.size(ProgressRingSize),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            progress = progress,
            modifier = Modifier.size(ProgressRingSize),
            strokeWidth = ProgressStrokeWidth,
            indicatorColor = accentColor,
            trackColor = MaterialTheme.colors.onSurface.copy(alpha = 0.18f),
        )

        CreatureVisual(
            stage = state.stage,
            creatureId = state.speciesIdForArt,
            eggRarityName = state.eggRarity,
            emoji = state.displayEmoji,
            accentColor = accentColor,
            speciesShortLabel = state.speciesShortLabel,
            stageScale = state.stageScale,
            modifier = Modifier.size(CreatureInRingSize),
        )
    }
}
