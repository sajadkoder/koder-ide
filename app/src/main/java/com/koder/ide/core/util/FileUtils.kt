package com.koder.ide.core.util

import java.io.File
import java.text.DecimalFormat

object FileUtils {
    
    private val sizeFormat = DecimalFormat("#,##0.#")
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
    
    fun getLanguageFromExtension(extension: String): String = when (extension.lowercase()) {
        "java" -> "Java"
        "kt", "kts" -> "Kotlin"
        "py" -> "Python"
        "js", "mjs" -> "JavaScript"
        "ts", "tsx" -> "TypeScript"
        "json" -> "JSON"
        "xml" -> "XML"
        "html", "htm" -> "HTML"
        "css", "scss", "sass" -> "CSS"
        "md", "markdown" -> "Markdown"
        "c", "h" -> "C"
        "cpp", "cc", "cxx", "hpp" -> "C++"
        "sh", "bash", "zsh" -> "Shell"
        "yaml", "yml" -> "YAML"
        "sql" -> "SQL"
        "go" -> "Go"
        "rs" -> "Rust"
        "swift" -> "Swift"
        "rb" -> "Ruby"
        "php" -> "PHP"
        "txt" -> "Plain Text"
        else -> extension.uppercase().ifEmpty { "Plain Text" }
    }
    
    fun getScopeFromExtension(extension: String): String? = when (extension.lowercase()) {
        "java" -> "source.java"
        "kt", "kts" -> "source.kotlin"
        "py" -> "source.python"
        "js", "mjs" -> "source.js"
        "ts", "tsx" -> "source.ts"
        "json" -> "source.json"
        "xml" -> "text.xml"
        "html", "htm" -> "text.html.basic"
        "css", "scss", "sass" -> "source.css"
        "md", "markdown" -> "text.html.markdown"
        "c", "h" -> "source.c"
        "cpp", "cc", "cxx", "hpp" -> "source.cpp"
        "sh", "bash", "zsh" -> "source.shell"
        "yaml", "yml" -> "source.yaml"
        "sql" -> "source.sql"
        else -> null
    }
    
    fun isTextFile(file: File): Boolean {
        val textExtensions = setOf(
            "java", "kt", "kts", "py", "js", "mjs", "ts", "tsx", "jsx",
            "json", "xml", "html", "htm", "css", "scss", "sass", "less",
            "md", "markdown", "txt", "log", "csv",
            "c", "h", "cpp", "cc", "cxx", "h", "hpp",
            "sh", "bash", "zsh", "bat", "cmd", "ps1",
            "yaml", "yml", "toml", "ini", "cfg", "conf", "properties",
            "sql", "gradle", "ktm", "swift", "go", "rs", "rb", "php",
            "vue", "svelte", "astro", "dart", "lua", "r", "scala"
        )
        return file.isFile && file.extension.lowercase() in textExtensions
    }
}
