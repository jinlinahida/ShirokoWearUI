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
}
