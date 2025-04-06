package org.example

import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.example.ui.ScriptEditorView
import org.jetbrains.skia.Image
import java.awt.Dimension

class Main {
    private val title = "Kotlin Script Tool"
    private val iconPath = "/drawable/icon.png"
    private val theme = "dark"

    fun start() = application {
        Window(onCloseRequest = ::exitApplication,
            title = title,
            icon = getIcon(),
            resizable = true
        ) {
            window.minimumSize = Dimension(640, 360)
            ScriptEditorView(theme).render()
        }
    }

    private fun getIcon(): BitmapPainter {
        val iconBytes = Main::class.java.getResource(iconPath)!!.readBytes()
        val skiaImage = Image.makeFromEncoded(iconBytes)
        val imageBitmap = skiaImage.toComposeImageBitmap()
        val iconPainter = BitmapPainter(imageBitmap)
        return iconPainter
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            Main().start()
        }
    }
}