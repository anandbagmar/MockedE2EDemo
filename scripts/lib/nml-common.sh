#!/usr/bin/env bash
# scripts/lib/nml-common.sh
#
# Shared helpers for Applitools NML (applitoolsify) download and instrumentation.
# Source this file — do NOT execute it directly.
#
# Public API:
#   ensure_applitoolsify PLATFORM        → downloads binary if absent; sets APPLITOOLSIFY_BIN
#   apply_nml_android    INPUT  OUTPUT [SUFFIX]
#                                         → Android: stage a renamed -nml input copy,
#                                           optionally add a filename suffix such as "-r",
#                                           let applitoolsify emit into legacy dist/,
#                                           then copy the result to the final output path
#   apply_nml_ios        INPUT  OUTPUT   → iOS:     applitoolsify overwrites the supplied file
#                                           in-place; no Android-style legacy path handling
#
# PLATFORM: "android" | "ios"
# Binaries are cached in <project-root>/libs/
#
# applitoolsify output behaviour:
#   Android – instrument a staged renamed copy (for example debug-nml.apk or
#             debug-nml-r.apk). applitoolsify is expected to emit the instrumented
#             APK into legacy dist/ using the staged input basename, after which we
#             copy it to the final builds/ location.
#               input:        builds/App Automation Playground-debug.apk
#               staged input: builds/App Automation Playground-debug-nml.apk
#               dist output:  dist/App Automation Playground-debug-nml.apk
#               final output: builds/App Automation Playground-debug-nml.apk
#               staged input: builds/App Automation Playground-debug-nml-r.apk
#               dist output:  dist/App Automation Playground-debug-nml-r.apk
#               final output: builds/App Automation Playground-debug-nml-r.apk
#   iOS     – accepts a real .app bundle (or .ipa), not a .zip. We therefore unpack the
#             base archive, rename the bundle to the desired -nml name, instrument it
#             in-place, and then re-zip it to the requested output path.
#               input:  builds/App Automation Playground-debug.app.zip
#               output: builds/App Automation Playground-debug-nml.app.zip

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

_nml_meta_file() {
  local DEST="$1"
  echo "${DEST}.metadata"
}

_nml_fetch_remote_metadata() {
  local URL="$1"
  local HEADERS_FILE="$2"

  curl -fsSLI --retry 3 --retry-delay 2 -o "$HEADERS_FILE" "$URL"
}

_nml_read_header_value() {
  local HEADERS_FILE="$1"
  local HEADER_NAME="$2"

  awk -F': ' -v name="$HEADER_NAME" '
    BEGIN { IGNORECASE = 1 }
    $1 == name {
      value = $2
      sub(/\r$/, "", value)
      print value
      exit
    }
  ' "$HEADERS_FILE"
}

_nml_write_metadata() {
  local META_FILE="$1"
  local ETAG="$2"
  local LAST_MODIFIED="$3"
  local CONTENT_LENGTH="$4"

  {
    printf 'ETAG=%q\n' "$ETAG"
    printf 'LAST_MODIFIED=%q\n' "$LAST_MODIFIED"
    printf 'CONTENT_LENGTH=%q\n' "$CONTENT_LENGTH"
  } > "$META_FILE"
}

_nml_metadata_matches() {
  local DEST="$1"
  local META_FILE="$2"
  local REMOTE_ETAG="$3"
  local REMOTE_LAST_MODIFIED="$4"
  local REMOTE_CONTENT_LENGTH="$5"

  [[ -f "$DEST" && -f "$META_FILE" ]] || return 1

  # shellcheck disable=SC1090
  source "$META_FILE"

  local LOCAL_SIZE
  LOCAL_SIZE="$(wc -c < "$DEST" | tr -d '[:space:]')"

  if [[ -n "${REMOTE_ETAG}" && -n "${ETAG:-}" && "$REMOTE_ETAG" == "$ETAG" ]]; then
    return 0
  fi

  if [[ -n "${REMOTE_LAST_MODIFIED}" && -n "${REMOTE_CONTENT_LENGTH}" ]] \
     && [[ -n "${LAST_MODIFIED:-}" && -n "${CONTENT_LENGTH:-}" ]] \
     && [[ "$REMOTE_LAST_MODIFIED" == "$LAST_MODIFIED" ]] \
     && [[ "$REMOTE_CONTENT_LENGTH" == "$CONTENT_LENGTH" ]] \
     && [[ "$REMOTE_CONTENT_LENGTH" == "$LOCAL_SIZE" ]]; then
    return 0
  fi

  return 1
}

