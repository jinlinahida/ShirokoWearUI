package io.github.jinlinahida.shirokowear.ui

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * LRA drive strength, calibrated against wrist tissue rather than a phone palm.
 *
 * LIGHT is 8ms/170, STANDARD 12ms/235, STRONG 16ms/255 — see the haptics
 * backend for the concrete waveforms.
 */
public enum class ShirokoWearHapticIntensity {
    LIGHT,
    STANDARD,
    STRONG,
}

public val LocalShirokoWearColors: ProvidableCompositionLocal<ShirokoWearColors> =
    staticCompositionLocalOf { shirokoWearInkColors() }

public val LocalShirokoWearDimens: ProvidableCompositionLocal<ShirokoWearDimens> =
    staticCompositionLocalOf { shirokoWearDimens() }

public val LocalShirokoWearAnimationsEnabled: ProvidableCompositionLocal<Boolean> =
    staticCompositionLocalOf { true }

public val LocalShirokoWearRotaryScrollingEnabled: ProvidableCompositionLocal<Boolean> =
    staticCompositionLocalOf { true }

/**
 * Master switch for every tick in the system.
 *
 * Haptics must stay behind this and [LocalShirokoWearHapticIntensity] — a
 * component that fires the vibrator directly is a defect, because watch users
 * mute haptics deliberately.
 */
public val LocalShirokoWearHapticFeedbackEnabled: ProvidableCompositionLocal<Boolean> =
    staticCompositionLocalOf { true }

public val LocalShirokoWearHapticIntensity: ProvidableCompositionLocal<ShirokoWearHapticIntensity> =
    staticCompositionLocalOf { ShirokoWearHapticIntensity.STANDARD }
