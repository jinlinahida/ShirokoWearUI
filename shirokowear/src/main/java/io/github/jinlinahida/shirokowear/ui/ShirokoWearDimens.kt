package io.github.jinlinahida.shirokowear.ui

import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.structuralEqualityPolicy
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.text.style.TextAlign

/** Physical bezel shape of the target Wear device. */
public enum class ShirokoWearScreenShape {
    ROUND,
    SQUARE,
}

/**
 * User-facing text size setting.
 *
 * [fontScale] and [dimenScale] are deliberately different numbers: raising the
 * font by 10% is not the same as inflating every padding by 12%, and conflating
 * them makes 40mm round screens clip titles.
 */
public enum class ShirokoWearContentScale(
    public val fontScale: Float,
    public val dimenScale: Float,
) {
    SMALL(0.90f, 0.88f),
    STANDARD(1.00f, 1.00f),
    LARGE(1.10f, 1.12f),
}

/**
 * Spacing tokens for the current content scale and screen shape.
 *
 * Fields are state-backed so a single value can be nudged at runtime (a denser
 * settings list, a wider card gutter) without rebuilding the theme.
 */
@Stable
public class ShirokoWearDimens(
    contentScale: ShirokoWearContentScale,
    screenShape: ShirokoWearScreenShape,
    horizontalPadding: Dp,
    verticalPadding: Dp,
    itemSpacing: Dp,
    cardVerticalPadding: Dp,
    cardInnerHorizontalPadding: Dp,
    cardBorderWidth: Dp,
) {
    public var contentScale: ShirokoWearContentScale
        by mutableStateOf(contentScale, structuralEqualityPolicy())
        internal set

    public var screenShape: ShirokoWearScreenShape
        by mutableStateOf(screenShape, structuralEqualityPolicy())
        internal set

    public var horizontalPadding: Dp
        by mutableStateOf(horizontalPadding, structuralEqualityPolicy())
        internal set

    public var verticalPadding: Dp
        by mutableStateOf(verticalPadding, structuralEqualityPolicy())
        internal set

    public var itemSpacing: Dp
        by mutableStateOf(itemSpacing, structuralEqualityPolicy())
        internal set

    public var cardVerticalPadding: Dp
        by mutableStateOf(cardVerticalPadding, structuralEqualityPolicy())
        internal set

    public var cardInnerHorizontalPadding: Dp
        by mutableStateOf(cardInnerHorizontalPadding, structuralEqualityPolicy())
        internal set

    public var cardBorderWidth: Dp
        by mutableStateOf(cardBorderWidth, structuralEqualityPolicy())
        internal set

    public val isRound: Boolean get() = screenShape == ShirokoWearScreenShape.ROUND

    /** Round bezels centre titles; square bezels align them to the reading edge. */
    public val titleTextAlign: TextAlign
        get() = if (isRound) TextAlign.Center else TextAlign.Start

    public val screenPadding: PaddingValues by derivedStateOf {
        PaddingValues(
            horizontal = horizontalPadding,
            vertical = verticalPadding,
        )
    }

    public val cardPadding: PaddingValues by derivedStateOf {
        PaddingValues(
            horizontal = cardInnerHorizontalPadding,
            vertical = cardVerticalPadding,
        )
    }

    /** Applies [contentScale] and [screenShape] and recomputes every spacing. */
    public fun apply(
        contentScale: ShirokoWearContentScale,
        screenShape: ShirokoWearScreenShape,
    ): ShirokoWearDimens {
        val resolved = shirokoWearDimens(contentScale, screenShape)
        this.contentScale = contentScale
        this.screenShape = screenShape
        horizontalPadding = resolved.horizontalPadding
        verticalPadding = resolved.verticalPadding
        itemSpacing = resolved.itemSpacing
        cardVerticalPadding = resolved.cardVerticalPadding
        cardInnerHorizontalPadding = resolved.cardInnerHorizontalPadding
        return this
    }
}

private const val RoundHorizontalBaseDp: Float = 20f
private const val SquareHorizontalBaseDp: Float = 16f
private const val VerticalBaseDp: Float = 24f
private const val ItemSpacingBaseDp: Float = 8f
private const val CardVerticalBaseDp: Float = 10f
private const val CardInnerHorizontalBaseDp: Float = 8f

/** Round bezels need more horizontal inset than square ones at the same text size. */
public fun shirokoWearDimens(
    contentScale: ShirokoWearContentScale = ShirokoWearContentScale.STANDARD,
    screenShape: ShirokoWearScreenShape = ShirokoWearScreenShape.ROUND,
): ShirokoWearDimens {
    val scale = contentScale.dimenScale
    val horizontalBase = when (screenShape) {
        ShirokoWearScreenShape.ROUND -> RoundHorizontalBaseDp
        ShirokoWearScreenShape.SQUARE -> SquareHorizontalBaseDp
    }
    return ShirokoWearDimens(
        contentScale = contentScale,
        screenShape = screenShape,
        horizontalPadding = horizontalBase.dp * scale,
        verticalPadding = VerticalBaseDp.dp * scale,
        itemSpacing = ItemSpacingBaseDp.dp * scale,
        cardVerticalPadding = CardVerticalBaseDp.dp * scale,
        cardInnerHorizontalPadding = CardInnerHorizontalBaseDp.dp * scale,
        cardBorderWidth = 0.4f.dp,
    )
}
