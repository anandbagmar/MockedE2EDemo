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
    *)      echo "❌ Unsupported OS: $(uname -s)" >&2; return 1 ;;
  esac
}

_nml_arch() {
  case "$(uname -m)" in
    arm64|aarch64) echo "arm64"  ;;
    x86_64)        echo "x86_64" ;;
    *)             echo "❌ Unsupported arch: $(uname -m)" >&2; return 1 ;;
  esac
}

_nml_binary_name() {
  local PLATFORM="$1"   # android | ios
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

# ensure_applitoolsify PLATFORM
#   Downloads the correct applitoolsify binary to libs/ if not already present.
#   Sets (and exports) the global APPLITOOLSIFY_BIN variable.
ensure_applitoolsify() {
  local PLATFORM="$1"
  local BINARY_NAME
  BINARY_NAME="$(_nml_binary_name "$PLATFORM")"

  local LIBS_DIR="${PROJECT_ROOT}/libs"
  mkdir -p "$LIBS_DIR"
  local DEST="${LIBS_DIR}/${BINARY_NAME}"

  if [[ -f "$DEST" ]]; then
    echo "==> applitoolsify cached: ${DEST}"
  else
    local URL
    URL="$(_nml_download_url "$PLATFORM" "$BINARY_NAME")"

    echo "==> Downloading applitoolsify (${PLATFORM}, $(uname -m)) …"
    echo "    URL : ${URL}"
    echo "    Dest: ${DEST}"

    if ! curl -fsSL --retry 3 --retry-delay 2 -o "$DEST" "$URL"; then
      echo "❌ Download failed. Check your internet connection or the URL above."
      rm -f "$DEST"
      exit 1
    fi

    echo "==> Setting permissions …"
    chmod +x "$DEST"

    # macOS Gatekeeper: remove quarantine and any other extended attributes
    # that would block execution of an unsigned binary downloaded from the internet.
    if [[ "$(uname -s)" == "Darwin" ]]; then
      echo "==> Removing macOS quarantine (xattr) …"
      xattr -d com.apple.quarantine "$DEST" 2>/dev/null || true
      xattr -c "$DEST" 2>/dev/null || true
      # xattr -c can strip the execute bit on some macOS versions — restore it
      chmod +x "$DEST"
    fi

    echo "✅ applitoolsify ready: ${DEST}"
  fi

  APPLITOOLSIFY_BIN="$DEST"
  export APPLITOOLSIFY_BIN
}

# ── Instrumentation helpers ───────────────────────────────────────────────────

_assert_bin() {
  if [[ -z "${APPLITOOLSIFY_BIN:-}" || ! -f "${APPLITOOLSIFY_BIN}" ]]; then
    echo "❌ APPLITOOLSIFY_BIN is not set or missing. Call ensure_applitoolsify first."
    exit 1
  fi
}

# apply_nml_android INPUT_APK OUTPUT_APK
#
#   Android behaviour: applitoolsify reads INPUT_APK and creates a NEW
#   instrumented file in the same directory, appending "-nml" before ".apk".
#
#   Strategy:
#     1. Run applitoolsify on the input APK (already in dist/).
#     2. The tool creates dist/<name>-nml.apk automatically.
#     3. If the auto-generated path differs from OUTPUT_APK, rename it.
apply_nml_android() {
  local INPUT="$1"    # e.g. dist/App Automation Playground-debug.apk
  local OUTPUT="$2"   # e.g. dist/App Automation Playground-debug-nml.apk
  _assert_bin

  if [[ ! -f "$INPUT" ]]; then
    echo "❌ Input APK not found: $INPUT"
    exit 1
  fi

  echo "==> Instrumenting (Android) with applitoolsify …"
  echo "    Binary : ${APPLITOOLSIFY_BIN}"
  echo "    Input  : ${INPUT}"

  "$APPLITOOLSIFY_BIN" "$INPUT"

  # The tool writes to: <dir>/<stem>-nml.<ext>
  local INPUT_DIR INPUT_BASE STEM AUTO_OUT
  INPUT_DIR="$(dirname "$INPUT")"
  INPUT_BASE="$(basename "$INPUT")"
  STEM="${INPUT_BASE%.apk}"
  AUTO_OUT="${INPUT_DIR}/${STEM}-nml.apk"

  if [[ ! -f "$AUTO_OUT" ]]; then
    echo "❌ applitoolsify did not produce expected output: ${AUTO_OUT}"
    echo "   Contents of $(dirname "$INPUT"):"
    ls -lh "$INPUT_DIR" || true
    exit 1
  fi

  if [[ "$AUTO_OUT" != "$OUTPUT" ]]; then
    mv -f "$AUTO_OUT" "$OUTPUT"
    echo "==> Renamed: $(basename "$AUTO_OUT") → $(basename "$OUTPUT")"
  fi

  echo "✅ NML-instrumented (Android): ${OUTPUT}"
}

# apply_nml_ios INPUT_ZIP OUTPUT_ZIP
#
#   iOS behaviour: applitoolsify overwrites the supplied file in-place.
#
#   Strategy:
#     1. Copy INPUT_ZIP → OUTPUT_ZIP.
#     2. Run applitoolsify on OUTPUT_ZIP; it instruments and overwrites the copy.
apply_nml_ios() {
  local INPUT="$1"    # e.g. dist/App Automation Playground-debug.app.zip
  local OUTPUT="$2"   # e.g. dist/App Automation Playground-debug-nml.app.zip
  _assert_bin

  if [[ ! -f "$INPUT" ]]; then
    echo "❌ Input app not found: $INPUT"
    exit 1
  fi

  echo "==> Copying $(basename "$INPUT") → $(basename "$OUTPUT")"
  cp -f "$INPUT" "$OUTPUT"

  echo "==> Instrumenting (iOS) with applitoolsify (overwrites in-place) …"
  echo "    Binary : ${APPLITOOLSIFY_BIN}"
  echo "    Target : ${OUTPUT}"

  "$APPLITOOLSIFY_BIN" "$OUTPUT"

  echo "✅ NML-instrumented (iOS): ${OUTPUT}"
}
