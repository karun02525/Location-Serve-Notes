package `in`.antef.database.mapping

// Domain models
data class Project(
    val projectId: Long,
    val title: String,
    val description: String,
    val createdAt: String,
    val coordinates: List<Coordinate> = emptyList()
)

data class Coordinate(
    val id: Long,
    val latitude: Double,
    val longitude: Double,
    val title: String,
    val description: String,
    val createdAt: String,
    val photos: List<Photo> = emptyList(),
    val audios: List<Audio> = emptyList()
)

data class Photo(
    val id: Long,
    val photoUrl: String
)

data class Audio(
    val id: Long,
    val audioUrl: String
)