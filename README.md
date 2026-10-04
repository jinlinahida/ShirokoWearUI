# ShirokoWear UI

Wear OS design system: motion, haptics, scrolling containers, card primitives and
visual canvases, extracted from a shipping Wear OS app so the feel survives across
products instead of being re-implemented per app.

Published on Maven Central as `io.github.jinlinahida:shirokowear-ui`.

## Get started

```kotlin
dependencies {
    // Replace <TAG> with the latest release from
    // https://central.sonatype.com/artifact/io.github.jinlinahida/shirokowear-ui
    implementation("io.github.jinlinahida:shirokowear-ui:<TAG>")
    implementation("io.github.jinlinahida:shirokowear-navigation:<TAG>")
}
```

```kotlin
@Composable
fun App() {
    ShirokoWearTheme(
        contentScale = ShirokoWearContentScale.STANDARD,
        screenShape = ShirokoWearScreenShape.ROUND,
        animationsEnabled = settings.animationsEnabled,
        rotaryScrollingEnabled = settings.rotaryScrollingEnabled,
        hapticFeedbackEnabled = settings.hapticFeedbackEnabled,
        hapticIntensity = settings.hapticIntensity,
    ) {
        // Every component below reads tokens and toggles from CompositionLocals,
        // so app settings never get re-threaded through 30 screens.
    }
}
```

## Compatibility

| ShirokoWear UI | Wear Compose | Jetpack Compose | compileSdk | minSdk |
|---|---|---|---|---|
| 0.1.x | 1.6.2 | 1.9.0 | 35 | 26 (Wear OS 2.0+) |

`androidx.wear.compose:compose-material3` 1.7.0 is already stable upstream; the
upgrade is deliberately a separate change, not folded into a component change.

## Modules

| Module | Artifact | Contains |
|---|---|---|
| `shirokowear` | `shirokowear-ui` | Tokens, haptics, cards, scrolling containers, wheel picker, loaders, canvases |
| `shirokowear-navigation` | `shirokowear-navigation` | `ShirokoWearRoute` contract, page transitions, ambient spotlight |
| `sample` | not published | Component gallery; this is the visual regression harness |

The library owns zero business semantics: no divination models, no card decks, no
string tables for domain content. Anything that names a hexagram, a tarot card or a
pulse belongs in the consuming app.

## Google Play and hidden platform APIs

The haptics backend dispatches vendor-specific constants (Samsung Galaxy Watch
`101` / `102` / `50107`) through `View.performHapticFeedback`, which is the same
route Google's own Wear Compose takes per device family. Apps that fail Play review
because of dependency reporting can disable it:

```kotlin
android {
    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }
}
```

The Galaxy Watch waveforms are HAL-dependent. On non-Samsung wearables the system
falls back to the standard Wear 4 constants (`18` / `19` / `20`) and then to
short-stroke one-shot pulses, so the tactile signature differs by device by design.

## Verification policy

Release notes state, per feature: what was verified by JVM tests, what was verified
on a specific real watch, and what was not verified at all. Bezel-specific behaviour
(crown scrolling, haptics, round layout clipping, frame rate) is never claimed without
a device.

## Licence

Apache-2.0, see `LICENSE`. Icons derived from Lucide (ISC) carry a `NOTICE` file at
the point they are ported.
