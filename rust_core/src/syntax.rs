//! Syntax highlighting using tree-sitter
//! 
//! Provides incremental parsing and syntax highlighting for multiple languages.

use serde::{Deserialize, Serialize};
use std::collections::HashMap;
use tree_sitter::{Node, Parser, Point, Tree, TreeCursor};

/// Supported programming languages
#[derive(Debug, Clone, Copy, PartialEq, Eq, Hash, Serialize, Deserialize)]
pub enum Language {
    JavaScript,
    TypeScript,
    Python,
    Rust,
    C,
    Cpp,
    Go,
    Java,
    Kotlin,
    Swift,
    Dart,
    Ruby,
    Lua,
    Html,
    Css,
    Json,
    Yaml,
    Toml,
    Xml,
    Markdown,
    Bash,
    PlainText,
}

impl Language {
    /// Get language from file extension
    pub fn from_extension(ext: &str) -> Self {
        match ext.to_lowercase().as_str() {
            "js" | "jsx" | "mjs" | "cjs" => Language::JavaScript,
            "ts" | "tsx" => Language::TypeScript,
            "py" | "pyw" => Language::Python,
            "rs" => Language::Rust,
            "c" => Language::C,
            "cpp" | "cc" | "cxx" | "hpp" | "h" => Language::Cpp,
            "go" => Language::Go,
            "java" => Language::Java,
            "kt" | "kts" => Language::Kotlin,
            "swift" => Language::Swift,
            "dart" => Language::Dart,
            "rb" | "rbw" => Language::Ruby,
            "lua" => Language::Lua,
            "html" | "htm" => Language::Html,
            "css" | "scss" | "sass" | "less" => Language::Css,
            "json" => Language::Json,
            "yaml" | "yml" => Language::Yaml,
            "toml" => Language::Toml,
            "xml" => Language::Xml,
            "md" | "markdown" => Language::Markdown,
            "sh" | "bash" | "zsh" => Language::Bash,
            _ => Language::PlainText,
        }
    }
    
    /// Get language name as string
    pub fn name(&self) -> &'static str {
        match self {
            Language::JavaScript => "JavaScript",
            Language::TypeScript => "TypeScript",
            Language::Python => "Python",
            Language::Rust => "Rust",
            Language::C => "C",
            Language::Cpp => "C++",
            Language::Go => "Go",
            Language::Java => "Java",
            Language::Kotlin => "Kotlin",
            Language::Swift => "Swift",
            Language::Dart => "Dart",
            Language::Ruby => "Ruby",
            Language::Lua => "Lua",
            Language::Html => "HTML",
            Language::Css => "CSS",
            Language::Json => "JSON",
            Language::Yaml => "YAML",
            Language::Toml => "TOML",
            Language::Xml => "XML",
            Language::Markdown => "Markdown",
            Language::Bash => "Bash",
            Language::PlainText => "Plain Text",
        }
    }
}

/// Syntax token types for highlighting
#[derive(Debug, Clone, Copy, PartialEq, Eq, Hash, Serialize, Deserialize)]
pub enum TokenType {
    // Keywords
    Keyword,
    KeywordControl,
    KeywordOperator,
    KeywordDeclaration,
    
    // Literals
    String,
    StringEscape,
    Number,
    Boolean,
    Null,
    
    // Identifiers
    Identifier,
    Function,
    FunctionCall,
    Method,
    Variable,
    Constant,
    Property,
    Parameter,
    
    // Types
    Type,
    TypeBuiltin,
    Class,
    Struct,
    Enum,
    Interface,
    Trait,
    
    // Comments
    Comment,
    CommentLine,
    CommentBlock,
    CommentDoc,
    
    // Punctuation
    Punctuation,
    PunctuationBracket,
    PunctuationDelimiter,
    PunctuationSpecial,
    
    // Operators
    Operator,
    OperatorArithmetic,
    OperatorComparison,
    OperatorLogical,
    OperatorBitwise,
    
    // Markup
    Tag,
    TagName,
    TagAttribute,
    TagDelimiter,
    
