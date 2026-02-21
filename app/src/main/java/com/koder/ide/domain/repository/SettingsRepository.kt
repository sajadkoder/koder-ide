package com.koder.ide.domain.repository

import com.koder.ide.domain.model.EditorTheme
import com.koder.ide.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getThemeMode(): Flow<ThemeMode>
    suspend fun setThemeMode(mode: ThemeMode)
    
    fun getEditorTheme(): Flow<EditorTheme>
    suspend fun setEditorTheme(theme: EditorTheme)
    
    fun getFontSize(): Flow<Int>
    suspend fun setFontSize(size: Int)
    
    fun getFontFamily(): Flow<String>
    suspend fun setFontFamily(family: String)
    
    fun getTabSize(): Flow<Int>
    suspend fun setTabSize(size: Int)
    
    fun isWordWrapEnabled(): Flow<Boolean>
    suspend fun setWordWrapEnabled(enabled: Boolean)
    
    fun isLineNumbersEnabled(): Flow<Boolean>
    suspend fun setLineNumbersEnabled(enabled: Boolean)
    
    fun isMinimapEnabled(): Flow<Boolean>
    suspend fun setMinimapEnabled(enabled: Boolean)
    
    fun isAutoSaveEnabled(): Flow<Boolean>
    suspend fun setAutoSaveEnabled(enabled: Boolean)
    
    fun getAutoSaveDelay(): Flow<Int>
    suspend fun setAutoSaveDelay(delayMs: Int)
    
    fun isAutoCompleteEnabled(): Flow<Boolean>
    suspend fun setAutoCompleteEnabled(enabled: Boolean)
    
    fun isSyntaxHighlightingEnabled(): Flow<Boolean>
    suspend fun setSyntaxHighlightingEnabled(enabled: Boolean)
    
    fun isBracketMatchingEnabled(): Flow<Boolean>
    suspend fun setBracketMatchingEnabled(enabled: Boolean)
    
    fun isIndentGuidesEnabled(): Flow<Boolean>
    suspend fun setIndentGuidesEnabled(enabled: Boolean)
    
    fun isCurrentLineHighlightEnabled(): Flow<Boolean>
    suspend fun setCurrentLineHighlightEnabled(enabled: Boolean)
    
    fun getRecentProjectsLimit(): Flow<Int>
    suspend fun setRecentProjectsLimit(limit: Int)
    
    fun getDefaultEncoding(): Flow<String>
    suspend fun setDefaultEncoding(encoding: String)
    
    fun isShowHiddenFilesEnabled(): Flow<Boolean>
    suspend fun setShowHiddenFilesEnabled(enabled: Boolean)
    
    fun isConfirmBeforeDeleteEnabled(): Flow<Boolean>
    suspend fun setConfirmBeforeDeleteEnabled(enabled: Boolean)
    
    fun isVibrationEnabled(): Flow<Boolean>
    suspend fun setVibrationEnabled(enabled: Boolean)
}
