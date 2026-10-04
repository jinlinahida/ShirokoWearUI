package io.github.jinlinahida.shirokowear.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

/**
 * Entry point of the design system.
 *
 * Toggles that every Wear app has to plumb through 30 screens (animations off,
 * rotary crown off, haptics muted) are provided as locals here instead, so a
 * component reads them instead of receiving another parameter.
 */
@Composable
public fun ShirokoWearTheme(
    colors: ShirokoWearColors = shirokoWearInkColors(),
    dimens: ShirokoWearDimens = shirokoWearDimens(),
    contentScale: ShirokoWearContentScale = dimens.contentScale,
    screenShape: ShirokoWearScreenShape = dimens.screenShape,
    animationsEnabled: Boolean = true,
    rotaryScrollingEnabled: Boolean = true,
    hapticFeedbackEnabled: Boolean = true,
    hapticIntensity: ShirokoWearHapticIntensity = ShirokoWearHapticIntensity.STANDARD,
    content: @Composable () -> Unit,
) {
    val resolvedDimens = if (
        dimens.contentScale == contentScale && dimens.screenShape == screenShape
    ) {
        dimens
    } else {
        shirokoWearDimens(contentScale, screenShape)
    }

    val baseDensity = LocalDensity.current
    val scaledDensity = remember(baseDensity.density, baseDensity.fontScale, contentScale) {
        Density(
            density = baseDensity.density,
            fontScale = baseDensity.fontScale * contentScale.fontScale,
        )
    }

    CompositionLocalProvider(
        LocalShirokoWearColors provides colors,
        LocalShirokoWearDimens provides resolvedDimens,
        LocalShirokoWearAnimationsEnabled provides animationsEnabled,
        LocalShirokoWearRotaryScrollingEnabled provides rotaryScrollingEnabled,
        LocalShirokoWearHapticFeedbackEnabled provides hapticFeedbackEnabled,
        LocalShirokoWearHapticIntensity provides hapticIntensity,
        LocalDensity provides scaledDensity,
        content = content,
    )
}

/** Token accessors for components: `ShirokoWearTheme.dimens.cardPadding`. */
public object ShirokoWearTheme {
    public val colors: ShirokoWearColors
        @Composable @ReadOnlyComposable get() = LocalShirokoWearColors.current

    public val dimens: ShirokoWearDimens
        @Composable @ReadOnlyComposable get() = LocalShirokoWearDimens.current

    public val animationsEnabled: Boolean
        @Composable @ReadOnlyComposable get() = LocalShirokoWearAnimationsEnabled.current

    public val rotaryScrollingEnabled: Boolean
        @Composable @ReadOnlyComposable get() = LocalShirokoWearRotaryScrollingEnabled.current

    public val hapticFeedbackEnabled: Boolean
        @Composable @ReadOnlyComposable get() = LocalShirokoWearHapticFeedbackEnabled.current

    public val hapticIntensity: ShirokoWearHapticIntensity
        @Composable @ReadOnlyComposable get() = LocalShirokoWearHapticIntensity.current
}
