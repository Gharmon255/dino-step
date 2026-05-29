package com.gharmon255.dinostep.wear.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
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

/** Insets for round displays — keeps labels off top/bottom bezels. */
private val RoundContentPadding = PaddingValues(
    top = 34.dp,
    bottom = 38.dp,
    start = 24.dp,
    end = 24.dp,
)
private val ProgressRingSize = 52.dp
private val ProgressStrokeWidth = 3.dp
private val CreatureSize = 30.dp

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
        val formatted = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(millis))
        "Updated $formatted"
    }
    val listState = rememberScalingLazyListState()

    Scaffold(
        modifier = modifier,
    ) {
        ScalingLazyColumn(
            modifier = Modifier.fillMaxWidth(),
            state = listState,
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = RoundContentPadding,
            verticalArrangement = Arrangement.spacedBy(1.dp, Alignment.CenterVertically),
        ) {
            item {
                Text(
                    text = state.syncStatusMessage,
                    style = MaterialTheme.typography.caption3,
                    fontSize = 9.sp,
                    color = if (state.isFromPhone) {
                        MaterialTheme.colors.primary
                    } else {
                        MaterialTheme.colors.onSurface.copy(alpha = 0.75f)
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            item {
                Text(
                    text = state.displayName,
                    style = MaterialTheme.typography.caption3,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            item {
                Text(
                    text = state.stageLabel,
                    style = MaterialTheme.typography.caption3,
                    fontSize = 9.sp,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.85f),
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            item {
                Box(
                    modifier = Modifier
                        .padding(vertical = 2.dp)
                        .size(ProgressRingSize),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        progress = progress,
                        modifier = Modifier.size(ProgressRingSize),
                        strokeWidth = ProgressStrokeWidth,
                        indicatorColor = MaterialTheme.colors.primary,
                        trackColor = MaterialTheme.colors.onSurface.copy(alpha = 0.2f),
                    )

                    CreatureVisual(
                        stage = state.stage,
                        emoji = state.displayEmoji,
                        modifier = Modifier.size(CreatureSize),
                    )
                }
            }

            item {
                Text(
                    text = "${state.progressPercent.toInt()}%",
                    style = MaterialTheme.typography.caption3,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            item {
                Text(
                    text = stepsUntilLabel,
                    style = MaterialTheme.typography.caption3,
                    fontSize = 9.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            if (lastUpdatedLabel != null) {
                item {
                    Text(
                        text = lastUpdatedLabel,
                        style = MaterialTheme.typography.caption3,
                        fontSize = 8.sp,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.55f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}
