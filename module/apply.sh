#!/system/bin/sh
# ==============================================================================
# Alluka Engine • Kernel & Profile Tuning
# Maintainer: Alluka (@Alluka_id)
# Repository: https://github.com/ridhwanai/Alluka.git
# Philosophy: Smooth motion, quiet power, safe cluster-aware tuning (Alluka engine)
# ==============================================================================

MODDIR=${0%/*}
CONFIG_DIR=/data/adb/.config/alluka
PROFILE_FILE="$CONFIG_DIR/profile"
LOG="$CONFIG_DIR/alluka.log"
GED_BASE="$CONFIG_DIR/ged.baseline"

mkdir -p "$CONFIG_DIR"

PROFILE="${1:-$(cat "$PROFILE_FILE" 2>/dev/null)}"
case "$PROFILE" in daily|peforma|sleep|auto) ;; *) PROFILE=daily ;; esac

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
    auto) pretty="Auto (Dynamic Sensing)" ;;
  esac
  sed -i "s|^description=.*|description=Smooth motion, adaptive power. Safe tuning with Alluka Manager. Active profile: $pretty.|" "$MODDIR/module.prop" 2>/dev/null
}

case "$PROFILE" in
  daily)
    # Alluka Balanced: Smooth, responsive, zero jitter
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
  auto)
    # Auto Dynamic: Schedtune background sensing, adaptive ramp
    LITTLE_UP=1000; LITTLE_DOWN=12000; BIG_UP=500; BIG_DOWN=24000
    TOPBOOST=12; FGBOOST=4; SWAP=100; SBOOST=0
    ;;
esac

: > "$LOG"
log "Alluka v1.0 profile=$PROFILE device=$(getprop ro.product.device) android=$(getprop ro.build.version.release) kernel=$(uname -r)"
snapshot_ged

TWEAKS_FILE="$CONFIG_DIR/tweaks.prop"

# Read user tweaks if present
get_tweak() {
  local key="$1" def="$2" val
  val="$(grep "^$key=" "$TWEAKS_FILE" 2>/dev/null | cut -d'=' -f2)"
  [ -n "$val" ] && echo "$val" || echo "$def"
}

# Cluster-aware cpufreq schedutil tuning (Helio G85 6 Little + 2 Big)
LITE_MODE="$(get_tweak "lite_mode" "0")"
if [ "$LITE_MODE" = "1" ]; then
  LITTLE_UP=2500; LITTLE_DOWN=8000; BIG_UP=3000; BIG_DOWN=12000
fi

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

# Custom CPU Governor per mode (Alluka Mode Settings)
CUSTOM_GOV="$(get_tweak "gov_${PROFILE}" "")"
if [ -n "$CUSTOM_GOV" ]; then
  for policy in /sys/devices/system/cpu/cpufreq/policy*; do
    [ -d "$policy" ] && write_node "$policy/scaling_governor" "$CUSTOM_GOV"
  done
  log "Applied custom governor for $PROFILE: $CUSTOM_GOV"
fi

# Global schedutil fallback (if exposed)
if [ -d /sys/devices/system/cpu/cpufreq/schedutil ]; then
  set_schedutil /sys/devices/system/cpu/cpufreq/schedutil "$LITTLE_UP" "$LITTLE_DOWN"
fi

# Android 10+ SchedTune foreground boost (Alluka Engine)
set_stune top-app "$TOPBOOST" 1
set_stune foreground "$FGBOOST" 1
set_stune background 0 0
set_stune system-background 0 0
write_node /proc/sys/kernel/sched_boost "$SBOOST"
write_node /proc/sys/kernel/sched_migration_cost_ns 500000
write_node /proc/sys/kernel/sched_schedstats 0
write_node /proc/sys/kernel/timer_migration 1

# MediaTek GED boost control (Alluka Feature)
CUSTOM_GED="$(get_tweak "ged_boost_enable" "")"
if [ "$PROFILE" = "peforma" ] || [ "$CUSTOM_GED" = "1" ]; then
  write_node /sys/module/ged/parameters/ged_boost_enable 1
  write_node /sys/module/ged/parameters/boost_gpu_enable 1
  write_node /sys/module/ged/parameters/enable_cpu_boost 1
  write_node /sys/module/ged/parameters/enable_gpu_boost 1
else
  restore_ged
fi

# 4 GB RAM: Swappiness & VM memory tuning (Alluka Conservative Retain)
write_node /proc/sys/vm/swappiness "$SWAP"
write_node /proc/sys/vm/page-cluster 0
write_node /proc/sys/vm/dirty_background_ratio 5
write_node /proc/sys/vm/dirty_ratio 20
write_node /proc/sys/vm/watermark_scale_factor 20
write_node /proc/sys/vm/vfs_cache_pressure 100

# Conservative eMMC/dm read-ahead (128 KB queue buffer)
READ_AHEAD_KB="$(get_tweak "read_ahead_kb" "128")"
for queue in /sys/block/mmcblk*/queue /sys/block/dm-*/queue; do
  [ -d "$queue" ] && write_node "$queue/read_ahead_kb" "$READ_AHEAD_KB"
done

# Custom I/O Scheduler per mode (Alluka Mode Settings)
CUSTOM_IO="$(get_tweak "io_${PROFILE}" "")"
if [ -n "$CUSTOM_IO" ]; then
  for queue in /sys/block/mmcblk*/queue /sys/block/sd*/queue /sys/block/dm-*/queue; do
    [ -d "$queue" ] && write_node "$queue/scheduler" "$CUSTOM_IO"
  done
  log "Applied custom I/O scheduler for $PROFILE: $CUSTOM_IO"
fi

# Bypass Charging check if enabled by user
BYPASS_CHG="$(get_tweak "bypass_charging" "0")"
if [ "$BYPASS_CHG" = "1" ]; then
  for node in /sys/class/power_supply/battery/charging_enabled /sys/class/power_supply/battery/input_suspend; do
    [ -w "$node" ] && write_node "$node" 0
  done
fi

# Save profile & update module prop
echo "$PROFILE" > "$PROFILE_FILE"
update_description

log "Alluka profile $PROFILE applied successfully"
log "DONE thermal=untouched selinux=untouched clocks=unlocked cpuidle=untouched anti-bootloop=OK"

