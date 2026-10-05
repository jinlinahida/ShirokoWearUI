package io.github.jinlinahida.shirokowear.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.AutoCenteringParams
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyColumnDefaults
import androidx.wear.compose.foundation.lazy.ScalingLazyListScope
import androidx.wear.compose.foundation.lazy.ScalingLazyListState
import androidx.wear.compose.foundation.lazy.ScalingParams
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.foundation.requestFocusOnHierarchyActive
import androidx.wear.compose.foundation.rotary.RotaryScrollableDefaults
import androidx.wear.compose.foundation.rotary.rotaryScrollable
import androidx.wear.compose.material3.ScreenScaffold

/**
 * Crown behaviour must survive the animation toggle: with animations off, the
 * fish-eye scale and edge fade flatten to 1.0, but the list still scrolls.
 *
 * Public because a host that builds its own `ScalingLazyColumn` needs the same
 * degradation rule rather than re-inventing it — the readable `edgeScale`/`edgeAlpha`
 * also make it assertable.
 */
public fun shirokoWearScalingParams(animationsEnabled: Boolean): ScalingParams =
    if (animationsEnabled) defaultAnimatedScalingParams else defaultFlatScalingParams

private val defaultAnimatedScalingParams: ScalingParams by lazy {
    ScalingLazyColumnDefaults.scalingParams()
}
private val defaultFlatScalingParams: ScalingParams by lazy {
    ScalingLazyColumnDefaults.scalingParams(
        edgeScale = 1.0f,
        edgeAlpha = 1.0f,
    )
}

/**
 * Standard `LazyColumn` wired to the crown and to the ScreenScaffold scroll
 * indicator. The list and the scaffold share one state, so the right-edge
 * position bar is accurate without a second measuring pass.
 */
@Composable
public fun ShirokoWearRotaryColumn(
    itemSpacing: Dp,
    modifier: Modifier = Modifier.fillMaxSize(),
    rotaryEnabled: Boolean = ShirokoWearTheme.rotaryScrollingEnabled,
    hapticFeedbackEnabled: Boolean = ShirokoWearTheme.hapticFeedbackEnabled,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(),
    content: LazyListScope.() -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    val rotaryBehavior = if (rotaryEnabled) {
        RotaryScrollableDefaults.behavior(
            scrollableState = state,
            hapticFeedbackEnabled = hapticFeedbackEnabled,
        )
    } else {
        null
    }
    val rotaryModifier = remember(rotaryEnabled, rotaryBehavior, focusRequester, modifier) {
        if (rotaryEnabled && rotaryBehavior != null) {
            modifier
                .requestFocusOnHierarchyActive()
                .rotaryScrollable(behavior = rotaryBehavior, focusRequester = focusRequester)
        } else {
            modifier
        }
    }

    ScreenScaffold(scrollState = state, contentPadding = contentPadding) { scaffoldPadding ->
        LazyColumn(
            state = state,
            modifier = rotaryModifier,
            contentPadding = scaffoldPadding,
            verticalArrangement = Arrangement.spacedBy(itemSpacing),
            content = content,
        )
    }
}

/**
 * Fish-eye card stream: the row under the crown grows to full size while the
 * rows near the curved edges shrink and fade, which is what makes a long list
 * readable through a round bezel.
 */
@Composable
public fun ShirokoWearScalingRotaryColumn(
    modifier: Modifier = Modifier.fillMaxSize(),
    rotaryEnabled: Boolean = ShirokoWearTheme.rotaryScrollingEnabled,
    hapticFeedbackEnabled: Boolean = ShirokoWearTheme.hapticFeedbackEnabled,
    animationsEnabled: Boolean = ShirokoWearTheme.animationsEnabled,
    state: ScalingLazyListState = rememberScalingLazyListState(),
    contentPadding: PaddingValues = ShirokoWearTheme.dimens.screenPadding,
    itemSpacing: Dp = 8.dp,
    autoCentering: AutoCenteringParams? = AutoCenteringParams(itemIndex = 0),
    scalingParams: ScalingParams = shirokoWearScalingParams(animationsEnabled),
    content: ScalingLazyListScope.() -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    val rotaryBehavior = if (rotaryEnabled) {
        RotaryScrollableDefaults.behavior(
            scrollableState = state,
            hapticFeedbackEnabled = hapticFeedbackEnabled,
        )
    } else {
        null
    }
    val rotaryModifier = remember(rotaryEnabled, rotaryBehavior, focusRequester, modifier) {
        if (rotaryEnabled && rotaryBehavior != null) {
            modifier
                .requestFocusOnHierarchyActive()
                .rotaryScrollable(behavior = rotaryBehavior, focusRequester = focusRequester)
        } else {
            modifier
        }
    }

    ScreenScaffold(scrollState = state, contentPadding = contentPadding) { scaffoldPadding ->
        ScalingLazyColumn(
            state = state,
            modifier = rotaryModifier,
            contentPadding = scaffoldPadding,
            verticalArrangement = Arrangement.spacedBy(itemSpacing),
            scalingParams = scalingParams,
            autoCentering = autoCentering,
            content = content,
        )
    }
}
