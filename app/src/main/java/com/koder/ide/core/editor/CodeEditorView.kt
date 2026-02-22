package com.koder.ide.core.editor

import android.content.Context
import android.widget.FrameLayout
import io.github.rosemoe.sora.widget.CodeEditor
import java.io.File

class CodeEditorView(context: Context) : FrameLayout(context) {

    val editor: CodeEditor = CodeEditor(context)
    private var currentFile: File? = null
    var isModified: Boolean = false
        private set
    
    var onContentChanged: ((String) -> Unit)? = null

    init {
        addView(editor, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        LanguageManager.configureEditor(editor)
        
        editor.subscribeEvent(io.github.rosemoe.sora.event.ContentChangeEvent::class.java) { _, _ ->
            isModified = true
            onContentChanged?.invoke(editor.text.toString())
        }
    }

    fun openFile(file: File) {
        currentFile = file
        val content = if (file.exists()) file.readText() else ""
        editor.setText(content)
        isModified = false
        LanguageManager.setLanguage(editor, file.extension, true)
    }

    fun save(): Boolean {
        return currentFile?.let { file ->
            try {
                file.writeText(editor.text.toString())
                isModified = false
                true
            } catch (e: Exception) {
                false
            }
        } ?: false
    }

    fun getContent(): String = editor.text.toString()
    
    fun setContent(content: String) {
        editor.setText(content)
    }
    
    fun getCurrentFile(): File? = currentFile

    fun undo() = editor.undo()
    fun redo() = editor.redo()
    
    fun canUndo(): Boolean = editor.canUndo()
    fun canRedo(): Boolean = editor.canRedo()

    fun release() {
        editor.release()
    }
}
