package org.example.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.withStyle
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.example.config.ConfigLoader
import java.io.File

class ScriptTransformation(
    private val defaultColor: Color,
    private var stringColor: Color? = null,
    private var commentColor: Color? = null
) : CustomVisualTransformation() {
    private val config = ConfigLoader.config
    private val keywords = mutableListOf<Keyword>()

    init {
        stringColor = stringColor ?: Utils.parseColor(config.colors.string)
        commentColor = commentColor ?: Utils.parseColor(config.colors.comment)
    }

    fun loadKeywordsFromJson(filePath: String) {
        val json = File(filePath).readText()
        val type = object : TypeToken<List<Keyword>>() {}.type
        val loadedKeywords: List<Keyword> = Gson().fromJson(json, type)
        keywords.clear()
        keywords.addAll(loadedKeywords)
    }

    override fun highlight(text: String): Pair<AnnotatedString, OffsetMapping> {
        var inString = false
        var inComment = false

        // Replace tabs with spaces and create an initial offset map
        val (transformed, offsetMapToOriginal) = replaceTabsWithSpaces(text)

        val annotated = buildAnnotatedString {
            i = 0
            while (i < transformed.length) {
                val c = transformed[i]

                // Basic string and comment detection to avoid highlighting keywords inside them
                if ((c == '"' && !inComment)) {
                    inString = !inString
                    withStyle(SpanStyle(color = stringColor!!)) {
                        append(c)
                    }
                    i++
                } else if (c == '/' && i + 1 < transformed.length && transformed[i + 1] == '/' && !inString) {
                    inComment = true
                    withStyle(SpanStyle(color = commentColor!!)) {
                        append(c)
                    }
                    i++
                } else if (c == '\n') {
                    inComment = false
                    append(c)
                    i++
                } else if (!inString && !inComment && c.isLetterOrDigit()) {
                    val start = i
                    while (i < transformed.length && transformed[i].isLetterOrDigit()) {
                        i++
                    }
                    val word = transformed.substring(start, i)
                    val originalStart = offsetMapToOriginal[start]
                    val originalWord = text.substring(
                        originalStart,
                        originalStart + word.length.coerceAtMost(text.length - originalStart)
                    )
                    val keyword = keywords.find { it.word == originalWord }
                    val color = keyword?.let { Utils.parseColor(it.color) } ?: defaultColor
                    withStyle(SpanStyle(color = color)) {
                        append(word)
                    }
                } else {
                    val color = when {
                        inString -> stringColor
                        inComment -> commentColor
                        else -> defaultColor
                    }
                    withStyle(SpanStyle(color = color!!)) {
                        append(c)
                    }
                    i++
                }
            }
        }

        val offsetMap = createOffsetMapping(offsetMapToOriginal, text.length)
        return annotated to offsetMap
    }
}
