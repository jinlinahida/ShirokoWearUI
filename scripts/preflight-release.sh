#!/usr/bin/env bash
# Pre-flight check for a Maven Central release: proves, locally and without
# contacting Central, that Gradle can read your signing key material and produce
# signed artefacts. Run this BEFORE `git tag` — a failed release attempt is cheap,
# but debugging one through CI logs costs a full round trip.
#
#   ./scripts/preflight-release.sh <KEYID>
#
# Expects the secret key to live in your local keyring. Nothing is uploaded and
# nothing is written outside a temp file plus the local Maven cache.
set -euo pipefail

KEYID="${1:?usage: preflight-release.sh <8-hex-key-id>}"
REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
TMPKEY="$(mktemp -t swkey.XXXXXX)"
trap 'rm -f "$TMPKEY"' EXIT

cd "$REPO_ROOT"

echo "==> exporting secret key $KEYID (gpg may prompt for its passphrase)"
gpg --pinentry-mode loopback --export-secret-keys --armor "$KEYID" > "$TMPKEY"

if ! head -1 "$TMPKEY" | grep -q '^-----BEGIN PGP PRIVATE KEY BLOCK'; then
  echo "FAIL: export is not an armored PRIVATE key block — wrong id, or public key?" >&2
  exit 1
fi

echo "==> reading it through the same path CI uses (publishToMavenLocal + signing)"
ORG_GRADLE_PROJECT_signingInMemoryKey="$(cat "$TMPKEY")" \
ORG_GRADLE_PROJECT_signingInMemoryKeyId="$KEYID" \
ORG_GRADLE_PROJECT_signingInMemoryKeyPassword="${SIGNING_KEY_PASSWORD:-}" \
  ./gradlew --no-daemon -PpublicationVersion=0.0.0-preflight \
    :shirokowear:publishToMavenLocal :shirokowear-navigation:publishToMavenLocal

OUT=~/.m2/repository/io/github/jinlinahida/shirokowear-ui/0.0.0-preflight
if ls "$OUT"/*.asc >/dev/null 2>&1; then
  echo "PASS: Gradle signed the artefacts; CI has a readable key."
  echo "      remember to delete the probe artefacts:"
  echo "      rm -rf ~/.m2/repository/io/github/jinlinahida/*/0.0.0-preflight"
else
  echo "FAIL: no .asc produced — bouncycastle could not sign." >&2
  exit 1
fi
