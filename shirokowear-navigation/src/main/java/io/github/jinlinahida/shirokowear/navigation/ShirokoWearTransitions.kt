package io.github.jinlinahida.shirokowear.navigation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween

private const val SlideEnterDurationMs = 280
private const val SlideExitDurationMs = 260
private const val FadeDurationMs = 200
private const val ParallaxOffsetFraction = 0.20f
private const val LateralOffsetFraction = 0.15f

/** Wear OS motion curves: exit accelerates, entry decelerates long and softly. */
public val ShirokoWearAccelEasing: CubicBezierEasing = CubicBezierEasing(0.4f, 0.0f, 1.0f, 1.0f)

/**
 * Material "emphasized decelerate". A page that stops abruptly reads as
 * mechanical; this is the curve that makes the arrival feel weighted.
 */
public val ShirokoWearEmphasizedDecelEasing: CubicBezierEasing =
    CubicBezierEasing(0.05f, 0.0f, 0.1f, 1.0f)

/**
 * Spatial continuity between two routes.
 *
 * FORWARD: the incoming page slides the full width in over the outgoing one
 * (`targetContentZIndex = 1`), while the outgoing page only gives way 20% — a
 * parallax hint of where you came from rather than a hard swap.
 *
 * BACKWARD mirrors that: the top page leaves to the right and stays on top until
 * it is gone (`targetContentZIndex = 0`), the parent returns from its 20% offset.
 *
 * Z-index is the whole trick: getting it wrong makes a page visibly tear at the
 * seam on a round bezel.
 */
public fun shirokoWearPageTransition(
    direction: ShirokoWearNavigationDirection,
    animationsEnabled: Boolean = true,
): ContentTransform {
    if (!animationsEnabled) {
        return EnterTransition.None togetherWith ExitTransition.None
    }

    return when (direction) {
        ShirokoWearNavigationDirection.FORWARD -> shirokoWearContent(
            enterOffsetX = { fullWidth -> fullWidth },
            exitOffsetX = { fullWidth -> -(fullWidth * ParallaxOffsetFraction).toInt() },
            targetZIndex = 1f,
        )

        ShirokoWearNavigationDirection.BACKWARD -> shirokoWearContent(
            enterOffsetX = { fullWidth -> -(fullWidth * ParallaxOffsetFraction).toInt() },
            exitOffsetX = { fullWidth -> fullWidth },
            targetZIndex = 0f,
        )

        ShirokoWearNavigationDirection.LATERAL -> shirokoWearContent(
            enterOffsetX = { (it * LateralOffsetFraction).toInt() },
            exitOffsetX = { -(it * LateralOffsetFraction).toInt() },
            targetZIndex = 1f,
        )
    }
}

/** Convenience overload that derives the direction from the routes themselves. */
public fun shirokoWearPageTransition(
    from: ShirokoWearRoute?,
    to: ShirokoWearRoute,
    animationsEnabled: Boolean = true,
    override: ShirokoWearDirectionOverride? = null,
): ContentTransform = shirokoWearPageTransition(
    direction = resolveShirokoWearNavigationDirection(from, to, override),
    animationsEnabled = animationsEnabled,
)

private fun shirokoWearContent(
    enterOffsetX: (Int) -> Int,
    exitOffsetX: (Int) -> Int,
    targetZIndex: Float,
): ContentTransform {
    val enter = fadeIn(
        animationSpec = tween(
            FadeDurationMs,
            easing = LinearOutSlowInEasing,
        ),
    ) + slideInHorizontally(
        initialOffsetX = enterOffsetX,
        animationSpec = tween(
            SlideEnterDurationMs,
            easing = ShirokoWearEmphasizedDecelEasing,
        ),
    )

    val exit = fadeOut(
        animationSpec = tween(
            FadeDurationMs,
            easing = FastOutLinearInEasing,
        ),
    ) + slideOutHorizontally(
        targetOffsetX = exitOffsetX,
        animationSpec = tween(
            SlideExitDurationMs,
            easing = ShirokoWearAccelEasing,
        ),
    )

    return enter.togetherWith(exit).apply {
        this.targetContentZIndex = targetZIndex
    }
}

/**
 * In-page state change (loading -> content): fade plus a 0.95 scale, so the
 * result arrives as the same object the spinner was standing in for, rather than
 * as a new page. Degrades to no transition when animations are off.
 */
public fun shirokoWearLoadingContentTransition(
    animationsEnabled: Boolean = true,
): ContentTransform {
    if (!animationsEnabled) {
        return EnterTransition.None togetherWith ExitTransition.None
    }
    return (
        fadeIn(tween(240, easing = LinearOutSlowInEasing)) +
            scaleIn(
                initialScale = 0.95f,
                animationSpec = tween(
                    280,
                    easing = FastOutSlowInEasing,
                ),
            )
        ) togetherWith (
        fadeOut(tween(160, easing = FastOutLinearInEasing)) +
            scaleOut(
                targetScale = 0.95f,
                animationSpec = tween(
                    200,
                    easing = FastOutLinearInEasing,
                ),
            )
        )
}
