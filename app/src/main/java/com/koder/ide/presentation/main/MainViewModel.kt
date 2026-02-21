package com.koder.ide.presentation.main

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.koder.ide.domain.model.ThemeMode
import com.koder.ide.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = settingsRepository.getThemeMode()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeMode.SYSTEM)

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val _currentFilePath = MutableStateFlow<String?>(null)
    val currentFilePath: StateFlow<String?> = _currentFilePath.asStateFlow()

    fun openFileFromUri(uri: Uri) {
        viewModelScope.launch {
            _currentFilePath.value = uri.path
        }
    }

    fun createNewFile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(showNewFileDialog = true)
        }
    }

    fun showFilePicker() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(showFilePicker = true)
        }
    }

    fun openTerminal() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(showTerminal = true)
        }
    }

    fun hideDialogs() {
        _uiState.value = _uiState.value.copy(
            showNewFileDialog = false,
            showFilePicker = false
        )
    }

    fun openProject(path: String) {
        viewModelScope.launch {
            _currentFilePath.value = path
        }
    }

    fun openFile(path: String) {
        viewModelScope.launch {
            _currentFilePath.value = path
        }
    }

    fun toggleSidebar() {
        _uiState.value = _uiState.value.copy(
            isSidebarOpen = !_uiState.value.isSidebarOpen
        )
    }

    fun toggleBottomPanel() {
        _uiState.value = _uiState.value.copy(
            isBottomPanelOpen = !_uiState.value.isBottomPanelOpen
        )
    }

    fun setBottomPanelTab(tab: BottomPanelTab) {
        _uiState.value = _uiState.value.copy(
            bottomPanelTab = tab,
            isBottomPanelOpen = true
        )
    }
}

data class MainUiState(
    val showNewFileDialog: Boolean = false,
    val showFilePicker: Boolean = false,
    val showTerminal: Boolean = false,
    val isSidebarOpen: Boolean = true,
    val isBottomPanelOpen: Boolean = false,
    val bottomPanelTab: BottomPanelTab = BottomPanelTab.TERMINAL
)

enum class BottomPanelTab {
    TERMINAL,
    OUTPUT,
    PROBLEMS,
    DEBUG
}
