package io.github.jinlinahida.shirokowear.ui

/**
 * Marks APIs that are published but still expected to change shape.
 *
 * Everything in the "shells" lane (reveal state machines, spread layouts,
 * streaming cards) starts here and is promoted by removing the annotation in
 * a minor release.
 */
@RequiresOptIn(
    message = "This ShirokoWear UI API is experimental and may change or be removed.",
    level = RequiresOptIn.Level.ERROR,
)
@Retention(AnnotationRetention.BINARY)
public annotation class UnstableShirokoWearApi
