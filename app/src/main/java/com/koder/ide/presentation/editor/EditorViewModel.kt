package com.koder.ide.presentation.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.koder.ide.core.util.FileUtils
import com.koder.ide.domain.repository.EditorRepository
import com.koder.ide.domain.repository.FileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class EditorViewModel @Inject constructor(
    private val editorRepository: EditorRepository,
    private val fileRepository: FileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    private var currentFilePath: String? = null

    fun loadFile(path: String) {
        viewModelScope.launch {
            currentFilePath = path
            val file = File(path)
            
            editorRepository.loadFile(file).fold(
                onSuccess = { content ->
                    val language = FileUtils.getLanguageFromFile(file)
                    val fileSize = FileUtils.formatFileSize(file.length())
                    
                    _uiState.value = _uiState.value.copy(
                        content = content,
                        language = language,
                        encoding = "UTF-8",
                        fileSize = fileSize,
                        isLoading = false,
                        error = null
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
            )
        }
    }

    fun updateContent(content: String) {
        _uiState.value = _uiState.value.copy(
            content = content,
            isModified = true
        )
        
        val lines = content.lines()
        _uiState.value = _uiState.value.copy(
            totalLines = lines.size
        )
    }

    fun saveFile() {
        val path = currentFilePath ?: return
        viewModelScope.launch {
            editorRepository.saveFile(File(path), _uiState.value.content).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isModified = false)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(error = error.message)
                }
            )
        }
    }

    fun createNewFile() {
        currentFilePath = null
        _uiState.value = EditorUiState(
            content = "",
            language = "Plain Text",
            encoding = "UTF-8",
            fileSize = "0 B",
            isLoading = false,
            isModified = false
        )
    }

    fun undo() {
    }

    fun redo() {
    }

    fun formatCode() {
    }

    fun goToLine(lineNumber: Int) {
    }

    fun find(query: String) {
    }

    fun replace(find: String, replace: String) {
    }
}

data class EditorUiState(
    val content: String = "",
    val language: String = "Plain Text",
    val encoding: String = "UTF-8",
    val lineEnding: String = "LF",
    val fileSize: String = "0 B",
    val cursorLine: Int = 1,
    val cursorColumn: Int = 1,
    val totalLines: Int = 0,
    val selectionStart: Int = 0,
    val selectionEnd: Int = 0,
    val scrollPosition: Int = 0,
    val isModified: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null
)
