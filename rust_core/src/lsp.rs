//! Language Server Protocol client support
//! 
//! Provides LSP integration for autocomplete, go-to-definition, etc.

use serde::{Deserialize, Serialize};
use std::collections::HashMap;

/// LSP client for communicating with language servers
pub struct LspClient {
    language: Option<String>,
    server_path: Option<String>,
    initialized: bool,
    capabilities: ServerCapabilities,
}

/// Server capabilities from LSP server
#[derive(Debug, Clone, Default, Serialize, Deserialize)]
pub struct ServerCapabilities {
    pub completion_provider: Option<CompletionOptions>,
    pub hover_provider: bool,
    pub definition_provider: bool,
    pub references_provider: bool,
    pub document_symbol_provider: bool,
    pub document_formatting_provider: bool,
    pub rename_provider: bool,
}

/// Completion options
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct CompletionOptions {
    pub resolve_provider: bool,
    pub trigger_characters: Vec<String>,
}

/// Completion item
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct CompletionItem {
    pub label: String,
    pub kind: CompletionItemKind,
    pub detail: Option<String>,
    pub documentation: Option<String>,
    pub insert_text: Option<String>,
    pub sort_text: Option<String>,
    pub filter_text: Option<String>,
}

/// Completion item kinds
#[derive(Debug, Clone, Copy, PartialEq, Eq, Serialize, Deserialize)]
pub enum CompletionItemKind {
    Text = 1,
    Method = 2,
    Function = 3,
    Constructor = 4,
    Field = 5,
    Variable = 6,
    Class = 7,
    Interface = 8,
    Module = 9,
    Property = 10,
    Unit = 11,
    Value = 12,
    Enum = 13,
    Keyword = 14,
    Snippet = 15,
    Color = 16,
    File = 17,
    Reference = 18,
    Folder = 19,
    EnumMember = 20,
    Constant = 21,
    Struct = 22,
    Event = 23,
    Operator = 24,
    TypeParameter = 25,
}

/// Position in document
#[derive(Debug, Clone, Copy, Serialize, Deserialize)]
pub struct Position {
    pub line: usize,
    pub character: usize,
}

/// Range in document
#[derive(Debug, Clone, Copy, Serialize, Deserialize)]
pub struct Range {
    pub start: Position,
    pub end: Position,
}

/// Location (for go-to-definition)
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct Location {
    pub uri: String,
    pub range: Range,
}

/// Diagnostic (error/warning)
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct Diagnostic {
    pub range: Range,
    pub severity: DiagnosticSeverity,
    pub code: Option<String>,
    pub source: Option<String>,
    pub message: String,
}

/// Diagnostic severity
#[derive(Debug, Clone, Copy, PartialEq, Eq, Serialize, Deserialize)]
pub enum DiagnosticSeverity {
    Error = 1,
    Warning = 2,
    Information = 3,
    Hint = 4,
}

/// Hover information
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct Hover {
    pub contents: String,
    pub range: Option<Range>,
}

/// Document symbol
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct DocumentSymbol {
    pub name: String,
    pub kind: SymbolKind,
    pub range: Range,
    pub selection_range: Range,
    pub children: Vec<DocumentSymbol>,
}

/// Symbol kinds
#[derive(Debug, Clone, Copy, PartialEq, Eq, Serialize, Deserialize)]
pub enum SymbolKind {
    File = 1,
    Module = 2,
    Namespace = 3,
    Package = 4,
    Class = 5,
    Method = 6,
    Property = 7,
    Field = 8,
    Constructor = 9,
    Enum = 10,
    Interface = 11,
    Function = 12,
    Variable = 13,
    Constant = 14,
    String = 15,
    Number = 16,
    Boolean = 17,
    Array = 18,
    Object = 19,
    Key = 20,
    Null = 21,
    EnumMember = 22,
    Struct = 23,
    Event = 24,
    Operator = 25,
    TypeParameter = 26,
}

/// Text edit
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct TextEdit {
    pub range: Range,
    pub new_text: String,
}

impl LspClient {
    /// Create a new LSP client
    pub fn new() -> Self {
        LspClient {
            language: None,
            server_path: None,
            initialized: false,
            capabilities: ServerCapabilities::default(),
        }
    }
    
    /// Initialize with a language server
    pub fn initialize(&mut self, language: &str, server_path: &str) -> Result<(), String> {
        self.language = Some(language.to_string());
        self.server_path = Some(server_path.to_string());
        
        // In a real implementation, this would start the language server process
        // and send the initialize request
        
        self.capabilities = ServerCapabilities {
            completion_provider: Some(CompletionOptions {
                resolve_provider: true,
                trigger_characters: vec![".".to_string(), "(".to_string()],
            }),
            hover_provider: true,
            definition_provider: true,
            references_provider: true,
            document_symbol_provider: true,
            document_formatting_provider: true,
            rename_provider: true,
        };
        
        self.initialized = true;
        Ok(())
    }
    
