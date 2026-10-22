# The double-click kit

These `.bat` files are the whole user interface of this project for
non-technical owners: connect over ADB, answer y/n, done. Run them **in
number order**.

| File | What it does |
|---|---|
| `0-PROBE.bat` | Read-only device report (`PROBE-RESULT.txt` + screenshot) — for support requests and new-device submissions. Changes nothing. |
| `1-INSTALL-MTH.bat` | Installs `MTH-Launcher.apk` from this folder. |
| `2-SET-HOME-STEP1.bat` | Gentle Home takeover: set MTH as default, disable only the stock Home component. |
| `3-SET-HOME-STEP2-STRONG.bat` | Strong takeover (Bonfire OS needs this): disables the whole stock launcher package for user 0. |
| `4-RESTORE-STOCK-HOME.bat` | Undoes everything: re-enables the stock launcher and makes it default again. |
| `5-ADD-LANGUAGE.bat` | Pushes every pack in `lang-drop/` to `/sdcard/MTH-Launcher/lang/` and restarts the launcher. |

How the kit behaves:

- The first file you run asks for the projector's IP and stores it in
  `ip.txt` next to the scripts; the rest reuse it. Delete `ip.txt` to enter
  a different address.
- Every step that changes something opens MTH on screen first and asks you
  to confirm it is really showing before touching the stock launcher.
- All changes are `pm disable-user` / `pm enable` — nothing is uninstalled,
  nothing is written to `/system`.

## Assembling a release kit

The end-user release `.zip` is this folder plus:

- `adb.exe`, `AdbWinApi.dll`, `AdbWinUsbApi.dll` from the official Android
  platform-tools for Windows,
- the release APK renamed to `MTH-Launcher.apk`,
- `docs/end-user-guide.md` from the repository root.

From source, do the same: copy the three platform-tools files next to these
scripts, build the launcher (`launcher/build.sh`) and copy its output APK
here as `MTH-Launcher.apk`.
