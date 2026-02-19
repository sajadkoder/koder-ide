import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_code_editor/flutter_code_editor.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../providers/editor_provider.dart';

class SearchPanel extends ConsumerStatefulWidget {
  final CodeController controller;

  const SearchPanel({super.key, required this.controller});

  @override
  ConsumerState<SearchPanel> createState() => _SearchPanelState();
}

class _SearchPanelState extends ConsumerState<SearchPanel> {
  late final TextEditingController _searchController;
  late final TextEditingController _replaceController;
  final FocusNode _searchFocusNode = FocusNode();

  @override
  void initState() {
    super.initState();
    final searchState = ref.read(searchStateProvider);
    _searchController = TextEditingController(text: searchState.pattern);
    _replaceController = TextEditingController(text: searchState.replacement);
    _searchController.addListener(_onSearchChanged);
    _replaceController.addListener(_onReplacementChanged);

    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (mounted) {
        _searchFocusNode.requestFocus();
      }
    });
  }

  @override
  void dispose() {
    _searchController.removeListener(_onSearchChanged);
    _replaceController.removeListener(_onReplacementChanged);
    _searchController.dispose();
    _replaceController.dispose();
    _searchFocusNode.dispose();
    super.dispose();
  }

  void _onSearchChanged() {
    final notifier = ref.read(searchStateProvider.notifier);
    notifier.setPattern(_searchController.text);
    notifier.searchIn(widget.controller.text);
    _selectCurrentMatch();
  }

  void _onReplacementChanged() {
    ref
        .read(searchStateProvider.notifier)
        .setReplacement(_replaceController.text);
  }

  void _selectCurrentMatch() {
    final current = ref.read(searchStateProvider).currentResult;
    if (current == null) return;

    widget.controller.selection = TextSelection(
      baseOffset: current.startOffset,
      extentOffset: current.endOffset,
    );
  }

  void _nextMatch() {
    final notifier = ref.read(searchStateProvider.notifier);
    notifier.nextResult();
    _selectCurrentMatch();
  }

  void _previousMatch() {
    final notifier = ref.read(searchStateProvider.notifier);
    notifier.previousResult();
    _selectCurrentMatch();
  }

  void _replaceCurrent() {
    final notifier = ref.read(searchStateProvider.notifier);
    final newText = notifier.replaceCurrentInText(
        widget.controller.text, _replaceController.text);
    if (newText == widget.controller.text) {
      return;
    }

    widget.controller.text = newText;
    notifier.searchIn(widget.controller.text);
    _selectCurrentMatch();
  }

  void _replaceAll() {
    final notifier = ref.read(searchStateProvider.notifier);
    final newText = notifier.replaceAllInText(
        widget.controller.text, _replaceController.text);
    if (newText == widget.controller.text) {
      return;
    }

    widget.controller.text = newText;
    notifier.searchIn(widget.controller.text);
    _selectCurrentMatch();
  }

  @override
  Widget build(BuildContext context) {
    final searchState = ref.watch(searchStateProvider);

    return Container(
      color: const Color(0xFF252526),
      padding: const EdgeInsets.all(8),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Row(
            children: [
              Expanded(
                child: TextField(
                  controller: _searchController,
                  focusNode: _searchFocusNode,
                  decoration: InputDecoration(
                    hintText: 'Search',
                    hintStyle: TextStyle(color: Colors.grey.shade500),
                    prefixIcon: const Icon(Icons.search, size: 18),
                    suffixIcon: Row(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        IconButton(
                          icon: Icon(
                            Icons.text_fields,
                            size: 18,
                            color: searchState.caseSensitive
                                ? const Color(0xFF007ACC)
                                : Colors.grey,
                          ),
                          onPressed: () {
                            final notifier =
                                ref.read(searchStateProvider.notifier);
                            notifier.toggleCaseSensitive();
                            notifier.searchIn(widget.controller.text);
                            _selectCurrentMatch();
                          },
                          tooltip: 'Match Case',
                        ),
                        IconButton(
                          icon: Icon(
                            Icons.wrap_text,
                            size: 18,
                            color: searchState.wholeWord
                                ? const Color(0xFF007ACC)
                                : Colors.grey,
                          ),
                          onPressed: () {
                            final notifier =
                                ref.read(searchStateProvider.notifier);
                            notifier.toggleWholeWord();
                            notifier.searchIn(widget.controller.text);
                            _selectCurrentMatch();
                          },
                          tooltip: 'Match Whole Word',
                        ),
                        IconButton(
                          icon: Icon(
                            Icons.code,
                            size: 18,
                            color: searchState.useRegex
                                ? const Color(0xFF007ACC)
                                : Colors.grey,
                          ),
                          onPressed: () {
                            final notifier =
                                ref.read(searchStateProvider.notifier);
                            notifier.toggleRegex();
                            notifier.searchIn(widget.controller.text);
                            _selectCurrentMatch();
                          },
                          tooltip: 'Use Regex',
                        ),
                      ],
                    ),
                    contentPadding: const EdgeInsets.symmetric(horizontal: 8),
                    isDense: true,
                  ),
                  onSubmitted: (_) => _nextMatch(),
                  style: const TextStyle(fontSize: 14),
                ),
              ),
              const SizedBox(width: 8),
              Text(
                searchState.results.isEmpty
                    ? 'No results'
                    : '${searchState.currentResultIndex + 1} of ${searchState.results.length}',
                style: TextStyle(color: Colors.grey.shade400, fontSize: 12),
              ),
              IconButton(
                icon: const Icon(Icons.keyboard_arrow_up, size: 18),
                onPressed:
                    searchState.results.isNotEmpty ? _previousMatch : null,
                tooltip: 'Previous Match',
              ),
              IconButton(
                icon: const Icon(Icons.keyboard_arrow_down, size: 18),
                onPressed: searchState.results.isNotEmpty ? _nextMatch : null,
                tooltip: 'Next Match',
              ),
              IconButton(
                icon: const Icon(Icons.close, size: 18),
                onPressed: () {
                  ref.read(searchStateProvider.notifier).hide();
                },
                tooltip: 'Close',
              ),
            ],
          ),
          if (searchState.error != null)
            Padding(
              padding: const EdgeInsets.only(top: 4),
              child: Row(
                children: [
                  Icon(Icons.error_outline,
                      size: 14, color: Colors.red.shade300),
                  const SizedBox(width: 6),
                  Expanded(
                    child: Text(
                      searchState.error!,
                      style:
                          TextStyle(color: Colors.red.shade300, fontSize: 11),
                    ),
                  ),
                ],
              ),
            ),
          const SizedBox(height: 8),
          Row(
            children: [
              Expanded(
                child: TextField(
                  controller: _replaceController,
                  decoration: InputDecoration(
                    hintText: 'Replace',
                    hintStyle: TextStyle(color: Colors.grey.shade500),
                    prefixIcon: const Icon(Icons.find_replace, size: 18),
                    contentPadding: const EdgeInsets.symmetric(horizontal: 8),
                    isDense: true,
                  ),
                  style: const TextStyle(fontSize: 14),
                  onSubmitted: (_) => _replaceCurrent(),
                ),
              ),
              const SizedBox(width: 8),
              TextButton(
                onPressed:
                    searchState.results.isNotEmpty ? _replaceCurrent : null,
                child: const Text('Replace'),
              ),
              const SizedBox(width: 4),
              TextButton(
                onPressed: searchState.results.isNotEmpty ? _replaceAll : null,
                child: const Text('Replace All'),
              ),
            ],
          ),
          const SizedBox(height: 4),
          Row(
            children: [
              TextButton.icon(
                onPressed: () async {
                  await Clipboard.setData(
                      ClipboardData(text: _searchController.text));
                },
                icon: const Icon(Icons.copy, size: 14),
                label: const Text('Copy query'),
              ),
            ],
          ),
        ],
      ),
    );
  }
}