    /// Check if initialized
    pub fn is_initialized(&self) -> bool {
        self.initialized
    }
    
    /// Get server capabilities
    pub fn capabilities(&self) -> &ServerCapabilities {
        &self.capabilities
    }
    
    /// Request completions at position
    pub fn get_completions(
        &self,
        _file_path: &str,
        _text: &str,
        _position: Position,
    ) -> Result<Vec<CompletionItem>, String> {
        if !self.initialized {
            return Err("LSP client not initialized".to_string());
        }
        
        // In a real implementation, this would send a textDocument/completion request
        // For now, return empty completions
        Ok(vec![])
    }
    
    /// Request hover information
    pub fn get_hover(
        &self,
        _file_path: &str,
        _text: &str,
        _position: Position,
    ) -> Result<Option<Hover>, String> {
        if !self.initialized {
            return Err("LSP client not initialized".to_string());
        }
        
        // In a real implementation, this would send a textDocument/hover request
        Ok(None)
    }
    
    /// Request go-to-definition
    pub fn get_definition(
        &self,
        _file_path: &str,
        _text: &str,
        _position: Position,
    ) -> Result<Option<Location>, String> {
        if !self.initialized {
            return Err("LSP client not initialized".to_string());
        }
        
        // In a real implementation, this would send a textDocument/definition request
        Ok(None)
    }
    
    /// Request references
    pub fn get_references(
        &self,
        _file_path: &str,
        _text: &str,
        _position: Position,
    ) -> Result<Vec<Location>, String> {
        if !self.initialized {
            return Err("LSP client not initialized".to_string());
        }
        
        // In a real implementation, this would send a textDocument/references request
        Ok(vec![])
    }
    
    /// Request document symbols
    pub fn get_document_symbols(
        &self,
        _file_path: &str,
        _text: &str,
    ) -> Result<Vec<DocumentSymbol>, String> {
        if !self.initialized {
            return Err("LSP client not initialized".to_string());
        }
        
        // In a real implementation, this would send a textDocument/documentSymbol request
        Ok(vec![])
    }
    
    /// Request document formatting
    pub fn format_document(
        &self,
        _file_path: &str,
        _text: &str,
    ) -> Result<Vec<TextEdit>, String> {
        if !self.initialized {
            return Err("LSP client not initialized".to_string());
        }
        
        // In a real implementation, this would send a textDocument/formatting request
        Ok(vec![])
    }
    
    /// Shutdown the language server
    pub fn shutdown(&mut self) {
        if self.initialized {
            // In a real implementation, this would send the shutdown request
            self.initialized = false;
        }
    }
}

impl Default for LspClient {
    fn default() -> Self {
        Self::new()
    }
}

impl Drop for LspClient {
    fn drop(&mut self) {
        self.shutdown();
    }
}

/// Language server configurations
pub struct LanguageServerConfig {
    pub language: String,
    pub command: String,
    pub args: Vec<String>,
    pub initialization_options: Option<serde_json::Value>,
}

impl LanguageServerConfig {
    /// Get configurations for known languages
    pub fn get_config(language: &str) -> Option<Self> {
        match language {
            "rust" => Some(LanguageServerConfig {
                language: "rust".to_string(),
                command: "rust-analyzer".to_string(),
                args: vec![],
                initialization_options: None,
            }),
            "python" => Some(LanguageServerConfig {
                language: "python".to_string(),
                command: "pylsp".to_string(),
                args: vec![],
                initialization_options: None,
            }),
            "javascript" | "typescript" => Some(LanguageServerConfig {
                language: language.to_string(),
                command: "typescript-language-server".to_string(),
                args: vec!["--stdio".to_string()],
                initialization_options: None,
            }),
            "go" => Some(LanguageServerConfig {
                language: "go".to_string(),
                command: "gopls".to_string(),
                args: vec![],
                initialization_options: None,
            }),
            "c" | "cpp" => Some(LanguageServerConfig {
                language: language.to_string(),
                command: "clangd".to_string(),
                args: vec![],
                initialization_options: None,
            }),
            "java" => Some(LanguageServerConfig {
                language: "java".to_string(),
                command: "jdtls".to_string(),
                args: vec![],
                initialization_options: None,
            }),
            "dart" => Some(LanguageServerConfig {
                language: "dart".to_string(),
                command: "dart".to_string(),
                args: vec!["language-server".to_string()],
                initialization_options: None,
            }),
            _ => None,
        }
    }
}

/// Simple autocomplete provider (fallback when LSP is not available)
pub struct SimpleAutocomplete {
    keywords: HashMap<String, Vec<String>>,
}