    // Special
    Preprocessor,
    Macro,
    Annotation,
    Label,
    
    // Default
    None,
}

impl TokenType {
    /// Get the CSS class name for this token type
    pub fn css_class(&self) -> &'static str {
        match self {
            TokenType::Keyword | TokenType::KeywordControl | TokenType::KeywordOperator | TokenType::KeywordDeclaration => "token-keyword",
            TokenType::String => "token-string",
            TokenType::StringEscape => "token-string-escape",
            TokenType::Number => "token-number",
            TokenType::Boolean | TokenType::Null => "token-constant",
            TokenType::Function | TokenType::FunctionCall | TokenType::Method => "token-function",
            TokenType::Variable | TokenType::Parameter => "token-variable",
            TokenType::Constant => "token-constant",
            TokenType::Property => "token-property",
            TokenType::Type | TokenType::TypeBuiltin | TokenType::Class | TokenType::Struct | TokenType::Enum | TokenType::Interface | TokenType::Trait => "token-type",
            TokenType::Comment | TokenType::CommentLine | TokenType::CommentBlock | TokenType::CommentDoc => "token-comment",
            TokenType::Punctuation | TokenType::PunctuationBracket | TokenType::PunctuationDelimiter | TokenType::PunctuationSpecial => "token-punctuation",
            TokenType::Operator | TokenType::OperatorArithmetic | TokenType::OperatorComparison | TokenType::OperatorLogical | TokenType::OperatorBitwise => "token-operator",
            TokenType::Tag | TokenType::TagName | TokenType::TagAttribute | TokenType::TagDelimiter => "token-tag",
            TokenType::Preprocessor | TokenType::Macro | TokenType::Annotation => "token-macro",
            TokenType::Identifier => "token-identifier",
            TokenType::Label => "token-label",
            TokenType::None => "token-none",
        }
    }
}

/// A highlighted token with position and type
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct HighlightToken {
    /// Token type for styling
    pub token_type: TokenType,
    /// Start byte position
    pub start: usize,
    /// End byte position
    pub end: usize,
    /// Start line (0-indexed)
    pub start_line: usize,
    /// Start column (0-indexed)
    pub start_column: usize,
    /// End line (0-indexed)
    pub end_line: usize,
    /// End column (0-indexed)
    pub end_column: usize,
}

/// Syntax highlighting engine
pub struct SyntaxEngine {
    parser: Parser,
    trees: HashMap<String, Tree>,
    current_language: Option<Language>,
}

impl SyntaxEngine {
    /// Create a new syntax engine
    pub fn new() -> Self {
        SyntaxEngine {
            parser: Parser::new(),
            trees: HashMap::new(),
            current_language: None,
        }
    }
    
    /// Set the language for parsing
    pub fn set_language(&mut self, language: Language) -> Result<(), String> {
        let language_fn = match language {
            Language::JavaScript => tree_sitter_javascript::language,
            Language::TypeScript => tree_sitter_typescript::language_typescript,
            Language::Python => tree_sitter_python::language,
            Language::Rust => tree_sitter_rust::language,
            Language::C => tree_sitter_c::language,
            Language::Cpp => tree_sitter_cpp::language,
            Language::Go => tree_sitter_go::language,
            Language::Java => tree_sitter_java::language,
            Language::Kotlin => tree_sitter_kotlin::language,
            Language::Swift => tree_sitter_swift::language,
            Language::Dart => tree_sitter_dart::language,
            Language::Ruby => tree_sitter_ruby::language,
            Language::Lua => tree_sitter_lua::language,
            Language::Html => tree_sitter_html::language,
            Language::Css => tree_sitter_css::language,
            Language::Json => tree_sitter_json::language,
            Language::Yaml => tree_sitter_yaml::language,
            Language::Toml => tree_sitter_toml::language,
            Language::Xml => tree_sitter_xml::language,
            Language::Markdown => tree_sitter_markdown::language,
            Language::Bash => tree_sitter_bash::language,
            Language::PlainText => return Ok(()),
        };
        
        self.parser.set_language(language_fn())
            .map_err(|e| format!("Failed to set language: {}", e))?;
        self.current_language = Some(language);
        Ok(())
    }
    
