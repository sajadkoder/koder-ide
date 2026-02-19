//! Text buffer and editor operations
//! 
//! Uses ropey for efficient text manipulation with O(log n) operations.

use ropey::Rope;
use serde::{Deserialize, Serialize};
use std::path::PathBuf;

/// A cursor position in the document
#[derive(Debug, Clone, Copy, Serialize, Deserialize, PartialEq, Eq)]
pub struct Cursor {
    /// Line number (0-indexed)
    pub line: usize,
    /// Column number (0-indexed)
    pub column: usize,
}

impl Default for Cursor {
    fn default() -> Self {
        Cursor { line: 0, column: 0 }
    }
}

/// A text selection range
#[derive(Debug, Clone, Copy, Serialize, Deserialize, PartialEq, Eq)]
pub struct Selection {
    pub start: Cursor,
    pub end: Cursor,
}

impl Selection {
    pub fn new(start: Cursor, end: Cursor) -> Self {
        Selection { start, end }
    }
    
    pub fn is_empty(&self) -> bool {
        self.start == self.end
    }
    
    pub fn normalized(&self) -> Selection {
        if self.start.line > self.end.line 
            || (self.start.line == self.end.line && self.start.column > self.end.column) {
            Selection::new(self.end, self.start)
        } else {
            *self
        }
    }
}

/// Text buffer using rope data structure for efficient editing
#[derive(Debug, Clone)]
pub struct TextBuffer {
    rope: Rope,
    file_path: Option<PathBuf>,
    modified: bool,
    encoding: StringEncoding,
}

#[derive(Debug, Clone, Copy, Serialize, Deserialize, PartialEq, Eq)]
pub enum StringEncoding {
    Utf8,
    Utf16,
    Latin1,
}

impl Default for StringEncoding {
    fn default() -> Self {
        StringEncoding::Utf8
    }
}

impl TextBuffer {
    /// Create a new empty text buffer
    pub fn new() -> Self {
        TextBuffer {
            rope: Rope::new(),
            file_path: None,
            modified: false,
            encoding: StringEncoding::Utf8,
        }
    }
    
    /// Create a text buffer from a string
    pub fn from_text(text: &str) -> Self {
        TextBuffer {
            rope: Rope::from(text),
            file_path: None,
            modified: false,
            encoding: StringEncoding::Utf8,
        }
    }
    
    /// Load text buffer from a file
    pub fn from_file(path: &std::path::Path) -> std::io::Result<Self> {
        let text = std::fs::read_to_string(path)?;
        Ok(TextBuffer {
            rope: Rope::from(text),
            file_path: Some(path.to_path_buf()),
            modified: false,
            encoding: StringEncoding::Utf8,
        })
    }
    
    /// Save text buffer to file
    pub fn save(&self) -> std::io::Result<()> {
        if let Some(path) = &self.file_path {
            std::fs::write(path, self.rope.to_string())?;
        }
        Ok(())
    }
    
    /// Save to a specific path
    pub fn save_as(&mut self, path: &std::path::Path) -> std::io::Result<()> {
        std::fs::write(path, self.rope.to_string())?;
        self.file_path = Some(path.to_path_buf());
        self.modified = false;
        Ok(())
    }
    
    /// Get the total number of lines
    pub fn line_count(&self) -> usize {
        self.rope.len_lines()
    }
    
    /// Get the total number of characters
    pub fn char_count(&self) -> usize {
        self.rope.len_chars()
    }
    
    /// Get a specific line (0-indexed)
    pub fn get_line(&self, line: usize) -> Option<String> {
        if line < self.rope.len_lines() {
            Some(self.rope.line(line).to_string())
        } else {
            None
        }
    }
    
    /// Get text in a range
    pub fn get_text_range(&self, start: Cursor, end: Cursor) -> String {
        let start_char = self.cursor_to_char(start);
        let end_char = self.cursor_to_char(end);
        self.rope.slice(start_char..end_char).to_string()
    }
    
    /// Get the entire text content
    pub fn get_text(&self) -> String {
        self.rope.to_string()
    }
    
