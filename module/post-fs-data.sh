#!/system/bin/sh
# ==============================================================================
# Alluka Post-FS-Data • Anti-Bootloop Protection
# ==============================================================================

MODDIR=${0%/*}
CONFIG_DIR=/data/adb/.config/alluka
BOOT_MARKER="$CONFIG_DIR/.booting"

mkdir -p "$CONFIG_DIR"

# Anti-bootloop check
if [ -f "$BOOT_MARKER" ]; then
  COUNT=$(cat "$BOOT_MARKER" 2>/dev/null)
  COUNT=$((COUNT + 1))
  if [ "$COUNT" -ge 3 ]; then
    # Safe fallback: disable module if device rebooted 3 times before completing boot
    touch "$MODDIR/disable"
    rm -f "$BOOT_MARKER"
    exit 0
  fi
  echo "$COUNT" > "$BOOT_MARKER"
else
  echo "1" > "$BOOT_MARKER"
fi
