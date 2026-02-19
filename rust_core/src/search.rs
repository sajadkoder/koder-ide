//! Search and replace functionality
//! 
//! Provides fast regex-based search with multiple search modes.

use regex::{Regex, RegexBuilder};
use serde::{Deserialize, Serialize};

/// Search options for configuring search behavior
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct SearchOptions {
    /// Case-sensitive search
    pub case_sensitive: bool,
    /// Match whole words only
    pub whole_word: bool,
    /// Use regex pattern
    pub use_regex: bool,
    /// Search in selection only
    pub in_selection: bool,
    /// Maximum results to return
    pub max_results: usize,
}

impl Default for SearchOptions {
    fn default() -> Self {
        SearchOptions {
            case_sensitive: false,
            whole_word: false,
            use_regex: false,
            in_selection: false,
            max_results: 1000,
        }
    }
}

/// A single search result
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct SearchResult {
    /// Line number (0-indexed)
    pub line: usize,
    /// Start column (0-indexed)
    pub start_column: usize,
    /// End column (0-indexed)
    pub end_column: usize,
    /// The matched text
    pub matched_text: String,
    /// Surrounding context (line text)
    pub context: String,
    /// Start byte position
    pub start_byte: usize,
    /// End byte position
    pub end_byte: usize,
}

/// Search engine for text search operations
pub struct SearchEngine {
    options: SearchOptions,
    last_pattern: Option<String>,
    compiled_regex: Option<Regex>,
}

impl SearchEngine {
    /// Create a new search engine
    pub fn new() -> Self {
        SearchEngine {
            options: SearchOptions::default(),
            last_pattern: None,
            compiled_regex: None,
        }
    }
    
    /// Create with specific options
    pub fn with_options(options: SearchOptions) -> Self {
        SearchEngine {
            options,
            last_pattern: None,
            compiled_regex: None,
        }
    }
    
    /// Update search options
    pub fn set_options(&mut self, options: SearchOptions) {
        self.options = options;
        self.compiled_regex = None;
    }
    
    /// Get current options
    pub fn options(&self) -> &SearchOptions {
        &self.options
    }
    
    /// Compile a search pattern
    fn compile_pattern(&mut self, pattern: &str) -> Result<Regex, String> {
        if self.last_pattern.as_deref() == Some(pattern) {
            if let Some(ref regex) = self.compiled_regex {
                return Ok(regex.clone());
            }
        }
        
        let actual_pattern = if self.options.use_regex {
            pattern.to_string()
        } else {
            // Escape regex special characters for literal search
            regex::escape(pattern)
        };
        
        let final_pattern = if self.options.whole_word {
            format!(r"\b{}\b", actual_pattern)
        } else {
            actual_pattern
        };
        
        let regex = RegexBuilder::new(&final_pattern)
            .case_insensitive(!self.options.case_sensitive)
            .build()
            .map_err(|e| format!("Invalid pattern: {}", e))?;
        
        self.last_pattern = Some(pattern.to_string());
        self.compiled_regex = Some(regex.clone());
        
        Ok(regex)
    }
    
    /// Search in text and return all results
    pub fn search(&mut self, pattern: &str, text: &str) -> Result<Vec<SearchResult>, String> {
        let regex = self.compile_pattern(pattern)?;
        let mut results = Vec::new();
        
        // Calculate byte offsets for each line
        let line_offsets: Vec<usize> = std::iter::once(0)
            .chain(text.match_indices('\n').map(|(i, _)| i + 1))
            .collect();
        
        for (line_idx, line) in text.lines().enumerate() {
            for cap in regex.find_iter(line) {
                if results.len() >= self.options.max_results {
                    break;
                }
                
                let start_column = cap.start();
                let end_column = cap.end();
                let start_byte = line_offsets.get(line_idx).copied().unwrap_or(0) + start_column;
                let end_byte = line_offsets.get(line_idx).copied().unwrap_or(0) + end_column;
                
                results.push(SearchResult {
                    line: line_idx,
                    start_column,
                    end_column,
                    matched_text: cap.as_str().to_string(),
                    context: line.to_string(),
                    start_byte,
                    end_byte,
                });
            }
        }
        
        Ok(results)
    }
    
