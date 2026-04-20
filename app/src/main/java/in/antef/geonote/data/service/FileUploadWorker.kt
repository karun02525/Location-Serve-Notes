package `in`.antef.geonote.data.service
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import `in`.antef.geonote.R
import `in`.antef.geonote.domain.usecase.UploadCoordinateFileUseCase
import `in`.antef.geonote.ui.screens.MainActivity
import `in`.antef.geonote.util.CrashlyticsUtil
import `in`.antef.network.data.model.FileUploadResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File
import kotlin.text.compareTo

class FileUploadWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams), KoinComponent {

    companion object {
        const val PROGRESS_KEY = "progress"
        const val FILE_PATH_KEY = "file_path"
        const val FILE_ID_KEY = "file_id"
        const val FILE_NAME_KEY = "file_name"
        const val COORDINATE_ID_KEY = "coordinate_id"
        const val RESULT_URL_KEY = "result_url"
        const val ERROR_MESSAGE_KEY = "error_message"
        const val FILE_TYPE_KEY = "file_type"
        const val NOTIFICATION_ID = 1
        const val NOTIFICATION_CHANNEL_ID = "file_upload_channel"
        const val MAX_RETRY_COUNT = 3
    }

    private val uploadCoordinateFileUseCase: UploadCoordinateFileUseCase by inject()
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    override suspend fun doWork(): Result {
        val filePath = inputData.getString(FILE_PATH_KEY) ?: return Result.failure()
        val fileName = inputData.getString(FILE_NAME_KEY) ?: return Result.failure()
        val coordinateId = inputData.getLong(COORDINATE_ID_KEY, -1L)
        val fileId = inputData.getString(FILE_ID_KEY) ?: "file"
        val fileType = inputData.getString(FILE_TYPE_KEY) ?: "file"

        if (coordinateId == -1L) return Result.failure()

        val file = File(filePath)
        if (!file.exists()) {
            CrashlyticsUtil.recordException(
                Exception("File not found: $filePath"),
                "Upload failed - file not found",
                "FileUploadWorker"
            )
            return Result.failure(workDataOf(ERROR_MESSAGE_KEY to "File not found"))
        }

        setForeground(createForegroundInfo(0, fileType))

        var retryCount = 0
        var lastError: Exception? = null

        while (retryCount < MAX_RETRY_COUNT) {
            try {
                var resultUrl: String? = null
                var errorMessage: String? = null

                uploadCoordinateFileUseCase(fileId,coordinateId, file, fileName)
                    .onEach { result ->
                        when (result) {
                            is FileUploadResult.Loading -> {
                                setProgress(workDataOf(PROGRESS_KEY to 50))
                                updateNotification(50, fileType)
                            }

                            is FileUploadResult.Success -> {
                                setProgress(workDataOf(PROGRESS_KEY to 100))
                                updateNotification(100, fileType)
                                resultUrl = result.fileUrl
                            }

                            is FileUploadResult.Error -> {
                                errorMessage = result.exception.message
                                throw result.exception
                            }
                        }
                    }
                    .catch { e ->
                        throw e
                    }
                    .collect()

                if (resultUrl != null) {
                    // Show completion notification
                    showCompletionNotification(fileType)
                    return Result.success(
                        workDataOf(
                            RESULT_URL_KEY to resultUrl
                        )
                    )
                } else {
                    retryCount++
                    lastError = Exception(errorMessage ?: "Unknown error")
                }
            } catch (e: CancellationException) {
                CrashlyticsUtil.recordException(
                    e,
                    "Upload cancelled",
                    "FileUploadWorker"
                )
                return Result.failure(workDataOf(ERROR_MESSAGE_KEY to "Upload cancelled"))
            } catch (e: Exception) {
                Log.e("FileUploadWorker", "Upload attempt $retryCount failed", e)
                CrashlyticsUtil.recordException(
                    e,
                    "Upload attempt $retryCount failed",
                    "FileUploadWorker"
                )
                lastError = e
                retryCount++

                if (retryCount < MAX_RETRY_COUNT) {
                    // Update notification to show retry
                    updateNotificationForRetry(retryCount, fileType)
                    // Wait before retrying (exponential backoff)
                    kotlinx.coroutines.delay(2000L * retryCount)
                }
            }
        }

        // Show error notification after all retries failed
        showErrorNotification(fileType)

        return Result.failure(
            workDataOf(
                ERROR_MESSAGE_KEY to (lastError?.message
                    ?: "Unknown error after $MAX_RETRY_COUNT attempts")
            )
        )
    }

    private fun createForegroundInfo(progress: Int, fileType: String): ForegroundInfo {
        createNotificationChannel()

        val cancelIntent = WorkManager.getInstance(context)
            .createCancelPendingIntent(id)

        val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Uploading $fileType")
            .setTicker("Uploading $fileType")
            .setSmallIcon(R.drawable.ic_upload)
            .setOngoing(true)
            .setProgress(100, progress, progress == 0)
            .addAction(android.R.drawable.ic_delete, "Cancel", cancelIntent)
            .build()


        val serviceType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
        } else {
            0 // Fallback for lower API levels
        }

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // API 34
            ForegroundInfo(NOTIFICATION_ID, notification, serviceType)
        } else {
            ForegroundInfo(NOTIFICATION_ID, notification)
        }

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // API 34
            ForegroundInfo(NOTIFICATION_ID, notification, serviceType)
        } else {
            ForegroundInfo(NOTIFICATION_ID, notification)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "File Upload Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Used for displaying file upload progress"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun updateNotification(progress: Int, fileType: String) {
        val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Uploading $fileType")
            .setTicker("Uploading $fileType")
            .setSmallIcon(R.drawable.ic_upload)
            .setOngoing(true)
            .setProgress(100, progress, false)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun updateNotificationForRetry(retryCount: Int, fileType: String) {
        val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Retrying $fileType upload")
            .setContentText("Attempt $retryCount of $MAX_RETRY_COUNT")
            .setTicker("Retrying $fileType upload")
            .setSmallIcon(R.drawable.ic_upload)
            .setOngoing(true)
            .setProgress(100, 0, true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun showCompletionNotification(fileType: String) {
        val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("$fileType uploaded successfully")
            .setContentText("Your $fileType has been successfully uploaded")
            .setSmallIcon(R.drawable.ic_upload_success)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID + 1, notification)
        CoroutineScope(Dispatchers.Main).launch {
            delay(2000)
            notificationManager.cancel(NOTIFICATION_ID + 1)
        }
    }

    private fun showErrorNotification(fileType: String) {
        val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("$fileType upload failed")
            .setContentText("Failed to upload after multiple attempts. Please try again.")
            .setSmallIcon(R.drawable.ic_upload_error)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID + 2, notification)
        CoroutineScope(Dispatchers.Main).launch {
            delay(2000)
            notificationManager.cancel(NOTIFICATION_ID + 2)
        }
    }
}