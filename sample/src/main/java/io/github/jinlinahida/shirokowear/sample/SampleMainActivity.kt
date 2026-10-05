package io.github.jinlinahida.shirokowear.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.Text
import io.github.jinlinahida.shirokowear.navigation.ShirokoWearIntroSteps
import io.github.jinlinahida.shirokowear.navigation.ShirokoWearRoute
import io.github.jinlinahida.shirokowear.navigation.shirokoWearPageTransition
import io.github.jinlinahida.shirokowear.ui.ShirokoWearAmbient
import io.github.jinlinahida.shirokowear.ui.ShirokoWearAmbientPalette
import io.github.jinlinahida.shirokowear.ui.ShirokoWearCard
import io.github.jinlinahida.shirokowear.ui.ShirokoWearCardButton
import io.github.jinlinahida.shirokowear.ui.ShirokoWearContentScale
import io.github.jinlinahida.shirokowear.ui.ShirokoWearDetailField
import io.github.jinlinahida.shirokowear.ui.ShirokoWearLiveWaveCanvas
import io.github.jinlinahida.shirokowear.ui.ShirokoWearMorphingLoader
import io.github.jinlinahida.shirokowear.ui.ShirokoWearResultCard
import io.github.jinlinahida.shirokowear.ui.ShirokoWearScalingRotaryColumn
import io.github.jinlinahida.shirokowear.ui.ShirokoWearScreenShape
import io.github.jinlinahida.shirokowear.ui.ShirokoWearScreenTitle
import io.github.jinlinahida.shirokowear.ui.ShirokoWearSelectableButton
import io.github.jinlinahida.shirokowear.ui.ShirokoWearSettingsItem
import io.github.jinlinahida.shirokowear.ui.ShirokoWearSpotlights
import io.github.jinlinahida.shirokowear.ui.ShirokoWearStreamingCard
import io.github.jinlinahida.shirokowear.ui.ShirokoWearStreamingState
import io.github.jinlinahida.shirokowear.ui.ShirokoWearStripWaveCanvas
import io.github.jinlinahida.shirokowear.ui.ShirokoWearTheme
import io.github.jinlinahida.shirokowear.ui.ShirokoWearToggleCard
import io.github.jinlinahida.shirokowear.ui.ShirokoWearWheelPicker
import io.github.jinlinahida.shirokowear.ui.UnstableShirokoWearApi
import io.github.jinlinahida.shirokowear.ui.intro.ShirokoWearEnterPrompt
import io.github.jinlinahida.shirokowear.ui.intro.ShirokoWearGradientTitle
import io.github.jinlinahida.shirokowear.ui.intro.ShirokoWearTapToAdvance
import io.github.jinlinahida.shirokowear.ui.intro.ShirokoWearVideoBackdrop
import io.github.jinlinahida.shirokowear.ui.rememberShirokoWearHaptics
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Gallery host. Every screen here is a regression harness: a broken token, gate,
 * container or transition shows up on the watch face instead of in a diff.
 */
public class SampleMainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ShirokoWearTheme(
                contentScale = ShirokoWearContentScale.STANDARD,
                screenShape = ShirokoWearScreenShape.ROUND,
                ambientPalette = ShirokoWearAmbientPalette(
                    spotlights = mapOf(
                        GalleryRoute.routeKey to ShirokoWearSpotlights.default,
                        DetailRoute.routeKey to ShirokoWearSpotlights.violet,
                        IntroRoute.routeKey to ShirokoWearSpotlights.sandwood,
                        WaveformRoute.routeKey to ShirokoWearSpotlights.emerald,
                        SettingsRoute.routeKey to ShirokoWearSpotlights.amber,
                        StreamingRoute.routeKey to ShirokoWearSpotlights.teal,
                    ),
                ),
            ) {
                SampleNavigator()
            }
        }
    }
}

private data class SampleRoute(
    override val routeKey: String,
    override val depth: Int,
    override val backKey: String?,
) : ShirokoWearRoute

private val GalleryRoute = SampleRoute("gallery", 0, null)
private val DetailRoute = SampleRoute("detail", 1, "gallery")
private val IntroRoute = SampleRoute("intro", 1, "gallery")
private val WaveformRoute = SampleRoute("waveform", 1, "gallery")
private val SettingsRoute = SampleRoute("settings", 1, "gallery")
private val StreamingRoute = SampleRoute("streaming", 1, "gallery")

