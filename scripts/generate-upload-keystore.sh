#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
KEYSTORE_DIR="${HOME}/upload-keystores"
KEYSTORE_FILE="${KEYSTORE_DIR}/dino-step-upload.jks"
ALIAS="dino-step-upload"

mkdir -p "${KEYSTORE_DIR}"

if [[ -f "${KEYSTORE_FILE}" ]]; then
  echo "Keystore already exists: ${KEYSTORE_FILE}"
  echo "Delete it first if you intend to create a new one."
  exit 1
fi

echo "Creating upload keystore at ${KEYSTORE_FILE}"
echo "You will be prompted for a keystore password and your name/org (for the cert)."
echo "Store both passwords in a password manager — loss blocks future Play updates."
echo ""

keytool -genkey -v \
  -keystore "${KEYSTORE_FILE}" \
  -alias "${ALIAS}" \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000

PROPS="${ROOT}/keystore.properties"
if [[ -f "${PROPS}" ]]; then
  echo ""
  echo "keystore.properties already exists — update storePassword/keyPassword manually."
else
  cp "${ROOT}/keystore.properties.example" "${PROPS}"
  if [[ "$(uname)" == "Darwin" ]]; then
    sed -i '' "s|storeFile=upload-keystores/dino-step-upload.jks|storeFile=${KEYSTORE_FILE}|" "${PROPS}"
  else
    sed -i "s|storeFile=upload-keystores/dino-step-upload.jks|storeFile=${KEYSTORE_FILE}|" "${PROPS}"
  fi
  echo ""
  echo "Created ${PROPS} — edit storePassword and keyPassword, then run:"
  echo "  cd ${ROOT} && ./gradlew bundleRelease"
fi
