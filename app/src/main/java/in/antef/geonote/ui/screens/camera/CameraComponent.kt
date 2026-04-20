package `in`.antef.geonote.ui.screens.camera

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FallbackStrategy
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.util.Consumer
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.rememberAsyncImagePainter
import `in`.antef.geonote.R
import `in`.antef.geonote.ui.components.CustomFloatingButton
import `in`.antef.geonote.ui.components.RecordingBox
import `in`.antef.geonote.ui.components.TextMedium
import `in`.antef.geonote.ui.screens.videoplayer.VideoPlayerPreview
import `in`.antef.geonote.ui.theme.BG_GREEN_CROSS_COLOR
import `in`.antef.geonote.ui.theme.BG_RED_CROSS_COLOR
import `in`.antef.geonote.ui.theme.BG_WHITE_PHOTO_COLOR
import `in`.antef.geonote.ui.theme.TERTIARY
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.Executor
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

enum class CameraMode {
    PHOTO, VIDEO
}

enum class CameraState {
    CAMERA, PHOTO_PREVIEW, VIDEO_PREVIEW
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFFF0EAE2)
@Composable
fun CameraComponent(
    onPhotoCaptured: (Uri) -> Unit = {},
    onVideoCaptured: (Uri) -> Unit = {}, // Add parameter for video capture callback
    onDismiss: () -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    val executor = remember { ContextCompat.getMainExecutor(context) }
    val imageCapture = remember { ImageCapture.Builder().build() }

    // Video capture setup
    var recording by remember { mutableStateOf<Recording?>(null) }
    var videoCapture by remember { mutableStateOf<VideoCapture<Recorder>?>(null) }
    var isRecording by remember { mutableStateOf(false) }

    // State management
    var cameraMode by remember { mutableStateOf(CameraMode.PHOTO) }
    var cameraState by remember { mutableStateOf(CameraState.CAMERA) }
    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }
    var capturedVideoUri by remember { mutableStateOf<Uri?>(null) }

    // Add a PreviewView that we can reference
    val previewView = remember { PreviewView(context) }
    var recordingTime by remember { mutableStateOf(0) }


    LaunchedEffect(cameraMode) {
        val cameraProvider = context.getCameraProvider()
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        try {
            cameraProvider.unbindAll()

            if (cameraMode == CameraMode.PHOTO) {
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } else {
                // Setup video capture
                val recorder = Recorder.Builder()
                    .setQualitySelector(
                        QualitySelector.from(
                            Quality.HIGHEST,
                            FallbackStrategy.higherQualityOrLowerThan(Quality.SD)
                        )
                    )
                    .build()

                val newVideoCapture = VideoCapture.withOutput(recorder)
                videoCapture = newVideoCapture

                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    newVideoCapture
                )
            }
        } catch (e: Exception) {
            Log.e("CameraComponent", "Use case binding failed", e)
        }
    }

    LaunchedEffect(isRecording) {
        if (isRecording) {
            recordingTime = 0
            while (isRecording) {
                delay(1000)
                recordingTime += 1
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (cameraState) {
            CameraState.PHOTO_PREVIEW -> {
                // Show image preview with retake and done options
                ImagePreviewScreen(
                    imageUri = capturedImageUri!!,
                    onRetake = {
                        capturedImageUri = null
                        cameraState = CameraState.CAMERA
                    },
                    onDone = { uri ->
                        onPhotoCaptured(uri)
                        capturedImageUri = null
                        cameraState = CameraState.CAMERA
                    },
                    onDismiss = onDismiss
                )
            }

            CameraState.VIDEO_PREVIEW -> {
                // Show video preview with retake and done options
                VideoPreviewScreen(
                    videoUri = capturedVideoUri!!,
                    onRetake = {
                        capturedVideoUri = null
                        cameraState = CameraState.CAMERA
                    },
                    onDone = { uri ->
                        onVideoCaptured(uri)
                        capturedVideoUri = null
                        cameraState = CameraState.CAMERA
                    },
                    onDismiss = onDismiss
                )
            }

            CameraState.CAMERA -> {
                // Camera capture UI
                AndroidView(
                    factory = { ctx ->
                        // Use the remembered previewView
                        previewView.apply {
                            this.implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                            scaleType = PreviewView.ScaleType.FILL_CENTER
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Camera controls
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top =16.dp)
                ) {
                    // Close button
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow),
                            tint = Color.White,
                            contentDescription = "Close Camera"
                        )
                    }

                    // Camera mode selector
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(top = 16.dp)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        CustomFloatingButton(
                            icon = R.drawable.ic_camera,
                            contentColor = Color.Black,
                            containerColor = if (cameraMode == CameraMode.PHOTO) TERTIARY else BG_WHITE_PHOTO_COLOR,
                            onClick = {
                                if (!isRecording) {
                                    cameraMode = CameraMode.PHOTO
                                }
                            }
                        )


                        // Capture button
                        IconButton(
                            onClick = {
                                if (cameraMode == CameraMode.PHOTO) {
                                    // Capture photo
                                    capturePhotoAndUpdate(
                                        imageCapture = imageCapture,
                                        executor = executor,
                                        context = context,
                                        onImageCaptured = { uri ->
                                            capturedImageUri = uri
                                            cameraState = CameraState.PHOTO_PREVIEW
                                        }
                                    )
                                } else {
                                    // Toggle video recording
                                    if (isRecording) {
                                        // Stop recording
                                        recording?.stop()
                                        isRecording = false
                                    } else {
                                        // Start recording
                                        coroutineScope.launch {
                                            startRecording(
                                                videoCapture = videoCapture,
                                                context = context,
                                                executor = executor,
                                                onVideoRecorded = { uri ->
                                                    capturedVideoUri = uri
                                                    cameraState = CameraState.VIDEO_PREVIEW
                                                },
                                                onRecordingStarted = {
                                                    isRecording = true
                                                    recording = it
                                                }
                                            )
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .size(72.dp)
                        ) {
                            if (cameraMode == CameraMode.PHOTO) {
                                // Photo capture button
                                Card(
                                    modifier = Modifier
                                        .size(56.dp),
                                    shape = CircleShape,
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.White
                                    )
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.White)
                                    )
                                }
                            } else {
                                // Video record button that changes appearance when recording
                                Card(
                                    modifier = Modifier
                                        .size(56.dp),
                                    shape = CircleShape,
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isRecording) Color.Red else Color.White
                                    )
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(all = if (isRecording) 16.dp else 0.dp)
                                            .background(if (isRecording) Color.White else Color.Transparent)
                                    )
                                }

                            }
                        }

                        CustomFloatingButton(
                            icon = R.drawable.ic_video,
                            contentColor = if (cameraMode == CameraMode.VIDEO) Color.Red else Color.Black,
                            containerColor = if (cameraMode == CameraMode.VIDEO) TERTIARY else BG_WHITE_PHOTO_COLOR,
                            onClick = {
                                if (!isRecording) {
                                    cameraMode = CameraMode.VIDEO
                                }
                            }
                        )
                    }

                    if (isRecording) {
                        RecordingBox(
                            recordingTime = formatTime(recordingTime),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 110.dp)
                        )
                    }

                }
            }
        }
    }
}

