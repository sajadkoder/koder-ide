import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:google_fonts/google_fonts.dart';

import 'providers/navigation_provider.dart';
import 'providers/theme_provider.dart';
import 'screens/editor_screen.dart';
import 'screens/file_browser_screen.dart';
import 'screens/settings_screen.dart';

class KoderApp extends ConsumerWidget {
  const KoderApp({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final themeMode = ref.watch(themeModeProvider);

    return MaterialApp(
      title: 'Koder',
      debugShowCheckedModeBanner: false,
      theme: AppTheme.lightTheme(),
      darkTheme: AppTheme.darkTheme(),
      themeMode: themeMode,
      home: const MainScreen(),
    );
  }
}

class MainScreen extends ConsumerWidget {
  const MainScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final currentIndex = ref.watch(mainNavIndexProvider);

    const screens = [
      EditorScreen(),
      FileBrowserScreen(),
      SettingsScreen(),
    ];

    return Scaffold(
      body: IndexedStack(index: currentIndex, children: screens),
      bottomNavigationBar: NavigationBar(
        selectedIndex: currentIndex,
        onDestinationSelected: (index) {
          ref.read(mainNavIndexProvider.notifier).state = index;
        },
        destinations: const [
          NavigationDestination(
            icon: Icon(Icons.code_outlined),
            selectedIcon: Icon(Icons.code),
            label: 'Editor',
          ),
          NavigationDestination(
            icon: Icon(Icons.folder_outlined),
            selectedIcon: Icon(Icons.folder),
            label: 'Files',
          ),
          NavigationDestination(
            icon: Icon(Icons.settings_outlined),
            selectedIcon: Icon(Icons.settings),
            label: 'Settings',
          ),
        ],
      ),
    );
  }
}

class AppTheme {
  static TextTheme _textTheme(Brightness brightness) {
    final color =
        brightness == Brightness.dark ? Colors.white : const Color(0xFF111827);
    final secondary = brightness == Brightness.dark
        ? Colors.white70
        : const Color(0xFF374151);

    return GoogleFonts.plusJakartaSansTextTheme(
      TextTheme(
        headlineLarge:
            TextStyle(fontSize: 28, fontWeight: FontWeight.bold, color: color),
        headlineMedium:
            TextStyle(fontSize: 24, fontWeight: FontWeight.bold, color: color),
        headlineSmall:
            TextStyle(fontSize: 20, fontWeight: FontWeight.w600, color: color),
        titleLarge:
            TextStyle(fontSize: 18, fontWeight: FontWeight.w600, color: color),
        titleMedium:
            TextStyle(fontSize: 16, fontWeight: FontWeight.w500, color: color),
        bodyLarge: TextStyle(fontSize: 16, color: secondary),
        bodyMedium: TextStyle(fontSize: 14, color: secondary),
        labelLarge:
            TextStyle(fontSize: 14, fontWeight: FontWeight.w500, color: color),
      ),
    );
  }

  static ThemeData darkTheme() {
    return ThemeData(
      useMaterial3: true,
      brightness: Brightness.dark,
      colorScheme: ColorScheme.fromSeed(
        seedColor: const Color(0xFF007ACC),
        brightness: Brightness.dark,
        surface: const Color(0xFF1E1E1E),
      ),
      scaffoldBackgroundColor: const Color(0xFF1E1E1E),
      appBarTheme: const AppBarTheme(
        backgroundColor: Color(0xFF252526),
        foregroundColor: Colors.white,
        elevation: 0,
      ),
      navigationBarTheme: NavigationBarThemeData(
        backgroundColor: const Color(0xFF252526),
        indicatorColor: const Color(0xFF007ACC).withValues(alpha: 0.3),
      ),
      cardTheme: CardThemeData(
        color: const Color(0xFF252526),
        elevation: 0,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
      ),
      inputDecorationTheme: InputDecorationTheme(
        filled: true,
        fillColor: const Color(0xFF3C3C3C),
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(8),
          borderSide: BorderSide.none,
        ),
      ),
      textTheme: _textTheme(Brightness.dark),
    );
  }

  static ThemeData lightTheme() {
    return ThemeData(
      useMaterial3: true,
      brightness: Brightness.light,
      colorScheme: ColorScheme.fromSeed(seedColor: const Color(0xFF007ACC)),
      scaffoldBackgroundColor: const Color(0xFFF7F8FA),
      appBarTheme: const AppBarTheme(
        backgroundColor: Color(0xFFFFFFFF),
        foregroundColor: Color(0xFF111827),
        elevation: 0,
      ),
      navigationBarTheme: NavigationBarThemeData(
        backgroundColor: Colors.white,
        indicatorColor: const Color(0xFF007ACC).withValues(alpha: 0.14),
      ),
      cardTheme: CardThemeData(
        color: Colors.white,
        elevation: 0,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
      ),
      inputDecorationTheme: InputDecorationTheme(
        filled: true,
        fillColor: const Color(0xFFF3F4F6),
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(8),
          borderSide: BorderSide.none,
        ),
      ),
      textTheme: _textTheme(Brightness.light),
    );
  }
}
