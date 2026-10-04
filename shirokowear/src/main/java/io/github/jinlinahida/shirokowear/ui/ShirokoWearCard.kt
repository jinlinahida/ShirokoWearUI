package io.github.jinlinahida.shirokowear.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The system's base surface: a 0.4dp diagonal sheen that fades to nothing along
 * the border, over a 30% alpha ink fill, shrinking 0.9x on press.
 *
 * The background stays translucent on purpose — the ambient spotlight behind the
 * card is what keeps a Wear screen from reading as a stack of black boxes.
 */
@Composable
public fun ShirokoWearCard(
    modifier: Modifier = Modifier,
    shape: Shape = ShirokoWearShapes.card,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    isClickEnabled: Boolean = true,
    highlighted: Boolean = false,
    highlightColor: Color = ShirokoWearTheme.colors.cardHighlight,
    borderColor: Color = ShirokoWearTheme.colors.cardBorder,
    borderWidth: Dp = ShirokoWearTheme.dimens.cardBorderWidth,
    backgroundColor: Color = ShirokoWearTheme.colors.cardBackground,
    gradientBorder: Boolean = true,
    innerPadding: PaddingValues = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
    outerPadding: PaddingValues = PaddingValues(vertical = 4.dp),
    fillMaxWidth: Boolean = true,
    contentAlignment: Alignment = Alignment.TopStart,
    content: @Composable BoxScope.() -> Unit,
) {
    val secondColor by animateColorAsState(
        targetValue = when {
            highlighted -> highlightColor
            gradientBorder -> Color.Transparent
            else -> borderColor
        },
        label = "shirokoCardSecondColor",
    )
    val animatedBorder by animateColorAsState(
        targetValue = if (highlighted) highlightColor else borderColor,
        animationSpec = tween(),
        label = "shirokoCardBorderColor",
    )
    val animatedBackground by animateColorAsState(
        targetValue = if (highlighted) highlightColor.copy(alpha = 0.1f) else backgroundColor,
        animationSpec = tween(),
        label = "shirokoCardBackgroundColor",
    )
    val animatedWidth by animateDpAsState(
        targetValue = if (highlighted) 2.dp else borderWidth,
        label = "shirokoCardBorderWidth",
    )

    val clickable = onClick != null || onLongClick != null

    Box(
        modifier = modifier
            .padding(outerPadding)
            .then(
                if (isClickEnabled && clickable) {
                    Modifier.clickVfx(
                        enabled = true,
                        onClick = { onClick?.invoke() },
                        onLongClick = { onLongClick?.invoke() },
                    )
                } else {
                    Modifier
                },
            )
            .clip(shape)
            .border(
                width = animatedWidth,
                shape = shape,
                brush = Brush.linearGradient(
                    listOf(animatedBorder, secondColor),
                    start = Offset.Zero,
                    end = Offset.Infinite,
                ),
            )
            .background(color = animatedBackground)
            .padding(innerPadding)
            .then(if (fillMaxWidth) Modifier.fillMaxWidth() else Modifier),
        contentAlignment = contentAlignment,
        content = content,
    )
}

/**
 * Full-width column card used for result and archive sections.
 *
 * Padding and inter-item spacing come from the active [ShirokoWearDimens], so a
 * larger text setting widens the card instead of clipping its contents.
 */
@Composable
public fun ShirokoWearResultCard(
    modifier: Modifier = Modifier,
    shape: Shape = ShirokoWearShapes.card,
    borderColor: Color = ShirokoWearTheme.colors.cardBorder,
    borderWidth: Dp = ShirokoWearTheme.dimens.cardBorderWidth,
    backgroundColor: Color = ShirokoWearTheme.colors.cardBackground,
    gradientBorder: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    val dimens = ShirokoWearTheme.dimens
    val secondColor = if (gradientBorder) Color.Transparent else borderColor

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .border(
                width = borderWidth,
                shape = shape,
                brush = Brush.linearGradient(
                    listOf(borderColor, secondColor),
                    start = Offset.Zero,
                    end = Offset.Infinite,
                ),
            )
            .background(backgroundColor)
            .padding(
                horizontal = dimens.horizontalPadding / 2,
                vertical = dimens.cardVerticalPadding,
            ),
        verticalArrangement = Arrangement.spacedBy(dimens.itemSpacing / 2),
        content = content,
    )
}
