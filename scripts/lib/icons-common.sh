#!/usr/bin/env bash
# scripts/lib/icons-common.sh
#
# Auto-generates app icons when a source image is present and icons are stale.
# Source this file — do NOT execute it directly.
#
# Provides:
#   ensure_icons   → runs generate-icons.sh if needed; no-op otherwise
#
# Trigger logic:
#   • If assets/images/app-icon-source.png does NOT exist → skip silently.
#   • If .icon-generated marker does NOT exist            → generate icons.
#   • If source PNG is NEWER than the marker              → regenerate icons.
#   • Otherwise icons are up-to-date → skip.
#
# The marker file (.icon-generated) lives at the project root and is listed
# in .gitignore so it is never committed.

ensure_icons() {
  local SOURCE="${PROJECT_ROOT}/assets/images/app-icon-source.png"
  local MARKER="${PROJECT_ROOT}/.icon-generated"
  local GENERATE_SCRIPT="${PROJECT_ROOT}/scripts/generate-icons.sh"

  # No source icon provided — nothing to do
  if [[ ! -f "$SOURCE" ]]; then
    return 0
  fi

  local NEEDS_REGEN=false

  if [[ ! -f "$MARKER" ]]; then
    echo "==> Icon marker not found — icons have not been generated yet."
    NEEDS_REGEN=true
  elif [[ "$SOURCE" -nt "$MARKER" ]]; then
    echo "==> Icon source is newer than generated icons — regenerating."
    NEEDS_REGEN=true
  else
    echo "==> App icons are up-to-date (source unchanged)."
  fi

  if [[ "$NEEDS_REGEN" == "true" ]]; then
    echo "==> Running generate-icons.sh …"
    bash "$GENERATE_SCRIPT" "$SOURCE"
    touch "$MARKER"
    echo "✅ Icons generated and marker updated."
  fi
}
