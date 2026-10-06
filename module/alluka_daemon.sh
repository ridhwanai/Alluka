#!/system/bin/sh
# ==============================================================================
# Alluka Engine • Background Sensing Daemon (Auto Mode & GED Tracking)
# Maintainer: Alluka (@Alluka_id)
# ==============================================================================

MODDIR=${0%/*}
CONFIG_DIR=/data/adb/.config/alluka
PROFILE_FILE="$CONFIG_DIR/profile"
GAMELIST_FILE="$CONFIG_DIR/gamelist.txt"
PID_FILE="$CONFIG_DIR/service.pid"
LOG_FILE="$CONFIG_DIR/alluka.log"

mkdir -p "$CONFIG_DIR"
echo $$ > "$PID_FILE"

CURRENT_STATE="daily"

log_daemon() {
  echo "$(date '+%F %T' 2>/dev/null) [DAEMON] $*" >> "$LOG_FILE"
}

log_daemon "Alluka Daemon started with PID $$"

while true; do
  ACTIVE_PROFILE="$(cat "$PROFILE_FILE" 2>/dev/null)"
  
  if [ "$ACTIVE_PROFILE" = "auto" ]; then
    # 1. Screen State Detection
    IS_OFF=0
    dumpsys power 2>/dev/null | grep -qiE 'mWakefulness=Asleep|Display Power: state=OFF' && IS_OFF=1
    dumpsys display 2>/dev/null | grep -qiE 'mScreenState=OFF' && IS_OFF=1

    if [ "$IS_OFF" -eq 1 ]; then
      if [ "$CURRENT_STATE" != "sleep" ]; then
        sh "$MODDIR/apply.sh" sleep >/dev/null 2>&1
        CURRENT_STATE="sleep"
        log_daemon "Screen OFF -> Transitioned to Sleep mode"
      fi
    else
      # 2. Screen is ON -> Query Top Foreground Package
      TOP_PKG=""
      
      # Try window focus
      TOP_WINDOW="$(dumpsys window 2>/dev/null | grep -E 'mCurrentFocus|mFocusedApp' | head -n 1)"
      case "$TOP_WINDOW" in
        *{*/*}*)
          RAW="${TOP_WINDOW#*{}"
          RAW="${RAW%/*}"
          TOP_PKG="${RAW##* }"
          ;;
      esac
      
      # Fallback to activity resumption
      if [ -z "$TOP_PKG" ]; then
        TOP_RESUMED="$(dumpsys activity activities 2>/dev/null | grep -E 'topResumedActivity|mResumedActivity' | head -n 1)"
        case "$TOP_RESUMED" in
          *{*/*}*)
            RAW="${TOP_RESUMED#*{}"
            RAW="${RAW%/*}"
            TOP_PKG="${RAW##* }"
            ;;
        esac
      fi

      IS_GAME=0
      if [ -n "$TOP_PKG" ] && [ -f "$GAMELIST_FILE" ]; then
        grep -q "^$TOP_PKG$" "$GAMELIST_FILE" 2>/dev/null && IS_GAME=1
      fi

      if [ "$IS_GAME" -eq 1 ]; then
        if [ "$CURRENT_STATE" != "peforma" ]; then
          sh "$MODDIR/apply.sh" peforma >/dev/null 2>&1
          CURRENT_STATE="peforma"
          log_daemon "Game foreground detected ($TOP_PKG) -> Switched to Nanika Peforma"
        fi
      else
        if [ "$CURRENT_STATE" != "daily" ]; then
          sh "$MODDIR/apply.sh" daily >/dev/null 2>&1
          CURRENT_STATE="daily"
          log_daemon "Normal app ($TOP_PKG) -> Switched to Daily Balance"
        fi
      fi
    fi
  fi

  sleep 2
done
