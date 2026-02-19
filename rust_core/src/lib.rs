//! Koder Core - Blazing fast code editor engine
//! 
//! This library provides the core functionality for the Koder code editor,
//! including text buffer management, syntax highlighting, and LSP support.

pub mod editor;
pub mod syntax;
pub mod search;
pub mod lsp;
pub mod ffi;

// Re-export main types for convenience
pub use editor::{TextBuffer, Editor, Cursor, Selection};
pub use syntax::{SyntaxEngine, HighlightToken, Language};
pub use search::{SearchEngine, SearchResult, SearchOptions};
pub use lsp::LspClient;

/// Initialize the koder core library
pub fn init() {
    env_logger::init();
    log::info!("Koder Core initialized");
}