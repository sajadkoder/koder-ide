package com.koder.ide.presentation.main

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

data class EditorTab(
    val path: String,
    val name: String,
    var isModified: Boolean = false
)

data class MainState(
    val projectPath: String? = null,
    val tabs: List<EditorTab> = emptyList(),
    val currentTabIndex: Int = -1,
    val isSidebarOpen: Boolean = true,
    val isTerminalOpen: Boolean = false,
    val sidebarWidth: Int = 280,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(MainState())
    val state: StateFlow<MainState> = _state.asStateFlow()

    fun loadProject(path: String? = null) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                projectPath = path ?: android.os.Environment.getExternalStorageDirectory().absolutePath,
                isLoading = false
            )
        }
    }

    fun openFile(path: String) {
        viewModelScope.launch {
            val exists = withContext(Dispatchers.IO) {
                path.isNotBlank() && File(path).exists()
            }
            
            if (exists) {
                val file = File(path)
                val tabs = _state.value.tabs
                val existingIndex = tabs.indexOfFirst { it.path == path }
                
                if (existingIndex >= 0) {
                    _state.value = _state.value.copy(currentTabIndex = existingIndex)
                } else {
                    val newTab = EditorTab(path = path, name = file.name)
                    _state.value = _state.value.copy(
                        tabs = tabs + newTab,
                        currentTabIndex = tabs.size
                    )
                }
            }
        }
    }

    fun openFileFromUri(uri: Uri, context: Context) {
        viewModelScope.launch {
            val path = withContext(Dispatchers.IO) {
                when (uri.scheme) {
                    "file" -> uri.path
                    "content" -> {
                        val cursor = context.contentResolver.query(uri, null, null, null, null)
                        cursor?.use {
                            if (it.moveToFirst()) {
                                val nameIndex = it.getColumnIndex("_display_name")
                                if (nameIndex >= 0) {
                                    val name = it.getString(nameIndex)
                                    File(context.cacheDir, name).apply {
                                        context.contentResolver.openInputStream(uri)?.use { input ->
                                            outputStream().use { output ->
                                                input.copyTo(output)
                                            }
                                        }
                                    }.absolutePath
                                } else null
                            } else null
                        }
                    }
                    else -> uri.path
                }
            }
            path?.let { openFile(it) }
        }
    }

    fun closeTab(index: Int) {
        val tabs = _state.value.tabs.toMutableList()
        if (index in tabs.indices) {
            tabs.removeAt(index)
            val currentIndex = _state.value.currentTabIndex
            val newIndex = when {
                tabs.isEmpty() -> -1
                currentIndex >= tabs.size -> tabs.size - 1
                currentIndex == index -> (currentIndex - 1).coerceAtLeast(0)
                else -> currentIndex
            }
            _state.value = _state.value.copy(tabs = tabs, currentTabIndex = newIndex)
        }
    }

    fun closeTab(path: String) {
        val index = _state.value.tabs.indexOfFirst { it.path == path }
        if (index >= 0) closeTab(index)
    }

    fun setCurrentTab(index: Int) {
        if (index in _state.value.tabs.indices) {
            _state.value = _state.value.copy(currentTabIndex = index)
        }
    }

    fun updateTabModified(path: String, modified: Boolean) {
        val tabs = _state.value.tabs.map { tab ->
            if (tab.path == path) tab.copy(isModified = modified) else tab
        }
        _state.value = _state.value.copy(tabs = tabs)
    }

    fun toggleSidebar() {
        _state.value = _state.value.copy(isSidebarOpen = !_state.value.isSidebarOpen)
    }

    fun toggleTerminal() {
        _state.value = _state.value.copy(isTerminalOpen = !_state.value.isTerminalOpen)
    }

    fun getCurrentFile(): File? {
        val state = _state.value
        return if (state.currentTabIndex >= 0 && state.currentTabIndex < state.tabs.size) {
            File(state.tabs[state.currentTabIndex].path).takeIf { it.exists() }
        } else null
    }
}
