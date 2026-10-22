# Supporting a new device

Support for a new projector or TV model starts with a **probe report** —
a read-only dump of what the device actually is and runs. No list in this
repository is guessed from another model.

## 1. Run the probe

On a Windows PC on the same network as the device:

1. Double-click `kit/0-PROBE.bat` and type the device's IP address.
2. It connects over ADB and writes `PROBE-RESULT.txt` plus a screenshot
   (`probe-screen.png`) into the kit folder. **It changes nothing on the
   device.** The only active test is `adb root`, which simply reports
   whether the firmware allows it — the script returns ADB to normal
   (`adb unroot`) before it finishes.

The result file contains: full build properties (model, firmware, build
type — `user` vs `userdebug`, verified-boot state), SELinux mode, the
complete package list with APK paths, the system and disabled package
lists, the current Home owner, settings tables, and the installed Lak /
SmartTube versions if present.

## 2. Open an issue

Attach `PROBE-RESULT.txt` and the screenshot to a new issue titled
`Device support: <brand> <model> (<firmware version>)`. Say which stock
launcher the device shows and anything that misbehaves.

## 3. What happens next

From the probe we build, for your exact model and firmware:

- a device profile in `data/<device>/` (hardware, OS, stock launcher
  package, Home behaviour),
- a **never-disable** list (optics/remote/input packages — untouchable),
- debloat candidates in small batches, each batch marked `reported` until
  you confirm the device still focuses, the remote works, HDMI switching
  works and the settings panel still opens after a reboot — then `tested`.

That confirmation step is the project's quality gate: **the owner's eyes on
the real device outrank every tool's output.**
