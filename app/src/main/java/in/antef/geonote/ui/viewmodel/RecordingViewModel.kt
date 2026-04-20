package `in`.antef.geonote.ui.viewmodel

import CheckNetworkConnectivity
import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import `in`.antef.geonote.data.service.FileUploadManager
import `in`.antef.geonote.domain.usecase.InsertMediaUseCase
import `in`.antef.geonote.ui.screens.recording.RecordingState
import `in`.antef.geonote.util.CrashlyticsUtil
import `in`.antef.geonote.utils.operateAudio
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class RecordingViewModel(
    private val fileUploadManager: FileUploadManager,
    private val insertPhotoUseCase: InsertMediaUseCase,
    private val checkNetworkConnectivity: CheckNetworkConnectivity
) : ViewModel() {

    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var audioFilePath: String? = null
    private var uploadWorkId: UUID? = null

    private val _recordingState = MutableStateFlow(RecordingState.IDLE)
    val recordingState: StateFlow<RecordingState> = _recordingState

    private val _elapsedTime = MutableStateFlow(0L)
    val elapsedTime: StateFlow<Long> = _elapsedTime

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _uiState = MutableStateFlow<FileUploadUiState>(FileUploadUiState.Initial)
    val uiState: StateFlow<FileUploadUiState> = _uiState

    private var uuid = ""

    init {
        startTimer()
    }

    private fun startTimer() {
        viewModelScope.launch {
            while (isActive) {
                delay(1000)
                if (_recordingState.value == RecordingState.RECORDING ||
                    (_isPlaying.value && _recordingState.value == RecordingState.PAUSED) ||
                    (_isPlaying.value && _recordingState.value == RecordingState.STOPPED)
                ) {
                    _elapsedTime.value = _elapsedTime.value + 1
                }
            }
        }
    }

    fun startRecording(context: Context) {
        if (_recordingState.value == RecordingState.RECORDING) return

        // Setup media recorder
        val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }

        try {
            audioFilePath = createAudioFile(context)
            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(audioFilePath)
                prepare()
                start()
            }
            mediaRecorder = recorder
            _recordingState.value = RecordingState.RECORDING
            if (_recordingState.value == RecordingState.IDLE) {
                _elapsedTime.value = 0 // Only reset timer if coming from IDLE
            }
        } catch (e: IOException) {
            e.printStackTrace()
            CrashlyticsUtil.recordException(e, "Failed to start recording", "RecordingViewModel")
            releaseRecorder()
        }
    }

    fun pauseRecording() {
        if (_recordingState.value != RecordingState.RECORDING) return

        // API level 24 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mediaRecorder?.pause()
            _recordingState.value = RecordingState.PAUSED
        }
    }

    fun resumeRecording() {
        if (_recordingState.value != RecordingState.PAUSED) return

        // Stop any playback first
        stopPlayback()

        // API level 24 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mediaRecorder?.resume()
            _recordingState.value = RecordingState.RECORDING
        }
    }

    fun stopRecording() {
        if (_recordingState.value != RecordingState.RECORDING && _recordingState.value != RecordingState.PAUSED) return

        // Stop any playback first
        stopPlayback()

        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            CrashlyticsUtil.recordException(e, "Failed to stop recording", "RecordingViewModel")
        } finally {
            mediaRecorder = null
            _recordingState.value = RecordingState.STOPPED
        }
    }

    fun playAudio() {
        if (audioFilePath == null) return
        _elapsedTime.value = 0
        try {
            if (_isPlaying.value) {
                // If already playing, just pause it
                pauseAudio()
                return
            }

            if (mediaPlayer == null) {
                // Create a new media player if none exists
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(audioFilePath)
                    prepare()

                    setOnCompletionListener {
                        _isPlaying.value = false
                        // Don't release here, so we can replay
                    }
                }
            }

            // Start playback
            mediaPlayer?.start()
            _isPlaying.value = true

        } catch (e: IOException) {
            e.printStackTrace()
            CrashlyticsUtil.recordException(e, "Failed to play audio", "RecordingViewModel")
            _isPlaying.value = false
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    fun pauseAudio() {
        if (!_isPlaying.value) return

        try {
            mediaPlayer?.pause()
            _isPlaying.value = false
        } catch (e: Exception) {
            e.printStackTrace()
            CrashlyticsUtil.recordException(e, "Failed to pause audio", "RecordingViewModel")
            // In case of error, reset the player
            mediaPlayer?.release()
            mediaPlayer = null
            _isPlaying.value = false
        }
    }

    fun cancelRecording() {
        stopPlayback()
        releaseRecorder()
        audioFilePath?.let {
            try {
                File(it).delete()
            } catch (e: Exception) {
                e.printStackTrace()
                CrashlyticsUtil.recordException(
                    e,
                    "Failed to delete audio file",
                    "RecordingViewModel"
                )
            }
        }
        audioFilePath = null
        _recordingState.value = RecordingState.IDLE
        _elapsedTime.value = 0
    }

    fun saveRecording(context: Context, coordinateId: Long, latitude: Double, longitude: Double) {
        stopPlayback()
        _recordingState.value = RecordingState.IDLE
        _elapsedTime.value = 0

        val filePath = audioFilePath ?: return
        val uri = Uri.fromFile(File(filePath))
        uuid = UUID.randomUUID().toString()

        viewModelScope.launch {
            context.operateAudio(uri) { (outputFile, outputName) ->
                val file = outputFile ?: return@operateAudio
                viewModelScope.launch {
                    insertPhotoUseCase.localSaveMedia(uuid, coordinateId, file.absolutePath)
                }
                if (checkNetworkConnectivity.isNetworkAvailable()) {
                    uploadInBackground(uuid, coordinateId, file, outputName)
                }
            }
        }

        /*        context.operateAudioWithGps(uri, latitude, longitude) { (outputFile, outputName) ->
                    val file = outputFile ?: return@operateAudioWithGps
                    viewModelScope.launch {
                        insertPhotoUseCase.localSaveMedia(uuid, coordinateId, file.absolutePath)
                    }
                    if (checkNetworkConnectivity.isNetworkAvailable()) {
                        uploadInBackground(uuid, coordinateId, file, outputName)
                    }
                }*/
    }

    private fun uploadInBackground(uuid: String, coordinateId: Long, file: File, fileName: String) {
        uploadWorkId = fileUploadManager.enqueueUpload(
            uuid,
            coordinateId,
            file,
            fileName,
            FileUploadManager.FileType.AUDIO
        )
        _uiState.value = FileUploadUiState.Loading

        // Monitor the upload progress
        val workId = uploadWorkId
        if (workId != null) {
            fileUploadManager.getUploadStatus(workId)
                .onEach { status: FileUploadManager.UploadStatus ->
                    when (status) {
                        is FileUploadManager.UploadStatus.Queued -> {
                            _uiState.value = FileUploadUiState.Loading
                        }

                        is FileUploadManager.UploadStatus.InProgress -> {
                            _uiState.value = FileUploadUiState.Loading
                        }

                        is FileUploadManager.UploadStatus.Success -> {
                            _uiState.value = FileUploadUiState.Success(uuid, status.fileUrl)
                        }

                        is FileUploadManager.UploadStatus.Failed -> {
                            _uiState.value = FileUploadUiState.Error(status.error)
                        }

                        is FileUploadManager.UploadStatus.Cancelled -> {
                            _uiState.value = FileUploadUiState.Error("Upload cancelled")
                        }
                    }
                }
                .catch { e: Throwable ->
                    _uiState.value = FileUploadUiState.Error(e.message ?: "Unknown error")
                    CrashlyticsUtil.recordException(
                        e,
                        "Error monitoring upload",
                        "RecordingViewModel"
                    )
                }
                .launchIn(viewModelScope)
        }
    }

    private fun stopPlayback() {
        try {
            mediaPlayer?.apply {
                if (isPlaying) stop()
                reset()
                release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            mediaPlayer = null
            _isPlaying.value = false
        }
    }

    private fun releaseRecorder() {
        mediaRecorder?.apply {
            try {
                stop()
            } catch (e: Exception) {
                // Ignore errors during stop
            } finally {
                release()
            }
        }
        mediaRecorder = null
    }

    fun resetState() {
        _uiState.value = FileUploadUiState.Initial
    }

    private fun createAudioFile(context: Context): String {
        val timestamp = SimpleDateFormat("MMddHHmmss", Locale.getDefault()).format(Date())
        val fileName = "audio_$timestamp.mp3"
        val file = File(context.cacheDir, fileName)
        return file.absolutePath
    }

    override fun onCleared() {
        super.onCleared()
        releaseRecorder()
        stopPlayback()

        // Cancel any ongoing upload when the ViewModel is cleared
        val workId = uploadWorkId
        if (workId != null) {
            fileUploadManager.cancelUpload(workId)
        }
    }
}