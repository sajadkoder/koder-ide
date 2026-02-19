import 'dart:convert';
import 'dart:io';

import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:shared_preferences/shared_preferences.dart';

import '../utils/language_utils.dart';

class CursorPosition {
  final int line;
  final int column;

  const CursorPosition({required this.line, required this.column});

  factory CursorPosition.zero() => const CursorPosition(line: 0, column: 0);
}

class EditorState {
  final String text;
  final String filePath;
  final String fileName;
  final String language;
  final CursorPosition cursor;
  final bool isModified;
  final List<String> recentFiles;

  const EditorState({
    this.text = '',
    this.filePath = '',
    this.fileName = 'Untitled',
    this.language = 'plaintext',
    this.cursor = const CursorPosition(line: 0, column: 0),
    this.isModified = false,
    this.recentFiles = const [],
  });

  bool get hasFile => filePath.isNotEmpty;

  List<String> get lines => text.isEmpty ? const [''] : text.split('\n');

  int get lineCount => lines.length;

  EditorState copyWith({
    String? text,
    String? filePath,
    String? fileName,
    String? language,
    CursorPosition? cursor,
    bool? isModified,
    List<String>? recentFiles,
  }) {
    return EditorState(
      text: text ?? this.text,
      filePath: filePath ?? this.filePath,
      fileName: fileName ?? this.fileName,
      language: language ?? this.language,
      cursor: cursor ?? this.cursor,
      isModified: isModified ?? this.isModified,
      recentFiles: recentFiles ?? this.recentFiles,
    );
  }
}

class EditorStateNotifier extends StateNotifier<EditorState> {
  static const _recentFilesKey = 'recent_files';

  EditorStateNotifier() : super(const EditorState()) {
    _loadRecentFiles();
  }

  Future<void> _loadRecentFiles() async {
    final prefs = await SharedPreferences.getInstance();
    final saved = prefs.getStringList(_recentFilesKey) ?? const <String>[];
    state = state.copyWith(recentFiles: saved);
  }

  Future<void> _saveRecentFiles(List<String> files) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setStringList(_recentFilesKey, files);
  }

  Future<void> openFile(String path) async {
    final file = File(path);
    if (!await file.exists()) {
      throw Exception('File not found: $path');
    }

    final bytes = await file.readAsBytes();
    final content = utf8.decode(bytes, allowMalformed: true);
    openFileContent(path, content);
  }

  void openFileContent(String path, String content) {
    final fileName = path.split(Platform.pathSeparator).last;
    final language = detectLanguageFromPath(path);

    state = EditorState(
      text: content,
      filePath: path,
      fileName: fileName,
      language: language,
      cursor: const CursorPosition(line: 0, column: 0),
      isModified: false,
      recentFiles: state.recentFiles,
    );

    _addRecentFile(path);
  }

  void newDocument() {
    state = state.copyWith(
      text: '',
      filePath: '',
      fileName: 'Untitled',
      language: 'plaintext',
      cursor: CursorPosition.zero(),
      isModified: false,
    );
  }

  void setText(String text, {bool markModified = true}) {
    final nextText = text;
    final modified = markModified ? true : state.isModified;
    state = state.copyWith(text: nextText, isModified: modified);
  }

  void setCursorFromOffset(int offset) {
    final safeOffset = offset.clamp(0, state.text.length);
    final cursor = _lineColumnFromOffset(state.text, safeOffset);
    state = state.copyWith(cursor: cursor);
  }

  Future<void> saveCurrent(String text) async {
    if (!state.hasFile) {
      throw Exception('No file path selected');
    }

    final file = File(state.filePath);
    await file.writeAsString(text);
    state = state.copyWith(text: text, isModified: false);
    _addRecentFile(state.filePath);
  }

  Future<void> saveAs(String path, String text) async {
    final file = File(path);
    await file.writeAsString(text);

    final fileName = path.split(Platform.pathSeparator).last;
    final language = detectLanguageFromPath(path);

    state = state.copyWith(
      text: text,
      filePath: path,
      fileName: fileName,
      language: language,
      isModified: false,
    );

    _addRecentFile(path);
  }

  Future<void> _addRecentFile(String path) async {
    final updated = <String>[path, ...state.recentFiles.where((f) => f != path)]
        .take(20)
        .toList(growable: false);

    state = state.copyWith(recentFiles: updated);
    await _saveRecentFiles(updated);
  }

  Future<void> removeRecentFile(String path) async {
    final updated =
        state.recentFiles.where((f) => f != path).toList(growable: false);
    state = state.copyWith(recentFiles: updated);
    await _saveRecentFiles(updated);
  }

  static CursorPosition _lineColumnFromOffset(String text, int offset) {
    if (text.isEmpty) {
      return CursorPosition.zero();
    }

    var line = 0;
    var column = 0;

    for (var i = 0; i < offset && i < text.length; i++) {
      if (text[i] == '\n') {
        line += 1;
        column = 0;
      } else {
        column += 1;
      }
    }

    return CursorPosition(line: line, column: column);
  }
}

final editorStateProvider =
    StateNotifierProvider<EditorStateNotifier, EditorState>((ref) {
  return EditorStateNotifier();
});

