package org.example.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import org.example.config.ConfigLoader

class OutputTransformation(
    private val errorColor: Color,
    private var linkColor: Color? = null
) : CustomVisualTransformation() {
    private val config = ConfigLoader.config
    private val errorPrefix = config.errorPrefix

    private val scriptPath = config.command.scriptPath
    private val escapedPath = Regex.escape(scriptPath)
    private val regex = Regex("""$escapedPath:(\d+)(?::(\d+))?""")

    init {
        linkColor = linkColor ?: Utils.parseColor(config.colors.link)
    }

    override fun highlight(text: String): Pair<AnnotatedString, OffsetMapping> {
        // Replace tabs with spaces and create an initial offset map
        val (transformed, offsetMapToOriginal) = replaceTabsWithSpaces(text)

        val annotated = buildAnnotatedString {
            val lines = transformed.lines()
            var currentOffset = 0
            for (line in lines) {
                // Highlight the errors
                if (line.startsWith(errorPrefix)) {
                    withStyle(SpanStyle(color = errorColor)) {
                        append(line)
                    }
                } else {
                    append(line)
                }

                // Highlight the script path
                regex.findAll(line).forEach { matchResult ->
                    val start = currentOffset + matchResult.range.first
                    val end = currentOffset + matchResult.range.last + 1
                    addStyle(SpanStyle(color = linkColor!!, textDecoration = TextDecoration.Underline), start, end)
                    addStringAnnotation("URL", "$scriptPath:${matchResult.groupValues[1]}", start + 1, end)
                }
                append("\n")
                currentOffset += line.length + 1
            }
        }

        val offsetMap = createOffsetMapping(offsetMapToOriginal, text.length)
        return annotated to offsetMap
    }

    fun getAnnotationAtOffset(output: String, offset: Int): Pair<Int, Int>? {
        val annotations = filter(AnnotatedString(output)).text.getStringAnnotations("URL", offset, offset)
        if (annotations.isNotEmpty()) {
            val annotation = annotations.first()
            val match = regex.find(annotation.item)
            val line = match?.groupValues?.get(1)?.toIntOrNull() ?: return null
            val position = match.groupValues.getOrNull(2)?.toIntOrNull() ?: 0
            return line - 1 to position
        }
        return null
    }
}