impl SimpleAutocomplete {
    pub fn new() -> Self {
        let mut keywords = HashMap::new();
        
        // JavaScript/TypeScript keywords
        keywords.insert("javascript".to_string(), vec![
            "async", "await", "break", "case", "catch", "class", "const", "continue",
            "debugger", "default", "delete", "do", "else", "export", "extends", "false",
            "finally", "for", "function", "if", "import", "in", "instanceof", "let",
            "new", "null", "return", "static", "super", "switch", "this", "throw",
            "true", "try", "typeof", "undefined", "var", "void", "while", "with", "yield",
        ].iter().map(|s| s.to_string()).collect());
        
        keywords.insert("typescript".to_string(), keywords.get("javascript").unwrap().clone());
        
        // Python keywords
        keywords.insert("python".to_string(), vec![
            "and", "as", "assert", "async", "await", "break", "class", "continue",
            "def", "del", "elif", "else", "except", "False", "finally", "for",
            "from", "global", "if", "import", "in", "is", "lambda", "None",
            "nonlocal", "not", "or", "pass", "raise", "return", "True", "try",
            "while", "with", "yield",
        ].iter().map(|s| s.to_string()).collect());
        
        // Rust keywords
        keywords.insert("rust".to_string(), vec![
            "as", "async", "await", "break", "const", "continue", "crate", "dyn",
            "else", "enum", "extern", "false", "fn", "for", "if", "impl", "in",
            "let", "loop", "match", "mod", "move", "mut", "pub", "ref", "return",
            "self", "Self", "static", "struct", "super", "trait", "true", "type",
            "unsafe", "use", "where", "while",
        ].iter().map(|s| s.to_string()).collect());
        
        // Go keywords
        keywords.insert("go".to_string(), vec![
            "break", "case", "chan", "const", "continue", "default", "defer", "else",
            "fallthrough", "for", "func", "go", "goto", "if", "import", "interface",
            "map", "package", "range", "return", "select", "struct", "switch", "type", "var",
        ].iter().map(|s| s.to_string()).collect());
        
        // Java keywords
        keywords.insert("java".to_string(), vec![
            "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char",
            "class", "const", "continue", "default", "do", "double", "else", "enum",
            "extends", "final", "finally", "float", "for", "if", "implements", "import",
            "instanceof", "int", "interface", "long", "native", "new", "package", "private",
            "protected", "public", "return", "short", "static", "strictfp", "super", "switch",
            "synchronized", "this", "throw", "throws", "transient", "try", "void", "volatile", "while",
        ].iter().map(|s| s.to_string()).collect());
        
        // Dart keywords
        keywords.insert("dart".to_string(), vec![
            "abstract", "as", "assert", "async", "await", "break", "case", "catch",
            "class", "const", "continue", "covariant", "default", "deferred", "do", "dynamic",
            "else", "enum", "export", "extends", "extension", "external", "factory", "false",
            "final", "finally", "for", "Function", "get", "hide", "if", "implements",
            "import", "in", "interface", "is", "late", "library", "mixin", "new",
            "null", "on", "operator", "part", "required", "rethrow", "return", "set",
            "show", "static", "super", "switch", "this", "throw", "true", "try",
            "typedef", "var", "void", "while", "with", "yield",
        ].iter().map(|s| s.to_string()).collect());
        
        SimpleAutocomplete { keywords }
    }
    
    /// Get completions for a prefix
    pub fn get_completions(&self, language: &str, prefix: &str) -> Vec<CompletionItem> {
        let mut items = Vec::new();
        
        if let Some(lang_keywords) = self.keywords.get(language) {
            for keyword in lang_keywords {
                if keyword.starts_with(prefix) {
                    items.push(CompletionItem {
                        label: keyword.clone(),
                        kind: CompletionItemKind::Keyword,
                        detail: None,
                        documentation: None,
                        insert_text: Some(keyword.clone()),
                        sort_text: Some(keyword.clone()),
                        filter_text: Some(keyword.clone()),
                    });
                }
            }
        }
        
        items
    }
}

impl Default for SimpleAutocomplete {
    fn default() -> Self {
        Self::new()
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    
    #[test]
    fn test_lsp_client_creation() {
        let client = LspClient::new();
        assert!(!client.is_initialized());
    }
    
    #[test]
    fn test_simple_autocomplete() {
        let autocomplete = SimpleAutocomplete::new();
        let completions = autocomplete.get_completions("javascript", "as");
        
        assert!(!completions.is_empty());
        assert!(completions.iter().any(|c| c.label == "async"));
    }
    
    #[test]
    fn test_language_server_config() {
        let config = LanguageServerConfig::get_config("rust");
        assert!(config.is_some());
        assert_eq!(config.unwrap().command, "rust-analyzer");
    }
}