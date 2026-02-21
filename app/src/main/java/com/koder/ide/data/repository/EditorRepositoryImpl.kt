package com.koder.ide.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.koder.ide.domain.model.EditorTheme
import com.koder.ide.domain.model.LineEnding
import com.koder.ide.domain.model.ThemeMode
import com.koder.ide.domain.repository.EditorRepository
import com.koder.ide.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class EditorRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : EditorRepository {

    private val editorStates = mutableMapOf<String, com.koder.ide.domain.model.EditorState>()

    override suspend fun loadFile(file: File): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                if (!file.exists()) {
                    return@withContext Result.failure(Exception("File not found: ${file.path}"))
                }
                if (!file.canRead()) {
                    return@withContext Result.failure(Exception("Cannot read file: ${file.path}"))
                }
                Result.success(file.readText())
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun saveFile(file: File, content: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                file.writeText(content)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun getEditorState(filePath: String): com.koder.ide.domain.model.EditorState? {
        return editorStates[filePath]
    }

    override suspend fun saveEditorState(state: com.koder.ide.domain.model.EditorState) {
        state.filePath?.let { editorStates[it] = state }
    }

    override fun getFileContentFlow(file: File): Flow<String> {
        return kotlinx.coroutines.flow.flow {
            emit(file.readText())
        }
    }

    override suspend fun getTheme(): EditorTheme = EditorTheme.DEFAULT
    override suspend fun setTheme(theme: EditorTheme) {}
    override suspend fun getFontSize(): Int = 14
    override suspend fun setFontSize(size: Int) {}
    override suspend fun getFontFamily(): String = "JetBrains Mono"
    override suspend fun setFontFamily(family: String) {}
    override suspend fun getTabSize(): Int = 4
    override suspend fun setTabSize(size: Int) {}
    override suspend fun isWordWrapEnabled(): Boolean = false
    override suspend fun setWordWrapEnabled(enabled: Boolean) {}
    override suspend fun isLineNumbersEnabled(): Boolean = true
    override suspend fun setLineNumbersEnabled(enabled: Boolean) {}
    override suspend fun isMinimapEnabled(): Boolean = true
    override suspend fun setMinimapEnabled(enabled: Boolean) {}
    override suspend fun isAutoSaveEnabled(): Boolean = true
    override suspend fun setAutoSaveEnabled(enabled: Boolean) {}
    override suspend fun isAutoCompleteEnabled(): Boolean = true
    override suspend fun setAutoCompleteEnabled(enabled: Boolean) {}
    override suspend fun isSyntaxHighlightingEnabled(): Boolean = true
    override suspend fun setSyntaxHighlightingEnabled(enabled: Boolean) {}
    override suspend fun isBracketMatchingEnabled(): Boolean = true
    override suspend fun setBracketMatchingEnabled(enabled: Boolean) {}
    override suspend fun isIndentGuidesEnabled(): Boolean = true
    override suspend fun setIndentGuidesEnabled(enabled: Boolean) {}
    override suspend fun isCurrentLineHighlightEnabled(): Boolean = true
    override suspend fun setCurrentLineHighlightEnabled(enabled: Boolean) {}

    override suspend fun detectLineEnding(content: String): LineEnding {
        return when {
            content.contains("\r\n") -> LineEnding.CRLF
            content.contains("\r") -> LineEnding.CR
            else -> LineEnding.LF
        }
    }
}

class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {

    companion object {
        private val THEME_MODE = stringPreferencesKey("theme_mode")
        private val EDITOR_THEME = stringPreferencesKey("editor_theme")
        private val FONT_SIZE = intPreferencesKey("font_size")
        private val FONT_FAMILY = stringPreferencesKey("font_family")
        private val TAB_SIZE = intPreferencesKey("tab_size")
        private val WORD_WRAP = booleanPreferencesKey("word_wrap")
        private val LINE_NUMBERS = booleanPreferencesKey("line_numbers")
        private val MINIMAP = booleanPreferencesKey("minimap")
        private val AUTO_SAVE = booleanPreferencesKey("auto_save")
        private val AUTO_SAVE_DELAY = intPreferencesKey("auto_save_delay")
        private val AUTO_COMPLETE = booleanPreferencesKey("auto_complete")
        private val SYNTAX_HIGHLIGHTING = booleanPreferencesKey("syntax_highlighting")
        private val BRACKET_MATCHING = booleanPreferencesKey("bracket_matching")
        private val INDENT_GUIDES = booleanPreferencesKey("indent_guides")
        private val CURRENT_LINE_HIGHLIGHT = booleanPreferencesKey("current_line_highlight")
        private val RECENT_PROJECTS_LIMIT = intPreferencesKey("recent_projects_limit")
        private val DEFAULT_ENCODING = stringPreferencesKey("default_encoding")
        private val SHOW_HIDDEN_FILES = booleanPreferencesKey("show_hidden_files")
        private val CONFIRM_BEFORE_DELETE = booleanPreferencesKey("confirm_before_delete")
        private val VIBRATION = booleanPreferencesKey("vibration")
    }

    override fun getThemeMode(): Flow<ThemeMode> = dataStore.data.map { prefs ->
        prefs[THEME_MODE]?.let { ThemeMode.valueOf(it) } ?: ThemeMode.SYSTEM
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[THEME_MODE] = mode.name }
    }

    override fun getEditorTheme(): Flow<EditorTheme> = dataStore.data.map { prefs ->
        prefs[EDITOR_THEME]?.let { EditorTheme.valueOf(it) } ?: EditorTheme.MONOKAI
    }

    override suspend fun setEditorTheme(theme: EditorTheme) {
        dataStore.edit { it[EDITOR_THEME] = theme.name }
    }

    override fun getFontSize(): Flow<Int> = dataStore.data.map { prefs ->
        prefs[FONT_SIZE] ?: 14
    }

    override suspend fun setFontSize(size: Int) {
        dataStore.edit { it[FONT_SIZE] = size }
    }

    override fun getFontFamily(): Flow<String> = dataStore.data.map { prefs ->
        prefs[FONT_FAMILY] ?: "JetBrains Mono"
    }

    override suspend fun setFontFamily(family: String) {
        dataStore.edit { it[FONT_FAMILY] = family }
    }

    override fun getTabSize(): Flow<Int> = dataStore.data.map { prefs ->
        prefs[TAB_SIZE] ?: 4
    }

    override suspend fun setTabSize(size: Int) {
        dataStore.edit { it[TAB_SIZE] = size }
    }

    override fun isWordWrapEnabled(): Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[WORD_WRAP] ?: false
    }

    override suspend fun setWordWrapEnabled(enabled: Boolean) {
        dataStore.edit { it[WORD_WRAP] = enabled }
    }

    override fun isLineNumbersEnabled(): Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[LINE_NUMBERS] ?: true
    }

    override suspend fun setLineNumbersEnabled(enabled: Boolean) {
        dataStore.edit { it[LINE_NUMBERS] = enabled }
    }

    override fun isMinimapEnabled(): Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[MINIMAP] ?: true
    }

    override suspend fun setMinimapEnabled(enabled: Boolean) {
        dataStore.edit { it[MINIMAP] = enabled }
    }

    override fun isAutoSaveEnabled(): Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[AUTO_SAVE] ?: true
    }

    override suspend fun setAutoSaveEnabled(enabled: Boolean) {
        dataStore.edit { it[AUTO_SAVE] = enabled }
    }

    override fun getAutoSaveDelay(): Flow<Int> = dataStore.data.map { prefs ->
        prefs[AUTO_SAVE_DELAY] ?: 1000
    }

    override suspend fun setAutoSaveDelay(delayMs: Int) {
        dataStore.edit { it[AUTO_SAVE_DELAY] = delayMs }
    }

    override fun isAutoCompleteEnabled(): Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[AUTO_COMPLETE] ?: true
    }

    override suspend fun setAutoCompleteEnabled(enabled: Boolean) {
        dataStore.edit { it[AUTO_COMPLETE] = enabled }
    }

    override fun isSyntaxHighlightingEnabled(): Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[SYNTAX_HIGHLIGHTING] ?: true
    }

    override suspend fun setSyntaxHighlightingEnabled(enabled: Boolean) {
        dataStore.edit { it[SYNTAX_HIGHLIGHTING] = enabled }
    }

    override fun isBracketMatchingEnabled(): Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[BRACKET_MATCHING] ?: true
    }

    override suspend fun setBracketMatchingEnabled(enabled: Boolean) {
        dataStore.edit { it[BRACKET_MATCHING] = enabled }
    }

    override fun isIndentGuidesEnabled(): Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[INDENT_GUIDES] ?: true
    }

    override suspend fun setIndentGuidesEnabled(enabled: Boolean) {
        dataStore.edit { it[INDENT_GUIDES] = enabled }
    }

    override fun isCurrentLineHighlightEnabled(): Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[CURRENT_LINE_HIGHLIGHT] ?: true
    }

    override suspend fun setCurrentLineHighlightEnabled(enabled: Boolean) {
        dataStore.edit { it[CURRENT_LINE_HIGHLIGHT] = enabled }
    }

    override fun getRecentProjectsLimit(): Flow<Int> = dataStore.data.map { prefs ->
        prefs[RECENT_PROJECTS_LIMIT] ?: 20
    }

    override suspend fun setRecentProjectsLimit(limit: Int) {
        dataStore.edit { it[RECENT_PROJECTS_LIMIT] = limit }
    }

    override fun getDefaultEncoding(): Flow<String> = dataStore.data.map { prefs ->
        prefs[DEFAULT_ENCODING] ?: "UTF-8"
    }

    override suspend fun setDefaultEncoding(encoding: String) {
        dataStore.edit { it[DEFAULT_ENCODING] = encoding }
    }

    override fun isShowHiddenFilesEnabled(): Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[SHOW_HIDDEN_FILES] ?: false
    }

    override suspend fun setShowHiddenFilesEnabled(enabled: Boolean) {
        dataStore.edit { it[SHOW_HIDDEN_FILES] = enabled }
    }

    override fun isConfirmBeforeDeleteEnabled(): Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[CONFIRM_BEFORE_DELETE] ?: true
    }

    override suspend fun setConfirmBeforeDeleteEnabled(enabled: Boolean) {
        dataStore.edit { it[CONFIRM_BEFORE_DELETE] = enabled }
    }

    override fun isVibrationEnabled(): Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[VIBRATION] ?: true
    }

    override suspend fun setVibrationEnabled(enabled: Boolean) {
        dataStore.edit { it[VIBRATION] = enabled }
    }
}
