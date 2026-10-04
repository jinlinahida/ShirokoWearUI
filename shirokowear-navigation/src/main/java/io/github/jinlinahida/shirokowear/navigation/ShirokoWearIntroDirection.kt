package io.github.jinlinahida.shirokowear.navigation

/**
 * Direction implied by moving between numbered intro steps.
 *
 * A linear onboarding flow is a hierarchy of its own kind: step 3 sits deeper than
 * step 2, so advancing drills forward and returning pops back. Invert this and a
 * user re-reads the page they just dismissed as if it were brand new.
 *
 * Lives in a file of its own, apart from the composable that consumes it, because
 * the coverage gate measures this mapping and cannot do so while the same file class
 * also holds UI code that is untestable without a device.
 */
public fun introStepDirection(fromStep: Int, toStep: Int): ShirokoWearNavigationDirection = when {
    toStep > fromStep -> ShirokoWearNavigationDirection.FORWARD
    toStep < fromStep -> ShirokoWearNavigationDirection.BACKWARD
    else -> ShirokoWearNavigationDirection.LATERAL
}
