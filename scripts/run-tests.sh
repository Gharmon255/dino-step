#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

echo "==> Android unit tests (:shared + :app)"
./gradlew :shared:testDebugUnitTest :app:testDebugUnitTest

echo ""
echo "==> Android instrumented tests require a running emulator/device."
echo "    Run manually when ready:"
echo "    ./gradlew :app:connectedDebugAndroidTest"

echo ""
echo "All JVM unit tests passed."
