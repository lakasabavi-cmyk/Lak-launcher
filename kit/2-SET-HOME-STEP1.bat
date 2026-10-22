@echo off
title Lak Kit - 2. Set Lak as Home (step 1, gentle)
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
echo  STEP 2 (gentle): disable only the stock Home screen
echo  component and set Lak Launcher as the default Home.
echo  Nothing is uninstalled. Undo any time with
echo  4-RESTORE-STOCK-HOME.bat.
echo ============================================================
echo.
echo 1/4: Opening Lak Launcher remotely...
adb shell am start -n com.lakasa.n1launcher/.MainActivity
set /p OK=Is Lak Launcher showing on the projector? (y/n):
if /i not "%OK%"=="y" (
  echo Stopped - nothing was changed. Make sure Lak opens first.
  pause
  exit /b
)
echo.
echo 2/4: Setting Lak as the default Home...
adb shell cmd package set-home-activity com.lakasa.n1launcher/.MainActivity
adb shell pm set-home-activity com.lakasa.n1launcher/.MainActivity
echo.
echo 3/4: Disabling only the stock Home component...
adb shell pm disable-user --user 0 com.jmgo.launcher/com.jmgo.os.home.HomeActivity
echo (If the line above shows a SecurityException, your firmware
echo  blocks this gentle step - that is normal on Bonfire OS.
echo  Finish this file, reboot to check, then use step 3.)
echo.
echo 4/4: Checking who owns Home now (should say com.lakasa):
adb shell cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.HOME
echo.
set /p RB=Reboot the projector now to verify? (y/n):
if /i "%RB%"=="y" adb reboot
echo.
echo AFTER THE REBOOT:
echo - Boots into Lak and stays there = DONE.
echo - Boots into Bonfire again = run 3-SET-HOME-STEP2-STRONG.bat
pause
