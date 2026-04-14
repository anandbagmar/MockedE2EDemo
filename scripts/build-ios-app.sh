#!/usr/bin/env bash
# build-ios-app.sh – Build iOS simulator apps (offline / no Metro required).
#
# Usage:
#   scripts/build-ios-app.sh <variant[,variant…]>
#
# Variants (comma-separated, no spaces):
#   debug          Debug simulator app
#   release        Release simulator app
#   debug-nml      Debug app + Applitools NML instrumentation
#   release-nml    Release app + Applitools NML instrumentation
#   all            All four variants
#
# NML builds download applitoolsify automatically to libs/ on first use.
# Download source: https://sdksstorage.blob.core.windows.net/mobile/ios/nml/applitoolsify/release/
#
# All outputs are written to builds/ as .app.zip archives.

set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT"

# ── Shared helpers ────────────────────────────────────────────────────────────
# shellcheck source=scripts/lib/logging.sh
source "${PROJECT_ROOT}/scripts/lib/logging.sh"
# shellcheck source=scripts/lib/icons-common.sh
source "${PROJECT_ROOT}/scripts/lib/icons-common.sh"
# shellcheck source=scripts/lib/builds-common.sh
source "${PROJECT_ROOT}/scripts/lib/builds-common.sh"
# shellcheck source=scripts/lib/nml-common.sh
source "${PROJECT_ROOT}/scripts/lib/nml-common.sh"

# ── Constants ─────────────────────────────────────────────────────────────────
APP_NAME="App Automation Playground"
IOS_DIR="ios"
DERIVED_DATA="${IOS_DIR}/build"
DIST_DIR="$(builds_platform_dir "ios")"

WORKSPACE="$(find "${IOS_DIR}" -maxdepth 1 -name "*.xcworkspace" -type d | head -n 1)"
if [[ -z "${WORKSPACE}" ]]; then
  fail "Could not find .xcworkspace under ios/. Run: (cd ios && pod install)"
fi

XCODE_SCHEME="$(basename "${WORKSPACE}" .xcworkspace)"

# Xcode build outputs
DEBUG_SIM_APP="${DERIVED_DATA}/Build/Products/Debug-iphonesimulator/${XCODE_SCHEME}.app"
RELEASE_SIM_APP="${DERIVED_DATA}/Build/Products/Release-iphonesimulator/${XCODE_SCHEME}.app"

# Final builds paths (zipped .app bundles)
DIST_DEBUG_APP="${DIST_DIR}/${APP_NAME}-debug.app.zip"
DIST_RELEASE_APP="${DIST_DIR}/${APP_NAME}-release.app.zip"
DIST_DEBUG_NML_APP="${DIST_DIR}/${APP_NAME}-debug-nml.app.zip"
DIST_RELEASE_NML_APP="${DIST_DIR}/${APP_NAME}-release-nml.app.zip"

BUILT_DEBUG=0
BUILT_RELEASE=0
BUILT_DEBUG_NML=0
BUILT_RELEASE_NML=0

# ── Helpers ───────────────────────────────────────────────────────────────────

ensure_dirs() {
  step "Creating output directories"
  mkdir -p "$DIST_DIR"
  info "Dist : ${DIST_DIR}"
  ok "Directories ready"
}

pod_install_if_needed() {
  if [[ ! -d "${IOS_DIR}/Pods" ]]; then
    step "Pods not found — running pod install"
    info "Working dir: ${IOS_DIR}/"
    pushd "${IOS_DIR}" >/dev/null
    pod install
    popd >/dev/null
    ok "pod install complete"
  else
    info "Pods already installed — skipping pod install"
  fi
}

