package org.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindow
import org.example.config.Theme
import org.example.util.Utils

class ErrorDialog(themeAssets: Theme, private val message: String) {
    private val textColor = Utils.parseColor(themeAssets.text)
    private val backgroundColor = Utils.parseColor(themeAssets.background)

    @Composable
    fun show(onDismiss: () -> Unit) {
        DialogWindow(onCloseRequest = { onDismiss() }, resizable = false) {
            Surface(color = backgroundColor) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Error: $message",
                            style = TextStyle(color = textColor)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { onDismiss() }) {
                            Text("OK")
                        }
                    }
                }
            }
        }
    }
}