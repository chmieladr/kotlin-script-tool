package org.example.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    private val fontSize = config.fontSize
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
        CommonStyles.init(config, themeAssets)
    }

    @Composable
    @Preview
    fun render() {
        var script by remember { mutableStateOf(TextFieldValue(readScriptFromFile())) }
        var output by remember { mutableStateOf("") }
        var isRunning by remember { mutableStateOf(false) }
        var exitCode by remember { mutableStateOf<Int?>(null) }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        val scriptRunner = ScriptRunner()
        val scope = rememberCoroutineScope()
        val scriptScrollState = rememberScrollState()
        val outputScrollState = rememberScrollState()

        val focusRequester = remember { FocusRequester() }
        var layoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

        val lineHeightPx = with(LocalDensity.current) { fontSize.sp.toPx() }

        // Automatically scroll to the bottom of the output during execution
        LaunchedEffect(output) {
            outputScrollState.animateScrollTo(outputScrollState.maxValue)
        }

        Box(modifier = Modifier.fillMaxSize().background(backgroundColor)) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                // Script editor field
                BasicTextField(
                    value = script,
                    onValueChange = { script = it },
                    modifier = CommonStyles.textFieldModifier
                        .weight(0.6f)
                        .verticalScroll(scriptScrollState)
                        .focusRequester(focusRequester),
                    textStyle = CommonStyles.textStyle,
                    visualTransformation = scriptTransformation,
                    cursorBrush = SolidColor(textColor),
                    onTextLayout = { layoutResult = it },
                )

                MySpacer()

                // Output field
                Box(
                    modifier = CommonStyles.textFieldModifier
                        .weight(0.4f)
                        .verticalScroll(outputScrollState)
                        .pointerInput(Unit) {
                            detectTapGestures { tapOffset ->
                                val currentLayout = layoutResult ?: return@detectTapGestures
                                val offset = currentLayout.getOffsetForPosition(tapOffset)
                                val annotation = outputTransformation.getAnnotationAtOffset(output, offset)

                                // Handle the click on the annotation
                                annotation?.let { (line, position) ->
                                    val lines = script.text.lines()
                                    val targetLine = line.coerceIn(0, lines.size - 1)
                                    val targetPosition = position.coerceIn(0, lines[targetLine].length)
                                    val scrollOffset = (targetLine * lineHeightPx).toInt()

                                    scope.launch {
                                        scriptScrollState.animateScrollTo(scrollOffset)
                                    }

                                    val cursorIndex = lines.take(targetLine).sumOf { it.length + 1 } + targetPosition
                                    script = script.copy(selection = TextRange(cursorIndex))
                                    focusRequester.requestFocus()
                                }
                            }
                        }
                ) {
                    BasicText(
                        text = outputTransformation.filter(AnnotatedString(output)).text,
                        modifier = Modifier.fillMaxSize(), // modifier fully defined above
                        style = CommonStyles.textStyle,
                        onTextLayout = { layoutResult = it }
                    )
                }

                MySpacer()

                // Button that executes the script
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Button(
                        // Launching the script onClick
                        onClick = {
                            isRunning = true
                            output = ""
                            exitCode = null
                            errorMessage = null
                            scriptRunner.writeScriptToFile(script.text, scriptPath)

                            scope.launch(Dispatchers.IO) {
                                // Live output
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
                                } catch (e: Exception) {
                                    // Error handling
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