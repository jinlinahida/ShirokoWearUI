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

### Added — intro layer (`ui.intro`)

- `ShirokoWearVideoBackdrop`: full-bleed looping `MediaPlayer` + `SurfaceView`
  backdrop with an optional poster. The hard part is the asynchronous lifecycle, and
  it is encoded rather than re-learned per app: release flags stop any callback from
  touching a released player, `surfaceDestroyed` detaches the display, and every
  failure path (copy, IO, illegal state) leaves the poster visible because a
  backdrop must never blank a screen. With animations off, no player and no
  `SurfaceView` are created at all.
- `ShirokoWearVideoSource`: `CachedResource(resId, fileName)` / `FromFile` /
  `FromUri`. The clip itself never ships in the AAR — a multi-MB loop is one app's
  brand. `CachedResource` copies into **cacheDir**, deliberately not filesDir, so a
  decorative 10MB asset stays reclaimable; re-copying after eviction is the accepted
  cost. `content://` goes through the `setDataSource(Context, Uri)` overload.
- `isPlaybackEnabled` is a gate, not a hint: a Compose screen stays composed while
  its Activity is backgrounded, so without it a loop keeps decoding with nobody
  watching. The host passes its own resumed state — the library holds no lifecycle
  dependency.
- `ShirokoWearEnterPrompt`: the breathing dot + caption affordance (5dp dot, ±5dp at
  1100ms EaseInOutCubic, one-shot fade + half-height slide-in reveal). The float moves
  `translationY` in a graphics layer so it costs no layout pass, and the infinite
  transition is not created when animations are off.
- `ShirokoWearTapToAdvance`: full-bleed tap area with no ripple and no press-scale —
  the confirmation is haptic, because a visual reaction would fight the backdrop.
- `ShirokoWearGradientTitle` + pure `gradientAnnotatedString`: horizontal colour ramp
  wordmark. Gradient stops are required, never defaulted (brand colour is the app's
  identity); fewer than two stops degrades to plain text instead of an empty brush
  that renders nothing.

### Added — route contract, transitions and ambient light

- `shirokowear-navigation`: `ShirokoWearRoute` (`routeKey` / `depth` / `backKey`)
  replaces the 34-branch `AppScreen` switch the source app had baked into its
  motion code. A design system cannot know an app's screen enum; depth and
  parentage are the only facts a transition can be derived from.
- `resolveShirokoWearNavigationDirection(from, to, override)`: pure and total.
  Parentage beats depth numbers; an app-supplied override runs first, which is
  where a host says "entering onboarding from Settings is still a forward move"
  without the library knowing either screen exists.
- `shirokoWearPageTransition` (spatial continuity, incl. the `targetContentZIndex`
  rule that keeps a page from tearing at the seam on a round bezel),
  `shirokoWearLoadingContentTransition`, and the two named easings
  (`ShirokoWearEmphasizedDecelEasing`, `ShirokoWearAccelEasing`).
- `ShirokoWearAmbient` in `shirokowear`: top spotlight with 600ms per-route
  cross-fade and breathing that only runs while a task is in flight. Colour is
  resolved through `ShirokoWearAmbientPalette` (route key → colour, provided by
  the app), so renaming screens never touches the library.
- `ShirokoWearSpotlights`: the ramp the source app named after its screens
  (`TarotSpotlightColor`, `MuyuSpotlightColor`, `PulseSpotlightColor`) is now named
  by hue — violet / amber / teal / jade / emerald / sandwood — so a health app does
  not have to reach for a divination word to get a green light.

### Changed

- Unrelated routes at the same depth now transition as `LATERAL`. The source app
  returned `FORWARD` here, sliding a whole new page in for what is a peer change.
  A host that wants the old behaviour passes a direction override.

### Added — CI lanes, coverage gate, snapshot publication

- `ShirokoWearHapticWaveform.kt`: the gesture vocabulary, `ShirokoWearStep` /
  `ShirokoWearWaveform` and the waveform table moved into a file with **no
  `android.*` imports**. `toEffect()` and the vibrator/`VibrationAttributes`
  dispatch stayed in `ShirokoWearHaptics.kt`. This is a structural requirement of
  the coverage gate, not a style preference: while the table and the platform
  plumbing shared a file, the gate measured the motor driver too and fell to 73%.
