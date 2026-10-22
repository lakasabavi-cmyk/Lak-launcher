# Adding a language

MTH Launcher's UI text is not in the code — it is in plain-text packs, so
translating the launcher is a text-file job, not a programming job.

## Make a pack

1. Copy `launcher/lang/en.txt` and rename the copy to your language code:
   `th.txt` (Thai), `id.txt` (Indonesian), `zh.txt` (Chinese), ...
2. Translate the **values** (right of `=`). Never change the keys (left of
   `=`). Keep the `__name` line in your own language and set `__locale` to
   your `language-REGION` (it drives the date format on the home screen).
3. You may leave hard strings in English — anything missing falls back to
   English automatically, and the pack still counts as installed.

## Test it on your projector (no rebuild)

```sh
adb connect <projector-ip>:5555
adb shell mkdir -p /sdcard/MTH-Launcher/lang
adb push <your-file>.txt /sdcard/MTH-Launcher/lang/
adb shell pm grant com.lakasa.n1launcher android.permission.READ_EXTERNAL_STORAGE
adb shell am force-stop com.lakasa.n1launcher
```

…or drop the file into `kit/lang-drop/` and double-click
`kit/5-ADD-LANGUAGE.bat`. Re-open the launcher and tap **Language** — it
cycles through every installed pack, yours included.

## Share it

Open a pull request adding your file under `launcher/lang/`. Once merged,
your language ships inside the next APK for everyone, and its name in the
credits is yours.
