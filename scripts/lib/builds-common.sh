#!/usr/bin/env bash
# scripts/lib/builds-common.sh
#
# Shared helpers for timestamped builds/ output directories.

builds_timestamp_root() {
  if [[ -n "${BUILD_TIMESTAMP_ROOT:-}" ]]; then
    echo "${BUILD_TIMESTAMP_ROOT}"
    return 0
  fi

  local ROOT
  ROOT="$(date "+%b-%Y/%d-%b-%Y/%H-%M")"
  export BUILD_TIMESTAMP_ROOT="$ROOT"
  echo "$ROOT"
}

ensure_builds_root() {
  local ROOT="${PROJECT_ROOT}/builds/$(builds_timestamp_root)"

  if [[ "${BUILD_ROOT_INITIALIZED:-0}" -eq 0 ]]; then
    mkdir -p "$ROOT"
    ln -sfn "$ROOT" "${PROJECT_ROOT}/builds/latest"
    export BUILD_ROOT_INITIALIZED=1
  fi

  echo "$ROOT"
}

builds_platform_dir() {
  local PLATFORM="$1"
  local ROOT
  ROOT="$(ensure_builds_root)"
  local PLATFORM_DIR="${ROOT}/${PLATFORM}"
  local PLATFORM_FLAG
  local PLATFORM_UPPER

  PLATFORM_UPPER="$(printf '%s' "$PLATFORM" | tr '[:lower:]' '[:upper:]')"
  PLATFORM_FLAG="BUILD_${PLATFORM_UPPER}_DIR_INITIALIZED"

  if [[ "${!PLATFORM_FLAG:-0}" -eq 0 ]]; then
    mkdir -p "$PLATFORM_DIR"
    ln -sfn "$PLATFORM_DIR" "${PROJECT_ROOT}/builds/latest-${PLATFORM}"
    export "${PLATFORM_FLAG}=1"
  fi

  echo "$PLATFORM_DIR"
}
