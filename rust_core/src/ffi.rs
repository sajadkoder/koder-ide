//! FFI bindings for Flutter integration
//! 
//! This module provides the bridge between Rust and Flutter/Dart using flutter_rust_bridge.

use crate::editor::{Cursor, Editor, Selection, TextBuffer};
use crate::search::{SearchEngine, SearchOptions, SearchResult};
use crate::syntax::{HighlightToken, Language, SyntaxEngine, SyntaxTheme, TokenType};
use crate::lsp::{CompletionItem, LspClient, Position, SimpleAutocomplete};

use serde::{Deserialize, Serialize};
use std::sync::{Arc, Mutex};
use lazy_static::lazy_static;

// Global state for the editor
lazy_static! {
    static ref EDITOR_STATE: Arc<Mutex<EditorState>> = Arc::new(Mutex::new(EditorState::new()));
}

/// Global editor state
struct EditorState {
    editor: Editor,
    syntax_engine: SyntaxEngine,
    search_engine: SearchEngine,
    lsp_client: LspClient,
    autocomplete: SimpleAutocomplete,
    current_file_id: String,
    highlight_tokens: Vec<HighlightToken>,
    theme: SyntaxTheme,
}

impl EditorState {
    fn new() -> Self {
        EditorState {
            editor: Editor::new(),
            syntax_engine: SyntaxEngine::new(),
            search_engine: SearchEngine::new(),
            lsp_client: LspClient::new(),
            autocomplete: SimpleAutocomplete::new(),
            current_file_id: String::new(),
            highlight_tokens: Vec::new(),
            theme: SyntaxTheme::vscode_dark(),
        }
    }
}

// ============================================================================
// FFI Functions exposed to Flutter
// ============================================================================

/// Initialize the editor
pub fn ffi_init() -> String {
    crate::init();
    "Koder Core initialized".to_string()
}

/// Create a new empty document
pub fn ffi_new_document() -> FfiResult {
    let mut state = EDITOR_STATE.lock().unwrap();
    state.editor = Editor::new();
    state.current_file_id = uuid_string();
    state.highlight_tokens.clear();
    
    FfiResult {
        success: true,
        message: "New document created".to_string(),
    }
}

/// Open a file
pub fn ffi_open_file(path: String) -> FfiResult {
    let mut state = EDITOR_STATE.lock().unwrap();
    
    match Editor::from_file(std::path::Path::new(&path)) {
        Ok(editor) => {
            state.editor = editor;
            state.current_file_id = uuid_string();
            
            // Set language for syntax highlighting
            if let Some(lang) = state.editor.language.clone() {
                let language = Language::from_extension(&lang);
                let _ = state.syntax_engine.set_language(language);
            }
            
            // Parse for syntax highlighting
            let text = state.editor.buffer.get_text();
            state.highlight_tokens = state.syntax_engine
                .parse(&text, &state.current_file_id)
                .unwrap_or_default();
            
            FfiResult {
                success: true,
                message: format!("Opened: {}", path),
            }
        }
        Err(e) => FfiResult {
            success: false,
            message: format!("Failed to open file: {}", e),
        },
    }
}

/// Set document text
pub fn ffi_set_text(text: String) {
    let mut state = EDITOR_STATE.lock().unwrap();
    state.editor = Editor::with_text(&text);
    state.current_file_id = uuid_string();
    
    // Parse for syntax highlighting
    state.highlight_tokens = state.syntax_engine
        .parse(&text, &state.current_file_id)
        .unwrap_or_default();
}

/// Get document text
pub fn ffi_get_text() -> String {
    let state = EDITOR_STATE.lock().unwrap();
    state.editor.buffer.get_text()
}

/// Get line count
pub fn ffi_get_line_count() -> usize {
    let state = EDITOR_STATE.lock().unwrap();
    state.editor.buffer.line_count()
}

/// Get a specific line
pub fn ffi_get_line(line: usize) -> Option<String> {
    let state = EDITOR_STATE.lock().unwrap();
    state.editor.buffer.get_line(line)
}

