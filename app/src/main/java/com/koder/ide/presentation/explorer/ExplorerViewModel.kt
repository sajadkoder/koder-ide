package com.koder.ide.presentation.explorer

import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.koder.ide.domain.model.FileNode
import com.koder.ide.domain.repository.FileRepository
import com.koder.ide.domain.repository.ProjectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ExplorerViewModel @Inject constructor(
    private val fileRepository: FileRepository,
    private val projectRepository: ProjectRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExplorerUiState())
    val uiState: StateFlow<ExplorerUiState> = _uiState.asStateFlow()

    init {
        loadRootDirectory()
    }

    private fun loadRootDirectory() {
        viewModelScope.launch {
            val rootPath = Environment.getExternalStorageDirectory().absolutePath
            loadDirectory(rootPath)
        }
    }

    fun loadDirectory(path: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val directory = File(path)
            if (directory.exists() && directory.isDirectory) {
                val files = directory.listFiles()
                    ?.sortedWith(compareBy<File> { !it.isDirectory }.thenBy { it.name.lowercase() })
                    ?.map { file ->
                        FileNode(
                            file = file,
                            name = file.name,
                            isDirectory = file.isDirectory,
                            depth = 0
                        )
                    } ?: emptyList()

                _uiState.value = _uiState.value.copy(
                    currentPath = path,
                    files = files,
                    isLoading = false
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Directory not found: $path"
                )
            }
        }
    }

    fun toggleExpand(node: FileNode) {
        viewModelScope.launch {
            val currentFiles = _uiState.value.files.toMutableList()
            val index = currentFiles.indexOfFirst { it.path == node.path }
            
            if (index != -1) {
                val updatedNode = if (node.isExpanded) {
                    node.copy(
                        isExpanded = false,
                        children = mutableListOf()
                    )
                } else {
                    val children = node.file.listFiles()
                        ?.sortedWith(compareBy<File> { !it.isDirectory }.thenBy { it.name.lowercase() })
                        ?.map { file ->
                            FileNode(
                                file = file,
                                name = file.name,
                                isDirectory = file.isDirectory,
                                depth = node.depth + 1
                            )
                        }?.toMutableList() ?: mutableListOf()
                    
                    node.copy(
                        isExpanded = true,
                        children = children
                    )
                }
                
                currentFiles[index] = updatedNode
                _uiState.value = _uiState.value.copy(files = currentFiles)
            }
        }
    }

    fun createFile(name: String, isDirectory: Boolean) {
        viewModelScope.launch {
            val currentPath = _uiState.value.currentPath
            val parent = File(currentPath)
            
            projectRepository.createFile(parent, name, isDirectory).fold(
                onSuccess = {
                    refresh()
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(error = error.message)
                }
            )
        }
    }

    fun deleteFile(path: String) {
        viewModelScope.launch {
            projectRepository.deleteFile(File(path)).fold(
                onSuccess = {
                    refresh()
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(error = error.message)
                }
            )
        }
    }

    fun renameFile(path: String, newName: String) {
        viewModelScope.launch {
            projectRepository.renameFile(File(path), newName).fold(
                onSuccess = {
                    refresh()
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(error = error.message)
                }
            )
        }
    }

    fun refresh() {
        loadDirectory(_uiState.value.currentPath)
    }

    fun navigateUp() {
        val currentPath = _uiState.value.currentPath
        val parent = File(currentPath).parentFile
        if (parent != null && parent.canRead()) {
            loadDirectory(parent.absolutePath)
        }
    }
}

data class ExplorerUiState(
    val currentPath: String = "",
    val files: List<FileNode> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
