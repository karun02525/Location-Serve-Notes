package `in`.antef.network.data.source

import android.content.Context
import android.util.Log
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.regions.Region
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.PutObjectRequest
import `in`.antef.network.data.model.FileUploadResult
import `in`.antef.network.utils.Constants.removeBaseUrl
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException
import kotlin.toString

class S3DataSourceImpl(
    private val context: Context,
    private val identityPoolId: String,
    private val bucketName: String,
    private val region: Regions = Regions.AP_SOUTH_1
) : S3DataSource {

    private val credentialsProvider by lazy {
        CognitoCachingCredentialsProvider(
            context,
            identityPoolId,
            region
        )
    }

    private val s3Client by lazy {
        AmazonS3Client(credentialsProvider, Region.getRegion(region))
    }

    override suspend fun uploadFile(id: String,file: File, key: String): Flow<FileUploadResult> = callbackFlow {
        trySend(FileUploadResult.Loading)
        try {
            withContext(Dispatchers.IO) {
                if (!isActive) return@withContext // Check if the coroutine is still active

                val metadata = ObjectMetadata()
                metadata.contentLength = file.length()
                metadata.contentType = "application/octet-stream"

                val request = PutObjectRequest(bucketName, key, file).withMetadata(metadata)
                s3Client.putObject(request)

                val fileUrl = s3Client.getUrl(bucketName, key).toString()
                trySend(FileUploadResult.Success(id,fileUrl))
            }
        } catch (e: CancellationException) {
            // Handle cancellation gracefully
            Log.w("S3DataSource", "Upload cancelled", e)
        } catch (e: Exception) {
            trySend(FileUploadResult.Error(e))
            Log.e("S3DataSource", "Error uploading file", e)
        } finally {
            close()
        }
        awaitClose()
    }

    override suspend fun getFileUrl(key: String): String = withContext(Dispatchers.IO) {
        return@withContext s3Client.getUrl(bucketName, key).toString()
    }

    override suspend fun deleteFile(filename: String): Boolean = withContext(Dispatchers.IO) {
        try {
            s3Client.deleteObject(bucketName, filename.removeBaseUrl())
            true
        } catch (e: Exception) {
            Log.e("S3DataSource", "Error deleting file", e)
            false
        }
    }

    override suspend fun getAllImages(): List<String> = withContext(Dispatchers.IO) {
        val objectListing = s3Client.listObjects(bucketName)
        return@withContext objectListing.objectSummaries.map { summary ->
            s3Client.getUrl(bucketName, summary.key).toString()
        }
    }
}