@echo off
title MTH Kit - Probe (read-only device report)
cd /d %~dp0
set IP=
if exist ip.txt set /p IP=<ip.txt
if "%IP%"=="" set /p IP=Enter the projector IP address (example 192.168.1.50):
if "%IP%"=="" (
  echo No IP address entered. Nothing was done.
  pause
  exit /b
)
echo %IP%> ip.txt
adb connect %IP%:5555

echo ============================================================
echo  PROBE - READ ONLY. Nothing on the device is changed.
echo  It only tries whether "adb root" is allowed, and returns
echo  ADB to normal (adb unroot) before finishing.
echo  Output: PROBE-RESULT.txt + probe-screen.png in this folder.
echo ============================================================
set OUT=PROBE-RESULT.txt
echo MTH PROBE REPORT> %OUT%
echo Date: %DATE% %TIME%>> %OUT%
echo Projector IP: %IP%>> %OUT%
echo.>> %OUT%
echo ===== 1. CONNECTION =====>> %OUT%
adb devices>> %OUT% 2>&1
echo.>> %OUT%
echo ===== 2. BUILD PROPERTIES (FULL getprop) =====>> %OUT%
adb shell getprop>> %OUT% 2>&1
echo.>> %OUT%
echo ===== 3. KEY PROPERTIES =====>> %OUT%
for %%P in (ro.product.model ro.product.brand ro.product.manufacturer ro.product.device ro.build.fingerprint ro.build.version.release ro.build.version.sdk ro.build.version.incremental ro.build.date ro.build.type ro.build.tags ro.debuggable ro.adb.secure ro.secure ro.boot.verifiedbootstate ro.boot.vbmeta.device_state ro.boot.flash.locked ro.serialno ro.hardware) do (
  echo %%P = >> %OUT%
  adb shell getprop %%P>> %OUT% 2>&1
)
echo.>> %OUT%
echo ===== 4. SELINUX + SHELL IDENTITY =====>> %OUT%
adb shell getenforce>> %OUT% 2>&1
adb shell id>> %OUT% 2>&1
adb shell uname -a>> %OUT% 2>&1
echo.>> %OUT%
echo ===== 5. ADB ROOT TEST =====>> %OUT%
adb root>> %OUT% 2>&1
adb shell id>> %OUT% 2>&1
echo -- su / Magisk traces -->> %OUT%
adb shell "ls -l /system/bin/su /system/xbin/su /sbin/su /data/adb/magisk 2>/dev/null">> %OUT% 2>&1
adb unroot>> %OUT% 2>&1
adb shell id>> %OUT% 2>&1
echo.>> %OUT%
echo ===== 6. DISPLAY + STORAGE =====>> %OUT%
adb shell wm size>> %OUT% 2>&1
adb shell wm density>> %OUT% 2>&1
adb shell df -h>> %OUT% 2>&1
echo.>> %OUT%
echo ===== 7. HOME / FOREGROUND =====>> %OUT%
adb shell cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.HOME>> %OUT% 2>&1
adb shell "dumpsys activity activities | grep -i mResumedActivity">> %OUT% 2>&1
echo.>> %OUT%
echo ===== 8. ALL PACKAGES =====>> %OUT%
adb shell pm list packages>> %OUT% 2>&1
echo.>> %OUT%
echo ===== 9. PACKAGES WITH APK PATHS =====>> %OUT%
adb shell pm list packages -f>> %OUT% 2>&1
echo.>> %OUT%
echo ===== 10. SYSTEM PACKAGES =====>> %OUT%
adb shell pm list packages -s>> %OUT% 2>&1
echo.>> %OUT%
echo ===== 11. DISABLED PACKAGES =====>> %OUT%
adb shell pm list packages -d>> %OUT% 2>&1
echo.>> %OUT%
echo ===== 12. BRAND / PLATFORM PACKAGES =====>> %OUT%
adb shell pm list packages | findstr /i "jmgo bonfire mediatek">> %OUT% 2>&1
echo.>> %OUT%
echo ===== 13. MTH LAUNCHER + SMARTTUBE VERSIONS =====>> %OUT%
adb shell dumpsys package com.lakasa.n1launcher | findstr /i "versionName versionCode">> %OUT% 2>&1
adb shell dumpsys package org.smarttube.stable | findstr /i "versionName versionCode">> %OUT% 2>&1
adb shell dumpsys package org.smarttube.beta | findstr /i "versionName versionCode">> %OUT% 2>&1
echo.>> %OUT%
echo ===== 14. SETTINGS (global / secure / system) =====>> %OUT%
adb shell settings list global>> %OUT% 2>&1
adb shell settings list secure>> %OUT% 2>&1
adb shell settings list system>> %OUT% 2>&1
echo.>> %OUT%
echo ===== END OF REPORT =====>> %OUT%
adb shell screencap -p /sdcard/probe-screen.png
adb pull /sdcard/probe-screen.png probe-screen.png
echo.
echo DONE. Please send these two files back:
echo   PROBE-RESULT.txt
echo   probe-screen.png
pause
