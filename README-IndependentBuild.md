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
| Node.js | LTS (≥ 20) | `node -v` |
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

# 3. iOS only — install CocoaPods dependencies
cd ios && pod install && cd ..
```

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
- copy the finished artifact to `dist/`
- auto-generate app icons if the source image is present and stale
- update `dist/version.txt` and `dist/CHANGELOG.md`

### Build variants

| Variant | Description |
|---------|-------------|
| `debug` | Dev JS bundle, debug signing |
| `release` | Optimised JS bundle, debug signing *(swap keystore for production)* |
| `debug-nml` | `debug` + Applitools NML instrumentation |
| `release-nml` | `release` + Applitools NML instrumentation |
| `all` | Builds all four variants |

> **NML builds** automatically download the `applitoolsify` binary on first use and cache it in `libs/`. No manual download is required.

---

### Android

```bash
# Single variant
./scripts/build-android-apks.sh debug
./scripts/build-android-apks.sh release
./scripts/build-android-apks.sh debug-nml
./scripts/build-android-apks.sh release-nml

# All variants at once
./scripts/build-android-apks.sh all
```

**Output** → `dist/`

```
dist/
├── App Automation Playground-debug.apk
├── App Automation Playground-release.apk
├── App Automation Playground-debug-nml.apk   # requires NML
├── App Automation Playground-release-nml.apk # requires NML
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

# All variants at once
./scripts/build-ios-app.sh all
```

**Output** → `dist/`

```
dist/
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

# One platform, one variant
./scripts/build-all.sh android debug
./scripts/build-all.sh ios release
```

**Syntax**: `./scripts/build-all.sh [platform] [variant]`  
Defaults: `platform=all`, `variant=all`

---

## Build outputs

After any build, `dist/` contains the artifacts plus metadata:

```
dist/version.txt    – version, build date, platform
dist/CHANGELOG.md   – human-readable release notes (edit freely)
```

---

## Running Appium visual tests

The Appium test project lives in `appium-tests/` and uses **Gradle + TestNG + Applitools Eyes**.

### Prerequisites

- Appium 2.x installed globally: `npm install -g appium`
- UiAutomator2 driver: `appium driver install uiautomator2`
- XCUITest driver: `appium driver install xcuitest`
- `APPLITOOLS_API_KEY` environment variable set (for visual checks)
- The target app built and present in `dist/` (see [Building the app](#building-the-app))

### Run commands

```bash
cd appium-tests

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

### Using the NML-instrumented app

```bash
# Android NML
IS_NML=true APPLITOOLS_API_KEY=<your-key> ./gradlew runAndroid

# iOS NML
IS_NML=true APPLITOOLS_API_KEY=<your-key> ./gradlew runIos
```

When `IS_NML=true` the test automatically loads `dist/App Automation Playground-debug-nml.apk` (or `.app.zip`) instead of the standard build.

### Disabling visual checks

```bash
# Run automation only, no Applitools checkpoints
IS_EYES_ENABLED=false ./gradlew runAndroid
```

### Key environment variables

| Variable | Default | Description |
|----------|---------|-------------|
| `APPLITOOLS_API_KEY` | *(required for Eyes)* | Applitools API key |
| `IS_NML` | `false` | Load NML-instrumented app from `dist/` |
| `IS_EYES_ENABLED` | `true` | Enable/disable Applitools visual checkpoints |
| `USE_ALTERNATE_FLOW` | `false` | Run alternate user journey |
| `IOS_DEVICE_NAME` | `iPhone 16` | iOS simulator name |
| `IOS_PLATFORM_VERSION` | `18.4` | iOS simulator OS version |
| `LOG_DIR` | *(none)* | Directory to write `appium_logs.txt` |

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
pod deintegrate
pod install
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

### Appium tests: app not found in dist/
Build the app first:
```bash
./scripts/build-android-apks.sh debug   # or debug-nml for NML tests
./scripts/build-ios-app.sh debug
```

### iOS simulator app: unzip manually
```bash
cd /tmp
unzip ~/path/to/dist/"App Automation Playground-debug.app.zip"
# → App Automation Playground-debug.app/
```
