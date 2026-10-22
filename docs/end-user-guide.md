# End-user guide — no programming needed

This guide replaces the Chinese-only Bonfire OS home screen of a JMGO N1
(China domestic model) with MTH Launcher, using a Windows PC and
double-click files. Nothing is rooted, flashed or uninstalled, and a
restore file puts everything back.

## What you need

- A Windows PC connected to the **same Wi-Fi** as the projector.
- The projector switched on and showing its normal home screen.
- The release `.zip` from this repository's **Releases** page, unzipped.
  It contains `adb.exe`, the launcher APK (`MTH-Launcher.apk`), and the
  numbered kit files (`.bat`).

## Step 0 — Find the projector's IP address

The kits ask for it once and remember it (in `kit/ip.txt`).

Easiest ways, pick one:

- **Router app / admin page**: look at the connected-devices list for a
  device named like `JMGO`, `N1` or a MediaTek hostname; note its
  `192.168.x.x` address.
- **Phone app** like Fing (free): scan your home network and find the same
  device.
- The address usually starts with `192.168.` — you only need the numbers.

## Step 1 — Install the launcher

Double-click **`1-INSTALL-MTH.bat`**, type the IP, press Enter.

- You should see `Success` — that means the app is installed.
- The kit offers to open MTH Launcher on the projector right away. Say `y`
  and look at the projector: you should see the new home screen (it is not
  the default yet — that is the next step).

> If Windows shows a blue "Windows protected your PC" box, click
> **More info → Run anyway**. These are plain batch files — you can open
> any of them in Notepad and read every command before running it.

## Step 2 — Make MTH the default home

Double-click **`2-SET-HOME-STEP1.bat`** and follow the questions. It opens
MTH first and asks you to confirm it is on screen before changing anything,
then sets it as default and reboots the projector if you agree.

- After the reboot, if the projector starts into **MTH Launcher** and stays
  there: done. 🎉
- If it starts into the old **Bonfire** home again: your firmware re-asserts
  the stock launcher (the reference unit does this). Run
  **`3-SET-HOME-STEP2-STRONG.bat`** — it disables the stock launcher
  package for your user (it does not uninstall it) — then reboot and check
  again.

## Step 3 (optional) — Add your language

The launcher starts in English and already includes Vietnamese. For another
language:

1. Get the language file (e.g. `th.txt`) — from `launcher/lang/` in this
   repository, or make one from `en.txt` (see `adding-a-language.md`).
2. Put it in the **`lang-drop`** folder inside the kit.
3. Double-click **`5-ADD-LANGUAGE.bat`**.
4. On the projector, tap **Language** in the left rail — it cycles through
   every installed language.

## Going back to stock

Double-click **`4-RESTORE-STOCK-HOME.bat`** and reboot. The stock Bonfire
launcher is re-enabled and set as default again, exactly as before. MTH
Launcher stays installed but unused; you can uninstall it like any app, or
keep it and switch back any time by re-running step 2.

## Troubleshooting

| Symptom | What to do |
|---|---|
| `cannot connect` / `no device` | Projector off, PC on another network, or the IP changed. Check the IP again (Step 0), delete `kit/ip.txt`, retry. |
| `INSTALL_FAILED_UPDATE_INCOMPATIBLE` | A launcher signed with a different key is installed (e.g. a self-built APK). Uninstall the old one, then run step 1 again. |
| Reboot lands on a black screen | Run `4-RESTORE-STOCK-HOME.bat`, reboot, then open an issue with your firmware version. |
| The settings panel won't open from the launcher | On JMGO remotes the settings panel is a system feature: **long-press the Menu/Settings key** on the remote. The launcher deliberately never blocks that key. |
| YouTube app says "not supported on this device" | Expected on this hardware (no Google certification). Use **SmartTube** — the launcher's cards are built for it. Netflix is likewise unavailable on Bonfire OS. |

Still stuck? Open an issue and attach the result of `kit/0-PROBE.bat`
(see `new-device-support.md`).
