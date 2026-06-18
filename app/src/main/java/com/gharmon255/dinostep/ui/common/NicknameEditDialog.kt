package com.gharmon255.dinostep.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import com.gharmon255.dinostep.model.CreatureNickname

@Composable
fun NicknameEditDialog(
    title: String,
    speciesName: String,
    initialNickname: String?,
    onDismiss: () -> Unit,
    onSave: (String?) -> Unit,
) {
    var fieldValue by remember(initialNickname) {
        mutableStateOf(TextFieldValue(initialNickname.orEmpty()))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                Text("Species: $speciesName")
                OutlinedTextField(
                    value = fieldValue,
                    onValueChange = { updated ->
                        if (updated.text.length <= CreatureNickname.MAX_LENGTH) {
                            fieldValue = updated
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Nickname (optional)") },
                    singleLine = true,
                    supportingText = {
                        Text("${fieldValue.text.length}/${CreatureNickname.MAX_LENGTH}")
                    },
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(CreatureNickname.normalize(fieldValue.text))
                    onDismiss()
                },
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}
