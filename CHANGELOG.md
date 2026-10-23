# Changelog

## v2.7 — 2026-10-23 (section-based navigation)

- **The rail is now a real navigation menu.** It has six entries — Home,
  Apps, YouTube, VTVgo TV, Settings, Exit — and selecting Home / Apps /
  YouTube / Settings swaps the entire right pane to that section (VTVgo TV
  and Exit stay direct actions). The active section is highlighted.
- **Home page** is rows only, all horizontally scrolling: an apps row,
  the SmartTube "History & Library" cards and the "Explore" cards. The
  Rearrange button no longer sits on Home.
- **Apps page** shows every installed app in the full 8-column grid;
  Rearrange Apps moved here, top-right under the clock and date
  (long-press OK on an app still enters rearrange mode too).
- **YouTube page** shows only SmartTube content (the two card rows).
- **Settings page** groups the three former rail entries as submenus:
  Projector Settings, System Settings and Language (showing the current
  language), plus the remote long-press hint.
- Exit uses a door (log-out style) icon, as requested by the maintainer.
- New language keys `home` and `settings` in both packs.
- Same signing key as v2.4–v2.6, so it installs over them; app order and
  language choice carry over.

## v2.6 — 2026-10-23 (renamed to Lak Launcher)

- **Project renamed to Lak Launcher** (repository: `Lak-launcher`). The app's
  display name, the APK file name (`Lak-Launcher-v2.6.apk`), the install kit
  and every document now use the new name — no trace of the old name remains
  in the source.
- Because the name changed, user-installed language packs now load from
  `/sdcard/Lak-Launcher/lang/` (renamed from the v2.5 folder). If you added a
  pack under v2.5, push it once more with kit `5-ADD-LANGUAGE.bat`.
- Same signing key as v2.4/v2.5, so it installs over them; the saved app
  order and language choice carry over.

## v2.5 — 2026-10-23 (first public source release)

- **Language packs.** All UI strings moved out of the code into plain-text
  packs (`launcher/lang/en.txt`, `vi.txt`). English is the default and the
  fallback for missing keys.
- Packs are baked into the APK at build time from `launcher/lang/`, and the
  launcher additionally loads packs at runtime from
  `/sdcard/Lak-Launcher/lang/` and its app-data `files/lang/` folder — so a
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
