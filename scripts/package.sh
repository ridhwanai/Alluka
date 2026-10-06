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
if [ -f "$ROOT_DIR/manager/app/build/outputs/apk/release/app-release-unsigned.apk" ]; then
  cp "$ROOT_DIR/manager/app/build/outputs/apk/release/app-release-unsigned.apk" "$MODULE_DIR/Alluka.apk"
elif [ -f "$ROOT_DIR/manager/app/build/outputs/apk/debug/app-debug.apk" ]; then
  cp "$ROOT_DIR/manager/app/build/outputs/apk/debug/app-debug.apk" "$MODULE_DIR/Alluka.apk"
fi

ZIP_NAME="Alluka-v${VERSION}.zip"
cd "$MODULE_DIR"
rm -f "$DIST_DIR/$ZIP_NAME"
zip -r9 "$DIST_DIR/$ZIP_NAME" . -x "*.orig"

cd "$DIST_DIR"
sha256sum "$ZIP_NAME" > "${ZIP_NAME}.sha256"

echo "==> Build complete: $DIST_DIR/$ZIP_NAME"
