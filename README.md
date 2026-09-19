# Mindful Friction

Mindful Friction is a native Android app that adds gentle friction to compulsive
doomscrolling. Instead of blocking apps outright, it watches for the steady,
rhythmic scroll pattern of a mindless feed and escalates in two stages: a subtle
haptic nudge first, then a reminder overlay you can dismiss.

Everything runs on-device. There is no networking, no analytics, and no browsing
data leaves the phone.

## How it works

The app is a single `AccessibilityService` that listens for scroll and
window-change events system-wide.

1. **`ScrollTracker`** records scroll events and returns how long the user has
   been in a sustained doomscroll. It keeps a 10-second sliding window of event
   timestamps and calls the pattern a "zombie" scroll when the burst is long
   enough and regular enough:

   - at least 4 events in the window,
   - spanning at least 1.5 seconds,
   - at least 1 event per second,
   - average gap between consecutive events between 50 ms and 1.5 s,
   - interval variance below 200,000 (i.e. a *steady* rhythm, not paging around).

   A quiet gap longer than ~2 seconds or a switch to another app clears the run
   so the next burst starts a fresh timer.

2. **`FrictionEngine`** maps that elapsed zombie time onto a graduated response:

   | Zombie duration | Response |
   | --- | --- |
   | ≥ 3 s | Soft two-pulse haptic nudge (amplitude only where the device supports it) |
   | ≥ 6 s | A dismissible reminder overlay appears at the bottom of the screen |

   Dismissing the overlay, or switching apps, resets the meter to zero.

## Project structure

```
Mindful-Friction/
├── app/
│   ├── build.gradle.kts                 # module config (compileSdk 36, minSdk 26)
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml      # permissions, service registration
│       │   ├── java/com/mindful/friction/
│       │   │   ├── MainActivity.kt      # setup screen, status, permission buttons
│       │   │   ├── MindfulService.kt    # accessibility service, event handling, overlay
│       │   │   ├── ScrollTracker.kt     # doomscroll detection
│       │   │   └── FrictionEngine.kt    # haptic / overlay escalation
│       │   └── res/
│       │       ├── layout/              # activity_main, overlay_mindful_friction
│       │       ├── values/              # strings, themes, colors
│       │       └── xml/                 # accessibility_service_config
│       ├── test/                        # local JVM unit tests
│       └── androidTest/                 # instrumented on-device tests
├── gradle/libs.versions.toml            # dependency + plugin version catalog
├── gradlew / gradlew.bat                # Gradle wrapper (9.2.1)
├── settings.gradle.kts
└── build.gradle.kts
```

## Requirements

- **Android Studio** (or a standalone Android SDK install)
- **JDK 17+** (required by AGP 9.0)
- **Android SDK Platform 36** and build tools (matching `compileSdk = 36`)
- A device or emulator running **Android 8.0 (API 26) or newer**

No Node.js, npm, or browser is involved.

## Build and install

Clone the repository and use the Gradle wrapper:

```bash
git clone https://github.com/Tejas007bond/Mindful-Friction.git
cd Mindful-Friction

# Compile the debug APK
./gradlew :app:assembleDebug

# Build and install onto a connected device/emulator
./gradlew :app:installDebug
```

On Windows, use `gradlew.bat` in place of `./gradlew`. You can also simply open
the project folder in Android Studio and press **Run**.

## Enabling the service

The app does nothing until the accessibility service is switched on.

1. Launch **Mindful Friction** from the app drawer.
2. Tap **Enable accessibility service**. This opens the system Accessibility
   settings; select *Mindful Friction* and turn it on.
3. Return to the app: the status line should read *"All set. Mindful Friction
   is active."*
4. Optionally tap **Allow display over other apps**. The reminder is drawn as an
   accessibility overlay (`TYPE_ACCESSIBILITY_OVERLAY`), so it works from the
   accessibility service alone; this button grants the separate
   `SYSTEM_ALERT_WINDOW` permission for the edge cases where that helps.

## Usage

Open any app with a scrolling feed and scroll steadily. After roughly three
seconds of continuous scrolling you should feel the haptic nudge; after roughly
six seconds a reminder overlay appears near the bottom of the screen with a
**Resume with intent** button. Tapping it clears the timer and starts the cycle
over.

Behavior is tuned by two sets of constants if you want to experiment:

- detection sensitivity: `windowSizeMs`, `idleGapMs`, the `isZombie` thresholds
  in `ScrollTracker.kt`
- response timing: `HAPTIC_THRESHOLD_MS` and `OVERLAY_THRESHOLD_MS` in
  `FrictionEngine.kt`

## Testing and linting

```bash
# JVM unit tests
./gradlew :app:testDebugUnitTest

# Instrumented tests (requires a connected device or emulator)
./gradlew :app:connectedAndroidTest

# Android Lint
./gradlew :app:lintDebug
```

## Privacy

All detection happens locally inside the accessibility service, using only the
scroll event metadata the system delivers (scroll offsets and list indices).
Nothing is persisted and nothing is transmitted off the device. Although the
service is declared with window-content access so it can receive events, the
code never reads on-screen text or view content.

## Contributing

1. Fork the repository.
2. Create a branch: `git checkout -b feature/your-feature-name`.
3. Make your changes and run the build plus the unit tests above.
4. Commit and push to your branch.
5. Open a pull request describing the change.

## License

No license file is currently included in this repository. Until one is added,
all rights are reserved by the author.
