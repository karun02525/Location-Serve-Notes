package `in`.antef.database.repositories

import `in`.antef.database.dao.CoordinateDao
import `in`.antef.database.dao.MediaDao
import `in`.antef.database.dao.ProjectDao
import `in`.antef.database.entity.CoordinateEntity
import `in`.antef.database.entity.CoordinateWithMedias
import `in`.antef.database.entity.MediaEntity
import `in`.antef.database.entity.ProjectEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class GeoNoteRepository(
    private val projectDao: ProjectDao,
    private val coordinateDao: CoordinateDao,
    private val mediaDao: MediaDao,
) {
    // Project operations
    suspend fun insertProject(project: ProjectEntity): Long = withContext(Dispatchers.IO) {
        return@withContext projectDao.insertProject(project)
    }

    suspend fun updateProject(project: ProjectEntity) = withContext(Dispatchers.IO) {
        projectDao.updateProject(project)
    }

    suspend fun deleteProjectById(projectId: Long) = withContext(Dispatchers.IO) {
        projectDao.deleteProjectById(projectId)
    }

    fun getAllProjects(): Flow<List<ProjectEntity>> {
        return projectDao.getAllProjects()
    }

    suspend fun getCoordinateCountForProject(projectId: Long): Int = withContext(Dispatchers.IO) {
        return@withContext projectDao.getCoordinateCountForProject(projectId)
    }

    suspend fun getProjectById(id: Long): ProjectEntity? = withContext(Dispatchers.IO) {
        return@withContext projectDao.getProjectById(id)
    }

    suspend fun updateCoordinate(coordinate: CoordinateEntity) = withContext(Dispatchers.IO) {
        coordinateDao.updateCoordinate(coordinate)
    }

    suspend fun deleteCoordinate(id: Long) = withContext(Dispatchers.IO) {
        coordinateDao.deleteCoordinate(id)
    }

    suspend fun getCoordinateById(id: Long): CoordinateEntity? = withContext(Dispatchers.IO) {
        return@withContext coordinateDao.getCoordinateById(id)
    }

    fun getCoordinateByIdFlow(id: Long): Flow<CoordinateEntity?> {
        return coordinateDao.getCoordinateByIdFlow(id)
    }

    fun getCoordinatesWithMediasByProjectId(projectId: Long): Flow<List<CoordinateWithMedias>> {
        return coordinateDao.getCoordinatesWithMediasByProjectId(projectId)
    }

    // Media operations
    suspend fun insertMediaData(media: MediaEntity): Long = withContext(Dispatchers.IO) {
        return@withContext mediaDao.insertMediaData(media)
    }

    suspend fun updateMediaData(media: MediaEntity): Int = withContext(Dispatchers.IO) {
        return@withContext mediaDao.updateMediaData(media)
    }

    suspend fun deletePhoto(id: String) = withContext(Dispatchers.IO) {
        mediaDao.deleteMediaById(id)
    }

    fun getMediaByCoordinateId(coordinateId: Long): Flow<List<MediaEntity>> {
        return mediaDao.getPhotosByCoordinateId(coordinateId)
    }

    suspend fun insertCoordinate(coordinate: CoordinateEntity): Long = withContext(Dispatchers.IO) {
        return@withContext coordinateDao.insertCoordinate(coordinate)
    }

}
