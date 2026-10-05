package io.github.jinlinahida.shirokowear.ui

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Batching behaviour, asserted against a virtual clock.
 *
 * The assertions are exact (`listOf("Shiroko", "Wear")`, not `size <= 2`)
 * deliberately. The previous implementation polled `System.currentTimeMillis()` from
 * a ticker while the collector also flushed, so the emission count depended on
 * scheduler jitter — the same code passed or failed between runs on one machine.
 * Under `runTest`, `delay` advances virtual time, which pins the contract and
 * documents what a window actually means.
 */
@OptIn(UnstableShirokoWearApi::class)
public class ShirokoWearStreamingTest {

    @Test
    public fun rapidTokensCollapseIntoOneEmissionPerWindow() = runTest {
        val upstream = flow {
            emit("Shi")
            delay(10L)
            emit("ro")
            delay(10L)
            emit("ko")
            delay(120L)
            emit("Wear")
        }

        val buffered = upstream.bufferTextDeltas(windowMs = 60L).toList()

        // "Shi"/"ro"/"ko" land inside the window opened at t=0 (closing at t=60);
        // "Wear" arrives after that window shipped and goes out on completion.
        assertEquals(listOf("Shiroko", "Wear"), buffered)
        assertEquals("ShirokoWear", buffered.joinToString(""))
    }

    @Test
    public fun tokensArrivingInSeparateWindowsAreNotGluedTogether() = runTest {
        val upstream = flow {
            emit("A")
            delay(70L)
            emit("B")
        }

        val buffered = upstream.bufferTextDeltas(windowMs = 60L).toList()

        // Each token owns a window, and neither one waits on the other to render.
        assertEquals(listOf("A", "B"), buffered)
    }

    @Test
    public fun completionFlushesTheOpenWindowImmediately() = runTest {
        val buffered = flowOf("A", "B", "C")
            .bufferTextDeltas(windowMs = 200L)
            .toList()

        // No tail may be lost by ending mid-window, and the timer that is still due
        // must not then ship an empty duplicate.
        assertEquals(listOf("ABC"), buffered)
    }

    @Test
    public fun emptyStreamEmitsNothing() = runTest {
        val buffered = flowOf<String>()
            .bufferTextDeltas(windowMs = 60L)
            .toList()

        assertEquals(emptyList<String>(), buffered)
    }

    @Test
    public fun nonPositiveWindowIsRejectedAtTheBoundary() = runTest {
        val failure = runCatching {
            flowOf("A").bufferTextDeltas(windowMs = 0L).toList()
        }.exceptionOrNull()

        assertEquals(IllegalArgumentException::class.java, failure?.javaClass)
    }

    @Test
    public fun stateTypeHierarchySupportsGenericPayload() {
        val state: ShirokoWearStreamingState<Int> = ShirokoWearStreamingState.Completed(
            fullText = "Answer",
            payload = 42,
        )

        when (state) {
            is ShirokoWearStreamingState.Completed -> {
                assertEquals("Answer", state.fullText)
                assertEquals(42, state.payload)
            }
            else -> throw AssertionError("Expected Completed state")
        }
    }
}