    /// Search in a specific range of lines
    pub fn search_in_range(
        &mut self,
        pattern: &str,
        text: &str,
        start_line: usize,
        end_line: usize,
    ) -> Result<Vec<SearchResult>, String> {
        let regex = self.compile_pattern(pattern)?;
        let mut results = Vec::new();
        
        // Calculate byte offsets for each line
        let line_offsets: Vec<usize> = std::iter::once(0)
            .chain(text.match_indices('\n').map(|(i, _)| i + 1))
            .collect();
        
        for (line_idx, line) in text.lines().enumerate() {
            if line_idx < start_line || line_idx > end_line {
                continue;
            }
            
            for cap in regex.find_iter(line) {
                if results.len() >= self.options.max_results {
                    break;
                }
                
                let start_column = cap.start();
                let end_column = cap.end();
                let start_byte = line_offsets.get(line_idx).copied().unwrap_or(0) + start_column;
                let end_byte = line_offsets.get(line_idx).copied().unwrap_or(0) + end_column;
                
                results.push(SearchResult {
                    line: line_idx,
                    start_column,
                    end_column,
                    matched_text: cap.as_str().to_string(),
                    context: line.to_string(),
                    start_byte,
                    end_byte,
                });
            }
        }
        
        Ok(results)
    }
    
    /// Replace all occurrences in text
    pub fn replace_all(&mut self, pattern: &str, replacement: &str, text: &str) -> Result<String, String> {
        let regex = self.compile_pattern(pattern)?;
        Ok(regex.replace_all(text, replacement).to_string())
    }
    
    /// Replace first occurrence in text
    pub fn replace_first(&mut self, pattern: &str, replacement: &str, text: &str) -> Result<String, String> {
        let regex = self.compile_pattern(pattern)?;
        Ok(regex.replace(text, replacement).to_string())
    }
    
    /// Count matches
    pub fn count_matches(&mut self, pattern: &str, text: &str) -> Result<usize, String> {
        let regex = self.compile_pattern(pattern)?;
        Ok(regex.find_iter(text).count())
    }
    
    /// Find next match from a position
    pub fn find_next(
        &mut self,
        pattern: &str,
        text: &str,
        from_line: usize,
        from_column: usize,
    ) -> Result<Option<SearchResult>, String> {
        let regex = self.compile_pattern(pattern)?;
        
        // Calculate byte offsets for each line
        let line_offsets: Vec<usize> = std::iter::once(0)
            .chain(text.match_indices('\n').map(|(i, _)| i + 1))
            .collect();
        
        for (line_idx, line) in text.lines().enumerate() {
            if line_idx < from_line {
                continue;
            }
            
            for cap in regex.find_iter(line) {
                // Skip if before the from position
                if line_idx == from_line && cap.start() < from_column {
                    continue;
                }
                
                let start_column = cap.start();
                let end_column = cap.end();
                let start_byte = line_offsets.get(line_idx).copied().unwrap_or(0) + start_column;
                let end_byte = line_offsets.get(line_idx).copied().unwrap_or(0) + end_column;
                
                return Ok(Some(SearchResult {
                    line: line_idx,
                    start_column,
                    end_column,
                    matched_text: cap.as_str().to_string(),
                    context: line.to_string(),
                    start_byte,
                    end_byte,
                }));
            }
        }
        
        Ok(None)
    }
    
    /// Find previous match from a position
    pub fn find_previous(
        &mut self,
        pattern: &str,
        text: &str,
        from_line: usize,
        from_column: usize,
    ) -> Result<Option<SearchResult>, String> {
        let regex = self.compile_pattern(pattern)?;
        
        // Calculate byte offsets for each line
        let line_offsets: Vec<usize> = std::iter::once(0)
            .chain(text.match_indices('\n').map(|(i, _)| i + 1))
            .collect();
        
        let lines: Vec<&str> = text.lines().collect();
        let mut last_match: Option<SearchResult> = None;
        
        for (line_idx, line) in lines.iter().enumerate() {
            if line_idx > from_line {
                break;
            }
            
            for cap in regex.find_iter(line) {
                let start_column = cap.start();
                let end_column = cap.end();
                
                // Skip if at or after the from position
                if line_idx == from_line && end_column >= from_column {
                    break;
                }
                
                let start_byte = line_offsets.get(line_idx).copied().unwrap_or(0) + start_column;
                let end_byte = line_offsets.get(line_idx).copied().unwrap_or(0) + end_column;
                
                last_match = Some(SearchResult {
                    line: line_idx,
                    start_column,
                    end_column,
                    matched_text: cap.as_str().to_string(),
                    context: line.to_string(),
                    start_byte,
                    end_byte,
                });
            }
        }
        
        Ok(last_match)
    }
    
