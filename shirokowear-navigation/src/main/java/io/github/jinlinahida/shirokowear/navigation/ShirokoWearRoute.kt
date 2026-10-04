package io.github.jinlinahida.shirokowear.navigation

/**
 * The navigation contract the transition system needs from a host app.
 *
 * A design system cannot know an app's screen enum, and must not try to: the
 * source app this system came from had 34 branches of `AppScreen` baked into its
 * motion code, which made the motion un-portable. Depth and parentage are the
 * only two facts a transition can be derived from, so those are all the library
 * asks for.
 */
public interface ShirokoWearRoute {

    /** Stable identifier, used for keys and for override rules. */
    public val routeKey: String

    /** 0 is the root. Deeper routes are drilled into, shallower ones popped back to. */
    public val depth: Int

    /** The route this one returns to, or null at the root. */
    public val backKey: String?
}

/** Direction of a route change, which decides the spatial story it tells. */
public enum class ShirokoWearNavigationDirection {
    /** Push / drill-down: new page slides in over the old one from the right. */
    FORWARD,

    /** Pop / return: current page leaves to the right, the parent slides back. */
    BACKWARD,

    /** Sibling / tag swap: both pages shift a short distance horizontally. */
    LATERAL,
}

/**
 * Lets an app override the geometric verdict for a specific pair of routes.
 *
 * Return null to fall through to parent/depth reasoning. This is where an app
 * says "entering the onboarding screen from Settings is still a forward move",
 * without the library having to know either screen exists.
 */
public typealias ShirokoWearDirectionOverride =
    (from: ShirokoWearRoute?, to: ShirokoWearRoute) -> ShirokoWearNavigationDirection?

/**
 * Resolves a transition direction. Pure and total so the spatial logic can be
 * asserted without a composition.
 *
 * Parentage beats depth: `A.backKey == B` means B is A's parent regardless of
 * what the depth numbers say, and the same holds in reverse. Only when the two
 * routes are unrelated does depth decide.
 */
public fun resolveShirokoWearNavigationDirection(
    from: ShirokoWearRoute?,
    to: ShirokoWearRoute,
    override: ShirokoWearDirectionOverride? = null,
): ShirokoWearNavigationDirection {
    override?.invoke(from, to)?.let { return it }

    if (from == null) return ShirokoWearNavigationDirection.FORWARD
    if (from.routeKey == to.routeKey) return ShirokoWearNavigationDirection.LATERAL

    if (to.backKey == from.routeKey) return ShirokoWearNavigationDirection.FORWARD
    if (from.backKey == to.routeKey) return ShirokoWearNavigationDirection.BACKWARD

    return when {
        to.depth > from.depth -> ShirokoWearNavigationDirection.FORWARD
        to.depth < from.depth -> ShirokoWearNavigationDirection.BACKWARD
        else -> ShirokoWearNavigationDirection.LATERAL
    }
}
