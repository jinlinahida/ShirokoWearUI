package io.github.jinlinahida.shirokowear.ui

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * The scale factors below are the contract other apps depend on: boompala ships
 * 0.90/1.00/1.10 for text but 0.88/1.00/1.12 for spacing, and a title that
 * clips on a 40mm round bezel is the symptom of getting that wrong.
 */
public class ShirokoWearDimensTest {

    @Test
    public fun standardRoundMatchesBoompalaBaseline() {
        val dimens = shirokoWearDimens()
        assertEquals(20.dp, dimens.horizontalPadding)
        assertEquals(24.dp, dimens.verticalPadding)
        assertEquals(8.dp, dimens.itemSpacing)
        assertEquals(10.dp, dimens.cardVerticalPadding)
        assertEquals(0.4f.dp, dimens.cardBorderWidth)
    }

    @Test
    public fun squareBezelUsesTighterHorizontalInset() {
        val round = shirokoWearDimens(screenShape = ShirokoWearScreenShape.ROUND)
        val square = shirokoWearDimens(screenShape = ShirokoWearScreenShape.SQUARE)
        assertEquals(16.dp, square.horizontalPadding)
        assertEquals(round.verticalPadding, square.verticalPadding)
    }

    @Test
    public fun fontScaleAndDimenScaleAreIndependentAxes() {
        assertEquals(0.90f, ShirokoWearContentScale.SMALL.fontScale, 0f)
        assertEquals(0.88f, ShirokoWearContentScale.SMALL.dimenScale, 0f)
        assertEquals(1.10f, ShirokoWearContentScale.LARGE.fontScale, 0f)
        assertEquals(1.12f, ShirokoWearContentScale.LARGE.dimenScale, 0f)

        val large = shirokoWearDimens(ShirokoWearContentScale.LARGE)
        assertEquals(8.dp * 1.12f, large.itemSpacing)
    }

    @Test
    public fun titleAlignmentFollowsBezelShape() {
        assertEquals(
            true,
            shirokoWearDimens(screenShape = ShirokoWearScreenShape.ROUND).isRound,
        )
    }

    @Test
    public fun applyResolvesEverySpacingFromTheNewScale() {
        val dimens = shirokoWearDimens()
        dimens.apply(ShirokoWearContentScale.SMALL, ShirokoWearScreenShape.SQUARE)
        assertEquals(16.dp * 0.88f, dimens.horizontalPadding)
        assertEquals(ShirokoWearContentScale.SMALL, dimens.contentScale)
        assertEquals(ShirokoWearScreenShape.SQUARE, dimens.screenShape)
    }
}
