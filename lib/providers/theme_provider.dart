import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:shared_preferences/shared_preferences.dart';

import '../app.dart';

final themeModeProvider =
    StateNotifierProvider<ThemeModeNotifier, ThemeMode>((ref) {
  return ThemeModeNotifier();
});

class ThemeModeNotifier extends StateNotifier<ThemeMode> {
  ThemeModeNotifier() : super(ThemeMode.dark) {
    _loadThemeMode();
  }

  Future<void> _loadThemeMode() async {
    final prefs = await SharedPreferences.getInstance();
    final index = prefs.getInt('theme_mode') ?? ThemeMode.dark.index;
    state = ThemeMode.values[index.clamp(0, ThemeMode.values.length - 1)];
  }

  Future<void> setThemeMode(ThemeMode mode) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setInt('theme_mode', mode.index);
    state = mode;
  }
}

final themeProvider = Provider<ThemeData>((ref) {
  return AppTheme.darkTheme();
});

class EditorTheme {
  final String name;
  final Color background;
  final Color foreground;
  final Color cursor;
  final Color selection;
  final Color lineNumber;
  final Color lineNumberActive;
  final Map<String, Color> tokenColors;

  const EditorTheme({
    required this.name,
    required this.background,
    required this.foreground,
    required this.cursor,
    required this.selection,
    required this.lineNumber,
    required this.lineNumberActive,
    required this.tokenColors,
  });

  factory EditorTheme.vscodeDark() {
    return const EditorTheme(
      name: 'VS Code Dark+',
      background: Color(0xFF1E1E1E),
      foreground: Color(0xFFD4D4D4),
      cursor: Color(0xFFAEAFAD),
      selection: Color(0xFF264F78),
      lineNumber: Color(0xFF858585),
      lineNumberActive: Color(0xFFC6C6C6),
      tokenColors: {
        'keyword': Color(0xFF569CD6),
        'title': Color(0xFFDCDCAA),
        'string': Color(0xFFCE9178),
        'number': Color(0xFFB5CEA8),
        'literal': Color(0xFF4FC1FF),
        'type': Color(0xFF4EC9B0),
        'built_in': Color(0xFF4FC1FF),
        'comment': Color(0xFF6A9955),
        'section': Color(0xFF569CD6),
        'attr': Color(0xFF9CDCFE),
        'operator': Color(0xFFD4D4D4),
      },
    );
  }

  factory EditorTheme.monokai() {
    return const EditorTheme(
      name: 'Monokai',
      background: Color(0xFF272822),
      foreground: Color(0xFFF8F8F2),
      cursor: Color(0xFFF8F8F0),
      selection: Color(0xFF49483E),
      lineNumber: Color(0xFF75715E),
      lineNumberActive: Color(0xFFF8F8F2),
      tokenColors: {
        'keyword': Color(0xFFF92672),
        'title': Color(0xFFA6E22E),
        'string': Color(0xFFE6DB74),
        'number': Color(0xFFAE81FF),
        'literal': Color(0xFFAE81FF),
        'type': Color(0xFF66D9EF),
        'built_in': Color(0xFF66D9EF),
        'comment': Color(0xFF75715E),
        'section': Color(0xFFF92672),
        'attr': Color(0xFFA6E22E),
        'operator': Color(0xFFF92672),
      },
    );
  }

  factory EditorTheme.oneDark() {
    return const EditorTheme(
      name: 'One Dark',
      background: Color(0xFF282C34),
      foreground: Color(0xFFABB2BF),
      cursor: Color(0xFF528BFF),
      selection: Color(0xFF3E4451),
      lineNumber: Color(0xFF4B5263),
      lineNumberActive: Color(0xFFABB2BF),
      tokenColors: {
        'keyword': Color(0xFFC678DD),
        'title': Color(0xFF61AFEF),
        'string': Color(0xFF98C379),
        'number': Color(0xFFD19A66),
        'literal': Color(0xFFD19A66),
        'type': Color(0xFFE5C07B),
        'built_in': Color(0xFF56B6C2),
        'comment': Color(0xFF5C6370),
        'section': Color(0xFFE06C75),
        'attr': Color(0xFFE06C75),
        'operator': Color(0xFF56B6C2),
      },
    );
  }
}

final editorThemeProvider =
    StateNotifierProvider<EditorThemeNotifier, EditorTheme>((ref) {
  return EditorThemeNotifier();
});

class EditorThemeNotifier extends StateNotifier<EditorTheme> {
  EditorThemeNotifier() : super(EditorTheme.vscodeDark()) {
    _loadTheme();
  }

