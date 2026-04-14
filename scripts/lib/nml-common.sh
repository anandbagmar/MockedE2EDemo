#!/usr/bin/env bash
# scripts/lib/nml-common.sh
#
# Shared helpers for Applitools NML (applitoolsify) download and instrumentation.
# Source this file — do NOT execute it directly.
#
# Public API:
#   ensure_applitoolsify PLATFORM        → downloads binary if absent; sets APPLITOOLSIFY_BIN
#   apply_nml_android    INPUT  OUTPUT   → Android: applitoolsify writes a new file to dist/
#   apply_nml_ios        INPUT  OUTPUT   → iOS:     applitoolsify overwrites the file in-place
#
# PLATFORM: "android" | "ios"
# Binaries are cached in <project-root>/libs/
#
# applitoolsify output behaviour (per Applitools docs):
#   Android – creates a NEW instrumented file in the SAME directory as the input APK.
#             The tool appends "-nml" before the extension, e.g.
#               input:  dist/App Automation Playground-debug.apk
#               output: dist/App Automation Playground-debug-nml.apk   ← created by the tool
#   iOS     – OVERWRITES the supplied file in-place.
#               input:  dist/App Automation Playground-debug-nml.app.zip  (pre-copied)
#               output: same file, now instrumented

# ── URL bases ─────────────────────────────────────────────────────────────────
readonly NML_ANDROID_BASE="https://sdksstorage.blob.core.windows.net/mobile/android/nml/release"
readonly NML_IOS_BASE="https://sdksstorage.blob.core.windows.net/mobile/ios/nml/applitoolsify/release"

# ── Architecture detection ────────────────────────────────────────────────────

_nml_os() {
  case "$(uname -s)" in
    Darwin) echo "macos" ;;
    Linux)  echo "Linux" ;;
    *)      fail "Unsupported OS: $(uname -s)" ;;
  esac
}

_nml_arch() {
  case "$(uname -m)" in
    arm64|aarch64) echo "arm64"  ;;
    x86_64)        echo "x86_64" ;;
    *)             fail "Unsupported architecture: $(uname -m)" ;;
  esac
}

_nml_binary_name() {
  local PLATFORM="$1"
  echo "applitoolsify-${PLATFORM}-$(_nml_os)-$(_nml_arch)"
}

_nml_download_url() {
  local PLATFORM="$1"
  local BINARY_NAME="$2"
  if [[ "$PLATFORM" == "android" ]]; then
    echo "${NML_ANDROID_BASE}/${BINARY_NAME}"
  else
    echo "${NML_IOS_BASE}/${BINARY_NAME}"
  fi
}

# ── Download + permission setup ───────────────────────────────────────────────

ensure_applitoolsify() {
  local PLATFORM="$1"
  local BINARY_NAME
  BINARY_NAME="$(_nml_binary_name "$PLATFORM")"

  local LIBS_DIR="${PROJECT_ROOT}/libs"
  local DEST="${LIBS_DIR}/${BINARY_NAME}"

  step "Ensuring applitoolsify binary (${PLATFORM})"
  info "Binary : ${BINARY_NAME}"
  info "OS/arch: $(uname -s) / $(uname -m)"

  mkdir -p "$LIBS_DIR"

  if [[ -f "$DEST" ]]; then
    info "Already cached — skipping download"
    info "Path   : ${DEST}"
    ok "applitoolsify ready (cached)"
  else
    local URL
    URL="$(_nml_download_url "$PLATFORM" "$BINARY_NAME")"

    step "Downloading applitoolsify"
    info "URL  : ${URL}"
    info "Dest : ${DEST}"

    if ! curl -fsSL --retry 3 --retry-delay 2 -o "$DEST" "$URL"; then
      fail "Download failed. Check internet connection or URL above."
    fi

    step "Setting binary permissions"
    chmod +x "$DEST"
    info "chmod +x applied"

    if [[ "$(uname -s)" == "Darwin" ]]; then
      step "Removing macOS Gatekeeper quarantine (xattr)"
      xattr -d com.apple.quarantine "$DEST" 2>/dev/null && info "xattr -d com.apple.quarantine: done" || info "xattr -d: attribute not present (safe)"
      xattr -c "$DEST" 2>/dev/null && info "xattr -c (clear all): done" || info "xattr -c: nothing to clear"
      # xattr -c can strip the execute bit on some macOS versions — restore it
      chmod +x "$DEST"
      info "chmod +x re-applied after xattr"
    fi

    ok "applitoolsify downloaded and ready"
  fi

  APPLITOOLSIFY_BIN="$DEST"
  export APPLITOOLSIFY_BIN
}

# ── Instrumentation helpers ───────────────────────────────────────────────────

_assert_bin() {
  if [[ -z "${APPLITOOLSIFY_BIN:-}" || ! -f "${APPLITOOLSIFY_BIN}" ]]; then
    fail "APPLITOOLSIFY_BIN is not set or file missing. Call ensure_applitoolsify first."
  fi
}

apply_nml_android() {
  local INPUT="$1"
  local OUTPUT="$2"
  _assert_bin

  step "Applying NML instrumentation (Android)"
  info "Binary : ${APPLITOOLSIFY_BIN}"
  info "Input  : ${INPUT}"
  info "Output : ${OUTPUT}"

  [[ -f "$INPUT" ]] || fail "Input APK not found: ${INPUT}"

  "$APPLITOOLSIFY_BIN" "$INPUT"

  local INPUT_DIR INPUT_BASE STEM AUTO_OUT
  INPUT_DIR="$(dirname "$INPUT")"
  INPUT_BASE="$(basename "$INPUT")"
  STEM="${INPUT_BASE%.apk}"
  AUTO_OUT="${INPUT_DIR}/${STEM}-nml.apk"

  if [[ ! -f "$AUTO_OUT" ]]; then
    info "Expected NML output not found: ${AUTO_OUT}"
    info "Contents of $(dirname "$INPUT"):"
    ls -lh "$INPUT_DIR" >&2 || true
    fail "applitoolsify did not produce the expected output file."
  fi

  if [[ "$AUTO_OUT" != "$OUTPUT" ]]; then
    mv -f "$AUTO_OUT" "$OUTPUT"
    info "Renamed: $(basename "$AUTO_OUT") → $(basename "$OUTPUT")"
  fi

  ok "NML-instrumented APK ready: $(basename "$OUTPUT")"
}

apply_nml_ios() {
  local INPUT="$1"
  local OUTPUT="$2"
  _assert_bin

  step "Applying NML instrumentation (iOS)"
  info "Binary : ${APPLITOOLSIFY_BIN}"
  info "Input  : ${INPUT}"
  info "Output : ${OUTPUT}"

  [[ -f "$INPUT" ]] || fail "Input app not found: ${INPUT}"

  step "Copying base app to NML output path"
  cp -f "$INPUT" "$OUTPUT"
  info "Copied: $(basename "$INPUT") → $(basename "$OUTPUT")"

  step "Running applitoolsify (overwrites in-place)"
  "$APPLITOOLSIFY_BIN" "$OUTPUT"

  ok "NML-instrumented app ready: $(basename "$OUTPUT")"
}
