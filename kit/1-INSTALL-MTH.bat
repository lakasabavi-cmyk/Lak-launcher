@echo off
title MTH Kit - 1. Install MTH Launcher
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
echo  STEP 1: install MTH Launcher (it is NOT the default yet).
echo ============================================================
if not exist MTH-Launcher.apk (
  echo MTH-Launcher.apk is missing from this folder.
  echo Download the release zip and unzip ALL of it, then run again.
  pause
  exit /b
)
adb install -r MTH-Launcher.apk
echo.
set /p OPEN=Open MTH Launcher on the projector now? (y/n):
if /i "%OPEN%"=="y" adb shell am start -n com.lakasa.n1launcher/.MainActivity
echo.
echo Next: run 2-SET-HOME-STEP1.bat to make it the default home.
pause
