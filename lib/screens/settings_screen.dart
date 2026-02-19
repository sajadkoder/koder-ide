import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../providers/theme_provider.dart';

class SettingsScreen extends ConsumerWidget {
  const SettingsScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final editorTheme = ref.watch(editorThemeProvider);
    final fontSize = ref.watch(fontSizeProvider);
    final fontFamily = ref.watch(fontFamilyProvider);
    final tabSize = ref.watch(tabSizeProvider);

    final showLineNumbers = ref.watch(showLineNumbersProvider);
    final showMinimap = ref.watch(showMinimapProvider);
    final wordWrap = ref.watch(wordWrapProvider);
    final highlightCurrentLine = ref.watch(highlightCurrentLineProvider);

    final autoSave = ref.watch(autoSaveProvider);
    final autoIndent = ref.watch(autoIndentProvider);
    final smartEditing = ref.watch(smartEditingProvider);

    final themeMode = ref.watch(themeModeProvider);

    return Scaffold(
      appBar: AppBar(title: const Text('Settings')),
      body: ListView(
        children: [
          _buildSectionHeader('Editor'),
          _buildSettingsTile(
            title: 'Theme',
            subtitle: editorTheme.name,
            icon: Icons.palette,
            onTap: () => _showThemeSelector(context, ref),
          ),
          _buildSettingsTile(
            title: 'Font Family',
            subtitle: fontFamily,
            icon: Icons.font_download,
            onTap: () => _showFontSelector(context, ref),
          ),
          _buildSettingsTile(
            title: 'Font Size',
            subtitle: '${fontSize.toInt()} px',
            icon: Icons.text_fields,
            trailing: Row(
              mainAxisSize: MainAxisSize.min,
              children: [
                IconButton(
                  icon: const Icon(Icons.remove),
                  onPressed: () =>
                      ref.read(fontSizeProvider.notifier).decrease(),
                ),
                Text(fontSize.toStringAsFixed(0)),
                IconButton(
                  icon: const Icon(Icons.add),
                  onPressed: () =>
                      ref.read(fontSizeProvider.notifier).increase(),
                ),
              ],
            ),
          ),
          _buildSettingsTile(
            title: 'Tab Size',
            subtitle: '$tabSize spaces',
            icon: Icons.tab,
            trailing: SegmentedButton<int>(
              segments: const [
                ButtonSegment(value: 2, label: Text('2')),
                ButtonSegment(value: 4, label: Text('4')),
                ButtonSegment(value: 8, label: Text('8')),
              ],
              selected: {tabSize},
              onSelectionChanged: (selection) {
                ref.read(tabSizeProvider.notifier).setTabSize(selection.first);
              },
            ),
          ),
          const Divider(),
          _buildSectionHeader('Appearance'),
          SwitchListTile(
            title: const Text('Dark Theme'),
            subtitle: const Text('Use dark appearance across the app'),
            value: themeMode == ThemeMode.dark,
            onChanged: (value) {
              ref.read(themeModeProvider.notifier).setThemeMode(
                    value ? ThemeMode.dark : ThemeMode.light,
                  );
            },
          ),
          SwitchListTile(
            title: const Text('Show Line Numbers'),
            subtitle: const Text('Display line numbers in the gutter'),
            value: showLineNumbers,
            onChanged: (value) {
              ref.read(showLineNumbersProvider.notifier).setValue(value);
            },
          ),
          SwitchListTile(
            title: const Text('Show Minimap'),
            subtitle: const Text('Display a code overview on the right side'),
            value: showMinimap,
            onChanged: (value) {
              ref.read(showMinimapProvider.notifier).setValue(value);
            },
          ),
          SwitchListTile(
            title: const Text('Word Wrap'),
            subtitle:
                const Text('Wrap long lines instead of horizontal scroll'),
            value: wordWrap,
            onChanged: (value) {
              ref.read(wordWrapProvider.notifier).setValue(value);
            },
          ),
          SwitchListTile(
            title: const Text('Highlight Current Line'),
            subtitle:
                const Text('Use visual emphasis around the active editing row'),
            value: highlightCurrentLine,
            onChanged: (value) {
              ref.read(highlightCurrentLineProvider.notifier).setValue(value);
            },
          ),
          const Divider(),
          _buildSectionHeader('Editor Behavior'),
          SwitchListTile(
            title: const Text('Auto Save'),
            subtitle: const Text('Save changes automatically after edits'),
            value: autoSave,
            onChanged: (value) {
              ref.read(autoSaveProvider.notifier).setValue(value);
            },
          ),
          SwitchListTile(
            title: const Text('Auto Indent'),
            subtitle: const Text('Preserve indentation on new lines'),
            value: autoIndent,
            onChanged: (value) {
              ref.read(autoIndentProvider.notifier).setValue(value);
            },
          ),
          SwitchListTile(
            title: const Text('Smart Editing'),
            subtitle: const Text('Auto-close quotes and brackets while typing'),
            value: smartEditing,
            onChanged: (value) {
              ref.read(smartEditingProvider.notifier).setValue(value);
            },
          ),
          const Divider(),
          _buildSectionHeader('About'),
          const ListTile(
            leading: Icon(Icons.info_outline),
            title: Text('Koder'),
            subtitle: Text('Version 1.0.0'),
          ),
          const ListTile(
            leading: Icon(Icons.code),
            title: Text('Flutter + Dart'),
            subtitle: Text('Mobile-first code editor'),
          ),
          ListTile(
            leading: const Icon(Icons.bug_report),
            title: const Text('Report an Issue'),
            subtitle: const Text('github.com/sajadkoder/koder/issues'),
            onTap: () async {
              await Clipboard.setData(
                const ClipboardData(
                    text: 'https://github.com/sajadkoder/koder/issues'),
              );
              if (context.mounted) {
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(
                    content: Text('Issue tracker URL copied to clipboard'),
                    behavior: SnackBarBehavior.floating,
                  ),
                );
              }
            },
          ),
          const SizedBox(height: 24),
        ],
      ),
    );
  }

  Widget _buildSectionHeader(String title) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(16, 16, 16, 8),
      child: Text(
        title.toUpperCase(),
        style: TextStyle(
          fontSize: 12,
          fontWeight: FontWeight.bold,
          color: Colors.grey.shade500,
          letterSpacing: 1.2,
        ),
      ),
    );
  }

  Widget _buildSettingsTile({
    required String title,
    required String subtitle,
    required IconData icon,
    Widget? trailing,
    VoidCallback? onTap,
  }) {
    return ListTile(
      leading: Icon(icon),
      title: Text(title),
      subtitle: Text(subtitle),
      trailing: trailing ?? const Icon(Icons.chevron_right),
      onTap: onTap,
    );
  }

  void _showThemeSelector(BuildContext context, WidgetRef ref) {
    showModalBottomSheet(
      context: context,
      builder: (context) {
        return SafeArea(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              const Padding(
                padding: EdgeInsets.all(16),
                child: Text(
                  'Select Theme',
                  style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
                ),
              ),
              _buildThemeOption(context, ref, 'VS Code Dark+', 'vscode_dark',
                  EditorTheme.vscodeDark()),
              _buildThemeOption(
                  context, ref, 'Monokai', 'monokai', EditorTheme.monokai()),
              _buildThemeOption(
                  context, ref, 'One Dark', 'one_dark', EditorTheme.oneDark()),
            ],
          ),
        );
      },
    );
  }

  Widget _buildThemeOption(
    BuildContext context,
    WidgetRef ref,
    String name,
    String key,
    EditorTheme theme,
  ) {
    final current = ref.watch(editorThemeProvider);
    final isSelected = current.name == name;

    return ListTile(
      leading: Container(
        width: 40,
        height: 40,
        decoration: BoxDecoration(
          color: theme.background,
          borderRadius: BorderRadius.circular(8),
          border: Border.all(
            color: isSelected ? const Color(0xFF007ACC) : Colors.grey.shade700,
            width: isSelected ? 2 : 1,
          ),
        ),
        child: Center(
          child: Text(
            'Aa',
            style:
                TextStyle(color: theme.foreground, fontWeight: FontWeight.bold),
          ),
        ),
      ),
      title: Text(name),
      trailing:
          isSelected ? const Icon(Icons.check, color: Color(0xFF007ACC)) : null,
      onTap: () {
        ref.read(editorThemeProvider.notifier).setThemeByName(key);
        Navigator.pop(context);
      },
    );
  }

  void _showFontSelector(BuildContext context, WidgetRef ref) {
    final fonts = ['JetBrainsMono', 'FiraCode'];
    final current = ref.read(fontFamilyProvider);

    showModalBottomSheet(
      context: context,
      builder: (context) {
        return SafeArea(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              const Padding(
                padding: EdgeInsets.all(16),
                child: Text(
                  'Select Font',
                  style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
                ),
              ),
              ...fonts.map((font) {
                final isSelected = font == current;
                return ListTile(
                  title: Text(font, style: TextStyle(fontFamily: font)),
                  trailing: isSelected
                      ? const Icon(Icons.check, color: Color(0xFF007ACC))
                      : null,
                  onTap: () {
                    ref.read(fontFamilyProvider.notifier).setFontFamily(font);
                    Navigator.pop(context);
                  },
                );
              }),
            ],
          ),
        );
      },
    );
  }
}
