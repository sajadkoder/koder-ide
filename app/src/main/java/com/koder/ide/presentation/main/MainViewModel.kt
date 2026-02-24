package com.koder.ide.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.koder.ide.domain.model.EditorTab
import com.koder.ide.domain.model.GitStatus
import com.koder.ide.domain.model.ProjectFile
import com.koder.ide.domain.repository.FileRepository
import com.koder.ide.domain.repository.GitRepository
import com.koder.ide.domain.repository.TerminalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID
import javax.inject.Inject

data class MainUiState(
    val currentPath: String = "",
    val files: List<ProjectFile> = emptyList(),
    val tabs: List<EditorTab> = emptyList(),
    val currentTabIndex: Int = -1,
    val isSidebarOpen: Boolean = true,
    val isTerminalOpen: Boolean = false,
    val isGitPanelOpen: Boolean = false,
    val gitStatus: GitStatus? = null,
    val terminalOutput: List<String> = emptyList(),
    val isTerminalRunning: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val message: String? = null
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val fileRepository: FileRepository,
    private val gitRepository: GitRepository,
    private val terminalRepository: TerminalRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()
    
    init {
        loadExternalStorage()
    }
    
    fun loadExternalStorage() {
        val path = fileRepository.getExternalStoragePath()
        navigateTo(path)
    }
    
    fun navigateTo(path: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, currentPath = path)
            try {
                val files = fileRepository.getFiles(path)
                val gitStatus = if (gitRepository.isRepository(path)) {
                    gitRepository.getStatus(path)
                } else null
                
                _uiState.value = _uiState.value.copy(
                    files = files,
                    isLoading = false,
                    gitStatus = gitStatus
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
    
    fun navigateUp() {
        val currentPath = _uiState.value.currentPath
        val parent = File(currentPath).parentFile?.absolutePath
        if (parent != null) {
            navigateTo(parent)
        }
    }
    
    fun openFile(path: String) {
        viewModelScope.launch {
            val file = File(path)
            if (!file.exists() || file.isDirectory) return@launch
            
            val existingTab = _uiState.value.tabs.find { it.file.path == path }
            
            if (existingTab != null) {
                val index = _uiState.value.tabs.indexOf(existingTab)
                _uiState.value = _uiState.value.copy(currentTabIndex = index)
            } else {
                try {
                    val content = fileRepository.readFile(path)
                    val tab = EditorTab(
                        id = UUID.randomUUID().toString(),
                        file = ProjectFile.fromFile(file),
                        content = content,
                        isModified = false
                    )
                    _uiState.value = _uiState.value.copy(
                        tabs = _uiState.value.tabs + tab,
                        currentTabIndex = _uiState.value.tabs.size
                    )
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(error = "Failed to open file: ${e.message}")
                }
            }
        }
    }
    
    fun closeTab(index: Int) {
        val tabs = _uiState.value.tabs.toMutableList()
        if (index in tabs.indices) {
            tabs.removeAt(index)
            val currentIndex = _uiState.value.currentTabIndex
            val newIndex = when {
                tabs.isEmpty() -> -1
                currentIndex >= tabs.size -> tabs.size - 1
                currentIndex == index -> (currentIndex - 1).coerceAtLeast(0)
                else -> currentIndex
            }
            _uiState.value = _uiState.value.copy(tabs = tabs, currentTabIndex = newIndex)
        }
    }
    
    fun selectTab(index: Int) {
        if (index in _uiState.value.tabs.indices) {
            _uiState.value = _uiState.value.copy(currentTabIndex = index)
        }
    }
    
    fun updateTabContent(tabId: String, content: String) {
        val tabs = _uiState.value.tabs.map { tab ->
            if (tab.id == tabId) tab.copy(content = content, isModified = true) else tab
        }
        _uiState.value = _uiState.value.copy(tabs = tabs)
    }
    
    fun saveCurrentTab() {
        viewModelScope.launch {
            val state = _uiState.value
            val tabIndex = state.currentTabIndex
            if (tabIndex < 0 || tabIndex >= state.tabs.size) return@launch
            
            val tab = state.tabs[tabIndex]
            val success = fileRepository.writeFile(tab.file.path, tab.content)
            
            if (success) {
                val tabs = state.tabs.map { t ->
                    if (t.id == tab.id) t.copy(isModified = false) else t
                }
                _uiState.value = state.copy(tabs = tabs, message = "Saved")
            } else {
                _uiState.value = state.copy(error = "Failed to save file")
            }
        }
    }
    
    fun createFile(name: String, isDirectory: Boolean = false) {
        viewModelScope.launch {
            val currentPath = _uiState.value.currentPath
            val result = if (isDirectory) {
                fileRepository.createDirectory(currentPath, name)
            } else {
                fileRepository.createFile(currentPath, name)
            }
            
            if (result != null) {
                navigateTo(currentPath)
                _uiState.value = _uiState.value.copy(message = "Created: $name")
            } else {
                _uiState.value = _uiState.value.copy(error = "Failed to create")
            }
        }
    }
    
    fun deleteFile(path: String) {
        viewModelScope.launch {
            if (fileRepository.delete(path)) {
                navigateTo(_uiState.value.currentPath)
                _uiState.value = _uiState.value.copy(message = "Deleted")
            } else {
                _uiState.value = _uiState.value.copy(error = "Failed to delete")
            }
        }
    }
    
    fun toggleSidebar() {
        _uiState.value = _uiState.value.copy(isSidebarOpen = !_uiState.value.isSidebarOpen)
    }
    
    fun toggleTerminal() {
        val newState = !_uiState.value.isTerminalOpen
        _uiState.value = _uiState.value.copy(isTerminalOpen = newState)
        
        if (newState && !_uiState.value.isTerminalRunning) {
            startTerminal()
        }
    }
    
    private fun startTerminal() {
        viewModelScope.launch {
            val started = terminalRepository.startSession()
            if (started) {
                _uiState.value = _uiState.value.copy(
                    isTerminalRunning = true,
                    terminalOutput = listOf("Koder Terminal v1.0", "Type 'exit' to close", "")
                )
            }
        }
    }
    
    fun executeCommand(command: String) {
        viewModelScope.launch {
            val currentOutput = _uiState.value.terminalOutput
            val newOutput = currentOutput + listOf("\$$command")
            
            if (command == "exit") {
                terminalRepository.stopSession()
                _uiState.value = _uiState.value.copy(
                    terminalOutput = newOutput + listOf("Session closed"),
                    isTerminalRunning = false
                )
                return@launch
            }
            
            if (command == "clear") {
                _uiState.value = _uiState.value.copy(terminalOutput = listOf(""))
                return@launch
            }
            
            val result = terminalRepository.execute(command)
            val output = if (result.isError) {
                newOutput + result.text.lines()
            } else {
                newOutput + result.text.lines()
            }
            
            _uiState.value = _uiState.value.copy(terminalOutput = output)
        }
    }
    
    fun clearTerminal() {
        _uiState.value = _uiState.value.copy(terminalOutput = listOf(""))
    }
    
    fun toggleGitPanel() {
        _uiState.value = _uiState.value.copy(isGitPanelOpen = !_uiState.value.isGitPanelOpen)
    }
    
    fun refreshGitStatus() {
        viewModelScope.launch {
            val path = _uiState.value.currentPath
            if (gitRepository.isRepository(path)) {
                _uiState.value = _uiState.value.copy(
                    gitStatus = gitRepository.getStatus(path)
                )
            }
        }
    }
    
    fun gitInit() {
        viewModelScope.launch {
            val path = _uiState.value.currentPath
            val result = gitRepository.init(path)
            when (result) {
                is com.koder.ide.domain.model.GitResult.Success -> {
                    refreshGitStatus()
                    _uiState.value = _uiState.value.copy(message = result.message)
                }
                is com.koder.ide.domain.model.GitResult.Error -> {
                    _uiState.value = _uiState.value.copy(error = result.message)
                }
            }
        }
    }
    
    fun gitAdd(files: List<String> = emptyList<String>()) {
        viewModelScope.launch {
            val path = _uiState.value.currentPath
            val result = gitRepository.add(path, files)
            when (result) {
                is com.koder.ide.domain.model.GitResult.Success -> {
                    refreshGitStatus()
                    _uiState.value = _uiState.value.copy(message = result.message)
                }
                is com.koder.ide.domain.model.GitResult.Error -> {
                    _uiState.value = _uiState.value.copy(error = result.message)
                }
            }
        }
    }
    
    fun gitCommit(message: String) {
        viewModelScope.launch {
            val path = _uiState.value.currentPath
            val result = gitRepository.commit(path, message)
            when (result) {
                is com.koder.ide.domain.model.GitResult.Success -> {
                    refreshGitStatus()
                    _uiState.value = _uiState.value.copy(message = result.message)
                }
                is com.koder.ide.domain.model.GitResult.Error -> {
                    _uiState.value = _uiState.value.copy(error = result.message)
                }
            }
        }
    }
    
    fun gitPush() {
        viewModelScope.launch {
            val path = _uiState.value.currentPath
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = gitRepository.push(path)
            _uiState.value = _uiState.value.copy(isLoading = false)
            when (result) {
                is com.koder.ide.domain.model.GitResult.Success -> {
                    _uiState.value = _uiState.value.copy(message = result.message)
                }
                is com.koder.ide.domain.model.GitResult.Error -> {
                    _uiState.value = _uiState.value.copy(error = result.message)
                }
            }
        }
    }
    
    fun gitPull() {
        viewModelScope.launch {
            val path = _uiState.value.currentPath
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = gitRepository.pull(path)
            _uiState.value = _uiState.value.copy(isLoading = false)
            when (result) {
                is com.koder.ide.domain.model.GitResult.Success -> {
                    refreshGitStatus()
                    _uiState.value = _uiState.value.copy(message = result.message)
                }
                is com.koder.ide.domain.model.GitResult.Error -> {
                    _uiState.value = _uiState.value.copy(error = result.message)
                }
            }
        }
    }
    
    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null, error = null)
    }
    
    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            terminalRepository.stopSession()
        }
    }
}
