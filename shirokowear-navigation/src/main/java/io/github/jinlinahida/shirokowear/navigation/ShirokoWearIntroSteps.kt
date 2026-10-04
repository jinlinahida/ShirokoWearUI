package io.github.jinlinahida.shirokowear.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Step container for onboarding, consent and mode-picking flows.
 *
 * The host app owns the step index — it is app state, usually persisted as
 * "onboarding seen" — and owns what each step contains. All this decides is how one
 * step hands over to the next, using the same spatial story as route transitions so
 * an intro and the app behind it read as one continuous surface rather than two
 * unrelated animations bolted together.
 */
@Composable
public fun ShirokoWearIntroSteps(
    currentStep: Int,
    modifier: Modifier = Modifier,
    animationsEnabled: Boolean = true,
    content: @Composable AnimatedContentScope.(Int) -> Unit,
) {
    AnimatedContent(
        targetState = currentStep,
        modifier = modifier,
        transitionSpec = {
            // `initialState` is the step this animation started from; there is no
            // `prevState` on the transition scope, so the direction comes from the
            // pair the scope already guarantees to be correct.
            shirokoWearPageTransition(
                direction = introStepDirection(initialState, targetState),
                animationsEnabled = animationsEnabled,
            )
        },
        label = "shirokoIntroSteps",
        content = content,
    )
}
