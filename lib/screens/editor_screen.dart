import 'dart:async';
import 'dart:math';

import 'package:file_picker/file_picker.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_code_editor/flutter_code_editor.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../providers/editor_provider.dart';
import '../providers/theme_provider.dart';
import '../utils/language_utils.dart';
import '../widgets/code_editor.dart';
import '../widgets/search_panel.dart';

class EditorScreen extends ConsumerStatefulWidget {
  const EditorScreen({super.key});

  @override
  ConsumerState<EditorScreen> createState() => _EditorScreenState();
}

class _EditorScreenState extends ConsumerState<EditorScreen> {
  final FocusNode _editorFocusNode = FocusNode();
  final UndoHistoryController _undoController = UndoHistoryController();

  late CodeController _codeController;
  Timer? _autoSaveTimer;
  bool _isApplyingExternalState = false;
  String _currentLanguage = 'plaintext';

  @override
  void initState() {
    super.initState();
    final state = ref.read(editorStateProvider);
    _currentLanguage = state.language;
    _codeController =
        _createController(text: state.text, language: state.language);
    _codeController.addListener(_onControllerChanged);
  }

  @override
  void dispose() {
    _autoSaveTimer?.cancel();
    _codeController.removeListener(_onControllerChanged);
    _codeController.dispose();
    _undoController.dispose();
    _editorFocusNode.dispose();
    super.dispose();
  }

  CodeController _createController(
      {required String text, required String language}) {
    return CodeController(
      text: text,
      language: modeForLanguage(language),
      analyzer: const DefaultLocalAnalyzer(),
      params: EditorParams(tabSpaces: ref.read(tabSizeProvider)),
      modifiers: _buildModifiers(
        autoIndent: ref.read(autoIndentProvider),
        smartEditing: ref.read(smartEditingProvider),
      ),
    );
  }

  List<CodeModifier> _buildModifiers({
    required bool autoIndent,
    required bool smartEditing,
  }) {
    final modifiers = <CodeModifier>[
      const TabModifier(),
    ];

    if (autoIndent) {
      modifiers.insert(0, const IndentModifier());
    }

    if (smartEditing) {
      modifiers.add(const CloseBlockModifier());
    }

    return modifiers;
  }

  void _recreateController() {
    final oldText = _codeController.text;
    final oldSelection = _codeController.selection;

    _codeController.removeListener(_onControllerChanged);
    _codeController.dispose();

    _codeController =
        _createController(text: oldText, language: _currentLanguage);
    _codeController.selection = TextSelection(
      baseOffset: oldSelection.start.clamp(0, oldText.length),
      extentOffset: oldSelection.end.clamp(0, oldText.length),
    );
    _codeController.addListener(_onControllerChanged);

    if (mounted) {
      setState(() {});
    }
  }

  void _onControllerChanged() {
    if (_isApplyingExternalState) {
      return;
    }

    final editorNotifier = ref.read(editorStateProvider.notifier);
    editorNotifier.setText(_codeController.text);

    final offset = _codeController.selection.baseOffset;
    if (offset >= 0) {
      editorNotifier.setCursorFromOffset(offset);
    }

    final searchState = ref.read(searchStateProvider);
    if (searchState.pattern.isNotEmpty) {
      ref.read(searchStateProvider.notifier).searchIn(_codeController.text);
    }

    _scheduleAutosave();
  }

  void _scheduleAutosave() {
    if (!ref.read(autoSaveProvider)) {
      return;
    }

    final state = ref.read(editorStateProvider);
    if (!state.hasFile || !state.isModified) {
      return;
    }

    _autoSaveTimer?.cancel();
    _autoSaveTimer = Timer(const Duration(milliseconds: 900), () async {
      try {
        await ref
            .read(editorStateProvider.notifier)
            .saveCurrent(_codeController.text);
      } catch (_) {
        // Ignore autosave errors in background.
      }
    });
  }

