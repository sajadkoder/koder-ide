package com.koder.ide.domain.model

import java.io.File

data class ProjectFile(
    val path: String,
    val name: String,
    val isDirectory: Boolean,
    val size: Long = 0,
    val lastModified: Long = 0,
    val extension: String = ""
) {
    companion object {
        fun fromFile(file: File) = ProjectFile(
            path = file.absolutePath,
            name = file.name,
            isDirectory = file.isDirectory,
            size = if (file.isFile) file.length() else 0,
            lastModified = file.lastModified(),
            extension = file.extension
        )
    }
}

data class EditorTab(
    val id: String = java.util.UUID.randomUUID().toString(),
    val file: ProjectFile,
    var content: String = "",
    var isModified: Boolean = false,
    var cursorPosition: Int = 0,
    var scrollPosition: Int = 0
)

enum class FileChangeType {
    CREATED, MODIFIED, DELETED
}

sealed class FileEvent {
    data class Change(val type: FileChangeType, val file: ProjectFile) : FileEvent()
    data class Error(val message: String) : FileEvent()
}