    /// Parse text and return syntax tree
    pub fn parse(&mut self, text: &str, file_id: &str) -> Option<Vec<HighlightToken>> {
        if self.current_language == Some(Language::PlainText) {
            return None;
        }
        
        let tree = self.parser.parse(text, self.trees.get(file_id))?;
        let tokens = self.extract_tokens(&tree, text);
        self.trees.insert(file_id.to_string(), tree);
        Some(tokens)
    }
    
    /// Parse with incremental update
    pub fn parse_incremental(&mut self, text: &str, file_id: &str, edit: &Edit) -> Option<Vec<HighlightToken>> {
        if self.current_language == Some(Language::PlainText) {
            return None;
        }
        
        // Apply edit to existing tree if present
        if let Some(old_tree) = self.trees.get_mut(file_id) {
            let input_edit = tree_sitter::InputEdit {
                start_byte: edit.start_byte,
                old_end_byte: edit.old_end_byte,
                new_end_byte: edit.new_end_byte,
                start_position: Point::new(edit.start_line, edit.start_column),
                old_end_position: Point::new(edit.old_end_line, edit.old_end_column),
                new_end_position: Point::new(edit.new_end_line, edit.new_end_column),
            };
            old_tree.edit(&input_edit);
        }
        
        self.parse(text, file_id)
    }
    
    /// Extract highlight tokens from syntax tree
    fn extract_tokens(&self, tree: &Tree, source: &str) -> Vec<HighlightToken> {
        let mut tokens = Vec::new();
        let mut cursor = tree.walk();
        
        self.walk_tree(&mut cursor, source, &mut tokens);
        
        tokens
    }
    
    /// Walk the syntax tree and extract tokens
    fn walk_tree(&self, cursor: &mut TreeCursor, source: &str, tokens: &mut Vec<HighlightToken>) {
        loop {
            let node = cursor.node();
            
            if let Some(token) = self.node_to_token(&node, source) {
                tokens.push(token);
            }
            
            // Recurse into children
            if cursor.goto_first_child() {
                self.walk_tree(cursor, source, tokens);
                cursor.goto_parent();
            }
            
            if !cursor.goto_next_sibling() {
                break;
            }
        }
    }
    
    /// Convert a tree-sitter node to a highlight token
    fn node_to_token(&self, node: &Node, source: &str) -> Option<HighlightToken> {
        let kind = node.kind();
        let token_type = self.map_kind_to_token_type(kind, node);
        
        if token_type == TokenType::None {
            return None;
        }
        
        let start = node.start_byte();
        let end = node.end_byte();
        let start_pos = node.start_position();
        let end_pos = node.end_position();
        
        Some(HighlightToken {
            token_type,
            start,
            end,
            start_line: start_pos.row,
            start_column: start_pos.column,
            end_line: end_pos.row,
            end_column: end_pos.column,
        })
    }
    
