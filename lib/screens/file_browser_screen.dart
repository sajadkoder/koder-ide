import 'dart:io';

import 'package:file_picker/file_picker.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../providers/editor_provider.dart';
import '../providers/navigation_provider.dart';

class FileItem {
  final String name;
  final String path;
  final bool isDirectory;
  final DateTime modified;
  final int? size;

  const FileItem({
    required this.name,
    required this.path,
    required this.isDirectory,
    required this.modified,
    this.size,
  });
}

class FileBrowserScreen extends ConsumerStatefulWidget {
  const FileBrowserScreen({super.key});

  @override
  ConsumerState<FileBrowserScreen> createState() => _FileBrowserScreenState();
}

class _FileBrowserScreenState extends ConsumerState<FileBrowserScreen> {
  List<FileItem> _files = const [];
  String _currentPath = '';
  bool _isLoading = false;

  Future<void> _pickDirectory() async {
    try {
      final result = await FilePicker.platform.getDirectoryPath();
      if (result == null) return;

      setState(() {
        _currentPath = result;
      });
      await _loadFiles(result);
    } catch (e) {
      _showError('Failed to pick directory: $e');
    }
  }

  Future<void> _loadFiles(String path) async {
    setState(() {
      _isLoading = true;
    });

    try {
      final dir = Directory(path);
      final entities = await dir.list().toList();

      final mapped = entities.map((entity) {
        final stat = entity.statSync();
        return FileItem(
          name: entity.path.split(Platform.pathSeparator).last,
          path: entity.path,
          isDirectory: entity is Directory,
          modified: stat.modified,
          size: entity is File ? stat.size : null,
        );
      }).toList();

      mapped.sort((a, b) {
        if (a.isDirectory && !b.isDirectory) return -1;
        if (!a.isDirectory && b.isDirectory) return 1;
        return a.name.toLowerCase().compareTo(b.name.toLowerCase());
      });

      setState(() {
        _files = mapped;
        _isLoading = false;
      });
    } catch (e) {
      setState(() {
        _isLoading = false;
      });
      _showError('Failed to load files: $e');
    }
  }

  Future<void> _openFile(FileItem file) async {
    if (file.isDirectory) {
      setState(() {
        _currentPath = file.path;
      });
      await _loadFiles(file.path);
      return;
    }

    try {
      await ref.read(editorStateProvider.notifier).openFile(file.path);
      ref.read(mainNavIndexProvider.notifier).state = 0;
      _showMessage('Opened ${file.name}');
    } catch (e) {
      _showError('Failed to open file: $e');
    }
  }

  Future<void> _goUp() async {
    if (_currentPath.isEmpty) return;

    final parent = Directory(_currentPath).parent.path;
    if (parent == _currentPath) {
      setState(() {
        _currentPath = '';
        _files = const [];
      });
      return;
    }

    setState(() {
      _currentPath = parent;
    });
    await _loadFiles(parent);
  }

  Future<void> _createNewFile() async {
    if (_currentPath.isEmpty) return;

    final controller = TextEditingController();
    final name = await showDialog<String>(
      context: context,
      builder: (context) {
        return AlertDialog(
          title: const Text('New File'),
          content: TextField(
            controller: controller,
            autofocus: true,
            decoration: const InputDecoration(hintText: 'Enter file name'),
          ),
          actions: [
            TextButton(
                onPressed: () => Navigator.pop(context),
                child: const Text('Cancel')),
            FilledButton(
                onPressed: () => Navigator.pop(context, controller.text),
                child: const Text('Create')),
          ],
        );
      },
    );

    if (name == null || name.trim().isEmpty) return;

    final filePath = '$_currentPath${Platform.pathSeparator}${name.trim()}';
    try {
      await File(filePath).create();
      await _loadFiles(_currentPath);
      _showMessage('Created $name');
    } catch (e) {
      _showError('Failed to create file: $e');
    }
  }

  Future<void> _createNewFolder() async {
    if (_currentPath.isEmpty) return;

    final controller = TextEditingController();
    final name = await showDialog<String>(
      context: context,
      builder: (context) {
        return AlertDialog(
          title: const Text('New Folder'),
          content: TextField(
            controller: controller,
            autofocus: true,
            decoration: const InputDecoration(hintText: 'Enter folder name'),
          ),
          actions: [
            TextButton(
                onPressed: () => Navigator.pop(context),
                child: const Text('Cancel')),
            FilledButton(
                onPressed: () => Navigator.pop(context, controller.text),
                child: const Text('Create')),
          ],
        );
      },
    );

    if (name == null || name.trim().isEmpty) return;

    final folderPath = '$_currentPath${Platform.pathSeparator}${name.trim()}';
    try {
      await Directory(folderPath).create();
      await _loadFiles(_currentPath);
      _showMessage('Created folder $name');
    } catch (e) {
      _showError('Failed to create folder: $e');
    }
  }

