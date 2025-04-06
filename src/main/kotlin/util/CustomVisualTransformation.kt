package org.example.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

interface CustomVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val original = text.text
        val (transformedText, offsetMap) = highlight(original)
        return TransformedText(transformedText, offsetMap)
    }

    fun highlight(text: String): Pair<AnnotatedString, OffsetMapping> {
        // To be overridden by subclasses
        return AnnotatedString(text) to OffsetMapping.Identity
    }

    fun createOffsetMapping(offsetMapToOriginal: List<Int>, textLength: Int): OffsetMapping {
        return object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                return offsetMapToOriginal.indexOfFirst { it >= offset }.takeIf { it != -1 } ?: offsetMapToOriginal.size
            }

            override fun transformedToOriginal(offset: Int): Int {
                return if (offset < offsetMapToOriginal.size) offsetMapToOriginal[offset] else textLength
            }
        }
    }
}