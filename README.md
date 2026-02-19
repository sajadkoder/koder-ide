# Koder - Flutter Mobile Code Editor

A feature-rich, Flutter + Dart mobile code editor for Android and iOS.

## Highlights

- Flutter-first architecture (no runtime Rust dependency)
- Syntax highlighting editor with multiple languages
- Search and replace with:
  - case-sensitive toggle
  - whole-word toggle
  - regex toggle
- File operations:
  - open file
  - save
  - save as
  - create file/folder
  - rename/delete
  - recent files
- Editor productivity:
  - undo/redo
  - select all
  - copy/paste
  - format document (whitespace cleanup)
  - comment toggle
  - keyboard shortcuts (Ctrl+S/F/A/Z/Y)
- Configurable editor options (persisted in `shared_preferences`):
  - theme (VS Code Dark+, Monokai, One Dark)
  - font family + size
  - tab size
  - line numbers
  - minimap
  - word wrap
  - highlight current line accent
  - auto-save
  - auto-indent
  - smart editing
- Cross-platform project scaffolding for Android and iOS
- Branded app icon (`K` logo) applied to Android and iOS icon assets

## Architecture

```text
Flutter UI (Screens + Widgets)
        |
Riverpod State Layer
  - editor state
  - search state
  - theme/settings state
  - navigation state
        |
Dart Services
  - file system I/O (dart:io)
  - settings persistence (shared_preferences)
  - syntax language mapping (highlight)
```

## Tech Stack

- Flutter (Material 3)
- Dart
- Riverpod
- flutter_code_editor
- highlight
- shared_preferences
- file_picker

## Project Structure

```text
lib/
  app.dart
  main.dart
  providers/
    editor_provider.dart
    navigation_provider.dart
    theme_provider.dart
  screens/
    editor_screen.dart
    file_browser_screen.dart
    settings_screen.dart
  utils/
    language_utils.dart
  widgets/
    code_editor.dart
    search_panel.dart
    autocomplete_popup.dart
assets/
  images/
  fonts/
android/
ios/
```

## Getting Started

### 1. Prerequisites

- Flutter SDK 3.x+
- Android SDK (for APK builds)
- Xcode (for iOS builds, macOS only)

### 2. Install dependencies

```bash
flutter pub get
```

### 3. Run in debug

```bash
flutter run
```

### 4. Build release APK

```bash
flutter build apk --release
```

Generated APK path:

```text
build/app/outputs/flutter-apk/app-release.apk
```

### 5. Build iOS (macOS only)

```bash
flutter build ios --release
```

## Implemented Settings

All settings below are persisted:

- `theme_mode`
- `editor_theme`
- `font_size`
- `font_family`
- `tab_size`
- `show_line_numbers`
- `show_minimap`
- `word_wrap`
- `highlight_current_line`
- `auto_save`
- `auto_indent`
- `smart_editing`

## Notes

- The repository still contains `rust_core/` from earlier architecture drafts, but the app runtime now runs fully in Flutter/Dart.
- iOS binaries require macOS + Xcode, but iOS assets/project files are already prepared.

## License

MIT
