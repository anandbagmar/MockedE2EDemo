# App Automation Playground

A React Native app demonstrating end-to-end mobile testing with [Specmatic](https://specmatic.io) API mocking and [Applitools](https://applitools.com) visual testing.
The sample journey mixes native, web, and hybrid screens so Appium can exercise all three patterns in one flow.

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
├── e2eTests/               Appium + Selenium + Applitools test project (Gradle/Java)
│   ├── build.gradle
│   ├── gradlew
│   └── src/test/java/com/eot/e2edemo/tests/
│       ├── BaseTest.java
│       ├── android/CommunityMeetingPlannerAndroidTest.java
│       ├── ios/CommunityMeetingPlannerIOSTest.java
│       └── web/CommunityMeetingPlannerWebTest.java
│
├── webapp/                     Responsive web mirror of the workflow (Vite + React + TS)
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
cd e2eTests && ./gradlew runAndroid
```

### Run the web tests

```bash
# Start the webapp (in one terminal)
cd webapp && npm install && npm run dev      # http://localhost:5173

# Run the Selenium web tests against it (in another terminal)
cd e2eTests && ./gradlew runWeb
# ...or against a hosted build:
# ./gradlew runWeb -DWEB_BASE_URL=https://anandbagmar.github.io/MockedE2EDemo/
```

See the **[Build & Test Guide](./README-IndependentBuild.md)** for all variants, NML builds, iOS setup, the native/web/hybrid screen map, and test configuration. The iOS build script will install Bundler/CocoaPods dependencies automatically when needed.

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
npm run ios
```

If you are using the standalone iOS build scripts in `scripts/`, they will install Bundler/CocoaPods dependencies automatically when needed. For direct `npm run ios` usage, CocoaPods still needs to be available on the machine.

> After any JS/TS change, re-run the build script to update the embedded bundle in the APK/app used for Appium testing.
