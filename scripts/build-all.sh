#!/usr/bin/env bash
# build-all.sh – Master build script for App Automation Playground (Android + iOS).
#
# Usage:
#   scripts/build-all.sh <platform> <variant[,variant…]>
#
#   platform  : android | ios | all
#   variant   : debug | release | debug-nml | release-nml | all
#               comma-separate multiple variants, no spaces
#
# Examples:
#   scripts/build-all.sh android debug            # Android debug only
#   scripts/build-all.sh ios release              # iOS release only
#   scripts/build-all.sh android debug,debug-nml  # Android debug + debug-nml
#   scripts/build-all.sh all debug,release        # both platforms, debug + release
#   scripts/build-all.sh all release,release-nml  # both platforms, release + NML

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"

# shellcheck source=scripts/lib/logging.sh
source "${PROJECT_ROOT}/scripts/lib/logging.sh"
# shellcheck source=scripts/lib/builds-common.sh
source "${PROJECT_ROOT}/scripts/lib/builds-common.sh"

usage() {
  cat <<EOF
Usage: scripts/build-all.sh <platform> <variant[,variant…]>

Platforms:
  android
  ios
  all

Variants (comma-separated, no spaces):
  debug
  release
  debug-nml
  release-nml
  all

Examples:
  scripts/build-all.sh android debug
  scripts/build-all.sh ios release
  scripts/build-all.sh android debug,debug-nml
  scripts/build-all.sh all debug,release
  scripts/build-all.sh all release,release-nml
EOF
}

[[ $# -lt 2 ]] && { usage; exit 1; }

PLATFORM="$1"
VARIANT_ARG="$2"
BUILD_TIMESTAMP_ROOT="${BUILD_TIMESTAMP_ROOT:-$(builds_timestamp_root)}"
export BUILD_TIMESTAMP_ROOT
BUILD_OUTPUT_ROOT="$(ensure_builds_root)"
export BUILD_OUTPUT_ROOT

# ─── helpers ─────────────────────────────────────────────────────────────────

run_android() {
  local VARIANTS="$1"
  echo ""
  echo "══════════════════════════════════════"
  echo " Android  ›  ${VARIANTS}"
  echo "══════════════════════════════════════"
  bash "${SCRIPT_DIR}/build-android-apks.sh" "${VARIANTS}"
}

run_ios() {
  local VARIANTS="$1"
  echo ""
  echo "══════════════════════════════════════"
  echo " iOS  ›  ${VARIANTS}"
  echo "══════════════════════════════════════"
  bash "${SCRIPT_DIR}/build-ios-app.sh" "${VARIANTS}"
}

# ─── dispatch ────────────────────────────────────────────────────────────────

case "$PLATFORM" in
  android)
    run_android "$VARIANT_ARG"
    ;;
  ios)
    run_ios "$VARIANT_ARG"
    ;;
  all)
    run_android "$VARIANT_ARG"
    run_ios     "$VARIANT_ARG"
    ;;
  *)
    echo "❌ Unknown platform: ${PLATFORM}"
    echo "   Valid platforms: android | ios | all"
    exit 1
    ;;
esac

echo ""
echo "✅ build-all.sh complete."
echo "📦 Output root: ${BUILD_OUTPUT_ROOT}"
