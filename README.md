# Koder IDE

<div align="center">

![Koder Logo](app/src/main/res/drawable/ic_launcher_foreground.xml)

**A professional, full-featured Android IDE for developers**

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.22-purple.svg)](https://kotlinlang.org)
[![Android](https://img.shields.io/badge/Android-26%2B-green.svg)](https://developer.android.com)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

[Features](#features) • [Screenshots](#screenshots) • [Download](#download) • [Building](#building) • [Contributing](#contributing)

</div>

---

## Overview

Koder IDE is a powerful, feature-rich integrated development environment designed specifically for Android devices. Built with modern Android development practices and Material Design 3, it provides a professional coding experience comparable to desktop IDEs like Android Studio.

## Features

### Code Editor
- **Syntax Highlighting** - Support for 50+ programming languages
- **Code Completion** - Intelligent auto-complete suggestions
- **Code Formatting** - Automatic code formatting
- **Bracket Matching** - Visual matching for brackets and quotes
- **Line Numbers** - Toggle line number display
- **Minimap** - Code overview panel
- **Multiple Cursors** - Edit multiple locations simultaneously
- **Undo/Redo** - Full undo/redo support
- **Find & Replace** - Search and replace with regex support
- **Go to Definition** - Navigate to symbol definitions
- **Code Folding** - Collapse/expand code blocks

### File Explorer
- **Project Navigation** - Full directory tree view
- **File Operations** - Create, delete, rename, copy, move
- **Multiple Tabs** - Work with multiple files simultaneously
- **Recent Files** - Quick access to recently opened files
- **File Search** - Search files by name or content
- **Git Status** - Visual git status indicators

### Git Integration
- **Repository Management** - Initialize, clone repositories
- **Commit & Push** - Full git workflow support
- **Branch Management** - Create, switch, merge branches
- **Diff Viewer** - Compare file changes
- **Conflict Resolution** - Merge conflict handling
- **Git Log** - View commit history

### Terminal Emulator
- **Full Shell Access** - Execute shell commands
- **Session Management** - Multiple terminal sessions
- **Custom Shell** - Configurable shell environment
- **Command History** - Access previous commands
- **Copy/Paste** - Terminal content management

### Build System
- **Gradle Support** - Build Android projects
- **CMake Integration** - Native code compilation
- **Make Support** - Unix build systems
- **Build Output** - Detailed build logs
- **Error Navigation** - Jump to build errors

### Customization
- **13 Editor Themes** - Monokai, Dracula, One Dark, and more
- **System Theme** - Light/Dark mode support
- **Custom Fonts** - JetBrains Mono, Fira Code, and more
- **Configurable Settings** - Font size, tab size, line endings
- **Keybindings** - Customizable keyboard shortcuts

### Supported Languages
| Language | Extension | Syntax | Completion |
|----------|-----------|--------|------------|
| Kotlin | .kt, .kts | ✅ | ✅ |
| Java | .java | ✅ | ✅ |
| Python | .py | ✅ | ✅ |
| JavaScript | .js | ✅ | ✅ |
| TypeScript | .ts, .tsx | ✅ | ✅ |
| C/C++ | .c, .cpp, .h | ✅ | ✅ |
| Go | .go | ✅ | ✅ |
| Rust | .rs | ✅ | ✅ |
| HTML/CSS | .html, .css | ✅ | ✅ |
| JSON/XML | .json, .xml | ✅ | ✅ |
| Markdown | .md | ✅ | - |
| Shell | .sh, .bash | ✅ | - |
| SQL | .sql | ✅ | ✅ |
| And 40+ more... | | | |

## Architecture

Koder IDE follows **Clean Architecture** principles with clear separation of concerns:

```
app/src/main/java/com/koder/ide/
├── core/                    # Core utilities and services
│   ├── di/                  # Dependency Injection (Hilt modules)
│   ├── extension/           # Kotlin extensions
│   ├── native/              # Native C++ code interface
│   ├── security/            # Security utilities
│   ├── service/             # Background services
│   └── util/                # Utility classes
├── data/                    # Data layer
│   ├── local/               # Local data sources
│   │   ├── dao/             # Room DAOs
│   │   ├── database/        # Database configuration
│   │   └── entity/          # Database entities
│   ├── remote/              # Remote data sources
│   └── repository/          # Repository implementations
├── domain/                  # Domain layer
│   ├── model/               # Domain models
│   ├── repository/          # Repository interfaces
│   └── usecase/             # Use cases
└── presentation/            # UI layer (Jetpack Compose)
    ├── components/          # Reusable UI components
    ├── editor/              # Code editor screens
    ├── explorer/            # File explorer screens
    ├── git/                 # Git integration screens
    ├── main/                # Main activity and navigation
    ├── settings/            # Settings screens
    ├── terminal/            # Terminal screens
    └── theme/               # App theming
```

## Tech Stack

| Category | Technologies |
|----------|-------------|
| Language | Kotlin 1.9.22 |
| UI | Jetpack Compose, Material Design 3 |
| Architecture | MVVM + Clean Architecture |
| DI | Hilt |
| Database | Room |
| Async | Coroutines, Flow |
| Navigation | Navigation Compose |
| Git | JGit |
| Editor | Sora Editor |
| Serialization | Gson |
| Image Loading | Coil |
| Networking | Retrofit, OkHttp |

## Building

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- Android SDK 34
- NDK 25.x (for native code)
- CMake 3.22.1

### Build Steps

```bash
# Clone the repository
git clone https://github.com/sajadkoder/koder.git
cd koder

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Run tests
./gradlew test

# Run lint
./gradlew lint
```

### Build Variants
| Variant | Description |
|---------|-------------|
| `debug` | Debug build with logging enabled |
| `release` | Optimized release build |

## Project Structure

```
koder/
├── app/                           # Main application module
│   ├── src/main/
│   │   ├── java/                  # Kotlin source code
│   │   ├── res/                   # Android resources
│   │   ├── cpp/                   # Native C++ code
│   │   └── AndroidManifest.xml    # App manifest
│   ├── build.gradle.kts           # Module build configuration
│   └── proguard-rules.pro         # ProGuard rules
├── gradle/                        # Gradle wrapper
├── build.gradle.kts               # Project build configuration
├── settings.gradle.kts            # Project settings
└── README.md                      # This file
```

## Contributing

We welcome contributions! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Code Style
- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful variable and function names
- Add documentation for public APIs
- Write unit tests for new features

## Roadmap

- [ ] LSP (Language Server Protocol) integration
- [ ] AI-assisted code completion
- [ ] Plugin/extension system
- [ ] Remote development support
- [ ] Collaborative editing
- [ ] Debugging support
- [ ] Performance profiling tools
- [ ] Database browser
- [ ] REST client
- [ ] More themes and customization options

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- [Sora Editor](https://github.com/Rosemoe/sora-editor) - Code editor library
- [JGit](https://www.eclipse.org/jgit/) - Git implementation
- [Material Design 3](https://m3.material.io/) - Design system
- [Jetpack Compose](https://developer.android.com/jetpack/compose) - Modern UI toolkit

---

<div align="center">

Made with ❤️ by [Sajad Koder](https://github.com/sajadkoder)

[⬆ Back to Top](#koder-ide)

</div>
