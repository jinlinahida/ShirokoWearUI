package io.github.jinlinahida.shirokowear.navigation

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Intro flows are usually 2-4 steps, and the interesting cases are exactly the
 * boundaries of that range: the first advance, a jump back to step 0, and a "skip
 * to end" that lands on the last step.
 */
public class ShirokoWearIntroStepsTest {

    @Test
    public fun advancingDrillsForward() {
        assertEquals(ShirokoWearNavigationDirection.FORWARD, introStepDirection(0, 1))
        assertEquals(ShirokoWearNavigationDirection.FORWARD, introStepDirection(1, 3))
    }

    @Test
    public fun returningPopsBackward() {
        assertEquals(ShirokoWearNavigationDirection.BACKWARD, introStepDirection(2, 1))
    }

    /** Re-showing the same step must not animate as a drill-down. */
    @Test
    public fun sameStepIsLateral() {
        assertEquals(ShirokoWearNavigationDirection.LATERAL, introStepDirection(2, 2))
    }

    @Test
    public fun jumpingBackToFirstStepPops() {
        assertEquals(ShirokoWearNavigationDirection.BACKWARD, introStepDirection(3, 0))
    }

    /**
     * The library never clamps indices — that is the host's business — but a
     * negative step (a restored, corrupted preference for "onboarding step") must
     * still resolve to something rather than throw.
     */
    @Test
    public fun outOfRangeIndicesStillResolve() {
        assertEquals(ShirokoWearNavigationDirection.FORWARD, introStepDirection(-1, 0))
        assertEquals(ShirokoWearNavigationDirection.BACKWARD, introStepDirection(9, 4))
    }
}
