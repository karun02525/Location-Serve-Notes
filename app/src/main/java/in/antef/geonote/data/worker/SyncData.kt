package `in`.antef.geonote.data.worker

import `in`.antef.database.dao.MediaDao
import `in`.antef.database.entity.MediaEntity
import `in`.antef.geonote.data.service.FileUploadManager
import `in`.antef.geonote.domain.model.MediaType
import `in`.antef.geonote.domain.usecase.UploadCoordinateFileUseCase
import `in`.antef.geonote.utils.isAudioFile
import `in`.antef.geonote.utils.isPhotoFile
import `in`.antef.geonote.utils.isVideoFile
import `in`.antef.network.data.model.FileUploadResult
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import java.io.File

class SyncData(
    private val mediaDao: MediaDao,
    private val uploadCoordinateFileUseCase: UploadCoordinateFileUseCase
) {
    suspend fun doWorkSync() {
        val pendingMedia: List<MediaEntity> = mediaDao.getPendingMedia()
        for (media in pendingMedia) {
            if (media.status == false) {
                val coordinateId = media.coordinateId
                val fileId = media.id
                val filePath = media.path
                uploadCoordinateFileUseCase(fileId, coordinateId, File(filePath), filePath)
                    .onEach { result ->
                        when (result) {
                            is FileUploadResult.Loading -> {

                            }

                            is FileUploadResult.Success -> {
                                // resultUrl = result.fileUrl
                            }

                            is FileUploadResult.Error -> {
                                throw result.exception
                            }
                        }
                    }
                    .catch { e ->
                        throw e
                    }
                    .collect()
            }
        }
    }
}