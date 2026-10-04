package io.github.jinlinahida.shirokowear.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import io.github.jinlinahida.shirokowear.ui.ShirokoWearShapes
import io.github.jinlinahida.shirokowear.ui.ShirokoWearTheme

class SampleMainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ShirokoWearTheme {
                TokenSmokeScreen()
            }
        }
    }
}

/**
 * Smoke screen for the token layer: it consumes every local, so a wrong scale
 * or a missing provider shows up as clipped text on a 40mm bezel immediately.
 */
@Composable
private fun TokenSmokeScreen() {
    val dimens = ShirokoWearTheme.dimens
    val colors = ShirokoWearTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(dimens.screenPadding),
        verticalArrangement = Arrangement.spacedBy(dimens.itemSpacing),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.shirokowear_sample_title),
            style = MaterialTheme.typography.titleLarge,
            textAlign = dimens.titleTextAlign,
        )
        Column(
            modifier = Modifier
                .background(colors.cardBackground, ShirokoWearShapes.card)
                .border(dimens.cardBorderWidth, colors.cardBorder, ShirokoWearShapes.card)
                .padding(dimens.cardPadding),
            verticalArrangement = Arrangement.spacedBy(dimens.itemSpacing / 2),
        ) {
            Text(
                text = stringResource(R.string.shirokowear_sample_body),
                style = MaterialTheme.typography.bodySmall,
                color = colors.contentPrimary,
                textAlign = TextAlign.Start,
            )
        }
    }
}