@Composable
private fun SampleNavigator() {
    var current by remember { mutableStateOf<SampleRoute>(GalleryRoute) }
    var from by remember { mutableStateOf<SampleRoute?>(null) }
    val haptics = rememberShirokoWearHaptics()

    fun navigateTo(target: SampleRoute, onArrive: () -> Unit) {
        from = current
        current = target
        onArrive()
    }

    ShirokoWearAmbient(spotlightKey = current.routeKey) {
        AnimatedContent(
            targetState = current,
            transitionSpec = {
                shirokoWearPageTransition(from, current)
            },
            label = "sampleRoutes",
        ) { target ->
            when (target.routeKey) {
                GalleryRoute.routeKey -> ComponentGallery(
                    onOpenDetail = { navigateTo(DetailRoute) { haptics.click() } },
                    onOpenIntro = { navigateTo(IntroRoute) { haptics.click() } },
                    onOpenWaveform = { navigateTo(WaveformRoute) { haptics.click() } },
                    onOpenSettings = { navigateTo(SettingsRoute) { haptics.click() } },
                    onOpenStreaming = { navigateTo(StreamingRoute) { haptics.click() } },
                )

                IntroRoute.routeKey -> IntroScreen(
                    onFinish = { navigateTo(GalleryRoute) { haptics.back() } },
                )

                WaveformRoute.routeKey -> WaveformScreen(
                    onBack = { navigateTo(GalleryRoute) { haptics.back() } },
                )

                SettingsRoute.routeKey -> SettingsDemoScreen(
                    onBack = { navigateTo(GalleryRoute) { haptics.back() } },
                )

                StreamingRoute.routeKey -> StreamingDemoScreen(
                    onBack = { navigateTo(GalleryRoute) { haptics.back() } },
                )

                else -> DetailScreen(
                    onBack = { navigateTo(GalleryRoute) { haptics.back() } },
                )
            }
        }
    }
}

private val WheelItems = (1..12).map { value -> "%02d".format(value) }

