package `in`.antef.geonote.domain.usecase

import `in`.antef.network.data.model.FileUploadResult
import `in`.antef.network.domain.usecase.UploadFileUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import java.io.File

class UploadCoordinateFileUseCase(
    private val uploadFileUseCase: UploadFileUseCase,
    private val insertMediaUseCase: InsertMediaUseCase
) {
    suspend operator fun invoke(
        id: String,
        coordinateId: Long,
        file: File,
        filename: String
    ): Flow<FileUploadResult> {
        return uploadFileUseCase(id,file, filename)
            .onEach { result ->
                if (result is FileUploadResult.Success) {
                    insertMediaUseCase.updateMedia(result.fileId,coordinateId, result.fileUrl, true)
                }
            }
    }

    suspend fun deleteFile(filename: String): Boolean{
        return uploadFileUseCase.deleteFile(filename)
    }
}