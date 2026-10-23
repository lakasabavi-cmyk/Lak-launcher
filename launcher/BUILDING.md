# Building Lak Launcher

No Gradle, no Android Studio project — the build is one transparent shell
script, the same style the project started with.

## Requirements

- Linux or macOS (Windows: use WSL)
- JDK 17
- Android SDK: platform **android-31** and **build-tools 34.0.0**
  (`build.sh` downloads and installs them into `~/android-sdk` on first run
  if they are missing)

## Build

```sh
cd launcher
./build.sh
# -> output/Lak-Launcher-v2.9.apk
```

What the script does, in order:

1. Copies `lang/*.txt` into `app/src/main/assets/lang/` (language packs are
   baked into the APK from the `lang/` folder — adding a language to a build
   is literally dropping a `.txt` file in `lang/`).
2. `aapt2 compile` + `link` (with the assets), `javac` (source/target 8),
   `d8`, zipalign.
3. Signs with `keystore.jks` in this folder (created on first run if absent)
   using v1+v2+v3 signatures, then verifies the APK and prints its SHA-256.

## Signing notes

- `keystore.jks` is **git-ignored and must never be committed**.
- Official releases are signed with the maintainer's key. An APK signed with
  a different key will NOT install over an existing install — Android will
  refuse it. If you build with your own key, uninstall the old launcher
  first (your app order and settings live in the app, so they reset).
- Keep a backup of the release keystore: losing it means every user has to
  uninstall before updating.
