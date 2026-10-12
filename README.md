# Radiostic 📻

[![Android CI](https://img.shields.io/github/actions/workflow/status/radiostic/radiostic/build.yml?branch=main&style=for-the-badge&logo=github&label=Build%20%26%20CI)](https://github.com/radiostic/radiostic/actions)
[![Android SDK](https://img.shields.io/badge/Android%20SDK-34%2B-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.0-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Material Design 3](https://img.shields.io/badge/Material%20Design-M3%20Expressive-757575?style=for-the-badge&logo=materialdesign&logoColor=white)](https://m3.material.io)
[![License](https://img.shields.io/badge/License-MIT-blue?style=for-the-badge)](LICENSE)

**Radiostic** is a modern native Android application for **Offline FM Radio** and **Online Radio Streaming** built with **Jetpack Compose** and designed using the **Material 3 Expressive** design system.

It features hardware FM tuner control via **Shizuku** binder transactions on supported Samsung devices, high-fidelity HTTP stream playback via **Media3 ExoPlayer**, real-time full-screen audio spectrum visualizers, and customized dynamic look & feel controls.

---

## 🎨 Material 3 Expressive Features

- **Floating Oval Navigation Bar**:
  - Outlined floating pill container with smooth spring transitions.
  - Active destination indicators with animated icon and text labels.
  - Visible across Home, Favorites, and main Settings screens (hidden on sub-settings screens).
- **Sub-Tab Radio Navigation**:
  - Instant toggle between **Online** streams and **Offline** FM radio channels.
  - Floating Mini Player bar with swipe-to-stop control.
- **Expressive Station Editor**:
  - Multi-row horizontally scrollable Material 3 `FilterChip` icon pickers.
  - Non-bold typography and filled primary save buttons.
- **Swipe Gestures**:
  - **Home Tab**: Swipe Left-to-Right to favorite/heart; Right-to-Left to delete.
  - **Favorites Tab**: Swipe Right-to-Left to unheart; Left-to-Right to delete.
- **Look & Feel Customization**:
  - Amber dark theme palette mapped across all Material 3 color roles.
  - System Dynamic Color adaptation toggle.
  - Typography options: System Font, Serif, or import custom `.ttf` / `.otf` fonts.
  - Universal Bold Text toggle.

---

## ⚡ Core Engine Features

1. **Hardware Offline FM Radio**:
   - Direct Binder communication with Samsung's `FMPlayer` system service via **Shizuku**.
   - Frequency tuning, RDS station name decoding, and automated station scanning.
2. **Online Streaming (HTTP/HTTPS)**:
   - Powered by Jetpack **Media3 ExoPlayer**.
   - Automatic HTTP reachability status testing (unreachable online channels are styled in error red).
3. **3-Stage Audio Route Toggle**:
   - Toggle audio output between **Headphones**, **Forced Speaker**, and **Muted** directly in the Now Playing sheet.
4. **Full-Screen Audio Visualizer**:
   - Real-time FFT audio spectrum capture using Android's `Visualizer` API.
   - 3 render modes: **Spectrum Bars**, **Smooth Waveform**, and **Radial Spectrum Circle**.
   - Immersive full-screen overlay with auto-hiding system bars.

---

## 📲 Shizuku Setup Instructions

To use the hardware FM radio feature on supported devices:
1. Download and install [Shizuku](https://shizuku.rikka.app/) from Google Play or GitHub.
2. Start Shizuku via Wireless Debugging or Root.
3. Plug in wired earphones (acting as the FM antenna).
4. Tap the power button in Radiostic and grant Shizuku binder access when prompted.

---

## 🛠️ How to Build Locally

### Prerequisites
- **JDK**: Java 17 or higher
- **Android SDK**: API 34 (Android 14) installed
- **Gradle**: 8.8 (managed via Gradle Wrapper)

### Step-by-Step Local Build Tutorial

1. **Clone the repository**:
   ```bash
   git clone https://github.com/radiostic/radiostic.git
   cd radiostic
   ```

2. **Verify Environment Setup**:
   Ensure `JAVA_HOME` points to Java 17 and `ANDROID_HOME` points to your Android SDK folder:
   ```bash
   java -version
   echo $ANDROID_HOME
   ```

3. **Run Unit Tests**:
   ```bash
   ./gradlew test
   ```

4. **Build Debug APK**:
   ```bash
   ./gradlew assembleDebug
   ```
   The output APK will be located at:
   `app/build/outputs/apk/debug/app-debug.apk`

5. **Build Signed Release APK**:
   ```bash
   ./gradlew assembleRelease
   ```
   The generated release APK will be located at:
   `app/build/outputs/apk/release/app-release.apk`

---

## 🏗️ Architecture & Technology Stack

- **UI Framework**: Jetpack Compose with Material 3 Expressive APIs
- **Database & Persistence**: Room DB with migration support + SharedPreferences Repository
- **Audio & Media**: Media3 ExoPlayer + Android `Visualizer` API + Foreground Service
- **System Service Binder**: Shizuku API wrapper for Samsung `FMPlayer` service
- **Build System**: Gradle with Kotlin DSL and GitHub Actions CI workflow

---

## 📄 License

Distributed under the MIT License. See `LICENSE` for details.
