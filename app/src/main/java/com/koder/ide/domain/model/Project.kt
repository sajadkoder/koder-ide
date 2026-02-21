package com.koder.ide.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.io.File

@Parcelize
data class Project(
    val id: Long = 0,
    val name: String,
    val path: String,
    val description: String = "",
    val language: String = "Unknown",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val lastOpenedAt: Long = 0,
    val openCount: Int = 0
) : Parcelable {
    val file: File get() = File(path)
    val exists: Boolean get() = file.exists()
}

@Parcelize
data class RecentFile(
    val id: Long = 0,
    val projectId: Long? = null,
    val path: String,
    val name: String,
    val language: String = "Plain Text",
    val lastOpenedAt: Long = System.currentTimeMillis(),
    val cursorPosition: Int = 0,
    val scrollPosition: Int = 0
) : Parcelable {
    val file: File get() = File(path)
    val exists: Boolean get() = file.exists()
}

@Parcelize
data class FileNode(
    val file: File,
    val name: String = file.name,
    val isDirectory: Boolean = file.isDirectory,
    val children: MutableList<FileNode> = mutableListOf(),
    var isExpanded: Boolean = false,
    var depth: Int = 0
) : Parcelable {
    val path: String get() = file.absolutePath
    val extension: String get() = file.extension
    val size: Long get() = if (isDirectory) 0 else file.length()
    val lastModified: Long get() = file.lastModified()
}

@Parcelize
data class EditorState(
    val filePath: String? = null,
    val content: String = "",
    val cursorPosition: Int = 0,
    val selectionStart: Int = 0,
    val selectionEnd: Int = 0,
    val scrollX: Int = 0,
    val scrollY: Int = 0,
    val isModified: Boolean = false,
    val encoding: String = "UTF-8",
    val lineEnding: LineEnding = LineEnding.LF
) : Parcelable

enum class LineEnding(val displayName: String, val value: String) {
    LF("LF (Unix)", "\n"),
    CRLF("CRLF (Windows)", "\r\n"),
    CR("CR (Classic Mac)", "\r")
}

enum class ThemeMode(val displayName: String) {
    LIGHT("Light"),
    DARK("Dark"),
    SYSTEM("Follow System")
}

enum class EditorTheme(val displayName: String) {
    DEFAULT("Default"),
    MONOKAI("Monokai"),
    DRACULA("Dracula"),
    ONE_DARK("One Dark"),
    SOLARIZED_DARK("Solarized Dark"),
    SOLARIZED_LIGHT("Solarized Light"),
    GITHUB_DARK("GitHub Dark"),
    GITHUB_LIGHT("GitHub Light"),
    MATERIAL_DARKER("Material Darker"),
    MATERIAL_PALENIGHT("Material Palenight"),
    MATERIAL_OCEANIC("Material Oceanic"),
    NOVA("Nova")
}
