package org.example.controller

import org.example.config.ConfigLoader
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader

class ScriptRunner {
    private val config = ConfigLoader.config
    private val executable = config.command.executable
    private val scriptPath = config.command.scriptPath
    private val command = "$executable -script $scriptPath"

    fun writeScriptToFile(script: String, filePath: String) {
        File(filePath).writeText(script)
    }

    @Throws(IOException::class)
    fun runScript(outputHandler: (String) -> Unit, errorHandler: (String) -> Unit): Int {
        val processBuilder = ProcessBuilder(command.split(" "))
        val process = processBuilder.start()

        val outputThread = Thread {
            BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                reader.lines().forEach { line ->
                    outputHandler(line)
                }
            }
        }

        val errorThread = Thread {
            BufferedReader(InputStreamReader(process.errorStream)).use { reader ->
                reader.lines().forEach { line ->
                    errorHandler(line)
                }
            }
        }

        outputThread.start()
        errorThread.start()

        val exitCode = process.waitFor()

        outputThread.join()
        errorThread.join()

        return exitCode
    }
}
