#!/system/bin/sh
# ==============================================================================
# Alluka Engine • Kernel & Profile Tuning
# Maintainer: Alluka (@Alluka_id)
# Repository: https://github.com/ridhwanai/Alluka.git
# Philosophy: Smooth motion, quiet power, safe cluster-aware tuning (Hitori heritage)
# ==============================================================================

MODDIR=${0%/*}
CONFIG_DIR=/data/adb/.config/alluka
PROFILE_FILE="$CONFIG_DIR/profile"
LOG="$CONFIG_DIR/alluka.log"
GED_BASE="$CONFIG_DIR/ged.baseline"

mkdir -p "$CONFIG_DIR"

PROFILE="${1:-$(cat "$PROFILE_FILE" 2>/dev/null)}"
case "$PROFILE" in daily|peforma|sleep) ;; *) PROFILE=daily ;; esac

log() { echo "$(date '+%F %T' 2>/dev/null) $*" >> "$LOG"; }

write_node() {
  local path="$1" value="$2" before after
  [ -e "$path" ] || return 1
  [ -w "$path" ] || { log "SKIP readonly $path"; return 1; }
  before="$(cat "$path" 2>/dev/null)"
  if printf '%s' "$value" > "$path" 2>/dev/null; then
    after="$(cat "$path" 2>/dev/null)"
    log "SET $path: $before -> $after"
    return 0
  fi
  log "FAIL $path value=$value"
  return 1
}

set_governor() {
  local policy="$1" available
  available="$(cat "$policy/scaling_available_governors" 2>/dev/null)"
  case " $available " in *" schedutil "*) write_node "$policy/scaling_governor" schedutil ;; esac
}

set_schedutil() {
  local dir="$1" up="$2" down="$3"
  write_node "$dir/up_rate_limit_us" "$up"
  write_node "$dir/down_rate_limit_us" "$down"
}

set_stune() {
  local group="$1" boost="$2" idle="$3" base
  for base in /dev/stune /sys/fs/cgroup/stune; do
    [ -d "$base/$group" ] || continue
    write_node "$base/$group/schedtune.boost" "$boost"
    write_node "$base/$group/schedtune.prefer_idle" "$idle"
  done
}

snapshot_ged() {
  [ -s "$GED_BASE" ] && return 0
  : > "$GED_BASE"
  for p in \
    /sys/module/ged/parameters/ged_boost_enable \
    /sys/module/ged/parameters/boost_gpu_enable \
    /sys/module/ged/parameters/enable_cpu_boost \
    /sys/module/ged/parameters/enable_gpu_boost; do
    [ -r "$p" ] && printf '%s|%s\n' "$p" "$(cat "$p" 2>/dev/null)" >> "$GED_BASE"
  done
}

restore_ged() {
  [ -s "$GED_BASE" ] || return 0
  while IFS='|' read -r path value; do
    [ -n "$path" ] && write_node "$path" "$value"
  done < "$GED_BASE"
}

update_description() {
  local pretty
  case "$PROFILE" in
    daily) pretty="Daily (Balanced)" ;;
    peforma) pretty="Peforma (Nanika Awakened)" ;;
    sleep) pretty="Sleep (Battery Saver)" ;;
  esac
  sed -i "s|^description=.*|description=Smooth motion, adaptive power. Safe tuning with Alluka Manager. Active profile: $pretty.|" "$MODDIR/module.prop" 2>/dev/null
}

case "$PROFILE" in
  daily)
    # Hitori Balanced: Smooth, responsive, zero jitter
    LITTLE_UP=1000; LITTLE_DOWN=12000; BIG_UP=500; BIG_DOWN=24000
    TOPBOOST=10; FGBOOST=3; SWAP=100; SBOOST=0
    ;;
  peforma)
    # Nanika Awakened: Instant clock ramp, maximum gaming responsiveness
    LITTLE_UP=0; LITTLE_DOWN=24000; BIG_UP=0; BIG_DOWN=42000
    TOPBOOST=18; FGBOOST=5; SWAP=80; SBOOST=1
    ;;
  sleep)
    # Sleep / Eco: Gentle clock transitions, ultra battery saver
    LITTLE_UP=3000; LITTLE_DOWN=6000; BIG_UP=5000; BIG_DOWN=8000
    TOPBOOST=0; FGBOOST=0; SWAP=120; SBOOST=0
    ;;
esac

: > "$LOG"
log "Alluka v1.0 profile=$PROFILE device=$(getprop ro.product.device) android=$(getprop ro.build.version.release) kernel=$(uname -r)"
snapshot_ged

# Cluster-aware cpufreq schedutil tuning
for policy in /sys/devices/system/cpu/cpufreq/policy*; do
  [ -d "$policy" ] || continue
  set_governor "$policy"
  CPUs="$(cat "$policy/affected_cpus" 2>/dev/null)"
  case " $CPUs " in
    *" 6 "*|*" 7 "*) UP="$BIG_UP"; DOWN="$BIG_DOWN" ;;
    *) case "${policy##*policy}" in 6|7) UP="$BIG_UP"; DOWN="$BIG_DOWN" ;; *) UP="$LITTLE_UP"; DOWN="$LITTLE_DOWN" ;; esac ;;
  esac
  [ -d "$policy/schedutil" ] && set_schedutil "$policy/schedutil" "$UP" "$DOWN"
done

# Global schedutil fallback (if exposed)
if [ -d /sys/devices/system/cpu/cpufreq/schedutil ]; then
  set_schedutil /sys/devices/system/cpu/cpufreq/schedutil "$LITTLE_UP" "$LITTLE_DOWN"
fi

# SchedTune foreground boost
set_stune top-app "$TOPBOOST" 1
set_stune foreground "$FGBOOST" 0
set_stune background 0 0

# MediaTek GED boost control
if [ "$SBOOST" = 1 ]; then
  write_node /sys/module/ged/parameters/ged_boost_enable 1
  write_node /sys/module/ged/parameters/boost_gpu_enable 1
  write_node /sys/module/ged/parameters/enable_cpu_boost 1
  write_node /sys/module/ged/parameters/enable_gpu_boost 1
else
  restore_ged
fi

# Swappiness & VM memory tuning
write_node /proc/sys/vm/swappiness "$SWAP"
write_node /proc/sys/vm/vfs_cache_pressure 100
write_node /proc/sys/vm/dirty_ratio 20
write_node /proc/sys/vm/dirty_background_ratio 10

# Storage read-ahead
for q in /sys/block/*/queue/read_ahead_kb; do
  [ -w "$q" ] && write_node "$q" 128
done

# Save profile
echo "$PROFILE" > "$PROFILE_FILE"
update_description

log "Alluka profile $PROFILE applied successfully"
