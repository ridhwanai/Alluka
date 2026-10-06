#!/system/bin/sh
# ==============================================================================
# Alluka Service • Boot Trigger & Daemon Launcher
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
sleep 4

# Ensure config directory exists
mkdir -p "$CONFIG_DIR"
[ -s "$PROFILE_FILE" ] || echo "daily" > "$PROFILE_FILE"

# Apply active profile
PROFILE="$(cat "$PROFILE_FILE" 2>/dev/null)"
sh "$MODDIR/apply.sh" "${PROFILE:-daily}"

# Make scripts executable
chmod 0755 "$MODDIR/apply.sh" 2>/dev/null
chmod 0755 "$MODDIR/alluka_daemon.sh" 2>/dev/null

# Launch Alluka background daemon
killall alluka_daemon 2>/dev/null || true
pkill -f alluka_daemon.sh 2>/dev/null || true
nohup sh "$MODDIR/alluka_daemon.sh" >/dev/null 2>&1 &
