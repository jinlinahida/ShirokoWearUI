package io.github.jinlinahida.shirokowear.ui.intro

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.Text
import io.github.jinlinahida.shirokowear.ui.ShirokoWearTheme
import io.github.jinlinahida.shirokowear.ui.rememberShirokoWearHaptics

private const val FloatAmplitudeDp = 5f
private const val FloatDurationMs = 1100
private const val RevealDurationMs = 1000
private const val HintCaptionGapDp = 8f

/**
 * The "tap to continue" affordance: a small dot above a caption, breathing a few
 * pixels up and down, sliding in once after the screen settles.
 *
 * Three details are load-bearing rather than decorative:
 * - the infinite transition is never created when animations are off, so a muted
 *   watch stops burning frames;
 * - the entrance reveal fires after first composition, which is what makes the hint
 *   arrive *after* the backdrop instead of competing with it;
 * - the float moves `translationY` in pixels through a graphics layer, so it costs
 *   no layout pass.
 */
@Composable
public fun ShirokoWearEnterPrompt(
    label: String,
    modifier: Modifier = Modifier,
    dotSize: Dp = 5.dp,
    tint: Color = ShirokoWearTheme.colors.contentPrimary,
    labelTextStyle: TextUnit = 12.sp,
    letterSpacing: TextUnit = 0.5.sp,
    animationsEnabled: Boolean = ShirokoWearTheme.animationsEnabled,
) {
    var isRevealed by remember(animationsEnabled) { mutableStateOf(!animationsEnabled) }
    LaunchedEffect(Unit) { isRevealed = true }

    val density = LocalDensity.current
    val floatOffset = if (animationsEnabled) {
        val transition = rememberInfiniteTransition(label = "shirokoEnterPromptFloat")
        val offset by transition.animateFloat(
            initialValue = with(density) { -FloatAmplitudeDp.dp.toPx() },
            targetValue = with(density) { FloatAmplitudeDp.dp.toPx() },
            animationSpec = InfiniteRepeatableSpec(
                animation = tween(durationMillis = FloatDurationMs, easing = EaseInOutCubic),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "shirokoEnterPromptFloatOffset",
        )
        offset
    } else {
        0f
    }

    AnimatedVisibility(
        visible = isRevealed,
        modifier = modifier.fillMaxWidth(),
        enter = fadeIn(tween(RevealDurationMs)) +
            slideInVertically(tween(RevealDurationMs)) { it / 2 },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer { translationY = floatOffset },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(dotSize)
                    .clip(CircleShape)
                    .background(tint),
            )
            Spacer(modifier = Modifier.height(HintCaptionGapDp.dp))
            Text(
                text = label,
                fontSize = labelTextStyle,
                color = tint,
                letterSpacing = letterSpacing,
            )
        }
    }
}

/**
 * Full-bleed tap area that advances an intro screen.
 *
 * No ripple and no press-scale: on a backdrop like this, a visual reaction to the
 * tap would fight the very thing the user is looking at. The confirmation is haptic.
 */
@Composable
public fun ShirokoWearTapToAdvance(
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val haptics = rememberShirokoWearHaptics()
    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    haptics.click()
                    onTap()
                },
            ),
        content = content,
    )
}

/**
 * Brand wordmark rendered with a horizontal colour ramp.
 *
 * The gradient stops are required, never defaulted: brand colour is the app's
 * identity, and a design system that ships an opinion about it is one two apps
 * cannot share.
 */
@Composable
public fun ShirokoWearGradientTitle(
    text: String,
    gradientColors: List<Color>,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 26.sp,
    letterSpacing: TextUnit = 1.5.sp,
) {
    val styled = remember(text, gradientColors) { gradientAnnotatedString(text, gradientColors) }
    Text(
        text = styled,
        modifier = modifier,
        fontSize = fontSize,
        letterSpacing = letterSpacing,
    )
}

/**
 * Attaches the gradient as a single span over the whole string. Kept separate from
 * the composable so the mapping is assertable without a canvas — and so a
 * single-colour request degrades to plain text instead of an empty brush crash.
 */
public fun gradientAnnotatedString(
    text: String,
    gradientColors: List<Color>,
): AnnotatedString = buildAnnotatedString {
    if (gradientColors.size >= 2) {
        withStyle(SpanStyle(brush = Brush.horizontalGradient(gradientColors))) {
            append(text)
        }
    } else {
        append(text)
    }
}
