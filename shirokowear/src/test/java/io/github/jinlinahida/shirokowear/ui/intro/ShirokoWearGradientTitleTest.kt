package io.github.jinlinahida.shirokowear.ui.intro

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * The gradient title has one rule that is easy to break by refactoring: asking for
 * a gradient with fewer than two stops must degrade to plain text rather than
 * produce an empty brush (which renders nothing at all on a watch).
 */
public class ShirokoWearGradientTitleTest {

    @Test
    public fun twoStopsProduceOneSpanCoveringTheWholeWordmark() {
        val styled = gradientAnnotatedString(
            text = "Shiroko",
            gradientColors = listOf(Color.Red, Color.Blue),
        )

        assertEquals("Shiroko", styled.text)
        assertEquals(1, styled.spanStyles.size)
        assertEquals(0, styled.spanStyles.single().start)
        assertEquals(7, styled.spanStyles.single().end)
    }

    @Test
    public fun singleStopDegradesToPlainTextWithoutSpans() {
        val styled = gradientAnnotatedString("Shiroko", listOf(Color.Red))

        assertEquals("Shiroko", styled.text)
        assertEquals(0, styled.spanStyles.size)
    }

    @Test
    public fun emptyColorListDegradesToPlainText() {
        val styled = gradientAnnotatedString("Shiroko", emptyList())

        assertEquals("Shiroko", styled.text)
        assertEquals(0, styled.spanStyles.size)
    }
}