/// Get visible lines for rendering
pub fn ffi_get_visible_lines(start_line: usize, count: usize) -> Vec<FfiLine> {
    let state = EDITOR_STATE.lock().unwrap();
    let lines = state.editor.buffer.get_visible_lines(start_line, count);
    
    lines.into_iter().map(|(num, text)| {
        FfiLine {
            line_number: num,
            text,
        }
    }).collect()
}

/// Insert text at cursor
pub fn ffi_insert_text(text: String) -> FfiCursorPosition {
    let mut state = EDITOR_STATE.lock().unwrap();
    state.editor.insert(&text);
    
    // Update syntax highlighting
    let full_text = state.editor.buffer.get_text();
    state.highlight_tokens = state.syntax_engine
        .parse(&full_text, &state.current_file_id)
        .unwrap_or_default();
    
    FfiCursorPosition::from(state.editor.cursor)
}

/// Delete character before cursor (backspace)
pub fn ffi_delete_before() -> FfiCursorPosition {
    let mut state = EDITOR_STATE.lock().unwrap();
    state.editor.delete();
    
    // Update syntax highlighting
    let full_text = state.editor.buffer.get_text();
    state.highlight_tokens = state.syntax_engine
        .parse(&full_text, &state.current_file_id)
        .unwrap_or_default();
    
    FfiCursorPosition::from(state.editor.cursor)
}

/// Move cursor
pub fn ffi_move_cursor(direction: FfiMoveDirection) -> FfiCursorPosition {
    let mut state = EDITOR_STATE.lock().unwrap();
    let dir = match direction {
        FfiMoveDirection::Left => crate::editor::MoveDirection::Left,
        FfiMoveDirection::Right => crate::editor::MoveDirection::Right,
        FfiMoveDirection::Up => crate::editor::MoveDirection::Up,
        FfiMoveDirection::Down => crate::editor::MoveDirection::Down,
        FfiMoveDirection::Home => crate::editor::MoveDirection::Home,
        FfiMoveDirection::End => crate::editor::MoveDirection::End,
        FfiMoveDirection::PageUp => crate::editor::MoveDirection::PageUp,
        FfiMoveDirection::PageDown => crate::editor::MoveDirection::PageDown,
        FfiMoveDirection::DocumentStart => crate::editor::MoveDirection::DocumentStart,
        FfiMoveDirection::DocumentEnd => crate::editor::MoveDirection::DocumentEnd,
    };
    state.editor.move_cursor(dir);
    FfiCursorPosition::from(state.editor.cursor)
}

/// Set cursor position
pub fn ffi_set_cursor(line: usize, column: usize) -> FfiCursorPosition {
    let mut state = EDITOR_STATE.lock().unwrap();
    state.editor.cursor = Cursor { line, column };
    FfiCursorPosition::from(state.editor.cursor)
}

/// Get cursor position
pub fn ffi_get_cursor() -> FfiCursorPosition {
    let state = EDITOR_STATE.lock().unwrap();
    FfiCursorPosition::from(state.editor.cursor)
}

/// Start selection
pub fn ffi_start_selection() {
    let mut state = EDITOR_STATE.lock().unwrap();
    state.editor.start_selection();
}

/// Extend selection
pub fn ffi_extend_selection(direction: FfiMoveDirection) -> FfiSelection {
    let mut state = EDITOR_STATE.lock().unwrap();
    let dir = match direction {
        FfiMoveDirection::Left => crate::editor::MoveDirection::Left,
        FfiMoveDirection::Right => crate::editor::MoveDirection::Right,
        FfiMoveDirection::Up => crate::editor::MoveDirection::Up,
        FfiMoveDirection::Down => crate::editor::MoveDirection::Down,
        FfiMoveDirection::Home => crate::editor::MoveDirection::Home,
        FfiMoveDirection::End => crate::editor::MoveDirection::End,
        _ => crate::editor::MoveDirection::Left,
    };
    state.editor.extend_selection(dir);
    
    state.editor.selection
        .map(|s| FfiSelection::from(s))
        .unwrap_or_default()
}

