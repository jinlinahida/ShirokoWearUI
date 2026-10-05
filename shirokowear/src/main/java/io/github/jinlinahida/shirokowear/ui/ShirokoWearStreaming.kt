package io.github.jinlinahida.shirokowear.ui

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.isActive
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
 * Batches incoming rapid token deltas over [windowMs] (default 90ms) before emitting
 * downstream, drastically reducing full-tree recomposition frequency and CPU wakeups
 * on low-power wearable devices. Flushes instantly on stream completion or cancellation.
 */
@UnstableShirokoWearApi
public fun Flow<String>.bufferTextDeltas(windowMs: Long = 90L): Flow<String> = channelFlow {
    val deltaBuffer = StringBuilder()
    val mutex = Mutex()
    var batchStartTime = 0L

    val tickerJob = launch {
        while (isActive) {
            delay((windowMs / 2).coerceAtLeast(10L))
            mutex.withLock {
                if (deltaBuffer.isNotEmpty() && (System.currentTimeMillis() - batchStartTime >= windowMs)) {
                    val combined = deltaBuffer.toString()
                    deltaBuffer.clear()
                    send(combined)
                }
            }
        }
    }

    try {
        collect { text ->
            mutex.withLock {
                val now = System.currentTimeMillis()
                if (deltaBuffer.isEmpty()) {
                    batchStartTime = now
                }
                deltaBuffer.append(text)
                if (now - batchStartTime >= windowMs) {
                    val combined = deltaBuffer.toString()
                    deltaBuffer.clear()
                    send(combined)
                }
            }
        }
    } finally {
        tickerJob.cancel()
        mutex.withLock {
            if (deltaBuffer.isNotEmpty()) {
                val remaining = deltaBuffer.toString()
                deltaBuffer.clear()
                send(remaining)
            }
        }
    }
}
