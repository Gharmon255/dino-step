#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

if [[ ! -f "$ROOT/keystore.properties" ]]; then
  echo "Missing keystore.properties at project root."
  echo "Run ./scripts/generate-upload-keystore.sh or create it manually."
  echo "See LAUNCH_CHECKLIST.md → Release signing & AAB build"
  exit 1
fi

echo "==> Bundle phone app (release AAB)"
./gradlew :app:bundleRelease

echo ""
echo "==> Bundle Wear OS app (release AAB)"
./gradlew :wear:bundleRelease

PHONE_AAB="$ROOT/app/build/outputs/bundle/release/app-release.aab"
WEAR_AAB="$ROOT/wear/build/outputs/bundle/release/wear-release.aab"

echo ""
echo "Done."
echo "  Phone: $PHONE_AAB"
echo "  Wear:  $WEAR_AAB"
echo ""
echo "Upload the phone AAB in Play Console → Internal testing."
echo "Upload the Wear AAB under Advanced settings → Wear OS if listing a watch app."
echo "Full checklist: LAUNCH_CHECKLIST.md"
