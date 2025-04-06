package org.example.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.example.config.ConfigLoader
import org.example.controller.ScriptRunner
import org.example.ui.CommonStyles.MySpacer
import org.example.util.OutputTransformation
import org.example.util.ScriptTransformation
import org.example.util.Utils
import java.io.File
import java.io.IOException

class ScriptEditorView(theme: String) {
    private val config = ConfigLoader.config
    private val errorPrefix = config.errorPrefix
    private val scriptPath = config.command.scriptPath

    private val compilerNotFound = config.errors.compilerNotFound
    private val genericError = config.errors.generic

    private val keywordsJson = config.keywordsJson

    private val errorColor = Utils.parseColor(config.colors.error)
    private val primaryColor = Utils.parseColor(config.colors.primary)

    private val themeAssets = config.colors.themes[theme]!!
    private val textColor = Utils.parseColor(themeAssets.text)
    private val backgroundColor = Utils.parseColor(themeAssets.background)

    @Suppress("unused")  // Might be used in the future
    private val containerColor = Utils.parseColor(themeAssets.container)

    private val scriptTransformation: ScriptTransformation = ScriptTransformation(textColor)
    private val outputTransformation: OutputTransformation = OutputTransformation(errorColor)

    init {
        scriptTransformation.loadKeywordsFromJson(keywordsJson)
        outputTransformation.errorPrefix = errorPrefix

        CommonStyles.init(themeAssets)
    }

    @Composable
    @Preview
    fun render() {
        var script by remember { mutableStateOf(readScriptFromFile()) }
        var output by remember { mutableStateOf("") }
        var isRunning by remember { mutableStateOf(false) }
        var exitCode by remember { mutableStateOf<Int?>(null) }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        val scriptRunner = ScriptRunner()
        val scope = rememberCoroutineScope()
        val scrollState = rememberScrollState()

        // Automatically scroll to the bottom of the output during execution
        LaunchedEffect(output) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }

        Box(modifier = Modifier.fillMaxSize().background(backgroundColor)) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                BasicTextField(
                    value = script,
                    onValueChange = { script = it },
                    modifier = CommonStyles.textFieldModifier.weight(0.6f),
                    textStyle = CommonStyles.textStyle,
                    visualTransformation = scriptTransformation
                )
                MySpacer()
                BasicTextField(
                    value = output,
                    onValueChange = {},
                    readOnly = true,
                    modifier = CommonStyles.textFieldModifier
                        .weight(0.4f)
                        .verticalScroll(scrollState),
                    textStyle = CommonStyles.textStyle,
                    visualTransformation = outputTransformation
                )
                MySpacer()
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Button(
                        onClick = {  // Launching the script onClick
                            isRunning = true
                            output = ""
                            exitCode = null
                            errorMessage = null
                            scriptRunner.writeScriptToFile(script, scriptPath)

                            // Live output
                            scope.launch(Dispatchers.IO) {
                                try {
                                    val code = scriptRunner.runScript(
                                        { line ->
                                            scope.launch(Dispatchers.Main) {
                                                output += "$line\n"
                                            }
                                        },
                                        { error ->
                                            scope.launch(Dispatchers.Main) {
                                                output += "$errorPrefix $error\n"
                                            }
                                        }
                                    )
                                    isRunning = false
                                    exitCode = code
                                } catch (e: Exception) {  // Error handling
                                    isRunning = false
                                    errorMessage = if (e is IOException && e.message != null
                                        && e.message!!.contains("kotlinc")
                                        && e.message!!.startsWith("Cannot run program")
                                    )
                                        compilerNotFound + "\n\n${e.message}"
                                    else
                                        genericError + e.message
                                }
                            }
                        },
                        enabled = !isRunning
                    ) {
                        Text(if (isRunning) "Running..." else "Execute")
                    }
                }

                // Exit code display
                Text(
                    "Exit Code: ${exitCode?.toString() ?: "N/A"}",
                    color = if (exitCode == 0 || exitCode == null)
                        primaryColor
                    else
                        errorColor
                )

                // Error message dialog
                errorMessage?.let {
                    ErrorDialog(themeAssets, it).show {
                        errorMessage = null
                    }
                }
            }
        }
    }

    private fun readScriptFromFile(): String {
        return try {
            File(scriptPath).readText()
        } catch (e: IOException) {
            ""
        }
    }
}