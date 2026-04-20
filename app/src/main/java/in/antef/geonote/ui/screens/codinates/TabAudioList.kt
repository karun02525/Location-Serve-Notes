package `in`.antef.geonote.ui.screens.codinates

import android.media.MediaPlayer
import android.util.Log
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.antef.geonote.R
import `in`.antef.geonote.domain.model.MediaType
import `in`.antef.geonote.domain.model.UploadingMedia
import `in`.antef.geonote.domain.model.getFormattedTime
import `in`.antef.geonote.ui.components.TextMedium
import `in`.antef.geonote.ui.components.TextRegular
import `in`.antef.geonote.ui.theme.CARD_BORDER_COLOR
import `in`.antef.geonote.ui.theme.SEARCH_PLACEHOLDER_COLOR
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

@Composable
fun TabAudioList(
    media: List<UploadingMedia>,
    onDeleteMedia: (UploadingMedia) -> Unit
) {
    if (media.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            TextMedium(
                text = "No recordings added yet",
                color = Color.Gray,
            )
        }
        return
    }

    val currentlyPlayingAudio = remember { mutableStateOf<String?>(null) }
    val mediaPlayer = remember { MediaPlayer() }
    val isPlaying = remember { mutableStateOf(false) }
    val audioDurations = remember { mutableStateMapOf<String, String>() }
    val playbackProgress = remember { mutableFloatStateOf(0f) }

    DisposableEffect(Unit) {
        onDispose {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
            }
            mediaPlayer.release()
        }
    }

    val groupedAudios = media.groupBy {
        getFormattedTime(it.createdAt)
    }
    LazyColumn(
        contentPadding = PaddingValues(4.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        groupedAudios.forEach { (date, audiosInGroup) ->
            item {
                TextMedium(
                    text = date,
                    color = SEARCH_PLACEHOLDER_COLOR,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, top = 10.dp)
                )
            }
            items(audiosInGroup) { item ->
                val audioUrl = item.path
                val isThisPlaying = currentlyPlayingAudio.value == audioUrl && isPlaying.value
                LaunchedEffect(audioUrl) {
                    if (!audioDurations.containsKey(audioUrl)) {
                        try {
                            val tempMediaPlayer = MediaPlayer()
                            tempMediaPlayer.setDataSource(audioUrl)
                            tempMediaPlayer.prepare()
                            val durationMillis = tempMediaPlayer.duration
                            val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis.toLong())
                            val remainingSeconds =
                                TimeUnit.MILLISECONDS.toSeconds(durationMillis.toLong()) -
                                        TimeUnit.MINUTES.toSeconds(minutes)
                            val formattedTime = String.format("%d:%02d", minutes, remainingSeconds)
                            audioDurations[audioUrl] = formattedTime
                            tempMediaPlayer.release()
                        } catch (e: Exception) {
                            Log.e("TabAudioList", "Error getting duration: ${e.message}")
                            audioDurations[audioUrl] = "0:00"
                        }
                    }
                }

                // Update progress for currently playing audio
                if (isThisPlaying) {
                    LaunchedEffect(Unit) {
                        while (isPlaying.value && currentlyPlayingAudio.value == audioUrl) {
                            if (mediaPlayer.duration > 0) {
                                playbackProgress.floatValue =
                                    mediaPlayer.currentPosition.toFloat() / mediaPlayer.duration
                            }
                            delay(100)
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 20.dp)
                            .align(Alignment.BottomCenter)
                            .height(60.dp)
                            .border(1.dp, CARD_BORDER_COLOR, RoundedCornerShape(8.dp))
                            .background(
                                if (isThisPlaying) Color(0xFFFFF8E1) else Color.White,
                                RoundedCornerShape(8.dp)
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Play/Pause Icon
                        IconButton(
                            onClick = {
                                if (currentlyPlayingAudio.value == audioUrl && isPlaying.value) {
                                    // Pause current playing audio
                                    mediaPlayer.pause()
                                    isPlaying.value = false
                                } else {
                                    // Play new audio or resume paused audio
                                    try {
                                        if (currentlyPlayingAudio.value != audioUrl) {
                                            mediaPlayer.reset()
                                            mediaPlayer.setDataSource(audioUrl)
                                            mediaPlayer.prepare()
                                            currentlyPlayingAudio.value = audioUrl
                                            mediaPlayer.setOnCompletionListener {
                                                isPlaying.value = false
                                                playbackProgress.floatValue = 0f
                                            }
                                        }
                                        mediaPlayer.start()
                                        isPlaying.value = true
                                    } catch (e: Exception) {
                                        Log.e("TabAudioList", "Error playing audio: ${e.message}")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                painter = painterResource(
                                    id = if (isThisPlaying) R.drawable.ic_pause else R.drawable.ic_play_audio
                                ),
                                contentDescription = if (isThisPlaying) "Pause" else "Play",
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Waveform Visual
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                                .height(40.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            WaveformVisualization(
                                isPlaying = isThisPlaying,
                                progress = if (isThisPlaying) playbackProgress.floatValue else 0f
                            )
                        }

                        // Duration text
                        TextRegular(
                            text = audioDurations[audioUrl] ?: "0:00",
                            color = SEARCH_PLACEHOLDER_COLOR,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(end = 8.dp),
                        )
                    }

                    IconButton(
                        modifier = Modifier
                            .padding(end = 10.dp)
                            .size(24.dp)
                            .align(Alignment.TopEnd),
                        onClick = { onDeleteMedia(item) }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_cross),
                            contentDescription = "Delete",
                            tint = Color.Unspecified,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WaveformVisualization(
    isPlaying: Boolean,
    progress: Float
) {
    val barCount = 30
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    val animatedValue by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing)
        ),
        label = "wave"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until barCount) {
            val normalizedPos = i.toFloat() / barCount
            val isActive = normalizedPos <= progress

            // Height pattern based on position
            val height = when {
                isPlaying -> {
                    val wavePosition = (normalizedPos * 6 - animatedValue * 2).mod(1.0f)
                    (10 + (15 * sin(wavePosition * PI * 2))).dp
                }

                else -> {
                    val baseHeight = 5 + abs(i - barCount / 2)
                    (baseHeight).dp
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(height)
                    .padding(horizontal = 1.dp)
                    .background(
                        color = if (isActive) Color(0xFFFFA726) else Color.LightGray,
                        shape = RoundedCornerShape(4.dp)
                    )
            )
        }
    }
}