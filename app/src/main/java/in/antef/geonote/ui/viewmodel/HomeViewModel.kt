package `in`.antef.geonote.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import `in`.antef.database.dao.MediaDao
import `in`.antef.database.entity.ProjectEntity
import `in`.antef.database.repositories.GeoNoteRepository
import `in`.antef.geonote.data.worker.SyncData
import `in`.antef.geonote.domain.model.Coordinate
import `in`.antef.geonote.domain.model.MediaType
import `in`.antef.geonote.domain.model.ProjectModel
import `in`.antef.geonote.domain.model.UploadStatus
import `in`.antef.geonote.domain.model.UploadingMedia
import `in`.antef.geonote.share.ShareProjectUtils
import `in`.antef.geonote.util.CrashlyticsUtil
import `in`.antef.geonote.utils.isAudioFile
import `in`.antef.geonote.utils.isPhotoFile
import `in`.antef.geonote.utils.isVideoFile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UIState(
    val projectList: List<ProjectModel> = emptyList(),
    val shareProject: ProjectModel = ProjectModel(),
    val projectId: Long = 0,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val isPendingFileUpload: Boolean = false
)


class HomeViewModel(
    private val recordRepository: GeoNoteRepository,
    private val mediaDao: MediaDao,
    private val syncData: SyncData
) : ViewModel() {

    private val _uiState = MutableStateFlow(UIState())
    val uiState: StateFlow<UIState> = _uiState


    fun triggerManualSync() {
        viewModelScope.launch {
            syncData.doWorkSync()
        }
    }

    fun addProject(title: String, description: String) {
        val project = ProjectEntity(
            title = title,
            description = description
        )
        viewModelScope.launch {
            try {
                val newId = recordRepository.insertProject(project)
                _uiState.update {
                    it.copy(
                        isSuccess = true, projectId = newId
                    )
                }
                println("Added new project with ID: $newId")
            } catch (e: Exception) {
                CrashlyticsUtil.recordException(e, "Failed to add project", "HomeViewModel")
                _uiState.update { it.copy(isSuccess = false) }
            }
        }
    }

    fun updateProject(projectId: Long, title: String, description: String) {
        val project = ProjectEntity(
            id = projectId,
            title = title,
            description = description
        )
        viewModelScope.launch {
            try {
                recordRepository.updateProject(project)
            } catch (e: Exception) {
                CrashlyticsUtil.recordException(e, "Failed to update project", "HomeViewModel")
            }
        }
    }


    fun deleteProject(projectId: Long) {
        viewModelScope.launch {
            try {
                recordRepository.deleteProjectById(projectId)
            } catch (e: Exception) {
                CrashlyticsUtil.recordException(e, "Failed to delete project", "HomeViewModel")
            }
        }
    }

    fun getProjects() {
        viewModelScope.launch {
            try {
                recordRepository.getAllProjects().collect { data ->
                    val projectList = mutableListOf<ProjectModel>()
                    data.forEach { project ->
                        val coordinateCount =
                            recordRepository.getCoordinateCountForProject(project.id)
                        projectList.add(
                            ProjectModel(
                                projectId = project.id,
                                title = project.title,
                                description = project.description,
                                createdAt = project.createdAt,
                                coordinateCount = coordinateCount
                            )
                        )
                    }
                    _uiState.update { it.copy(projectList = projectList) }
                }
            } catch (e: Exception) {
                CrashlyticsUtil.recordException(e, "Failed to get projects", "HomeViewModel")
            }
        }
    }

    fun shareProject(projectId: Long, context: Context) {
        viewModelScope.launch {
            try {
                val project = recordRepository.getProjectById(projectId)
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

                                val status =
                                    if (item.status == true) UploadStatus.SUCCESS else UploadStatus.PENDING

                                UploadingMedia(
                                    id = item.id,
                                    path = item.path,
                                    status = status,
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
                        val mediaStatus = coordinates.flatMap { it.media.map { it.status } }
                        val pendingMediaStatus = mediaStatus.filter { it == UploadStatus.PENDING }
                        println("mediaStatus List of coordinates with media: $mediaStatus")
                        if (pendingMediaStatus.isEmpty()) {
                            val shareProject = ProjectModel(
                                projectId = project?.id ?: 0,
                                title = project?.title ?: "No project",
                                description = project?.description ?: "No description",
                                createdAt = project?.createdAt ?: System.currentTimeMillis()
                                    .toString(),
                                coordinates = coordinates
                            )
                            ShareProjectUtils.shareProjectAsKml(
                                context = context,
                                project = shareProject,
                                authorities = "in.antef.geonote.fileprovider"
                            )
                            _uiState.update { it.copy(isPendingFileUpload = false) }
                            println("shareProject List of coordinates with media: $shareProject")
                        } else {
                            _uiState.update {
                                it.copy(
                                    isPendingFileUpload = true,
                                    isSuccess = false
                                )
                            }
                            println("pendingMediaStatus List of coordinates with media: $pendingMediaStatus")
                        }
                    }
            } catch (e: Exception) {
                CrashlyticsUtil.recordException(e, "Failed to share project", "HomeViewModel")
            }
        }
    }

    fun resetStatus() {
        _uiState.update { it.copy(isSuccess = false,isPendingFileUpload = false) }
    }
}
