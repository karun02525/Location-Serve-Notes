package `in`.antef.geonote.ui.screens.videoplayer

import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import `in`.antef.geonote.R
import `in`.antef.geonote.utils.OnLifecycleEvent


@OptIn(UnstableApi::class)
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun VideoPlayer(
    url: String = "https://geonote-dev-mobile.s3.ap-south-1.amazonaws.com/8fc818b5-8ea7-4e38-89a8-df4a17a5f043.mp4",
    onBack: () -> Unit = {},
) {
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

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                PlayerView(context).apply {
                    player = exoPlayer
                    setShowNextButton(false)
                    setShowPreviousButton(false)
                }
            }
        )

        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 16.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow),
                tint = Color.White,
                contentDescription = "Close Video Player"
            )
        }
    }
}


