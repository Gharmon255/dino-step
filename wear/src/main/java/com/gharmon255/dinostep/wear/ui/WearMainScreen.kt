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
import java.text.SimpleDateFormat
import java.util.Date
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
    val progress = (state.progressPercent / 100f).coerceIn(0f, 1f)
    val stepsUntilLabel = if (state.stage == WearGrowthStage.ADULT) {
        "Fully grown"
    } else {
        "${numberFormat.format(state.stepsUntilNextMilestone)} to next"
    }
    val lastUpdatedLabel = state.lastUpdatedAtMillis?.let { millis ->
        val formatted = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(millis))
        "Updated $formatted"
    }

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
                stage = state.stage,
                emoji = state.displayEmoji,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${state.progressPercent.toInt()}%",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = stepsUntilLabel,
                fontSize = 10.sp,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.85f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            if (state.isFromPhone && lastUpdatedLabel != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = lastUpdatedLabel,
                    fontSize = 9.sp,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.5f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

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
    stage: WearGrowthStage,
    emoji: String,
) {
    Box(
        modifier = Modifier.size(ProgressRingSize),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            progress = progress,
            modifier = Modifier.size(ProgressRingSize),
            strokeWidth = ProgressStrokeWidth,
            indicatorColor = MaterialTheme.colors.primary,
            trackColor = MaterialTheme.colors.onSurface.copy(alpha = 0.18f),
        )

        CreatureVisual(
            stage = stage,
            emoji = emoji,
            modifier = Modifier.size(CreatureInRingSize),
        )
    }
}
