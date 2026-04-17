#!/usr/bin/env bash
# build-android-apks.sh – Build Android APKs (offline / no Metro required).
#
# Usage:
#   scripts/build-android-apks.sh <variant[,variant…]>
#
# Variants (comma-separated, no spaces):
#   debug          Offline debug APK
#   release        Offline release APK
#   debug-nml      Debug APK + Applitools NML instrumentation
#   release-nml    Release APK + Applitools NML instrumentation
#   all            All four variants
#
# NML builds download applitoolsify automatically to libs/ on first use.
# Download source: https://sdksstorage.blob.core.windows.net/mobile/android/nml/release/
#
# All outputs are written to builds/.

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
ASSETS_DIR="android/app/src/main/assets"
RES_DIR="android/app/src/main/res"
BUILD_TIMESTAMP_ROOT="$(builds_timestamp_root)"
DIST_DIR="$(builds_platform_dir "android")"

GRADLE_DEBUG_APK="android/app/build/outputs/apk/debug/app-debug.apk"
GRADLE_RELEASE_APK="android/app/build/outputs/apk/release/app-release.apk"

DIST_DEBUG_APK="${DIST_DIR}/${APP_NAME}-debug.apk"
DIST_RELEASE_APK="${DIST_DIR}/${APP_NAME}-release.apk"
DIST_DEBUG_NML_APK="${DIST_DIR}/${APP_NAME}-debug-nml.apk"
DIST_RELEASE_NML_APK="${DIST_DIR}/${APP_NAME}-release-nml.apk"

BUILT_DEBUG=0
BUILT_RELEASE=0
BUILT_DEBUG_NML=0
BUILT_RELEASE_NML=0

# ── Helpers ───────────────────────────────────────────────────────────────────

ensure_dirs() {
  step "Creating output directories"
  mkdir -p "$ASSETS_DIR" "$DIST_DIR"
  info "Assets : ${ASSETS_DIR}"
  info "Dist   : ${DIST_DIR}"
  ok "Directories ready"
}

bundle_js() {
  local DEV_FLAG="$1"
  local ENTRY_FILE="index.js"
  [[ -f "index.ts"  ]] && ENTRY_FILE="index.ts"
  [[ -f "index.tsx" ]] && ENTRY_FILE="index.tsx"

  step "Bundling JavaScript"
  info "Entry  : ${ENTRY_FILE}"
  info "Dev    : ${DEV_FLAG}"
  info "Output : ${ASSETS_DIR}/index.android.bundle"

  npx react-native bundle \
    --platform android \
    --dev "${DEV_FLAG}" \
    --entry-file "${ENTRY_FILE}" \
    --bundle-output "${ASSETS_DIR}/index.android.bundle" \
    --assets-dest "${RES_DIR}"

  ok "JavaScript bundle written to ${ASSETS_DIR}/index.android.bundle"
}

run_gradle() {
  local TASK="$1"
  step "Running Gradle task: ${TASK}"
  info "Working dir: android/"
  pushd android >/dev/null
  ./gradlew "${TASK}"
  popd >/dev/null
  ok "Gradle ${TASK} complete"
}

copy_to_dist() {
  local SRC="$1"
  local DEST="$2"

  step "Copying APK to builds/"
  info "From : ${SRC}"
  info "To   : ${DEST}"

  if [[ ! -f "$SRC" ]]; then
    info "Directory contents:"
    ls -la "$(dirname "$SRC")" >&2 || true
    fail "Expected APK not found: ${SRC}"
  fi

  cp -f "$SRC" "$DEST"
  ok "APK ready: $(basename "$DEST")  ($(du -sh "$DEST" | cut -f1))"
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

  ok "builds/version.txt updated"
}

# ── Build variants ────────────────────────────────────────────────────────────

