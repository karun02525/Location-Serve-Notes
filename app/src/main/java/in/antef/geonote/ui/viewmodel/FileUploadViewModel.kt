package `in`.antef.geonote.ui.viewmodel

import CheckNetworkConnectivity
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import `in`.antef.geonote.data.service.FileUploadManager
import `in`.antef.geonote.domain.usecase.InsertMediaUseCase
import `in`.antef.geonote.domain.usecase.UploadCoordinateFileUseCase
import `in`.antef.geonote.util.CrashlyticsUtil
import `in`.antef.geonote.utils.isAudioFile
import `in`.antef.geonote.utils.isPhotoFile
import `in`.antef.geonote.utils.isVideoFile
import `in`.antef.network.data.model.FileUploadResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

class FileUploadViewModel(
    private val uploadCoordinateFileUseCase: UploadCoordinateFileUseCase,
    private val fileUploadManager: FileUploadManager,
    private val insertPhotoUseCase: InsertMediaUseCase,
    private val checkNetworkConnectivity: CheckNetworkConnectivity
) : ViewModel() {

    private val _uiState = MutableStateFlow<FileUploadUiState>(FileUploadUiState.Initial)
    val uiState: StateFlow<FileUploadUiState> = _uiState

    private var uploadWorkId: UUID? = null
    private var uuid=""


    //upload online
    fun uploadFile(coordinateId: Long, file: File, filename: String) {
        uuid = UUID.randomUUID().toString()

        //Save Local
        viewModelScope.launch {
            insertPhotoUseCase.localSaveMedia(
                id = uuid,
                coordinateId = coordinateId,
                path = file.absolutePath
            )
        }


        if (checkNetworkConnectivity.isNetworkAvailable()) {
            viewModelScope.launch {
                _uiState.value = FileUploadUiState.Loading

                // Determine file type based on filename extension
                val fileType = when {
                    filename.isVideoFile() -> FileUploadManager.FileType.VIDEO
                    filename.isPhotoFile() -> FileUploadManager.FileType.PHOTO
                    filename.isAudioFile() -> FileUploadManager.FileType.AUDIO
                    else -> FileUploadManager.FileType.PHOTO // Default to photo
                }

                // Use background upload service
                uploadWorkId = fileUploadManager.enqueueUpload(
                    uuid,
                    coordinateId,
                    file,
                    filename,
                    fileType
                )

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
                                    _uiState.value = FileUploadUiState.Success(uuid,status.fileUrl)
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
                            Log.e("FileUploadViewModel", "Upload error", e)
                            CrashlyticsUtil.recordException(
                                e,
                                "Upload process failed",
                                "FileUploadViewModel"
                            )
                        }
                        .launchIn(viewModelScope)
                } else {
                    // Fall back to direct upload if WorkManager fails
                    directUpload(uuid,coordinateId, file, filename)
                }
            }
        }
    }




    private fun directUpload(uuid: String,coordinateId: Long, file: File, filename: String) {
        viewModelScope.launch {
            uploadCoordinateFileUseCase(uuid,coordinateId, file, filename)
                .onEach { result ->
                    when (result) {
                        is FileUploadResult.Loading -> {
                            _uiState.value = FileUploadUiState.Loading
                        }

                        is FileUploadResult.Success -> {
                            _uiState.value = FileUploadUiState.Success(uuid,result.fileUrl)
                        }

                        is FileUploadResult.Error -> {
                            _uiState.value =
                                FileUploadUiState.Error(result.exception.message ?: "Unknown error")
                            Log.e("FileUploadViewModel", "Upload error", result.exception)
                            CrashlyticsUtil.recordException(
                                result.exception,
                                "Upload failed",
                                "FileUploadViewModel"
                            )
                        }
                    }
                }
                .catch { e ->
                    _uiState.value = FileUploadUiState.Error(e.message ?: "Unknown error")
                    Log.e("FileUploadViewModel", "Upload error", e)
                    CrashlyticsUtil.recordException(
                        e,
                        "Upload process failed",
                        "FileUploadViewModel"
                    )
                }
                .launchIn(viewModelScope)
        }
    }

    fun resetState() {
        _uiState.value = FileUploadUiState.Initial

        // Cancel any ongoing upload if there is one
        val workId = uploadWorkId
        if (workId != null) {
            fileUploadManager.cancelUpload(workId)
            uploadWorkId = null
        }
    }

    override fun onCleared() {
        super.onCleared()

        // Cancel any ongoing upload when the ViewModel is cleared
        val workId = uploadWorkId
        if (workId != null) {
            fileUploadManager.cancelUpload(workId)
        }
    }
}
