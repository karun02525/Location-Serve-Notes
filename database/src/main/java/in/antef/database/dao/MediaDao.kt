package `in`.antef.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import `in`.antef.database.entity.MediaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMediaData(entity: MediaEntity): Long

    @Query("SELECT * FROM media WHERE status = 0")
    suspend fun getPendingMedia(): List<MediaEntity>

    @Update
    suspend fun updateMediaData(entity: MediaEntity): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhotos(entities: List<MediaEntity>): List<Long>

    @Query("SELECT * FROM media WHERE coordinateId = :coordinateId ORDER BY createdAt DESC")
    fun getPhotosByCoordinateId(coordinateId: Long): Flow<List<MediaEntity>>

    @Query("SELECT * FROM media WHERE id = :id")
    suspend fun getPhotoById(id: Long): MediaEntity?

    @Query("DELETE FROM media WHERE id = :id")
    suspend fun deleteMediaById(id: String)

    @Query("DELETE FROM media WHERE coordinateId = :coordinateId")
    suspend fun deletePhotosByCoordinateId(coordinateId: Long)
}