package `in`.antef.geonote.ui.viewmodel

sealed class FileUploadUiState {
    data object Initial : FileUploadUiState()
    data object Loading : FileUploadUiState()
    data class Success(val uuid: String,val fileUrl: String) : FileUploadUiState()
    data class Error(val message: String) : FileUploadUiState()
}