build_debug() {
  if [[ "$BUILT_DEBUG" -eq 1 ]]; then
    info "Android debug artifact already prepared — skipping rebuild"
    return 0
  fi

  banner "Android  │  debug"
  ensure_dirs
  bundle_js true
  run_gradle :app:assembleDebug
  copy_to_dist "$GRADLE_DEBUG_APK" "$DIST_DEBUG_APK"
  BUILT_DEBUG=1
}

build_release() {
  if [[ "$BUILT_RELEASE" -eq 1 ]]; then
    info "Android release artifact already prepared — skipping rebuild"
    return 0
  fi

  banner "Android  │  release"
  ensure_dirs
  bundle_js false
  run_gradle :app:assembleRelease
  copy_to_dist "$GRADLE_RELEASE_APK" "$DIST_RELEASE_APK"
  BUILT_RELEASE=1
}

build_debug_nml() {
  if [[ "$BUILT_DEBUG_NML" -eq 1 ]]; then
    info "Android debug-nml artifact already prepared — skipping re-instrumentation"
    return 0
  fi

  banner "Android  │  debug-nml"
  info "Prerequisite: debug APK must exist before NML instrumentation"
  build_debug
  ensure_applitoolsify "android"
  apply_nml_android "$DIST_DEBUG_APK" "$DIST_DEBUG_NML_APK"
  apply_nml_android "$DIST_DEBUG_APK" "${DIST_DIR}/${APP_NAME}-debug-nml-r.apk" "-r"
  BUILT_DEBUG_NML=1
}

build_release_nml() {
  if [[ "$BUILT_RELEASE_NML" -eq 1 ]]; then
    info "Android release-nml artifact already prepared — skipping re-instrumentation"
    return 0
  fi

  banner "Android  │  release-nml"
  info "Prerequisite: release APK must exist before NML instrumentation"
  build_release
  ensure_applitoolsify "android"
  apply_nml_android "$DIST_RELEASE_APK" "$DIST_RELEASE_NML_APK"
  apply_nml_android "$DIST_RELEASE_APK" "${DIST_DIR}/${APP_NAME}-release-nml-r.apk" "-r"
  BUILT_RELEASE_NML=1
}

build_all() {
  banner "Android  │  all variants"
  ensure_dirs

  step "Pre-downloading NML binary (shared across all variants)"
  ensure_applitoolsify "android"

  step "Cleaning previous Gradle build"
  run_gradle clean

  build_debug
  build_release
  build_debug_nml
  build_release_nml
}

# ── Usage ─────────────────────────────────────────────────────────────────────

usage() {
  cat <<EOF
Usage: scripts/build-android-apks.sh <variant[,variant…]>

Variants (comma-separated, no spaces):
  debug          Offline debug APK  → builds/${APP_NAME}-debug.apk
  release        Offline release APK→ builds/${APP_NAME}-release.apk
  debug-nml      Debug + NML        → builds/${APP_NAME}-debug-nml.apk
                 Requires debug APK first; script handles this automatically
  release-nml    Release + NML      → builds/${APP_NAME}-release-nml.apk
                 Requires release APK first; script handles this automatically
  all            Build all four variants

Examples:
  scripts/build-android-apks.sh debug
  scripts/build-android-apks.sh debug,release
  scripts/build-android-apks.sh debug,debug-nml
  scripts/build-android-apks.sh release,release-nml
  scripts/build-android-apks.sh all

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

step "Build plan: Android  │  ${input}"
info "Variants to build: ${VARIANTS[*]}"
info "Project root      : ${PROJECT_ROOT}"
info "Output dir        : ${DIST_DIR}"
info "Run root          : ${PROJECT_ROOT}/builds/${BUILD_TIMESTAMP_ROOT}"
echo ""

for v in "${VARIANTS[@]}"; do
  v="${v//[[:space:]]/}"
  run_variant "$v"
done

write_dist_metadata

banner "Android build complete"
step "Artifacts in ${DIST_DIR}/"
ls -lh "${DIST_DIR}/"
echo ""
