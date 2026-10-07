# Rusty Ranger - Android File Manager

Rusty Ranger is a fast, highly-optimized Android file manager built with Kotlin and Jetpack Compose. It features a modern, completely synchronous layout mechanism, robust IO file handling (using Java NIO streams), multiple themes, configurable views (Grid and List), and dynamic scaling without sacrificing visual fidelity.

## Features
- **Ultra-fast Startup**: Skips standard blocking splash screen delays and immediately draws the app UI. Uses Java NIO for lightning-fast file lookup.
- **Dynamic File Explorer**: Full support for copying, pasting, moving, deleting, creating, and renaming files. 
- **Clipboard Management**: Intelligent clipboard system that guards against recursive folder nesting and overwrite errors.
- **Highly Configurable**: Grid View vs List View, custom fonts, customizable padding, responsive layouts, and customizable background wallpapers.

## Getting Started

### Prerequisites
- **Android Studio** (Flamingo or newer recommended)
- **Java 17** (for Gradle 8+)
- **Android SDK** (API 35 target)

### Build Instructions
To build the project yourself:
1. Clone this repository:
   ```bash
   git clone <your-repository-url>
   cd RustyRanger
   ```
2. Open the directory in **Android Studio**.
3. Let Gradle sync download the dependencies.
4. To build a debug APK, run:
   ```bash
   ./gradlew assembleDebug
   ```
5. To build a highly optimized release APK (with AOT compilation and R8 minification):
   ```bash
   ./gradlew assembleRelease
   ```
*(Note: Building the `release` APK is highly recommended for performance testing, as Jetpack Compose debug builds run significantly slower).*

## Downloads
Pre-compiled release APKs are available in the `releases/` directory. Simply download `RustyRanger-release.apk` to your Android device and install it.

## License
Open Source.
