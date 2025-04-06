package org.example.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle

class OutputTransformation(private val errorColor: Color) :
    CustomVisualTransformation {
    var errorPrefix: String? = null

    override fun highlight(text: String): Pair<AnnotatedString, OffsetMapping> {
        val offsetMapToOriginal = mutableListOf<Int>()
        val annotated = buildAnnotatedString {
            val lines = text.lines()
            var currentOffset = 0
            for (line in lines) {
                if (errorPrefix != null && line.startsWith(errorPrefix!!)) {
                    withStyle(SpanStyle(color = errorColor, textDecoration = TextDecoration.Underline)) {
                        append(line)
                    }
                    addStringAnnotation(
                        tag = "ERROR",
                        annotation = line,
                        start = currentOffset,
                        end = currentOffset + line.length
                    )
                } else {
                    append(line)
                }
                append("\n")
                repeat(line.length + 1) {
                    offsetMapToOriginal.add(currentOffset++)
                }
            }
        }

        val offsetMap = createOffsetMapping(offsetMapToOriginal, text.length)
        return annotated to offsetMap
    }
}