  Future<void> _applyExternalState(EditorState state) async {
    final sameText = _codeController.text == state.text;
    final sameLanguage = _currentLanguage == state.language;
    if (sameText && sameLanguage) {
      return;
    }

    _isApplyingExternalState = true;
    try {
      if (!sameLanguage) {
        _currentLanguage = state.language;
        _codeController.setLanguage(modeForLanguage(state.language),
            analyzer: const DefaultLocalAnalyzer());
      }

      if (!sameText) {
        final cursor = min(state.text.length,
            _codeController.selection.baseOffset.clamp(0, state.text.length));
        _codeController.value = TextEditingValue(
          text: state.text,
          selection: TextSelection.collapsed(offset: cursor),
        );
      }
    } finally {
      _isApplyingExternalState = false;
    }
  }

  Future<bool> _confirmDiscardIfNeeded() async {
    final state = ref.read(editorStateProvider);
    if (!state.isModified) {
      return true;
    }

    final decision = await showDialog<bool>(
      context: context,
      builder: (context) {
        return AlertDialog(
          title: const Text('Unsaved Changes'),
          content: const Text(
              'You have unsaved changes. Continue and discard them?'),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(context, false),
              child: const Text('Cancel'),
            ),
            FilledButton(
              onPressed: () => Navigator.pop(context, true),
              child: const Text('Discard'),
            ),
          ],
        );
      },
    );

    return decision ?? false;
  }

  Future<void> _openFile() async {
    if (!await _confirmDiscardIfNeeded()) {
      return;
    }

    try {
      final result = await FilePicker.platform
          .pickFiles(type: FileType.any, allowMultiple: false);
      if (result == null ||
          result.files.isEmpty ||
          result.files.first.path == null) {
        return;
      }

      final path = result.files.first.path!;
      await ref.read(editorStateProvider.notifier).openFile(path);
      _showMessage('Opened ${path.split('\\').last.split('/').last}');
    } catch (e) {
      _showError('Failed to open file: $e');
    }
  }

  Future<void> _saveFile() async {
    try {
      final state = ref.read(editorStateProvider);
      if (!state.hasFile) {
        await _saveFileAs();
        return;
      }

      await ref
          .read(editorStateProvider.notifier)
          .saveCurrent(_codeController.text);
      _showMessage('Saved ${state.fileName}');
    } catch (e) {
      _showError('Failed to save file: $e');
    }
  }

  Future<void> _saveFileAs() async {
    try {
      final result = await FilePicker.platform.saveFile(
        dialogTitle: 'Save File As',
        fileName: ref.read(editorStateProvider).fileName,
      );
      if (result == null) {
        return;
      }

      await ref
          .read(editorStateProvider.notifier)
          .saveAs(result, _codeController.text);
      _showMessage('Saved to $result');
    } catch (e) {
      _showError('Failed to save file: $e');
    }
  }

  Future<void> _newFile() async {
    if (!await _confirmDiscardIfNeeded()) {
      return;
    }

    ref.read(editorStateProvider.notifier).newDocument();
    ref.read(searchStateProvider.notifier).clear();
  }

  Future<void> _copySelection() async {
    final selection = _codeController.selection;
    if (selection.start >= 0 && selection.end > selection.start) {
      final text =
          _codeController.text.substring(selection.start, selection.end);
      await Clipboard.setData(ClipboardData(text: text));
      _showMessage('Selection copied');
      return;
    }

    await Clipboard.setData(ClipboardData(text: _codeController.text));
    _showMessage('Document copied');
  }

  Future<void> _pasteClipboard() async {
    final data = await Clipboard.getData(Clipboard.kTextPlain);
    final text = data?.text;
    if (text == null || text.isEmpty) {
      return;
    }

    final current = _codeController.text;
    final selection = _codeController.selection;
    final start = selection.start.clamp(0, current.length);
    final end = selection.end.clamp(0, current.length);

    final next = current.replaceRange(start, end, text);
    final nextCursor = start + text.length;
    _codeController.value = TextEditingValue(
      text: next,
      selection: TextSelection.collapsed(offset: nextCursor),
    );
  }

  void _selectAll() {
    _codeController.selection =
        TextSelection(baseOffset: 0, extentOffset: _codeController.text.length);
  }

  void _undo() {
    _undoController.undo();
  }

  void _redo() {
    _undoController.redo();
  }

  void _showSearch() {
    final notifier = ref.read(searchStateProvider.notifier);
    notifier.show();
    notifier.searchIn(_codeController.text);
  }

  void _formatDocument() {
    final formatted = _codeController.text
        .split('\n')
        .map((line) => line.replaceFirst(RegExp(r'\s+$'), ''))
        .join('\n');

    _codeController.value = TextEditingValue(
      text: formatted,
      selection: TextSelection.collapsed(
          offset: min(_codeController.selection.baseOffset, formatted.length)),
    );

    _showMessage('Document formatted');
  }

  void _toggleComment() {
    _codeController.commentOutOrUncommentSelection();
  }

  void _showMessage(String message) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(message),
        behavior: SnackBarBehavior.floating,
        duration: const Duration(seconds: 2),
      ),
    );
  }

  void _showError(String message) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(message),
        backgroundColor: Colors.red,
        behavior: SnackBarBehavior.floating,
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final editorState = ref.watch(editorStateProvider);
    final searchState = ref.watch(searchStateProvider);

    ref.listen<EditorState>(editorStateProvider, (previous, next) {
      _applyExternalState(next);
    });

    ref.listen<int>(tabSizeProvider, (previous, next) {
      if (previous != next) {
        _recreateController();
      }
    });

    ref.listen<bool>(autoIndentProvider, (previous, next) {
      if (previous != next) {
        _recreateController();
      }
    });

    ref.listen<bool>(smartEditingProvider, (previous, next) {
      if (previous != next) {
        _recreateController();
      }
    });

    return Scaffold(
      appBar: AppBar(
        title: Row(
          children: [
            Expanded(
              child: Text(
                editorState.fileName,
                overflow: TextOverflow.ellipsis,
                style: const TextStyle(fontSize: 16),
              ),
            ),
            if (editorState.isModified)
              const Padding(
                padding: EdgeInsets.only(left: 4),
                child: Text('*', style: TextStyle(color: Colors.orange)),
              ),
          ],
        ),
        actions: [
          IconButton(
            icon: const Icon(Icons.add_circle_outline),
            onPressed: _newFile,
            tooltip: 'New File',
          ),
          IconButton(
            icon: const Icon(Icons.folder_open),
            onPressed: _openFile,
            tooltip: 'Open File',
          ),
          IconButton(
            icon: const Icon(Icons.save),
            onPressed: editorState.isModified ? _saveFile : null,
            tooltip: 'Save',
          ),
          IconButton(
            icon: const Icon(Icons.search),
            onPressed: _showSearch,
            tooltip: 'Search',
          ),
          PopupMenuButton<String>(
            icon: const Icon(Icons.more_vert),
            onSelected: (value) {
              switch (value) {
                case 'save_as':
                  _saveFileAs();
                  break;
                case 'select_all':
                  _selectAll();
                  break;
                case 'copy':
                  _copySelection();
                  break;
                case 'paste':
                  _pasteClipboard();
                  break;
              }
            },
            itemBuilder: (context) => const [
              PopupMenuItem(
                value: 'save_as',
                child: ListTile(
                    leading: Icon(Icons.save_as), title: Text('Save As...')),
              ),
              PopupMenuDivider(),
              PopupMenuItem(
                value: 'select_all',
                child: ListTile(
                    leading: Icon(Icons.select_all), title: Text('Select All')),
              ),
              PopupMenuItem(
                value: 'copy',
                child: ListTile(leading: Icon(Icons.copy), title: Text('Copy')),
              ),
              PopupMenuItem(
                value: 'paste',
                child:
                    ListTile(leading: Icon(Icons.paste), title: Text('Paste')),
              ),
            ],
          ),
        ],
      ),
      body: Shortcuts(
        shortcuts: const <ShortcutActivator, Intent>{
          SingleActivator(LogicalKeyboardKey.keyS, control: true):
              _SaveIntent(),
          SingleActivator(LogicalKeyboardKey.keyF, control: true):
              _SearchIntent(),
          SingleActivator(LogicalKeyboardKey.keyA, control: true):
              _SelectAllIntent(),
          SingleActivator(LogicalKeyboardKey.keyZ, control: true):
              _UndoIntent(),
          SingleActivator(LogicalKeyboardKey.keyY, control: true):
              _RedoIntent(),
        },
        child: Actions(
          actions: <Type, Action<Intent>>{
            _SaveIntent:
                CallbackAction<_SaveIntent>(onInvoke: (_) => _saveFile()),
            _SearchIntent:
                CallbackAction<_SearchIntent>(onInvoke: (_) => _showSearch()),
            _SelectAllIntent:
                CallbackAction<_SelectAllIntent>(onInvoke: (_) => _selectAll()),
            _UndoIntent: CallbackAction<_UndoIntent>(onInvoke: (_) => _undo()),
            _RedoIntent: CallbackAction<_RedoIntent>(onInvoke: (_) => _redo()),
          },
          child: Column(
            children: [
              if (searchState.isVisible)
                SearchPanel(controller: _codeController),
              Expanded(
                child: CodeEditor(
                  controller: _codeController,
                  undoController: _undoController,
                  focusNode: _editorFocusNode,
                ),
              ),
              _StatusBar(editorState: editorState, language: _currentLanguage),
            ],
          ),
        ),
      ),
      floatingActionButton: FloatingActionButton.small(
        onPressed: () {
          showModalBottomSheet(
            context: context,
            builder: (context) {
              return SafeArea(
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    ListTile(
                      leading: const Icon(Icons.undo),
                      title: const Text('Undo'),
                      onTap: () {
                        Navigator.pop(context);
                        _undo();
                      },
                    ),
                    ListTile(
                      leading: const Icon(Icons.redo),
                      title: const Text('Redo'),
                      onTap: () {
                        Navigator.pop(context);
                        _redo();
                      },
                    ),
                    ListTile(
                      leading: const Icon(Icons.format_align_left),
                      title: const Text('Format Document'),
                      onTap: () {
                        Navigator.pop(context);
                        _formatDocument();
                      },
                    ),
                    ListTile(
                      leading: const Icon(Icons.comment),
                      title: const Text('Toggle Comment'),
                      onTap: () {
                        Navigator.pop(context);
                        _toggleComment();
                      },
                    ),
                  ],
                ),
              );
            },
          );
        },
        child: const Icon(Icons.more_horiz),
      ),
    );
  }
}

