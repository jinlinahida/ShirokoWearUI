# Changelog

All notable changes to ShirokoWear UI are documented here. Versions follow
`MAJOR.MINOR.PATCH` with `-alphaNN` / `-betaNN` pre-releases; breaking changes are
only allowed in pre-releases.

## 0.1.0-alpha01 (unreleased)

### Added

- `shirokowear` module: token layer built from scratch (the source app had no token
  system at all — colours were scattered literals).
  - `ShirokoWearTheme` composable + `ShirokoWearTheme.colors/dimens/animationsEnabled/`
    `rotaryScrollingEnabled/hapticFeedbackEnabled/hapticIntensity` accessors, so the
    four app-level toggles stop being threaded through every screen signature.
  - `ShirokoWearColors` (`shirokoWearInkColors()`): the hairline-sheen card quartet
    recovered from the source app — border `rgb(54,54,54)`, 30% alpha ink background,
    pink highlight ring, near-transparent outlined-button fill.
  - `ShirokoWearDimens` with state-backed fields plus `screenPadding` / `cardPadding`
    derived values, replacing the immutable metrics object that forced a whole-tree
    rebuild on every text-size change.
  - `ShirokoWearContentScale` keeps the source app's two separate axes: text scale
    0.90 / 1.00 / 1.10 and spacing scale 0.88 / 1.00 / 1.12. Merging them clips titles
    on 40mm round bezels.
  - `ShirokoWearScreenShape` round (20dp) vs square (16dp) horizontal inset and the
    title alignment rule that follows it.
  - `ShirokoWearShapes`, `ShirokoWearHapticIntensity`, `@UnstableShirokoWearApi`.
- `sample` module: token smoke screen that consumes every local.
- Publishing: `io.github.jinlinahida:shirokowear-ui` / `:shirokowear-navigation`
  via vanniktech maven-publish, full POM, Dokka javadoc + sources, signing gated on
  key availability, and a separate SNAPSHOT version line for `publishToMavenLocal`.

### Notes

- Verified by JVM tests: 5 assertions over scale mapping, bezel geometry and runtime
  re-application (`ShirokoWearDimensTest`).
- Not yet verified on a real watch: nothing user-visible in this release touches the
  crown, the vibrator or a bezel.
