#!/system/bin/sh
# ==============================================================================
# Alluka Service • Boot Trigger
# Maintainer: Alluka (@Alluka_id)
# ==============================================================================

MODDIR=${0%/*}
CONFIG_DIR=/data/adb/.config/alluka
PROFILE_FILE="$CONFIG_DIR/profile"

# Wait for boot completion
until [ "$(getprop sys.boot_completed)" = "1" ]; do
  sleep 2
done

# Delay slightly to allow system services to settle
sleep 5

# Ensure config directory exists
mkdir -p "$CONFIG_DIR"
[ -s "$PROFILE_FILE" ] || echo "daily" > "$PROFILE_FILE"

# Apply active profile
PROFILE="$(cat "$PROFILE_FILE" 2>/dev/null)"
sh "$MODDIR/apply.sh" "${PROFILE:-daily}"