@Composable
private fun ComponentGallery(
    onOpenDetail: () -> Unit,
    onOpenIntro: () -> Unit,
    onOpenWaveform: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenStreaming: () -> Unit,
) {
    val dimens = ShirokoWearTheme.dimens
    val haptics = rememberShirokoWearHaptics()
    var selectedIndex by remember { mutableIntStateOf(0) }
    var picked by remember { mutableIntStateOf(3) }
    var toggleOn by remember { mutableStateOf(false) }

    ShirokoWearScalingRotaryColumn(itemSpacing = dimens.itemSpacing) {
        item(key = "title") {
            ShirokoWearScreenTitle(text = "ShirokoWear UI", marquee = true)
        }

        item(key = "field-card") {
            ShirokoWearResultCard {
                ShirokoWearDetailField(label = "Bezel", value = "Round 40mm")
                ShirokoWearDetailField(
                    label = "Long value uses the marquee instead of wrapping",
                    value = "Jiazi · Yichou · Bingyin · Dingmao · Wuchen",
                    marquee = true,
                )
            }
        }

        item(key = "highlight-card") {
            ShirokoWearCard(
                highlighted = toggleOn,
                onClick = {
                    toggleOn = !toggleOn
                    haptics.toggle(toggleOn)
                },
            ) {
                Text(text = if (toggleOn) "Latch on" else "Tap to latch")
            }
        }

        item(key = "wheel") {
            ShirokoWearWheelPicker(
                items = WheelItems,
                selectedIndex = picked,
                onSelectedIndexChanged = { picked = it },
                labelProvider = { it },
            )
        }

        item(key = "segmented") {
            Row(horizontalArrangement = Arrangement.spacedBy(dimens.itemSpacing)) {
                ShirokoWearSelectableButton(
                    selected = selectedIndex == 0,
                    onClick = {
                        selectedIndex = 0
                        haptics.click()
                    },
                ) {
                    Text("A", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                ShirokoWearSelectableButton(
                    selected = selectedIndex == 1,
                    onClick = {
                        selectedIndex = 1
                        haptics.click()
                    },
                ) {
                    Text("B", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }

        item(key = "loader") {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(dimens.itemSpacing / 2),
            ) {
                ShirokoWearMorphingLoader()
                ShirokoWearDetailField(label = "Stage", value = "Shape morph")
            }
        }

        item(key = "open-waveform") {
            ShirokoWearCardButton(onClick = onOpenWaveform) {
                Text(text = "Waveform Canvases")
            }
        }

        item(key = "open-settings") {
            ShirokoWearCardButton(onClick = onOpenSettings) {
                Text(text = "Settings & Toggles")
            }
        }

        item(key = "open-streaming") {
            ShirokoWearCardButton(onClick = onOpenStreaming) {
                Text(text = "Streaming Card")
            }
        }

        item(key = "open-intro") {
            ShirokoWearCardButton(onClick = onOpenIntro) {
                Text(text = "Intro flow")
            }
        }

        item(key = "open-detail") {
            ShirokoWearCardButton(onClick = onOpenDetail) {
                Text(text = "Drill down")
            }
        }
    }
}

@Composable
private fun DetailScreen(onBack: () -> Unit) {
    val dimens = ShirokoWearTheme.dimens

    ShirokoWearScalingRotaryColumn(itemSpacing = dimens.itemSpacing) {
        item(key = "title") {
            ShirokoWearScreenTitle(text = "Detail", marquee = true)
        }
        item(key = "body") {
            ShirokoWearResultCard {
                ShirokoWearDetailField(
                    label = "Ambient",
                    value = "Cross-fades to violet over 600ms on arrival",
                )
            }
        }
        item(key = "back") {
            ShirokoWearCardButton(onClick = onBack) {
                Text(text = "Pop back")
            }
        }
    }
}

private const val IntroStepCount = 3

@Composable
private fun IntroScreen(onFinish: () -> Unit) {
    var step by remember { mutableIntStateOf(0) }

    ShirokoWearTapToAdvance(
        onTap = {
            if (step >= IntroStepCount - 1) onFinish() else step += 1
        },
    ) {
        ShirokoWearIntroSteps(
            currentStep = step,
            modifier = Modifier.fillMaxSize(),
            animationsEnabled = ShirokoWearTheme.animationsEnabled,
        ) { index ->
            Box(modifier = Modifier.fillMaxSize()) {
                ShirokoWearVideoBackdrop(
                    modifier = Modifier.fillMaxSize(),
                    videoSource = null,
                )

                if (index == 0) {
                    ShirokoWearGradientTitle(
                        text = "Shiroko",
                        gradientColors = listOf(
                            ShirokoWearSpotlights.default,
                            ShirokoWearSpotlights.teal,
                        ),
                        modifier = Modifier.align(Alignment.Center),
                    )
                } else {
                    ShirokoWearResultCard(modifier = Modifier.align(Alignment.Center)) {
                        ShirokoWearDetailField(
                            label = "Step ${index + 1} of $IntroStepCount",
                            value = introStepBody(index),
                        )
                    }
                }

                ShirokoWearEnterPrompt(
                    label = if (index == IntroStepCount - 1) "Finish" else "Tap to continue",
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp),
                )
            }
        }
    }
}

private fun introStepBody(index: Int): String = when (index) {
    1 -> "Consent step: cards stay translucent so this sandwood light bleeds through"
    else -> "Last step: the same tap finishes instead of advancing"
}

@OptIn(UnstableShirokoWearApi::class)
@Composable
private fun WaveformScreen(onBack: () -> Unit) {
    val dimens = ShirokoWearTheme.dimens
    val livePoints = remember { listOf(0.2f, 0.25f, 0.45f, 0.85f, 0.35f, 0.5f, 0.2f, 0.3f, 0.9f) }
    val stripPoints = remember {
        (0..40).map { i ->
            (kotlin.math.sin(i * 0.4f) * 0.35f + 0.5f).coerceIn(0.1f, 0.9f)
        }
    }

    ShirokoWearScalingRotaryColumn(itemSpacing = dimens.itemSpacing) {
        item(key = "title") {
            ShirokoWearScreenTitle(text = "Waveforms", marquee = true)
        }
        item(key = "live-wave") {
            ShirokoWearResultCard {
                ShirokoWearDetailField(label = "Live Wave Canvas", value = "Bezier glow + nucleus")
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                ) {
                    ShirokoWearLiveWaveCanvas(points = livePoints)
                }
            }
        }
        item(key = "strip-wave") {
            ShirokoWearResultCard {
                ShirokoWearDetailField(label = "Strip Wave Canvas", value = "Dense polyline + baseline")
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                ) {
                    ShirokoWearStripWaveCanvas(points = stripPoints)
                }
            }
        }
        item(key = "back") {
            ShirokoWearCardButton(onClick = onBack) {
                Text(text = "Pop back")
            }
        }
    }
}

@OptIn(UnstableShirokoWearApi::class)
@Composable
private fun SettingsDemoScreen(onBack: () -> Unit) {
    val dimens = ShirokoWearTheme.dimens
    var toggleState by remember { mutableStateOf(true) }
    var rotaryState by remember { mutableStateOf(false) }

    ShirokoWearScalingRotaryColumn(itemSpacing = dimens.itemSpacing) {
        item(key = "title") {
            ShirokoWearScreenTitle(text = "Settings", marquee = true)
        }
        item(key = "item-motion") {
            ShirokoWearSettingsItem(
                title = "Appearance",
                subtitle = "Round 40mm bezel with amber ambient",
                onClick = {},
            )
        }
        item(key = "toggle-1") {
            ShirokoWearToggleCard(
                checked = toggleState,
                onCheckedChange = { toggleState = it },
                label = "Haptic Feedback",
                secondaryLabel = "Direct LRA tactile strikes",
            )
        }
        item(key = "toggle-2") {
            ShirokoWearToggleCard(
                checked = rotaryState,
                onCheckedChange = { rotaryState = it },
                label = "Rotary Snapping",
                secondaryLabel = "Crown detent alignment",
            )
        }
        item(key = "back") {
            ShirokoWearCardButton(onClick = onBack) {
                Text(text = "Pop back")
            }
        }
    }
}

@OptIn(UnstableShirokoWearApi::class)
@Composable
private fun StreamingDemoScreen(onBack: () -> Unit) {
    val dimens = ShirokoWearTheme.dimens
    val scope = rememberCoroutineScope()
    var streamingState by remember {
        mutableStateOf<ShirokoWearStreamingState<Unit>>(ShirokoWearStreamingState.Idle)
    }
    var activeJob by remember { mutableStateOf<Job?>(null) }

    fun startSimulation() {
        activeJob?.cancel()
        streamingState = ShirokoWearStreamingState.Loading()
        activeJob = scope.launch {
            delay(600)
            val tokens = listOf(
                "Analyzing ", "wearable ", "telemetry...\n",
                "Heart ", "rate ", "stable, ",
                "motion ", "smooth.\n",
                "All ", "systems ", "optimal.",
            )
            val acc = StringBuilder()
            for (token in tokens) {
                acc.append(token)
                streamingState = ShirokoWearStreamingState.Streaming(acc.toString())
                delay(120)
            }
            streamingState = ShirokoWearStreamingState.Completed(acc.toString())
        }
    }

    ShirokoWearScalingRotaryColumn(itemSpacing = dimens.itemSpacing) {
        item(key = "title") {
            ShirokoWearScreenTitle(text = "Streaming", marquee = true)
        }
        item(key = "card") {
            ShirokoWearStreamingCard(
                state = streamingState,
                title = "Assistant Stream",
                onCancel = {
                    activeJob?.cancel()
                    streamingState = ShirokoWearStreamingState.Idle
                },
                onReset = {
                    streamingState = ShirokoWearStreamingState.Idle
                },
                idleContent = {
                    Text(
                        text = "Tap below to simulate wearable token streaming.",
                        style = androidx.wear.compose.material3.MaterialTheme.typography.bodySmall,
                        color = androidx.wear.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    ShirokoWearCardButton(onClick = { startSimulation() }) {
                        Text(text = "Start Stream")
                    }
                },
                completedActions = {
                    ShirokoWearCardButton(onClick = { startSimulation() }) {
                        Text(text = "Run Again")
                    }
                },
            )
        }
        item(key = "back") {
            ShirokoWearCardButton(onClick = onBack) {
                Text(text = "Pop back")
            }
        }
    }
}
