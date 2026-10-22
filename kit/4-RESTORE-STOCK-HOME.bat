@echo off
title Lak Kit - 4. Restore stock Bonfire home
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
echo  RESTORE: re-enable the stock JMGO launcher and set it
echo  back as the default Home. Lak Launcher stays installed
echo  but unused.
echo ============================================================
echo.
echo Re-enabling the stock launcher package + Home component...
adb shell pm enable com.jmgo.launcher
adb shell pm enable com.jmgo.launcher/com.jmgo.os.home.HomeActivity
echo Setting JMGO back as the default Home...
adb shell cmd package set-home-activity com.jmgo.launcher
adb shell pm set-home-activity com.jmgo.launcher
echo.
echo Checking who owns Home now:
adb shell cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.HOME
set /p RB=Reboot the projector now? (y/n):
if /i "%RB%"=="y" adb reboot
pause
