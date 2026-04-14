#!/usr/bin/env bash
# scripts/lib/logging.sh – Shared logging helpers.
# Source this file — do NOT execute it directly.
#
# Functions:
#   banner  TITLE            – top-level section header
#   step    MESSAGE          – major step about to start  (▶)
#   info    MESSAGE          – sub-detail under a step    (  │)
#   ok      MESSAGE          – step completed             (✅)
#   warn    MESSAGE          – non-fatal warning          (⚠️)
#   fail    MESSAGE [code]   – fatal error + exit         (❌)

_ts() { date "+%H:%M:%S"; }

banner() {
  local title="${1}"
  local width=56
  local line
  line="$(printf '━%.0s' $(seq 1 $width))"
  echo ""
  echo "  ${line}"
  printf "  ┃  %-$((width - 4))s  ┃\n" "${title}"
  echo "  ${line}"
  echo ""
}

step() { echo "$(_ts)  ▶  ${*}"; }
info() { echo "              ${*}"; }
ok()   { echo "$(_ts)  ✅ ${*}"; echo ""; }
warn() { echo "$(_ts)  ⚠️  ${*}"; }
fail() { echo "$(_ts)  ❌ ${*}" >&2; exit "${2:-1}"; }
