package com.koder.ide.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.koder.ide.domain.model.EditorTheme
import com.koder.ide.domain.model.ThemeMode
import com.koder.ide.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            _uiState.value = SettingsUiState(
                themeMode = settingsRepository.getThemeMode().first(),
                editorTheme = settingsRepository.getEditorTheme().first(),
                fontSize = settingsRepository.getFontSize().first(),
                fontFamily = settingsRepository.getFontFamily().first(),
                tabSize = settingsRepository.getTabSize().first(),
                wordWrap = settingsRepository.isWordWrapEnabled().first(),
                lineNumbers = settingsRepository.isLineNumbersEnabled().first(),
                minimap = settingsRepository.isMinimapEnabled().first(),
                autoSave = settingsRepository.isAutoSaveEnabled().first(),
                autoComplete = settingsRepository.isAutoCompleteEnabled().first(),
                syntaxHighlighting = settingsRepository.isSyntaxHighlightingEnabled().first(),
                bracketMatching = settingsRepository.isBracketMatchingEnabled().first(),
                indentGuides = settingsRepository.isIndentGuidesEnabled().first(),
                currentLineHighlight = settingsRepository.isCurrentLineHighlightEnabled().first(),
                showHiddenFiles = settingsRepository.isShowHiddenFilesEnabled().first(),
                confirmBeforeDelete = settingsRepository.isConfirmBeforeDeleteEnabled().first(),
                vibration = settingsRepository.isVibrationEnabled().first()
            )
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            settingsRepository.setThemeMode(mode)
            _uiState.value = _uiState.value.copy(themeMode = mode)
        }
    }

    fun setEditorTheme(theme: EditorTheme) {
        viewModelScope.launch {
            settingsRepository.setEditorTheme(theme)
            _uiState.value = _uiState.value.copy(editorTheme = theme)
        }
    }

    fun setFontSize(size: Int) {
        viewModelScope.launch {
            settingsRepository.setFontSize(size)
            _uiState.value = _uiState.value.copy(fontSize = size)
        }
    }

    fun setFontFamily(family: String) {
        viewModelScope.launch {
            settingsRepository.setFontFamily(family)
            _uiState.value = _uiState.value.copy(fontFamily = family)
        }
    }

    fun setTabSize(size: Int) {
        viewModelScope.launch {
            settingsRepository.setTabSize(size)
            _uiState.value = _uiState.value.copy(tabSize = size)
        }
    }

    fun setWordWrap(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setWordWrapEnabled(enabled)
            _uiState.value = _uiState.value.copy(wordWrap = enabled)
        }
    }

    fun setLineNumbers(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setLineNumbersEnabled(enabled)
            _uiState.value = _uiState.value.copy(lineNumbers = enabled)
        }
    }

    fun setMinimap(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setMinimapEnabled(enabled)
            _uiState.value = _uiState.value.copy(minimap = enabled)
        }
    }

    fun setAutoSave(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setAutoSaveEnabled(enabled)
            _uiState.value = _uiState.value.copy(autoSave = enabled)
        }
    }

    fun setAutoComplete(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setAutoCompleteEnabled(enabled)
            _uiState.value = _uiState.value.copy(autoComplete = enabled)
        }
    }

    fun setSyntaxHighlighting(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setSyntaxHighlightingEnabled(enabled)
            _uiState.value = _uiState.value.copy(syntaxHighlighting = enabled)
        }
    }

    fun setBracketMatching(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setBracketMatchingEnabled(enabled)
            _uiState.value = _uiState.value.copy(bracketMatching = enabled)
        }
    }

    fun setIndentGuides(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setIndentGuidesEnabled(enabled)
            _uiState.value = _uiState.value.copy(indentGuides = enabled)
        }
    }

    fun setCurrentLineHighlight(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setCurrentLineHighlightEnabled(enabled)
            _uiState.value = _uiState.value.copy(currentLineHighlight = enabled)
        }
    }

    fun setShowHiddenFiles(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setShowHiddenFilesEnabled(enabled)
            _uiState.value = _uiState.value.copy(showHiddenFiles = enabled)
        }
    }

    fun setConfirmBeforeDelete(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setConfirmBeforeDeleteEnabled(enabled)
            _uiState.value = _uiState.value.copy(confirmBeforeDelete = enabled)
        }
    }

    fun setVibration(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setVibrationEnabled(enabled)
            _uiState.value = _uiState.value.copy(vibration = enabled)
        }
    }

    fun showThemeDialog() {
    }

    fun showEditorThemeDialog() {
    }

    fun showFontDialog() {
    }
}

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val editorTheme: EditorTheme = EditorTheme.MONOKAI,
    val fontSize: Int = 14,
    val fontFamily: String = "JetBrains Mono",
    val tabSize: Int = 4,
    val wordWrap: Boolean = false,
    val lineNumbers: Boolean = true,
    val minimap: Boolean = true,
    val autoSave: Boolean = true,
    val autoComplete: Boolean = true,
    val syntaxHighlighting: Boolean = true,
    val bracketMatching: Boolean = true,
    val indentGuides: Boolean = true,
    val currentLineHighlight: Boolean = true,
    val showHiddenFiles: Boolean = false,
    val confirmBeforeDelete: Boolean = true,
    val vibration: Boolean = true,
    val shell: String = "/system/bin/sh"
)
