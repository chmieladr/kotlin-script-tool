package org.example.config

import com.google.gson.Gson
import java.io.File

object ConfigLoader {
    private const val JSON = "src/main/resources/files/config.json"
    val config: Config by lazy {
        val configFile = File(JSON)
        val configJson = configFile.readText()
        Gson().fromJson(configJson, Config::class.java)
    }
}