fun formatTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%02d:%02d".format(m, s)
}

@Composable
private fun VideoPreviewScreen(
    videoUri: Uri,
    onRetake: () -> Unit,
    onDone: (Uri) -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {

        IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopStart)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow),
                    tint = Color.White,
                    contentDescription = "Close Video Preview"
                )
            }

            TextMedium(
                "Video Preview",
                color = Color.White,
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(top = 16.dp)
                    .align(Alignment.TopCenter)
            )

        VideoPlayerPreview(
            url = videoUri,
            modifier = Modifier
                .fillMaxSize()
        )

        // Bottom action buttons Video preview
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CustomFloatingButton(
                icon = R.drawable.ic_close,
                contentColor = Color.Unspecified,
                containerColor = BG_RED_CROSS_COLOR,
                onClick = onRetake
            )
            Spacer(Modifier.width(16.dp))
            CustomFloatingButton(
                icon = R.drawable.ic_sign_tick,
                contentColor = Color.Unspecified,
                containerColor = BG_GREEN_CROSS_COLOR,
                onClick = { onDone(videoUri) }
            )
        }
    }
}

@Composable
private fun ImagePreviewScreen(
    imageUri: Uri,
    onRetake: () -> Unit,
    onDone: (Uri) -> Unit,
    onDismiss: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Image preview
        Image(
            painter = rememberAsyncImagePainter(imageUri),
            contentDescription = "Captured image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Close button
        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 16.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow),
                tint = Color.White,
                contentDescription = "Close Camera"
            )
        }

        // Bottom action buttons camera
        Row(
            modifier = Modifier
                .padding(bottom = 32.dp)
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {

            CustomFloatingButton(
                icon = R.drawable.ic_close,
                contentColor = Color.Unspecified,
                containerColor = BG_RED_CROSS_COLOR,
                onClick = onRetake
            )
            Spacer(Modifier.width(16.dp))
            CustomFloatingButton(
                icon = R.drawable.ic_sign_tick,
                contentColor = Color.Unspecified,
                containerColor = BG_GREEN_CROSS_COLOR,
                onClick = { onDone(imageUri) }
            )
        }
    }
}

