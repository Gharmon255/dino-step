#!/usr/bin/env bash
#
# Deep save-integrity / lifecycle debug checks for the Android app.
#
# These are the "did a new build quietly break saves or the miss-a-day reset?" guardrails. They run
# as fast JVM unit tests (no emulator needed). Pass `--all` to also run the full unit-test suite.
#
# Usage:
#   scripts/run-debug-checks.sh          # just the deep debug suites
#   scripts/run-debug-checks.sh --all    # debug suites + every other unit test
#
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

DEBUG_FILTER='com.gharmon255.dinostep.debug.*'

echo "=============================================================="
echo " Dino Step — Android debug checks"
echo "=============================================================="
echo ""
echo "Scenarios covered:"
echo "  • Save survives an app update (Room entity + cloud round trips, legacy back-fill)"
echo "  • Room migration chain is unbroken 1 -> current (no wipe-on-update)"
echo "  • Miss a day -> dino resets to a 500-step egg -> regrows correctly"
echo "  • 150 seeded multi-day journeys with per-day invariant + save-integrity checks"
echo "  • Save round-trip fuzzing over thousands of random states (auto-catches dropped fields)"
echo "  • Growth economy thresholds + reward-roll table"
echo "  • Promo codes are one-time-use on-device"
echo "  • Cross-platform constant parity (must match iOS)"
echo ""

if [[ "${1:-}" == "--all" ]]; then
  echo "==> Running ALL :app unit tests (including debug suites)"
  ./gradlew :app:testDebugUnitTest --console=plain
else
  echo "==> Running debug suites only ($DEBUG_FILTER)"
  ./gradlew :app:testDebugUnitTest --tests "$DEBUG_FILTER" --console=plain
fi

echo ""
echo "HTML report: app/build/reports/tests/testDebugUnitTest/index.html"
echo "All debug checks passed."
