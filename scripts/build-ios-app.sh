#!/usr/bin/env bash
set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT"

IOS_DIR="ios"
DERIVED_DATA="${IOS_DIR}/build"

WORKSPACE="$(find "${IOS_DIR}" -maxdepth 1 -name "*.xcworkspace" -type d | head -n 1)"
if [[ -z "${WORKSPACE}" ]]; then
  echo "❌ Could not find an .xcworkspace under ios/. Run: (cd ios && pod install)"
  exit 1
fi

APP_NAME="$(basename "${WORKSPACE}" .xcworkspace)"
SCHEME="${APP_NAME}"

DEBUG_SIM_APP="${DERIVED_DATA}/Build/Products/Debug-iphonesimulator/${APP_NAME}.app"
RELEASE_SIM_APP="${DERIVED_DATA}/Build/Products/Release-iphonesimulator/${APP_NAME}.app"

detect_entry_file() {
  local ENTRY="index.js"
  [[ -f "index.ts" ]] && ENTRY="index.ts"
  [[ -f "index.tsx" ]] && ENTRY="index.tsx"
  echo "$ENTRY"
}

pod_install_if_needed() {
  if [[ ! -d "${IOS_DIR}/Pods" ]]; then
    echo "==> Pods not found. Running pod install..."
    pushd "${IOS_DIR}" >/dev/null
    pod install
    popd >/dev/null
  fi
}

bundle_into_app() {
  local DEV_FLAG="$1"      # true/false
  local APP_PATH="$2"      # .../*.app
  local ENTRY_FILE
  ENTRY_FILE="$(detect_entry_file)"

  if [[ ! -d "${APP_PATH}" ]]; then
    echo "❌ .app not found: ${APP_PATH}"
    exit 1
  fi

  echo "==> Bundling iOS JS INTO app (dev=${DEV_FLAG}) entry=${ENTRY_FILE}"
  echo "    Target app: ${APP_PATH}"

  # Put bundle directly inside the .app so Bundle.main.url(...) can find it
  npx react-native bundle \
    --platform ios \
    --dev "${DEV_FLAG}" \
    --entry-file "${ENTRY_FILE}" \
    --bundle-output "${APP_PATH}/main.jsbundle" \
    --assets-dest "${APP_PATH}"

  if [[ ! -f "${APP_PATH}/main.jsbundle" ]]; then
    echo "❌ main.jsbundle not found inside app: ${APP_PATH}/main.jsbundle"
    exit 1
  fi

  echo "✅ Embedded bundle: ${APP_PATH}/main.jsbundle"
}

build_debug() {
  echo "==> Building iOS DEBUG simulator app (independent; no Metro)"
  pod_install_if_needed

  xcodebuild \
    -workspace "${WORKSPACE}" \
    -scheme "${SCHEME}" \
    -configuration Debug \
    -sdk iphonesimulator \
    -derivedDataPath "${DERIVED_DATA}" \
    clean build

  echo "==> Built: ${DEBUG_SIM_APP}"
  bundle_into_app true "${DEBUG_SIM_APP}"
}

build_release() {
  echo "==> Building iOS RELEASE simulator app (independent; no Metro)"
  pod_install_if_needed

  xcodebuild \
    -workspace "${WORKSPACE}" \
    -scheme "${SCHEME}" \
    -configuration Release \
    -sdk iphonesimulator \
    -derivedDataPath "${DERIVED_DATA}" \
    clean build

  echo "==> Built: ${RELEASE_SIM_APP}"
  bundle_into_app false "${RELEASE_SIM_APP}"
}

usage() {
  cat <<EOF
Usage:
  scripts/build-ios-independent.sh debug
  scripts/build-ios-independent.sh release
  scripts/build-ios-independent.sh all

Outputs:
  Debug:   ${DEBUG_SIM_APP}
  Release: ${RELEASE_SIM_APP}

Notes:
- This script embeds main.jsbundle directly into the built .app, so Metro is NOT needed.
- Ensure AppDelegate.swift returns Bundle.main.url(forResource:"main", withExtension:"jsbundle")
EOF
}

cmd="${1:-}"
case "${cmd}" in
  debug) build_debug ;;
  release) build_release ;;
  all) build_debug; build_release ;;
  *) usage; exit 1 ;;
esac
