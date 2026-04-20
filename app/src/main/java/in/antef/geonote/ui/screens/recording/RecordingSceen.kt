package `in`.antef.geonote.ui.screens.recording

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import `in`.antef.geonote.R
import `in`.antef.geonote.ui.components.AudioTopBar
import `in`.antef.geonote.ui.components.TextMedium
import `in`.antef.geonote.ui.components.TextRegular
import `in`.antef.geonote.ui.screens.camera.ErrorDialog
import `in`.antef.geonote.ui.theme.BG_COLOR
import `in`.antef.geonote.ui.theme.BG_WHITE_PHOTO_COLOR
import `in`.antef.geonote.ui.theme.BORDER_COLOR
import `in`.antef.geonote.ui.viewmodel.FileUploadUiState
import `in`.antef.geonote.ui.viewmodel.RecordingViewModel
import `in`.antef.geonote.utils.ProgressDialog
import `in`.antef.geonote.utils.formatTime
import org.koin.androidx.compose.koinViewModel

enum class RecordingState {
    IDLE, RECORDING, PAUSED, STOPPED
}


@Composable
fun RecordingScreen(
    coordinateId: Long = 0L,
    latitude: Double,
    longitude: Double,
    viewModel: RecordingViewModel = koinViewModel(),
    onBack: () -> Unit = {},
) {
    val context = LocalContext.current
    val recordingState by viewModel.recordingState.collectAsState()
    val recordingTime by viewModel.elapsedTime.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(key1 = uiState) {
        if (uiState is FileUploadUiState.Success) {
            viewModel.resetState()
         //   onBack()
        }
    }


    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Request audio permission
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasAudioPermission = isGranted
        if (isGranted) {
            viewModel.startRecording(context)
        }
    }

    // Visual components for different states
    val statusCircleColor = Color.Red

    val statusText = when (recordingState) {
        RecordingState.IDLE -> "Start Recording"
        RecordingState.RECORDING -> "Recording"
        RecordingState.PAUSED -> "Paused"
        RecordingState.STOPPED -> "Recorded"
    }

    Scaffold(
        topBar = {
            AudioTopBar(onBack = onBack)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .background(BG_COLOR)
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
                .background(Color.White, shape = RoundedCornerShape(16.dp))
                .border(1.dp, BORDER_COLOR, shape = RoundedCornerShape(16.dp))
            ,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Status indicator with red circle and text
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_recording),
                    contentDescription = "Recording Icon",
                    tint = Color.Unspecified
                )
                Spacer(modifier = Modifier.width(8.dp))
                TextRegular(text = statusText)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Timer display
            TextMedium(
                text = formatTime(recordingTime),
                fontSize = 32.sp,
            )

            Spacer(modifier = Modifier.weight(1f))

            // Audio wave visualization - only visible when not in IDLE state
            if (recordingState != RecordingState.IDLE) {
                AudioWaveform(
                    isActive = recordingState == RecordingState.RECORDING || isPlaying,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .padding(horizontal = 24.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Control buttons
            when (recordingState) {
                RecordingState.IDLE -> {
                    // Start recording button
                    Image(
                        painter = painterResource(id = R.drawable.ic_recoding),
                        contentDescription = "Record",
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                if (hasAudioPermission) {
                                    viewModel.startRecording(context)
                                    //back
                                } else {
                                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            }

                    )
                }

                RecordingState.RECORDING -> {
                    // Row with controls for recording state
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(bottom = 32.dp)
                    ) {
                        // Play button (disabled during recording)
                        /*       IconButton(
                                   onClick = { *//* Disabled during recording *//* },
                            modifier = Modifier
                                .size(56.dp)
                                .background(Color.LightGray, RoundedCornerShape(8.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play",
                                tint = Color.White
                            )
                        }*/

                        // Pause button
                        IconButton(
                            onClick = { viewModel.pauseRecording() },
                            modifier = Modifier
                                .size(56.dp)
                                .background(BG_WHITE_PHOTO_COLOR, RoundedCornerShape(8.dp))
                        ) {
                            Icon(
                                painterResource(R.drawable.ic_pause),
                                contentDescription = "Pause",
                                tint = Color.Red
                            )
                        }

                        // Stop button
                        IconButton(
                            onClick = { viewModel.stopRecording() },
                            modifier = Modifier
                                .size(56.dp)
                                .background(BG_WHITE_PHOTO_COLOR, RoundedCornerShape(8.dp))
                        ) {
                            Icon(
                                painterResource(R.drawable.ic_stop),
                                contentDescription = "Stop",
                                tint = Color.Black
                            )
                        }
                    }
                }

                RecordingState.PAUSED -> {
                    // Row with controls for paused state
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(bottom = 32.dp)
                    ) {
                        // Play/Pause button
                        /*            IconButton(
                                        onClick = {
                                            if (isPlaying) {
                                                viewModel.pauseAudio()
                                            } else {
                                                viewModel.playAudio()
                                            }
                                        },
                                        modifier = Modifier
                                            .size(56.dp)
                                            .background(BG_WHITE_PHOTO_COLOR, RoundedCornerShape(8.dp))
                                    ) {
                                        Icon(
                                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                            contentDescription = if (isPlaying) "Pause" else "Play",
                                            tint = Color.Black
                                        )
                                    }*/

                        // Record button (resume recording)
                        IconButton(
                            onClick = { viewModel.resumeRecording() },
                            modifier = Modifier
                                .size(56.dp)
                                .background(BG_WHITE_PHOTO_COLOR, RoundedCornerShape(8.dp))
                        ) {
                            Box(modifier = Modifier.size(28.dp))
                            Icon(
                                painterResource(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play_audio),
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = if (isPlaying) Color.Red else Color.Black
                            )
                        }

                        // Stop button
                        IconButton(
                            onClick = { viewModel.stopRecording() },
                            modifier = Modifier
                                .size(56.dp)
                                .background(BG_WHITE_PHOTO_COLOR, RoundedCornerShape(8.dp))
                        ) {
                            Icon(
                                painterResource(R.drawable.ic_stop),
                                contentDescription = "Stop",
                                tint = Color.Black
                            )
                        }
                    }
                }

                RecordingState.STOPPED -> {
                    // Row with controls for stopped state
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(bottom = 32.dp)
                    ) {
                        // Cancel button
                        IconButton(
                            onClick = { viewModel.cancelRecording() },
                            modifier = Modifier
                                .size(56.dp)
                                .background(Color(0xFFFFCDCD), RoundedCornerShape(8.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancel",
                                tint = Color.Red
                            )
                        }

                        // Play button
                        IconButton(
                            onClick = {
                                if (isPlaying) {
                                    viewModel.pauseAudio()
                                } else {
                                    viewModel.playAudio()
                                }
                            },
                            modifier = Modifier
                                .size(56.dp)
                                .background(BG_WHITE_PHOTO_COLOR, RoundedCornerShape(8.dp))
                        ) {
                            Icon(
                                painterResource(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play_audio),
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = if (isPlaying) Color.Red else Color.Black
                            )
                        }

                        // Save button
                        IconButton(
                            onClick = {
                                onBack()
                                viewModel.saveRecording(context, coordinateId, latitude, longitude,)


                            },
                            modifier = Modifier
                                .size(56.dp)
                                .background(Color(0xFFCDFFD8), RoundedCornerShape(8.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Save",
                                tint = Color.Green
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        if (uiState is FileUploadUiState.Error) {
            ErrorDialog(
                message = (uiState as FileUploadUiState.Error).message,
                onDismiss = { viewModel.resetState() }
            )
        }
        ProgressDialog(uiState is FileUploadUiState.Loading)
    }
}