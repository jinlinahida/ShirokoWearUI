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

### Added — tactile and container primitives

- Haptics: `ShirokoWearHaptics` direct-LRA engine with a pure waveform table
  (`shirokoWearWaveform(kind, intensity)`), vendor constant mapping
  (`ShirokoWearHapticVendorConstants`: Galaxy Watch `101`/`102`/`50107`, Wear 4
  `18`/`19`/`20`, then standard Android fallback), and
  `rememberShirokoWearHaptics()` which reads the intensity and mute gate from the
  theme so no call site can fire the motor behind a muted user's back.
- Gesture vocabulary is physical, not domain-specific: `click`, `toggle`, `flip`,
  `back`, `knock`, `pulseWave`, `impact(multiple)`. The source app's `coinToss`
  maps to `impact`, `muyuTap` to `knock`, `pulseBeat` to `pulseWave`.
- Press physics: `Modifier.clickVfx` (0.9x, 150ms, two overloads incl. long click)
  and `Modifier.pressFeedback` (spring 0.5f damping / 300f stiffness to 0.96x).
  Both haptic on `PressInteraction.Release` only — a slide-to-scroll that starts
  on a card must not click, and dispatching on press costs the first scroll frame.
- `Modifier.wearMarquee`: no animation at all when the text fits, 1.2s dwell at
  each end when it does not.
- Cards: `ShirokoWearCard` (the two source overloads merged into one
  `highlighted: Boolean`) and `ShirokoWearResultCard`.
- Buttons: `ShirokoWearButtonDefaults`, `ShirokoWearCardButton`,
  `ShirokoWearSelectableButton` (1.2dp ring + 18% tint, 200ms crossfade).
- Text: `ShirokoWearScreenTitle` (bezel-driven alignment) and
  `ShirokoWearDetailField`.
- Containers: `ShirokoWearRotaryColumn`, `ShirokoWearScalingRotaryColumn`
  (fish-eye flattens to 1.0 when animations are off; the crown keeps working).
- `ShirokoWearWheelPicker<T>`: snap-detent wheel with copper slot; its previously
  duplicated literals are now tokens (`accentCopper`, `accentGold`,
  `wheelSlotBackground`).
- `ShirokoWearMorphingLoader` + `ShirokoWearLoadingIndicator` (Material 3
  Expressive sparkle → circle → scallop → squircle, 2400ms cycle).
- `sample` is now a component gallery, not a smoke screen.
- `sample` module: token smoke screen that consumes every local.
- Publishing: `io.github.jinlinahida:shirokowear-ui` / `:shirokowear-navigation`
  via vanniktech maven-publish, full POM, Dokka javadoc + sources, signing gated on
  key availability, and a separate SNAPSHOT version line for `publishToMavenLocal`.

### Notes

- Verified by JVM tests (10 assertions, 0 failures): scale mapping, bezel geometry
  and runtime re-application (`ShirokoWearDimensTest`); waveform table validity,
  amplitude ordering and pulse structure across all 9 gestures x 3 intensities
  (`ShirokoWearHapticsTest`).
- Verified by compilation only: the gallery APK builds and consumes every public
  entry point.
- **Not verified**: crown scrolling, haptic waveforms, round-bezel clipping and
  frame rate. No watch or Wear emulator was attached during this change, so no
  claim is made about them (AGENTS.md verification policy).