    /// Map tree-sitter node kind to token type
    fn map_kind_to_token_type(&self, kind: &str, node: &Node) -> TokenType {
        match kind {
            // Keywords
            "if" | "else" | "elif" | "switch" | "case" | "default" | "break" | "continue" | "return" | "yield" | "throw" | "try" | "catch" | "finally" | "for" | "while" | "do" | "loop" | "repeat" | "until" | "when" | "match" | "where" => TokenType::KeywordControl,
            "fn" | "function" | "func" | "def" | "fun" | "method" | "constructor" | "destructor" | "class" | "struct" | "enum" | "interface" | "trait" | "impl" | "type" | "typedef" | "namespace" | "module" | "import" | "export" | "from" | "use" | "require" | "include" | "package" | "extends" | "implements" | "with" => TokenType::KeywordDeclaration,
            "const" | "let" | "var" | "val" | "static" | "final" | "abstract" | "virtual" | "override" | "public" | "private" | "protected" | "internal" | "async" | "await" | "sync" | "mut" | "ref" | "out" | "in" => TokenType::Keyword,
            "new" | "delete" | "sizeof" | "typeof" | "instanceof" | "as" | "is" | "in" | "of" => TokenType::KeywordOperator,
            
            // Literals
            "string" | "string_literal" | "template_string" | "raw_string_literal" | "char_literal" => TokenType::String,
            "escape_sequence" | "escape_char" => TokenType::StringEscape,
            "number" | "integer" | "float" | "double" | "hex_integer_literal" | "octal_integer_literal" | "binary_integer_literal" | "number_literal" => TokenType::Number,
            "true" | "false" => TokenType::Boolean,
            "null" | "nil" | "None" | "undefined" | "void" => TokenType::Null,
            
            // Comments
            "comment" | "line_comment" | "single_line_comment" => TokenType::CommentLine,
            "block_comment" | "multi_line_comment" | "multiline_comment" => TokenType::CommentBlock,
            "doc_comment" | "documentation_comment" | "comment_block" => TokenType::CommentDoc,
            
            // Functions and methods
            "function_declaration" | "function_definition" | "method_definition" | "function_item" | "function_signature" | "arrow_function" | "generator_function" | "generator_function_declaration" => TokenType::Function,
            "call_expression" | "function_call" | "method_call" | "call_expression" => TokenType::FunctionCall,
            "method_identifier" | "method" => TokenType::Method,
            
            // Types
            "type_identifier" | "type_name" | "primitive_type" | "builtin_type" | "predefined_type" => TokenType::Type,
            "class_declaration" | "class_definition" | "class" => TokenType::Class,
            "struct_declaration" | "struct_definition" | "struct_item" => TokenType::Struct,
            "enum_declaration" | "enum_definition" | "enum_item" => TokenType::Enum,
            "interface_declaration" | "interface_definition" | "trait_item" => TokenType::Interface,
            
            // Variables and identifiers
            "identifier" | "identifier_name" | "variable_name" | "property_identifier" | "field_identifier" | "shorthand_property_identifier" => TokenType::Identifier,
            "variable_declaration" | "variable_declarator" | "lexical_declaration" | "variable_definition" | "let_declaration" | "var_declaration" | "const_declaration" | "val_declaration" => TokenType::Variable,
            "constant" | "constant_item" | "const_item" => TokenType::Constant,
            "property" | "property_name" | "field" | "member_expression" | "field_expression" | "property_signature" => TokenType::Property,
            "parameter" | "formal_parameter" | "parameter_name" | "required_parameter" | "optional_parameter" | "rest_parameter" => TokenType::Parameter,
            
            // Operators
            "+" | "-" | "*" | "/" | "%" | "**" | "//" => TokenType::OperatorArithmetic,
            "==" | "!=" | "<" | ">" | "<=" | ">=" | "===" | "!==" | "<=>" => TokenType::OperatorComparison,
            "&&" | "||" | "!" | "and" | "or" | "not" => TokenType::OperatorLogical,
            "&" | "|" | "^" | "~" | "<<" | ">>" | ">>>" => TokenType::OperatorBitwise,
            "=" | "+=" | "-=" | "*=" | "/=" | "%=" | "&=" | "|=" | "^=" | "<<=" | ">>=" => TokenType::Operator,
            
            // Punctuation
            "(" | ")" | "[" | "]" | "{" | "}" | "<" | ">" => TokenType::PunctuationBracket,
            "," | ";" | ":" | "." | "->" | "=>" | "::" | "?" | "?." | "?:" | "!!" => TokenType::PunctuationDelimiter,
            
            // HTML/Markup
            "tag" | "tag_name" | "element" | "start_tag" | "end_tag" | "self_closing_tag" => TokenType::Tag,
            "attribute" | "attribute_name" | "attribute_value" => TokenType::TagAttribute,
            "<" | "</" | "/>" | ">" => TokenType::TagDelimiter,
            
            // Special
            "preproc" | "preproc_function" | "preproc_call" | "preproc_directive" | "preprocessor_directive" | "#define" | "#include" | "#ifdef" | "#ifndef" | "#if" | "#else" | "#elif" | "#endif" => TokenType::Preprocessor,
            "macro" | "macro_definition" | "macro_invocation" | "macro_use" => TokenType::Macro,
            "annotation" | "decorator" | "attribute_item" | "attribute" => TokenType::Annotation,
            "label" | "label_name" => TokenType::Label,
            
            _ => {
                // Check parent context for better classification
                if let Some(parent) = node.parent() {
                    match parent.kind() {
                        "call_expression" | "function_call" => TokenType::FunctionCall,
                        "member_expression" | "field_expression" => TokenType::Property,
                        "binary_expression" | "unary_expression" => TokenType::Operator,
                        _ => TokenType::None,
                    }
                } else {
                    TokenType::None
                }
            }
        }
    }
    
