package io.github.jinlinahida.shirokowear.ui

/**
 * The gesture vocabulary and its waveform table — pure data, no Android types.
 *
 * Keeping this file free of `android.os` imports is what lets the coverage gate
 * measure the tactile design without a vibrator, a view or a device in the room.
 * Conversion to `VibrationEffect` lives in the dispatch layer.
 */

/** A single timed drive of the LRA; amplitude 0 is a silent gap. */
public data class ShirokoWearStep(public val durationMs: Long, public val amplitude: Int)

/**
 * A tactile gesture expressed as timed drive levels.
 *
 * Steps with amplitude 0 are gaps, which is exactly how
 * `VibrationEffect.createWaveform` reads its timing array — so a waveform converts
 * to platform data without reinterpretation.
 */
public data class ShirokoWearWaveform(public val steps: List<ShirokoWearStep>) {

    public val timingsMs: LongArray
        get() = LongArray(steps.size + 1) { index ->
            if (index == 0) 0L else steps[index - 1].durationMs
        }

    public val amplitudes: IntArray
        get() = IntArray(steps.size + 1) { index ->
            if (index == 0) 0 else steps[index - 1].amplitude
        }

    /** Peak drive, used to compare how assertive two gestures feel. */
    public val peakAmplitude: Int
        get() = steps.maxOfOrNull { it.amplitude } ?: 0

    public val totalDurationMs: Long
        get() = steps.sumOf { it.durationMs }
}

/**
 * Gesture names are physical, not domain-specific, so a health app and a card app
 * reach for the same gesture without importing each other's vocabulary.
 */
public enum class ShirokoWearHapticKind {
    /** Single key-like strike. */
    CLICK,

    /** Fine graduation: stepper, wheel detent, pager advance, slider notch. */
    TICK,

    /** Favouriting / confirming: two strikes with a deliberately wide gap. */
    CONFIRM,

    /** Detent into the on position: compact two-stage latch. */
    TOGGLE_ON,

    /** Release from the on position: single damped thud. */
    TOGGLE_OFF,

    /** Card face turning: light lift then a firm settle. */
    FLIP,

    /** Two or three objects tumbling, then landing together. */
    IMPACT_MULTIPLE,

    /** One object settling firmly. */
    IMPACT_SINGLE,

    /** Hard wooden mallet on a carved shell: single heavy strike. */
    KNOCK,

    /** Physiological pulse: systolic peak, diastolic gap, dicrotic notch. */
    PULSE_WAVE,

    /** Gesture crossed its commit threshold while retreating a page. */
    BACK,
}

/**
 * Pure waveform table. Kept separate from the dispatcher so the tactile design is
 * assertable on the JVM (see `ShirokoWearHapticsTest`).
 */