/// Get selection
pub fn ffi_get_selection() -> Option<FfiSelection> {
    let state = EDITOR_STATE.lock().unwrap();
    state.editor.selection.map(|s| FfiSelection::from(s))
}

/// Get selected text
pub fn ffi_get_selected_text() -> Option<String> {
    let state = EDITOR_STATE.lock().unwrap();
    state.editor.get_selected_text()
}

/// Select all
pub fn ffi_select_all() {
    let mut state = EDITOR_STATE.lock().unwrap();
    state.editor.select_all();
}

/// Clear selection
pub fn ffi_clear_selection() {
    let mut state = EDITOR_STATE.lock().unwrap();
    state.editor.selection = None;
}

/// Get syntax highlight tokens
pub fn ffi_get_highlight_tokens() -> Vec<FfiHighlightToken> {
    let state = EDITOR_STATE.lock().unwrap();
    state.highlight_tokens.iter().map(|t| FfiHighlightToken::from(t)).collect()
}

/// Set language for syntax highlighting
pub fn ffi_set_language(language: String) -> FfiResult {
    let mut state = EDITOR_STATE.lock().unwrap();
    let lang = Language::from_extension(&language);
    
    match state.syntax_engine.set_language(lang) {
        Ok(_) => {
            // Re-parse with new language
            let text = state.editor.buffer.get_text();
            state.highlight_tokens = state.syntax_engine
                .parse(&text, &state.current_file_id)
                .unwrap_or_default();
            
            FfiResult {
                success: true,
                message: format!("Language set to: {}", lang.name()),
            }
        }
        Err(e) => FfiResult {
            success: false,
            message: e,
        },
    }
}

/// Get current theme
pub fn ffi_get_theme() -> FfiSyntaxTheme {
    let state = EDITOR_STATE.lock().unwrap();
    FfiSyntaxTheme::from(&state.theme)
}

/// Set theme
pub fn ffi_set_theme(theme_name: String) -> FfiResult {
    let mut state = EDITOR_STATE.lock().unwrap();
    
    state.theme = match theme_name.as_str() {
        "monokai" => SyntaxTheme::monokai(),
        "one_dark" => SyntaxTheme::one_dark(),
        _ => SyntaxTheme::vscode_dark(),
    };
    
    FfiResult {
        success: true,
        message: format!("Theme set to: {}", state.theme.name),
    }
}

/// Search in document
pub fn ffi_search(pattern: String, case_sensitive: bool, whole_word: bool, use_regex: bool) -> Vec<FfiSearchResult> {
    let mut state = EDITOR_STATE.lock().unwrap();
    
    let options = SearchOptions {
        case_sensitive,
        whole_word,
        use_regex,
        in_selection: false,
        max_results: 1000,
    };
    
    state.search_engine.set_options(options);
    let text = state.editor.buffer.get_text();
    
    match state.search_engine.search(&pattern, &text) {
        Ok(results) => results.iter().map(|r| FfiSearchResult::from(r)).collect(),
        Err(_) => vec![],
    }
}

/// Find next match
pub fn ffi_find_next(pattern: String) -> Option<FfiSearchResult> {
    let mut state = EDITOR_STATE.lock().unwrap();
    let text = state.editor.buffer.get_text();
    let cursor = state.editor.cursor;
    
    state.search_engine.find_next(&pattern, &text, cursor.line, cursor.column)
        .ok()
        .flatten()
        .map(|r| FfiSearchResult::from(&r))
}

/// Find previous match
pub fn ffi_find_previous(pattern: String) -> Option<FfiSearchResult> {
    let mut state = EDITOR_STATE.lock().unwrap();
    let text = state.editor.buffer.get_text();
    let cursor = state.editor.cursor;
    
    state.search_engine.find_previous(&pattern, &text, cursor.line, cursor.column)
        .ok()
        .flatten()
        .map(|r| FfiSearchResult::from(&r))
}