ensure_applitoolsify() {
  local PLATFORM="$1"
  local BINARY_NAME
  BINARY_NAME="$(_nml_binary_name "$PLATFORM")"

  local LIBS_DIR="${PROJECT_ROOT}/libs"
  local DEST="${LIBS_DIR}/${BINARY_NAME}"
  local META_FILE
  META_FILE="$(_nml_meta_file "$DEST")"

  step "Ensuring applitoolsify binary (${PLATFORM})"
  info "Binary : ${BINARY_NAME}"
  info "OS/arch: $(uname -s) / $(uname -m)"

  mkdir -p "$LIBS_DIR"

  local URL
  URL="$(_nml_download_url "$PLATFORM" "$BINARY_NAME")"

  local HEADERS_FILE
  HEADERS_FILE="$(mktemp "${TMPDIR:-/tmp}/applitoolsify-headers-XXXXXX")"

  local REMOTE_METADATA_OK=0
  local REMOTE_ETAG=""
  local REMOTE_LAST_MODIFIED=""
  local REMOTE_CONTENT_LENGTH=""

  if _nml_fetch_remote_metadata "$URL" "$HEADERS_FILE"; then
    REMOTE_METADATA_OK=1
    REMOTE_ETAG="$(_nml_read_header_value "$HEADERS_FILE" "ETag")"
    REMOTE_LAST_MODIFIED="$(_nml_read_header_value "$HEADERS_FILE" "Last-Modified")"
    REMOTE_CONTENT_LENGTH="$(_nml_read_header_value "$HEADERS_FILE" "Content-Length")"
    info "Remote metadata fetched successfully"
  else
    info "Could not fetch remote metadata — will rely on cached binary if present"
  fi

  if [[ -f "$DEST" ]]; then
    if [[ "$REMOTE_METADATA_OK" -eq 1 ]] \
      && _nml_metadata_matches "$DEST" "$META_FILE" "$REMOTE_ETAG" "$REMOTE_LAST_MODIFIED" "$REMOTE_CONTENT_LENGTH"; then
      info "Cached binary matches remote metadata — skipping download"
      info "Path   : ${DEST}"
      rm -f "$HEADERS_FILE"
      ok "applitoolsify ready (cached)"
      APPLITOOLSIFY_BIN="$DEST"
      export APPLITOOLSIFY_BIN
      return 0
    fi

    if [[ "$REMOTE_METADATA_OK" -eq 0 ]]; then
      info "Using cached binary without freshness verification"
      info "Path   : ${DEST}"
      rm -f "$HEADERS_FILE"
      ok "applitoolsify ready (cached)"
      APPLITOOLSIFY_BIN="$DEST"
      export APPLITOOLSIFY_BIN
      return 0
    fi

    info "Cached binary differs from remote metadata — re-downloading"
  else
    step "Downloading applitoolsify"
    info "URL  : ${URL}"
    info "Dest : ${DEST}"
  fi

  step "Downloading applitoolsify"
  info "URL  : ${URL}"
  info "Dest : ${DEST}"

  if ! curl -fsSL --retry 3 --retry-delay 2 -o "$DEST" "$URL"; then
    rm -f "$HEADERS_FILE"
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

  _nml_write_metadata "$META_FILE" "$REMOTE_ETAG" "$REMOTE_LAST_MODIFIED" "$REMOTE_CONTENT_LENGTH"
  rm -f "$HEADERS_FILE"
  ok "applitoolsify downloaded and ready"

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
  local OUTPUT_SUFFIX="${3:-}"
  _assert_bin

  step "Applying NML instrumentation (Android)"
  info "Binary : ${APPLITOOLSIFY_BIN}"
  info "Input  : ${INPUT}"
  info "Output : ${OUTPUT}"

  [[ -f "$INPUT" ]] || fail "Input APK not found: ${INPUT}"

  local INPUT_DIR INPUT_BASE OUTPUT_BASE STAGED_DIR STAGED_INPUT
  INPUT_DIR="$(dirname "$INPUT")"
  INPUT_BASE="$(basename "$INPUT")"
  OUTPUT_BASE="$(basename "$OUTPUT")"
  if [[ -n "$OUTPUT_SUFFIX" ]]; then
    OUTPUT_BASE="${OUTPUT_BASE%.apk}${OUTPUT_SUFFIX}.apk"
  fi
  STAGED_DIR="$(mktemp -d "${TMPDIR:-/tmp}/applitoolsify-android-stage-XXXXXX")"
  STAGED_INPUT="${STAGED_DIR}/${OUTPUT_BASE}"

  local LEGACY_DIST_DIR="${PROJECT_ROOT}/dist"
  local LEGACY_DIST_OUTPUT="${LEGACY_DIST_DIR}/${OUTPUT_BASE}"
  local LEGACY_LOG_FILE="${LEGACY_DIST_DIR}/android-instrumentation.log"
  local OUTPUT_DIR
  OUTPUT_DIR="$(dirname "$OUTPUT")"
  local OUTPUT_LOG_FILE="${OUTPUT_DIR}/android-instrumentation.log"

  step "Creating staged Android APK for NML instrumentation"
  cp -f "$INPUT" "$STAGED_INPUT"
  info "Copied: ${INPUT_BASE} → ${OUTPUT_BASE}"
  info "Stage : ${STAGED_INPUT}"

  rm -f "$LEGACY_DIST_OUTPUT"

  if [[ "$OUTPUT_SUFFIX" == "-r" ]]; then
    info "Args   : -r"
    "$APPLITOOLSIFY_BIN" -r "$STAGED_INPUT"
  else
    "$APPLITOOLSIFY_BIN" "$STAGED_INPUT"
  fi

  local ACTUAL_OUT=""
  if [[ -f "$LEGACY_DIST_OUTPUT" ]]; then
    ACTUAL_OUT="$LEGACY_DIST_OUTPUT"
  fi

  if [[ -z "$ACTUAL_OUT" ]]; then
    info "Expected NML output not found in known locations."
    info "Checked:"
    info "  - ${LEGACY_DIST_OUTPUT}"
    info "Contents of ${INPUT_DIR}:"
    ls -lh "$INPUT_DIR" >&2 || true
    if [[ -d "$LEGACY_DIST_DIR" ]]; then
      info "Contents of ${LEGACY_DIST_DIR}:"
      ls -lh "$LEGACY_DIST_DIR" >&2 || true
    fi
    rm -rf "$STAGED_DIR"
    fail "applitoolsify did not produce the expected output file."
  fi

  if [[ "$ACTUAL_OUT" != "$OUTPUT" ]]; then
    cp -f "$ACTUAL_OUT" "$OUTPUT"
    info "Copied: $(basename "$ACTUAL_OUT") → $(basename "$OUTPUT")"
  fi

  if [[ -f "$LEGACY_LOG_FILE" ]]; then
    cp -f "$LEGACY_LOG_FILE" "$OUTPUT_LOG_FILE"
    info "Copied instrumentation log: ${LEGACY_LOG_FILE} → ${OUTPUT_LOG_FILE}"
  fi

  rm -rf "$STAGED_DIR"

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

  local WORK_DIR
  WORK_DIR="$(mktemp -d "${TMPDIR:-/tmp}/applitoolsify-ios-XXXXXX")"
  local OUTPUT_DIR
  OUTPUT_DIR="$(dirname "$OUTPUT")"
  local OUTPUT_BASENAME
  OUTPUT_BASENAME="$(basename "$OUTPUT")"
  local OUTPUT_APP_BASENAME
  OUTPUT_APP_BASENAME="${OUTPUT_BASENAME%.zip}"

  step "Unpacking base app archive for iOS instrumentation"
  info "Temp dir: ${WORK_DIR}"
  unzip -q "$INPUT" -d "$WORK_DIR"

  local SOURCE_APP
  SOURCE_APP="$(find "$WORK_DIR" -maxdepth 1 -type d -name "*.app" | head -n 1)"
  if [[ -z "$SOURCE_APP" ]]; then
    rm -rf "$WORK_DIR"
    fail "No .app bundle found after extracting: ${INPUT}"
  fi

  local INSTRUMENT_TARGET="${WORK_DIR}/${OUTPUT_APP_BASENAME}"

  step "Preparing renamed .app bundle for NML output"
  mv "$SOURCE_APP" "$INSTRUMENT_TARGET"
  info "Prepared: $(basename "$SOURCE_APP") → $(basename "$INSTRUMENT_TARGET")"

  step "Running applitoolsify for iOS (overwrites in-place)"
  "$APPLITOOLSIFY_BIN" "$INSTRUMENT_TARGET"

  if [[ ! -d "$INSTRUMENT_TARGET" ]]; then
    rm -rf "$WORK_DIR"
    fail "Expected instrumented iOS app bundle not found after applitoolsify: ${INSTRUMENT_TARGET}"
  fi

  step "Repacking instrumented iOS app"
  rm -f "$OUTPUT"
  pushd "$WORK_DIR" >/dev/null
  zip -qr "$OUTPUT" "$OUTPUT_APP_BASENAME"
  popd >/dev/null

  rm -rf "$WORK_DIR"

  if [[ ! -f "$OUTPUT" ]]; then
    fail "Expected instrumented iOS app archive not found after repacking: ${OUTPUT}"
  fi

  ok "NML-instrumented app ready: $(basename "$OUTPUT")"
}
