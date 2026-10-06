SKIPUNZIP=0

ui_print " "
ui_print "  ┌────────────────────────────────────┐"
ui_print "  │               ALLUKA               │"
ui_print "  │     Smooth Motion • Quiet Power    │"
ui_print "  └────────────────────────────────────┘"
ui_print "  Manager    : Alluka Companion App"
ui_print "  Maintainer : Alluka (@Alluka_id)"
ui_print "  Version    : v1.0"
ui_print "  GitHub     : https://github.com/ridhwanai/Alluka"
ui_print " "

DEVICE="$(getprop ro.product.device)"
ANDROID="$(getprop ro.build.version.release)"
KERNEL="$(uname -r)"

ui_print "  [✓] Device   : $DEVICE"
ui_print "  [✓] Android  : $ANDROID"
ui_print "  [✓] Kernel   : $KERNEL"
ui_print " "

CONFIG_DIR=/data/adb/.config/alluka
mkdir -p "$CONFIG_DIR"
[ -s "$CONFIG_DIR/profile" ] || echo daily > "$CONFIG_DIR/profile"

touch "$MODPATH/skip_mount"
cp -f "$MODPATH/module.prop" "$MODPATH/module.prop.orig"
set_perm_recursive "$MODPATH" 0 0 0755 0644

for script in customize.sh post-fs-data.sh service.sh action.sh apply.sh uninstall.sh; do
  [ -f "$MODPATH/$script" ] && set_perm "$MODPATH/$script" 0 0 0755
done
set_perm_recursive "$CONFIG_DIR" 0 0 0755 0644

# Auto-install Manager APK if packaged
APK="$MODPATH/Alluka.apk"
if [ "$BOOTMODE" = true ] && [ -f "$APK" ]; then
  ui_print "- Menginstal Alluka Manager..."
  RESULT="$(pm install -r -d --user 0 "$APK" 2>&1)"
  if echo "$RESULT" | grep -qi Success; then
    ui_print "  [✓] Alluka Manager berhasil diinstal!"
  else
    ui_print "  [!] Penginstalan APK otomatis tertunda."
    ui_print "      Silakan instal manual dari: $APK"
  fi
else
  ui_print "  [i] Recovery Mode: Silakan instal Alluka.apk setelah booting."
fi

ui_print "- Profil aktif awal : $(cat "$CONFIG_DIR/profile")"
ui_print "- Keamanan sistem   : Thermal & SELinux tetap aman"
ui_print " "
ui_print "  [✓] Penginstalan selesai! Silakan reboot perangkat."
ui_print " "
