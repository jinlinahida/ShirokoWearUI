# Releasing ShirokoWear UI

Public, non-secret facts about the release pipeline. Everything here is safe to read:
key ids and fingerprints are published on keyservers anyway.

## Coordinates

| module | artifact | group |
|---|---|---|
| `:shirokowear` | `io.github.jinlinahida:shirokowear-ui` | `io.github.jinlinahida` |
| `:shirokowear-navigation` | `io.github.jinlinahida:shirokowear-navigation` | `io.github.jinlinahida` |

Version source of truth: `gradle/libs.versions.toml` → `version` (release line) and
`mavenLocalVersion` (local + snapshot line). A tag `vX.Y.Z` must match `version`.

## Signing key (public identifiers only)

| item | value |
|---|---|
| key id (`SIGNING_KEY_ID`) | `F89469A1` |
| full fingerprint | `3E303D5C2AA8C8A4CE6EBB720D4D10E7F89469A1` |
| uid | `Glorious Aster <jinlinahida@gmail.com>` |
| public key lookup | `https://keyserver.ubuntu.com/pks/lookup?op=get&search=0x3E303D5C2AA8C8A4CE6EBB720D4D10E7F89469A1` |

If CI fails with `Could not read PGP secret key`, check in this order:

1. `SIGNING_KEY_ID` equals `F89469A1` (8 hex, last group of the fingerprint). A wrong id
   selects no key and produces exactly that message — this bit us on the first attempt.
2. `SIGNING_IN_MEMORY_KEY` is the *complete* ASCII-armored **private** block from
   `gpg --export-secret-keys --armor F89469A1`, including both `-----BEGIN/END PGP
   PRIVATE KEY BLOCK-----` lines and the blank line between the armor header and the body.
   An armored *public* key pasted here fails the same way.
3. `SIGNING_KEY_PASSWORD` is the key's real passphrase (empty string only if the key has
   none).
4. Only then consider toolchain limits: GnuPG ≥ 2.4 wraps passphrase-protected secret
   keys with AEAD (RFC 9580) and Bouncy Castle — which backs Gradle signing — has
   historically failed to parse those. The control experiment that narrows it: a key
   created with `%no-protection` signs fine with the same Gradle version on the same
   machine.

## Required checks before a tag

```bash
./scripts/preflight-release.sh F89469A1
```

Runs the same code path CI uses (in-memory signing + `publishToMavenLocal`, throwaway
version `0.0.0-preflight`) and fails in seconds instead of burning a release attempt.
Delete the probe afterwards:

```bash
rm -rf ~/.m2/repository/io/github/jinlinahida/*/0.0.0-preflight
```

## Release paths

| trigger | result |
|---|---|
| push to `main` | `Verify` + GitHub Packages `-SNAPSHOT` (overwriteable, unsigned) |
| tag `vX.Y.Z` | `Verify` → `publishAndReleaseToMavenCentral` (signed, **immutable**) |
| manual dispatch | same as the tag path; version comes from the `version` input, or the catalog when left empty |

A failed release does **not** consume the version number: Central only reserves it on a
successful deployment. Re-running the failed workflow picks up the current secrets, so a
misconfigured secret needs no tag gymnastics to retry.

Gating (see `AGENTS.md`): `-alphaNN` / `-betaNN` may ship on a green CI while device
behaviour is unverified; an unsuffixed `x.y.z` additionally requires a consuming app on
the source track and per-model hardware sign-off recorded in the release notes.

## Post-release verification

Do not trust the Actions tick. Query the repository:

```bash
for a in shirokowear-ui shirokowear-navigation; do
  curl -s -o /dev/null -w "%{http_code} $a\n" \
    https://repo1.maven.org/maven2/io/github/jinlinahida/$a/<VERSION>/$a-<VERSION>.pom
done
```

`200` on the `.pom` (and ideally the matching `.pom.asc`) is the real signal that
downstream builds — including `boompala` switching off `includeBuild` — can resolve it.
Synchronisation from "published" to "downloadable" usually takes 10-30 minutes.

## Other secrets (names only; values are never committed)

`MAVEN_CENTRAL_USERNAME`, `MAVEN_CENTRAL_PASSWORD` — a Central Portal *user token* pair
(not the login email/password; the username looks like `xxxx:yyyy...`),
`SIGNING_IN_MEMORY_KEY`, `SIGNING_KEY_ID`, `SIGNING_KEY_PASSWORD`.
