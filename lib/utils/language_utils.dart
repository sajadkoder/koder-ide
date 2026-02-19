import 'dart:io';

import 'package:highlight/highlight_core.dart';
import 'package:highlight/languages/bash.dart' as lang_bash;
import 'package:highlight/languages/cpp.dart' as lang_cpp;
import 'package:highlight/languages/css.dart' as lang_css;
import 'package:highlight/languages/dart.dart' as lang_dart;
import 'package:highlight/languages/go.dart' as lang_go;
import 'package:highlight/languages/htmlbars.dart' as lang_html;
import 'package:highlight/languages/java.dart' as lang_java;
import 'package:highlight/languages/javascript.dart' as lang_js;
import 'package:highlight/languages/json.dart' as lang_json;
import 'package:highlight/languages/kotlin.dart' as lang_kotlin;
import 'package:highlight/languages/lua.dart' as lang_lua;
import 'package:highlight/languages/markdown.dart' as lang_markdown;
import 'package:highlight/languages/perl.dart' as lang_perl;
import 'package:highlight/languages/powershell.dart' as lang_powershell;
import 'package:highlight/languages/python.dart' as lang_python;
import 'package:highlight/languages/ruby.dart' as lang_ruby;
import 'package:highlight/languages/rust.dart' as lang_rust;
import 'package:highlight/languages/scala.dart' as lang_scala;
import 'package:highlight/languages/swift.dart' as lang_swift;
import 'package:highlight/languages/typescript.dart' as lang_ts;
import 'package:highlight/languages/ini.dart' as lang_ini;
import 'package:highlight/languages/xml.dart' as lang_xml;
import 'package:highlight/languages/yaml.dart' as lang_yaml;

String detectLanguageFromPath(String path) {
  final fileName = path.split(Platform.pathSeparator).last.toLowerCase();
  if (!fileName.contains('.')) {
    return 'plaintext';
  }

  final ext = fileName.split('.').last;
  switch (ext) {
    case 'js':
    case 'jsx':
    case 'mjs':
    case 'cjs':
      return 'javascript';
    case 'ts':
    case 'tsx':
      return 'typescript';
    case 'py':
    case 'pyw':
      return 'python';
    case 'rs':
      return 'rust';
    case 'c':
      return 'c';
    case 'cc':
    case 'cpp':
    case 'cxx':
    case 'h':
    case 'hpp':
      return 'cpp';
    case 'go':
      return 'go';
    case 'java':
      return 'java';
    case 'kt':
    case 'kts':
      return 'kotlin';
    case 'swift':
      return 'swift';
    case 'dart':
      return 'dart';
    case 'rb':
      return 'ruby';
    case 'lua':
      return 'lua';
    case 'pl':
      return 'perl';
    case 'scala':
      return 'scala';
    case 'html':
    case 'htm':
      return 'html';
    case 'css':
    case 'scss':
    case 'sass':
      return 'css';
    case 'json':
      return 'json';
    case 'yaml':
    case 'yml':
      return 'yaml';
    case 'toml':
      return 'toml';
    case 'xml':
      return 'xml';
    case 'md':
    case 'markdown':
      return 'markdown';
    case 'sh':
    case 'bash':
    case 'zsh':
      return 'bash';
    case 'ps1':
      return 'powershell';
    default:
      return 'plaintext';
  }
}

String languageLabel(String language) {
  switch (language) {
    case 'javascript':
      return 'JavaScript';
    case 'typescript':
      return 'TypeScript';
    case 'python':
      return 'Python';
    case 'rust':
      return 'Rust';
    case 'cpp':
      return 'C++';
    case 'powershell':
      return 'PowerShell';
    default:
      if (language.isEmpty) return 'Plain Text';
      return language[0].toUpperCase() + language.substring(1);
  }
}

Mode? modeForLanguage(String language) {
  switch (language) {
    case 'javascript':
      return lang_js.javascript;
    case 'typescript':
      return lang_ts.typescript;
    case 'python':
      return lang_python.python;
    case 'rust':
      return lang_rust.rust;
    case 'c':
      return lang_cpp.cpp;
    case 'cpp':
      return lang_cpp.cpp;
    case 'go':
      return lang_go.go;
    case 'java':
      return lang_java.java;
    case 'kotlin':
      return lang_kotlin.kotlin;
    case 'swift':
      return lang_swift.swift;
    case 'dart':
      return lang_dart.dart;
    case 'ruby':
      return lang_ruby.ruby;
    case 'lua':
      return lang_lua.lua;
    case 'perl':
      return lang_perl.perl;
    case 'scala':
      return lang_scala.scala;
    case 'html':
      return lang_html.htmlbars;
    case 'css':
      return lang_css.css;
    case 'json':
      return lang_json.json;
    case 'yaml':
      return lang_yaml.yaml;
    case 'toml':
      return lang_ini.ini;
    case 'xml':
      return lang_xml.xml;
    case 'markdown':
      return lang_markdown.markdown;
    case 'bash':
      return lang_bash.bash;
    case 'powershell':
      return lang_powershell.powershell;
    default:
      return null;
  }
}

String commentPrefixForLanguage(String language) {
  switch (language) {
    case 'python':
    case 'bash':
    case 'ruby':
    case 'yaml':
    case 'toml':
    case 'perl':
    case 'powershell':
      return '#';
    case 'html':
    case 'xml':
      return '<!--';
    default:
      return '//';
  }
}
