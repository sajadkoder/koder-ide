// ignore_for_file: equal_elements_in_set, unused_element, deprecated_member_use

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../providers/editor_provider.dart';
import '../providers/theme_provider.dart';

/// Autocomplete suggestion
class AutocompleteSuggestion {
  final String label;
  final String kind;
  final String? detail;
  final String? documentation;
  final String insertText;

  const AutocompleteSuggestion({
    required this.label,
    required this.kind,
    this.detail,
    this.documentation,
    required this.insertText,
  });
}

/// Autocomplete popup widget
class AutocompletePopup extends ConsumerStatefulWidget {
  final String prefix;
  final Offset position;
  final VoidCallback onDismiss;
  final Function(AutocompleteSuggestion) onSelect;

  const AutocompletePopup({
    super.key,
    required this.prefix,
    required this.position,
    required this.onDismiss,
    required this.onSelect,
  });

  @override
  ConsumerState<AutocompletePopup> createState() => _AutocompletePopupState();
}

class _AutocompletePopupState extends ConsumerState<AutocompletePopup> {
  List<AutocompleteSuggestion> _suggestions = [];
  int _selectedIndex = 0;

  @override
  void initState() {
    super.initState();
    _loadSuggestions();
  }

  void _loadSuggestions() {
    final editorState = ref.read(editorStateProvider);
    final suggestions =
        _getSuggestionsForLanguage(editorState.language, widget.prefix);
    setState(() {
      _suggestions = suggestions;
      _selectedIndex = 0;
    });
  }

  List<AutocompleteSuggestion> _getSuggestionsForLanguage(
      String language, String prefix) {
    final keywords = _getKeywords(language);
    return keywords
        .where((k) => k.toLowerCase().startsWith(prefix.toLowerCase()))
        .map((k) => AutocompleteSuggestion(
              label: k,
              kind: 'keyword',
              insertText: k,
            ))
        .toList();
  }

  Set<String> _getKeywords(String language) {
    switch (language) {
      case 'javascript':
      case 'typescript':
        return {
          'async',
          'await',
          'break',
          'case',
          'catch',
          'class',
          'const',
          'continue',
          'debugger',
          'default',
          'delete',
          'do',
          'else',
          'export',
          'extends',
          'false',
          'finally',
          'for',
          'function',
          'if',
          'import',
          'in',
          'instanceof',
          'let',
          'new',
          'null',
          'return',
          'static',
          'super',
          'switch',
          'this',
          'throw',
          'true',
          'try',
          'typeof',
          'undefined',
          'var',
          'void',
          'while',
          'with',
          'yield',
          'console',
          'log',
          'document',
          'window',
          'Array',
          'Object',
          'String',
          'Number',
          'Boolean',
          'Promise',
          'async',
          'await',
          'fetch',
          'JSON',
          'parseInt',
          'parseFloat',
        };
      case 'python':
        return {
          'and',
          'as',
          'assert',
          'async',
          'await',
          'break',
          'class',
          'continue',
          'def',
          'del',
          'elif',
          'else',
          'except',
          'False',
          'finally',
          'for',
          'from',
          'global',
          'if',
          'import',
          'in',
          'is',
          'lambda',
          'None',
          'nonlocal',
          'not',
          'or',
          'pass',
          'raise',
          'return',
          'True',
          'try',
          'while',
          'with',
          'yield',
          'print',
          'len',
          'range',
          'str',
          'int',
          'float',
          'list',
          'dict',
          'set',
          'tuple',
          'open',
          'self',
          'init',
        };
      case 'rust':
        return {
          'as',
          'async',
          'await',
          'break',
          'const',
          'continue',
          'crate',
          'dyn',
          'else',
          'enum',
          'extern',
          'false',
          'fn',
          'for',
          'if',
          'impl',
          'in',
          'let',
          'loop',
          'match',
          'mod',
          'move',
          'mut',
          'pub',
          'ref',
          'return',
          'self',
          'Self',
          'static',
          'struct',
          'super',
          'trait',
          'true',
          'type',
          'unsafe',
          'use',
          'where',
          'while',
          'println',
          'vec',
          'String',
          'Option',
          'Result',
          'Some',
          'None',
          'Ok',
          'Err',
          'Box',
          'Vec',
          'HashMap',
          'HashSet',
        };
      case 'dart':
        return {
          'abstract',
          'as',
          'assert',
          'async',
          'await',
          'break',
          'case',
          'catch',
          'class',
          'const',
          'continue',
          'covariant',
          'default',
          'deferred',
          'do',
          'dynamic',
          'else',
          'enum',
          'export',
          'extends',
          'extension',
          'external',
          'factory',
          'false',
          'final',
          'finally',
          'for',
          'Function',
          'get',
          'hide',
          'if',
          'implements',
          'import',
          'in',
          'interface',
          'is',
          'late',
          'library',
          'mixin',
          'new',
          'null',
          'on',
          'operator',
          'part',
          'required',
          'rethrow',
          'return',
          'set',
          'show',
          'static',
          'super',
          'switch',
          'this',
          'throw',
          'true',
          'try',
          'typedef',
          'var',
          'void',
          'while',
          'with',
          'yield',
          'print',
          'String',
          'int',
          'double',
          'bool',
          'List',
          'Map',
          'Set',
          'Future',
          'Stream',
          'Widget',
          'StatelessWidget',
          'StatefulWidget',
          'State',
          'BuildContext',
        };
      default:
        return {};
    }
  }

