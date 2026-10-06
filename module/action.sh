#!/system/bin/sh
# ==============================================================================
# Alluka Action • Launch Alluka Manager from Root Manager UI
# Maintainer: Alluka (@Alluka_id)
# ==============================================================================

MODDIR=${0%/*}
PKG="id.alluka.manager"
APK="$MODPATH/Alluka.apk"

# Check if app is installed
if pm list packages | grep -q "$PKG"; then
  echo "Meluncurkan Alluka Manager..."
  monkey -p "$PKG" -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1
  exit 0
fi

# Fallback: install APK if not installed
if [ -f "$APK" ]; then
  echo "Menginstal Alluka Manager..."
  pm install -r "$APK" >/dev/null 2>&1
  monkey -p "$PKG" -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1
  exit 0
fi

echo "Alluka Manager APK tidak ditemukan di $APK"
