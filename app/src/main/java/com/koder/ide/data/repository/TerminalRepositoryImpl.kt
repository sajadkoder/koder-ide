package com.koder.ide.data.repository

import com.koder.ide.domain.model.TerminalOutput
import com.koder.ide.domain.repository.TerminalRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import javax.inject.Inject

class TerminalRepositoryImpl @Inject constructor() : TerminalRepository {

    private var process: Process? = null
    private var outputWriter: OutputStreamWriter? = null
    private var inputReader: BufferedReader? = null
    private var errorReader: BufferedReader? = null
    private var isRunning = false

    override suspend fun startSession(): Boolean = withContext(Dispatchers.IO) {
        try {
            process = Runtime.getRuntime().exec("/system/bin/sh")
            outputWriter = OutputStreamWriter(process!!.outputStream)
            inputReader = BufferedReader(InputStreamReader(process!!.inputStream))
            errorReader = BufferedReader(InputStreamReader(process!!.errorStream))
            isRunning = true
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override suspend fun execute(command: String): TerminalOutput = withContext(Dispatchers.IO) {
        try {
            if (!isRunning) {
                startSession()
            }

            outputWriter?.apply {
                write("$command\n")
                flush()
            }

            val output = StringBuilder()
            val errorOutput = StringBuilder()

            // Read stdout
            while (inputReader?.ready() == true) {
                inputReader?.readLine()?.let { output.append(it).append("\n") }
            }

            // Read stderr
            while (errorReader?.ready() == true) {
                errorReader?.readLine()?.let { errorOutput.append(it).append("\n") }
            }

            TerminalOutput(
                text = output.toString().ifEmpty { errorOutput.toString() },
                isError = errorOutput.isNotEmpty()
            )
        } catch (e: Exception) {
            TerminalOutput(text = "Error: ${e.message}", isError = true)
        }
    }

    override suspend fun stopSession() = withContext(Dispatchers.IO) {
        try {
            outputWriter?.write("exit\n")
            outputWriter?.flush()
            process?.destroy()
            outputWriter?.close()
            inputReader?.close()
            errorReader?.close()
            isRunning = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun isSessionRunning(): Boolean = isRunning
}
