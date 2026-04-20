package `in`.antef.network.domain.repository

import `in`.antef.network.data.model.FileUploadResult
import java.io.File
import kotlinx.coroutines.flow.Flow

interface FileRepository {
    suspend fun uploadFile(id:String,file: File, filename: String): Flow<FileUploadResult>
    suspend fun getFileUrl(filename: String): String
    suspend fun deleteFile(filename: String):Boolean
    suspend fun deleteAllFiles()
    suspend fun getAllImages(): List<String>
}