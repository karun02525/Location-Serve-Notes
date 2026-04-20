package `in`.antef.geonote.ui.screens.videoplayer

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import `in`.antef.geonote.utils.OnLifecycleEvent


@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerPreview(url: Uri,modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val listener = object : Player.Listener {
        override fun onPlayerError(error: PlaybackException) {
            super.onPlayerError(error)
            println("VIDEO ERROR: $error")
        }
    }

    val exoPlayer by remember {
        mutableStateOf(
            ExoPlayer.Builder(context)
                .build()
                .apply {
                    addListener(listener)
                }
        )
    }

    val mediaItem by remember(url) { mutableStateOf(MediaItem.fromUri(url)) }
    var currentTimeStamp by remember { mutableLongStateOf(0L) }

    OnLifecycleEvent { _, event ->
        when (event) {
            Lifecycle.Event.ON_START -> {
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.prepare()
                exoPlayer.playWhenReady = true // Ensure auto-play
            }

            Lifecycle.Event.ON_RESUME -> {
                if (currentTimeStamp != 0L && !exoPlayer.isReleased) {
                    exoPlayer.seekTo(currentTimeStamp)
                }
                exoPlayer.playWhenReady = true // Resume auto-play
            }

            Lifecycle.Event.ON_PAUSE -> {
                currentTimeStamp = exoPlayer.currentPosition
                exoPlayer.pause()
            }

            Lifecycle.Event.ON_DESTROY -> {
                exoPlayer.release()
            }

            else -> Unit
        }
    }
    AndroidView(
        modifier = modifier,
        factory = { context ->
            PlayerView(context).apply {
                player = exoPlayer
                setShowNextButton(false)
                setShowPreviousButton(false)
            }
        }
    )
}


