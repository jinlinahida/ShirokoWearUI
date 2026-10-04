package io.github.jinlinahida.shirokowear.ui.intro

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import io.github.jinlinahida.shirokowear.ui.ShirokoWearTheme
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Where the looping backdrop clip comes from.
 *
 * The library never bundles the video: a multi-megabyte loop belongs to one app's
 * brand, not to a design system. An app names its own `res/raw` entry, or points at
 * a file/uri it already owns.
 */
public sealed interface ShirokoWearVideoSource {
    /**
     * Copies [resId] from the app's `res/raw` into the **cache** directory on first
     * use. Cache rather than files dir: a decorative loop must stay reclaimable,
     * and if the system evicts it the next launch simply re-copies it.
     */
    public data class CachedResource(val resId: Int, val fileName: String) : ShirokoWearVideoSource

    public data class FromFile(val file: File) : ShirokoWearVideoSource

    public data class FromUri(val uri: Uri) : ShirokoWearVideoSource
}

/**
 * Full-bleed looping video backdrop with an optional static poster underneath.
 *
 * `MediaPlayer` + `SurfaceView` is easy to get subtly wrong: every callback arrives
 * asynchronously, so touching the player after release crashes, and the surface can
 * be created either before or after the player finishes preparing. The invariants
 * encoded here are the ones that cost real debugging time:
 *
 * - [isPlaybackEnabled] is a gate, not a hint. A Compose screen stays composed while
 *   its Activity is in the background, so a loop would keep decoding with nobody
 *   watching. Pass the host's resumed state in; the library deliberately holds no
 *   lifecycle dependency of its own.
 * - With animations off, no `MediaPlayer` and no `SurfaceView` are created at all —
 *   on a watch that is a battery decision, not a preference.
 * - Every failure path (copy, IO, illegal state, unsupported codec) leaves the
 *   poster showing. A backdrop is decoration and must never blank a screen.
 *
 * The video is stretched to the surface by the decoder, not re-scaled here: a
 * `SurfaceView` cannot be crop-fitted like an [Image], so the clip should be authored
 * to the target aspect ratio.
 */
@Composable
public fun ShirokoWearVideoBackdrop(
    modifier: Modifier = Modifier,
    videoSource: ShirokoWearVideoSource? = null,
    poster: Painter? = null,
    posterContentScale: ContentScale = ContentScale.Crop,
    isPlaybackEnabled: Boolean = true,
    animationsEnabled: Boolean = ShirokoWearTheme.animationsEnabled,
    content: @Composable BoxScope.() -> Unit = {},
) {
    Box(modifier = modifier.fillMaxSize()) {
        if (poster != null) {
            Image(
                painter = poster,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = posterContentScale,
            )
        }

        val canPlay = videoSource != null && animationsEnabled && isPlaybackEnabled
        if (canPlay && videoSource != null) {
            VideoLayer(source = videoSource)
        }

        content()
    }
}

@Composable
private fun VideoLayer(source: ShirokoWearVideoSource) {
    val context = LocalContext.current
    val player = remember {
        MediaPlayer().apply {
            isLooping = true
            setVolume(0f, 0f)
        }
    }

    var isReleased by remember { mutableStateOf(false) }
    var isPrepared by remember { mutableStateOf(false) }
    var surfaceHolder by remember { mutableStateOf<android.view.SurfaceHolder?>(null) }

    fun attachToSurface() {
        val holder = surfaceHolder
        if (isReleased || holder == null || !holder.surface.isValid) return
        runCatching {
            player.setDisplay(holder)
            if (!player.isPlaying) player.start()
        }
    }

    LaunchedEffect(source) {
        withContext(Dispatchers.IO) {
            if (isReleased) return@withContext
            runCatching {
                when (source) {
                    is ShirokoWearVideoSource.CachedResource -> {
                        val file = cachedVideoFile(context, source)
                        player.setDataSource(file.absolutePath)
                    }

                    is ShirokoWearVideoSource.FromFile ->
                        player.setDataSource(source.file.absolutePath)

                    is ShirokoWearVideoSource.FromUri ->
                        // The Context overload is required for content:// uris; a raw
                        // uri string cannot be resolved through the media provider.
                        player.setDataSource(context, source.uri)
                }
            }.onSuccess {
                player.setOnPreparedListener {
                    isPrepared = true
                    attachToSurface()
                }
                player.prepareAsync()
            }
            // On failure nothing is prepared, so the poster stays visible.
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            isReleased = true
            runCatching { if (isPrepared) player.stop() }
            runCatching { player.release() }
        }
    }

    AndroidView(
        factory = { viewContext ->
            android.view.SurfaceView(viewContext).apply {
                holder.addCallback(
                    object : android.view.SurfaceHolder.Callback {
                        override fun surfaceCreated(holder: android.view.SurfaceHolder) {
                            surfaceHolder = holder
                            if (isPrepared) attachToSurface()
                        }

                        override fun surfaceChanged(
                            holder: android.view.SurfaceHolder,
                            format: Int,
                            width: Int,
                            height: Int,
                        ) {
                            surfaceHolder = holder
                            if (isPrepared) attachToSurface()
                        }

                        override fun surfaceDestroyed(holder: android.view.SurfaceHolder) {
                            surfaceHolder = null
                            // Detaching the display keeps a released or preparing player safe.
                            if (!isReleased) runCatching { player.setDisplay(null) }
                        }
                    },
                )
            }
        },
        modifier = Modifier.fillMaxSize(),
    )
}

private fun cachedVideoFile(context: Context, source: ShirokoWearVideoSource.CachedResource): File {
    val file = File(context.cacheDir, source.fileName)
    if (!file.exists() || file.length() == 0L) {
        context.resources.openRawResource(source.resId).use { input ->
            file.outputStream().use { output -> input.copyTo(output) }
        }
    }
    return file
}
