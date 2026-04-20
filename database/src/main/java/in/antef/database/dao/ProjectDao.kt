package `in`.antef.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Delete
import `in`.antef.database.entity.CoordinateEntity
import `in`.antef.database.entity.ProjectEntity
import `in`.antef.database.entity.ProjectWithCoordinates
import `in`.antef.database.entity.ProjectWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(entity: ProjectEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCoordinate(entity: CoordinateEntity): Long

    @Query("SELECT * FROM project ORDER BY createdAt DESC")
    fun getAllProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT COUNT(*) FROM coordinate WHERE projectId = :projectId")
    suspend fun getCoordinateCountForProject(projectId: Long): Int

    @Query("SELECT * FROM project WHERE id = :id")
    suspend fun getProjectById(id: Long): ProjectEntity?
    
    @Query("SELECT * FROM project WHERE id = :id")
    fun getProjectByIdFlow(id: Long): Flow<ProjectEntity?>

    @Update
    suspend fun updateProject(entity: ProjectEntity)

    @Delete
    suspend fun deleteProject(entity: ProjectEntity)

    @Query("DELETE FROM project WHERE id = :id")
    suspend fun deleteProjectById(id: Long)

    @Transaction
    @Query("SELECT * FROM project WHERE id = :projectId")
    suspend fun getProjectWithCoordinates(projectId: Long): ProjectWithCoordinates?

    @Transaction
    @Query("SELECT * FROM project WHERE id = :projectId")
    fun getProjectWithCoordinatesFlow(projectId: Long): Flow<ProjectWithCoordinates?>

    @Transaction
    @Query("SELECT * FROM project ORDER BY createdAt DESC")
    fun getAllProjectsWithCoordinates(): Flow<List<ProjectWithCoordinates>>

    @Transaction
    @Query("SELECT * FROM project WHERE id = :projectId")
    suspend fun getProjectWithDetails(projectId: Long): ProjectWithDetails?

    @Transaction
    @Query("SELECT * FROM project WHERE id = :projectId")
    fun getProjectWithDetailsFlow(projectId: Long): Flow<ProjectWithDetails?>

    @Transaction
    @Query("SELECT * FROM project ORDER BY createdAt DESC")
    fun getAllProjectsWithDetails(): Flow<List<ProjectWithDetails>>
}
