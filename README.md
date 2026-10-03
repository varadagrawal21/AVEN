# LifeTracker

LifeTracker is an offline-first Android personal dashboard for health, learning, finance, relationships, and custom tracking. It uses Kotlin and Jetpack Compose with Room persistence and reactive Flow-based UI state.

## Features

- Dashboard with daily summaries
- Hydration goals, water logging, progress tracking, and reminder scheduling
- Health weight/BMI and nutrition tracking with trend charts
- Finance income/expense logging, monthly summaries, and category charts
- Offline vocabulary entries with search and detail views
- Merriam-Webster vocabulary lookup with definitions, examples, and pronunciation audio
- Academic subjects and study-session tracking
- Books, page progress, notes, detail views, and PDF viewing
- Quotes with reflections and applications
- People records with relationship and behavior notes
- Custom sections and entries
- Settings for body measurements and notification preferences
- Dark/light Material 3 theme and notification channels
- Room database with 12 entities and Hilt dependency injection

## Tech Stack

- Kotlin 2.0.21 and Java 17 bytecode target
- Android Gradle Plugin 8.5.2 and Gradle 8.7
- Jetpack Compose BOM 2024.09.03 and Material 3
- Minimum SDK 26 (Android 8.0), target/compile SDK 35
- Navigation Compose 2.8.2
- Room 2.6.1 with KSP
- Hilt 2.52 with WorkManager integration
- WorkManager 2.9.1
- DataStore Preferences 1.1.1
- Kotlin Coroutines 1.9.0
- MPAndroidChart 3.1.0
- AndroidPdfViewer 2.8.2
- Coil 2.7.0
- JUnit 4, MockK, Room in-memory tests, and Compose UI tests

## Requirements

- Latest stable Android Studio
- Android SDK Platform 35 and SDK Build Tools installed
- Android SDK Platform 26 or newer for an emulator/device
- JDK 17 or newer; the project compiles with Java/Kotlin target 17
- A device or emulator running Android 8.0/API 26 or newer

The Android SDK path should be configured by Android Studio or `local.properties`. Android Studio's embedded JDK is suitable when it is version 17 or newer.

## Open and Run

1. Open `D:\LifeTracker` in Android Studio.
2. Allow Gradle to sync and install the requested SDK components.
3. Select the `app` run configuration.
4. Start an API 26+ emulator or connect an Android device with USB debugging enabled.
5. Run the app from Android Studio.

The app stores module data locally in Room. Notification permissions and exact-alarm behavior may require granting permissions on the test device, depending on its Android version.

## Dictionary API Setup

Vocabulary lookup uses the Merriam-Webster Collegiate Dictionary API. Create an API key at [dictionaryapi.com](https://www.dictionaryapi.com/) and add it to a local Gradle properties file as follows:

```properties
mwDictionaryApiKey=YOUR_API_KEY
```

Put the property in the user Gradle properties file (`%USERPROFILE%\\.gradle\\gradle.properties`) or in the ignored project `local.properties` file. Do not commit an API key. Without a key, the existing manual vocabulary entry flow still works.

## Build a Debug APK

From Android Studio, use **Build > Build APK(s)**. The generated APK is written to:

```text
app/build/outputs/apk/debug/app-debug.apk
```

From a terminal with Gradle available, the equivalent command is:

```powershell
.\gradlew.bat assembleDebug
```

The repository currently contains the Gradle wrapper configuration but requires the standard `gradlew.bat` launcher and wrapper JAR to be restored/generated before the command-line form can run. Android Studio can use its configured Gradle installation after project sync.

## Tests

Run unit tests from Android Studio with the test gutter actions, or from a configured Gradle environment:

```powershell
.\gradlew.bat test
.\gradlew.bat connectedDebugAndroidTest
```

The instrumentation suite covers critical hydration, finance, and vocabulary flows. A connected API 26+ emulator or physical device is required for `connectedDebugAndroidTest`.

## Project Layout

- `app/src/main/java/com/lifetracker/data`: Room entities, DAOs, database, and repositories
- `app/src/main/java/com/lifetracker/di`: Hilt database and dependency modules
- `app/src/main/java/com/lifetracker/ui`: Compose screens, navigation, components, and theme
- `app/src/main/java/com/lifetracker/viewmodel`: ViewModels for module state and actions
- `app/src/main/java/com/lifetracker/notifications`: notification channels, boot receiver, and workers
- `app/src/test`: repository and unit tests
- `app/src/androidTest`: Compose instrumentation tests
- `gradle/libs.versions.toml`: pinned plugin and library versions
