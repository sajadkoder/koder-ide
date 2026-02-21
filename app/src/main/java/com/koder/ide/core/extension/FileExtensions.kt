package com.koder.ide.core.extension

import java.io.File

fun File.isCodeFile(): Boolean {
    val codeExtensions = setOf(
        "kt", "java", "py", "js", "ts", "jsx", "tsx",
        "c", "cpp", "h", "hpp", "cs", "go", "rs", "rb",
        "php", "swift", "m", "mm", "scala", "groovy",
        "lua", "r", "sql", "html", "css", "scss", "sass",
        "less", "xml", "json", "yaml", "yml", "toml",
        "sh", "bash", "zsh", "ps1", "bat", "md"
    )
    return extension.lowercase() in codeExtensions
}

fun File.isImageFile(): Boolean {
    val imageExtensions = setOf("png", "jpg", "jpeg", "gif", "bmp", "webp", "svg")
    return extension.lowercase() in imageExtensions
}

fun File.isBinaryFile(): Boolean {
    val binaryExtensions = setOf(
        "png", "jpg", "jpeg", "gif", "bmp", "ico",
        "mp3", "mp4", "wav", "avi", "mkv",
        "zip", "tar", "gz", "rar", "7z",
        "pdf", "exe", "dll", "so", "dylib",
        "class", "dex", "apk", "aab"
    )
    return extension.lowercase() in binaryExtensions
}

fun File.readTextSafely(): String? {
    return try {
        if (exists() && isFile && canRead()) {
            readText()
        } else null
    } catch (e: Exception) {
        null
    }
}

fun File.writeTextSafely(content: String): Boolean {
    return try {
        writeText(content)
        true
    } catch (e: Exception) {
        false
    }
}

fun File.deleteRecursivelySafely(): Boolean {
    return try {
        deleteRecursively()
    } catch (e: Exception) {
        false
    }
}

fun File.getMimeType(): String {
    return when (extension.lowercase()) {
        "txt" -> "text/plain"
        "html", "htm" -> "text/html"
        "css" -> "text/css"
        "js" -> "application/javascript"
        "json" -> "application/json"
        "xml" -> "application/xml"
        "java" -> "text/x-java"
        "kt", "kts" -> "text/x-kotlin"
        "py" -> "text/x-python"
        "md" -> "text/markdown"
        "pdf" -> "application/pdf"
        "png" -> "image/png"
        "jpg", "jpeg" -> "image/jpeg"
        "gif" -> "image/gif"
        "svg" -> "image/svg+xml"
        "zip" -> "application/zip"
        else -> "application/octet-stream"
    }
}
