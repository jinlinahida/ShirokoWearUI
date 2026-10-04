package io.github.jinlinahida.shirokowear.ui

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * A tactile gesture, independent of the hardware that has to render it.
 *
 * Steps with amplitude 0 are silent gaps, which is exactly how
 * [VibrationEffect.createWaveform] reads its timing array — so a waveform
 * converts to platform data without reinterpretation.
 */
public data class ShirokoWearStep(public val durationMs: Long, public val amplitude: Int)

public data class ShirokoWearWaveform(public val steps: List<ShirokoWearStep>) {

    public val timingsMs: LongArray
        get() = LongArray(steps.size + 1) { index -> if (index == 0) 0L else steps[index - 1].durationMs }

    public val amplitudes: IntArray
        get() = IntArray(steps.size + 1) { index -> if (index == 0) 0 else steps[index - 1].amplitude }

    /** Peak drive, used to compare how assertive two gestures feel. */
    public val peakAmplitude: Int
        get() = steps.maxOfOrNull { it.amplitude } ?: 0

    public val totalDurationMs: Long
        get() = steps.sumOf { it.durationMs }

    public fun toEffect(): VibrationEffect = if (steps.size == 1) {
        VibrationEffect.createOneShot(steps[0].durationMs, steps[0].amplitude)
    } else {
        VibrationEffect.createWaveform(timingsMs, amplitudes, -1)
    }
}

/**
 * The gesture vocabulary. Named after physical sensations rather than the
 * screens that first used them, so a health app and a card app reach for the
 * same gesture without importing each other's domain words.
 */
public enum class ShirokoWearHapticKind {
    /** Single key-like strike. */
    CLICK,

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
 * Pure waveform table. Kept separate from the dispatcher so the tactile design
 * is assertable on the JVM (see `ShirokoWearHapticsTest`).
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

/**
 * Per-vendor haptic constants, mirroring what Google's Wear Compose dispatches.
 *
 * Galaxy Watch exposes tuned LRA waveforms as private constants; dispatching the
 * standard Android constant there yields the buzzy, over-travelled click that
 * watch users notice immediately.
 */
public object ShirokoWearHapticVendorConstants {

    public val isGalaxyWatch: Boolean by lazy {
        (Build.MANUFACTURER ?: "").contains("Samsung", ignoreCase = true) &&
            (Build.MODEL ?: "").matches(Regex("^SM-R.*$"))
    }

    public val isWear4OrLater: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    /** Soft graduation tick: fine steps, gesture threshold, physiological micro-beat. */
    public val TICK: Int = when {
        isGalaxyWatch -> 101
        isWear4OrLater -> 18
        else -> HapticFeedbackConstants.CLOCK_TICK
    }

    /** Crisp keystroke: cards, buttons, focus landing on an item. */
    public val CLICK: Int = when {
        isGalaxyWatch -> 102
        isWear4OrLater -> 19
        else -> HapticFeedbackConstants.KEYBOARD_TAP
    }

