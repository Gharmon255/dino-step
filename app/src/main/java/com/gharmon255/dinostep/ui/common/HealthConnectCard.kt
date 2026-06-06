package com.gharmon255.dinostep.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gharmon255.dinostep.health.HealthConnectUiStatus

@Composable
fun HealthConnectCard(
    status: HealthConnectUiStatus,
    lastSyncedStepTotal: Int,
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier,
    showRequestButton: Boolean = true,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Health Connect",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = status.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "Step data stays on your device. Stepasaurus does not sell or share your steps for ads.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (status is HealthConnectUiStatus.Ready) {
                Text(
                    text = "Last synced Health Connect total today: $lastSyncedStepTotal",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (showRequestButton && status is HealthConnectUiStatus.PermissionRequired) {
                Button(
                    onClick = onRequestPermission,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Allow step access")
                }
            }
            if (status is HealthConnectUiStatus.Unavailable || status is HealthConnectUiStatus.Error) {
                OutlinedButton(
                    onClick = onRequestPermission,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = status !is HealthConnectUiStatus.Unavailable,
                ) {
                    Text("Recheck Health Connect")
                }
            }
        }
    }
}
