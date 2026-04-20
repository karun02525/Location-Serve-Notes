package `in`.antef.geonote.domain.usecase

import `in`.antef.database.entity.MediaEntity
import `in`.antef.database.repositories.GeoNoteRepository

class InsertMediaUseCase(private val repository: GeoNoteRepository) {
    suspend fun localSaveMedia(id: String, coordinateId: Long, path: String) {
        val media = MediaEntity(
            id = id,
            coordinateId = coordinateId,
            path = path,
            status = false
        )
        //Save database
        repository.insertMediaData(media)
    }

    suspend fun updateMedia(id: String,coordinateId: Long,path: String,status: Boolean) {
        //Update Media
        val media = MediaEntity(
            id = id,
            coordinateId = coordinateId,
            path = path,
            status = status
        )
        repository.updateMediaData(media)
    }

}