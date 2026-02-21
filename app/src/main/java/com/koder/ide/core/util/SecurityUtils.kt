package com.koder.ide.core.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.security.MessageDigest

object SecurityUtils {

    fun calculateMD5(file: File): String {
        val md = MessageDigest.getInstance("MD5")
        val inputStream = FileInputStream(file)
        val buffer = ByteArray(8192)
        var bytesRead: Int
        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
            md.update(buffer, 0, bytesRead)
        }
        inputStream.close()
        return md.digest().joinToString("") { "%02x".format(it) }
    }

    fun calculateSHA256(file: File): String {
        val md = MessageDigest.getInstance("SHA-256")
        val inputStream = FileInputStream(file)
        val buffer = ByteArray(8192)
        var bytesRead: Int
        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
            md.update(buffer, 0, bytesRead)
        }
        inputStream.close()
        return md.digest().joinToString("") { "%02x".format(it) }
    }

    suspend fun executeCommand(
        command: List<String>,
        workingDir: File? = null,
        timeoutMs: Long = 30000
    ): CommandResult = withContext(Dispatchers.IO) {
        try {
            val processBuilder = ProcessBuilder(command)
            workingDir?.let { processBuilder.directory(it) }
            processBuilder.redirectErrorStream(true)
            
            val process = processBuilder.start()
            val output = StringBuilder()
            
            BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    output.append(line).append("\n")
                }
            }
            
            val completed = process.waitFor(timeoutMs, java.util.concurrent.TimeUnit.MILLISECONDS)
            
            if (!completed) {
                process.destroyForcibly()
                CommandResult(
                    exitCode = -1,
                    output = output.toString(),
                    error = "Command timed out"
                )
            } else {
                CommandResult(
                    exitCode = process.exitValue(),
                    output = output.toString(),
                    error = null
                )
            }
        } catch (e: Exception) {
            CommandResult(
                exitCode = -1,
                output = "",
                error = e.message ?: "Unknown error"
            )
        }
    }
}

data class CommandResult(
    val exitCode: Int,
    val output: String,
    val error: String?
) {
    val isSuccess: Boolean get() = exitCode == 0
}
