#!/usr/bin/env bash
set -euo pipefail

BT="${ANDROID_HOME:?ANDROID_HOME não definido}/build-tools/35.0.0"
APK="${1:-clone-x-dpc/app/build/outputs/apk/debug/app-debug.apk}"
test -s "$APK" || { echo "APK ausente: $APK"; exit 1; }

"$BT/aapt2" dump badging "$APK" > /tmp/clonex-badging.txt
grep -F "package: name='app.clonex.profile.v2'" /tmp/clonex-badging.txt
grep -F "application-label:'CLONE X'" /tmp/clonex-badging.txt
grep -F "sdkVersion:'28'" /tmp/clonex-badging.txt
grep -F "targetSdkVersion:'29'" /tmp/clonex-badging.txt

"$BT/aapt2" dump xmltree "$APK" --file AndroidManifest.xml > /tmp/clonex-manifest.txt
grep -F "app.clonex.profile.CloneXAdminReceiver" /tmp/clonex-manifest.txt
grep -F "android.permission.BIND_DEVICE_ADMIN" /tmp/clonex-manifest.txt
grep -F "android.app.device_admin" /tmp/clonex-manifest.txt
grep -F "android.software.managed_users" /tmp/clonex-manifest.txt
if grep -F "sharedUserId" /tmp/clonex-manifest.txt; then
  echo "ERRO: sharedUserId declarado"; exit 1
fi

"$BT/apksigner" verify --verbose "$APK"
"$BT/apksigner" verify --print-certs "$APK" | grep -F "SHA-256"

mapfile -t DEXES < <(unzip -Z1 "$APK" | grep -E '^classes([0-9]+)?\.dex$' || true)
if [ "${#DEXES[@]}" -eq 0 ]; then
  echo "ERRO: nenhum classes*.dex no APK"; exit 1
fi
rm -f /tmp/clonex_dex_*.txt
for i in "${!DEXES[@]}"; do
  unzip -p "$APK" "${DEXES[$i]}" | strings > "/tmp/clonex_dex_$i.txt"
done
for cls in \
  app.clonex.profile.MainActivity \
  app.clonex.profile.CloneXAdminReceiver
do
  descriptor="L${cls//.//};"
  grep -qF "$descriptor" /tmp/clonex_dex_*.txt || {
    echo "ERRO: classe ausente: $cls"; exit 1;
  }
done
echo "APK estruturalmente verificado: $APK"
