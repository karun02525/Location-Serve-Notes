package `in`.antef.network.domain.usecase


import `in`.antef.network.data.model.FileUploadResult
import `in`.antef.network.domain.repository.FileRepository
import java.io.File
import kotlinx.coroutines.flow.Flow

class UploadFileUseCase(private val fileRepository: FileRepository) {
    suspend operator fun invoke(id: String,file: File, filename: String): Flow<FileUploadResult> {
        return fileRepository.uploadFile(id,file, filename)
    }
    suspend fun getFileUrl(filename: String): String {
        return fileRepository.getFileUrl(filename)
    }

    suspend fun deleteFile(filename: String): Boolean {
        return fileRepository.deleteFile(filename)
    }

    suspend fun getAllImages(): List<String> = fileRepository.getAllImages()

}