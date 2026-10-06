#!/bin/bash
# ==============================================================================
# Alluka Packaging Script
# ==============================================================================
set -e

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
DIST_DIR="$ROOT_DIR/dist"
MODULE_DIR="$ROOT_DIR/module"
VERSION="1.0"

mkdir -p "$DIST_DIR"

echo "==> Packaging Alluka v$VERSION KernelSU / APatch / Magisk Module..."

# Copy manager APK if built
APK_FILE=""
if [ -f "$ROOT_DIR/manager/app/build/outputs/apk/release/app-release-unsigned.apk" ]; then
  APK_FILE="$ROOT_DIR/manager/app/build/outputs/apk/release/app-release-unsigned.apk"
elif [ -f "$ROOT_DIR/manager/app/build/outputs/apk/release/app-release.apk" ]; then
  APK_FILE="$ROOT_DIR/manager/app/build/outputs/apk/release/app-release.apk"
elif [ -f "$ROOT_DIR/manager/app/build/outputs/apk/debug/app-debug.apk" ]; then
  APK_FILE="$ROOT_DIR/manager/app/build/outputs/apk/debug/app-debug.apk"
else
  # Generic search if output location has variant or different naming
  APK_FILE=$(find "$ROOT_DIR/manager/app/build/outputs/apk" -name "*.apk" 2>/dev/null | head -n 1 || true)
fi

if [ -n "$APK_FILE" ] && [ -f "$APK_FILE" ]; then
  echo "==> [✓] Found Alluka Manager APK: $APK_FILE ($(du -h "$APK_FILE" | cut -f1))"
  cp -f "$APK_FILE" "$MODULE_DIR/Alluka.apk"
  cp -f "$APK_FILE" "$DIST_DIR/Alluka.apk"
  echo "==> [✓] Successfully included Alluka.apk into module/ and dist/"
else
  echo "==> [!] WARNING: No Manager APK found in manager/app/build/outputs/apk!"
fi

ZIP_NAME="Alluka-v${VERSION}.zip"
cd "$MODULE_DIR"
rm -f "$DIST_DIR/$ZIP_NAME"
zip -r9 "$DIST_DIR/$ZIP_NAME" . -x "*.orig"

cd "$DIST_DIR"
sha256sum "$ZIP_NAME" > "${ZIP_NAME}.sha256"

echo "==> Verifying module zip contents:"
unzip -l "$ZIP_NAME"

echo "==> Build complete: $DIST_DIR/$ZIP_NAME"

