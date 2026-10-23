#!/bin/bash
# Manual Android build pipeline (no Gradle) for Lak Launcher.
# Language packs in lang/*.txt are copied into the APK assets at build
# time, so adding a language to a build is literally "drop a .txt in lang/".
set -e
cd "$(dirname "$0")"

export JAVA_HOME="${JAVA_HOME:-$HOME/tools/jdk17}"
export PATH="$JAVA_HOME/bin:$PATH"
export TMPDIR="$HOME/workspace/tmp"; mkdir -p "$TMPDIR"
SDK="${ANDROID_SDK_ROOT:-$HOME/android-sdk}"
BT="$SDK/build-tools/34.0.0"
PLATFORM="$SDK/platforms/android-31/android.jar"

if [ ! -f "$PLATFORM" ]; then
  echo "== Installing Android SDK (platform 31 + build-tools 34) =="
  mkdir -p "$SDK/cmdline-tools"
  if [ ! -d "$SDK/cmdline-tools/latest" ]; then
    curl -L -o /tmp/cmdtools.zip https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip
    unzip -q -o /tmp/cmdtools.zip -d "$SDK/cmdline-tools"
    mv "$SDK/cmdline-tools/cmdline-tools" "$SDK/cmdline-tools/latest"
  fi
  yes | "$SDK/cmdline-tools/latest/bin/sdkmanager" --sdk_root="$SDK" --licenses > /dev/null || true
  "$SDK/cmdline-tools/latest/bin/sdkmanager" --sdk_root="$SDK" "platforms;android-31" "build-tools;34.0.0"
fi

SRC=app/src/main
OUT=build
rm -rf "$OUT"
mkdir -p "$OUT/compiled" "$OUT/classes" "$OUT/dex"

echo "== language packs -> assets =="
mkdir -p "$SRC/assets/lang"
cp lang/*.txt "$SRC/assets/lang/"
ls "$SRC/assets/lang"

echo "== aapt2 compile + link =="
"$BT/aapt2" compile --dir "$SRC/res" -o "$OUT/compiled/res.zip"
"$BT/aapt2" link -o "$OUT/app-unsigned.apk" \
  -I "$PLATFORM" \
  -A "$SRC/assets" \
  --manifest "$SRC/AndroidManifest.xml" \
  --java "$OUT/gen" \
  --min-sdk-version 23 --target-sdk-version 28 \
  "$OUT/compiled/res.zip"

echo "== javac =="
find "$SRC/java" "$OUT/gen" -name '*.java' > "$OUT/sources.txt"
javac -source 8 -target 8 -encoding UTF-8 -classpath "$PLATFORM" -d "$OUT/classes" @"$OUT/sources.txt"

echo "== d8 =="
"$BT/d8" --lib "$PLATFORM" --output "$OUT/dex" $(find "$OUT/classes" -name '*.class')

echo "== package dex into apk =="
cp "$OUT/app-unsigned.apk" "$OUT/app-dex.apk"
cd "$OUT/dex" && zip -q ../app-dex.apk classes.dex && cd ../..

echo "== zipalign + sign =="
mkdir -p output
"$BT/zipalign" -f 4 "$OUT/app-dex.apk" "$OUT/app-aligned.apk"
if [ ! -f keystore.jks ]; then
  keytool -genkeypair -keystore keystore.jks -storepass n1launcher -keypass n1launcher \
    -alias n1 -keyalg RSA -keysize 2048 -validity 10000 \
    -dname "CN=N1 Launcher, O=Lakasa, C=VN" -noprompt
fi
"$BT/apksigner" sign --ks keystore.jks --ks-pass pass:n1launcher --key-pass pass:n1launcher \
  --v1-signing-enabled true --v2-signing-enabled true --v3-signing-enabled true \
  --out output/Lak-Launcher-v2.10.apk "$OUT/app-aligned.apk"

echo "== verify =="
"$BT/apksigner" verify --verbose output/Lak-Launcher-v2.10.apk
"$BT/aapt2" dump badging output/Lak-Launcher-v2.10.apk | head -5
sha256sum output/Lak-Launcher-v2.10.apk; ls -l output/Lak-Launcher-v2.10.apk
ls -lh output/
echo "BUILD OK"