    /// Get the current language
    pub fn current_language(&self) -> Option<Language> {
        self.current_language
    }
    
    /// Clear cached syntax tree
    pub fn clear_cache(&mut self, file_id: &str) {
        self.trees.remove(file_id);
    }
    
    /// Clear all cached syntax trees
    pub fn clear_all_cache(&mut self) {
        self.trees.clear();
    }
}

impl Default for SyntaxEngine {
    fn default() -> Self {
        Self::new()
    }
}

/// Edit information for incremental parsing
#[derive(Debug, Clone)]
pub struct Edit {
    pub start_byte: usize,
    pub old_end_byte: usize,
    pub new_end_byte: usize,
    pub start_line: usize,
    pub start_column: usize,
    pub old_end_line: usize,
    pub old_end_column: usize,
    pub new_end_line: usize,
    pub new_end_column: usize,
}

/// Highlighted line for rendering
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct HighlightedLine {
    pub line_number: usize,
    pub text: String,
    pub tokens: Vec<HighlightToken>,
}

/// Syntax theme colors
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct SyntaxTheme {
    pub name: String,
    pub background: String,
    pub foreground: String,
    pub cursor: String,
    pub selection: String,
    pub line_number: String,
    pub line_number_active: String,
    pub token_colors: HashMap<String, String>,
}

impl SyntaxTheme {
    /// VS Code Dark+ theme
    pub fn vscode_dark() -> Self {
        let mut token_colors = HashMap::new();
        token_colors.insert("token-keyword".to_string(), "#569cd6".to_string());
        token_colors.insert("token-string".to_string(), "#ce9178".to_string());
        token_colors.insert("token-number".to_string(), "#b5cea8".to_string());
        token_colors.insert("token-constant".to_string(), "#4fc1ff".to_string());
        token_colors.insert("token-function".to_string(), "#dcdcaa".to_string());
        token_colors.insert("token-variable".to_string(), "#9cdcfe".to_string());
        token_colors.insert("token-type".to_string(), "#4ec9b0".to_string());
        token_colors.insert("token-class".to_string(), "#4ec9b0".to_string());
        token_colors.insert("token-comment".to_string(), "#6a9955".to_string());
        token_colors.insert("token-operator".to_string(), "#d4d4d4".to_string());
        token_colors.insert("token-punctuation".to_string(), "#d4d4d4".to_string());
        token_colors.insert("token-tag".to_string(), "#569cd6".to_string());
        token_colors.insert("token-property".to_string(), "#9cdcfe".to_string());
        token_colors.insert("token-macro".to_string(), "#c586c0".to_string());
        
        SyntaxTheme {
            name: "VS Code Dark+".to_string(),
            background: "#1e1e1e".to_string(),
            foreground: "#d4d4d4".to_string(),
            cursor: "#aeafad".to_string(),
            selection: "#264f78".to_string(),
            line_number: "#858585".to_string(),
            line_number_active: "#c6c6c6".to_string(),
            token_colors,
        }
    }
    
