package io.github.jinlinahida.shirokowear.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.circle
import androidx.graphics.shapes.star
import androidx.graphics.shapes.toPath
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text

/**
 * Material 3 Expressive shape morph: sparkle → circle → scallop → squircle → back,
 * one 2400ms cycle split into four 600ms stages, with a slow 90° rotation per
 * stage so the geometry feels driven rather than looping.
 *
 * With animations disabled it renders the first stage statically and burns no
 * frame budget — the shape is the identity of this loader, the motion is not.
 */
@Composable
public fun ShirokoWearMorphingLoader(
    modifier: Modifier = Modifier,
    size: Dp = 38.dp,
    color: Color = MaterialTheme.colorScheme.primary,
    animationsEnabled: Boolean = ShirokoWearTheme.animationsEnabled,
) {
    val sparkle = remember {
        RoundedPolygon.star(
            numVerticesPerRadius = 4,
            innerRadius = 0.5f,
            rounding = CornerRounding(0.2f),
        )
    }
    val circle = remember { RoundedPolygon.circle(numVertices = 16) }
    val scallop = remember {
        RoundedPolygon.star(
            numVerticesPerRadius = 8,
            innerRadius = 0.75f,
            rounding = CornerRounding(0.15f),
        )
    }
    val squircle = remember {
        RoundedPolygon(numVertices = 4, rounding = CornerRounding(0.35f))
    }

    val morphs = remember(sparkle, circle, scallop, squircle) {
        listOf(
            Morph(sparkle, circle),
            Morph(circle, scallop),
            Morph(scallop, squircle),
            Morph(squircle, sparkle),
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "shirokoMorphShape")
    val rawProgress by if (animationsEnabled) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 4f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2400, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "shirokoMorphProgress",
        )
    } else {
        remember { mutableFloatStateOf(0f) }
    }

    val rotationDegrees = rawProgress * 90f
    val stage = rawProgress.toInt().coerceIn(0, 3)
    val stageFraction = rawProgress - stage
    val morphProgress = FastOutSlowInEasing.transform(stageFraction)
    val currentMorph = morphs[stage]

    Canvas(modifier = modifier.size(size)) {
        val minDim = this.size.minDimension
        val scaleRadius = (minDim / 2f) * 0.9f
        val centerX = this.size.width / 2f
        val centerY = this.size.height / 2f

        val path = currentMorph.toPath(progress = morphProgress).asComposePath()

        withTransform({
            translate(left = centerX, top = centerY)
            rotate(degrees = rotationDegrees, pivot = Offset.Zero)
            scale(scaleX = scaleRadius, scaleY = scaleRadius, pivot = Offset.Zero)
        }) {
            drawPath(path = path, color = color)
        }
    }
}

/** Full-screen loading state: morphing mark centred above a quiet caption. */
@Composable
public fun ShirokoWearLoadingIndicator(
    label: String,
    modifier: Modifier = Modifier,
    animationsEnabled: Boolean = ShirokoWearTheme.animationsEnabled,
) {
    val dimens = ShirokoWearTheme.dimens
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(dimens.screenPadding),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            ShirokoWearMorphingLoader(
                size = 38.dp,
                animationsEnabled = animationsEnabled,
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}
