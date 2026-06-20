package com.gharmon255.dinostep.ui.battle

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.gharmon255.dinostep.ui.help.BattleIntroContent

@Composable
fun BattleIntroDialog(
    onDismiss: () -> Unit,
    onDontShowAgain: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(BattleIntroContent.TITLE) },
        text = { Text(BattleIntroContent.BODY) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Got it")
            }
        },
        dismissButton = {
            TextButton(onClick = onDontShowAgain) {
                Text("Don't show again")
            }
        },
    )
}
