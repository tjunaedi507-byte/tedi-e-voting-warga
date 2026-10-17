#!/data/data/com.termux/files/usr/bin/bash
set -eu

cd "$(dirname "$0")"

SDK="$HOME/android-sdk"
PLATFORM="$SDK/platforms/android-35/android.jar"
BT="$SDK/build-tools/34.0.4"
PKG="id.tediberkah.evotingwarga"
SRC="src/id/tediberkah/evotingwarga/MainActivity.java"
OUT="build"

echo "=== 1. PERIKSA ALAT ==="

for f in "$PLATFORM" "$BT/aapt" "$BT/d8" "$BT/apksigner" "$BT/zipalign"; do
    if [ ! -f "$f" ]; then
        echo "GAGAL: alat tidak ditemukan: $f"
        exit 1
    fi
done

command -v javac >/dev/null
command -v keytool >/dev/null
command -v zip >/dev/null

mkdir -p "$OUT/classes" "$OUT/dex" res

echo "=== 2. KOMPILASI JAVA ==="

javac \
    -source 8 \
    -target 8 \
    -classpath "$PLATFORM" \
    -d "$OUT/classes" \
    "$SRC"

echo "=== 3. BUAT DEX ==="

"$BT/d8" \
    --lib "$PLATFORM" \
    --min-api 23 \
    --output "$OUT/dex" \
    "$OUT/classes/id/tediberkah/evotingwarga/MainActivity.class"

echo "=== 4. BUAT APK DASAR ==="

aapt2 link \
    -o "$OUT/base.apk" \
    --manifest AndroidManifest.xml \
    -I "$PLATFORM" \
    --min-sdk-version 23 \
    --target-sdk-version 28

echo "=== 5. MASUKKAN KODE DEX ==="

cp "$OUT/base.apk" "$OUT/unsigned.apk"
cd "$OUT/dex"
zip -q -j ../unsigned.apk classes.dex
cd ..

echo "=== 6. SELARASKAN APK ==="

"$BT/zipalign" -f 4 unsigned.apk aligned.apk

echo "=== 7. SIAPKAN KUNCI PENGUJIAN ==="

if [ ! -f tedi-debug.keystore ]; then
    keytool -genkeypair \
        -keystore tedi-debug.keystore \
        -storepass android \
        -keypass android \
        -alias tedi \
        -keyalg RSA \
        -keysize 2048 \
        -validity 3650 \
        -dname "CN=TEDI BERKAH, OU=Testing, O=TEDI, C=ID"
fi

echo "=== 8. TANDA TANGANI APK ==="

"$BT/apksigner" sign \
    --ks tedi-debug.keystore \
    --ks-pass pass:android \
    --key-pass pass:android \
    --ks-key-alias tedi \
    --out tedi-e-voting-warga.apk \
    aligned.apk

echo "=== 9. VERIFIKASI APK ==="

"$BT/apksigner" verify --verbose tedi-e-voting-warga.apk

echo
echo "SELESAI"
echo "APK: $(pwd)/tedi-e-voting-warga.apk"
ls -lh tedi-e-voting-warga.apk
