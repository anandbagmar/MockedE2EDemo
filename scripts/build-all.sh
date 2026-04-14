#!/usr/bin/env bash
# build-all.sh – Master build script for App Automation Playground (Android + iOS).
#
# Usage:
#   scripts/build-all.sh [platform] [variant]
#
#   platform  : android | ios | all         (default: all)
#   variant   : debug | release | debug-nml | release-nml | all  (default: all)
#
# Examples:
#   scripts/build-all.sh                        # build everything
#   scripts/build-all.sh android debug          # Android debug only
#   scripts/build-all.sh ios release            # iOS release only
#   scripts/build-all.sh android debug-nml      # Android debug + NML
#   scripts/build-all.sh all all                # same as no args
#
# Environment variables for NML builds:
#   NML_JAR             Path to Applitools NML setup jar
#   APPLITOOLS_API_KEY  Applitools API key

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

PLATFORM="${1:-all}"
VARIANT="${2:-all}"

run_android() {
  local VARIANT="$1"
  echo ""
  echo "══════════════════════════════════════"
  echo " Android  ›  ${VARIANT}"
  echo "══════════════════════════════════════"
  bash "${SCRIPT_DIR}/build-android-apks.sh" "${VARIANT}"
}

run_ios() {
  local VARIANT="$1"
  echo ""
  echo "══════════════════════════════════════"
  echo " iOS  ›  ${VARIANT}"
  echo "══════════════════════════════════════"
  bash "${SCRIPT_DIR}/build-ios-app.sh" "${VARIANT}"
}

VALID_VARIANTS=(debug release debug-nml release-nml all)

is_valid_variant() {
  local v="$1"
  for vv in "${VALID_VARIANTS[@]}"; do [[ "$vv" == "$v" ]] && return 0; done
  return 1
}

if ! is_valid_variant "$VARIANT"; then
  echo "❌ Unknown variant: ${VARIANT}"
  echo "   Valid variants: ${VALID_VARIANTS[*]}"
  exit 1
fi

case "$PLATFORM" in
  android)
    run_android "$VARIANT"
    ;;
  ios)
    run_ios "$VARIANT"
    ;;
  all)
    if [[ "$VARIANT" == "all" ]]; then
      run_android all
      run_ios     all
    else
      run_android "$VARIANT"
      run_ios     "$VARIANT"
    fi
    ;;
  *)
    echo "❌ Unknown platform: ${PLATFORM}"
    echo "   Valid platforms: android | ios | all"
    exit 1
    ;;
esac

echo ""
echo "✅ build-all.sh complete."
