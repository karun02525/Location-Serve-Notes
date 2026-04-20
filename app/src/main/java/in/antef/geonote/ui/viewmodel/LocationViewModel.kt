package `in`.antef.geonote.ui.viewmodel

import android.content.Context
import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.maps.model.LatLng
import `in`.antef.database.entity.CoordinateEntity
import `in`.antef.database.repositories.GeoNoteRepository
import `in`.antef.geonote.data.service.LocationService
import `in`.antef.geonote.domain.model.Coordinate
import `in`.antef.geonote.domain.model.MediaType
import `in`.antef.geonote.domain.model.UploadingMedia
import `in`.antef.geonote.util.CrashlyticsUtil
import `in`.antef.geonote.utils.LocationUtils
import `in`.antef.geonote.utils.isAudioFile
import `in`.antef.geonote.utils.isPhotoFile
import `in`.antef.geonote.utils.isVideoFile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LocationUiState(
    val isLoading: Boolean = false,
    val location: Location? = null,
    val error: String? = null,
    val isGpsEnabled: Boolean = false,
    val gpsResolution: ResolvableApiException? = null,
    val isPermissionsGranted: Boolean = false,
    val showSettingsDialog: Boolean = false,
    val circleRadius: Double = 500.0,
    val sourceLocation: LatLng? = null,
    val destinationLocation: LatLng? = null,
    var sourceSearchText: String = "",
    var destinationSearchText: String = "",
    val distance: Double? = null,
    val projectName: String = "",
    val coordinates: List<Coordinate> = emptyList(),
    val projectId: Long = 0,
    val coordinateId: Long = 0,
    val coordinateCountId: Long = 0,
    val selectedCoordinate: Coordinate? = null,
)

