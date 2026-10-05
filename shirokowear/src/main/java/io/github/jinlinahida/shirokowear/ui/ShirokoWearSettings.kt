package io.github.jinlinahida.shirokowear.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonColors
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text

/**
 * Standard settings entry item: full-width card button with leading icon,
 * primary title, marquee-capable subtitle, and optional trailing widget.
 */
@UnstableShirokoWearApi
@Composable
public fun ShirokoWearSettingsItem(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: (@Composable () -> Unit)? = null,
    trailing: (@Composable RowScope.() -> Unit)? = null,
    enabled: Boolean = true,
    marqueeSubtitle: Boolean = true,
    shape: Shape = ButtonDefaults.shape,
    colors: ButtonColors = ShirokoWearButtonDefaults.buttonColors(),
    border: BorderStroke? = ShirokoWearButtonDefaults.borderStroke,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .pressFeedback(interactionSource, enabled),
        enabled = enabled,
        shape = shape,
        colors = colors,
        border = border,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                icon()
                Spacer(modifier = Modifier.width(10.dp))
            }

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.wearMarquee(marqueeSubtitle),
                    )
                }
            }

            if (trailing != null) {
                Spacer(modifier = Modifier.width(8.dp))
                trailing()
            }
        }
    }
}

/**
 * Toggle card entry: switches boolean state on click with animated halo border,
 * translucent tinted background, and dual-gated tactile toggle haptics.
 *
 * [confirmEnableAudibly] is for one row only: the haptics master switch itself.
 * Reading the mute gate at that instant makes turning haptics on silent, so the user
 * cannot tell whether the setting took effect. Leaving it `false` (the default) keeps
 * every other toggle properly gated — turning it on for anything else would fire the
 * motor for a user who muted their watch.
 */
@UnstableShirokoWearApi
@Composable
public fun ShirokoWearToggleCard(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    secondaryLabel: String? = null,
    icon: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
    confirmEnableAudibly: Boolean = false,
    shape: Shape = ButtonDefaults.shape,
    highlightColor: Color = ShirokoWearTheme.colors.cardHighlight,
    contentPadding: PaddingValues = ShirokoWearButtonDefaults.compactContentPadding,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    val haptics = rememberShirokoWearHaptics()
    val colors = ShirokoWearTheme.colors

    val animatedBorderColor by animateColorAsState(
        targetValue = if (checked) highlightColor else colors.cardBorder,
        animationSpec = tween(durationMillis = 200),
        label = "shirokoToggleBorderColor",
    )
    val animatedContainerColor by animateColorAsState(
        targetValue = if (checked) highlightColor.copy(alpha = 0.18f) else colors.cardBackground,
        animationSpec = tween(durationMillis = 200),
        label = "shirokoToggleContainerColor",
    )
    val animatedBorderWidth by animateDpAsState(
        targetValue = if (checked) 1.2.dp else ShirokoWearTheme.dimens.cardBorderWidth,
        animationSpec = tween(durationMillis = 200),
        label = "shirokoToggleBorderWidth",
    )

    val borderStroke = remember(checked, animatedBorderColor, animatedBorderWidth) {
        if (checked) {
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
        onClick = {
            val next = !checked
            if (next && confirmEnableAudibly) {
                haptics.preview(ShirokoWearHapticKind.TOGGLE_ON)
            } else {
                haptics.toggle(next)
            }
            onCheckedChange(next)
        },
        modifier = modifier
            .fillMaxWidth()
            .pressFeedback(interactionSource, enabled),
        enabled = enabled,
        shape = shape,
        colors = buttonColors,
        border = borderStroke,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                icon()
                Spacer(modifier = Modifier.width(8.dp))
            }

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(1.dp),
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = if (checked) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                )
                if (!secondaryLabel.isNullOrBlank()) {
                    Text(
                        text = secondaryLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}
