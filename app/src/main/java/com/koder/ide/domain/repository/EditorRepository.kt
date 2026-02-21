package com.koder.ide.domain.repository

import com.koder.ide.domain.model.EditorState
import com.koder.ide.domain.model.EditorTheme
import com.koder.ide.domain.model.LineEnding
import kotlinx.coroutines.flow.Flow
import java.io.File

interface EditorRepository {
    suspend fun loadFile(file: File): Result<String>
    suspend fun saveFile(file: File, content: String): Result<Unit>
    suspend fun getEditorState(filePath: String): EditorState?
    suspend fun saveEditorState(state: EditorState)
    fun getFileContentFlow(file: File): Flow<String>
    
    suspend fun getTheme(): EditorTheme
    suspend fun setTheme(theme: EditorTheme)
    suspend fun getFontSize(): Int
    suspend fun setFontSize(size: Int)
    suspend fun getFontFamily(): String
    suspend fun setFontFamily(family: String)
    suspend fun getTabSize(): Int
    suspend fun setTabSize(size: Int)
    suspend fun isWordWrapEnabled(): Boolean
    suspend fun setWordWrapEnabled(enabled: Boolean)
    suspend fun isLineNumbersEnabled(): Boolean
    suspend fun setLineNumbersEnabled(enabled: Boolean)
    suspend fun isMinimapEnabled(): Boolean
    suspend fun setMinimapEnabled(enabled: Boolean)
    suspend fun isAutoSaveEnabled(): Boolean
    suspend fun setAutoSaveEnabled(enabled: Boolean)
    suspend fun isAutoCompleteEnabled(): Boolean
    suspend fun setAutoCompleteEnabled(enabled: Boolean)
    suspend fun isSyntaxHighlightingEnabled(): Boolean
    suspend fun setSyntaxHighlightingEnabled(enabled: Boolean)
    suspend fun isBracketMatchingEnabled(): Boolean
    suspend fun setBracketMatchingEnabled(enabled: Boolean)
    suspend fun isIndentGuidesEnabled(): Boolean
    suspend fun setIndentGuidesEnabled(enabled: Boolean)
    suspend fun isCurrentLineHighlightEnabled(): Boolean
    suspend fun setCurrentLineHighlightEnabled(enabled: Boolean)
    suspend fun detectLineEnding(content: String): LineEnding
}