    /** Heavy damper: end of travel, flip settle, strong press. */
    public val LIMIT: Int = when {
        isGalaxyWatch -> 50107
        isWear4OrLater -> 20
        else -> HapticFeedbackConstants.CONTEXT_CLICK
    }
}

/**
 * Direct LRA drive.
 *
 * Two dispatch paths on purpose. `performHapticFeedback` with
 * [HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING] is the only route that
 * wakes the Samsung HAL into playing its tuned waveform, but it needs a decor
 * view; with no view (background sensor callbacks, gesture detectors) the engine
 * drives the vibrator itself with `USAGE_PHYSICAL_EMULATION` so the pulse is
 * never silently dropped by the system's UI-feedback policy.
 */
public class ShirokoWearHaptics internal constructor(
    private val context: Context,
    public val intensity: ShirokoWearHapticIntensity,
    public val enabled: Boolean,
) {

    public fun play(kind: ShirokoWearHapticKind): Unit = play(kind, intensity)

    public fun play(
        kind: ShirokoWearHapticKind,
        intensity: ShirokoWearHapticIntensity,
    ): Unit {
        if (!enabled) return

        val vibrator = resolveVibrator(context)
        if (vibrator != null && vibrator.hasVibrator()) {
            vibratePhysical(vibrator, shirokoWearWaveform(kind, intensity).toEffect())
            return
        }

        val constant = when (kind) {
            ShirokoWearHapticKind.FLIP,
            ShirokoWearHapticKind.IMPACT_MULTIPLE,
            ShirokoWearHapticKind.KNOCK,
            -> ShirokoWearHapticVendorConstants.LIMIT

            ShirokoWearHapticKind.BACK,
            ShirokoWearHapticKind.PULSE_WAVE,
            -> ShirokoWearHapticVendorConstants.TICK

            else -> when (intensity) {
                ShirokoWearHapticIntensity.LIGHT -> HapticFeedbackConstants.KEYBOARD_TAP
                ShirokoWearHapticIntensity.STANDARD -> HapticFeedbackConstants.VIRTUAL_KEY
                ShirokoWearHapticIntensity.STRONG -> HapticFeedbackConstants.CONFIRM
            }
        }
        performViewHaptic(context, constant, shirokoWearWaveform(kind, intensity))
    }

    public fun click(): Unit = play(ShirokoWearHapticKind.CLICK)

    public fun toggle(on: Boolean): Unit =
        play(if (on) ShirokoWearHapticKind.TOGGLE_ON else ShirokoWearHapticKind.TOGGLE_OFF)

    public fun success(): Unit = play(ShirokoWearHapticKind.TOGGLE_ON)

    public fun flip(): Unit = play(ShirokoWearHapticKind.FLIP)

    public fun back(): Unit = play(ShirokoWearHapticKind.BACK)

    public fun knock(): Unit = play(ShirokoWearHapticKind.KNOCK)

    public fun pulseWave(): Unit = play(ShirokoWearHapticKind.PULSE_WAVE)

    public fun impact(multiple: Boolean): Unit =
        play(if (multiple) ShirokoWearHapticKind.IMPACT_MULTIPLE else ShirokoWearHapticKind.IMPACT_SINGLE)
}

/**
 * The gate is the API: intensity and the master switch are read from the theme
 * here, so a call site cannot fire the motor behind a muted user's back.
 */
@Composable
public fun rememberShirokoWearHaptics(
    intensity: ShirokoWearHapticIntensity = ShirokoWearTheme.hapticIntensity,
    enabled: Boolean = ShirokoWearTheme.hapticFeedbackEnabled,
): ShirokoWearHaptics {
    val context = LocalContext.current
    return remember(context, intensity, enabled) {
        ShirokoWearHaptics(context.applicationContext ?: context, intensity, enabled)
    }
}

private var cachedVibrator: Vibrator? = null
private var vibratorResolved = false

private fun resolveVibrator(context: Context): Vibrator? {
    if (!vibratorResolved) {
        val appContext = context.applicationContext ?: context
        cachedVibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            appContext.getSystemService(VibratorManager::class.java)?.defaultVibrator
                ?: appContext.getSystemService(Vibrator::class.java)
        } else {
            @Suppress("DEPRECATION")
            appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
        vibratorResolved = true
    }
    return cachedVibrator
}

private fun performViewHaptic(
    context: Context,
    constant: Int,
    fallback: ShirokoWearWaveform,
) {
    val view = findDecorView(context)
    val performed = if (view != null) {
        try {
            view.performHapticFeedback(
                constant,
                HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING,
            )
        } catch (_: Throwable) {
            false
        }
    } else {
        false
    }
    if (performed) return

    val vibrator = resolveVibrator(context) ?: return
    if (!vibrator.hasVibrator()) return
    vibrateTouch(vibrator, fallback.toEffect())
}

private fun findDecorView(context: Context): android.view.View? {
    var current: Context? = context
    while (current is android.content.ContextWrapper) {
        if (current is android.app.Activity) {
            return current.window?.decorView
        }
        current = current.baseContext
    }
    return null
}

private fun vibratePhysical(vibrator: Vibrator, effect: VibrationEffect) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Api30Helper.vibrateWith(vibrator, effect, Api30Helper.physicalAttributes)
        } else {
            vibrator.vibrate(effect)
        }
    } catch (_: Throwable) {
        try {
            vibrator.vibrate(effect)
        } catch (_: Throwable) {
            // Stripped-down vendor builds that swallow every request.
        }
    }
}

private fun vibrateTouch(vibrator: Vibrator, effect: VibrationEffect) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Api30Helper.vibrateWith(vibrator, effect, Api30Helper.touchAttributes)
        } else {
            vibrator.vibrate(effect)
        }
    } catch (_: Throwable) {
        try {
            vibrator.vibrate(effect)
        } catch (_: Throwable) {
            // Stripped-down vendor builds that swallow every request.
        }
    }
}

@RequiresApi(Build.VERSION_CODES.R)
private object Api30Helper {
    val touchAttributes: android.os.VibrationAttributes by lazy {
        android.os.VibrationAttributes.Builder()
            .setUsage(android.os.VibrationAttributes.USAGE_TOUCH)
            .build()
    }

    /**
     * `USAGE_PHYSICAL_EMULATION` (33+) is what makes a skeuomorphic strike
     * survive the system's "mute UI feedback" policy; below 33 the touch usage
     * is the closest available.
     */
    val physicalAttributes: android.os.VibrationAttributes by lazy {
        val builder = android.os.VibrationAttributes.Builder()
        builder.setUsage(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                android.os.VibrationAttributes.USAGE_PHYSICAL_EMULATION
            } else {
                android.os.VibrationAttributes.USAGE_TOUCH
            },
        )
        builder.build()
    }

    fun vibrateWith(vibrator: Vibrator, effect: VibrationEffect, attributes: android.os.VibrationAttributes) {
        vibrator.vibrate(effect, attributes)
    }
}
