package org.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.config.Config
import org.example.config.Theme
import org.example.util.Utils
import kotlin.properties.Delegates

object CommonStyles {
    private var fontSize: Int by Delegates.notNull()
    private var containerColor: Color by Delegates.notNull()
    private var textColor: Color by Delegates.notNull()

    fun init(config: Config, themeAssets: Theme) {
        fontSize = config.fontSize

        containerColor = Utils.parseColor(themeAssets.container)
        textColor = Utils.parseColor(themeAssets.text)
    }

    val textFieldModifier: Modifier
        get() = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(containerColor)

    val textStyle: TextStyle
        get() = TextStyle(
            color = textColor,
            fontSize = fontSize.sp,
            fontFamily = FontFamily.Monospace
        )

    @JvmStatic
    @Suppress("FunctionName")
    @Composable
    fun MySpacer() {
        Spacer(modifier = Modifier.height(8.dp))
    }
}