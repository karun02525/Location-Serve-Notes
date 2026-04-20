package `in`.antef.network.data.repository

import `in`.antef.network.data.model.FileUploadResult
import `in`.antef.network.data.source.S3DataSource
import `in`.antef.network.domain.repository.FileRepository
import java.io.File
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class FileRepositoryImpl(
    private val s3DataSource: S3DataSource
) : FileRepository {

    override suspend fun uploadFile(id:String,file: File, filename: String): Flow<FileUploadResult> {
        val key = generateUniqueKey(filename)
        return s3DataSource.uploadFile(id,file, key)
    }

    override suspend fun getFileUrl(filename: String): String {
        println("getFileUrl called with filename: $filename")
        return s3DataSource.getFileUrl(filename)
    }

    override suspend fun deleteFile(filename: String): Boolean{
        println("deleteFile called with filename: $filename")
        return s3DataSource.deleteFile(filename)
    }


    override suspend fun deleteAllFiles() {
        TODO("Not yet implemented")
    }

    override suspend fun getAllImages(): List<String> {
        return s3DataSource.getAllImages()
    }

    private fun generateUniqueKey(filename: String): String {
        val uuid = UUID.randomUUID().toString()
        val extension = filename.substringAfterLast(".", "")
        return "$uuid.$extension"
    }
}