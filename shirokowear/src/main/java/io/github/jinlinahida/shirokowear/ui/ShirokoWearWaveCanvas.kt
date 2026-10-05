package io.github.jinlinahida.shirokowear.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Real-time glowing pulse / physiological sensor wave canvas.
 *
 * Connects sample points using cubic Bezier curves, dual horizontal fade brushes,
 * and a leading energy nucleus dot at the pulse wave front.
 *
 * @param points Normalized sample points in range 0.0f..1.0f.
 * @param modifier Canvas layout modifier.
 * @param lineColor Primary tint for the core stroke and glow bloom.
 * @param glowWidth Outer laser glow bloom stroke width.
 * @param coreWidth Sharp core path stroke width.
 * @param nucleusRadius Leading energy nucleus radial gradient radius.
 * @param coreDotRadius Leading center core dot radius.
 * @param amplitudeScale Vertical excursion scale relative to available height.
 */
@UnstableShirokoWearApi
@Composable
public fun ShirokoWearLiveWaveCanvas(
    points: List<Float>,
    modifier: Modifier = Modifier,
    lineColor: Color = ShirokoWearTheme.colors.waveformLine,
    glowWidth: Dp = 5.dp,
    coreWidth: Dp = 1.8.dp,
    nucleusRadius: Dp = 7.dp,
    coreDotRadius: Dp = 2.dp,
    amplitudeScale: Float = 0.55f,
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        if (points.size < 2) return@Canvas

        val w = size.width
        val h = size.height
        val centerY = h * 0.50f
        val stepX = w / (points.size - 1)

        val path = Path()

        // 1. Cubic Bezier curve smooth interpolation
        for (i in 0 until points.size - 1) {
            val p0 = points[i]
            val p1 = points[i + 1]
            val x0 = i * stepX
            val y0 = centerY - (p0 - 0.5f) * (h * amplitudeScale)
            val x1 = (i + 1) * stepX
            val y1 = centerY - (p1 - 0.5f) * (h * amplitudeScale)

            if (i == 0) path.moveTo(x0, y0)
            val cx = (x0 + x1) / 2f
            path.cubicTo(cx, y0, cx, y1, x1, y1)
        }

        // 2. Horizontal gradient: fades from transparent at left to full glow at right
        val glowBrush = Brush.horizontalGradient(
            0.0f to Color.Transparent,
            0.15f to lineColor.copy(alpha = 0.20f),
            0.80f to lineColor.copy(alpha = 0.50f),
            1.0f to Color.White.copy(alpha = 0.90f),
        )
        val coreBrush = Brush.horizontalGradient(
            0.0f to Color.Transparent,
            0.18f to lineColor.copy(alpha = 0.50f),
            0.85f to lineColor.copy(alpha = 0.95f),
            1.0f to Color.White,
        )

        // 3. Outer glow bloom
        drawPath(
            path = path,
            brush = glowBrush,
            style = Stroke(width = glowWidth.toPx(), cap = StrokeCap.Round),
        )

        // 4. Core trajectory line
        drawPath(
            path = path,
            brush = coreBrush,
            style = Stroke(width = coreWidth.toPx(), cap = StrokeCap.Round),
        )

        // 5. Wavefront energy nucleus
        val lastIdx = points.lastIndex
        val lastX = lastIdx * stepX
        val lastY = centerY - (points[lastIdx] - 0.5f) * (h * amplitudeScale)

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White, lineColor.copy(alpha = 0.40f), Color.Transparent),
                center = Offset(lastX, lastY),
                radius = nucleusRadius.toPx(),
            ),
            radius = nucleusRadius.toPx(),
            center = Offset(lastX, lastY),
        )
        drawCircle(
            color = Color.White,
            radius = coreDotRadius.toPx(),
            center = Offset(lastX, lastY),
        )
    }
}

/**
 * Historical / strip chart waveform canvas.
 *
 * Supports two display modes:
 * 1. High-density full strip (> 32 points): sharp lineTo rendering with baseline
 *    and equidistant time division ticks.
 * 2. Low-density typical cycle (<= 32 points): cubic Bezier smooth interpolation.
 *
 * @param points Normalized sample points in range 0.0f..1.0f.
 * @param modifier Canvas layout modifier.
 * @param lineColor Color of the waveform line.
 * @param showBaseline Whether to draw the subtle horizontal baseline.
 * @param baselineFraction Vertical fraction from bottom for baseline (default 0.20f).
 * @param showTimeTicks Whether to draw time division ticks along the bottom.
 * @param timeTickFractions Horizontal fractions for time ticks (default 0.25, 0.5, 0.75).
 */
@UnstableShirokoWearApi
@Composable
public fun ShirokoWearStripWaveCanvas(
    points: List<Float>,
    modifier: Modifier = Modifier,
    lineColor: Color = ShirokoWearTheme.colors.waveformLine,
    showBaseline: Boolean = true,
    baselineFraction: Float = 0.20f,
    showTimeTicks: Boolean = true,
    timeTickFractions: List<Float> = listOf(0.25f, 0.5f, 0.75f),
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        if (points.size < 2) return@Canvas

        val w = size.width
        val h = size.height
        val paddingV = h * 0.12f
        val availableH = h - (paddingV * 2)
        val stepX = w / (points.size - 1)

        val path = Path()
        val isFullStrip = points.size > 32

        if (isFullStrip) {
            // 1. Subtle baseline
            if (showBaseline) {
                val baselineY = h - paddingV - (baselineFraction * availableH)
                drawLine(
                    color = Color.White.copy(alpha = 0.08f),
                    start = Offset(0f, baselineY),
                    end = Offset(w, baselineY),
                    strokeWidth = 0.8.dp.toPx(),
                )
            }

            // 2. Time division tick marks
            if (showTimeTicks) {
                for (fraction in timeTickFractions) {
                    val tickX = w * fraction
                    drawLine(
                        color = Color.White.copy(alpha = 0.15f),
                        start = Offset(tickX, h - 3.dp.toPx()),
                        end = Offset(tickX, h),
                        strokeWidth = 0.8.dp.toPx(),
                    )
                }
            }

            // 3. High-density polyline
            for (i in points.indices) {
                val p = points[i]
                val x = i * stepX
                val y = h - paddingV - (p * availableH)
                if (i == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
            }

            // 4. Outer glow
            drawPath(
                path = path,
                color = lineColor.copy(alpha = 0.22f),
                style = Stroke(width = 2.4.dp.toPx(), cap = StrokeCap.Round),
            )
            // 5. Core trajectory line
            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = 1.2.dp.toPx(), cap = StrokeCap.Round),
            )
        } else {
            // Cubic Bezier smoothing for fewer sample points
            for (i in 0 until points.size - 1) {
                val p0 = points[i]
                val p1 = points[i + 1]
                val x0 = i * stepX
                val y0 = h - paddingV - (p0 * availableH)
                val x1 = (i + 1) * stepX
                val y1 = h - paddingV - (p1 * availableH)

                if (i == 0) path.moveTo(x0, y0)
                val cx = (x0 + x1) / 2f
                path.cubicTo(cx, y0, cx, y1, x1, y1)
            }

            // Outer glow
            drawPath(
                path = path,
                color = lineColor.copy(alpha = 0.35f),
                style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round),
            )
            // Core line
            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round),
            )
        }
    }
}