  void _selectPrevious() {
    if (_suggestions.isEmpty) return;
    setState(() {
      _selectedIndex = (_selectedIndex - 1).clamp(0, _suggestions.length - 1);
    });
  }

  void _selectNext() {
    if (_suggestions.isEmpty) return;
    setState(() {
      _selectedIndex = (_selectedIndex + 1).clamp(0, _suggestions.length - 1);
    });
  }

  void _confirmSelection() {
    if (_suggestions.isEmpty || _selectedIndex < 0) return;
    widget.onSelect(_suggestions[_selectedIndex]);
  }

  @override
  Widget build(BuildContext context) {
    final editorTheme = ref.watch(editorThemeProvider);

    if (_suggestions.isEmpty) {
      return const SizedBox.shrink();
    }

    return Positioned(
      left: widget.position.dx,
      top: widget.position.dy,
      child: Material(
        color: editorTheme.background,
        elevation: 8,
        borderRadius: BorderRadius.circular(8),
        child: Container(
          width: 280,
          constraints: const BoxConstraints(maxHeight: 200),
          decoration: BoxDecoration(
            color: editorTheme.background,
            borderRadius: BorderRadius.circular(8),
            border: Border.all(color: Colors.grey.shade700),
          ),
          child: ListView.builder(
            shrinkWrap: true,
            padding: EdgeInsets.zero,
            itemCount: _suggestions.length,
            itemBuilder: (context, index) {
              final suggestion = _suggestions[index];
              final isSelected = index == _selectedIndex;

              return InkWell(
                onTap: () {
                  setState(() {
                    _selectedIndex = index;
                  });
                  _confirmSelection();
                },
                child: Container(
                  padding:
                      const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                  decoration: BoxDecoration(
                    color: isSelected
                        ? const Color(0xFF007ACC).withOpacity(0.3)
                        : null,
                  ),
                  child: Row(
                    children: [
                      // Kind icon
                      Container(
                        width: 20,
                        height: 20,
                        decoration: BoxDecoration(
                          color: _getKindColor(suggestion.kind),
                          borderRadius: BorderRadius.circular(4),
                        ),
                        child: Center(
                          child: Text(
                            suggestion.kind[0].toUpperCase(),
                            style: const TextStyle(
                              color: Colors.white,
                              fontSize: 10,
                              fontWeight: FontWeight.bold,
                            ),
                          ),
                        ),
                      ),
                      const SizedBox(width: 8),
                      // Label
                      Expanded(
                        child: Text(
                          suggestion.label,
                          style: TextStyle(
                            color: editorTheme.foreground,
                            fontSize: 13,
                          ),
                        ),
                      ),
                      // Detail
                      if (suggestion.detail != null)
                        Text(
                          suggestion.detail!,
                          style: TextStyle(
                            color: Colors.grey.shade500,
                            fontSize: 11,
                          ),
                        ),
                    ],
                  ),
                ),
              );
            },
          ),
        ),
      ),
    );
  }

  Color _getKindColor(String kind) {
    switch (kind) {
      case 'keyword':
        return const Color(0xFF569CD6);
      case 'function':
        return const Color(0xFFDCDCAA);
      case 'variable':
        return const Color(0xFF9CDCFE);
      case 'class':
        return const Color(0xFF4EC9B0);
      case 'constant':
        return const Color(0xFF4FC1FF);
      default:
        return Colors.grey;
    }
  }
}