  Future<void> _renameFile(FileItem file) async {
    final controller = TextEditingController(text: file.name);
    final newName = await showDialog<String>(
      context: context,
      builder: (context) {
        return AlertDialog(
          title: const Text('Rename'),
          content: TextField(
            controller: controller,
            autofocus: true,
            decoration: const InputDecoration(hintText: 'Enter new name'),
          ),
          actions: [
            TextButton(
                onPressed: () => Navigator.pop(context),
                child: const Text('Cancel')),
            FilledButton(
                onPressed: () => Navigator.pop(context, controller.text),
                child: const Text('Rename')),
          ],
        );
      },
    );

    if (newName == null || newName.trim().isEmpty) return;

    final parent =
        file.path.substring(0, file.path.lastIndexOf(Platform.pathSeparator));
    final newPath = '$parent${Platform.pathSeparator}${newName.trim()}';

    try {
      if (file.isDirectory) {
        await Directory(file.path).rename(newPath);
      } else {
        await File(file.path).rename(newPath);
      }
      await _loadFiles(_currentPath);
      _showMessage('Renamed to $newName');
    } catch (e) {
      _showError('Failed to rename: $e');
    }
  }

  Future<void> _deleteFile(FileItem file) async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (context) {
        return AlertDialog(
          title: const Text('Delete'),
          content: Text('Are you sure you want to delete "${file.name}"?'),
          actions: [
            TextButton(
                onPressed: () => Navigator.pop(context, false),
                child: const Text('Cancel')),
            FilledButton(
              style: FilledButton.styleFrom(backgroundColor: Colors.red),
              onPressed: () => Navigator.pop(context, true),
              child: const Text('Delete'),
            ),
          ],
        );
      },
    );

    if (confirmed != true) return;

    try {
      if (file.isDirectory) {
        await Directory(file.path).delete(recursive: true);
      } else {
        await File(file.path).delete();
      }
      await _loadFiles(_currentPath);
      _showMessage('Deleted ${file.name}');
    } catch (e) {
      _showError('Failed to delete: $e');
    }
  }

  void _showFileOptions(FileItem file) {
    showModalBottomSheet(
      context: context,
      builder: (context) {
        return SafeArea(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              ListTile(
                leading: const Icon(Icons.edit),
                title: const Text('Rename'),
                onTap: () {
                  Navigator.pop(context);
                  _renameFile(file);
                },
              ),
              ListTile(
                leading: const Icon(Icons.copy),
                title: const Text('Copy Path'),
                onTap: () async {
                  Navigator.pop(context);
                  await Clipboard.setData(ClipboardData(text: file.path));
                  _showMessage('Path copied');
                },
              ),
              ListTile(
                leading: const Icon(Icons.delete, color: Colors.red),
                title:
                    const Text('Delete', style: TextStyle(color: Colors.red)),
                onTap: () {
                  Navigator.pop(context);
                  _deleteFile(file);
                },
              ),
            ],
          ),
        );
      },
    );
  }

  void _showMessage(String message) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text(message), behavior: SnackBarBehavior.floating),
    );
  }

  void _showError(String message) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(message),
        behavior: SnackBarBehavior.floating,
        backgroundColor: Colors.red,
      ),
    );
  }

  IconData _getFileIcon(FileItem file) {
    if (file.isDirectory) return Icons.folder;

    final ext = file.name.split('.').last.toLowerCase();
    switch (ext) {
      case 'js':
      case 'jsx':
        return Icons.javascript;
      case 'html':
        return Icons.html;
      case 'css':
        return Icons.css;
      case 'json':
        return Icons.data_object;
      case 'md':
        return Icons.description;
      case 'png':
      case 'jpg':
      case 'jpeg':
      case 'gif':
        return Icons.image;
      default:
        return Icons.insert_drive_file;
    }
  }

  String _formatFileSize(int bytes) {
    if (bytes < 1024) {
      return '$bytes B';
    }
    if (bytes < 1024 * 1024) {
      return '${(bytes / 1024).toStringAsFixed(1)} KB';
    }
    if (bytes < 1024 * 1024 * 1024) {
      return '${(bytes / (1024 * 1024)).toStringAsFixed(1)} MB';
    }
    return '${(bytes / (1024 * 1024 * 1024)).toStringAsFixed(1)} GB';
  }

  @override
  Widget build(BuildContext context) {
    final recentFiles =
        ref.watch(editorStateProvider.select((s) => s.recentFiles));

    return Scaffold(
      appBar: AppBar(
        title: const Text('Files'),
        actions: [
          IconButton(
            icon: const Icon(Icons.create_new_folder_outlined),
            onPressed: _currentPath.isNotEmpty ? _createNewFolder : null,
            tooltip: 'New Folder',
          ),
          IconButton(
            icon: const Icon(Icons.add_circle_outline),
            onPressed: _currentPath.isNotEmpty ? _createNewFile : null,
            tooltip: 'New File',
          ),
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed:
                _currentPath.isNotEmpty ? () => _loadFiles(_currentPath) : null,
            tooltip: 'Refresh',
          ),
        ],
      ),
      body: Column(
        children: [
          if (_currentPath.isNotEmpty)
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
              color: const Color(0xFF252526),
              child: Row(
                children: [
                  IconButton(
                    icon: const Icon(Icons.arrow_upward, size: 18),
                    onPressed: _goUp,
                    tooltip: 'Go Up',
                  ),
                  Expanded(
                    child: Text(
                      _currentPath,
                      style: const TextStyle(
                          fontFamily: 'JetBrainsMono', fontSize: 12),
                      overflow: TextOverflow.ellipsis,
                    ),
                  ),
                ],
              ),
            ),
          Expanded(
            child: _isLoading
                ? const Center(child: CircularProgressIndicator())
                : _currentPath.isEmpty
                    ? _buildWelcomeView(recentFiles)
                    : _files.isEmpty
                        ? const Center(child: Text('Empty folder'))
                        : ListView.builder(
                            itemCount: _files.length,
                            itemBuilder: (context, index) {
                              final file = _files[index];
                              return ListTile(
                                leading: Icon(_getFileIcon(file), size: 20),
                                title: Text(file.name),
                                subtitle: Text(
                                  file.isDirectory
                                      ? 'Folder'
                                      : _formatFileSize(file.size ?? 0),
                                  style: TextStyle(
                                      fontSize: 11,
                                      color: Colors.grey.shade500),
                                ),
                                trailing: file.isDirectory
                                    ? const Icon(Icons.chevron_right, size: 18)
                                    : null,
                                onTap: () => _openFile(file),
                                onLongPress: () => _showFileOptions(file),
                              );
                            },
                          ),
          ),
        ],
      ),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: _pickDirectory,
        icon: const Icon(Icons.folder_open),
        label: const Text('Open Folder'),
      ),
    );
  }

  Widget _buildWelcomeView(List<String> recentFiles) {
    return Center(
      child: SingleChildScrollView(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(Icons.folder_open, size: 64, color: Colors.grey.shade600),
            const SizedBox(height: 16),
            Text(
              'Open a folder to browse files',
              style: TextStyle(fontSize: 16, color: Colors.grey.shade500),
            ),
            const SizedBox(height: 32),
            if (recentFiles.isNotEmpty) ...[
              Text(
                'Recent Files',
                style: TextStyle(
                    fontSize: 14,
                    fontWeight: FontWeight.bold,
                    color: Colors.grey.shade400),
              ),
              const SizedBox(height: 8),
              ...recentFiles.take(8).map((path) {
                final name = path.split(Platform.pathSeparator).last;
                return ListTile(
                  leading: const Icon(Icons.history, size: 20),
                  title: Text(name),
                  subtitle: Text(
                    path,
                    style: TextStyle(fontSize: 11, color: Colors.grey.shade500),
                    overflow: TextOverflow.ellipsis,
                  ),
                  onTap: () async {
                    try {
                      await ref
                          .read(editorStateProvider.notifier)
                          .openFile(path);
                      ref.read(mainNavIndexProvider.notifier).state = 0;
                    } catch (e) {
                      _showError('Failed to open file: $e');
                      await ref
                          .read(editorStateProvider.notifier)
                          .removeRecentFile(path);
                    }
                  },
                );
              }),
            ],
          ],
        ),
      ),
    );
  }
}
