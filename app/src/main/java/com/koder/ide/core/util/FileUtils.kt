package com.koder.ide.core.util

import java.io.File
import java.util.Locale

object FileUtils {

    private val LANGUAGE_EXTENSIONS = mapOf(
        "kt" to "Kotlin",
        "java" to "Java",
        "py" to "Python",
        "js" to "JavaScript",
        "ts" to "TypeScript",
        "jsx" to "JSX",
        "tsx" to "TSX",
        "c" to "C",
        "cpp" to "C++",
        "h" to "C Header",
        "hpp" to "C++ Header",
        "cs" to "C#",
        "go" to "Go",
        "rs" to "Rust",
        "rb" to "Ruby",
        "php" to "PHP",
        "swift" to "Swift",
        "m" to "Objective-C",
        "mm" to "Objective-C++",
        "scala" to "Scala",
        "groovy" to "Groovy",
        "lua" to "Lua",
        "r" to "R",
        "sql" to "SQL",
        "html" to "HTML",
        "htm" to "HTML",
        "css" to "CSS",
        "scss" to "SCSS",
        "sass" to "Sass",
        "less" to "Less",
        "xml" to "XML",
        "json" to "JSON",
        "yaml" to "YAML",
        "yml" to "YAML",
        "toml" to "TOML",
        "ini" to "INI",
        "properties" to "Properties",
        "sh" to "Shell",
        "bash" to "Bash",
        "zsh" to "Zsh",
        "ps1" to "PowerShell",
        "bat" to "Batch",
        "md" to "Markdown",
        "markdown" to "Markdown",
        "rst" to "reStructuredText",
        "tex" to "LaTeX",
        "gradle" to "Gradle",
        "kts" to "Kotlin Script",
        "dart" to "Dart",
        "vue" to "Vue",
        "svelte" to "Svelte",
        "ex" to "Elixir",
        "exs" to "Elixir",
        "erl" to "Erlang",
        "hs" to "Haskell",
        "clj" to "Clojure",
        "cljs" to "ClojureScript",
        "lisp" to "Lisp",
        "el" to "Emacs Lisp",
        "vim" to "Vim Script",
        "dockerfile" to "Dockerfile",
        "makefile" to "Makefile",
        "cmake" to "CMake"
    )

    fun getLanguageFromExtension(extension: String): String {
        return LANGUAGE_EXTENSIONS[extension.lowercase(Locale.getDefault())] ?: "Plain Text"
    }

    fun getLanguageFromFile(file: File): String {
        val name = file.name.lowercase(Locale.getDefault())
        if (name == "dockerfile" || name == "makefile" || name == "cmakelists.txt") {
            return when (name) {
                "dockerfile" -> "Dockerfile"
                "makefile" -> "Makefile"
                "cmakelists.txt" -> "CMake"
                else -> "Plain Text"
            }
        }
        val extension = file.extension
        return getLanguageFromExtension(extension)
    }

    fun getFileIcon(language: String): Int {
        return when (language) {
            "Kotlin", "Kotlin Script" -> com.koder.ide.R.drawable.ic_language_kotlin
            "Java" -> com.koder.ide.R.drawable.ic_language_java
            "Python" -> com.koder.ide.R.drawable.ic_language_python
            "JavaScript", "JSX" -> com.koder.ide.R.drawable.ic_language_javascript
            "TypeScript", "TSX" -> com.koder.ide.R.drawable.ic_language_typescript
            "HTML" -> com.koder.ide.R.drawable.ic_language_html
            "CSS", "SCSS", "Sass", "Less" -> com.koder.ide.R.drawable.ic_language_css
            "JSON" -> com.koder.ide.R.drawable.ic_language_json
            "Markdown" -> com.koder.ide.R.drawable.ic_language_markdown
            else -> com.koder.ide.R.drawable.ic_file_generic
        }
    }

    fun isBinaryFile(file: File): Boolean {
        val binaryExtensions = setOf(
            "png", "jpg", "jpeg", "gif", "bmp", "ico", "svg",
            "mp3", "mp4", "wav", "avi", "mov", "mkv",
            "zip", "tar", "gz", "rar", "7z",
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx",
            "exe", "dll", "so", "dylib", "a", "o",
            "jar", "aar", "apk", "aab", "dex",
            "class", "obj", "bin", "dat"
        )
        return file.extension.lowercase(Locale.getDefault()) in binaryExtensions
    }

    fun isImageFile(file: File): Boolean {
        val imageExtensions = setOf("png", "jpg", "jpeg", "gif", "bmp", "webp", "svg")
        return file.extension.lowercase(Locale.getDefault()) in imageExtensions
    }

    fun isCodeFile(file: File): Boolean {
        val codeExtensions = setOf(
            "kt", "java", "py", "js", "ts", "jsx", "tsx",
            "c", "cpp", "h", "hpp", "cs", "go", "rs", "rb",
            "php", "swift", "m", "mm", "scala", "groovy",
            "lua", "r", "sql", "html", "css", "scss", "sass",
            "less", "xml", "json", "yaml", "yml", "toml",
            "sh", "bash", "zsh", "ps1", "bat", "md"
        )
        return file.extension.lowercase(Locale.getDefault()) in codeExtensions
    }

    fun formatFileSize(size: Long): String {
        return when {
            size < 1024 -> "$size B"
            size < 1024 * 1024 -> String.format(Locale.getDefault(), "%.1f KB", size / 1024.0)
            size < 1024 * 1024 * 1024 -> String.format(Locale.getDefault(), "%.1f MB", size / (1024.0 * 1024))
            else -> String.format(Locale.getDefault(), "%.1f GB", size / (1024.0 * 1024 * 1024))
        }
    }
}
