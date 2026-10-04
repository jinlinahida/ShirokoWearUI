package io.github.jinlinahida.shirokowear.ui

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Palette resolution is a lookup, but it is the lookup that decides whether a
 * renamed route silently loses its ambient light.
 */
public class ShirokoWearAmbientPaletteTest {

    private val violet = Color(0xFF9575CD)
    private val amber = Color(0xFFFFB74D)
    private val fallback = Color.Red

    @Test
    public fun knownKeyResolvesToItsSpotlight() {
        val palette = ShirokoWearAmbientPalette(mapOf("tarot" to violet, "fortune" to amber))
        assertEquals(violet, palette.resolve("tarot", fallback))
        assertEquals(amber, palette.resolve("fortune", fallback))
    }

    @Test
    public fun unknownAndNullKeysFallBack() {
        val palette = ShirokoWearAmbientPalette(mapOf("tarot" to violet))
        assertEquals(fallback, palette.resolve("renamedScreen", fallback))
        assertEquals(fallback, palette.resolve(null, fallback))
    }

    @Test
    public fun paletteLevelFallbackBeatsThemeFallback() {
        val palette = ShirokoWearAmbientPalette(
            spotlights = mapOf("tarot" to violet),
            fallback = amber,
        )
        assertEquals(amber, palette.resolve("unknown", fallback))
        assertEquals(violet, palette.resolve("tarot", fallback))
    }

    /**
     * `merged` follows Kotlin map semantics: the incoming palette wins a key
     * collision. Anything else would be a hidden rule an app cannot predict.
     */
    @Test
    public fun mergedAddsNewKeysAndLetsTheIncomingPaletteWinCollisions() {
        val base = ShirokoWearAmbientPalette(mapOf("tarot" to violet))
        val merged = base.merged(mapOf("tarot" to amber, "pulse" to violet))

        assertEquals(amber, merged.resolve("tarot", fallback))
        assertEquals(violet, merged.resolve("pulse", fallback))
    }

    @Test
    public fun themeDefaultSpotlightIsTheRampDefault() {
        assertEquals(
            ShirokoWearSpotlights.default,
            shirokoWearInkColors().spotlightDefault,
        )
    }
}
