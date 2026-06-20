#!/usr/bin/env bash
# Verify hosted privacy policies include current disclosure text.
# Run after pushing docs/privacy-policy.html to main (GitHub Pages).
set -euo pipefail

ANDROID_URL="${ANDROID_PRIVACY_URL:-https://gharmon255.github.io/dino-step/privacy-policy.html}"
IOS_URL="${IOS_PRIVACY_URL:-https://gharmon255.github.io/dino-step-ios/privacy-policy.html}"

check_url() {
  local name="$1"
  local url="$2"
  echo "Checking $name: $url"
  local body
  body="$(curl -fsSL "$url")"
  local missing=0
  for phrase in "once per hour" "Optional cloud backup" "friend battles" "support@gharmon255.dev"; do
    if ! grep -qi "$phrase" <<< "$body"; then
      echo "  MISSING: $phrase"
      missing=$((missing + 1))
    fi
  done
  if [[ "$missing" -eq 0 ]]; then
    echo "  OK"
  else
    echo "  FAIL ($missing phrases missing) — push main and wait for GitHub Pages, or update hosted HTML"
    return 1
  fi
}

fail=0
check_url "Android policy" "$ANDROID_URL" || fail=1
check_url "iOS policy" "$IOS_URL" || fail=1
exit "$fail"
