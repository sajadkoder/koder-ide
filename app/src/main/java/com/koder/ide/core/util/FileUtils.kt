package com.koder.ide.core.util

import java.io.File
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

object FileUtils {
    
    private val sizeFormat = DecimalFormat("#,##0.#")
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    
    private val sizeUnits = arrayOf("B", "KB", "MB", "GB")
    
    fun formatFileSize(bytes: Long): String {
        if (bytes < 1024) return "$bytes B"
        var size = bytes.toDouble()
        var unitIndex = 0
        while (size >= 1024 && unitIndex < sizeUnits.size - 1) {
            size /= 1024
            unitIndex++
        }
        return "${sizeFormat.format(size)} ${sizeUnits[unitIndex]}"
    }
    
    fun formatDate(timestamp: Long): String = dateFormat.format(Date(timestamp))
    
    fun getFileExtension(fileName: String): String {
        val lastDot = fileName.lastIndexOf('.')
        return if (lastDot > 0) fileName.substring(lastDot + 1) else ""
    }
    
    fun getFileName(path: String): String {
        val lastSlash = path.lastIndexOf('/')
        return if (lastSlash >= 0) path.substring(lastSlash + 1) else path
    }
    
    fun getFileIconName(extension: String): String = when (extension.lowercase()) {
        "java" -> "java"
        "kt", "kts" -> "kotlin"
        "py" -> "python"
        "js" -> "javascript"
        "ts" -> "typescript"
        "json" -> "json"
        "xml" -> "xml"
        "html", "htm" -> "html"
        "css" -> "css"
        "md" -> "markdown"
        "c", "cpp", "h", "hpp" -> "cpp"
        "sh" -> "shell"
        "yaml", "yml" -> "yaml"
        "sql" -> "database"
        "txt" -> "text"
        "png", "jpg", "jpeg", "gif", "webp", "svg" -> "image"
        "mp3", "wav", "ogg" -> "audio"
        "mp4", "avi", "mkv" -> "video"
        "zip", "tar", "gz", "rar", "7z" -> "archive"
        else -> "file"
    }
    
    fun isTextFile(extension: String): Boolean {
        return extension.lowercase() in setOf(
            "java", "kt", "kts", "py", "js", "ts", "jsx", "tsx",
            "json", "xml", "html", "htm", "css", "scss", "sass", "less",
            "md", "markdown", "txt", "log", "csv",
            "c", "cpp", "h", "hpp", "cc", "cxx",
            "sh", "bash", "zsh", "bat", "cmd", "ps1",
            "yaml", "yml", "toml", "ini", "cfg", "conf", "properties",
            "sql", "gradle", "ktm", "swift", "go", "rs", "rb", "php",
            "vue", "svelte", "astro"
        )
    }
    
    fun listFiles(directory: File, showHidden: Boolean = false): List<File> {
        return directory.listFiles()
            ?.filter { showHidden || !it.name.startsWith(".") }
            ?.sortedWith(compareBy<File> { !it.isDirectory }.thenBy { it.name.lowercase() })
            ?: emptyList()
    }
    
    fun createFile(parent: File, name: String): File? {
        return try {
            File(parent, name).apply { createNewFile() }
        } catch (e: Exception) {
            null
        }
    }
    
    fun createDirectory(parent: File, name: String): File? {
        return try {
            File(parent, name).apply { mkdirs() }
        } catch (e: Exception) {
            null
        }
    }
    
    fun deleteRecursively(file: File): Boolean {
        return if (file.isDirectory) {
            file.deleteRecursively()
        } else {
            file.delete()
        }
    }
    
    fun copyFile(source: File, dest: File): Boolean {
        return try {
            source.copyTo(dest, overwrite = true)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    fun moveFile(source: File, dest: File): Boolean {
        return try {
            source.renameTo(dest)
        } catch (e: Exception) {
            false
        }
    }
    
    fun renameFile(file: File, newName: String): Boolean {
        return try {
            file.renameTo(File(file.parentFile ?: return false, newName))
        } catch (e: Exception) {
            false
        }
    }
}
