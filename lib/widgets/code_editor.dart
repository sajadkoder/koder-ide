import 'package:flutter/material.dart';
import 'package:flutter_code_editor/flutter_code_editor.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../providers/theme_provider.dart';

class CodeEditor extends ConsumerWidget {
  final CodeController controller;
  final UndoHistoryController undoController;
  final FocusNode focusNode;

  const CodeEditor({
    super.key,
    required this.controller,
    required this.undoController,
    required this.focusNode,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final editorTheme = ref.watch(editorThemeProvider);
    final fontSize = ref.watch(fontSizeProvider);
    final fontFamily = ref.watch(fontFamilyProvider);

    final showLineNumbers = ref.watch(showLineNumbersProvider);
    final showMinimap = ref.watch(showMinimapProvider);
    final wordWrap = ref.watch(wordWrapProvider);
    final highlightCurrentLine = ref.watch(highlightCurrentLineProvider);
    final smartEditing = ref.watch(smartEditingProvider);

    final codeStyles = _buildCodeThemeStyles(editorTheme, fontFamily, fontSize);

    return Container(
      color: editorTheme.background,
      child: Row(
        children: [
          Expanded(
            child: CodeTheme(
              data: CodeThemeData(styles: codeStyles),
              child: Container(
                decoration: highlightCurrentLine
                    ? BoxDecoration(
                        border: Border(
                          left: BorderSide(
                            color: editorTheme.selection.withValues(alpha: 0.75),
                            width: 2,
                          ),
                        ),
                      )
                    : null,
                child: CodeField(
                  controller: controller,
                  undoController: undoController,
                  focusNode: focusNode,
                  wrap: wordWrap,
                  textStyle: TextStyle(
                    fontFamily: fontFamily,
                    fontSize: fontSize,
                    height: 1.45,
                    color: editorTheme.foreground,
                  ),
                  cursorColor: editorTheme.cursor,
                  gutterStyle:
                      showLineNumbers ? const GutterStyle() : GutterStyle.none,
                  smartQuotesType: smartEditing
                      ? SmartQuotesType.enabled
                      : SmartQuotesType.disabled,
                  smartDashesType: smartEditing
                      ? SmartDashesType.enabled
                      : SmartDashesType.disabled,
                  decoration: const BoxDecoration(),
                ),
              ),
            ),
          ),
          if (showMinimap)
            SizedBox(
              width: 68,
              child: ColoredBox(
                color: editorTheme.background.withValues(alpha: 0.7),
                child: CustomPaint(
                  painter: MinimapPainter(
                    lines: controller.text.split('\n'),
                    color: editorTheme.foreground.withValues(alpha: 0.22),
                  ),
                ),
              ),
            ),
        ],
      ),
    );
  }

  Map<String, TextStyle> _buildCodeThemeStyles(
    EditorTheme editorTheme,
    String fontFamily,
    double fontSize,
  ) {
    final base = TextStyle(
      color: editorTheme.foreground,
      fontFamily: fontFamily,
      fontSize: fontSize,
      height: 1.45,
    );

    TextStyle token(String key) =>
        base.copyWith(color: editorTheme.tokenColors[key]);

    return {
      'root': base,
      'keyword': token('keyword'),
      'built_in': token('built_in'),
      'type': token('type'),
      'literal': token('literal'),
      'number': token('number'),
      'regexp': token('number'),
      'string': token('string'),
      'subst': token('foreground'),
      'symbol': token('literal'),
      'class': token('type'),
      'function': token('title'),
      'title': token('title'),
      'params': token('foreground'),
      'comment': token('comment'),
      'doctag': token('comment'),
      'meta': token('comment'),
      'section': token('section'),
      'attr': token('attr'),
      'selector-tag': token('keyword'),
      'selector-id': token('attr'),
      'selector-class': token('attr'),
      'selector-attr': token('attr'),
      'selector-pseudo': token('keyword'),
      'addition': token('type'),
      'deletion': token('string'),
      'emphasis': base.copyWith(fontStyle: FontStyle.italic),
      'strong': base.copyWith(fontWeight: FontWeight.bold),
      'link': token('literal').copyWith(decoration: TextDecoration.underline),
      'bullet': token('literal'),
      'quote': token('comment'),
    };
  }
}

class MinimapPainter extends CustomPainter {
  final List<String> lines;
  final Color color;

  MinimapPainter({required this.lines, required this.color});

  @override
  void paint(Canvas canvas, Size size) {
    if (lines.isEmpty) return;

    final visibleLineCount = lines.length.clamp(1, 250);
    final lineHeight = size.height / visibleLineCount;
    final paint = Paint()..color = color;

    for (var i = 0; i < lines.length && i < 250; i++) {
      final line = lines[i];
      if (line.trim().isEmpty) continue;
      final y = i * lineHeight;
      final w = (line.length * 0.35).clamp(2.0, size.width - 4);
      canvas.drawRect(
          Rect.fromLTWH(2, y, w, (lineHeight * 0.75).clamp(1.0, 8.0)), paint);
    }
  }

  @override
  bool shouldRepaint(covariant MinimapPainter oldDelegate) {
    return oldDelegate.lines != lines || oldDelegate.color != color;
  }
}
