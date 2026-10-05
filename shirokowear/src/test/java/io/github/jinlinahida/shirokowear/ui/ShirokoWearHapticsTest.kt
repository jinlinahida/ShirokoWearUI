package io.github.jinlinahida.shirokowear.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The tactile design is a table, so the table is what gets tested: every gesture
 * must order LIGHT < STANDARD < STRONG in peak drive, and every waveform must be
 * structurally valid for VibrationEffect (leading 0 delay, matching lengths).
 *
 * Whether a Galaxy Watch actually reproduces these waveforms can only be checked
 * on that device — see the verification policy in AGENTS.md.
 */
public class ShirokoWearHapticsTest {

    private val kinds = ShirokoWearHapticKind.entries

    @Test
    public fun everyWaveformIsStructurallyValid() {
        for (kind in kinds) {
            for (intensity in ShirokoWearHapticIntensity.entries) {
                val waveform = shirokoWearWaveform(kind, intensity)
                val timings = waveform.timingsMs
                val amplitudes = waveform.amplitudes

                assertEquals(
                    "$kind/$intensity timings and amplitudes must align",
                    amplitudes.size,
                    timings.size,
                )
                assertEquals("first timing is the initial delay", 0L, timings[0])
                assertEquals("first amplitude must be silent", 0, amplitudes[0])
                assertTrue("$kind/$intensity needs at least one pulse", waveform.steps.isNotEmpty())
                assertTrue(
                    "$kind/$intensity amplitude out of range",
                    waveform.steps.all { it.amplitude in 1..255 || it.amplitude == 0 },
                )
            }
        }
    }

    @Test
    public fun intensityNeverLowersPeakDrive() {
        for (kind in kinds) {
            val light = shirokoWearWaveform(kind, ShirokoWearHapticIntensity.LIGHT).peakAmplitude
            val standard = shirokoWearWaveform(kind, ShirokoWearHapticIntensity.STANDARD).peakAmplitude
            val strong = shirokoWearWaveform(kind, ShirokoWearHapticIntensity.STRONG).peakAmplitude

            assertTrue("$kind light must be <= strong", light <= strong)
            if (kind != ShirokoWearHapticKind.IMPACT_MULTIPLE) {
                assertTrue("$kind standard must exceed light", standard > light)
            }
        }
    }

    @Test
    public fun clickMatchesCalibratedLraDrive() {
        assertEquals(
            ShirokoWearWaveform(listOf(ShirokoWearStep(12L, 235))),
            shirokoWearWaveform(
                ShirokoWearHapticKind.CLICK,
                ShirokoWearHapticIntensity.STANDARD,
            ),
        )
    }

    @Test
    public fun multiStageGesturesHaveSilentGapsBetweenPulses() {
        val flip = shirokoWearWaveform(
            ShirokoWearHapticKind.FLIP,
            ShirokoWearHapticIntensity.STANDARD,
        )
        assertEquals(3, flip.steps.size)
        assertEquals("lift, gap, settle", 0, flip.steps[1].amplitude)
        assertEquals(30L, flip.totalDurationMs)
    }

    @Test
    public fun toggleOnIsTwoStageLatchAndOffIsSingleThud() {
        val on = shirokoWearWaveform(
            ShirokoWearHapticKind.TOGGLE_ON,
            ShirokoWearHapticIntensity.STANDARD,
        )
        val off = shirokoWearWaveform(
            ShirokoWearHapticKind.TOGGLE_OFF,
            ShirokoWearHapticIntensity.STANDARD,
        )
        assertEquals(3, on.steps.size)
        assertEquals(1, off.steps.size)
    }

    /** A detent tick has to stay far below a click, or a spinning wheel feels like hammering. */
    @Test
    public fun tickIsLighterThanClickAtEveryLevel() {
        for (intensity in ShirokoWearHapticIntensity.entries) {
            val tick = shirokoWearWaveform(ShirokoWearHapticKind.TICK, intensity)
            val click = shirokoWearWaveform(ShirokoWearHapticKind.CLICK, intensity)
            assertTrue(
                "$intensity tick ${tick.peakAmplitude} must stay below click ${click.peakAmplitude}",
                tick.peakAmplitude < click.peakAmplitude,
            )
            assertEquals(1, tick.steps.size)
        }

        assertEquals(
            ShirokoWearWaveform(listOf(ShirokoWearStep(8L, 180))),
            shirokoWearWaveform(ShirokoWearHapticKind.TICK, ShirokoWearHapticIntensity.STANDARD),
        )
    }

    /**
     * Confirm reads as "saved" rather than "switch clatched" purely because of the
     * gap: 16-20ms of silence between the strikes against TOGGLE_ON's 10ms.
     */
    @Test
    public fun confirmGapIsWiderThanToggleLatch() {
        val confirm = shirokoWearWaveform(
            ShirokoWearHapticKind.CONFIRM,
            ShirokoWearHapticIntensity.STANDARD,
        )
        val latch = shirokoWearWaveform(
            ShirokoWearHapticKind.TOGGLE_ON,
            ShirokoWearHapticIntensity.STANDARD,
        )

        assertEquals(3, confirm.steps.size)
        assertEquals(0, confirm.steps[1].amplitude)
        assertEquals(18L, confirm.steps[1].durationMs)
        assertTrue(
            "confirm gap ${confirm.steps[1].durationMs} must exceed latch ${latch.steps[1].durationMs}",
            confirm.steps[1].durationMs > latch.steps[1].durationMs,
        )
    }

    /**
     * Apps persist these levels as whatever their settings screen showed — an enum
     * name, a localised label, or a bare ordinal. Nothing recognisable may reset the
     * user's choice to STANDARD behind their back, but unparseable junk must still
     * resolve to a safe default instead of throwing.
     */
    @Test
    public fun persistedIntensityResolvesTolerantly() {
        assertEquals(ShirokoWearHapticIntensity.STRONG, ShirokoWearHapticIntensity.fromPersisted("strong"))
        assertEquals(ShirokoWearHapticIntensity.LIGHT, ShirokoWearHapticIntensity.fromPersisted(" LIGHT "))
        assertEquals(ShirokoWearHapticIntensity.STANDARD, ShirokoWearHapticIntensity.fromPersisted("1"))
        assertEquals(
            ShirokoWearHapticIntensity.STRONG,
            ShirokoWearHapticIntensity.fromPersisted(
                "强劲",
                labelsByValue = mapOf(ShirokoWearHapticIntensity.STRONG to "强劲"),
            ),
        )
        assertEquals(ShirokoWearHapticIntensity.STANDARD, ShirokoWearHapticIntensity.fromPersisted(null))
        assertEquals(ShirokoWearHapticIntensity.STANDARD, ShirokoWearHapticIntensity.fromPersisted("garbage"))
    }
}
