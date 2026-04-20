package `in`.antef.geonote.domain.model

import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@Serializable
data class ProjectModel(
    val projectId: Long = 0L,
    val title: String = "",
    val description: String = "",
    val createdAt: String = "",
    val coordinateCount: Int = 0,
    val coordinates: List<Coordinate> = emptyList(),
)

@Serializable
data class Coordinate(
    val id: Long = 0L,
    val projectId: Long = 0L,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val title: String = "",
    val description: String = "",
    val createdAt: String = "",
    val media: List<UploadingMedia> = emptyList(),
)

@Serializable
data class UploadingMedia(
    val id: String ="",
    val path: String = "",
    val progress: Float = 0f,      // 0.0 to 1.0
    val status: UploadStatus = UploadStatus.UPLOADING,
    val mediaType: MediaType = MediaType.UNKNOWN,
    val createdAt: Long = 0L,
)

enum class TimeCategory {
    MINUTES_AGO,
    HOURS_AGO,
    TODAY,
    YESTERDAY,
    OLDER
}


private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

private fun formatTime(timestamp: Long): String {
    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    return formatter.format(Date(timestamp))
}

private fun formatFullDateTime(timestamp: Long): String {
    val formatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    return formatter.format(Date(timestamp))
}

fun getTimeCategory(createdAt: Long): TimeCategory {
    val now = System.currentTimeMillis()
    val diffInMillis = now - createdAt

    val minutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis)
    val hours = TimeUnit.MILLISECONDS.toHours(diffInMillis)

    val today = Calendar.getInstance()
    val createdDate = Calendar.getInstance().apply { timeInMillis = createdAt }

    val yesterday = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, -1)
    }

    return when {
        minutes < 60 -> TimeCategory.MINUTES_AGO
        hours < 24 -> TimeCategory.HOURS_AGO
        isSameDay(today, createdDate) -> TimeCategory.TODAY
        isSameDay(yesterday, createdDate) -> TimeCategory.YESTERDAY
        else -> TimeCategory.OLDER
    }
}

fun getFormattedTime(createdAt: Long): String {
    val now = System.currentTimeMillis()
    val diffInMillis = now - createdAt

    val minutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis)
    val hours = TimeUnit.MILLISECONDS.toHours(diffInMillis)

    val today = Calendar.getInstance()
    val createdDate = Calendar.getInstance().apply { timeInMillis = createdAt }

    val yesterday = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, -1)
    }

    return when {
        minutes < 60 -> "$minutes minutes ago"
        hours < 24 -> "$hours hours ago"
        isSameDay(today, createdDate) -> "Today at ${formatTime(createdAt)}"
        isSameDay(yesterday, createdDate) -> "Yesterday at ${formatTime(createdAt)}"
        else -> formatFullDateTime(createdAt)
    }
}

enum class UploadStatus { UPLOADING,PENDING, SUCCESS, FAILED }
enum class MediaType { PHOTO, VIDEO, AUDIO,UNKNOWN }