private suspend fun Context.getCameraProvider(): ProcessCameraProvider {
    return suspendCoroutine { continuation ->
        ProcessCameraProvider.getInstance(this).also { future ->
            future.addListener(
                {
                    continuation.resume(future.get())
                },
                ContextCompat.getMainExecutor(this)
            )
        }
    }
}

// Renamed function to clarify its role
private fun capturePhotoAndUpdate(
    imageCapture: ImageCapture,
    executor: Executor,
    context: Context,
    onImageCaptured: (Uri) -> Unit
) {
    val photoFile = File(
        context.externalCacheDir,
        "IMG_${
            SimpleDateFormat(
                "yyyyMMdd_HHmmss",
                Locale.ENGLISH
            ).format(System.currentTimeMillis())
        }.jpg"
    )

    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    imageCapture.takePicture(
        outputOptions,
        executor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                output.savedUri?.let { uri ->
                    onImageCaptured(uri)
                }
            }

            override fun onError(exception: ImageCaptureException) {
                Log.e("Camera", "Error taking photo", exception)
            }
        }
    )
}

private suspend fun startRecording(
    videoCapture: VideoCapture<Recorder>?,
    context: Context,
    executor: Executor,
    onVideoRecorded: (Uri) -> Unit,
    onRecordingStarted: (Recording) -> Unit
) {
    videoCapture?.let { capture ->
        val name = "GeoNote-${
            SimpleDateFormat(
                "yyyyMMdd_HHmmss",
                Locale.ENGLISH
            ).format(System.currentTimeMillis())
        }.mp4"

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/GeoNote")
            }
        }

        val mediaStoreOutputOptions = MediaStoreOutputOptions
            .Builder(context.contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            .setContentValues(contentValues)
            .build()

        val recording = capture.output
            .prepareRecording(context, mediaStoreOutputOptions)
            .start(executor, object : Consumer<VideoRecordEvent> {
                override fun accept(event: VideoRecordEvent) {
                    when (event) {
                        is VideoRecordEvent.Finalize -> {
                            if (event.hasError()) {
                                Log.e("CameraComponent", "Video capture failed: ${event.error}")
                            } else {
                                event.outputResults.outputUri.let { uri ->
                                    onVideoRecorded(uri)
                                }
                            }
                        }
                    }
                }
            })

        onRecordingStarted(recording)
    }
}
