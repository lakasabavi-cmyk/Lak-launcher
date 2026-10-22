@echo off
title Lak Kit - 5. Add a language pack
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
echo  ADD A LANGUAGE: pushes every .txt pack found in the
echo  lang-drop folder to the projector. The launcher picks
echo  new languages up on its next start - no reinstall.
echo ============================================================
echo.
adb shell mkdir -p /sdcard/Lak-Launcher/lang
set FOUND=
for %%F in (lang-drop\*.txt) do (
  echo Pushing %%~nxF ...
  adb push "%%F" /sdcard/Lak-Launcher/lang/
  set FOUND=1
)
if not defined FOUND (
  echo No .txt files found in the lang-drop folder.
  echo Put a language pack there first - see docs/adding-a-language.md
  pause
  exit /b
)
adb shell pm grant com.lakasa.n1launcher android.permission.READ_EXTERNAL_STORAGE
adb shell am force-stop com.lakasa.n1launcher
echo.
echo Done. Open the launcher and tap LANGUAGE in the left rail -
echo it cycles through every installed language.
pause
