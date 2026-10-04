package io.github.jinlinahida.shirokowear.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonColors
import androidx.wear.compose.material3.ButtonDefaults

/**
 * Shared button geometry: hairline diagonal sheen border, 30% alpha ink fill so
 * the ambient light behind the button still shows through it.
 */
public object ShirokoWearButtonDefaults {

    public val borderStroke: BorderStroke
        @Composable get() = BorderStroke(
            width = ShirokoWearTheme.dimens.cardBorderWidth,
            brush = Brush.linearGradient(
                colors = listOf(ShirokoWearTheme.colors.cardBorder, Color.Transparent),
                start = Offset.Zero,
                end = Offset.Infinite,
            ),
        )

    @Composable
    public fun highlightedBorderStroke(
        highlightColor: Color = ShirokoWearTheme.colors.cardHighlight,
    ): BorderStroke = BorderStroke(
        width = 1.dp,
        brush = Brush.linearGradient(
            colors = listOf(highlightColor, Color.Transparent),
            start = Offset.Zero,
            end = Offset.Infinite,
        ),
    )

    @Composable
    public fun buttonColors(
        containerColor: Color = ShirokoWearTheme.colors.cardBackground,
        contentColor: Color = ShirokoWearTheme.colors.contentPrimary,
    ): ButtonColors = ButtonDefaults.buttonColors(
        containerColor = containerColor,
        contentColor = contentColor,
        disabledContainerColor = containerColor.copy(alpha = 0.2f),
        disabledContentColor = contentColor.copy(alpha = 0.38f),
    )

    @Composable
    public fun outlinedButtonColors(
        containerColor: Color = ShirokoWearTheme.colors.outlineButtonBackground,
        contentColor: Color = ShirokoWearTheme.colors.contentPrimary,
    ): ButtonColors = ButtonDefaults.buttonColors(
        containerColor = containerColor,
        contentColor = contentColor,
        disabledContainerColor = Color.Transparent,
        disabledContentColor = contentColor.copy(alpha = 0.38f),
    )

    public val compactContentPadding: PaddingValues = PaddingValues(
        horizontal = 8.dp,
        vertical = 4.dp,
    )
}

/** Full-width, centre-aligned button carrying arbitrary row content. */
@Composable
public fun ShirokoWearCardButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ButtonDefaults.shape,
    colors: ButtonColors = ShirokoWearButtonDefaults.buttonColors(),
    border: BorderStroke? = ShirokoWearButtonDefaults.borderStroke,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Center,
    content: @Composable RowScope.() -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier.pressFeedback(interactionSource, enabled),
        enabled = enabled,
        shape = shape,
        colors = colors,
        border = border,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = horizontalArrangement,
            verticalAlignment = Alignment.CenterVertically,
            content = content,
        )
    }
}

/**
 * Selected/unselected pill used in pickers and segmented choices.
 *
 * Unselected keeps the hairline sheen; selected gets a 1.2dp solid ring plus an
 * 18% tint of the highlight, animated over 200ms so the state change is legible
 * on a wrist-flick without being showy.
 */
@Composable
public fun ShirokoWearSelectableButton(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ButtonDefaults.shape,
    highlightColor: Color = ShirokoWearTheme.colors.spotlightDefault,
    contentPadding: PaddingValues = ShirokoWearButtonDefaults.compactContentPadding,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Center,
    content: @Composable RowScope.() -> Unit,
) {
    val colors = ShirokoWearTheme.colors
    val animatedBorderColor by animateColorAsState(
        targetValue = if (selected) highlightColor else colors.cardBorder,
        animationSpec = tween(durationMillis = 200),
        label = "shirokoSelectableBorderColor",
    )
    val animatedContainerColor by animateColorAsState(
        targetValue = if (selected) highlightColor.copy(alpha = 0.18f) else colors.cardBackground,
        animationSpec = tween(durationMillis = 200),
        label = "shirokoSelectableContainerColor",
    )
    val animatedBorderWidth by animateDpAsState(
        targetValue = if (selected) 1.2.dp else ShirokoWearTheme.dimens.cardBorderWidth,
        animationSpec = tween(durationMillis = 200),
        label = "shirokoSelectableBorderWidth",
    )

    val borderStroke = remember(selected, animatedBorderColor, animatedBorderWidth) {
        if (selected) {
            BorderStroke(width = animatedBorderWidth, color = animatedBorderColor)
        } else {
            BorderStroke(
                width = animatedBorderWidth,
                brush = Brush.linearGradient(
                    colors = listOf(animatedBorderColor, Color.Transparent),
                    start = Offset.Zero,
                    end = Offset.Infinite,
                ),
            )
        }
    }

    val buttonColors = ButtonDefaults.buttonColors(
        containerColor = animatedContainerColor,
        contentColor = colors.contentPrimary,
        disabledContainerColor = animatedContainerColor.copy(alpha = 0.2f),
        disabledContentColor = colors.contentDisabled,
    )

    Button(
        onClick = onClick,
        modifier = modifier.pressFeedback(interactionSource, enabled),
        enabled = enabled,
        shape = shape,
        colors = buttonColors,
        border = borderStroke,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = horizontalArrangement,
            verticalAlignment = Alignment.CenterVertically,
            content = content,
        )
    }
}