  Future<void> _loadTheme() async {
    final prefs = await SharedPreferences.getInstance();
    final themeName = prefs.getString('editor_theme') ?? 'vscode_dark';
    setThemeByName(themeName);
  }

  void setThemeByName(String name) {
    switch (name.toLowerCase()) {
      case 'monokai':
        state = EditorTheme.monokai();
        break;
      case 'one_dark':
      case 'one dark':
        state = EditorTheme.oneDark();
        break;
      default:
        state = EditorTheme.vscodeDark();
    }
    _saveTheme(name);
  }

  Future<void> _saveTheme(String name) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString('editor_theme', name);
  }
}

final fontSizeProvider = StateNotifierProvider<FontSizeNotifier, double>((ref) {
  return FontSizeNotifier();
});

class FontSizeNotifier extends StateNotifier<double> {
  FontSizeNotifier() : super(14.0) {
    _load();
  }

  Future<void> _load() async {
    final prefs = await SharedPreferences.getInstance();
    state = prefs.getDouble('font_size') ?? 14.0;
  }

  Future<void> setFontSize(double size) async {
    final clamped = size.clamp(10.0, 32.0);
    final prefs = await SharedPreferences.getInstance();
    await prefs.setDouble('font_size', clamped);
    state = clamped;
  }

  void increase() => setFontSize(state + 1);

  void decrease() => setFontSize(state - 1);
}

final fontFamilyProvider =
    StateNotifierProvider<FontFamilyNotifier, String>((ref) {
  return FontFamilyNotifier();
});

class FontFamilyNotifier extends StateNotifier<String> {
  FontFamilyNotifier() : super('JetBrainsMono') {
    _load();
  }

  Future<void> _load() async {
    final prefs = await SharedPreferences.getInstance();
    state = prefs.getString('font_family') ?? 'JetBrainsMono';
  }

  Future<void> setFontFamily(String family) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString('font_family', family);
    state = family;
  }
}

final tabSizeProvider = StateNotifierProvider<TabSizeNotifier, int>((ref) {
  return TabSizeNotifier();
});

class TabSizeNotifier extends StateNotifier<int> {
  TabSizeNotifier() : super(2) {
    _load();
  }

  Future<void> _load() async {
    final prefs = await SharedPreferences.getInstance();
    state = prefs.getInt('tab_size') ?? 2;
  }

  Future<void> setTabSize(int size) async {
    final clamped = size.clamp(2, 8);
    final prefs = await SharedPreferences.getInstance();
    await prefs.setInt('tab_size', clamped);
    state = clamped;
  }
}

class BoolPreferenceNotifier extends StateNotifier<bool> {
  final String key;
  final bool defaultValue;

  BoolPreferenceNotifier({required this.key, required this.defaultValue})
      : super(defaultValue) {
    _load();
  }

  Future<void> _load() async {
    final prefs = await SharedPreferences.getInstance();
    state = prefs.getBool(key) ?? defaultValue;
  }

  Future<void> setValue(bool value) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setBool(key, value);
    state = value;
  }

  Future<void> toggle() => setValue(!state);
}

final showLineNumbersProvider =
    StateNotifierProvider<BoolPreferenceNotifier, bool>((ref) {
  return BoolPreferenceNotifier(key: 'show_line_numbers', defaultValue: true);
});

final showMinimapProvider =
    StateNotifierProvider<BoolPreferenceNotifier, bool>((ref) {
  return BoolPreferenceNotifier(key: 'show_minimap', defaultValue: true);
});

final wordWrapProvider =
    StateNotifierProvider<BoolPreferenceNotifier, bool>((ref) {
  return BoolPreferenceNotifier(key: 'word_wrap', defaultValue: false);
});

final highlightCurrentLineProvider =
    StateNotifierProvider<BoolPreferenceNotifier, bool>((ref) {
  return BoolPreferenceNotifier(
      key: 'highlight_current_line', defaultValue: true);
});

final autoSaveProvider =
    StateNotifierProvider<BoolPreferenceNotifier, bool>((ref) {
  return BoolPreferenceNotifier(key: 'auto_save', defaultValue: true);
});

final autoIndentProvider =
    StateNotifierProvider<BoolPreferenceNotifier, bool>((ref) {
  return BoolPreferenceNotifier(key: 'auto_indent', defaultValue: true);
});

final smartEditingProvider =
    StateNotifierProvider<BoolPreferenceNotifier, bool>((ref) {
  return BoolPreferenceNotifier(key: 'smart_editing', defaultValue: true);
});
