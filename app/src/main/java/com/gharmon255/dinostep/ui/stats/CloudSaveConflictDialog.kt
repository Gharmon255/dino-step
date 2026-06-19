package com.gharmon255.dinostep.ui.stats

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.gharmon255.dinostep.cloud.CloudSaveConflict

@Composable
fun CloudSaveConflictDialog(
    conflict: CloudSaveConflict.LocalVsCloud,
    onKeepLocal: () -> Unit,
    onUseCloud: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose a save") },
        text = {
            Text(
                "This device and your cloud backup both have progress. " +
                    "Keeping this device uploads your current game to the cloud. " +
                    "Using cloud save replaces this device with the backup from your account.",
            )
        },
        confirmButton = {
            Button(onClick = onKeepLocal) {
                Text("Keep this device")
            }
        },
        dismissButton = {
            TextButton(onClick = onUseCloud) {
                Text("Use cloud save")
            }
        },
    )
}