    /// Insert text at a cursor position
    pub fn insert(&mut self, cursor: Cursor, text: &str) -> Cursor {
        let char_idx = self.cursor_to_char(cursor);
        self.rope.insert(char_idx, text);
        self.modified = true;
        
        // Calculate new cursor position
        let lines: Vec<&str> = text.lines().collect();
        if lines.len() <= 1 {
            Cursor {
                line: cursor.line,
                column: cursor.column + text.chars().count(),
            }
        } else {
            Cursor {
                line: cursor.line + lines.len() - 1,
                column: lines.last().map(|l| l.chars().count()).unwrap_or(0),
            }
        }
    }
    
    /// Delete text in a range
    pub fn delete(&mut self, start: Cursor, end: Cursor) {
        let start_char = self.cursor_to_char(start);
        let end_char = self.cursor_to_char(end);
        if start_char < end_char {
            self.rope.remove(start_char..end_char);
            self.modified = true;
        }
    }
    
    /// Delete a character at cursor (backspace behavior)
    pub fn delete_char_before(&mut self, cursor: Cursor) -> Cursor {
        if cursor.column > 0 {
            let new_cursor = Cursor {
                line: cursor.line,
                column: cursor.column - 1,
            };
            self.delete(new_cursor, cursor);
            new_cursor
        } else if cursor.line > 0 {
            // Join with previous line
            let prev_line_len = self.get_line(cursor.line - 1).map(|l| l.chars().count() - 1).unwrap_or(0);
            let new_cursor = Cursor {
                line: cursor.line - 1,
                column: prev_line_len,
            };
            let start_char = self.cursor_to_char(new_cursor);
            let end_char = self.cursor_to_char(cursor);
            self.rope.remove(start_char..end_char);
            self.modified = true;
            new_cursor
        } else {
            cursor
        }
    }
    
    /// Delete a character after cursor (delete key behavior)
    pub fn delete_char_after(&mut self, cursor: Cursor) -> Cursor {
        let end_char = self.cursor_to_char(cursor) + 1;
        if end_char <= self.rope.len_chars() {
            let start_char = self.cursor_to_char(cursor);
            self.rope.remove(start_char..end_char);
            self.modified = true;
        }
        cursor
    }
    
    /// Insert a newline at cursor
    pub fn insert_newline(&mut self, cursor: Cursor) -> Cursor {
        let char_idx = self.cursor_to_char(cursor);
        self.rope.insert(char_idx, "\n");
        self.modified = true;
        Cursor {
            line: cursor.line + 1,
            column: 0,
        }
    }
    
    /// Convert cursor position to character index
    pub fn cursor_to_char(&self, cursor: Cursor) -> usize {
        let line_start = self.rope.line_to_char(cursor.line);
        let line_len = self.rope.line(cursor.line).len_chars();
        line_start + cursor.column.min(line_len.saturating_sub(1))
    }
    
    /// Convert character index to cursor position
    pub fn char_to_cursor(&self, char_idx: usize) -> Cursor {
        let line = self.rope.char_to_line(char_idx);
        let line_start = self.rope.line_to_char(line);
        Cursor {
            line,
            column: char_idx - line_start,
        }
    }
    
    /// Check if buffer has been modified
    pub fn is_modified(&self) -> bool {
        self.modified
    }
    
    /// Get the file path
    pub fn file_path(&self) -> Option<&PathBuf> {
        self.file_path.as_ref()
    }
    
    /// Set modified flag
    pub fn set_modified(&mut self, modified: bool) {
        self.modified = modified;
    }
    
    /// Get visible lines for rendering (with line numbers)
    pub fn get_visible_lines(&self, start_line: usize, count: usize) -> Vec<(usize, String)> {
        let mut lines = Vec::with_capacity(count);
        for i in start_line..(start_line + count).min(self.line_count()) {
            if let Some(line) = self.get_line(i) {
                lines.push((i + 1, line)); // 1-indexed line numbers
            }
        }
        lines
    }
}

impl Default for TextBuffer {
    fn default() -> Self {
        Self::new()
    }
}

/// Main editor state
#[derive(Debug, Clone)]
pub struct Editor {
    pub buffer: TextBuffer,
    pub cursor: Cursor,
    pub selection: Option<Selection>,
    pub scroll_offset: ScrollOffset,
    pub language: Option<String>,
}

#[derive(Debug, Clone, Copy, Serialize, Deserialize, PartialEq)]
pub struct ScrollOffset {
    pub x: f64,
    pub y: f64,
}

