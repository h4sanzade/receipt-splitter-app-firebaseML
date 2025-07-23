package com.hasanzade.germanstyle.components

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.window.DialogProperties

@Composable
fun ErrorDialog(
    errorMessage: String?,
    onDismiss: () -> Unit
) {
    if (errorMessage != null) {
        AlertDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text("OK")
                }
            },
            title = {
                Text("Error")
            },
            text = {
                Text(errorMessage)
            },
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        )
    }
}
