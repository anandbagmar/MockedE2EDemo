# App Automation Playground

A React Native app demonstrating end-to-end mobile testing with [Specmatic](https://specmatic.io) API mocking and [Applitools](https://applitools.com) visual testing.

---

## Quick links

| I want to… | Go to |
|------------|-------|
| Build the APK / .app and run Appium tests | **[Build & Test Guide](./README-IndependentBuild.md)** |
| Set up the Specmatic mock server | [Specmatic Stub Setup](./README-Specmatic-Stub-Setup.md) |

---

## Repository structure

```
├── android/                    React Native Android project
├── ios/                        React Native iOS project
├── App.tsx                     Main app component
│
├── assets/
│   └── images/
│       └── app-icon-source.png ← place your icon here (≥ 1024×1024 px)
│
├── scripts/
│   ├── build-android-apks.sh   Build Android APKs (debug/release/debug-nml/release-nml/all)
│   ├── build-ios-app.sh        Build iOS .app bundles
│   ├── build-all.sh            Build both platforms in one command
│   ├── generate-icons.sh       Resize icon source into all Android/iOS sizes
│   └── lib/
│       ├── icons-common.sh     Auto icon-generation logic (sourced by build scripts)
│       └── nml-common.sh       Applitools NML download & instrumentation logic
│
├── builds/                     Timestamped build outputs land here
│   └── Apr-2026/14-Apr-2026/16-12/
│       ├── android/
│       └── ios/
│
├── appium-tests/               Appium + Applitools visual test project (Gradle/Java)
│   ├── build.gradle
│   ├── gradlew
│   └── src/test/java/io/specmatic/tests/
│       ├── BaseTest.java
│       ├── android/CommunityMeetingPlannerAndroidTest.java
│       └── ios/CommunityMeetingPlannerIOSTest.java
│
└── libs/                       Auto-downloaded tool binaries (git-ignored)
    └── applitoolsify-*         Applitools NML instrumentation binary
```

---

## Build in 3 commands

```bash
# 1. Install dependencies
npm install

# 2. Build (replace 'debug' with any variant — see Build & Test Guide)
./scripts/build-android-apks.sh debug
./scripts/build-ios-app.sh debug

# 3. Run Appium visual tests
cd appium-tests && ./gradlew runAndroid
```

See the **[Build & Test Guide](./README-IndependentBuild.md)** for all variants, NML builds, iOS setup, and test configuration.

---

## Development workflow (with Metro)

Use this when actively developing the app UI.

### Start Metro

```bash
npm start
```

### Run on Android

```bash
npm run android
```

### Run on iOS

```bash
# First time only
bundle install
cd ios
bundle exec pod install
cd ..

npm run ios
```

> After any JS/TS change, re-run the build script to update the embedded bundle in the APK/app used for Appium testing.
