# Changelog

## v2.10 — 2026-10-23 (declare READ_EPG_DATA so channel rows actually load)

Field diagnosis on the JMGO N1 (Bonfire OS, Android 11): the v2.8/v2.9
channel reading returned empty cursors even after SmartTube had
published its channels and a reference launcher on the same device
displayed them fine. A permission comparison found the difference —
the reference launcher holds `com.android.providers.tv.permission.
READ_EPG_DATA` (granted at install on this firmware), while Lak
declared no TV-provider permission at all, so the provider filtered
its rows out for this app (the ADB shell sees the same empty store).

- Manifest now declares `com.android.providers.tv.permission.
  READ_EPG_DATA` (read-only; the launcher never writes to the TV
  provider). On firmware that grants it, the published channel rows
  load; where it is not granted, the rows simply stay empty, exactly
  as before.
- No behavior or layout changes. Same signing key as v2.4–v2.9;
  installs over them.

## v2.9 — 2026-10-23 (video rows placed per section; SmartTube search)

Layout rework requested by the maintainer after v2.7/v2.8 testing
(v2.7's section cards confirmed on-device to open the right SmartTube
sections):

- **Home** no longer carries section shortcut cards. Its YouTube rows are
  video rows only: "YouTube — History" shows just the history channel's
  videos, "YouTube — Explore" just the suggested/recommended channel's
  videos (channels are matched by name; unmatched published channels still
  surface under their own names).
- **YouTube page** order is now: row 1 suggested videos, row 2 history
  videos, then Watch Next and any other published channel, and at the
  bottom one shortcut row — Library, Liked, Subscriptions, Watch Later.
- **Search pill** opens SmartTube's own search page (the same verified
  section route the cards use) instead of the system global search, which
  is decorative on devices without Google services. The pill's label now
  says what it does in both language packs.
- New language keys `ytSuggested` / `ytRow3`; `ytRow1` / `ytRow2` values
  shortened to match the new rows.
- Same signing key as v2.4–v2.8; installs over them.

## v2.8 — 2026-10-23 (real video rows from TV channels)

- **The launcher now reads Android TV channels** (`content://android.media.tv`)
  — the standard mechanism Google TV's home uses. Every channel an app
  publishes becomes a poster row of real videos on the **Home** page and on
  the **YouTube** page; tapping a tile opens that exact video through the
  program's stored intent URI. A **Watch Next** row appears when the system
  has watch-next data. Data refreshes whenever you return to the launcher.
- This is the route to the maintainer's actual request — SmartTube's real
  history and suggested videos as tiles on the home screen. Whether any
  rows appear depends on SmartTube publishing channels on this ROM: the
  projector's TV provider exists (verified on-device 2026-10-23), but its
  tables were empty when probed over ADB — and the ADB shell may simply
  lack read access, so the launcher itself is now the probe: when nothing
  is shared, the YouTube page shows a status line with the exact counts it
  could read (channels / videos / watch-next, plus any read error).
- Added the `INTERNET` permission (poster images may be remote URLs) and
  the `watchNext` / `noChannelData` language keys.
- Same signing key as v2.4–v2.7; installs over them, settings carry over.

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
