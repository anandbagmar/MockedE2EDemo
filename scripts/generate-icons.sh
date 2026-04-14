#!/usr/bin/env bash
# generate-icons.sh – generate Android and iOS app icons from a single source image.
#
# Prerequisites:
#   macOS: uses built-in `sips` (no extra install needed)
#   Linux: uses `convert` from ImageMagick  (brew/apt install imagemagick)
#
# Usage:
#   ./scripts/generate-icons.sh                            # uses default source
#   ./scripts/generate-icons.sh path/to/icon.png          # custom source (must be ≥1024x1024)
#
# Place your source icon (square, ≥1024×1024, transparent bg OK) at:
#   assets/images/app-icon-source.png
# then run this script.

set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT"

SOURCE="${1:-assets/images/app-icon-source.png}"

if [[ ! -f "$SOURCE" ]]; then
  echo "❌ Source icon not found: $SOURCE"
  echo "   Please place a square PNG (≥1024×1024) at assets/images/app-icon-source.png"
  exit 1
fi

# ─── resize helper ───────────────────────────────────────────────────────────
resize() {
  local SIZE="$1"
  local DEST="$2"
  mkdir -p "$(dirname "$DEST")"
  if command -v sips &>/dev/null; then
    sips -z "$SIZE" "$SIZE" "$SOURCE" --out "$DEST" >/dev/null
  elif command -v convert &>/dev/null; then
    convert "$SOURCE" -resize "${SIZE}x${SIZE}" "$DEST"
  else
    echo "❌ Neither sips nor ImageMagick convert found. Install ImageMagick and retry."
    exit 1
  fi
  echo "  -> ${DEST} (${SIZE}x${SIZE})"
}

echo "==> Generating Android icons …"
ANDROID_RES="android/app/src/main/res"

declare -A ANDROID_SIZES=(
  [mipmap-mdpi]=48
  [mipmap-hdpi]=72
  [mipmap-xhdpi]=96
  [mipmap-xxhdpi]=144
  [mipmap-xxxhdpi]=192
)

for FOLDER in "${!ANDROID_SIZES[@]}"; do
  SIZE="${ANDROID_SIZES[$FOLDER]}"
  resize "$SIZE" "${ANDROID_RES}/${FOLDER}/ic_launcher.png"
  resize "$SIZE" "${ANDROID_RES}/${FOLDER}/ic_launcher_round.png"
done

echo ""
echo "==> Generating iOS icons …"
IOS_ICONSET="ios/MockedE2EDemo/Images.xcassets/AppIcon.appiconset"

# Sizes: <filename>:<px>
declare -A IOS_ICONS=(
  [AppIcon-20@2x.png]=40
  [AppIcon-20@3x.png]=60
  [AppIcon-29@2x.png]=58
  [AppIcon-29@3x.png]=87
  [AppIcon-40@2x.png]=80
  [AppIcon-40@3x.png]=120
  [AppIcon-60@2x.png]=120
  [AppIcon-60@3x.png]=180
  [AppIcon-1024.png]=1024
)

for FILENAME in "${!IOS_ICONS[@]}"; do
  SIZE="${IOS_ICONS[$FILENAME]}"
  resize "$SIZE" "${IOS_ICONSET}/${FILENAME}"
done

# Update Contents.json to reference the generated filenames
cat > "${IOS_ICONSET}/Contents.json" <<'JSON'
{
  "images" : [
    { "filename": "AppIcon-20@2x.png",  "idiom": "iphone", "scale": "2x", "size": "20x20" },
    { "filename": "AppIcon-20@3x.png",  "idiom": "iphone", "scale": "3x", "size": "20x20" },
    { "filename": "AppIcon-29@2x.png",  "idiom": "iphone", "scale": "2x", "size": "29x29" },
    { "filename": "AppIcon-29@3x.png",  "idiom": "iphone", "scale": "3x", "size": "29x29" },
    { "filename": "AppIcon-40@2x.png",  "idiom": "iphone", "scale": "2x", "size": "40x40" },
    { "filename": "AppIcon-40@3x.png",  "idiom": "iphone", "scale": "3x", "size": "40x40" },
    { "filename": "AppIcon-60@2x.png",  "idiom": "iphone", "scale": "2x", "size": "60x60" },
    { "filename": "AppIcon-60@3x.png",  "idiom": "iphone", "scale": "3x", "size": "60x60" },
    { "filename": "AppIcon-1024.png",   "idiom": "ios-marketing", "scale": "1x", "size": "1024x1024" }
  ],
  "info" : { "author": "xcode", "version": 1 }
}
JSON

echo ""
echo "✅ Icons generated successfully!"
echo "   Rebuild the app to see the new icon on your device/simulator."
