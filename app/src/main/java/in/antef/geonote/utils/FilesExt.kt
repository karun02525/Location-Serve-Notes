package `in`.antef.geonote.utils

import android.R.id.input
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import java.io.InputStream
import java.io.OutputStream
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import `in`.antef.geonote.R
import `in`.antef.geonote.ui.components.TextRegular
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.exifinterface.media.ExifInterface
import androidx.core.graphics.scale
import org.json.JSONObject
import android.webkit.URLUtil


private fun ExifInterface.setGpsInfo(lat: Double, lon: Double) {
    this.setLatLong(lat, lon)
}

fun Context.fileOperation(uri: Uri, latitude: Double, longitude: Double): Pair<File, String> {
    val filename = uri.lastPathSegment ?: "unknown_file"
    var resultFile: File? = null

    val inputStream = this.contentResolver.openInputStream(uri)
    inputStream?.use { input ->
        val originalBitmap = BitmapFactory.decodeStream(input)?.copy(Bitmap.Config.ARGB_8888, true)
        if (originalBitmap != null) {
            // Resize bitmap
            val maxDim = 1024
            val scale = minOf(
                maxDim.toFloat() / originalBitmap.width,
                maxDim.toFloat() / originalBitmap.height,
                1f
            )
            val newWidth = (originalBitmap.width * scale).toInt()
            val newHeight = (originalBitmap.height * scale).toInt()
            val resizedBitmap = if (scale < 1f) {
                originalBitmap.scale(newWidth, newHeight)
            } else {
                originalBitmap
            }

            // Get orientation
            val exifInputStream = this.contentResolver.openInputStream(uri)
            val originalExif = exifInputStream?.use { ExifInterface(it) }
            val orientation = originalExif?.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            ) ?: ExifInterface.ORIENTATION_NORMAL

            val matrix = android.graphics.Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            }
            val rotatedBitmap = Bitmap.createBitmap(
                resizedBitmap, 0, 0,
                resizedBitmap.width, resizedBitmap.height, matrix, true
            )

            val dateTime =
                SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())
            val watermarkText = "$latitude : $longitude\n$dateTime"

            val canvas = Canvas(rotatedBitmap)
            val paint = Paint().apply {
                color = "#05387B".toColorInt()
                textSize = 30f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            }
            val lines = watermarkText.split("\n")
            var y = 50f
            for (line in lines) {
                canvas.drawText(line, 20f, y, paint)
                y += paint.textSize + 10f
            }

            val file = File(this.cacheDir, filename)
            file.outputStream().use { out ->
                rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            val newExif = ExifInterface(file)
            newExif.setLatLong(latitude, longitude)
            newExif.saveAttributes()
            resultFile = file
        }
    }
    return Pair(resultFile ?: File(this.cacheDir, filename), filename)
}

