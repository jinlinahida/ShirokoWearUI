package io.github.jinlinahida.shirokowear.ui

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Ported from the source app's own test, where this rule lived beside the widget.
 *
 * The rule is worth pinning because it is a fallback, and fallbacks rot silently:
 * a user who turns animations off must still get a scrollable list, just without
 * fish-eye scaling and edge fade. Flat 1.0/1.0 is the assertion that proves the
 * degradation path was not "optimized" into also disabling scrolling.
 */
public class ShirokoWearScalingParamsTest {

    @Test
    public fun animationsOnUseStandardFishEyeScaling() {
        val params = shirokoWearScalingParams(animationsEnabled = true)
        assertEquals(0.7f, params.edgeScale, 0.01f)
        assertEquals(0.5f, params.edgeAlpha, 0.01f)
    }

    @Test
    public fun animationsOffFlattenEdgeScalingAndAlpha() {
        val params = shirokoWearScalingParams(animationsEnabled = false)
        assertEquals(1.0f, params.edgeScale, 0.001f)
        assertEquals(1.0f, params.edgeAlpha, 0.001f)
    }
}
