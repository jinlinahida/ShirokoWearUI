package io.github.jinlinahida.shirokowear.ui

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(UnstableShirokoWearApi::class)
public class ShirokoWearStreamingTest {

    @Test
    public fun bufferTextDeltasAggregatesRapidTokens() = runBlocking {
        val rawFlow = flow {
            emit("Shi")
            delay(10L)
            emit("ro")
            delay(10L)
            emit("ko")
            delay(120L)
            emit("Wear")
        }

        val buffered = rawFlow.bufferTextDeltas(windowMs = 60L).toList()
        assertTrue("Rapid tokens should be grouped into fewer emissions", buffered.size <= 2)
        assertEquals("ShirokoWear", buffered.joinToString(""))
    }

    @Test
    public fun bufferTextDeltasFlushesOnCompletion() = runBlocking {
        val rawFlow = flow {
            emit("A")
            emit("B")
            emit("C")
        }

        val buffered = rawFlow.bufferTextDeltas(windowMs = 200L).toList()
        assertEquals("All tokens must be flushed upon stream completion", "ABC", buffered.joinToString(""))
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