fun Context.operateVideo(uri: Uri): Pair<File, String> {
    val originalName = uri.lastPathSegment ?: "unknown_file"
    val filename = if (originalName.endsWith(".mp4", ignoreCase = true)) originalName else "$originalName.mp4"
    val outputFile = File(this.cacheDir, filename)

    if (!outputFile.exists() || outputFile.length() == 0L) {
        contentResolver.openInputStream(uri)?.use { input ->
            outputFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }
    return Pair(outputFile, filename)
}




/*fun Context.operateVideoWithGps(
    uri: Uri,
    latitude: Double,
    longitude: Double,
    onComplete: (Pair<File?, String>) -> Unit
) {
    val originalName = uri.lastPathSegment ?: "unknown_file"
    val filename = if (originalName.endsWith(".mp4", ignoreCase = true)) originalName else "$originalName.mp4"
    val inputFile = File(this.cacheDir, filename)

    // Copy video from uri to cacheDir if not exists
    if (!inputFile.exists()) {
        contentResolver.openInputStream(uri)?.use { input ->
            inputFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }

    val outputFile = File(this.cacheDir, "gps_$filename")
    val locationString = "GPS:$latitude,$longitude"
    val command = listOf(
        "-i", inputFile.absolutePath,
        "-metadata", "comment=$locationString",
        "-c", "copy",
        outputFile.absolutePath
    ).joinToString(" ")

  *//*  val inputPath = capturedFile.absolutePath
    val outputPath = File(downloadsDir, "compressed_video.mp4").absolutePath
    val compressCommand = "-i $inputPath -vcodec mpeg4 -b:v 1M $outputPath"

    FFmpegKit.executeAsync(compressCommand) { session ->
        Log.d("TAGS", "FFmpeg : Compression finished: ${session.state}")
        Log.d("TAGS", "FFmpeg : Compression finished outputPath: $outputPath")
    }*//*
    FFmpegKit.executeAsync(command) { session ->
        val returnCode = session.returnCode
        val success = returnCode.isValueSuccess
        Log.d("TAGS", "Geotagging completed: $returnCode")
        Log.d("TAGS", "Geotag added: ${session.state}")
        Log.d("TAGS", "Geotag added name location: $latitude , $longitude")
        Log.d("TAGS", "Geotag added success absolutePath: ${outputFile.absolutePath}")

        if (success) {
            inputFile.delete()
        }

        onComplete(
            if (success) Pair(outputFile, outputFile.name)
            else Pair(null, filename)
        )
    }
}*/


suspend fun Context.operateAudio(
    uri: Uri,
    onComplete: (Pair<File?, String>) -> Unit
) {
    val originalName = uri.lastPathSegment ?: "unknown_file"
    val filename = if (originalName.endsWith(".mp3", ignoreCase = true)) originalName else "$originalName.mp3"
    val outputFile = File(this.cacheDir, filename)

    withContext(Dispatchers.IO) {
        try {
            if (!outputFile.exists() || outputFile.length() == 0L) {
                contentResolver.openInputStream(uri)?.use { input ->
                    outputFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            }
            onComplete(Pair(outputFile, filename))
        } catch (e: Exception) {
            onComplete(Pair(null, filename))
        }
    }
}

/*fun Context.operateAudioWithGps(
    uri: Uri,
    latitude: Double,
    longitude: Double,
    onComplete: (Pair<File?, String>) -> Unit
) {
    val originalName = uri.lastPathSegment ?: "unknown_file"
    val filename = if (originalName.endsWith(".mp3", ignoreCase = true)) originalName else "$originalName.mp3"
    val inputFile = File(this.cacheDir, filename)
    Log.d("TAGS", "Geotagging inputFile path : ${inputFile.absolutePath}")

    // Copy video from uri to cacheDir if not exists
    if (!inputFile.exists()) {
        contentResolver.openInputStream(uri)?.use { input ->
            inputFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }

    val outputFile = File(this.cacheDir, "gps_$filename")
    val locationString = "GPS:$latitude,$longitude"
    val command = listOf(
        "-i", inputFile.absolutePath,
        "-metadata", "comment=$locationString",
        outputFile.absolutePath
    ).joinToString(" ")

    FFmpegKit.executeAsync(command) { session ->
        val returnCode = session.returnCode
        val success = returnCode.isValueSuccess
        Log.d("TAGS", "Geotagging Audio completed: $returnCode")

        if (success) {
            inputFile.delete()
        }
        onComplete(
            if (success) Pair(outputFile, outputFile.name)
            else Pair(null, filename)
        )
    }
}*/

// Format seconds to display as HH:MM:SS
fun formatTime(timeInSeconds: Long): String {
    val hours = timeInSeconds / 3600
    val minutes = (timeInSeconds % 3600) / 60
    val seconds = timeInSeconds % 60

    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}

fun String.isPhotoFile() =
    this.endsWith(".jpg", ignoreCase = true)

fun String.isAudioFile() =
    this.endsWith(".mp3", ignoreCase = true)

fun String.isVideoFile() =
    this.endsWith(".mp4", ignoreCase = true)


@Composable
fun VideoThumbnailAndDuration(
    videoUrl: String,
    modifier: Modifier = Modifier
) {
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var durationMs by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(videoUrl) {
        withContext(Dispatchers.IO) {
            try {
                val retriever = MediaMetadataRetriever()
                if (URLUtil.isNetworkUrl(videoUrl)) {
                    retriever.setDataSource(videoUrl, HashMap())
                } else {
                    retriever.setDataSource(videoUrl)
                }
                val frame = retriever.getFrameAtTime(0)
                val duration =
                    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                        ?.toLongOrNull()
                retriever.release()
                bitmap = frame
                durationMs = duration
            } catch (_: Exception) {
                bitmap = null
                durationMs = null
            }
        }
    }

    Box(
        modifier = modifier
    ) {
        bitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = "Video thumbnail",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Row(
            modifier = Modifier
                .padding(6.dp)
                .background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .align(Alignment.BottomStart)
                .padding(vertical = 4.dp, horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_payer_icon),
                contentDescription = "Play Video",
                tint = Color.Unspecified,
                modifier = Modifier.size(10.dp)
            )
            durationMs?.let {
                val seconds = it / 1000
                val formatted = String.format("%02d:%02d", seconds / 60, seconds % 60)
                TextRegular(
                    fontSize = 12.sp,
                    text = "$formatted",
                    modifier = Modifier.padding(start = 2.dp)
                )
            }
        }
    }
}