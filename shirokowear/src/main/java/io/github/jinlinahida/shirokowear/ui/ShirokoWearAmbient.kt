package io.github.jinlinahida.shirokowear.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

/**
 * Neutral spotlight ramp. Named by hue, not by screen: the source app had these
 * as `TarotSpotlightColor` / `MuyuSpotlightColor` / `PulseSpotlightColor`, which
 * made a health app reach for a divination word to get a green light.
 *
 * The default is the single definition of the theme's fallback spotlight, so the
 * literal cannot drift apart from [ShirokoWearColors.spotlightDefault].
 */
public object ShirokoWearSpotlights {
    public val default: Color = Color(0xFF64B5F6)
    public val amber: Color = Color(0xFFFFB74D)
    public val violet: Color = Color(0xFF9575CD)
    public val teal: Color = Color(0xFF4DB6AC)
    public val jade: Color = Color(0xFF80CBC4)
    public val emerald: Color = Color(0xFF00E5A3)
    public val sandwood: Color = Color(0xFFD4AF37)
}

/**
 * Maps a route's ambient key to a spotlight colour.
 *
 * The library owns the cross-fade, the app owns the vocabulary: a redesign that
 * renames screens must not require a design-system change. Unlisted keys fall back
 * to [ShirokoWearColors.spotlightDefault].
 */
public class ShirokoWearAmbientPalette(
    public val spotlights: Map<String, Color> = emptyMap(),
    public val fallback: Color? = null,
) {
    public fun resolve(key: String?, fallbackColor: Color): Color =
        (key?.let(spotlights::get) ?: fallback) ?: fallbackColor

    /**
     * Layer [other] on top of this palette. Kotlin map semantics apply: on a key
     * collision the incoming value wins, so an app can restyle one screen without
     * having to rebuild the whole map.
     */
    public fun merged(other: Map<String, Color>): ShirokoWearAmbientPalette =
        ShirokoWearAmbientPalette(spotlights + other, fallback)
}

public val LocalShirokoWearAmbientPalette: ProvidableCompositionLocal<ShirokoWearAmbientPalette> =
    staticCompositionLocalOf { ShirokoWearAmbientPalette() }

private const val SpotlightColorTransitionMs = 600
private const val SpotlightBreathingMs = 1200
private const val BreathingMinAlpha = 0.35f
private const val DefaultAmbientAlpha = 0.45f

/**
 * Ambient light hanging off the top edge of the bezel.
 *
 * Pure black is deliberate: an always-on watch saves real power on dead pixels. The
 * single radial disc is what translucent cards then let bleed through them, so a
 * screen reads as lit from above rather than as a stack of opaque boxes.
 *
 * The light recolours per route over 600ms and breathes only while work is in
 * flight. With animations off it holds steady, and the infinite transition is never
 * created rather than created and ignored.
 */
@Composable
public fun ShirokoWearAmbient(
    spotlightKey: String?,
    modifier: Modifier = Modifier,
    palette: ShirokoWearAmbientPalette = LocalShirokoWearAmbientPalette.current,
    isGenerating: Boolean = false,
    animationsEnabled: Boolean = ShirokoWearTheme.animationsEnabled,
    backgroundColor: Color = Color.Black,
    ambientAlpha: Float = DefaultAmbientAlpha,
    content: @Composable BoxScope.() -> Unit,
) {
    val targetColor = palette.resolve(spotlightKey, ShirokoWearTheme.colors.spotlightDefault)

    val animatedColor by if (animationsEnabled) {
        animateColorAsState(
            targetValue = targetColor,
            animationSpec = tween(
                durationMillis = SpotlightColorTransitionMs,
                easing = LinearOutSlowInEasing,
            ),
            label = "shirokoSpotlightColorTransition",
        )
    } else {
        remember(targetColor) { mutableStateOf(targetColor) }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "shirokoSpotlightBreathing")
    val breathingAlpha by if (animationsEnabled && isGenerating) {
        infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = BreathingMinAlpha,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = SpotlightBreathingMs, easing = EaseInOutCubic),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "shirokoSpotlightBreathingAlpha",
        )
    } else {
        remember { mutableFloatStateOf(1f) }
    }

    val currentAlpha = if (isGenerating) ambientAlpha * breathingAlpha else ambientAlpha
    val localDensity = LocalDensity.current
    var circleHeight by remember { mutableStateOf(0.dp) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor),
    ) {
        // One full width tall, pushed up by half its height: only the lower
        // hemisphere lands on screen, so it reads as a spread of light, not a disc.
        Box(
            modifier = Modifier
                .offset(y = circleHeight * -0.5f)
                .fillMaxWidth()
                .aspectRatio(1f)
                .alpha(currentAlpha)
                .background(
                    shape = CircleShape,
                    brush = Brush.radialGradient(listOf(animatedColor, Color.Transparent)),
                )
                .onSizeChanged {
                    circleHeight = with(localDensity) { it.height.toDp() }
                },
        )
        content()
    }
}
