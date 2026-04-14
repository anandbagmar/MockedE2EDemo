#!/usr/bin/env bash
# build-android-apks.sh – Build Android APKs (offline / no Metro required).
#
# Usage:
#   scripts/build-android-apks.sh debug
#   scripts/build-android-apks.sh release
#   scripts/build-android-apks.sh debug-nml
#   scripts/build-android-apks.sh release-nml
#   scripts/build-android-apks.sh all            # builds all four variants
#
# NML (Applitools Native Mobile Library) dynamic instrumentation:
#   The applitoolsify binary is downloaded automatically to libs/ on first use.
#   No manual setup is required.
#   Download source: https://sdksstorage.blob.core.windows.net/mobile/android/nml/release/
#
# Outputs (all in dist/):
#   App Automation Playground-debug.apk
#   App Automation Playground-release.apk
#   App Automation Playground-debug-nml.apk
#   App Automation Playground-release-nml.apk

set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT"

# Shared helpers
# shellcheck source=scripts/lib/icons-common.sh
source "${PROJECT_ROOT}/scripts/lib/icons-common.sh"
# shellcheck source=scripts/lib/nml-common.sh
source "${PROJECT_ROOT}/scripts/lib/nml-common.sh"

# Auto-generate app icons if source image is present and icons are stale
ensure_icons

APP_NAME="App Automation Playground"
ASSETS_DIR="android/app/src/main/assets"
RES_DIR="android/app/src/main/res"
DIST_DIR="${PROJECT_ROOT}/dist"

# Gradle raw outputs
GRADLE_DEBUG_APK="android/app/build/outputs/apk/debug/app-debug.apk"
GRADLE_RELEASE_APK="android/app/build/outputs/apk/release/app-release.apk"

# Final dist paths
DIST_DEBUG_APK="${DIST_DIR}/${APP_NAME}-debug.apk"
DIST_RELEASE_APK="${DIST_DIR}/${APP_NAME}-release.apk"
DIST_DEBUG_NML_APK="${DIST_DIR}/${APP_NAME}-debug-nml.apk"
DIST_RELEASE_NML_APK="${DIST_DIR}/${APP_NAME}-release-nml.apk"

# ─── helpers ─────────────────────────────────────────────────────────────────

ensure_dirs() {
  mkdir -p "$ASSETS_DIR" "$DIST_DIR"
}

bundle_js() {
  local DEV_FLAG="$1"
  local ENTRY_FILE="index.js"
  [[ -f "index.ts"  ]] && ENTRY_FILE="index.ts"
  [[ -f "index.tsx" ]] && ENTRY_FILE="index.tsx"

  echo "==> Bundling JS (dev=${DEV_FLAG}) entry=${ENTRY_FILE}"
  npx react-native bundle \
    --platform android \
    --dev "${DEV_FLAG}" \
    --entry-file "${ENTRY_FILE}" \
    --bundle-output "${ASSETS_DIR}/index.android.bundle" \
    --assets-dest "${RES_DIR}"
}

copy_to_dist() {
  local SRC="$1"
  local DEST="$2"

  if [[ ! -f "$SRC" ]]; then
    echo "❌ Expected APK not found: $SRC"
    ls -la "$(dirname "$SRC")" || true
    exit 1
  fi
  cp -f "$SRC" "$DEST"
  echo "==> Copied to dist: $(basename "$DEST")"
}

write_dist_metadata() {
  local VERSION
  VERSION=$(node -p "require('./package.json').version" 2>/dev/null || echo "0.0.1")
  local BUILD_DATE
  BUILD_DATE=$(date -u "+%Y-%m-%d %H:%M UTC")

  cat > "${DIST_DIR}/version.txt" <<EOF
Version:    ${VERSION}
Build date: ${BUILD_DATE}
Platform:   Android
App:        ${APP_NAME}
EOF

  if [[ ! -f "${DIST_DIR}/CHANGELOG.md" ]]; then
    cat > "${DIST_DIR}/CHANGELOG.md" <<EOF
# Changelog – ${APP_NAME} (Android)

## ${VERSION} – ${BUILD_DATE}
- Initial build
EOF
  fi
  echo "==> dist/version.txt and dist/CHANGELOG.md updated"
}

# ─── build variants ──────────────────────────────────────────────────────────

build_debug() {
  echo "==> Building OFFLINE DEBUG APK"
  ensure_dirs
  bundle_js true
  pushd android >/dev/null
  ./gradlew :app:assembleDebug
  popd >/dev/null
  copy_to_dist "$GRADLE_DEBUG_APK" "$DIST_DEBUG_APK"
}

build_release() {
  echo "==> Building OFFLINE RELEASE APK"
  ensure_dirs
  bundle_js false
  pushd android >/dev/null
  ./gradlew :app:assembleRelease
  popd >/dev/null
  copy_to_dist "$GRADLE_RELEASE_APK" "$DIST_RELEASE_APK"
}

build_debug_nml() {
  echo "==> Building OFFLINE DEBUG APK + NML instrumentation"
  build_debug
  ensure_applitoolsify "android"   # downloads binary to libs/ if absent
  apply_nml_android "$DIST_DEBUG_APK" "$DIST_DEBUG_NML_APK"
}

build_release_nml() {
  echo "==> Building OFFLINE RELEASE APK + NML instrumentation"
  build_release
  ensure_applitoolsify "android"
  apply_nml_android "$DIST_RELEASE_APK" "$DIST_RELEASE_NML_APK"
}

build_all() {
  echo "==> Building ALL variants (debug, release, debug-nml, release-nml)"
  ensure_dirs
  # Pre-download the NML binary once before all builds
  ensure_applitoolsify "android"

  pushd android >/dev/null
  ./gradlew clean
  popd >/dev/null

  build_debug
  build_release
  apply_nml_android "$DIST_DEBUG_APK"   "$DIST_DEBUG_NML_APK"
  apply_nml_android "$DIST_RELEASE_APK" "$DIST_RELEASE_NML_APK"
}

# ─── usage ───────────────────────────────────────────────────────────────────

usage() {
  cat <<EOF
Usage: scripts/build-android-apks.sh <command>

Commands:
  debug          Offline debug APK  → dist/${APP_NAME}-debug.apk
  release        Offline release APK→ dist/${APP_NAME}-release.apk
  debug-nml      Debug + NML        → dist/${APP_NAME}-debug-nml.apk
  release-nml    Release + NML      → dist/${APP_NAME}-release-nml.apk
  all            Build all four variants

NML instrumentation:
  The applitoolsify binary is downloaded automatically to libs/ on first use.
  Supported hosts: macOS (arm64 / x86_64) and Linux (x86_64).

Notes:
  - All APKs are copied to the dist/ folder.
  - APKs run WITHOUT Metro (JS bundle is embedded).
  - Re-run after App.tsx changes to rebuild the embedded JS bundle.
EOF
}

# ─── dispatch ────────────────────────────────────────────────────────────────

cmd="${1:-}"
case "$cmd" in
  debug)       build_debug;       write_dist_metadata ;;
  release)     build_release;     write_dist_metadata ;;
  debug-nml)   build_debug_nml;   write_dist_metadata ;;
  release-nml) build_release_nml; write_dist_metadata ;;
  all)         build_all;         write_dist_metadata ;;
  *)           usage; exit 1 ;;
esac

echo ""
echo "✅ Done. Artifacts in: ${DIST_DIR}/"
ls -lh "${DIST_DIR}/"