impl Default for ScrollOffset {
    fn default() -> Self {
        ScrollOffset { x: 0.0, y: 0.0 }
    }
}

impl Editor {
    /// Create a new editor
    pub fn new() -> Self {
        Editor {
            buffer: TextBuffer::new(),
            cursor: Cursor::default(),
            selection: None,
            scroll_offset: ScrollOffset::default(),
            language: None,
        }
    }
    
    /// Create editor with text
    pub fn with_text(text: &str) -> Self {
        Editor {
            buffer: TextBuffer::from_text(text),
            cursor: Cursor::default(),
            selection: None,
            scroll_offset: ScrollOffset::default(),
            language: None,
        }
    }
    
    /// Create editor from file
    pub fn from_file(path: &std::path::Path) -> std::io::Result<Self> {
        let buffer = TextBuffer::from_file(path)?;
        let language = detect_language_from_path(path);
        Ok(Editor {
            buffer,
            cursor: Cursor::default(),
            selection: None,
            scroll_offset: ScrollOffset::default(),
            language,
        })
    }
    
    /// Insert text at cursor
    pub fn insert(&mut self, text: &str) {
        // Delete selection if present
        if let Some(sel) = self.selection {
            let sel = sel.normalized();
            self.buffer.delete(sel.start, sel.end);
            self.cursor = sel.start;
            self.selection = None;
        }
        
        self.cursor = self.buffer.insert(self.cursor, text);
    }
    
    /// Move cursor
    pub fn move_cursor(&mut self, direction: MoveDirection) {
        if let Some(sel) = self.selection.take() {
            // If there was a selection, move to appropriate end
            match direction {
                MoveDirection::Left | MoveDirection::Up => {
                    self.cursor = sel.normalized().start;
                }
                MoveDirection::Right | MoveDirection::Down => {
                    self.cursor = sel.normalized().end;
                }
                _ => {}
            }
            return;
        }
        
        match direction {
            MoveDirection::Left => {
                if self.cursor.column > 0 {
                    self.cursor.column -= 1;
                } else if self.cursor.line > 0 {
                    self.cursor.line -= 1;
                    self.cursor.column = self.buffer.get_line(self.cursor.line)
                        .map(|l| l.chars().count().saturating_sub(1))
                        .unwrap_or(0);
                }
            }
            MoveDirection::Right => {
                let line_len = self.buffer.get_line(self.cursor.line)
                    .map(|l| l.chars().count().saturating_sub(1))
                    .unwrap_or(0);
                if self.cursor.column < line_len {
                    self.cursor.column += 1;
                } else if self.cursor.line < self.buffer.line_count() - 1 {
                    self.cursor.line += 1;
                    self.cursor.column = 0;
                }
            }
            MoveDirection::Up => {
                if self.cursor.line > 0 {
                    self.cursor.line -= 1;
                    self.cursor.column = self.cursor.column.min(
                        self.buffer.get_line(self.cursor.line)
                            .map(|l| l.chars().count().saturating_sub(1))
                            .unwrap_or(0)
                    );
                }
            }
            MoveDirection::Down => {
                if self.cursor.line < self.buffer.line_count() - 1 {
                    self.cursor.line += 1;
                    self.cursor.column = self.cursor.column.min(
                        self.buffer.get_line(self.cursor.line)
                            .map(|l| l.chars().count().saturating_sub(1))
                            .unwrap_or(0)
                    );
                }
            }
            MoveDirection::Home => {
                self.cursor.column = 0;
            }
            MoveDirection::End => {
                self.cursor.column = self.buffer.get_line(self.cursor.line)
                    .map(|l| l.chars().count().saturating_sub(1))
                    .unwrap_or(0);
            }
            MoveDirection::PageUp => {
                self.cursor.line = self.cursor.line.saturating_sub(20);
            }
            MoveDirection::PageDown => {
                self.cursor.line = (self.cursor.line + 20).min(self.buffer.line_count() - 1);
            }
            MoveDirection::DocumentStart => {
                self.cursor = Cursor::default();
            }
            MoveDirection::DocumentEnd => {
                self.cursor = Cursor {
                    line: self.buffer.line_count().saturating_sub(1),
                    column: 0,
                };
            }
        }
    }
    
