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
 * Platform conversion, kept out of the pure table so that file has no Android
 * imports and can be measured without a vibrator in the room.
 */
private fun ShirokoWearWaveform.toEffect(): VibrationEffect = if (steps.size == 1) {
    VibrationEffect.createOneShot(steps[0].durationMs, steps[0].amplitude)
} else {
    VibrationEffect.createWaveform(timingsMs, amplitudes, -1)
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
