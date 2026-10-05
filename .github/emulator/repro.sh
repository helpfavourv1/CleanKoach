#!/usr/bin/env bash
# Runs inside the emulator. Never fails the job: it records what happened.
PKG=com.zdmgold.cleankoach.debug
APK=app/build/outputs/apk/debug/app-debug.apk
OUT=emulator-out
mkdir -p "$OUT"
RES="$OUT/results.txt"
: > "$RES"

adb install -r "$APK" >/dev/null 2>&1 || { echo "install failed" >> "$RES"; exit 0; }
for p in READ_MEDIA_IMAGES READ_MEDIA_VIDEO READ_MEDIA_AUDIO POST_NOTIFICATIONS; do
  adb shell pm grant "$PKG" "android.permission.$p" >/dev/null 2>&1
done

adb logcat -c
adb logcat -v time > "$OUT/logcat.txt" 2>&1 &
LOGPID=$!

launch() {
  adb shell am force-stop "$PKG"
  adb shell pm clear "$PKG" >/dev/null 2>&1
  adb shell pm grant "$PKG" android.permission.READ_MEDIA_IMAGES >/dev/null 2>&1
  adb shell am start -n "$PKG/com.zdmgold.cleankoach.MainActivity" >/dev/null 2>&1
  sleep 12
}

check() {  # $1 = label
  sleep 6
  local alive nodes
  alive=$(adb shell pidof "$PKG" | tr -d '\r')
  adb shell uiautomator dump /sdcard/u.xml >/dev/null 2>&1
  nodes=$(adb shell cat /sdcard/u.xml 2>/dev/null | grep -o "<node " | wc -l)
  adb exec-out screencap -p > "$OUT/shot_$1.png" 2>/dev/null
  if [ -z "$alive" ]; then
    echo "CRASHED  $1" >> "$RES"
  elif [ "$nodes" -lt 5 ]; then
    echo "BLANK    $1 (alive, ui nodes=$nodes)" >> "$RES"
  else
    echo "ok       $1 (ui nodes=$nodes)" >> "$RES"
  fi
  adb shell "run-as $PKG cat files/event_trail.txt" > "$OUT/trail_$1.txt" 2>/dev/null
  adb shell "run-as $PKG cat files/last_crash.txt" > "$OUT/crash_$1.txt" 2>/dev/null
}

echo "== baseline" >> "$RES"
launch
check baseline

echo "== in-app picker (tap the language icon, pick a language)" >> "$RES"
for pair in "fr:Français" "ja:日本語" "zh-CN:简体中文" "ar:العربية" "he:עברית" "hi:हिन्दी" "th:ไทย" "de:Deutsch"; do
  tag=${pair%%:*}; name=${pair#*:}
  launch
  python3 .github/emulator/ui_tap.py "Language" || { echo "no-icon  in-app-$tag" >> "$RES"; continue; }
  sleep 2
  python3 .github/emulator/ui_tap.py "$name" --scroll || { echo "no-row   in-app-$tag" >> "$RES"; continue; }
  check "inapp_$tag"
done

echo "== system per-app locale for every language" >> "$RES"
launch
for tag in en es pt fr de it hi id ja ko ru ar tr vi th pl nl zh-CN zh-TW bn uk sv ro cs el ms fa ur he ta te mr sw hu da fi nb sk bg ca; do
  adb shell cmd locale set-app-locales "$PKG" --locales "$tag" >/dev/null 2>&1
  check "sys_$tag"
  # restart the app if it died so the next language is still tested
  if [ -z "$(adb shell pidof "$PKG" | tr -d '\r')" ]; then
    adb shell am start -n "$PKG/com.zdmgold.cleankoach.MainActivity" >/dev/null 2>&1
    sleep 8
  fi
done

kill $LOGPID 2>/dev/null
grep -n -A30 "FATAL EXCEPTION" "$OUT/logcat.txt" | head -150 > "$OUT/fatal.txt"
echo "== done" >> "$RES"
exit 0
