package `in`.antef.geonote.data.service

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File
import java.util.UUID

class FileUploadManager(private val context: Context) {
    private val workManager = WorkManager.getInstance(context)

    enum class FileType(val displayName: String) {
        PHOTO("photo"),
        AUDIO("audio"),
        VIDEO("video")
    }

    fun enqueueUpload(
        fileId: String,
        coordinateId: Long,
        file: File,
        fileName: String,
        fileType: FileType
    ): UUID {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val inputData = Data.Builder()
            .putString(FileUploadWorker.FILE_PATH_KEY, file.absolutePath)
            .putString(FileUploadWorker.FILE_ID_KEY, fileId)
            .putString(FileUploadWorker.FILE_NAME_KEY, fileName)
            .putLong(FileUploadWorker.COORDINATE_ID_KEY, coordinateId)
            .putString(FileUploadWorker.FILE_TYPE_KEY, fileType.displayName)
            .build()

        val uploadWorkRequest = OneTimeWorkRequestBuilder<FileUploadWorker>()
            .setConstraints(constraints)
            .setInputData(inputData)
            .addTag("file_upload")
            .addTag("coordinate_$coordinateId")
            .addTag(fileType.displayName)
            .build()

        // Use unique work name to prevent duplicate uploads of the same file
        val uniqueWorkName = "file_upload_${file.name}_${System.currentTimeMillis()}"
        workManager.enqueueUniqueWork(
            uniqueWorkName,
            ExistingWorkPolicy.KEEP,
            uploadWorkRequest
        )

        return uploadWorkRequest.id
    }

    fun getUploadStatus(uploadId: UUID): Flow<UploadStatus> {
        return workManager.getWorkInfoByIdFlow(uploadId)
            .map { workInfo ->
                when (workInfo.state) {
                    WorkInfo.State.ENQUEUED -> UploadStatus.Queued
                    WorkInfo.State.RUNNING -> {
                        val progress = workInfo.progress.getInt(FileUploadWorker.PROGRESS_KEY, 0)
                        UploadStatus.InProgress(progress)
                    }

                    WorkInfo.State.SUCCEEDED -> {
                        val fileUrl = workInfo.outputData.getString(FileUploadWorker.RESULT_URL_KEY)
                        UploadStatus.Success(fileUrl ?: "")
                    }

                    WorkInfo.State.FAILED -> {
                        val errorMessage =
                            workInfo.outputData.getString(FileUploadWorker.ERROR_MESSAGE_KEY)
                                ?: "Upload failed"
                        UploadStatus.Failed(errorMessage)
                    }

                    WorkInfo.State.BLOCKED -> UploadStatus.Queued
                    WorkInfo.State.CANCELLED -> UploadStatus.Cancelled
                }
            }
    }

    fun cancelUpload(uploadId: UUID) {
        workManager.cancelWorkById(uploadId)
    }

    fun cancelAllUploads() {
        workManager.cancelAllWorkByTag("file_upload")
    }

    fun cancelUploadsForCoordinate(coordinateId: Long) {
        workManager.cancelAllWorkByTag("coordinate_$coordinateId")
    }

    sealed class UploadStatus {
        data object Queued : UploadStatus()
        data class InProgress(val progress: Int) : UploadStatus()
        data class Success(val fileUrl: String) : UploadStatus()
        data class Failed(val error: String) : UploadStatus()
        data object Cancelled : UploadStatus()
    }
}