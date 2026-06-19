package com.gharmon255.dinostep.ui.stats

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gharmon255.dinostep.cloud.CloudAccountUiState
import com.gharmon255.dinostep.cloud.CloudSyncStatus
import com.gharmon255.dinostep.game.GameViewModel
import java.text.DateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AccountBackupCard(
    cloudState: CloudAccountUiState,
    googleWebClientId: String,
    onGoogleSignIn: () -> Unit,
    onSignOut: () -> Unit,
    onExportSave: () -> String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Account & backup",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            when {
                !cloudState.isConfigured -> {
                    Text(
                        text = "Cloud backup is not configured in this build.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                cloudState.signedInEmail != null -> {
                    Text(
                        text = "Signed in as ${cloudState.signedInEmail}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    cloudState.signedInProvider?.let { provider ->
                        Text(
                            text = "Provider: $provider",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    backupStatusText(cloudState)?.let { status ->
                        Text(
                            text = status,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (cloudState.syncStatus == CloudSyncStatus.Syncing) {
                        CircularProgressIndicator()
                    }
                    cloudState.lastError?.let { error ->
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                    OutlinedButton(
                        onClick = onSignOut,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Sign out")
                    }
                }
                else -> {
                    Text(
                        text = "Sign in to back up progress across devices. Gameplay works offline without an account.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (googleWebClientId.isNotBlank()) {
                        Button(
                            onClick = onGoogleSignIn,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = cloudState.syncStatus != CloudSyncStatus.Syncing,
                        ) {
                            Text("Sign in with Google")
                        }
                    }
                    if (cloudState.syncStatus == CloudSyncStatus.Syncing) {
                        CircularProgressIndicator()
                    }
                    cloudState.lastError?.let { error ->
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }

            OutlinedButton(
                onClick = {
                    val json = onExportSave()
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/json"
                        putExtra(Intent.EXTRA_TEXT, json)
                        putExtra(Intent.EXTRA_SUBJECT, "stepasaurus-save-backup.json")
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Export local save"))
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Export local save")
            }
        }
    }
}

private fun backupStatusText(cloudState: CloudAccountUiState): String? {
    return when (cloudState.syncStatus) {
        CloudSyncStatus.Syncing -> "Backing up…"
        CloudSyncStatus.BackedUp -> {
            val millis = cloudState.lastBackedUpAtMillis
            if (millis != null) {
                val formatted = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.getDefault())
                    .format(Date(millis))
                "Last backed up $formatted"
            } else {
                "Backup enabled"
            }
        }
        CloudSyncStatus.Error -> "Backup error"
        CloudSyncStatus.SignedOut, CloudSyncStatus.Unavailable -> null
    }
}
