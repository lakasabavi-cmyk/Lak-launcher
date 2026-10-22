# Device profile — JMGO N1 (China domestic, Bonfire OS)

The reference device this project started on.

| Field | Value |
|---|---|
| Model | JMGO N1 (base model, China domestic SKU) |
| SoC | MediaTek MT9671 |
| RAM / storage | 3 GB / 32 GB |
| OS | Bonfire OS (JMGO + frog design), Android-based, Chinese UI only |
| Stock launcher | `com.jmgo.launcher` — Home activity `com.jmgo.os.home.HomeActivity` |
| ADB | LAN port 5555 open on the reference unit out of the box |
| Remote | No Home button; long-press Menu/Settings opens the projector settings panel (system level) |
| Google services | None. Official YouTube TV app installs but reports "not supported on this device" (no GMS/certification); Netflix unavailable |

## Verified behaviour (maintainer's unit, 2026-10-22)

- `cmd package set-home-activity` reports success but the stock launcher
  re-asserts itself as Home after reboot.
- Disabling only the stock Home **component** is refused by the ROM
  (`SecurityException`: shell cannot change component state).
- Disabling the whole **`com.jmgo.launcher`** package for user 0 works:
  Lak Launcher stays Home across reboots. The settings panel and HDMI
  inputs are separate packages and keep working.
- Restore: `pm enable com.jmgo.launcher` + set it back as Home
  (kit file `4-RESTORE-STOCK-HOME.bat`).

## Files in this folder

- `never-disable.txt` — packages that run the projector itself.
- `debloat-candidates.txt` — candidates for the Bonfire debloat list,
  collected from the two JMGO entries in cleanyourtv (N1S international and
  G9S China domestic). **Status: UNVERIFIED on the N1/Bonfire** until a
  probe report from the reference unit confirms which of them exist here
  and survive a reboot. Do not apply blindly.

## Related models (not yet supported)

N1 Pro / N1 Ultra share Bonfire OS on a different SoC (MT9669, 4 GB / 64 GB).
Probe reports welcome — see `docs/new-device-support.md`.
