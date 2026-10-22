# Lak Launcher

**A multilingual replacement launcher for China-domestic JMGO N1 projectors (Bonfire OS) — installed over ADB, no root, with double-click kits for non-technical users.**

> 🤖 **Vibe-coded with Meta Muse Spark.** This project was designed, written and iterated with an AI coding agent (Muse Spark by Meta) working side by side with the device owner, testing on real hardware. The code is open — read it, audit it, improve it.

China-domestic JMGO projectors ship with Bonfire OS in Chinese only: no English settings, a home screen full of local content services, and no Google apps. Lak Launcher replaces that home screen with a clean, TV-style launcher in **your** language — starting with English.

![status](https://img.shields.io/badge/device-JMGO%20N1%20(China%20domestic)-blue) ![os](https://img.shields.io/badge/OS-Bonfire%20OS%20%2F%20Android-green) ![root](https://img.shields.io/badge/root-not%20required-brightgreen) ![license](https://img.shields.io/badge/license-MIT-blue)

## Why this exists

- The stock Bonfire OS home is Chinese-only and cannot be switched to English.
- The official YouTube TV app and Netflix are blocked on this hardware (no Google certification) — so the launcher is built around **SmartTube** instead, with direct shortcuts into its sections.
- Existing debloat projects (cleanyourtv, dumbtv) do not cover China-domestic projectors at all. This project starts with the JMGO N1 and is designed to grow, model by model, from user-submitted probe reports.

## Features

- **English-first UI**, Android TV 9-style layout: left rail, clock and localized date, 8-column app grid.
- **File-based language packs.** UI text lives in plain `key=value` `.txt` files — no Android string resources, no rebuild needed to add a language:
  - Builders: drop a new `xx.txt` into `launcher/lang/` and rebuild; it is baked into the APK automatically.
  - End users: push a `.txt` pack to the projector with ADB and the launcher picks it up on next start (see [Adding a language](docs/adding-a-language.md)).
  - Missing keys fall back to English, so partial translations still work.
- **SmartTube section cards** (Watch History, Library, Liked Videos, Subscriptions, Search, Watch Later, Open SmartTube) that open the right screen directly, plus a VTVgo TV shortcut where Netflix would be.
- **Rearrange mode**: pick an app up, move it, drop it — order is saved.
- **Respects the projector**: it never intercepts the remote's Menu/Settings key, because on JMGO remotes a long-press of that key is how you open the projector's own settings panel.
- **No root, nothing flashed, nothing uninstalled.** Home takeover uses `pm disable-user` on the stock launcher package and can be undone with the restore kit.

## Status

| Part | Status |
|---|---|
| Launcher as default Home on a real China-domestic JMGO N1 | ✅ Verified on the maintainer's unit (running since 2026-10-22) |
| v2.6 (this code): language packs + the Lak rename | 🧪 Built and signature-verified; on-device confirmation pending — see CHANGELOG |
| Debloat lists for Bonfire OS | 🚧 Candidates collected in `data/jmgo-n1-bonfire/`, being verified from probe reports |
| Other models (N1 Pro / N1 Ultra, other brands) | 📋 Planned — submit a probe report, see [Supporting a new device](docs/new-device-support.md) |

## Quick start (end users)

You do **not** need to know any programming. You need a Windows PC on the same Wi-Fi as the projector.

1. Download the latest release `.zip` (launcher APK + kits + this guide) from **Releases**.
2. Find your projector's IP address — the [End-user guide](docs/end-user-guide.md) shows how, step by step.
3. Run the kit files **in number order** (double-click): install → set as Home (step 1, then step 2 only if step 1 doesn't survive a reboot) — and keep the restore file; it puts everything back.
4. Optional: add your language with kit file `5-ADD-LANGUAGE.bat`.

Full walkthrough with screenshots: **[docs/end-user-guide.md](docs/end-user-guide.md)** · Safety model: **[docs/safety.md](docs/safety.md)**

## Build from source

Requirements: JDK 17 and the Android SDK (platform 31, build-tools 34). No Gradle — the build is a transparent shell pipeline (aapt2 → javac → d8 → zipalign → apksigner).

```sh
cd launcher
./build.sh   # produces output/Lak-Launcher-v2.6.apk
```

See [launcher/BUILDING.md](launcher/BUILDING.md) for signing notes.

## Repository layout

| Path | What is inside |
|---|---|
| `launcher/` | The Android app (plain Java, no libraries) + `lang/` language packs |
| `kit/` | Double-click Windows kits for end users (install, set Home, restore, add language, probe) |
| `data/` | Per-device profiles, debloat candidates, never-disable lists |
| `docs/` | End-user guide, safety, how to add a language, how to support a new device |

## Contributing

The fastest ways to help:

- **Translate**: copy `launcher/lang/en.txt`, translate the values, send a pull request — [guide](docs/adding-a-language.md).
- **Report your device**: run the probe kit and open an issue with the result file — that is how new models get supported, no coding needed — [guide](docs/new-device-support.md).
- **Code**: see [CONTRIBUTING.md](CONTRIBUTING.md). Rule of the house: never submit a package list you have not actually run on the named device and firmware.

## Credits

- Method inspiration: [cleanyourtv](https://github.com/Gathin23/cleanyourtv) and [dumbtv](https://github.com/humblemuzzu/dumbtv) — this project covers the China-domestic projector gap they leave open, and aims at users who will never open a terminal.
- SmartTube section shortcuts target [SmartTube](https://github.com/yuliskov/SmartTube) by Yuriy Liskov.

## License

MIT — see [LICENSE](LICENSE).
