package io.github.jinlinahida.shirokowear.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.res.stringResource

/**
 * LRA drive strength, calibrated against wrist tissue rather than a phone palm.
 *
 * LIGHT is 8ms/170, STANDARD 12ms/235, STRONG 16ms/255 — see the haptics
 * backend for the concrete waveforms.
 *
 * [labelRes] exists because a settings row needs to show this level in the user's
 * language, and a design system that hardcodes "弱/标准/强劲" in code can't be
 * localised by its host. [fromPersisted] tolerates whatever an app already wrote to
 * disk (enum name, or a previously stored label) instead of resetting silently.
 */
public enum class ShirokoWearHapticIntensity(
    @androidx.annotation.StringRes public val labelRes: Int,
) {
    LIGHT(R.string.shirokowear_haptic_light),
    STANDARD(R.string.shirokowear_haptic_standard),
    STRONG(R.string.shirokowear_haptic_strong),
    ;

    public companion object {
        /**
         * Resolves a persisted value tolerantly: enum name (any case), the localised
         * label, or the ordinal — anything unrecognisable becomes STANDARD.
         */
        public fun fromPersisted(
            value: String?,
            labelsByValue: Map<ShirokoWearHapticIntensity, String> = emptyMap(),
        ): ShirokoWearHapticIntensity {
            val key = value?.trim()?.lowercase() ?: return STANDARD
            entries.firstOrNull { it.name.lowercase() == key }?.let { return it }
            entries.firstOrNull { it.ordinal.toString() == key }?.let { return it }
            labelsByValue.entries.firstOrNull { it.value.trim().lowercase() == key }
                ?.let { return it.key }
            return STANDARD
        }
    }
}

/** Localised display label for a haptic level, e.g. for a settings row. */
@Composable
public fun ShirokoWearHapticIntensity.displayName(): String = stringResource(id = labelRes)

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