    /// Start selection
    pub fn start_selection(&mut self) {
        self.selection = Some(Selection::new(self.cursor, self.cursor));
    }
    
    /// Extend selection in direction
    pub fn extend_selection(&mut self, direction: MoveDirection) {
        if self.selection.is_none() {
            self.start_selection();
        }
        
        // Move cursor
        self.move_cursor(direction);
        
        // Update selection end
        if let Some(sel) = &mut self.selection {
            sel.end = self.cursor;
        }
    }
    
    /// Select all text
    pub fn select_all(&mut self) {
        let end = Cursor {
            line: self.buffer.line_count().saturating_sub(1),
            column: self.buffer.get_line(self.buffer.line_count().saturating_sub(1))
                .map(|l| l.chars().count())
                .unwrap_or(0),
        };
        self.selection = Some(Selection::new(Cursor::default(), end));
        self.cursor = end;
    }
    
    /// Delete selected text or character
    pub fn delete(&mut self) {
        if let Some(sel) = self.selection {
            let sel = sel.normalized();
            self.buffer.delete(sel.start, sel.end);
            self.cursor = sel.start;
            self.selection = None;
        } else {
            self.cursor = self.buffer.delete_char_before(self.cursor);
        }
    }
    
    /// Get selected text
    pub fn get_selected_text(&self) -> Option<String> {
        self.selection.map(|sel| {
            let sel = sel.normalized();
            self.buffer.get_text_range(sel.start, sel.end)
        })
    }
    
    /// Undo last operation (placeholder - would need history tracking)
    pub fn undo(&mut self) {
        // TODO: Implement undo history
    }
    
    /// Redo last undone operation (placeholder)
    pub fn redo(&mut self) {
        // TODO: Implement redo history
    }
}

impl Default for Editor {
    fn default() -> Self {
        Self::new()
    }
}

/// Movement directions for cursor
#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub enum MoveDirection {
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

/// Detect language from file path
fn detect_language_from_path(path: &std::path::Path) -> Option<String> {
    let ext = path.extension()?.to_str()?.to_lowercase();
    let lang = match ext.as_str() {
        "js" => "javascript",
        "jsx" => "javascript",
        "ts" => "typescript",
        "tsx" => "typescript",
        "py" => "python",
        "rs" => "rust",
        "c" => "c",
        "cpp" | "cc" | "cxx" => "cpp",
        "h" | "hpp" => "cpp",
        "go" => "go",
        "java" => "java",
        "kt" | "kts" => "kotlin",
        "swift" => "swift",
        "dart" => "dart",
        "rb" => "ruby",
        "lua" => "lua",
        "html" | "htm" => "html",
        "css" => "css",
        "scss" | "sass" => "css",
        "json" => "json",
        "yaml" | "yml" => "yaml",
        "toml" => "toml",
        "xml" => "xml",
        "md" => "markdown",
        "sh" | "bash" => "bash",
        "ps1" => "powershell",
        _ => return None,
    };
    Some(lang.to_string())
}

#[cfg(test)]
mod tests {
    use super::*;
    
    #[test]
    fn test_text_buffer_creation() {
        let buffer = TextBuffer::new();
        assert_eq!(buffer.line_count(), 1);
        assert_eq!(buffer.char_count(), 0);
    }
    
    #[test]
    fn test_text_buffer_from_text() {
        let buffer = TextBuffer::from_text("Hello\nWorld");
        assert_eq!(buffer.line_count(), 2);
        assert_eq!(buffer.char_count(), 11);
    }
    
    #[test]
    fn test_insert_text() {
        let mut buffer = TextBuffer::new();
        let cursor = buffer.insert(Cursor::default(), "Hello");
        assert_eq!(cursor.line, 0);
        assert_eq!(cursor.column, 5);
        assert_eq!(buffer.get_text(), "Hello");
    }
    
    #[test]
    fn test_cursor_movement() {
        let mut editor = Editor::with_text("Hello\nWorld");
        editor.move_cursor(MoveDirection::Right);
        assert_eq!(editor.cursor.column, 1);
        editor.move_cursor(MoveDirection::Down);
        assert_eq!(editor.cursor.line, 1);
        assert_eq!(editor.cursor.column, 1);
    }
}