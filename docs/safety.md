# Safety model

Everything in this project is designed to be **undoable**, because most
users running it are not technicians and many devices have no local service
support.

## What the kits never do

- **No root.** Every step uses the standard ADB shell.
- **Nothing is written to `/system`.** No flashing, no firmware modification,
  no repacked images.
- **Nothing is uninstalled.** Packages are only ever *disabled for user 0*
  (`pm disable-user --user 0`). The package stays on the device and can be
  re-enabled at any time — the restore kit does exactly that.
- **No other apps are bundled.** The kits install only the Lak Launcher APK
  from the same release. If a guide tells you to install SmartTube or another
  app, it links the official source and lets you decide.

## The Home takeover, and why it is two steps

On Bonfire OS the stock launcher (`com.jmgo.launcher`) re-asserts itself as
Home after every reboot, and the ROM refuses to disable just its Home
component (`pm disable-user` on the component throws a SecurityException).
So the kit works in stages:

1. **Step 1** disables only the stock Home *component* and sets Lak Launcher
   as the default. On firmware that accepts it, this is all you need.
2. **Step 2** (only if step 1 does not survive a reboot) disables the whole
   `com.jmgo.launcher` package for user 0.

Before either step, the kit opens Lak on screen and asks you to confirm it
is really showing — the projector is never left without a working home
screen. **Restore** re-enables the stock launcher and sets it back as
default.

## The never-disable rule

Some packages run the projector itself: autofocus, keystone correction, the
gimbal, brightness, remote key handling, HDMI inputs, Bluetooth. They are
listed per device in `data/<device>/never-disable.txt`, they are excluded
from every debloat list in this repo, and contributions that touch them are
rejected. A cleaner home screen is never worth a projector that cannot
focus.

## If something goes wrong

- The projector boots to a black screen or the wrong home: run
  `kit/4-RESTORE-STOCK-HOME.bat`, reboot, and you are back to stock.
- ADB stops answering: reboot the projector and check its IP did not change
  (routers reassign IPs; the kits store the IP in `kit/ip.txt` — delete that
  file to enter a new one).
- Worst case: a factory reset removes everything this project ever did,
  because nothing was written to `/system`.