    /// Monokai theme
    pub fn monokai() -> Self {
        let mut token_colors = HashMap::new();
        token_colors.insert("token-keyword".to_string(), "#f92672".to_string());
        token_colors.insert("token-string".to_string(), "#e6db74".to_string());
        token_colors.insert("token-number".to_string(), "#ae81ff".to_string());
        token_colors.insert("token-constant".to_string(), "#ae81ff".to_string());
        token_colors.insert("token-function".to_string(), "#a6e22e".to_string());
        token_colors.insert("token-variable".to_string(), "#f8f8f2".to_string());
        token_colors.insert("token-type".to_string(), "#66d9ef".to_string());
        token_colors.insert("token-class".to_string(), "#66d9ef".to_string());
        token_colors.insert("token-comment".to_string(), "#75715e".to_string());
        token_colors.insert("token-operator".to_string(), "#f92672".to_string());
        token_colors.insert("token-punctuation".to_string(), "#f8f8f2".to_string());
        token_colors.insert("token-tag".to_string(), "#f92672".to_string());
        token_colors.insert("token-property".to_string(), "#a6e22e".to_string());
        token_colors.insert("token-macro".to_string(), "#c586c0".to_string());
        
        SyntaxTheme {
            name: "Monokai".to_string(),
            background: "#272822".to_string(),
            foreground: "#f8f8f2".to_string(),
            cursor: "#f8f8f0".to_string(),
            selection: "#49483e".to_string(),
            line_number: "#75715e".to_string(),
            line_number_active: "#f8f8f2".to_string(),
            token_colors,
        }
    }
    
    /// One Dark theme
    pub fn one_dark() -> Self {
        let mut token_colors = HashMap::new();
        token_colors.insert("token-keyword".to_string(), "#c678dd".to_string());
        token_colors.insert("token-string".to_string(), "#98c379".to_string());
        token_colors.insert("token-number".to_string(), "#d19a66".to_string());
        token_colors.insert("token-constant".to_string(), "#d19a66".to_string());
        token_colors.insert("token-function".to_string(), "#61afef".to_string());
        token_colors.insert("token-variable".to_string(), "#e06c75".to_string());
        token_colors.insert("token-type".to_string(), "#e5c07b".to_string());
        token_colors.insert("token-class".to_string(), "#e5c07b".to_string());
        token_colors.insert("token-comment".to_string(), "#5c6370".to_string());
        token_colors.insert("token-operator".to_string(), "#56b6c2".to_string());
        token_colors.insert("token-punctuation".to_string(), "#abb2bf".to_string());
        token_colors.insert("token-tag".to_string(), "#e06c75".to_string());
        token_colors.insert("token-property".to_string(), "#e06c75".to_string());
        token_colors.insert("token-macro".to_string(), "#c678dd".to_string());
        
        SyntaxTheme {
            name: "One Dark".to_string(),
            background: "#282c34".to_string(),
            foreground: "#abb2bf".to_string(),
            cursor: "#528bff".to_string(),
            selection: "#3e4451".to_string(),
            line_number: "#4b5263".to_string(),
            line_number_active: "#abb2bf".to_string(),
            token_colors,
        }
    }
    
    /// Get token color
    pub fn get_token_color(&self, token_type: &TokenType) -> Option<&String> {
        self.token_colors.get(token_type.css_class())
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    
    #[test]
    fn test_language_from_extension() {
        assert_eq!(Language::from_extension("js"), Language::JavaScript);
        assert_eq!(Language::from_extension("py"), Language::Python);
        assert_eq!(Language::from_extension("rs"), Language::Rust);
        assert_eq!(Language::from_extension("unknown"), Language::PlainText);
    }
    
    #[test]
    fn test_syntax_engine_creation() {
        let engine = SyntaxEngine::new();
        assert!(engine.current_language.is_none());
    }
    
    #[test]
    fn test_theme_creation() {
        let theme = SyntaxTheme::vscode_dark();
        assert_eq!(theme.name, "VS Code Dark+");
        assert!(theme.get_token_color(&TokenType::Keyword).is_some());
    }
}