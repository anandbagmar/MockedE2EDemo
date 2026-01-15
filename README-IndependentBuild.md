# MockedE2EDemo - Build independent apk/app without Metro 

## Build & Run (Fresh Clone)

This repo contains a React Native app that can be built **independently/offline** (without starting Metro manually) using the provided scripts for **Android** and **iOS**.

---

## Prerequisites

### Common
- **Git**
- **Node.js** (LTS recommended) + **npm**
  - Verify:
    ```bash
    node -v
    npm -v
    ```
- `npx` (bundled with npm)

---

### Android prerequisites
- **Java JDK 17+**
  - Verify:
    ```bash
    java -version
    ```
- **Android Studio**
  - Android SDK installed
  - Android Platform Tools installed
- Environment setup (macOS):
  ```bash
  export ANDROID_HOME="$HOME/Library/Android/sdk"
  export PATH="$PATH:$ANDROID_HOME/emulator:$ANDROID_HOME/platform-tools:$ANDROID_HOME/tools:$ANDROID_HOME/tools/bin"
  ```
- Verify ADB:
  ```bash
  adb version
  ```
- One of:
  - Android Emulator, OR
  - Real Android device (USB debugging enabled)

---

### iOS prerequisites (macOS only)
- **Xcode**
  - Install command line tools:
    ```bash
    xcode-select --install
    ```
  - Accept license:
    ```bash
    sudo xcodebuild -license accept
    ```
- **CocoaPods**
  - Install:
    ```bash
    sudo gem install cocoapods
    pod --version
    ```
  - If CocoaPods fails due to old Ruby, use Homebrew Ruby:
    ```bash
    brew install ruby
    echo 'export PATH="/opt/homebrew/opt/ruby/bin:$PATH"' >> ~/.zshrc
    source ~/.zshrc
    gem install cocoapods
    ```

---

## Setup

### 1) Clone repo
```bash
git clone https://github.com/anandbagmar/MockedE2EDemo.git
cd MockedE2EDemo
```

### 2) Install dependencies
```bash
npm install
```

---

## Build Android app (offline / independent)

These APK builds embed the JS bundle and run **WITHOUT Metro**.

### Debug APK
```bash
chmod +x scripts/build-android-apks.sh
./scripts/build-android-apks.sh debug
```

### Release APK
```bash
./scripts/build-android-apks.sh release
```

### Build both Debug + Release
```bash
./scripts/build-android-apks.sh all
```

(Optional) install after build:
```bash
./scripts/build-android-apks.sh debug+install
./scripts/build-android-apks.sh release+install
```

---

## Build iOS app (offline / independent)

These builds embed the JS bundle and run **WITHOUT Metro**.

### 3) Install CocoaPods (first time only)
```bash
cd ios
pod install
cd ..
```

### Debug build
```bash
chmod +x scripts/build-ios-app.sh
./scripts/build-ios-app.sh debug
```

### Release build
```bash
./scripts/build-ios-app.sh release
```

### Build both Debug + Release
```bash
./scripts/build-ios-app.sh all
```

---

## Notes / Troubleshooting

### Rebuild required after App.tsx changes
Offline builds embed the JS bundle in the app package.  
So after any JS/TS change (like `App.tsx`), re-run the build script.

### Android: device/emulator not detected
```bash
adb devices
```

### iOS: CocoaPods errors
If `pod install` fails due to Ruby constraints, upgrade Ruby (Homebrew Ruby recommended) and reinstall CocoaPods.
