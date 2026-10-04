package io.github.jinlinahida.shirokowear.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput

private const val PressSpringDamping = 0.5f

/** 300f is neither Spring.StiffnessLow (200) nor Medium (1500); keep the literal. */
private const val PressSpringStiffness = 300f
private const val PressScaleTarget = 0.96f

/**
 * Smooth marquee for the wrist: a label that fits its width never animates at
 * all, an overflowing one loops with a 1.2s dwell at each end so a title is
 * readable before it moves. Degrades to a no-op when animations are off.
 */
public fun Modifier.wearMarquee(
    animationsEnabled: Boolean = true,
    initialDelayMillis: Int = 1200,
    repeatDelayMillis: Int = 1200,
    iterations: Int = Int.MAX_VALUE,
): Modifier = if (animationsEnabled) {
    this.basicMarquee(
        iterations = iterations,
        initialDelayMillis = initialDelayMillis,
        repeatDelayMillis = repeatDelayMillis,
    )
} else {
    this
}

/**
 * Press-to-shrink click with no system ripple: 0.9x while held, 150ms spring
 * back on release.
 *
 * The haptic fires on [PressInteraction.Release], never on press — a finger
 * that slides off a card to scroll must not leave a trail of clicks behind, and
 * dispatching on press blocks the first frame of the scroll on binder IPC.
 */
@Composable
public fun Modifier.clickVfx(
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    isEnabled: Boolean = true,
    animationsEnabled: Boolean = ShirokoWearTheme.animationsEnabled,
    onClick: () -> Unit,
): Modifier = composed {
    val haptics = rememberShirokoWearHaptics()
    if (!isEnabled) return@composed Modifier

    if (!animationsEnabled) {
        return@composed clickable(
            indication = null,
            interactionSource = interactionSource,
            onClick = {
                haptics.click()
                onClick()
            },
        )
    }

    val isPressed by interactionSource.collectIsPressedAsState()
    val sizePercent by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = tween(durationMillis = 150),
        label = "shirokoClickVfxScale",
    )
    ReleaseHaptic(interactionSource, haptics)

    scale(sizePercent).clickable(
        indication = null,
        interactionSource = interactionSource,
        onClick = onClick,
    )
}

/**
 * Long-click capable variant of [clickVfx]. Uses pointer gestures instead of
 * [clickable] so the long press gets a heavier strike of its own.
 */
@Composable
public fun Modifier.clickVfx(
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    enabled: Boolean = true,
    animationsEnabled: Boolean = ShirokoWearTheme.animationsEnabled,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
): Modifier = composed {
    val haptics = rememberShirokoWearHaptics()
    if (!enabled) return@composed Modifier

    if (!animationsEnabled) {
        return@composed pointerInputHaptics(haptics, onClick, onLongClick)
    }

    val isPressed by interactionSource.collectIsPressedAsState()
    val sizePercent by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = tween(durationMillis = 150),
        label = "shirokoClickVfxScaleWithLongClick",
    )

    scale(sizePercent).pointerInputHaptics(haptics, onClick, onLongClick)
}

@Composable
private fun Modifier.pointerInputHaptics(
    haptics: ShirokoWearHaptics,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
): Modifier = pointerInput(Unit) {
    detectTapGestures(
        onTap = {
            haptics.click()
            onClick()
        },
        onLongPress = {
            haptics.play(
                ShirokoWearHapticKind.CLICK,
                ShirokoWearHapticIntensity.STRONG,
            )
            onLongClick()
        },
    )
}

/**
 * Spring-based interactive highlight for `Button`/`OutlinedButton` that already
 * own an [interactionSource].
 *
 * Pass the *same* source you gave the button, otherwise the press is never
 * observable and the modifier silently does nothing.
 */
@Composable
public fun Modifier.pressFeedback(
    interactionSource: MutableInteractionSource,
    enabled: Boolean = true,
): Modifier {
    if (!enabled) return this

    val isPressed by interactionSource.collectIsPressedAsState()
    val haptics = rememberShirokoWearHaptics()
    ReleaseHaptic(interactionSource, haptics)

    val scale by animateFloatAsState(
        targetValue = if (isPressed) PressScaleTarget else 1.0f,
        animationSpec = spring(
            dampingRatio = PressSpringDamping,
            stiffness = PressSpringStiffness,
            visibilityThreshold = 0.001f,
        ),
        label = "shirokoPressScale",
    )

    return graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

@Composable
private fun ReleaseHaptic(
    interactionSource: MutableInteractionSource,
    haptics: ShirokoWearHaptics,
) {
    LaunchedEffect(interactionSource, haptics.enabled) {
        if (!haptics.enabled) return@LaunchedEffect
        interactionSource.interactions.collect { interaction ->
            if (interaction is PressInteraction.Release) {
                haptics.click()
            }
        }
    }
}
