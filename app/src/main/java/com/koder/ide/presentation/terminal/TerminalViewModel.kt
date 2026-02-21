package com.koder.ide.presentation.terminal

import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.koder.ide.core.util.CommandResult
import com.koder.ide.core.util.SecurityUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class TerminalViewModel @Inject constructor(
) : ViewModel() {

    private val _uiState = MutableStateFlow(TerminalUiState())
    val uiState: StateFlow<TerminalUiState> = _uiState.asStateFlow()

    private var currentJob: Job? = null
    private var lineIdCounter = 0L

    fun updateCommand(command: String) {
        _uiState.value = _uiState.value.copy(commandInput = command)
    }

    fun executeCommand() {
        val command = _uiState.value.commandInput.trim()
        if (command.isEmpty()) return

        val inputLine = TerminalLine(
            id = lineIdCounter++,
            content = "${_uiState.value.currentDirectory} $ ${command}",
            type = LineType.INPUT
        )

        _uiState.value = _uiState.value.copy(
            outputLines = _uiState.value.outputLines + inputLine,
            commandInput = "",
            isRunning = true
        )

        currentJob = viewModelScope.launch {
            when {
                command == "clear" -> {
                    _uiState.value = _uiState.value.copy(
                        outputLines = emptyList(),
                        isRunning = false
                    )
                }
                command == "pwd" -> {
                    addOutput(_uiState.value.currentDirectory)
                }
                command.startsWith("cd ") -> {
                    val path = command.removePrefix("cd ").trim()
                    changeDirectory(path)
                }
                command == "ls" || command == "ls -la" || command == "ls -l" -> {
                    listDirectory(command.contains("-a") || command.contains("-la"))
                }
                command.startsWith("mkdir ") -> {
                    createDirectory(command.removePrefix("mkdir ").trim())
                }
                command.startsWith("touch ") -> {
                    createFile(command.removePrefix("touch ").trim())
                }
                command.startsWith("rm ") -> {
                    removeFile(command.removePrefix("rm ").trim())
                }
                command.startsWith("cat ") -> {
                    catFile(command.removePrefix("cat ").trim())
                }
                command.startsWith("echo ") -> {
                    addOutput(command.removePrefix("echo ").trim())
                }
                command == "help" -> {
                    showHelp()
                }
                else -> {
                    executeSystemCommand(command)
                }
            }
            
            _uiState.value = _uiState.value.copy(isRunning = false)
        }
    }

    private fun addOutput(content: String, type: LineType = LineType.OUTPUT) {
        val line = TerminalLine(
            id = lineIdCounter++,
            content = content,
            type = type
        )
        _uiState.value = _uiState.value.copy(
            outputLines = _uiState.value.outputLines + line
        )
    }

    private fun changeDirectory(path: String) {
        val currentDir = File(_uiState.value.currentDirectory)
        val newDir = if (path.startsWith("/")) {
            File(path)
        } else if (path == "..") {
            currentDir.parentFile ?: currentDir
        } else if (path == "~") {
            File(Environment.getExternalStorageDirectory().absolutePath)
        } else {
            File(currentDir, path)
        }

        if (newDir.exists() && newDir.isDirectory) {
            _uiState.value = _uiState.value.copy(
                currentDirectory = newDir.absolutePath
            )
            addOutput("Changed directory to ${newDir.absolutePath}")
        } else {
            addOutput("cd: no such file or directory: $path", LineType.ERROR)
        }
    }

    private fun listDirectory(showHidden: Boolean) {
        val currentDir = File(_uiState.value.currentDirectory)
        val files = currentDir.listFiles()
        
        if (files == null) {
            addOutput("ls: cannot access directory", LineType.ERROR)
            return
        }

        val output = files
            .filter { showHidden || !it.name.startsWith(".") }
            .sortedWith(compareBy<File> { !it.isDirectory }.thenBy { it.name.lowercase() })
            .joinToString("\n") { file ->
                val prefix = if (file.isDirectory) "d" else "-"
                val size = if (file.isDirectory) "-" else formatSize(file.length())
                "$prefix ${file.name} $size"
            }
        
        addOutput(output)
    }

    private fun createDirectory(name: String) {
        val dir = File(_uiState.value.currentDirectory, name)
        if (dir.mkdirs()) {
            addOutput("Created directory: $name")
        } else {
            addOutput("mkdir: cannot create directory '$name'", LineType.ERROR)
        }
    }

    private fun createFile(name: String) {
        val file = File(_uiState.value.currentDirectory, name)
        try {
            if (file.createNewFile()) {
                addOutput("Created file: $name")
            } else {
                addOutput("touch: file already exists: $name", LineType.ERROR)
            }
        } catch (e: Exception) {
            addOutput("touch: cannot create file '$name': ${e.message}", LineType.ERROR)
        }
    }

    private fun removeFile(name: String) {
        val file = File(_uiState.value.currentDirectory, name)
        if (!file.exists()) {
            addOutput("rm: cannot remove '$name': No such file or directory", LineType.ERROR)
            return
        }
        
        val success = if (file.isDirectory) file.deleteRecursively() else file.delete()
        if (success) {
            addOutput("Removed: $name")
        } else {
            addOutput("rm: cannot remove '$name'", LineType.ERROR)
        }
    }

    private fun catFile(name: String) {
        val file = File(_uiState.value.currentDirectory, name)
        if (!file.exists()) {
            addOutput("cat: $name: No such file or directory", LineType.ERROR)
            return
        }
        if (file.isDirectory) {
            addOutput("cat: $name: Is a directory", LineType.ERROR)
            return
        }
        
        try {
            val content = file.readText()
            if (content.isNotEmpty()) {
                content.lines().forEach { line ->
                    addOutput(line)
                }
            }
        } catch (e: Exception) {
            addOutput("cat: $name: ${e.message}", LineType.ERROR)
        }
    }

    private suspend fun executeSystemCommand(command: String) {
        withContext(Dispatchers.IO) {
            try {
                val parts = command.split(" ")
                val result = SecurityUtils.executeCommand(
                    command = parts,
                    workingDir = File(_uiState.value.currentDirectory),
                    timeoutMs = 30000
                )
                
                if (result.output.isNotEmpty()) {
                    result.output.lines().forEach { line ->
                        if (line.isNotBlank()) {
                            addOutput(line)
                        }
                    }
                }
                
                if (result.error != null) {
                    addOutput("Error: ${result.error}", LineType.ERROR)
                }
            } catch (e: Exception) {
                addOutput("Command not found: ${parts.first()}", LineType.ERROR)
            }
        }
    }

    private fun showHelp() {
        val help = """
            Available commands:
            help          - Show this help
            clear         - Clear terminal
            pwd           - Print working directory
            cd <dir>      - Change directory
            ls            - List files
            mkdir <name>  - Create directory
            touch <name>  - Create file
            rm <name>     - Remove file/directory
            cat <file>    - Display file contents
            echo <text>   - Print text
        """.trimIndent()
        
        help.lines().forEach { addOutput(it, LineType.SYSTEM) }
    }

    fun stopProcess() {
        currentJob?.cancel()
        _uiState.value = _uiState.value.copy(isRunning = false)
        addOutput("^C", LineType.SYSTEM)
    }

    private fun formatSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> String.format("%.1f KB", bytes / 1024.0)
            else -> String.format("%.1f MB", bytes / (1024.0 * 1024))
        }
    }
}

data class TerminalUiState(
    val currentDirectory: String = Environment.getExternalStorageDirectory().absolutePath,
    val commandInput: String = "",
    val outputLines: List<TerminalLine> = emptyList(),
    val isRunning: Boolean = false
)
