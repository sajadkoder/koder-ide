package com.koder.ide.domain.model

data class EditorSettings(
    val fontSize: Int = 14,
    val tabSize: Int = 4,
    val showLineNumbers: Boolean = true,
    val wordWrap: Boolean = false,
    val autoSave: Boolean = true,
    val theme: EditorTheme = EditorTheme.DARK
)

enum class EditorTheme {
    LIGHT, DARK, SYSTEM
}

data class AppSettings(
    val editor: EditorSettings = EditorSettings(),
    val terminal: TerminalSettings = TerminalSettings(),
    val explorer: ExplorerSettings = ExplorerSettings()
)

data class TerminalSettings(
    val fontSize: Int = 14,
    val fontFamily: String = "monospace",
    val scrollback: Int = 10000
)

data class ExplorerSettings(
    val showHiddenFiles: Boolean = false,
    val sortBy: SortBy = SortBy.NAME,
    val sortOrder: SortOrder = SortOrder.ASC
)

enum class SortBy {
    NAME, SIZE, DATE
}

enum class SortOrder {
    ASC, DESC
}
