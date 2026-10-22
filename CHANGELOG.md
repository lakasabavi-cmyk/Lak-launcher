# Changelog

## v2.5 — 2026-10-23 (first public source release)

- **Language packs.** All UI strings moved out of the code into plain-text
  packs (`launcher/lang/en.txt`, `vi.txt`). English is the default and the
  fallback for missing keys.
- Packs are baked into the APK at build time from `launcher/lang/`, and the
  launcher additionally loads packs at runtime from
  `/sdcard/MTH-Launcher/lang/` and its app-data `files/lang/` folder — so a
  language can be added to an installed launcher with one ADB push, no update.
- The Language menu entry now cycles through every installed pack instead of
  toggling English/Vietnamese; the v2.4 toggle setting migrates automatically.
- Launcher icon is now a vector drawable (the repository is text-only).
- Added `READ_EXTERNAL_STORAGE` so packs in the public folder can be read.
- Status: built and signature-verified (same signing key as v2.4, so it
  installs over v2.4). On-device confirmation pending.

## v2.4 — 2026-10-22

- SmartTube shortcuts reworked: cards open SmartTube sections (History,
  Subscriptions, Search, Playlists) through explicit component intents
  instead of youtube.com VIEW links, which could land on SmartTube's
  settings screen when the app resumed an old task.

## v2.3 – v2.2 — 2026-10-22

- YouTube entry and cards target SmartTube (the official YouTube TV app is
  blocked on this hardware); the Netflix rail entry replaced by VTVgo TV.

## v2.1 – v2.0 — 2026-10-22

- Android TV 9-style layout: left rail (Apps, YouTube, VTVgo/Netflix,
  Projector Settings, System Settings, Language, Exit), 8-column app grid,
  large shortcut cards, rearrange mode (long-press OK or the Rearrange
  button).

## v1.0 – v1.9 — 2026-10-22

- First working launcher and the ADB kits that made it the default Home on
  Bonfire OS: the stock launcher re-asserts itself after `set-home-activity`,
  the ROM blocks disabling its Home component, and disabling the whole
  `com.jmgo.launcher` package for user 0 is what finally sticks. A restore
  kit ships alongside.
