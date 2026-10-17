#!/usr/bin/env bash
#
# Populate the GitHub Actions secrets that .github/workflows/deploy-play-store.yml
# needs. Reads every value from your local, untracked files -- nothing secret is
# stored in this script or printed to the terminal.
#
# Prerequisites: gh auth login, and a local.properties with the signing config.
# Usage: bash scripts/setup-ci-secrets.sh
#
set -euo pipefail

cd "$(dirname "$0")/.."

prop() {
  # Read a key from local.properties and un-escape the Java .properties form
  # (C\:\\Users\\... -> C:\Users\...).
  grep -m1 "^$1=" local.properties \
    | cut -d= -f2- \
    | tr -d '\r' \
    | sed 's/\\:/:/g; s/\\\\/\\/g'
}

set_secret() {
  local name="$1" value="$2"
  if [ -z "$value" ]; then
    echo "SKIP  $name (no local value found)" >&2
    return
  fi
  printf '%s' "$value" | gh secret set "$name"
  echo "OK    $name"
}

KEYSTORE_PATH="$(prop keystore.file)"
[ -f "$KEYSTORE_PATH" ] || { echo "Keystore not found at the path in local.properties" >&2; exit 1; }
PLAY_KEY="fastlane/play-store-key.json"
[ -f "$PLAY_KEY" ] || { echo "Missing $PLAY_KEY" >&2; exit 1; }

# Signing
set_secret KEYSTORE_BASE64   "$(base64 -w0 "$KEYSTORE_PATH" 2>/dev/null || base64 "$KEYSTORE_PATH" | tr -d '\n')"
set_secret KEYSTORE_PASSWORD "$(prop keystore.password)"
set_secret KEY_ALIAS         "$(prop key.alias)"
set_secret KEY_PASSWORD      "$(prop key.password)"

# Play Store service account
set_secret PLAY_STORE_JSON_KEY "$(cat "$PLAY_KEY")"

# App build config. GitHub refuses secret names starting with GITHUB_, hence APP_.
set_secret APP_GITHUB_TOKEN         "$(prop github.token)"
set_secret APP_GITHUB_CLIENT_ID     "$(prop github.client.id)"
set_secret APP_GITHUB_CLIENT_SECRET "$(prop github.client.secret)"

echo
echo "Done. Verify with: gh secret list"
echo "Then a 'v*' tag push will deploy to the internal track."
