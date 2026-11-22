package com.example.ui.components

import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.example.model.Post
import com.example.ui.theme.BangladeshGreen
import com.example.util.AppSoundManager
import com.example.util.VoiceAudioManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.abs

/**
 * Ensures ONLY ONE video plays audio at a time across the entire FotoFilm app.
 * Automatically pauses the previous Reel/Video immediately on swipe or when voice/music plays.
 */
object VideoPlaybackCoordinator {
    private var activeOwnerKey: String? = null
    private var activePlayer: ExoPlayer? = null

    @Synchronized
    fun claimFocus(ownerKey: String, player: ExoPlayer) {
        val prev = activePlayer
        if (prev != null && prev !== player) {
            try {
                prev.volume = 0f
                prev.playWhenReady = false
                prev.pause()
            } catch (_: Throwable) {
            }
        }
        activeOwnerKey = ownerKey
        activePlayer = player
    }

    @Synchronized
    fun pauseActiveVideo() {
        val cur = activePlayer ?: return
        try {
            cur.volume = 0f
            cur.playWhenReady = false
            cur.pause()
        } catch (_: Throwable) {
        }
    }

    @Synchronized
    fun releaseIfOwner(ownerKey: String, player: ExoPlayer) {
        if (activeOwnerKey == ownerKey || activePlayer === player) {
            activeOwnerKey = null
            activePlayer = null
        }
    }
}

data class VideoMediaSource(
    val videoUrl: String,
    val thumbnailUrl: String?
)

fun isPlayableVideoUrl(url: String?): Boolean {
    if (url.isNullOrBlank()) return false
    val lower = url.lowercase()
    return lower.startsWith("content://") ||
        lower.startsWith("file://") ||
        lower.startsWith("/") ||
        lower.contains(".mp4") ||
        lower.contains(".mov") ||
        lower.contains(".webm") ||
        lower.contains(".mkv") ||
        lower.contains(".3gp") ||
        lower.contains("/video/upload/")
}

fun resolvePlayableVideoSource(post: Post? = null, rawUrl: String? = null): VideoMediaSource {
    val candidate = rawUrl?.takeIf { it.isNotBlank() } ?: post?.images?.firstOrNull().orEmpty()
    if (isPlayableVideoUrl(candidate)) {
        val thumb = post?.images?.firstOrNull { !isPlayableVideoUrl(it) } ?: candidate
        return VideoMediaSource(videoUrl = candidate, thumbnailUrl = thumb)
    }
    return VideoMediaSource(videoUrl = "", thumbnailUrl = candidate.ifBlank { null })
}

@OptIn(UnstableApi::class)
@Composable
fun FotoFilmExoVideoSurface(
    ownerKey: String,
    videoUrl: String,
    thumbnailUrl: String?,
    isActivePage: Boolean,
    isPaused: Boolean,
    isMuted: Boolean,
    fillCrop: Boolean = true,
    repeatLoop: Boolean = true,
    seekRequestMs: Long? = null,
    onSeekConsumed: () -> Unit = {},
    onProgressUpdate: (currentSec: Int, totalSec: Int) -> Unit = { _, _ -> },
    onVideoEnded: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var isVideoRendered by remember(ownerKey, videoUrl) { mutableStateOf(false) }
    var isBuffering by remember(ownerKey, videoUrl) { mutableStateOf(true) }
    var playbackFailed by remember(ownerKey, videoUrl) { mutableStateOf(false) }

    val exoPlayer = remember(ownerKey, videoUrl) {
        ExoPlayer.Builder(context.applicationContext)
            .build()
            .apply {
                val attrs = AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                    .build()
                setAudioAttributes(attrs, true)
                repeatMode = if (repeatLoop) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
                volume = if (isMuted || !isActivePage) 0f else 1f
                val mediaUri = if (videoUrl.startsWith("/")) Uri.parse("file://$videoUrl") else Uri.parse(videoUrl)
                setMediaItem(MediaItem.fromUri(mediaUri))
                prepare()
            }
    }

    // Track ExoPlayer rendering, buffering, and end state cleanly
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onRenderedFirstFrame() {
                isVideoRendered = true
                isBuffering = false
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> isBuffering = true
                    Player.STATE_READY -> {
                        isBuffering = false
                        isVideoRendered = true
                    }
                    Player.STATE_ENDED -> {
                        isBuffering = false
                        onVideoEnded()
                    }
                    Player.STATE_IDLE -> Unit
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                isBuffering = false
                playbackFailed = true
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            VideoPlaybackCoordinator.releaseIfOwner(ownerKey, exoPlayer)
            try {
                exoPlayer.playWhenReady = false
                exoPlayer.stop()
                exoPlayer.release()
            } catch (_: Throwable) {
            }
        }
    }

    // Immediate exclusive playback & audio switch on swipe / pause / mute change
    LaunchedEffect(exoPlayer, isActivePage, isPaused, isMuted) {
        if (isActivePage && !isPaused) {
            AppSoundManager.stopAudio()
            VoiceAudioManager.stopPlayback()
            VideoPlaybackCoordinator.claimFocus(ownerKey, exoPlayer)
            exoPlayer.volume = if (isMuted) 0f else 1f
            exoPlayer.playWhenReady = true
            exoPlayer.play()
        } else {
            exoPlayer.volume = 0f
            exoPlayer.playWhenReady = false
            exoPlayer.pause()
            if (!isActivePage) {
                exoPlayer.seekTo(0L)
            }
        }
    }

    // Handle manual seek requests (for Dialog Video Player)
    LaunchedEffect(exoPlayer, seekRequestMs) {
        if (seekRequestMs != null) {
            exoPlayer.seekTo(seekRequestMs.coerceAtLeast(0L))
            onSeekConsumed()
        }
    }

    // Pause cleanly when app goes to background
    DisposableEffect(lifecycleOwner, exoPlayer) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
                try {
                    exoPlayer.volume = 0f
                    exoPlayer.playWhenReady = false
                    exoPlayer.pause()
                } catch (_: Throwable) {
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Progress reporter loop when active
    LaunchedEffect(exoPlayer, isActivePage, isPaused) {
        while (isActive && isActivePage) {
            val durMs = exoPlayer.duration
            val posMs = exoPlayer.currentPosition
            if (durMs > 0L) {
                onProgressUpdate(
                    (posMs / 1000L).toInt().coerceAtLeast(0),
                    (durMs / 1000L).toInt().coerceAtLeast(1)
                )
            }
            delay(250L)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    useController = false
                    resizeMode = if (fillCrop) {
                        AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    } else {
                        AspectRatioFrameLayout.RESIZE_MODE_FIT
                    }
                    setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER)
                    setKeepContentOnPlayerReset(true)
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    player = exoPlayer
                }
            },
            update = { pv ->
                if (pv.player !== exoPlayer) {
                    pv.player = exoPlayer
                }
                pv.resizeMode = if (fillCrop) {
                    AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                } else {
                    AspectRatioFrameLayout.RESIZE_MODE_FIT
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Show thumbnail smoothly while buffering first frame or if fallback needed
        if ((!isVideoRendered || playbackFailed) && !thumbnailUrl.isNullOrBlank()) {
            AsyncImage(
                model = thumbnailUrl,
                contentDescription = "Video preview",
                modifier = Modifier.fillMaxSize(),
                contentScale = if (fillCrop) ContentScale.Crop else ContentScale.Fit
            )
        }

        if (isActivePage && !isPaused && isBuffering && !playbackFailed) {
            CircularProgressIndicator(
                modifier = Modifier.size(38.dp),
                color = BangladeshGreen,
                strokeWidth = 3.dp
            )
        }
    }
}
