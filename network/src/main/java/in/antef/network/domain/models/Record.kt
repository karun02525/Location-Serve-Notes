package `in`.antef.network.domain.models

data class Record(
    val id: Long,
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val imagePaths: List<String>,
    val audioPaths: List<String>,
    val createdAt: Long
)

