# PathBuilder 🧩

[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-7F52FF.svg?style=flat&logo=kotlin)](https://kotlinlang.org)
[![Android](https://img.shields.io/badge/Android-API%2024%2B-3DDC84.svg?style=flat&logo=android)](https://developer.android.com)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-Material3-4285F4.svg?style=flat&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)

**PathBuilder** is a modern, tactile path-building puzzle game built with **Kotlin** and **Jetpack Compose** for Android. Your objective is to place, rotate, and route tiles on a grid to forge a continuous path from **START** to **FINISH** while navigating obstacles and managing a limited tile inventory.

---

## 🌟 Key Features

* **🧩 Handcrafted Puzzle Mechanics**:
  * **6x6 Grid Board** with predefined blocked obstacle cells.
  * **Dynamic Tile Tray & Bag**: Manage 3 active tiles in your tray pulled from a limited bag.
  * **Tile Rotation**: Turn tiles 90° clockwise to align openings with adjacent tiles.
  * **Live Route Highlighting**: Connected tiles dynamically light up as soon as they link back to **START**.
  * **Undo Support**: Revert placed tiles anytime before the game ends.

* **🎨 Custom Tactile Design & Aesthetics**:
  * **Custom Canvas Rendering**: Board, tiles, grooves, and connecting paths are rendered using Jetpack Compose `Canvas`.
  * **Multiple Board Themes**: Toggle between **Classic** (warm wood & metal aesthetics) and **Midnight** (dark sleek slate) board styles.
  * **App Themes**: Full support for Light, Dark, and System Theme modes with edge-to-edge support.

* **🎵 Audio & Haptic Feedback**:
  * Crisp sound effects and tactile haptic feedback on tile selection, rotation, placement, undo, victory, and failure.

* **📊 Game Stats & Persistence**:
  * Tracks total games played, routes completed, personal best moves, and fastest completion time using **Jetpack DataStore**.

---

## 🛠️ Architecture & Tech Stack

PathBuilder follows modern Android development best practices and Clean Architecture principles:

* **Language**: [Kotlin](https://kotlinlang.org/)
* **UI**: [Jetpack Compose](https://developer.android.com/jetpack/compose) & Material Design 3
* **Navigation**: [Jetpack Navigation Compose](https://developer.android.com/jetpack/compose/navigation)
* **Architecture Pattern**: MVVM (Model-View-ViewModel) + Unidirectional Data Flow (UDF)
* **State Management**: `StateFlow` and `collectAsStateWithLifecycle`
* **Storage**: [Jetpack DataStore Preferences](https://developer.android.com/topic/libraries/architecture/datastore) for settings and statistics
* **Build System**: Gradle Version Catalog (`libs.versions.toml`) with Android Gradle Plugin & Kotlin 2.2

### Project Structure

```
com.example.pathbuilder/
├── data/           # DataStore repositories (Settings & Stats)
├── feedback/       # Sound player & haptic feedback management
├── game/           # Pure game rules, board logic, BFS pathfinding & puzzle layouts
├── navigation/     # NavHost setup & route transitions
├── ui/
│   ├── components/ # Custom composable components, tile art, and canvas drawing
│   ├── screens/    # Game, Home, Result, HowToPlay, Settings, Splash, About
│   └── theme/      # Material3 theme, color schemes, typography, board palettes
```

---

## 🎮 How to Play

1. **Select a Tile**: Choose a tile (Straight or Corner) from your tile tray at the bottom.
2. **Rotate**: Tap **Rotate** to turn the selected tile 90° clockwise.
3. **Place**: Tap an empty grid cell on the board to place your tile.
4. **Connect**: Align neighboring tile openings so they match.
5. **Complete**: Build an uninterrupted route connecting **START** (left) to **FINISH** (right) before you run out of tiles!

---

## 🚀 Getting Started

### Prerequisites
* **Android Studio**: Ladybug / Meerkat (or newer)
* **JDK**: Version 17 or higher
* **Minimum SDK**: API Level 24 (Android 7.0)
* **Target SDK**: API Level 35 (Android 15)

### Build & Run

1. Clone the repository:
   ```bash
   git clone https://github.com/abdullah-ateeq/Path-Builder.git
   cd Path-Builder
   ```

2. Open the project in **Android Studio**.

3. Build and run the app on an Android device or emulator using Gradle:
   ```bash
   ./gradlew installDebug
   ```

### Running Unit Tests

Run the core game rules and pathfinder unit tests with:
```bash
./gradlew test
```

---

## 📄 License

```text
Copyright (C) 2025 Abdullah Ateeq

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
