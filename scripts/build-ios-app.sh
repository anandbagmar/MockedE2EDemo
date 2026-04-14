#!/usr/bin/env bash
# build-ios-app.sh – Build iOS simulator apps (offline / no Metro required).
#
# Usage:
#   scripts/build-ios-app.sh debug
#   scripts/build-ios-app.sh release
#   scripts/build-ios-app.sh debug-nml
#   scripts/build-ios-app.sh release-nml
#   scripts/build-ios-app.sh all
#
# NML (Applitools Native Mobile Library) dynamic instrumentation:
#   The applitoolsify binary is downloaded automatically to libs/ on first use.
#   No manual setup is required.
#   Download source: https://sdksstorage.blob.core.windows.net/mobile/ios/nml/applitoolsify/release/
#
# Outputs (all in dist/):
#   App Automation Playground-debug.app      (zipped as .zip for distribution)
#   App Automation Playground-release.app
#   App Automation Playground-debug-nml.app
#   App Automation Playground-release-nml.app

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
IOS_DIR="ios"
DERIVED_DATA="${IOS_DIR}/build"
DIST_DIR="${PROJECT_ROOT}/dist"

WORKSPACE="$(find "${IOS_DIR}" -maxdepth 1 -name "*.xcworkspace" -type d | head -n 1)"
if [[ -z "${WORKSPACE}" ]]; then
  echo "❌ Could not find .xcworkspace under ios/. Run: (cd ios && pod install)"
  exit 1
fi

XCODE_SCHEME="$(basename "${WORKSPACE}" .xcworkspace)"

# Xcode build outputs
DEBUG_SIM_APP="${DERIVED_DATA}/Build/Products/Debug-iphonesimulator/${XCODE_SCHEME}.app"
RELEASE_SIM_APP="${DERIVED_DATA}/Build/Products/Release-iphonesimulator/${XCODE_SCHEME}.app"

# Final dist paths (zipped .app bundles)
DIST_DEBUG_APP="${DIST_DIR}/${APP_NAME}-debug.app.zip"
DIST_RELEASE_APP="${DIST_DIR}/${APP_NAME}-release.app.zip"
DIST_DEBUG_NML_APP="${DIST_DIR}/${APP_NAME}-debug-nml.app.zip"
DIST_RELEASE_NML_APP="${DIST_DIR}/${APP_NAME}-release-nml.app.zip"

# ─── helpers ─────────────────────────────────────────────────────────────────

detect_entry_file() {
  local ENTRY="index.js"
  [[ -f "index.ts"  ]] && ENTRY="index.ts"
  [[ -f "index.tsx" ]] && ENTRY="index.tsx"
  echo "$ENTRY"
}

pod_install_if_needed() {
  if [[ ! -d "${IOS_DIR}/Pods" ]]; then
    echo "==> Pods not found. Running pod install …"
    pushd "${IOS_DIR}" >/dev/null
    pod install
    popd >/dev/null
  fi
}

bundle_into_app() {
  local DEV_FLAG="$1"
  local APP_PATH="$2"
  local ENTRY_FILE
  ENTRY_FILE="$(detect_entry_file)"

  if [[ ! -d "${APP_PATH}" ]]; then
    echo "❌ .app not found: ${APP_PATH}"
    exit 1
  fi

  echo "==> Bundling iOS JS into .app (dev=${DEV_FLAG}) entry=${ENTRY_FILE}"
  npx react-native bundle \
    --platform ios \
    --dev "${DEV_FLAG}" \
    --entry-file "${ENTRY_FILE}" \
    --bundle-output "${APP_PATH}/main.jsbundle" \
    --assets-dest "${APP_PATH}"

  if [[ ! -f "${APP_PATH}/main.jsbundle" ]]; then
    echo "❌ main.jsbundle not found inside .app"
    exit 1
  fi
  echo "✅ Embedded bundle: ${APP_PATH}/main.jsbundle"
}

zip_app_to_dist() {
  local APP_PATH="$1"   # path to .app directory
  local DEST_ZIP="$2"   # destination zip in dist/

  mkdir -p "$DIST_DIR"
  local APP_DIR
  APP_DIR="$(dirname "$APP_PATH")"
  local APP_BASENAME
  APP_BASENAME="$(basename "$APP_PATH")"

  pushd "$APP_DIR" >/dev/null
  zip -qr "$DEST_ZIP" "$APP_BASENAME"
  popd >/dev/null
  echo "==> Copied to dist: $(basename "$DEST_ZIP")"
}

