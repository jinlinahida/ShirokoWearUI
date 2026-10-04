package io.github.jinlinahida.shirokowear.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Covers the mapping and mutation logic this library actually owns.
 *
 * `screenPadding`/`cardPadding` are `derivedStateOf`, and asserting that a derived
 * value updates after a snapshot commit tests Compose's snapshot scheduler rather
 * than this design system — it passed on the debug variant and failed on the
 * release variant of the same source, which is exactly the kind of test that later
 * gets deleted for being "flaky". The raw Dp fields are what `apply()` owns, so
 * those are what gets pinned; the derived values are pinned only as pure mappings.
 */
public class ShirokoWearDimensDerivedTest {

    @Test
    public fun screenPaddingIsDerivedFromBezelAndVerticalPadding() {
        val dimens = shirokoWearDimens(
            ShirokoWearContentScale.STANDARD,
            ShirokoWearScreenShape.ROUND,
        )
        assertEquals(
            PaddingValues(horizontal = 20.dp, vertical = 24.dp),
            dimens.screenPadding,
        )
    }

    @Test
    public fun cardPaddingIsDerivedFromTheCardTokens() {
        val dimens = shirokoWearDimens(ShirokoWearContentScale.SMALL)
        assertEquals(
            PaddingValues(horizontal = 8.dp * 0.88f, vertical = 10.dp * 0.88f),
            dimens.cardPadding,
        )
    }

    @Test
    public fun applyingANewScaleRecalculatesEverySpacingToken() {
        val dimens = shirokoWearDimens(
            ShirokoWearContentScale.STANDARD,
            ShirokoWearScreenShape.ROUND,
        )
        dimens.apply(ShirokoWearContentScale.LARGE, ShirokoWearScreenShape.SQUARE)

        assertEquals(16.dp * 1.12f, dimens.horizontalPadding)
        assertEquals(24.dp * 1.12f, dimens.verticalPadding)
        assertEquals(8.dp * 1.12f, dimens.itemSpacing)
        assertEquals(10.dp * 1.12f, dimens.cardVerticalPadding)
        assertEquals(8.dp * 1.12f, dimens.cardInnerHorizontalPadding)
        assertEquals(ShirokoWearContentScale.LARGE, dimens.contentScale)
        assertEquals(ShirokoWearScreenShape.SQUARE, dimens.screenShape)
    }

    /** Border width and corner radius do not scale — a hairline stays a hairline. */
    @Test
    public fun hairlineBorderIsScaleInvariant() {
        val small = shirokoWearDimens(ShirokoWearContentScale.SMALL)
        val large = shirokoWearDimens(ShirokoWearContentScale.LARGE)
        assertEquals(0.4f.dp, small.cardBorderWidth)
        assertEquals(large.cardBorderWidth, small.cardBorderWidth)
    }

    @Test
    public fun titleAlignmentFollowsBezelShape() {
        assertEquals(
            TextAlign.Center,
            shirokoWearDimens(screenShape = ShirokoWearScreenShape.ROUND).titleTextAlign,
        )
        assertEquals(
            TextAlign.Start,
            shirokoWearDimens(screenShape = ShirokoWearScreenShape.SQUARE).titleTextAlign,
        )
    }
}
