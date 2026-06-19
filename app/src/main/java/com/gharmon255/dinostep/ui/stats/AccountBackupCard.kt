package com.gharmon255.dinostep.ui.stats

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gharmon255.dinostep.cloud.CloudAccountUiState

internal const val ACCOUNT_SIGN_IN_ENABLED = false

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

            if (!ACCOUNT_SIGN_IN_ENABLED) {
                Text(
                    text = "Sign in to back up progress across devices. Gameplay works offline without an account.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "Coming soon",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                )
            } else when {
                !cloudState.isConfigured -> {
                    Text(
                        text = "Cloud backup is not configured in this build.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                cloudState.signedInEmail != null -> {
                    SignedInAccountContent(
                        cloudState = cloudState,
                        onSignOut = onSignOut,
                    )
                }
                else -> {
                    SignedOutAccountContent(
                        cloudState = cloudState,
                        googleWebClientId = googleWebClientId,
                        onGoogleSignIn = onGoogleSignIn,
                    )
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

@Composable
private fun SignedInAccountContent(
    cloudState: CloudAccountUiState,
    onSignOut: () -> Unit,
) {
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
    OutlinedButton(
        onClick = onSignOut,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text("Sign out")
    }
}

@Composable
private fun SignedOutAccountContent(
    cloudState: CloudAccountUiState,
    googleWebClientId: String,
    onGoogleSignIn: () -> Unit,
) {
    Text(
        text = "Sign in to back up progress across devices. Gameplay works offline without an account.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    if (googleWebClientId.isNotBlank()) {
        OutlinedButton(
            onClick = onGoogleSignIn,
            modifier = Modifier.fillMaxWidth(),
            enabled = cloudState.syncStatus != com.gharmon255.dinostep.cloud.CloudSyncStatus.Syncing,
        ) {
            Text("Sign in with Google")
        }
    }
    cloudState.lastError?.let { error ->
        Text(
            text = error,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
        )
    }
}

private fun backupStatusText(cloudState: CloudAccountUiState): String? {
    return when (cloudState.syncStatus) {
        com.gharmon255.dinostep.cloud.CloudSyncStatus.Syncing -> "Backing up…"
        com.gharmon255.dinostep.cloud.CloudSyncStatus.BackedUp -> {
            val millis = cloudState.lastBackedUpAtMillis
            if (millis != null) {
                val formatted = java.text.DateFormat.getDateTimeInstance(
                    java.text.DateFormat.MEDIUM,
                    java.text.DateFormat.SHORT,
                    java.util.Locale.getDefault(),
                ).format(java.util.Date(millis))
                "Last backed up $formatted"
            } else {
                "Backup enabled"
            }
        }
        com.gharmon255.dinostep.cloud.CloudSyncStatus.Error -> "Backup error"
        com.gharmon255.dinostep.cloud.CloudSyncStatus.SignedOut,
        com.gharmon255.dinostep.cloud.CloudSyncStatus.Unavailable,
        -> null
    }
}
