package org.example.util

import androidx.compose.ui.graphics.Color

object Utils {
    @JvmStatic
    fun parseColor(colorString: String): Color {
        val color = colorString.removePrefix("#")
        return when (color.length) {
            6 -> Color(
                color.substring(0, 2).toInt(16) / 255f,
                color.substring(2, 4).toInt(16) / 255f,
                color.substring(4, 6).toInt(16) / 255f
            )
            8 -> Color(
                color.substring(2, 4).toInt(16) / 255f,
                color.substring(4, 6).toInt(16) / 255f,
                color.substring(6, 8).toInt(16) / 255f,
                color.substring(0, 2).toInt(16) / 255f
            )
            else -> throw IllegalArgumentException("Invalid color format: $colorString")
        }
    }
}