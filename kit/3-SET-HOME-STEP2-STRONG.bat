@echo off
title Lak Kit - 3. Set Lak as Home (step 2, strong)
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
echo  STEP 3 (strong): disable the WHOLE stock launcher package
echo  com.jmgo.launcher for user 0. Use only if step 2 did not
echo  survive a reboot. The stock desktop disappears completely
echo  (that is the point). The projector settings panel and the
echo  HDMI inputs are separate packages and keep working.
echo  ADB can bring everything back with 4-RESTORE-STOCK-HOME.bat.
echo ============================================================
echo.
echo 1/3: Opening Lak + setting it as default again...
adb shell am start -n com.lakasa.n1launcher/.MainActivity
set /p OK=Is Lak Launcher showing on the projector? (y/n):
if /i not "%OK%"=="y" (
  echo Stopped - nothing was changed.
  pause
  exit /b
)
adb shell cmd package set-home-activity com.lakasa.n1launcher/.MainActivity
adb shell pm set-home-activity com.lakasa.n1launcher/.MainActivity
echo.
echo 2/3: Disabling the whole com.jmgo.launcher package...
adb shell pm disable-user --user 0 com.jmgo.launcher
echo.
echo 3/3: Checking who owns Home now:
adb shell cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.HOME
echo.
set /p RB=Reboot the projector now to verify? (y/n):
if /i "%RB%"=="y" adb reboot
echo.
echo After the reboot the projector must boot straight into Lak.
pause
