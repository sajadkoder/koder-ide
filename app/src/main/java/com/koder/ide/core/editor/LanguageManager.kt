package com.koder.ide.core.editor

import android.content.Context
import android.graphics.Typeface
import io.github.rosemoe.sora.langs.textmate.TextMateColorScheme
import io.github.rosemoe.sora.langs.textmate.TextMateLanguage
import io.github.rosemoe.sora.langs.textmate.registry.FileProviderRegistry
import io.github.rosemoe.sora.langs.textmate.registry.GrammarRegistry
import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry
import io.github.rosemoe.sora.langs.textmate.registry.model.ThemeModel
import io.github.rosemoe.sora.langs.textmate.registry.provider.AssetsFileResolver
import io.github.rosemoe.sora.widget.CodeEditor
import org.eclipse.tm4e.core.registry.IThemeSource

object LanguageManager {
    
    private const val DEFAULT_THEME = "darcula"
    
    private val languageScopes = mapOf(
        "java" to "source.java",
        "kt" to "source.kotlin",
        "kts" to "source.kotlin",
        "py" to "source.python",
        "js" to "source.js",
        "ts" to "source.ts",
        "json" to "source.json",
        "xml" to "text.xml",
        "html" to "text.html.basic",
        "css" to "source.css",
        "md" to "text.html.markdown",
        "c" to "source.c",
        "cpp" to "source.cpp",
        "h" to "source.c",
        "sh" to "source.shell",
        "yaml" to "source.yaml",
        "yml" to "source.yaml",
        "sql" to "source.sql"
    )
    
    fun initialize(context: Context) {
        try {
            FileProviderRegistry.getInstance().addFileProvider(
                AssetsFileResolver(context.applicationContext.assets)
            )
            
            loadDefaultTheme(context)
            loadGrammars()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun loadDefaultTheme(context: Context) {
        try {
            val themeRegistry = ThemeRegistry.getInstance()
            val themeSource = createThemeSource(context, DEFAULT_THEME)
            themeRegistry.loadTheme(
                ThemeModel(themeSource, DEFAULT_THEME).apply { isDark = true }
            )
            themeRegistry.setTheme(DEFAULT_THEME)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun createThemeSource(context: Context, themeName: String): IThemeSource {
        val path = "textmate/themes/$themeName.json"
        val inputStream = context.assets.open(path)
        return IThemeSource.fromInputStream(inputStream, path, null)
    }
    
    private fun loadGrammars() {
        try {
            GrammarRegistry.getInstance().loadGrammars("textmate/languages.json")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun configureEditor(editor: CodeEditor) {
        editor.apply {
            typefaceText = Typeface.MONOSPACE
            setTextSize(14f)
            isLineNumberEnabled = true
            nonPrintablePaintingFlags = CodeEditor.FLAG_DRAW_WHITESPACE_LEADING
            
            try {
                colorScheme = TextMateColorScheme.create(ThemeRegistry.getInstance())
            } catch (e: Exception) {
                setDarkColorScheme()
            }
        }
    }
    
    fun setLanguage(editor: CodeEditor, extension: String): Boolean {
        val scopeName = languageScopes[extension.lowercase()] ?: return false
        
        return try {
            val language = TextMateLanguage.create(scopeName, true)
            editor.setEditorLanguage(language)
            true
        } catch (e: Exception) {
            try {
                val language = TextMateLanguage.create(scopeName, false)
                editor.setEditorLanguage(language)
                true
            } catch (e2: Exception) {
                false
            }
        }
    }
    
    fun setLanguage(editor: CodeEditor, fileExtension: String, fallbackToJava: Boolean = true): Boolean {
        val success = setLanguage(editor, fileExtension)
        if (!success && fallbackToJava) {
            return try {
                val language = TextMateLanguage.create("source.java", true)
                editor.setEditorLanguage(language)
                true
            } catch (e: Exception) {
                false
            }
        }
        return success
    }
    
    fun getLanguageName(extension: String): String = when (extension.lowercase()) {
        "java" -> "Java"
        "kt", "kts" -> "Kotlin"
        "py" -> "Python"
        "js" -> "JavaScript"
        "ts" -> "TypeScript"
        "json" -> "JSON"
        "xml" -> "XML"
        "html" -> "HTML"
        "css" -> "CSS"
        "md" -> "Markdown"
        "c", "h" -> "C"
        "cpp" -> "C++"
        "sh" -> "Shell"
        "yaml", "yml" -> "YAML"
        "sql" -> "SQL"
        else -> extension.uppercase().ifEmpty { "Text" }
    }
    
    private fun CodeEditor.setDarkColorScheme() {
        val scheme = io.github.rosemoe.sora.widget.schemes.EditorColorScheme()
        scheme.setColor(io.github.rosemoe.sora.widget.schemes.EditorColorScheme.WHOLE_BACKGROUND, -0xd55556)
        scheme.setColor(io.github.rosemoe.sora.widget.schemes.EditorColorScheme.TEXT_NORMAL, -0x1)
        scheme.setColor(io.github.rosemoe.sora.widget.schemes.EditorColorScheme.LINE_NUMBER, -0x888889)
        scheme.setColor(io.github.rosemoe.sora.widget.schemes.EditorColorScheme.LINE_NUMBER_BACKGROUND, -0xdedee0)
        scheme.setColor(io.github.rosemoe.sora.widget.schemes.EditorColorScheme.CURRENT_LINE, -0xcccccd)
        scheme.setColor(io.github.rosemoe.sora.widget.schemes.EditorColorScheme.SELECTION_INSERT, -0x33b5e5)
        scheme.setColor(io.github.rosemoe.sora.widget.schemes.EditorColorScheme.SELECTION_HANDLE, -0x33b5e5)
        colorScheme = scheme
    }
}
