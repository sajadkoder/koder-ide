# Koder IDE

<div align="center">

**A professional, native Android code editor/IDE**

[![Kotlin](https://img.shields.io/badge/Kotlin-2.1.0-purple.svg)](https://kotlinlang.org)
[![Android](https://img.shields.io/badge/Android-26%2B-green.svg)](https://developer.android.com)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

</div>

---

## Overview

Koder IDE is a powerful code editor for Android devices. Built with Kotlin and Jetpack Compose, it provides a professional coding experience with syntax highlighting, file management, terminal, and Git integration.

## Features

### Code Editor
- **Syntax Highlighting** - Powered by Sora Editor with TextMate grammars
- **Multi-language Support** - Java, Kotlin, Python, JavaScript, TypeScript, JSON, XML, HTML, CSS, Markdown, C/C++, YAML, SQL
- **Undo/Redo** - Full undo/redo support
- **Line Numbers** - Display line numbers
- **File Tabs** - Work with multiple files simultaneously

### File Explorer
- **Project Navigation** - Full directory tree view
- **File Operations** - Create, delete files and folders
- **Quick Access** - Open files from anywhere on your device

### Terminal
- **Shell Execution** - Run shell commands directly
- **Command History** - Execute common commands

### Git Integration
- **Repository Init** - Initialize new repositories
- **Stage & Commit** - Stage files and commit changes
- **Push/Pull** - Push and pull from remote

### UI/UX
- **Dark Theme** - Beautiful dark theme inspired by GitHub Dark
- **Material 3** - Modern Material Design components
- **Responsive** - Works on phones and tablets

## Architecture

```
com.koder.ide/
├── domain/
│   ├── model/       # Data models (EditorTab, ProjectFile, GitStatus)
│   └── repository/  # Repository interfaces
├── data/
│   └── repository/   # Repository implementations
├── core/
│   ├── di/          # Dependency Injection (Hilt)
│   ├── editor/      # Sora Editor wrapper
│   └── util/        # Utility classes
└── presentation/
    ├── main/        # Main screen, ViewModel
    └── theme/       # App theming
```

## Tech Stack

| Category | Technologies |
|----------|-------------|
| Language | Kotlin 2.1.0 |
| UI | Jetpack Compose, Material Design 3 |
| Architecture | Clean Architecture + MVVM |
| DI | Hilt |
| Async | Coroutines, Flow |
| Editor | Sora Editor 0.24.4 |
| Git | JGit |

## Building

### Prerequisites
- Android Studio (latest version)
- JDK 17
- Android SDK 35

### Build Steps

```bash
# Clone the repository
git clone https://github.com/sajadkoder/koder-ide.git
cd koder-ide

# Build debug APK
./gradlew assembleDebug

# APK will be at:
# app/build/outputs/apk/debug/app-debug.apk
```

## Supported Languages

| Language | Extension | Syntax Highlighting |
|----------|-----------|---------------------|
| Java | .java | ✅ |
| Kotlin | .kt, .kts | ✅ |
| Python | .py | ✅ |
| JavaScript | .js | ✅ |
| TypeScript | .ts | ✅ |
| JSON | .json | ✅ |
| XML | .xml | ✅ |
| HTML | .html | ✅ |
| CSS | .css | ✅ |
| Markdown | .md | ✅ |
| C/C++ | .c, .cpp, .h | ✅ |
| YAML | .yaml, .yml | ✅ |
| SQL | .sql | ✅ |
| Shell | .sh | ✅ |

## Roadmap

- [x] Terminal emulator
- [x] Git integration
- [ ] LSP support
- [ ] Build/Run projects
- [ ] Plugin system
- [ ] Custom themes
- [ ] Find & Replace

## Credits

- [Sora Editor](https://github.com/Rosemoe/sora-editor) - High-performance code editor library
- [Termux](https://github.com/termux/termux-app) - Terminal emulator reference
- [Acode](https://github.com/Acode-Foundation/Acode) - UI/UX inspiration

## License

MIT License

Copyright (c) 2025 Koder IDE

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

---

<div align="center">

Made with ❤️ by [Sajad Koder](https://github.com/sajadkoder)

</div>
