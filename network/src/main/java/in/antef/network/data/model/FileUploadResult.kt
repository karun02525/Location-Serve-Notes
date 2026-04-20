package `in`.antef.network.data.model

sealed class FileUploadResult {
    data class Success(val fileId: String,val fileUrl: String) : FileUploadResult()
    data class Error(val exception: Exception) : FileUploadResult()
    data object Loading : FileUploadResult()
}