package io.github.jinlinahida.shirokowear.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text

/**
 * Page heading. Alignment is a bezel decision, not a call-site one: centred on
 * round watches, leading-edge aligned on square ones, so a title never gets
 * pulled under the curved cutout.
 */
@Composable
public fun ShirokoWearScreenTitle(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.titleLarge,
    color: Color = Color.Unspecified,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign = ShirokoWearTheme.dimens.titleTextAlign,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    marquee: Boolean = false,
    animationsEnabled: Boolean = ShirokoWearTheme.animationsEnabled,
) {
    Text(
        text = text,
        style = style,
        color = color,
        fontWeight = fontWeight,
        textAlign = textAlign,
        maxLines = if (marquee) 1 else maxLines,
        softWrap = !marquee,
        overflow = if (marquee) TextOverflow.Ellipsis else overflow,
        modifier = modifier
            .fillMaxWidth()
            .then(if (marquee) Modifier.wearMarquee(animationsEnabled) else Modifier),
    )
}

/**
 * Label/value row inside a card: one quiet line of caption, then the value.
 *
 * [marquee] is for the 40mm case where a value is structurally longer than the
 * bezel allows (a stem-branch pair, an ephemeris line) and wrapping would push
 * the next field off screen.
 */
@Composable
public fun ShirokoWearDetailField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    marquee: Boolean = false,
    spacing: Dp = 1.dp,
    animationsEnabled: Boolean = ShirokoWearTheme.animationsEnabled,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(spacing),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            maxLines = if (marquee) 1 else Int.MAX_VALUE,
            softWrap = !marquee,
            overflow = if (marquee) TextOverflow.Ellipsis else TextOverflow.Clip,
            modifier = if (marquee) Modifier.wearMarquee(animationsEnabled) else Modifier,
        )
    }
}
