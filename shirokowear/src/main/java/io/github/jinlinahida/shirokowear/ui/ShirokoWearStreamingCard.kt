package io.github.jinlinahida.shirokowear.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text

/**
 * Wear OS generative card container.
 *
 * Hosts long-running and streaming responses through [ShirokoWearStreamingState]
 * (Idle, Loading, Streaming, Completed, Error), providing tactile feedback upon
 * completion or failure, top-level stage indication, and responsive layout.
 *
 * @param state Current streaming lifecycle state.
 * @param title Header title of this section.
 * @param modifier Card modifier.
 * @param borderColor Sheen border tint.
 * @param onCancel Callback when user cancels an active stream.
 * @param onReset Callback to reset or retry from an error or completed state.
 * @param idleContent Content rendered in [ShirokoWearStreamingState.Idle].
 * @param loadingContent Custom content for [ShirokoWearStreamingState.Loading].
 * @param errorContent Custom content for [ShirokoWearStreamingState.Error].
 * @param completedActions Extra action buttons rendered beneath completed text.
 */
@UnstableShirokoWearApi
@Composable
public fun <T> ShirokoWearStreamingCard(
    state: ShirokoWearStreamingState<T>,
    title: String,
    modifier: Modifier = Modifier,
    borderColor: Color = ShirokoWearTheme.colors.cardBorder,
    onCancel: (() -> Unit)? = null,
    onReset: (() -> Unit)? = null,
    idleContent: @Composable ColumnScope.() -> Unit = {},
    loadingContent: (@Composable ColumnScope.() -> Unit)? = null,
    errorContent: (@Composable ColumnScope.(message: String, cause: Throwable?) -> Unit)? = null,
    completedActions: (@Composable ColumnScope.(fullText: String) -> Unit)? = null,
) {
    val haptics = rememberShirokoWearHaptics()
    val dimens = ShirokoWearTheme.dimens

    LaunchedEffect(state) {
        when (state) {
            is ShirokoWearStreamingState.Completed -> haptics.success()
            is ShirokoWearStreamingState.Error -> haptics.click()
            else -> Unit
        }
    }

    ShirokoWearResultCard(
        borderColor = borderColor,
        modifier = modifier,
    ) {
        // Header bar: Title + status icon / cancel action
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
            )

            when (state) {
                is ShirokoWearStreamingState.Loading,
                is ShirokoWearStreamingState.Streaming,
                -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        ShirokoWearMorphingLoader(size = 18.dp)
                        if (onCancel != null) {
                            ShirokoWearCardButton(
                                onClick = onCancel,
                                contentPadding = ShirokoWearButtonDefaults.compactContentPadding,
                                colors = ShirokoWearButtonDefaults.outlinedButtonColors(),
                            ) {
                                Text(
                                    text = stringResource(R.string.shirokowear_action_cancel),
                                    fontSize = 11.sp,
                                )
                            }
                        }
                    }
                }

                is ShirokoWearStreamingState.Completed,
                is ShirokoWearStreamingState.Error,
                -> {
                    if (onReset != null) {
                        ShirokoWearCardButton(
                            onClick = onReset,
                            contentPadding = ShirokoWearButtonDefaults.compactContentPadding,
                            colors = ShirokoWearButtonDefaults.outlinedButtonColors(),
                        ) {
                            Text(
                                text = stringResource(
                                    if (state is ShirokoWearStreamingState.Error) {
                                        R.string.shirokowear_action_retry
                                    } else {
                                        R.string.shirokowear_action_reset
                                    },
                                ),
                                fontSize = 11.sp,
                            )
                        }
                    }
                }

                ShirokoWearStreamingState.Idle -> Unit
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Body content
        AnimatedContent(
            targetState = state,
            label = "shirokoStreamingState",
        ) { targetState ->
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(dimens.itemSpacing / 2),
            ) {
                when (targetState) {
                    ShirokoWearStreamingState.Idle -> {
                        idleContent()
                    }

                    is ShirokoWearStreamingState.Loading -> {
                        if (loadingContent != null) {
                            loadingContent()
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                ShirokoWearMorphingLoader(size = 32.dp)
                                Text(
                                    text = stringResource(R.string.shirokowear_streaming_loading),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }

                    is ShirokoWearStreamingState.Streaming -> {
                        Text(
                            text = targetState.text,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }

                    is ShirokoWearStreamingState.Completed -> {
                        Text(
                            text = targetState.fullText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        if (completedActions != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            completedActions(targetState.fullText)
                        }
                    }

                    is ShirokoWearStreamingState.Error -> {
                        if (errorContent != null) {
                            errorContent(targetState.message, targetState.cause)
                        } else {
                            Text(
                                text = targetState.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }
            }
        }
    }
}
