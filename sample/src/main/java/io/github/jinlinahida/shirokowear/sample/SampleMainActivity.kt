package io.github.jinlinahida.shirokowear.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.Text
import io.github.jinlinahida.shirokowear.ui.ShirokoWearCard
import io.github.jinlinahida.shirokowear.ui.ShirokoWearCardButton
import io.github.jinlinahida.shirokowear.ui.ShirokoWearContentScale
import io.github.jinlinahida.shirokowear.ui.ShirokoWearDetailField
import io.github.jinlinahida.shirokowear.ui.ShirokoWearMorphingLoader
import io.github.jinlinahida.shirokowear.ui.ShirokoWearResultCard
import io.github.jinlinahida.shirokowear.ui.ShirokoWearScalingRotaryColumn
import io.github.jinlinahida.shirokowear.ui.ShirokoWearScreenTitle
import io.github.jinlinahida.shirokowear.ui.ShirokoWearScreenShape
import io.github.jinlinahida.shirokowear.ui.ShirokoWearSelectableButton
import io.github.jinlinahida.shirokowear.ui.ShirokoWearTheme
import io.github.jinlinahida.shirokowear.ui.ShirokoWearWheelPicker
import io.github.jinlinahida.shirokowear.ui.rememberShirokoWearHaptics

/**
 * Gallery host. Every screen here is a regression harness: if a token, a gate or
 * a container stops working, it shows up on the watch face rather than in a diff.
 */
class SampleMainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ShirokoWearTheme(
                contentScale = ShirokoWearContentScale.STANDARD,
                screenShape = ShirokoWearScreenShape.ROUND,
            ) {
                ComponentGallery()
            }
        }
    }
}

private val WheelItems = (1..12).map { value -> "%02d".format(value) }

@Composable
private fun ComponentGallery() {
    val dimens = ShirokoWearTheme.dimens
    val haptics = rememberShirokoWearHaptics()
    var selectedIndex by remember { mutableIntStateOf(0) }
    var picked by remember { mutableIntStateOf(3) }
    var toggleOn by remember { mutableStateOf(false) }

    ShirokoWearScalingRotaryColumn(itemSpacing = dimens.itemSpacing) {
        item(key = "title") {
            ShirokoWearScreenTitle(
                text = "ShirokoWear UI",
                marquee = true,
            )
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
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(dimens.itemSpacing / 2),
            ) {
                ShirokoWearMorphingLoader()
                ShirokoWearDetailField(label = "Stage", value = "Shape morph")
            }
        }

        item(key = "button") {
            ShirokoWearCardButton(
                onClick = { haptics.flip() },
            ) {
                Text(text = "Flip strike")
            }
        }
    }
}
