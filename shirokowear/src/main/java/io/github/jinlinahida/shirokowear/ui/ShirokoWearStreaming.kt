package io.github.jinlinahida.shirokowear.ui

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Lifecycle states of an in-flight generative stream or long-running background task.
 */
@UnstableShirokoWearApi
public sealed interface ShirokoWearStreamingState<out T> {
    public object Idle : ShirokoWearStreamingState<Nothing>
    public data class Loading<T>(val payload: T? = null) : ShirokoWearStreamingState<T>
    public data class Streaming<T>(val text: String, val payload: T? = null) : ShirokoWearStreamingState<T>
    public data class Completed<T>(val fullText: String, val payload: T? = null) : ShirokoWearStreamingState<T>
    public data class Error<T>(
        val message: String,
        val cause: Throwable? = null,
        val payload: T? = null,
    ) : ShirokoWearStreamingState<T>
}

/**
 * Wear OS optimized stream chunk buffer.
 *
 * Batches incoming token deltas into one emission per window so a watch recomposes
 * a few times a second instead of once per token — recomposition on this hardware
 * is the difference between a smooth reveal and a stuttering one.
 *
 * Leading-edge semantics: the *first* pending token opens a window of [windowMs];
 * tokens arriving inside it are appended and shipped together when the window
 * closes. The window is driven purely by `delay`, never by a wall clock, and there
 * is exactly one flush path per window. Both details matter:
 *
 * - reading `System.currentTimeMillis()` from a poller made the batching depend on
 *   real scheduler jitter, so a unit test could not state what it should produce
 *   (it passed or failed depending on the machine);
 * - two independent flushers (a ticker plus the collector) raced each other for the
 *   same buffer, which is also how a chunk can be emitted twice or split.
 *
 * Flushes on completion so a stream that ends mid-window never loses its tail, and
 * leaves nothing queued afterwards.
 */
@UnstableShirokoWearApi
public fun Flow<String>.bufferTextDeltas(windowMs: Long = 90L): Flow<String> = channelFlow {
    require(windowMs > 0) { "windowMs must be positive, got $windowMs" }

    val buffer = StringBuilder()
    val bufferLock = Mutex()
    var windowOpen = false

    collect { delta ->
        val shouldOpenWindow = bufferLock.withLock {
            buffer.append(delta)
            if (windowOpen) false else { windowOpen = true; true }
        }

        if (shouldOpenWindow) {
            launch {
                delay(windowMs)
                val chunk = bufferLock.withLock {
                    val text = buffer.toString()
                    buffer.setLength(0)
                    windowOpen = false
                    text
                }
                if (chunk.isNotEmpty()) send(chunk)
            }
        }
    }

    // Upstream finished. Anything still inside the open window ships now, not later.
    val tail = bufferLock.withLock {
        val text = buffer.toString()
        buffer.setLength(0)
        text
    }
    if (tail.isNotEmpty()) send(tail)
}