# apply_nml_ios is provided directly by scripts/lib/nml-common.sh (sourced above).

write_dist_metadata() {
  local VERSION
  VERSION=$(node -p "require('./package.json').version" 2>/dev/null || echo "0.0.1")
  local BUILD_DATE
  BUILD_DATE=$(date -u "+%Y-%m-%d %H:%M UTC")

  cat > "${DIST_DIR}/version.txt" <<EOF
Version:    ${VERSION}
Build date: ${BUILD_DATE}
Platform:   iOS (simulator)
App:        ${APP_NAME}
EOF

  if [[ ! -f "${DIST_DIR}/CHANGELOG.md" ]]; then
    cat > "${DIST_DIR}/CHANGELOG.md" <<EOF
# Changelog – ${APP_NAME} (iOS)

## ${VERSION} – ${BUILD_DATE}
- Initial build
EOF
  fi
  echo "==> dist/version.txt and dist/CHANGELOG.md updated"
}

# ─── build variants ──────────────────────────────────────────────────────────

build_debug() {
  echo "==> Building iOS DEBUG simulator app"
  pod_install_if_needed
  mkdir -p "$DIST_DIR"

  xcodebuild \
    -workspace "${WORKSPACE}" \
    -scheme "${XCODE_SCHEME}" \
    -configuration Debug \
    -sdk iphonesimulator \
    -derivedDataPath "${DERIVED_DATA}" \
    clean build

  bundle_into_app true "${DEBUG_SIM_APP}"
  zip_app_to_dist "${DEBUG_SIM_APP}" "${DIST_DEBUG_APP}"
}

build_release() {
  echo "==> Building iOS RELEASE simulator app"
  pod_install_if_needed
  mkdir -p "$DIST_DIR"

  xcodebuild \
    -workspace "${WORKSPACE}" \
    -scheme "${XCODE_SCHEME}" \
    -configuration Release \
    -sdk iphonesimulator \
    -derivedDataPath "${DERIVED_DATA}" \
    clean build

  bundle_into_app false "${RELEASE_SIM_APP}"
  zip_app_to_dist "${RELEASE_SIM_APP}" "${DIST_RELEASE_APP}"
}

build_debug_nml() {
  echo "==> Building iOS DEBUG app + NML instrumentation"
  build_debug
  ensure_applitoolsify "ios"          # downloads binary to libs/ if absent
  apply_nml_ios "${DIST_DEBUG_APP}" "${DIST_DEBUG_NML_APP}"
}

build_release_nml() {
  echo "==> Building iOS RELEASE app + NML instrumentation"
  build_release
  ensure_applitoolsify "ios"
  apply_nml_ios "${DIST_RELEASE_APP}" "${DIST_RELEASE_NML_APP}"
}

build_all() {
  echo "==> Building ALL iOS variants"
  # Pre-download the NML binary once before building
  ensure_applitoolsify "ios"
  build_debug
  build_release
  apply_nml_ios "${DIST_DEBUG_APP}"   "${DIST_DEBUG_NML_APP}"
  apply_nml_ios "${DIST_RELEASE_APP}" "${DIST_RELEASE_NML_APP}"
}

# ─── usage ───────────────────────────────────────────────────────────────────

usage() {
  cat <<EOF
Usage: scripts/build-ios-app.sh <command>

Commands:
  debug          Debug simulator app   → dist/${APP_NAME}-debug.app.zip
  release        Release simulator app → dist/${APP_NAME}-release.app.zip
  debug-nml      Debug + NML           → dist/${APP_NAME}-debug-nml.app.zip
  release-nml    Release + NML         → dist/${APP_NAME}-release-nml.app.zip
  all            Build all four variants

NML instrumentation:
  The applitoolsify binary is downloaded automatically to libs/ on first use.
  Supported hosts: macOS (arm64 / x86_64) and Linux (x86_64).

Notes:
  - Apps are zipped and copied to dist/. Unzip before running with Appium.
  - Apps run WITHOUT Metro (JS bundle is embedded).
  - AppDelegate.swift must load Bundle.main "main.jsbundle" (not Metro).
  - Ensure CocoaPods is installed: gem install cocoapods
EOF
}

# ─── dispatch ────────────────────────────────────────────────────────────────

cmd="${1:-}"
case "${cmd}" in
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
