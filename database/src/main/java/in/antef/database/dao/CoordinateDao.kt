package `in`.antef.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Delete
import `in`.antef.database.entity.CoordinateEntity
import `in`.antef.database.entity.CoordinateWithMedias
import kotlinx.coroutines.flow.Flow

@Dao
interface CoordinateDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCoordinate(entity: CoordinateEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCoordinates(entities: List<CoordinateEntity>): List<Long>

    @Query("SELECT * FROM coordinate WHERE projectId = :projectId ORDER BY createdAt DESC")
    fun getCoordinatesByProjectId(projectId: Long): Flow<List<CoordinateEntity>>

    @Query("SELECT * FROM coordinate WHERE id = :id")
    suspend fun getCoordinateById(id: Long): CoordinateEntity?

    @Query("SELECT * FROM coordinate WHERE id = :id")
    fun getCoordinateByIdFlow(id: Long): Flow<CoordinateEntity?>

    @Update
    suspend fun updateCoordinate(entity: CoordinateEntity)

    @Delete
    suspend fun deleteCoordinateEntity(entity: CoordinateEntity)

    @Query("DELETE FROM coordinate WHERE id = :id")
    suspend fun deleteCoordinate(id: Long)

    @Query("DELETE FROM coordinate WHERE projectId = :projectId")
    suspend fun deleteCoordinatesByProjectId(projectId: Long)

    @Transaction
    @Query("SELECT * FROM coordinate WHERE id = :coordinateId")
    suspend fun getCoordinateWithMedias(coordinateId: Long): CoordinateWithMedias?

    @Transaction
    @Query("SELECT * FROM coordinate WHERE id = :coordinateId")
    fun getCoordinateWithMediasFlow(coordinateId: Long): Flow<CoordinateWithMedias?>

    @Transaction
    @Query("SELECT * FROM coordinate WHERE projectId = :projectId ORDER BY createdAt DESC")
    fun getCoordinatesWithMediasByProjectId(projectId: Long): Flow<List<CoordinateWithMedias>>
}