/// Replace all occurrences
pub fn ffi_replace_all(pattern: String, replacement: String) -> FfiResult {
    let mut state = EDITOR_STATE.lock().unwrap();
    let text = state.editor.buffer.get_text();
    
    match state.search_engine.replace_all(&pattern, &replacement, &text) {
        Ok(new_text) => {
            state.editor = Editor::with_text(&new_text);
            state.highlight_tokens = state.syntax_engine
                .parse(&new_text, &state.current_file_id)
                .unwrap_or_default();
            
            FfiResult {
                success: true,
                message: "Replaced all occurrences".to_string(),
            }
        }
        Err(e) => FfiResult {
            success: false,
            message: e,
        },
    }
}

/// Get autocomplete suggestions
pub fn ffi_get_completions(prefix: String) -> Vec<FfiCompletionItem> {
    let state = EDITOR_STATE.lock().unwrap();
    let language = state.editor.language.clone().unwrap_or_else(|| "plaintext".to_string());
    
    state.autocomplete.get_completions(&language, &prefix)
        .iter()
        .map(|c| FfiCompletionItem::from(c))
        .collect()
}

/// Check if document is modified
pub fn ffi_is_modified() -> bool {
    let state = EDITOR_STATE.lock().unwrap();
    state.editor.buffer.is_modified()
}

/// Save document
pub fn ffi_save() -> FfiResult {
    let state = EDITOR_STATE.lock().unwrap();
    
    match state.editor.buffer.save() {
        Ok(_) => FfiResult {
            success: true,
            message: "Document saved".to_string(),
        },
        Err(e) => FfiResult {
            success: false,
            message: format!("Failed to save: {}", e),
        },
    }
}

/// Save document as
pub fn ffi_save_as(path: String) -> FfiResult {
    let mut state = EDITOR_STATE.lock().unwrap();
    
    match state.editor.buffer.save_as(std::path::Path::new(&path)) {
        Ok(_) => FfiResult {
            success: true,
            message: format!("Saved to: {}", path),
        },
        Err(e) => FfiResult {
            success: false,
            message: format!("Failed to save: {}", e),
        },
    }
}

// ============================================================================
// FFI Data Types
// ============================================================================

/// Result type for FFI operations
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct FfiResult {
    pub success: bool,
    pub message: String,
}

/// Cursor position for FFI
#[derive(Debug, Clone, Copy, Serialize, Deserialize)]
pub struct FfiCursorPosition {
    pub line: usize,
    pub column: usize,
}

impl From<Cursor> for FfiCursorPosition {
    fn from(cursor: Cursor) -> Self {
        FfiCursorPosition {
            line: cursor.line,
            column: cursor.column,
        }
    }
}

/// Selection for FFI
#[derive(Debug, Clone, Copy, Serialize, Deserialize)]
pub struct FfiSelection {
    pub start_line: usize,
    pub start_column: usize,
    pub end_line: usize,
    pub end_column: usize,
}

impl Default for FfiSelection {
    fn default() -> Self {
        FfiSelection {
            start_line: 0,
            start_column: 0,
            end_line: 0,
            end_column: 0,
        }
    }
}

impl From<Selection> for FfiSelection {
    fn from(sel: Selection) -> Self {
        FfiSelection {
            start_line: sel.start.line,
            start_column: sel.start.column,
            end_line: sel.end.line,
            end_column: sel.end.column,
        }
    }
}

/// Movement direction for FFI
#[derive(Debug, Clone, Copy, Serialize, Deserialize)]
pub enum FfiMoveDirection {
    Left,
    Right,
    Up,
    Down,
    Home,
    End,
    PageUp,
    PageDown,
    DocumentStart,
    DocumentEnd,
}

/// Line for FFI
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct FfiLine {
    pub line_number: usize,
    pub text: String,
}