public fun shirokoWearWaveform(
    kind: ShirokoWearHapticKind,
    intensity: ShirokoWearHapticIntensity,
): ShirokoWearWaveform = when (kind) {
    ShirokoWearHapticKind.CLICK -> when (intensity) {
        ShirokoWearHapticIntensity.LIGHT -> ShirokoWearWaveform(listOf(ShirokoWearStep(8L, 170)))
        ShirokoWearHapticIntensity.STANDARD -> ShirokoWearWaveform(listOf(ShirokoWearStep(12L, 235)))
        ShirokoWearHapticIntensity.STRONG -> ShirokoWearWaveform(listOf(ShirokoWearStep(16L, 255)))
    }

    ShirokoWearHapticKind.TICK -> when (intensity) {
        // Sub-threshold ticks: too light to read as a button, firm enough to feel
        // once per detent while a wheel or stepper is spinning.
        ShirokoWearHapticIntensity.LIGHT -> ShirokoWearWaveform(listOf(ShirokoWearStep(5L, 130)))
        ShirokoWearHapticIntensity.STANDARD -> ShirokoWearWaveform(listOf(ShirokoWearStep(8L, 180)))
        ShirokoWearHapticIntensity.STRONG -> ShirokoWearWaveform(listOf(ShirokoWearStep(10L, 230)))
    }

    ShirokoWearHapticKind.CONFIRM -> when (intensity) {
        // Gap of 16-20ms between the two strikes, versus 10ms for TOGGLE_ON: this
        // has to read as "saved", not as a switch clatching.
        ShirokoWearHapticIntensity.LIGHT -> ShirokoWearWaveform(
            listOf(ShirokoWearStep(8L, 160), ShirokoWearStep(20L, 0), ShirokoWearStep(10L, 190)),
        )
        ShirokoWearHapticIntensity.STANDARD -> ShirokoWearWaveform(
            listOf(ShirokoWearStep(10L, 210), ShirokoWearStep(18L, 0), ShirokoWearStep(12L, 255)),
        )
        ShirokoWearHapticIntensity.STRONG -> ShirokoWearWaveform(
            listOf(ShirokoWearStep(12L, 255), ShirokoWearStep(16L, 0), ShirokoWearStep(14L, 255)),
        )
    }

    ShirokoWearHapticKind.TOGGLE_ON -> when (intensity) {
        ShirokoWearHapticIntensity.LIGHT -> ShirokoWearWaveform(
            listOf(ShirokoWearStep(6L, 160), ShirokoWearStep(10L, 0), ShirokoWearStep(10L, 205)),
        )
        ShirokoWearHapticIntensity.STANDARD -> ShirokoWearWaveform(
            listOf(ShirokoWearStep(8L, 220), ShirokoWearStep(10L, 0), ShirokoWearStep(12L, 255)),
        )
        ShirokoWearHapticIntensity.STRONG -> ShirokoWearWaveform(
            listOf(ShirokoWearStep(10L, 255), ShirokoWearStep(10L, 0), ShirokoWearStep(14L, 255)),
        )
    }

    ShirokoWearHapticKind.TOGGLE_OFF -> when (intensity) {
        ShirokoWearHapticIntensity.LIGHT -> ShirokoWearWaveform(listOf(ShirokoWearStep(8L, 130)))
        ShirokoWearHapticIntensity.STANDARD -> ShirokoWearWaveform(listOf(ShirokoWearStep(10L, 185)))
        ShirokoWearHapticIntensity.STRONG -> ShirokoWearWaveform(listOf(ShirokoWearStep(14L, 235)))
    }

    ShirokoWearHapticKind.FLIP -> when (intensity) {
        ShirokoWearHapticIntensity.LIGHT -> ShirokoWearWaveform(
            listOf(ShirokoWearStep(5L, 120), ShirokoWearStep(12L, 0), ShirokoWearStep(9L, 195)),
        )
        ShirokoWearHapticIntensity.STANDARD -> ShirokoWearWaveform(
            listOf(ShirokoWearStep(6L, 160), ShirokoWearStep(12L, 0), ShirokoWearStep(12L, 245)),
        )
        ShirokoWearHapticIntensity.STRONG -> ShirokoWearWaveform(
            listOf(ShirokoWearStep(8L, 200), ShirokoWearStep(12L, 0), ShirokoWearStep(15L, 255)),
        )
    }

    ShirokoWearHapticKind.IMPACT_MULTIPLE -> when (intensity) {
        ShirokoWearHapticIntensity.LIGHT -> ShirokoWearWaveform(
            listOf(
                ShirokoWearStep(6L, 140), ShirokoWearStep(12L, 0), ShirokoWearStep(10L, 200),
                ShirokoWearStep(8L, 0), ShirokoWearStep(5L, 100),
            ),
        )
        ShirokoWearHapticIntensity.STANDARD -> ShirokoWearWaveform(
            listOf(
                ShirokoWearStep(7L, 180), ShirokoWearStep(12L, 0), ShirokoWearStep(12L, 255),
                ShirokoWearStep(10L, 0), ShirokoWearStep(6L, 130),
            ),
        )
        ShirokoWearHapticIntensity.STRONG -> ShirokoWearWaveform(
            listOf(
                ShirokoWearStep(9L, 220), ShirokoWearStep(10L, 0), ShirokoWearStep(15L, 255),
                ShirokoWearStep(10L, 0), ShirokoWearStep(8L, 170),
            ),
        )
    }

    ShirokoWearHapticKind.IMPACT_SINGLE -> when (intensity) {
        ShirokoWearHapticIntensity.LIGHT -> ShirokoWearWaveform(listOf(ShirokoWearStep(8L, 170)))
        ShirokoWearHapticIntensity.STANDARD -> ShirokoWearWaveform(listOf(ShirokoWearStep(10L, 225)))
        ShirokoWearHapticIntensity.STRONG -> ShirokoWearWaveform(listOf(ShirokoWearStep(14L, 255)))
    }

    ShirokoWearHapticKind.KNOCK -> when (intensity) {
        ShirokoWearHapticIntensity.LIGHT -> ShirokoWearWaveform(listOf(ShirokoWearStep(16L, 220)))
        ShirokoWearHapticIntensity.STANDARD -> ShirokoWearWaveform(listOf(ShirokoWearStep(24L, 255)))
        ShirokoWearHapticIntensity.STRONG -> ShirokoWearWaveform(listOf(ShirokoWearStep(32L, 255)))
    }

    ShirokoWearHapticKind.PULSE_WAVE -> when (intensity) {
        ShirokoWearHapticIntensity.LIGHT -> ShirokoWearWaveform(
            listOf(ShirokoWearStep(12L, 190), ShirokoWearStep(26L, 0), ShirokoWearStep(8L, 120)),
        )
        ShirokoWearHapticIntensity.STANDARD -> ShirokoWearWaveform(
            listOf(ShirokoWearStep(16L, 245), ShirokoWearStep(24L, 0), ShirokoWearStep(10L, 165)),
        )
        ShirokoWearHapticIntensity.STRONG -> ShirokoWearWaveform(
            listOf(ShirokoWearStep(18L, 255), ShirokoWearStep(22L, 0), ShirokoWearStep(12L, 200)),
        )
    }

    ShirokoWearHapticKind.BACK -> when (intensity) {
        ShirokoWearHapticIntensity.LIGHT -> ShirokoWearWaveform(listOf(ShirokoWearStep(7L, 160)))
        ShirokoWearHapticIntensity.STANDARD -> ShirokoWearWaveform(listOf(ShirokoWearStep(11L, 220)))
        ShirokoWearHapticIntensity.STRONG -> ShirokoWearWaveform(listOf(ShirokoWearStep(14L, 255)))
    }
}