bundle_js() {
  local DEV_FLAG="$1"
  local APP_PATH="$2"
  local ENTRY_FILE="index.js"
  [[ -f "index.ts"  ]] && ENTRY_FILE="index.ts"
  [[ -f "index.tsx" ]] && ENTRY_FILE="index.tsx"

  step "Bundling JavaScript"
  info "Entry  : ${ENTRY_FILE}"
  info "Dev    : ${DEV_FLAG}"
  info "Output : ${APP_PATH}/main.jsbundle"

  if [[ ! -d "${APP_PATH}" ]]; then
    fail ".app directory not found: ${APP_PATH}"
  fi

  npx react-native bundle \
    --platform ios \
    --dev "${DEV_FLAG}" \
    --entry-file "${ENTRY_FILE}" \
    --bundle-output "${APP_PATH}/main.jsbundle" \
    --assets-dest "${APP_PATH}"

  if [[ ! -f "${APP_PATH}/main.jsbundle" ]]; then
    fail "main.jsbundle not found inside .app after bundling"
  fi

  ok "JavaScript bundle written to ${APP_PATH}/main.jsbundle"
}

run_xcodebuild() {
  local CONFIGURATION="$1"
  step "Running xcodebuild: ${CONFIGURATION}"
  info "Workspace : ${WORKSPACE}"
  info "Scheme    : ${XCODE_SCHEME}"
  info "SDK       : iphonesimulator"
  info "DerivedData: ${DERIVED_DATA}"

  xcodebuild \
    -workspace "${WORKSPACE}" \
    -scheme "${XCODE_SCHEME}" \
    -configuration "${CONFIGURATION}" \
    -sdk iphonesimulator \
    -derivedDataPath "${DERIVED_DATA}" \
    clean build

  ok "xcodebuild ${CONFIGURATION} complete"
}

zip_app_to_dist() {
  local APP_PATH="$1"
  local DEST_ZIP="$2"

  step "Zipping .app to builds/"
  info "From : ${APP_PATH}"
  info "To   : ${DEST_ZIP}"

  if [[ ! -d "${APP_PATH}" ]]; then
    info "Contents of $(dirname "$APP_PATH"):"
    ls -la "$(dirname "$APP_PATH")" >&2 || true
    fail "Expected .app directory not found: ${APP_PATH}"
  fi

  local APP_DIR APP_BASENAME
  APP_DIR="$(dirname "$APP_PATH")"
  APP_BASENAME="$(basename "$APP_PATH")"

  pushd "$APP_DIR" >/dev/null
  zip -qr "$DEST_ZIP" "$APP_BASENAME"
  popd >/dev/null

  ok "App archive ready: $(basename "$DEST_ZIP")  ($(du -sh "$DEST_ZIP" | cut -f1))"
}

write_dist_metadata() {
  step "Writing builds metadata"
  local VERSION
  VERSION=$(node -p "require('./package.json').version" 2>/dev/null || echo "0.0.1")
  local BUILD_DATE
  BUILD_DATE=$(date -u "+%Y-%m-%d %H:%M UTC")

  info "Version    : ${VERSION}"
  info "Build date : ${BUILD_DATE}"

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

  ok "builds/version.txt updated"
}

# ── Build variants ────────────────────────────────────────────────────────────

build_debug() {
  if [[ "$BUILT_DEBUG" -eq 1 ]]; then
    info "iOS debug artifact already prepared — skipping rebuild"
    return 0
  fi

  banner "iOS  │  debug"
  ensure_dirs
  pod_install_if_needed
  run_xcodebuild Debug
  bundle_js true "${DEBUG_SIM_APP}"
  zip_app_to_dist "${DEBUG_SIM_APP}" "${DIST_DEBUG_APP}"
  BUILT_DEBUG=1
}

build_release() {
  if [[ "$BUILT_RELEASE" -eq 1 ]]; then
    info "iOS release artifact already prepared — skipping rebuild"
    return 0
  fi

  banner "iOS  │  release"
  ensure_dirs
  pod_install_if_needed
  run_xcodebuild Release
  bundle_js false "${RELEASE_SIM_APP}"
  zip_app_to_dist "${RELEASE_SIM_APP}" "${DIST_RELEASE_APP}"
  BUILT_RELEASE=1
}