    /// Clear cached pattern
    pub fn clear_cache(&mut self) {
        self.last_pattern = None;
        self.compiled_regex = None;
    }
}

impl Default for SearchEngine {
    fn default() -> Self {
        Self::new()
    }
}

/// Multi-file search result
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct FileSearchResult {
    pub file_path: String,
    pub results: Vec<SearchResult>,
}

/// Multi-file search engine
pub struct MultiFileSearch {
    engine: SearchEngine,
}

impl MultiFileSearch {
    pub fn new() -> Self {
        MultiFileSearch {
            engine: SearchEngine::new(),
        }
    }
    
    /// Search in multiple files
    pub fn search_files(
        &mut self,
        pattern: &str,
        files: &[(String, String)], // (file_path, content)
    ) -> Result<Vec<FileSearchResult>, String> {
        let mut results = Vec::new();
        
        for (file_path, content) in files {
            let file_results = self.engine.search(pattern, content)?;
            if !file_results.is_empty() {
                results.push(FileSearchResult {
                    file_path: file_path.clone(),
                    results: file_results,
                });
            }
        }
        
        Ok(results)
    }
}

impl Default for MultiFileSearch {
    fn default() -> Self {
        Self::new()
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    
    #[test]
    fn test_basic_search() {
        let mut engine = SearchEngine::new();
        let text = "Hello World\nHello Rust\nGoodbye World";
        let results = engine.search("Hello", text).unwrap();
        
        assert_eq!(results.len(), 2);
        assert_eq!(results[0].line, 0);
        assert_eq!(results[1].line, 1);
    }
    
    #[test]
    fn test_case_insensitive_search() {
        let mut engine = SearchEngine::new();
        let text = "Hello hello HELLO";
        let results = engine.search("hello", text).unwrap();
        
        assert_eq!(results.len(), 3);
    }
    
    #[test]
    fn test_case_sensitive_search() {
        let mut options = SearchOptions::default();
        options.case_sensitive = true;
        let mut engine = SearchEngine::with_options(options);
        
        let text = "Hello hello HELLO";
        let results = engine.search("hello", text).unwrap();
        
        assert_eq!(results.len(), 1);
    }
    
    #[test]
    fn test_whole_word_search() {
        let mut options = SearchOptions::default();
        options.whole_word = true;
        let mut engine = SearchEngine::with_options(options);
        
        let text = "hello helloworld worldhello";
        let results = engine.search("hello", text).unwrap();
        
        assert_eq!(results.len(), 1);
        assert_eq!(results[0].start_column, 0);
    }
    
    #[test]
    fn test_regex_search() {
        let mut options = SearchOptions::default();
        options.use_regex = true;
        let mut engine = SearchEngine::with_options(options);
        
        let text = "abc123 def456 ghi789";
        let results = engine.search(r"\d+", text).unwrap();
        
        assert_eq!(results.len(), 3);
    }
    
    #[test]
    fn test_replace_all() {
        let mut engine = SearchEngine::new();
        let text = "Hello World, Hello Rust";
        let result = engine.replace_all("Hello", "Hi", text).unwrap();
        
        assert_eq!(result, "Hi World, Hi Rust");
    }
    
    #[test]
    fn test_find_next() {
        let mut engine = SearchEngine::new();
        let text = "Hello\nWorld\nHello";
        
        let result = engine.find_next("Hello", text, 0, 0).unwrap();
        assert!(result.is_some());
        assert_eq!(result.unwrap().line, 0);
        
        let result = engine.find_next("Hello", text, 0, 5).unwrap();
        assert!(result.is_some());
        assert_eq!(result.unwrap().line, 2);
    }
}