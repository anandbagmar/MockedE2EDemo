#!/usr/bin/env bash
set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT"

ASSETS_DIR="android/app/src/main/assets"
RES_DIR="android/app/src/main/res"

# Desired final APK names
DEBUG_APK="android/app/build/outputs/apk/debug/SpecmaticAndroidE2EDemo-debug.apk"
RELEASE_APK="android/app/build/outputs/apk/release/SpecmaticAndroidE2EDemo-release.apk"

# Actual Gradle outputs (default)
GRADLE_DEBUG_APK="android/app/build/outputs/apk/debug/app-debug.apk"
GRADLE_RELEASE_APK="android/app/build/outputs/apk/release/app-release.apk"

ensure_assets_dir() {
  mkdir -p "$ASSETS_DIR"
}

bundle_js() {
  local DEV_FLAG="$1"   # true/false
  local ENTRY_FILE="index.js"
  if [[ -f "index.ts" ]]; then ENTRY_FILE="index.ts"; fi
  if [[ -f "index.tsx" ]]; then ENTRY_FILE="index.tsx"; fi

  echo "==> Bundling JS (dev=${DEV_FLAG}) using entry file: ${ENTRY_FILE}"
  npx react-native bundle \
    --platform android \
    --dev "${DEV_FLAG}" \
    --entry-file "${ENTRY_FILE}" \
    --bundle-output "${ASSETS_DIR}/index.android.bundle" \
    --assets-dest "${RES_DIR}"
}

rename_apk() {
  local SRC="$1"
  local DEST="$2"

  if [[ ! -f "$SRC" ]]; then
    echo "❌ Expected APK not found: $SRC"
    echo "   Listing output folder:"
    ls -la "$(dirname "$SRC")" || true
    exit 1
  fi

  mkdir -p "$(dirname "$DEST")"
  cp -f "$SRC" "$DEST"
  echo "==> Renamed APK: $DEST"
}

build_debug_offline() {
  echo "==> Building OFFLINE DEBUG APK (includes JS bundle; no Metro needed)"
  ensure_assets_dir
  bundle_js true

  pushd android >/dev/null
  ./gradlew :app:assembleDebug
  popd >/dev/null

  rename_apk "$GRADLE_DEBUG_APK" "$DEBUG_APK"
  echo "==> DEBUG APK (final): ${DEBUG_APK}"
}

build_release_offline() {
  echo "==> Building OFFLINE RELEASE APK (includes JS bundle; no Metro needed)"
  ensure_assets_dir
  bundle_js false

  pushd android >/dev/null
  ./gradlew :app:assembleRelease
  popd >/dev/null

  rename_apk "$GRADLE_RELEASE_APK" "$RELEASE_APK"
  echo "==> RELEASE APK (final): ${RELEASE_APK}"
}

build_all_offline() {
  echo "==> Building OFFLINE DEBUG + RELEASE (single clean; no Metro needed)"
  ensure_assets_dir

  # Clean once
  pushd android >/dev/null
  ./gradlew clean
  popd >/dev/null

  # Build debug (dev bundle)
  bundle_js true
  pushd android >/dev/null
  ./gradlew :app:assembleDebug
  popd >/dev/null
  rename_apk "$GRADLE_DEBUG_APK" "$DEBUG_APK"
  echo "==> DEBUG APK (final): ${DEBUG_APK}"

  # Build release (prod bundle)
  bundle_js false
  pushd android >/dev/null
  ./gradlew :app:assembleRelease
  popd >/dev/null
  rename_apk "$GRADLE_RELEASE_APK" "$RELEASE_APK"
  echo "==> RELEASE APK (final): ${RELEASE_APK}"
}

install_debug() {
  echo "==> Installing DEBUG APK on connected device/emulator"
  if [[ ! -f "$DEBUG_APK" ]]; then
    echo "⚠️  Renamed debug APK not found. Building first..."
    build_debug_offline
  fi
  adb install -r "$DEBUG_APK"
}

install_release() {
  echo "==> Installing RELEASE APK on connected device/emulator"
  if [[ ! -f "$RELEASE_APK" ]]; then
    echo "⚠️  Renamed release APK not found. Building first..."
    build_release_offline
  fi
  adb install -r "$RELEASE_APK"
}

usage() {
  cat <<EOF
Usage:
  scripts/build-android-apks.sh debug        # offline debug APK (renamed)
  scripts/build-android-apks.sh release      # offline release APK (renamed)
  scripts/build-android-apks.sh all          # build both (single clean; renamed)
  scripts/build-android-apks.sh debug+install
  scripts/build-android-apks.sh release+install

Outputs:
  $DEBUG_APK
  $RELEASE_APK

Notes:
- These APKs run WITHOUT Metro.
- Re-run after App.tsx changes to rebuild the embedded JS bundle.
EOF
}

cmd="${1:-}"
case "$cmd" in
  debug)
    build_debug_offline
    ;;
  release)
    build_release_offline
    ;;
  all)
    build_all_offline
    ;;
  debug+install)
    build_debug_offline
    install_debug
    ;;
  release+install)
    build_release_offline
    install_release
    ;;
  *)
    usage
    exit 1
    ;;
esac