class LocationViewModel(
    private val locationService: LocationService,
    private val recordRepository: GeoNoteRepository
) : ViewModel() {


    private val _locationState = MutableStateFlow(LocationUiState())
    val locationState = _locationState.asStateFlow()

    fun checkLocationPermissionsAndStartUpdates(context: Context) {
        if (LocationUtils.hasLocationPermissions(context)) {
            enableGpsAndGetLocation()
        } else {
            _locationState.update { it.copy(isPermissionsGranted = false) }
        }
    }

    fun onPermissionResult(granted: Boolean) {
        if (granted) {
            _locationState.update { it.copy(isPermissionsGranted = true) }
            enableGpsAndGetLocation()
        } else {
            _locationState.update {
                it.copy(
                    isPermissionsGranted = false,
                    showSettingsDialog = true
                )
            }
        }
    }

    fun dismissSettingsDialog() {
        _locationState.update { it.copy(showSettingsDialog = false) }
    }

    fun clearGpsResolution() {
        _locationState.update { it.copy(gpsResolution = null) }
    }



    private fun enableGpsAndGetLocation() {
        viewModelScope.launch {
            locationService.enableGps()
                .onSuccess {
                    startLocationUpdates()
                }
                .onFailure { exception ->
                    when (exception) {
                        is ResolvableApiException -> {
                            _locationState.update { it.copy(gpsResolution = exception) }
                        }

                        else -> {
                            _locationState.update {
                                it.copy(error = exception.message ?: "Unknown error occurred")
                            }
                        }
                    }
                }
        }
    }

    fun startLocationUpdates() {
        viewModelScope.launch {
            _locationState.update { it.copy(isLoading = true, error = null) }
            try {
                locationService.getLocationUpdates()
                    .collect { location ->
                        println("Location: $location")
                        _locationState.update {
                            it.copy(
                                location = location,
                                isLoading = false,
                                error = null
                            )
                        }
                    }
            } catch (e: Exception) {
                CrashlyticsUtil.recordException(e, "Location updates failed", "LocationViewModel")
                _locationState.update {
                    it.copy(
                        error = e.message ?: "Unknown error occurred",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun getProject(projectId: Long) {
        viewModelScope.launch {
            try {
                val project = recordRepository.getProjectById(projectId)
                _locationState.update { it.copy(projectName = project?.title ?: "") }
            } catch (e: Exception) {
                CrashlyticsUtil.recordException(e, "Failed to get project", "LocationViewModel")
            }
        }
    }

    fun getAllCoordinates(projectId: Long) {
        viewModelScope.launch {
            try {
                val project = recordRepository.getProjectById(projectId)
                val projectName = project?.title ?: ""
                recordRepository.getCoordinatesWithMediasByProjectId(projectId)
                    .collect { coordinatesWithMedias ->
                        val coordinates = coordinatesWithMedias.map { coordinateWithMedias ->
                            val coordinate = coordinateWithMedias.coordinate
                            val media = coordinateWithMedias.paths.map { item ->
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
                            Coordinate(
                                id = coordinate.id,
                                title = coordinate.title,
                                description = coordinate.description,
                                latitude = coordinate.latitude,
                                longitude = coordinate.longitude,
                                createdAt = coordinate.createdAt,
                                media = media,
                            )
                        }
                        println("List of coordinates with media: $coordinates")
                        _locationState.update {
                            it.copy(
                                coordinates = coordinates,
                                projectName = projectName
                            )
                        }
                    }
            } catch (e: Exception) {
                CrashlyticsUtil.recordException(e, "Failed to get coordinates", "LocationViewModel")
            }
        }
    }

  /*  fun getAllCoordinates() {
        viewModelScope.launch {
            try {
                val allCoordinates = mutableListOf<Coordinate>()

                recordRepository.getAllProjects().collect { projects ->
                    projects.forEach { project ->
                        recordRepository.getCoordinatesWithMediasByProjectId(project.id)
                            .collect { coordinatesWithMedias ->
                                val projectCoordinates =
                                    coordinatesWithMedias.map { coordinateWithMedias ->
                                        val coordinate = coordinateWithMedias.coordinate
                                        val photos = coordinateWithMedias.photos.map { photo ->
                                            Photo(
                                                id = photo.id,
                                                photoUrl = photo.path
                                            )
                                        }
                                        val audios = coordinateWithMedias.photos.map { audio ->
                                            Audio(
                                                id = audio.id,
                                                audioUrl = audio.path
                                            )
                                        }
                                        Coordinate(
                                            id = coordinate.id,
                                            title = coordinate.title,
                                            description = coordinate.description,
                                            latitude = coordinate.latitude,
                                            longitude = coordinate.longitude,
                                            createdAt = coordinate.createdAt,
                                            photos = photos,
                                            audios = audios
                                        )
                                    }
                                allCoordinates.addAll(projectCoordinates)

                                _locationState.update { it.copy(coordinates = allCoordinates.toList()) }
                            }
                    }
                }
            } catch (e: Exception) {
                CrashlyticsUtil.recordException(
                    e,
                    "Failed to get all coordinates",
                    "LocationViewModel"
                )
            }
        }
    }*/

    fun addCoordinate(
        title: String,
        description: String,
        projectId: Long,
        location: Location?,
    ) {
        val project = CoordinateEntity(
            projectId = projectId,
            latitude = location?.latitude ?: 0.0,
            longitude = location?.longitude ?: 0.0,
            title = title,
            description = description,
        )
        viewModelScope.launch {
            try {
                val newId = recordRepository.insertCoordinate(project)
                _locationState.update { it.copy(coordinateId = newId,coordinateCountId = newId) }
                println("Added new addCoordinate with ID: $newId")
            } catch (e: Exception) {
                CrashlyticsUtil.recordException(e, "Failed to add coordinate", "LocationViewModel")
            }
        }
    }

    fun updateProject(projectId: Long) {
        _locationState.update { it.copy(projectId = projectId) }
    }

    fun resetCoordinateId(){
        _locationState.update { it.copy(coordinateId = 0) }
    }

    fun onMarkerSelected(coordinate: Coordinate) {
        _locationState.update { it.copy(selectedCoordinate = coordinate) }
    }
}