/// Highlight token for FFI
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct FfiHighlightToken {
    pub token_type: String,
    pub start: usize,
    pub end: usize,
    pub start_line: usize,
    pub start_column: usize,
    pub end_line: usize,
    pub end_column: usize,
    pub color: String,
}

impl From<&HighlightToken> for FfiHighlightToken {
    fn from(token: &HighlightToken) -> Self {
        FfiHighlightToken {
            token_type: token.token_type.css_class().to_string(),
            start: token.start,
            end: token.end,
            start_line: token.start_line,
            start_column: token.start_column,
            end_line: token.end_line,
            end_column: token.end_column,
            color: String::new(), // Will be filled by theme
        }
    }
}

/// Search result for FFI
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct FfiSearchResult {
    pub line: usize,
    pub start_column: usize,
    pub end_column: usize,
    pub matched_text: String,
    pub context: String,
}

impl From<&SearchResult> for FfiSearchResult {
    fn from(result: &SearchResult) -> Self {
        FfiSearchResult {
            line: result.line,
            start_column: result.start_column,
            end_column: result.end_column,
            matched_text: result.matched_text.clone(),
            context: result.context.clone(),
        }
    }
}

/// Completion item for FFI
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct FfiCompletionItem {
    pub label: String,
    pub kind: i32,
    pub detail: Option<String>,
    pub documentation: Option<String>,
    pub insert_text: Option<String>,
}

impl From<&CompletionItem> for FfiCompletionItem {
    fn from(item: &CompletionItem) -> Self {
        FfiCompletionItem {
            label: item.label.clone(),
            kind: item.kind as i32,
            detail: item.detail.clone(),
            documentation: item.documentation.clone(),
            insert_text: item.insert_text.clone(),
        }
    }
}

/// Syntax theme for FFI
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct FfiSyntaxTheme {
    pub name: String,
    pub background: String,
    pub foreground: String,
    pub cursor: String,
    pub selection: String,
    pub line_number: String,
    pub line_number_active: String,
    pub token_colors: Vec<(String, String)>,
}

impl From<&SyntaxTheme> for FfiSyntaxTheme {
    fn from(theme: &SyntaxTheme) -> Self {
        FfiSyntaxTheme {
            name: theme.name.clone(),
            background: theme.background.clone(),
            foreground: theme.foreground.clone(),
            cursor: theme.cursor.clone(),
            selection: theme.selection.clone(),
            line_number: theme.line_number.clone(),
            line_number_active: theme.line_number_active.clone(),
            token_colors: theme.token_colors.iter()
                .map(|(k, v)| (k.clone(), v.clone()))
                .collect(),
        }
    }
}

// Helper function to generate UUID-like strings
fn uuid_string() -> String {
    use std::time::{SystemTime, UNIX_EPOCH};
    let duration = SystemTime::now()
        .duration_since(UNIX_EPOCH)
        .unwrap();
    format!("{:?}", duration)
}

#[cfg(test)]
mod tests {
    use super::*;
    
    #[test]
    fn test_ffi_init() {
        let result = ffi_init();
        assert!(result.contains("initialized"));
    }
    
    #[test]
    fn test_ffi_new_document() {
        let result = ffi_new_document();
        assert!(result.success);
    }
    
    #[test]
    fn test_ffi_set_get_text() {
        ffi_new_document();
        ffi_set_text("Hello, World!".to_string());
        let text = ffi_get_text();
        assert_eq!(text, "Hello, World!");
    }
    
    #[test]
    fn test_ffi_cursor() {
        ffi_new_document();
        ffi_set_text("Hello\nWorld".to_string());
        
        let cursor = ffi_get_cursor();
        assert_eq!(cursor.line, 0);
        assert_eq!(cursor.column, 0);
        
        let cursor = ffi_move_cursor(FfiMoveDirection::Down);
        assert_eq!(cursor.line, 1);
    }
}