build_debug_nml() {
  if [[ "$BUILT_DEBUG_NML" -eq 1 ]]; then
    info "iOS debug-nml artifact already prepared — skipping re-instrumentation"
    return 0
  fi

  banner "iOS  │  debug-nml"
  info "Prerequisite: debug .app.zip must exist before NML instrumentation"
  build_debug
  ensure_applitoolsify "ios"
  apply_nml_ios "${DIST_DEBUG_APP}" "${DIST_DEBUG_NML_APP}"
  BUILT_DEBUG_NML=1
}

build_release_nml() {
  if [[ "$BUILT_RELEASE_NML" -eq 1 ]]; then
    info "iOS release-nml artifact already prepared — skipping re-instrumentation"
    return 0
  fi

  banner "iOS  │  release-nml"
  info "Prerequisite: release .app.zip must exist before NML instrumentation"
  build_release
  ensure_applitoolsify "ios"
  apply_nml_ios "${DIST_RELEASE_APP}" "${DIST_RELEASE_NML_APP}"
  BUILT_RELEASE_NML=1
}

build_all() {
  banner "iOS  │  all variants"
  ensure_dirs

  step "Pre-downloading NML binary (shared across all variants)"
  ensure_applitoolsify "ios"

  build_debug
  build_release
  build_debug_nml
  build_release_nml
}

# ── Usage ─────────────────────────────────────────────────────────────────────

usage() {
  cat <<EOF
Usage: scripts/build-ios-app.sh <variant[,variant…]>

Variants (comma-separated, no spaces):
  debug          Debug simulator app   → builds/${APP_NAME}-debug.app.zip
  release        Release simulator app → builds/${APP_NAME}-release.app.zip
  debug-nml      Debug + NML           → builds/${APP_NAME}-debug-nml.app.zip
                 Requires debug .app.zip first; script handles this automatically
  release-nml    Release + NML         → builds/${APP_NAME}-release-nml.app.zip
                 Requires release .app.zip first; script handles this automatically
  all            Build all four variants

Examples:
  scripts/build-ios-app.sh debug
  scripts/build-ios-app.sh debug,release
  scripts/build-ios-app.sh debug,debug-nml
  scripts/build-ios-app.sh release,release-nml
  scripts/build-ios-app.sh all

NML builds download applitoolsify automatically to libs/ on first use.
EOF
}

# ── Per-variant dispatcher ────────────────────────────────────────────────────

run_variant() {
  local v="$1"
  case "$v" in
    debug)       build_debug ;;
    release)     build_release ;;
    debug-nml)   build_debug_nml ;;
    release-nml) build_release_nml ;;
    all)         build_all ;;
    *)
      fail "Unknown variant: '${v}'. Valid: debug | release | debug-nml | release-nml | all"
      ;;
  esac
}

# ── Entry point ───────────────────────────────────────────────────────────────

input="${1:-}"
[[ -z "$input" ]] && { usage; exit 1; }

# Auto-generate app icons if source image is present and stale
ensure_icons

IFS=',' read -ra VARIANTS <<< "$input"

step "Build plan: iOS  │  ${input}"
info "Variants to build : ${VARIANTS[*]}"
info "Project root      : ${PROJECT_ROOT}"
info "Workspace         : ${WORKSPACE}"
info "Scheme            : ${XCODE_SCHEME}"
info "Output dir        : ${DIST_DIR}"
info "Run root          : ${PROJECT_ROOT}/builds/${BUILD_TIMESTAMP_ROOT}"
echo ""

for v in "${VARIANTS[@]}"; do
  v="${v//[[:space:]]/}"
  run_variant "$v"
done

write_dist_metadata

banner "iOS build complete"
step "Artifacts in ${DIST_DIR}/"
ls -lh "${DIST_DIR}/"
echo ""
