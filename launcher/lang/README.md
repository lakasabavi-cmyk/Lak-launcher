# Language packs

Every UI string of MTH Launcher lives in a plain-text pack in this folder.
English (`en.txt`) is the reference pack and the default language.

## Format

UTF-8 text, one `key=value` per line. Lines starting with `#` are comments.
Values are trimmed, so leading/trailing spaces are not possible (the code
adds spaces where it joins strings).

Two meta keys describe the pack itself:

| Key | Meaning | Example |
|---|---|---|
| `__name` | Name shown for this language | `__name=Tiếng Việt` |
| `__locale` | Locale for the date line, `language-REGION` | `__locale=vi-VN` |

The file name is the language code: `vi.txt`, `zh.txt`, `th.txt`, ...

## How packs are loaded (first match loses — later sources win)

1. **APK assets** — everything in this `lang/` folder is copied into the APK
   at build time by `build.sh`.
2. **App folder** — `Android/data/com.lakasa.n1launcher/files/lang/*.txt`
   on the device (no permission needed).
3. **Public folder** — `/sdcard/MTH-Launcher/lang/*.txt` (needs the storage
   permission the launcher declares; the language kit grants it).

A pack in a later source overrides single keys of the same language in an
earlier one, and a missing key falls back to English — partial packs work.

## Add a language without rebuilding

```sh
adb connect <projector-ip>:5555
adb shell mkdir -p /sdcard/MTH-Launcher/lang
adb push th.txt /sdcard/MTH-Launcher/lang/
adb shell pm grant com.lakasa.n1launcher android.permission.READ_EXTERNAL_STORAGE
adb shell am force-stop com.lakasa.n1launcher
```

Then open the launcher and use the **Language** entry — it cycles through
every installed pack. Or double-click `kit/5-ADD-LANGUAGE.bat`, which does
all of the above for files dropped into `kit/lang-drop/`.

## Contribute a translation

Copy `en.txt`, translate the values (never the keys), keep `__name` in the
pack's own language, and open a pull request. See `docs/adding-a-language.md`
in the repository root.
