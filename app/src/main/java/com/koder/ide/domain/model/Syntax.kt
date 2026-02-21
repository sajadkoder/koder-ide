package com.koder.ide.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SyntaxHighlight(
    val language: String,
    val patterns: List<SyntaxPattern>
) : Parcelable

@Parcelize
data class SyntaxPattern(
    val pattern: String,
    val type: SyntaxType,
    val color: String
) : Parcelable

enum class SyntaxType {
    KEYWORD,
    STRING,
    NUMBER,
    COMMENT,
    FUNCTION,
    CLASS,
    VARIABLE,
    OPERATOR,
    ANNOTATION,
    TYPE,
    CONSTANT,
    INTERFACE,
    ENUM,
    PROPERTY,
    PARAMETER
}

@Parcelize
data class CodeCompletion(
    val text: String,
    val displayText: String,
    val type: CompletionType,
    val icon: Int? = null,
    val documentation: String? = null,
    val insertText: String = text,
    val sortText: String = text
) : Parcelable

enum class CompletionType {
    KEYWORD,
    FUNCTION,
    CLASS,
    VARIABLE,
    PROPERTY,
    METHOD,
    INTERFACE,
    ENUM,
    CONSTANT,
    SNIPPET,
    FILE,
    FOLDER
}

@Parcelize
data class Diagnostic(
    val severity: DiagnosticSeverity,
    val message: String,
    val startLine: Int,
    val startColumn: Int,
    val endLine: Int,
    val endColumn: Int,
    val source: String? = null,
    val code: String? = null
) : Parcelable

enum class DiagnosticSeverity {
    ERROR,
    WARNING,
    INFORMATION,
    HINT
}

@Parcelize
data class SearchResult(
    val filePath: String,
    val line: Int,
    val column: Int,
    val matchText: String,
    val contextBefore: String,
    val contextAfter: String
) : Parcelable

@Parcelize
data class SymbolDefinition(
    val name: String,
    val type: SymbolType,
    val filePath: String,
    val line: Int,
    val column: Int,
    val documentation: String? = null
) : Parcelable

enum class SymbolType {
    CLASS,
    INTERFACE,
    ENUM,
    FUNCTION,
    METHOD,
    VARIABLE,
    CONSTANT,
    PROPERTY,
    FIELD,
    ANNOTATION
}
