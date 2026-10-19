# CLAUDE.md

This is a native Android project, not Flutter. Ignore all Flutter/Dart
guidance from the global `~/.claude/CLAUDE.md` when working in this repo;
the rules below take precedence.

## Tech Stack

- **Language:** Kotlin
- **UI Framework:** Jetpack Compose with Material 3
- **Architecture:** MVVM
- **Dependency Injection:** Koin
- **Async:** Kotlin Coroutines & Flow
- **Network:** Retrofit

## Coding Rules

1. Prefer state hoisting in Compose. Use `StateFlow` in ViewModels.
2. Use UI state classes (e.g., `LoginUiState`) instead of multiple
   independent states.

## Coding Style

Code should be simple, flexible, easy to modify, easy to maintain, and easy to
test, following SOLID, OOAD, and OOP principles.
