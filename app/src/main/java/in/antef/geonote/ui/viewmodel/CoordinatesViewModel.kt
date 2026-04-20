package `in`.antef.geonote.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import `in`.antef.database.repositories.GeoNoteRepository
import `in`.antef.geonote.domain.model.Coordinate
import `in`.antef.geonote.domain.model.MediaType
import `in`.antef.geonote.domain.model.ProjectModel
import `in`.antef.geonote.domain.model.UploadingMedia
import `in`.antef.geonote.domain.usecase.UploadCoordinateFileUseCase
import `in`.antef.geonote.util.CrashlyticsUtil
import `in`.antef.geonote.utils.isAudioFile
import `in`.antef.geonote.utils.isPhotoFile
import `in`.antef.geonote.utils.isVideoFile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CoordinatesUIState(
    val project: List<ProjectModel> = emptyList(),
    val coordinates: List<Coordinate> = emptyList(),
    val media: List<UploadingMedia> = emptyList(),
    val isLoading: Boolean = false,
    val coordinate: Coordinate = Coordinate(),
    val coordinateId: Long = 0,
    val title: String = "",
    val description: String = "",
    val shouldClearFocus: Boolean = false,
    val createdAt: String = ""
)


class CoordinatesViewModel(
    private val recordRepository: GeoNoteRepository,
    private val uploadCoordinateFileUseCase: UploadCoordinateFileUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CoordinatesUIState())
    val uiState: StateFlow<CoordinatesUIState> = _uiState
    private var currentCoordinateId: Long = 0

    fun getCoordinate(coordinateId: Long) {
        currentCoordinateId = coordinateId
        viewModelScope.launch {
            try {
                recordRepository.getCoordinateByIdFlow(coordinateId).collect { res ->
                    res?.let {
                        val coordinate = Coordinate(
                            id = it.id,
                            projectId = it.projectId,
                            latitude = it.latitude,
                            longitude = it.longitude,
                            title = it.title,
                            description = it.description,
                            createdAt = it.createdAt
                        )
                        _uiState.update { state ->
                            state.copy(
                                coordinate = coordinate,
                                title = coordinate.title,
                                description = coordinate.description,
                                createdAt = coordinate.createdAt
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                CrashlyticsUtil.recordException(
                    e,
                    "Failed to get coordinate data",
                    "CoordinatesViewModel"
                )
            }
        }
    }


    fun deleteCoordinate(coordinateId: Long) {
        viewModelScope.launch {
            try {
                recordRepository.deleteCoordinate(coordinateId)
            } catch (e: Exception) {
                CrashlyticsUtil.recordException(
                    e,
                    "Failed to delete coordinate",
                    "CoordinatesViewModel"
                )
            }
        }
    }

    fun updateCoordinate(coordinateId: Long, title: String, description: String) {
        viewModelScope.launch {
            try {
                val currentCoordinate = recordRepository.getCoordinateById(coordinateId)
                currentCoordinate?.let {
                    val updatedCoordinate = it.copy(
                        title = title,
                        description = description
                    )
                    recordRepository.updateCoordinate(updatedCoordinate)
                    val coordinate = Coordinate(
                        id = updatedCoordinate.id,
                        projectId = updatedCoordinate.projectId,
                        latitude = updatedCoordinate.latitude,
                        longitude = updatedCoordinate.longitude,
                        title = updatedCoordinate.title,
                        description = updatedCoordinate.description,
                    )
                    _uiState.update { it.copy(coordinate = coordinate) }
                }
            } catch (e: Exception) {
                CrashlyticsUtil.recordException(
                    e,
                    "Failed to update coordinate",
                    "CoordinatesViewModel"
                )
            }
        }
    }

    fun getAllMedia(coordinateId: Long) {
        viewModelScope.launch {
            try {
                recordRepository.getMediaByCoordinateId(coordinateId).collect { data ->
                    val media = data.map { item ->
                        val mediaType = when {
                            item.path.isPhotoFile() -> MediaType.PHOTO
                            item.path.isAudioFile() -> MediaType.AUDIO
                            item.path.isVideoFile() -> MediaType.VIDEO
                            else -> MediaType.UNKNOWN
                        }
                        UploadingMedia(
                            id = item.id,
                            path = item.path,
                            mediaType = mediaType,
                            createdAt = item.createdAt
                        )
                    }
                    _uiState.update { it.copy(media = media) }
                    println("Media Photos and Videos & Audios List: $media ")
                }
            } catch (e: Exception) {
                CrashlyticsUtil.recordException(
                    e,
                    "Failed to get photos and audios",
                    "CoordinatesViewModel"
                )
            }
        }
    }

    fun deletePhoto(media: UploadingMedia) {
        viewModelScope.launch {
            try {
                recordRepository.deletePhoto(media.id)
                uploadCoordinateFileUseCase.deleteFile(media.path)
            } catch (e: Exception) {
                CrashlyticsUtil.recordException(e, "Failed to delete photo", "CoordinatesViewModel")
            }
        }
    }
}