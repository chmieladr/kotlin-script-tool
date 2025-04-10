package org.example.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

abstract class CustomVisualTransformation : VisualTransformation {
    protected var i = 0

    override fun filter(text: AnnotatedString): TransformedText {
        val original = text.text
        val (transformedText, offsetMap) = highlight(original)
        return TransformedText(transformedText, offsetMap)
    }

    protected open fun highlight(text: String): Pair<AnnotatedString, OffsetMapping> {
        // To be overridden by subclasses
        return if (text.isEmpty()) AnnotatedString("") to OffsetMapping.Identity
        else AnnotatedString(text) to OffsetMapping.Identity
    }

    protected fun createOffsetMapping(offsetMapToOriginal: List<Int>, textLength: Int): OffsetMapping {
        return object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offsetMapToOriginal.isEmpty()) return 0
                return offsetMapToOriginal.indexOfFirst { it >= offset }.takeIf {
                    it != -1
                } ?: offsetMapToOriginal.size
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offsetMapToOriginal.isEmpty()) return 0
                return if (offset < offsetMapToOriginal.size) offsetMapToOriginal[offset] else textLength
            }
        }
    }

    protected fun replaceTabsWithSpaces(text: String): Pair<StringBuilder, List<Int>> {
        val transformed = StringBuilder()
        val offsetMapToOriginal = mutableListOf<Int>()
        var i = 0

        while (i < text.length) {
            val c = text[i]
            if (c == '\t') {
                repeat(4) {
                    transformed.append(' ')
                    offsetMapToOriginal.add(i)
                }
            } else {
                transformed.append(c)
                offsetMapToOriginal.add(i)
            }
            i++
        }

        return transformed to offsetMapToOriginal
    }
}