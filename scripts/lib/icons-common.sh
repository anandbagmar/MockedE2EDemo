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

ensure_icons() {
  local SOURCE="${PROJECT_ROOT}/assets/images/app-icon-source.png"
  local MARKER="${PROJECT_ROOT}/.icon-generated"
  local GENERATE_SCRIPT="${PROJECT_ROOT}/scripts/generate-icons.sh"

  step "Checking app icons"
  info "Source : ${SOURCE}"
  info "Marker : ${MARKER}"

  if [[ ! -f "$SOURCE" ]]; then
    info "No icon source found — skipping icon generation."
    info "(Place a ≥1024×1024 PNG at assets/images/app-icon-source.png to enable)"
    echo ""
    return 0
  fi

  local NEEDS_REGEN=false

  if [[ ! -f "$MARKER" ]]; then
    info "Marker not found — icons have never been generated."
    NEEDS_REGEN=true
  elif [[ "$SOURCE" -nt "$MARKER" ]]; then
    info "Source image is newer than last generation — will regenerate."
    NEEDS_REGEN=true
  else
    ok "App icons are up-to-date (source unchanged)"
    return 0
  fi

  if [[ "$NEEDS_REGEN" == "true" ]]; then
    step "Generating app icons from source image"
    info "Script : ${GENERATE_SCRIPT}"
    bash "$GENERATE_SCRIPT" "$SOURCE"
    touch "$MARKER"
    ok "Icons generated and marker updated"
  fi
}
