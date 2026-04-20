package `in`.antef.network.data.source

import `in`.antef.network.data.model.FileUploadResult
import java.io.File
import kotlinx.coroutines.flow.Flow

interface S3DataSource {
    suspend fun uploadFile(id:String,file: File, key: String): Flow<FileUploadResult>
    suspend fun getFileUrl(key: String): String
    suspend fun deleteFile(key: String): Boolean
    suspend fun getAllImages(): List<String>
}