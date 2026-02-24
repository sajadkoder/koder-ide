package com.koder.ide.core.editor

import android.content.Context
import android.graphics.Typeface
import io.github.rosemoe.sora.langs.textmate.TextMateColorScheme
import io.github.rosemoe.sora.langs.textmate.TextMateLanguage
import io.github.rosemoe.sora.langs.textmate.registry.FileProviderRegistry
import io.github.rosemoe.sora.langs.textmate.registry.GrammarRegistry
import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry
import io.github.rosemoe.sora.langs.textmate.registry.model.ThemeModel
import io.github.rosemoe.sora.widget.CodeEditor
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme
import org.eclipse.tm4e.core.registry.IThemeSource

object EditorHelper {
    
    private var isInitialized = false
    private const val DEFAULT_THEME = "darcula"
    
    private val scopeMap = mapOf(
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
        "bash" to "source.shell",
        "yaml" to "source.yaml",
        "yml" to "source.yaml",
        "sql" to "source.sql"
    )
    
    fun initialize(context: Context) {
        if (isInitialized) return
        
        try {
            FileProviderRegistry.getInstance().addFileProvider(
                io.github.rosemoe.sora.langs.textmate.registry.provider.AssetsFileResolver(context.assets)
            )
            
            loadTheme(context)
            loadGrammars()
            isInitialized = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun loadTheme(context: Context) {
        try {
            val themeRegistry = ThemeRegistry.getInstance()
            val themeSource = IThemeSource.fromInputStream(
                context.assets.open("textmate/themes/$DEFAULT_THEME.json"),
                "textmate/themes/$DEFAULT_THEME.json",
                null
            )
            themeRegistry.loadTheme(ThemeModel(themeSource, DEFAULT_THEME).apply { isDark = true })
            themeRegistry.setTheme(DEFAULT_THEME)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun loadGrammars() {
        try {
            GrammarRegistry.getInstance().loadGrammars("textmate/languages.json")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun configure(editor: CodeEditor) {
        editor.apply {
            typefaceText = Typeface.MONOSPACE
            setTextSize(14f)
            isLineNumberEnabled = true
            nonPrintablePaintingFlags = CodeEditor.FLAG_DRAW_WHITESPACE_LEADING
            
            try {
                colorScheme = TextMateColorScheme.create(ThemeRegistry.getInstance())
            } catch (e: Exception) {
                setFallbackColorScheme()
            }
        }
    }
    
    fun setLanguage(editor: CodeEditor, extension: String) {
        val scope = scopeMap[extension.lowercase()]
        
        if (scope != null) {
            try {
                val language = TextMateLanguage.create(scope, true)
                editor.setEditorLanguage(language)
            } catch (e: Exception) {
                try {
                    val language = TextMateLanguage.create(scope, false)
                    editor.setEditorLanguage(language)
                } catch (e2: Exception) {
                    // Fallback to no highlighting
                }
            }
        }
    }
    
    private fun CodeEditor.setFallbackColorScheme() {
        val scheme = EditorColorScheme()
        scheme.setColor(EditorColorScheme.WHOLE_BACKGROUND, -0x1a1a1b) // Dark background
        scheme.setColor(EditorColorScheme.TEXT_NORMAL, -0x1)
        scheme.setColor(EditorColorScheme.LINE_NUMBER, -0x666667)
        scheme.setColor(EditorColorScheme.LINE_NUMBER_BACKGROUND, -0x1e1e1f)
        scheme.setColor(EditorColorScheme.CURRENT_LINE, -0x2d2d30)
        scheme.setColor(EditorColorScheme.SELECTION_INSERT, -0x3d5a80)
        scheme.setColor(EditorColorScheme.SELECTION_HANDLE, -0x3d5a80)
        colorScheme = scheme
    }
}