- Coverage gate (Kover 0.9.11) on the deterministic core only —
  `ShirokoWearDimens*`, `ShirokoWearHapticWaveform*`, `ShirokoWearAmbientPalette*`,
  and `ShirokoWearRouteKt*` in the navigation module — at `minBound(80)` line
  coverage. Composables are excluded deliberately; a coverage number for them
  without a device would be theatre.
- Snapshot lane: `-PsnapshotPublication=true` flips the version to
  `mavenLocalVersion` and registers a GitHub Packages repository authenticated with
  the workflow's `GITHUB_TOKEN`, so trials of unfinished components never consume an
  immutable Central version number. Signing is skipped on this lane.
- Workflows: `verify.yml` (any branch / PR / `workflow_call`),
  `publish-snapshot.yml` (main), `release.yml` (`v*` tag →
  `publishAndReleaseToMavenCentral`, `-PpublicationVersion` derived from the tag).
  Both publication lanes declare `needs: verify`.
- `publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)` wired explicitly; the
  `mavenCentralPublishing` gradle property turned out to be a no-op on 0.30.0.

### Fixed

- `PaddingValues` assertions in tests used a non-existent `start`/`top` API and
  `Snapshot.withMutableSnapshot("name")`, which has no label parameter in this
  Compose version. Tests now use value equality on `PaddingValues`.
- Dropped a test that asserted *when* `derivedStateOf` invalidates: it passed on the
  debug variant and failed on the release variant of identical source, i.e. it was
  testing Compose's snapshot scheduler rather than this library. Replaced with
  assertions on the mappings `apply()` actually owns.
- `sample` module: token smoke screen that consumes every local.
- Publishing: `io.github.jinlinahida:shirokowear-ui` / `:shirokowear-navigation`
  via vanniktech maven-publish, full POM, Dokka javadoc + sources, signing gated on
  key availability, and a separate SNAPSHOT version line for `publishToMavenLocal`.

### Notes

- Verified by JVM tests (21 assertions, 0 failures): scale mapping, bezel geometry
  and runtime re-application (`ShirokoWearDimensTest`); waveform table validity,
  amplitude ordering and pulse structure across all 9 gestures x 3 intensities
  (`ShirokoWearHapticsTest`); palette resolution, fallback precedence and merge
  semantics (`ShirokoWearAmbientPaletteTest`); direction resolution incl.
  parentage-over-depth, same-route lateral and app override
  (`ShirokoWearRouteTest`).
- Verified by compilation only: the gallery APK and both AARs build, and the
  gallery consumes every public entry point.
- **Not verified**: crown scrolling, haptic waveforms, transition smoothness,
  round-bezel clipping and frame rate. No watch or Wear emulator was attached
  during these changes, so no claim is made about them (AGENTS.md verification
  policy).
- **Not verified** for the intro layer specifically: video decode/start/teardown on
  real hardware, the breathing dot's actual frame cost on a watch, and the reveal
  timing. Only `gradientAnnotatedString` is JVM-tested (3 assertions); the MediaPlayer
  and animation paths need a device.
- Not migrated yet from the source app's welcome screen: the step machine that hosts
  these pieces (awaiting a decision), the disclaimer/mode-selection step content, and
  the persisted "onboarding seen" flag — the last two are app state by design.

### Added — intro step machine

- `introStepDirection(fromStep, toStep)` (pure, in `ShirokoWearIntroDirection.kt`) and
  `ShirokoWearIntroSteps` (composable, in its own file): a linear onboarding flow is a
  hierarchy of its own kind, so advancing drills forward and returning pops back,
  reusing the same spatial story as route transitions. The host keeps owning the step
  index and each step's content — "onboarding seen" is app state.
- Direction derives from `TransitionScope.initialState`/`targetState`; there is no
  `prevState` on the transition scope, and `content` must be typed
  `@Composable AnimatedContentScope.(Int) -> Unit`, both learned from compiler errors
  rather than guessed correctly the first time.
- `sample` gains a third route (`intro`) exercising step machine + breathing prompt +
  tap-to-advance + gradient wordmark, with the video backdrop deliberately passed a
  null source: a clip is a brand asset and stays in the consuming app, so the gallery
  proves the API and the blank-degradation path, not the loop.
- Coverage gate lesson recorded in `AGENTS.md`: a gated file may contain neither
  `android.*` imports nor `@Composable` code. Adding `ShirokoWearIntroStepsKt*` to the
  filter dropped navigation coverage to 51.85% for the same reason haptics did at 44%
  — pure functions must live in their own file or the gate measures the wrong thing.
