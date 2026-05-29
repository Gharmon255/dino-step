package com.gharmon255.dinostep.ui.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gharmon255.dinostep.game.GameViewModel
import com.gharmon255.dinostep.ui.common.HealthConnectCard
import com.gharmon255.dinostep.ui.common.StatRow
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StatsScreen(
    viewModel: GameViewModel,
    onRequestHealthPermission: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val numberFormat = NumberFormat.getIntegerInstance(Locale.getDefault())
    val wearDebug = viewModel.wearSyncDebug
    val lastSyncTimeLabel = wearDebug.lastAttemptTimeMillis?.let { millis ->
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(millis))
    } ?: "—"

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        HealthConnectCard(
            status = viewModel.healthConnectStatus,
            lastSyncedStepTotal = viewModel.lastSyncedStepTotal,
            onRequestPermission = onRequestHealthPermission,
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "Watch sync debug",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )

                StatRow(
                    label = "Watch nodes connected",
                    value = numberFormat.format(wearDebug.connectedNodeCount),
                )
                StatRow(
                    label = "Last watch sync attempt",
                    value = lastSyncTimeLabel,
                )
                StatRow(
                    label = "Last watch sync status",
                    value = wearDebug.lastStatusMessage,
                )
                StatRow(
                    label = "Last payload name",
                    value = wearDebug.lastPayloadDisplayName,
                )
                StatRow(
                    label = "Last payload stage",
                    value = wearDebug.lastPayloadStage,
                )
                StatRow(
                    label = "Last payload steps",
                    value = wearDebug.lastPayloadSteps?.let { numberFormat.format(it) } ?: "—",
                )
                Text(
                    text = "Last payload detail: ${wearDebug.lastPayloadSummary}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Button(
                    onClick = viewModel::forceWatchSync,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Force Watch Sync")
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "Player stats",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )

                StatRow(
                    label = "Active creature steps",
                    value = numberFormat.format(viewModel.steps),
                )
                StatRow(
                    label = "Total fake steps added",
                    value = numberFormat.format(viewModel.totalFakeStepsAdded),
                )
                StatRow(
                    label = "Last synced HC total (today)",
                    value = numberFormat.format(viewModel.lastSyncedStepTotal),
                )
                StatRow(
                    label = "Eggs hatched",
                    value = numberFormat.format(viewModel.eggsHatched),
                )
                StatRow(
                    label = "Completed dinosaurs",
                    value = numberFormat.format(viewModel.completedCount),
                )
                StatRow(
                    label = "Current stage",
                    value = viewModel.stage.name,
                )
                StatRow(
                    label = "Current progress",
                    value = "${viewModel.progressPercent.toInt()}%",
                )
            }
        }
    }
}