class _StatusBar extends StatelessWidget {
  final EditorState editorState;
  final String language;

  const _StatusBar({
    required this.editorState,
    required this.language,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      height: 24,
      color: const Color(0xFF007ACC),
      padding: const EdgeInsets.symmetric(horizontal: 8),
      child: Row(
        children: [
          Text(
            languageLabel(language),
            style: const TextStyle(
                color: Colors.white, fontSize: 11, fontWeight: FontWeight.w500),
          ),
          const Spacer(),
          Text(
            'Ln ${editorState.cursor.line + 1}, Col ${editorState.cursor.column + 1}',
            style: const TextStyle(color: Colors.white, fontSize: 11),
          ),
          const SizedBox(width: 16),
          Text(
            '${editorState.lineCount} lines',
            style: const TextStyle(color: Colors.white, fontSize: 11),
          ),
          const SizedBox(width: 16),
          Text(
            editorState.isModified ? 'Unsaved' : 'Saved',
            style: const TextStyle(color: Colors.white, fontSize: 11),
          ),
        ],
      ),
    );
  }
}

class _SaveIntent extends Intent {
  const _SaveIntent();
}

class _SearchIntent extends Intent {
  const _SearchIntent();
}

class _SelectAllIntent extends Intent {
  const _SelectAllIntent();
}

class _UndoIntent extends Intent {
  const _UndoIntent();
}

class _RedoIntent extends Intent {
  const _RedoIntent();
}
