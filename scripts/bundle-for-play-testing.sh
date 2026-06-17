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
echo "Upload in Play Console → Internal testing (one release):"
echo "  1. Phone AAB → main App bundles slot (phones/tablets)."
echo "  2. Wear AAB → Advanced settings → Wear OS only (NOT the phone slot)."
echo ""
echo "If testers see \"Your device isn't compatible\":"
echo "  - Latest release is missing the phone AAB, or wear was uploaded to the phone slot."
echo "  - Phone and wear share applicationId; wear-only bundles exclude phones."
echo "Full checklist: LAUNCH_CHECKLIST.md"
