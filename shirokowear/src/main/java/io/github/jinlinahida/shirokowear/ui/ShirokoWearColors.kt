package io.github.jinlinahida.shirokowear.ui

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color

/**
 * Every colour the design system owns, resolved once and shared through
 * [LocalShirokoWearColors].
 *
 * The card quartet is the Wear-specific "frosted ink" surface this system exists
 * to preserve: a hairline diagonal sheen border over a 30% alpha grey, so the
 * ambient spotlight behind a card keeps bleeding through it.
 */
@Stable
public class ShirokoWearColors(
    cardBorder: Color,
    cardBackground: Color,
    cardHighlight: Color,
    outlineButtonBackground: Color,
    contentPrimary: Color,
    contentDisabled: Color,
    spotlightDefault: Color,
    accentCopper: Color,
    accentGold: Color,
    wheelSlotBackground: Color,
    waveformLine: Color = ShirokoWearSpotlights.emerald,
) {
    public val cardBorder: Color by mutableStateOf(cardBorder)
    public val cardBackground: Color by mutableStateOf(cardBackground)

    /** Pink-red ring used when a card is marked as current or selected. */
    public val cardHighlight: Color by mutableStateOf(cardHighlight)

    /** Near-transparent fill for outlined buttons (half of [cardBackground]). */
    public val outlineButtonBackground: Color by mutableStateOf(outlineButtonBackground)
    public val contentPrimary: Color by mutableStateOf(contentPrimary)
    public val contentDisabled: Color by mutableStateOf(contentDisabled)

    /** Fallback ambient light colour when a route declares no spotlight. */
    public val spotlightDefault: Color by mutableStateOf(spotlightDefault)

    /** Brushed brass: wheel slot edge, mechanical affordances. */
    public val accentCopper: Color by mutableStateOf(accentCopper)

    /** Selected-wheel text and other "this one is chosen" marks. */
    public val accentGold: Color by mutableStateOf(accentGold)

    /** The recessed trough a wheel picker highlights the active row inside. */
    public val wheelSlotBackground: Color by mutableStateOf(wheelSlotBackground)

    /** High-precision physiological pulse / sensor data line color. */
    public val waveformLine: Color by mutableStateOf(waveformLine)

    public fun copy(
        cardBorder: Color = this.cardBorder,
        cardBackground: Color = this.cardBackground,
        cardHighlight: Color = this.cardHighlight,
        outlineButtonBackground: Color = this.outlineButtonBackground,
        contentPrimary: Color = this.contentPrimary,
        contentDisabled: Color = this.contentDisabled,
        spotlightDefault: Color = this.spotlightDefault,
        accentCopper: Color = this.accentCopper,
        accentGold: Color = this.accentGold,
        wheelSlotBackground: Color = this.wheelSlotBackground,
        waveformLine: Color = this.waveformLine,
    ): ShirokoWearColors = ShirokoWearColors(
        cardBorder = cardBorder,
        cardBackground = cardBackground,
        cardHighlight = cardHighlight,
        outlineButtonBackground = outlineButtonBackground,
        contentPrimary = contentPrimary,
        contentDisabled = contentDisabled,
        spotlightDefault = spotlightDefault,
        accentCopper = accentCopper,
        accentGold = accentGold,
        wheelSlotBackground = wheelSlotBackground,
        waveformLine = waveformLine,
    )
}

/** The ink-on-black palette the system ships with. */
public fun shirokoWearInkColors(): ShirokoWearColors = ShirokoWearColors(
    cardBorder = Color(54, 54, 54, 255),
    cardBackground = Color(38, 38, 38, 77),
    cardHighlight = Color(231, 86, 136, 255),
    outlineButtonBackground = Color(38, 38, 38, 38),
    contentPrimary = Color.White,
    contentDisabled = Color.White.copy(alpha = 0.38f),
    spotlightDefault = ShirokoWearSpotlights.default,
    accentCopper = Color(0xFFC5A059),
    accentGold = Color(0xFFFFD700),
    wheelSlotBackground = Color(0xFF221E18),
    waveformLine = ShirokoWearSpotlights.emerald,
)
