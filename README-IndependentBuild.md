# App Automation Playground — Build & Test Guide

> **Note**: Complete the [Specmatic stub server setup](./README-Specmatic-Stub-Setup.md) before running the app on a device or simulator — the WebView loads content from `http://localhost:8080`.

This guide covers:
- [Prerequisites](#prerequisites)
- [First-time setup](#first-time-setup)
- [App icon](#app-icon)
- [Building the app](#building-the-app)
- [Build outputs](#build-outputs)
- [Running Appium visual tests](#running-appium-visual-tests)
- [Troubleshooting](#troubleshooting)

---

## Prerequisites

### Common (all platforms)

| Tool | Version | Check |
|------|---------|-------|
| Git | any | `git --version` |
| Node.js | LTS (≥ 22.11) | `node -v` |
| npm | bundled with Node | `npm -v` |
| Java JDK | 17+ | `java -version` |

---

### Android

1. **Android Studio** with the Android SDK installed  
2. **Environment variables** (add to `~/.zshrc` or `~/.bashrc`):
   ```bash
   export ANDROID_HOME="$HOME/Library/Android/sdk"
   export PATH="$PATH:$ANDROID_HOME/emulator:$ANDROID_HOME/platform-tools"
   ```
3. Verify ADB is reachable:
   ```bash
   adb version
   ```
4. A running Android Emulator **or** a real device with USB debugging enabled.

---

### iOS *(macOS only)*

1. **Xcode** (latest stable) from the Mac App Store  
2. Xcode command-line tools:
   ```bash
   xcode-select --install
   sudo xcodebuild -license accept
   ```
3. **CocoaPods**:
   ```bash
   sudo gem install cocoapods
   pod --version
   ```
   > If `gem install` fails due to system Ruby restrictions, use Homebrew Ruby:
   > ```bash
   > brew install ruby
   > echo 'export PATH="/opt/homebrew/opt/ruby/bin:$PATH"' >> ~/.zshrc
   > source ~/.zshrc
   > gem install cocoapods
   > ```

---

## First-time setup

```bash
# 1. Clone
git clone https://github.com/anandbagmar/MockedE2EDemo.git
cd MockedE2EDemo

# 2. Install Node dependencies
npm install
```

For iOS, the build script now bootstraps Ruby gems and CocoaPods automatically the first time you build. If you want to pre-install them manually, you still can, but it is no longer required before running `./scripts/build-ios-app.sh`.
The iOS build script also forces React Native Core to build from source during CocoaPods setup, which avoids the prebuilt VFS overlay mismatch that can appear with newer React Native releases.

---

## App icon

Place a **square PNG (≥ 1024 × 1024 px)** at:

```
assets/images/app-icon-source.png
```

Icons are **generated automatically** the first time you run any build script (and regenerated whenever the source image changes). You never need to run `generate-icons.sh` manually.

---

## Building the app

All build scripts live in `scripts/`. They:
- embed the JS bundle in the app (no Metro server required at runtime)
- copy the finished artifact to a timestamped folder under `builds/`
- auto-generate app icons if the source image is present and stale
- update `builds/version.txt` and `builds/CHANGELOG.md`
- for iOS, bootstrap Bundler/CocoaPods automatically when needed

### Build variants

| Variant | Description |
|---------|-------------|
| `debug` | Dev JS bundle, debug signing |
| `release` | Optimised JS bundle, debug signing *(swap keystore for production)* |
| `debug-nml` | `debug` + Applitools NML instrumentation |
| `release-nml` | `release` + Applitools NML instrumentation |
| `all` | Builds all four variants |

> **NML builds** automatically download the `applitoolsify` binary on first use and cache it in `libs/`. No manual download is required.
> `debug-nml` depends on the plain `debug` build, and `release-nml` depends on the plain `release` build. The scripts handle those prerequisites automatically and avoid rebuilding them twice in the same run.

---

### Android

```bash
# Single variant
./scripts/build-android-apks.sh debug
./scripts/build-android-apks.sh release
./scripts/build-android-apks.sh debug-nml
./scripts/build-android-apks.sh release-nml

# Multiple variants (comma-separated, no spaces)
./scripts/build-android-apks.sh debug,release
./scripts/build-android-apks.sh debug,debug-nml
./scripts/build-android-apks.sh release,release-nml

# All variants at once
./scripts/build-android-apks.sh all
```

**Output** → `builds/<Mon-YYYY>/<DD-MMM-YYYY>/<HH-MM>/android/`

```
builds/
└── Apr-2026/14-Apr-2026/16-12/android/
    ├── App Automation Playground-debug.apk
    ├── App Automation Playground-release.apk
    ├── App Automation Playground-debug-nml.apk   # requires NML
    ├── App Automation Playground-release-nml.apk # requires NML
    ├── android-instrumentation.log
    ├── version.txt
    └── CHANGELOG.md
```

---

### iOS

```bash
# Single variant
./scripts/build-ios-app.sh debug
./scripts/build-ios-app.sh release
./scripts/build-ios-app.sh debug-nml
./scripts/build-ios-app.sh release-nml

# Multiple variants (comma-separated, no spaces)
./scripts/build-ios-app.sh debug,release
./scripts/build-ios-app.sh debug,debug-nml
./scripts/build-ios-app.sh release,release-nml

# All variants at once
./scripts/build-ios-app.sh all
```

**Output** → `builds/<Mon-YYYY>/<DD-MMM-YYYY>/<HH-MM>/ios/`

```
builds/
└── Apr-2026/14-Apr-2026/16-12/ios/
    ├── App Automation Playground-debug.app.zip
    ├── App Automation Playground-release.app.zip
    ├── App Automation Playground-debug-nml.app.zip   # requires NML
    ├── App Automation Playground-release-nml.app.zip # requires NML
    ├── version.txt
    └── CHANGELOG.md
```

> iOS apps are zipped because `.app` is a directory. Appium's `app` capability accepts the zip directly.

---

### Both platforms at once

```bash
# All platforms, all variants
./scripts/build-all.sh

# All platforms, one variant
./scripts/build-all.sh all debug
./scripts/build-all.sh all release-nml

# All platforms, multiple variants (comma-separated)
./scripts/build-all.sh all debug,debug-nml
./scripts/build-all.sh all release,release-nml

# One platform, one or more variants
./scripts/build-all.sh android debug
./scripts/build-all.sh ios release,release-nml
./scripts/build-all.sh android debug,release
```

**Syntax**: `./scripts/build-all.sh [platform] [variant[,variant…]]`  
Defaults: `platform=all`, `variant=all`

---

## Build outputs

After any build, artifacts are written to a timestamped platform folder:

```
builds/<Mon-YYYY>/<DD-MMM-YYYY>/<HH-MM>/<platform>/
```

Inside each platform folder:

```
version.txt    – version, build date, platform
CHANGELOG.md   – human-readable release notes (edit freely)
```

Convenience symlinks are also updated on each run:

```
builds/latest
builds/latest-android
builds/latest-ios
```

---

## Running Appium visual tests

The Appium test project lives in `e2eTests/` and uses **Gradle + TestNG + Applitools Eyes**.
The app journey now includes native, web, and hybrid screens, plus a native interlude screen in the middle of the flow. Both the original and alternate variants follow the same structure; only the copy/layout differs where the app already branches.

### Prerequisites

- Appium 2.x installed globally: `npm install -g appium`
- UiAutomator2 driver: `appium driver install uiautomator2`
- XCUITest driver: `appium driver install xcuitest`
- `APPLITOOLS_API_KEY` environment variable set (for visual checks)
- The target app built and present in the relevant timestamped folder under `builds/` (see [Building the app](#building-the-app))

### Run commands

```bash
cd e2eTests

# Android — original flow
./gradlew runAndroid

# Android — alternate flow
./gradlew runAndroid -DUSE_ALTERNATE_FLOW=true

# iOS — original flow
./gradlew runIos

# iOS — alternate flow
./gradlew runIos -DUSE_ALTERNATE_FLOW=true

# Both platforms
./gradlew test
```

The screen-by-screen journey in the tests is intentionally split into helper methods so it is easier to follow and maintain:
- home
- planner
- native interlude
- guest lookup
- web checklist
- summary

### Using the NML-instrumented app

```bash
# Android NML
IS_NML=true APPLITOOLS_API_KEY=<your-key> ./gradlew runAndroid

# iOS NML
IS_NML=true APPLITOOLS_API_KEY=<your-key> ./gradlew runIos
```

When `IS_NML=true` the test should load the `-nml` app from the relevant timestamped folder under `builds/`.

Appium server logs are written to:
- `reports/appium/appium-server-android.log`
- `reports/appium/appium-server-ios.log`

The console prints the exact log path at startup.

### Disabling visual checks

```bash
# Run automation only, no Applitools checkpoints
IS_EYES_ENABLED=false ./gradlew runAndroid
```

### Key environment variables

| Variable | Default | Description |
|----------|---------|-------------|
| `APPLITOOLS_API_KEY` | *(required for Eyes)* | Applitools API key |
| `IS_NML` | `false` | Load NML-instrumented app from the selected folder under `builds/` |
| `IS_EYES_ENABLED` | `true` | Enable/disable Applitools visual checkpoints |
| `USE_ALTERNATE_FLOW` | `false` | Run alternate user journey |
| `IOS_DEVICE_NAME` | `iPhone 16` | iOS simulator name |
| `IOS_PLATFORM_VERSION` | `18.4` | iOS simulator OS version |
| `TEST_PLATFORM` | set by Gradle task | Labels the batch and Appium log file as Android, iOS, or Android+iOS |

All variables can also be passed as `-D` system properties:

```bash
./gradlew runAndroid -DUSE_ALTERNATE_FLOW=true -DIS_EYES_ENABLED=false
```

---

## Troubleshooting

### Icons not updating
Delete the marker file and rebuild:
```bash
rm .icon-generated
./scripts/build-android-apks.sh debug
```

### Android: device not found
```bash
adb devices          # check connected devices/emulators
adb kill-server
adb start-server
```

### iOS: CocoaPods errors
```bash
cd ios
bundle exec pod install
cd ..
```

### NML binary download fails
The `applitoolsify` binary is downloaded from Applitools' Azure CDN. If behind a proxy, set `HTTPS_PROXY` before running the build script:
```bash
HTTPS_PROXY=http://your-proxy:port ./scripts/build-android-apks.sh debug-nml
```
The cached binary lives in `libs/`. Delete it to force a re-download:
```bash
rm libs/applitoolsify-*
```

### Appium tests: app not found in builds/
Build the app first:
```bash
./scripts/build-android-apks.sh debug   # or debug-nml for NML tests
./scripts/build-ios-app.sh debug
```

### Appium logs
If a test run is noisy or fails during session startup, check the Appium server log in `reports/appium/` first. The test console prints the exact log file name when the server starts.

### iOS simulator app: unzip manually
```bash
cd /tmp
unzip ~/path/to/builds/latest-ios/"App Automation Playground-debug.app.zip"
# → App Automation Playground-debug.app/
```