class SearchResult {
  final int startOffset;
  final int endOffset;
  final int line;
  final int startColumn;
  final int endColumn;
  final String matchedText;
  final String context;

  const SearchResult({
    required this.startOffset,
    required this.endOffset,
    required this.line,
    required this.startColumn,
    required this.endColumn,
    required this.matchedText,
    required this.context,
  });
}

class SearchState {
  final String pattern;
  final String replacement;
  final bool caseSensitive;
  final bool wholeWord;
  final bool useRegex;
  final List<SearchResult> results;
  final int currentResultIndex;
  final bool isVisible;
  final String? error;

  const SearchState({
    this.pattern = '',
    this.replacement = '',
    this.caseSensitive = false,
    this.wholeWord = false,
    this.useRegex = false,
    this.results = const [],
    this.currentResultIndex = -1,
    this.isVisible = false,
    this.error,
  });

  SearchResult? get currentResult {
    if (currentResultIndex < 0 || currentResultIndex >= results.length) {
      return null;
    }
    return results[currentResultIndex];
  }

  SearchState copyWith({
    String? pattern,
    String? replacement,
    bool? caseSensitive,
    bool? wholeWord,
    bool? useRegex,
    List<SearchResult>? results,
    int? currentResultIndex,
    bool? isVisible,
    String? error,
    bool clearError = false,
  }) {
    return SearchState(
      pattern: pattern ?? this.pattern,
      replacement: replacement ?? this.replacement,
      caseSensitive: caseSensitive ?? this.caseSensitive,
      wholeWord: wholeWord ?? this.wholeWord,
      useRegex: useRegex ?? this.useRegex,
      results: results ?? this.results,
      currentResultIndex: currentResultIndex ?? this.currentResultIndex,
      isVisible: isVisible ?? this.isVisible,
      error: clearError ? null : (error ?? this.error),
    );
  }
}

class SearchStateNotifier extends StateNotifier<SearchState> {
  SearchStateNotifier() : super(const SearchState());

  void setPattern(String pattern) {
    state = state.copyWith(pattern: pattern);
  }

  void setReplacement(String replacement) {
    state = state.copyWith(replacement: replacement);
  }

  void toggleCaseSensitive() {
    state = state.copyWith(caseSensitive: !state.caseSensitive);
  }

  void toggleWholeWord() {
    state = state.copyWith(wholeWord: !state.wholeWord);
  }

  void toggleRegex() {
    state = state.copyWith(useRegex: !state.useRegex);
  }

  void show() {
    state = state.copyWith(isVisible: true);
  }

  void hide() {
    state = state.copyWith(isVisible: false);
  }

  void clear() {
    state = const SearchState();
  }

  void searchIn(String text) {
    if (state.pattern.isEmpty) {
      state = state.copyWith(
          results: const [], currentResultIndex: -1, clearError: true);
      return;
    }

    final regex = _compileRegex(state);
    if (regex == null) {
      return;
    }

    final results = <SearchResult>[];
    var globalOffset = 0;
    final lines = text.split('\n');

    for (var lineIndex = 0; lineIndex < lines.length; lineIndex++) {
      final line = lines[lineIndex];
      for (final match in regex.allMatches(line)) {
        results.add(
          SearchResult(
            startOffset: globalOffset + match.start,
            endOffset: globalOffset + match.end,
            line: lineIndex,
            startColumn: match.start,
            endColumn: match.end,
            matchedText: match.group(0) ?? '',
            context: line,
          ),
        );
      }
      globalOffset += line.length + 1;
    }

    state = state.copyWith(
      results: results,
      currentResultIndex: results.isEmpty ? -1 : 0,
      clearError: true,
    );
  }

  void nextResult() {
    if (state.results.isEmpty) {
      return;
    }
    final nextIndex = (state.currentResultIndex + 1) % state.results.length;
    state = state.copyWith(currentResultIndex: nextIndex);
  }

  void previousResult() {
    if (state.results.isEmpty) {
      return;
    }
    final prevIndex = state.currentResultIndex <= 0
        ? state.results.length - 1
        : state.currentResultIndex - 1;
    state = state.copyWith(currentResultIndex: prevIndex);
  }

  String replaceCurrentInText(String text, String replacement) {
    final result = state.currentResult;
    if (result == null) {
      return text;
    }

    return text.replaceRange(result.startOffset, result.endOffset, replacement);
  }

  String replaceAllInText(String text, String replacement) {
    final regex = _compileRegex(state);
    if (regex == null) {
      return text;
    }
    return text.replaceAll(regex, replacement);
  }

  RegExp? _compileRegex(SearchState searchState) {
    if (searchState.pattern.isEmpty) {
      return null;
    }

    final source = searchState.useRegex
        ? searchState.pattern
        : RegExp.escape(searchState.pattern);
    final pattern = searchState.wholeWord ? '\\b(?:$source)\\b' : source;

    try {
      return RegExp(pattern,
          caseSensitive: searchState.caseSensitive, multiLine: true);
    } on FormatException catch (e) {
      state = state.copyWith(
          error: e.message, results: const [], currentResultIndex: -1);
      return null;
    }
  }
}

final searchStateProvider =
    StateNotifierProvider<SearchStateNotifier, SearchState>((ref) {
  return SearchStateNotifier();
});
