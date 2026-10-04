package io.github.jinlinahida.shirokowear.navigation

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * The spatial story is the part a host app cannot eyeball during development, so
 * it is asserted directly rather than left to "looks right on the watch".
 */
public class ShirokoWearRouteTest {

    private data class Route(
        override val routeKey: String,
        override val depth: Int,
        override val backKey: String?,
    ) : ShirokoWearRoute

    private val home = Route("home", 0, null)
    private val feature = Route("feature", 1, "home")
    private val result = Route("result", 2, "feature")
    private val sibling = Route("otherFeature", 1, "home")
    private val deepUnrelated = Route("elsewhere", 3, "nowhere")

    @Test
    public fun firstRouteIsAlwaysForward() {
        assertEquals(
            ShirokoWearNavigationDirection.FORWARD,
            resolveShirokoWearNavigationDirection(null, home),
        )
    }

    @Test
    public fun sameRouteIsLateral() {
        assertEquals(
            ShirokoWearNavigationDirection.LATERAL,
            resolveShirokoWearNavigationDirection(feature, feature),
        )
    }

    @Test
    public fun parentChildRelationBeatsDepthNumbers() {
        assertEquals(
            ShirokoWearNavigationDirection.FORWARD,
            resolveShirokoWearNavigationDirection(feature, result),
        )
        assertEquals(
            ShirokoWearNavigationDirection.BACKWARD,
            resolveShirokoWearNavigationDirection(result, feature),
        )
    }

    @Test
    public fun unrelatedRoutesAreDecidedByDepth() {
        assertEquals(
            ShirokoWearNavigationDirection.FORWARD,
            resolveShirokoWearNavigationDirection(sibling, deepUnrelated),
        )
        assertEquals(
            ShirokoWearNavigationDirection.BACKWARD,
            resolveShirokoWearNavigationDirection(deepUnrelated, sibling),
        )
    }

    /**
     * Equal depth with no parent relation is a sibling swap, not a drill-down.
     *
     * The source app returned FORWARD here, which slid a whole new page in for a
     * peer change; the library returns LATERAL and a host that wants the old
     * behaviour can pass an override.
     */
    @Test
    public fun sameDepthSiblingsAreLateral() {
        val other = Route("settings", 1, "home")
        assertEquals(
            ShirokoWearNavigationDirection.LATERAL,
            resolveShirokoWearNavigationDirection(sibling, other),
        )
    }

    @Test
    public fun appOverrideWinsOverGeometry() {
        val about = Route("about", 2, "home")
        val welcome = Route("welcome", 0, null)

        assertEquals(
            ShirokoWearNavigationDirection.BACKWARD,
            resolveShirokoWearNavigationDirection(about, welcome),
        )

        val forced = resolveShirokoWearNavigationDirection(about, welcome) { from, to ->
            if (from?.routeKey == "about" && to.routeKey == "welcome") {
                ShirokoWearNavigationDirection.FORWARD
            } else {
                null
            }
        }
        assertEquals(ShirokoWearNavigationDirection.FORWARD, forced)
    }
}
