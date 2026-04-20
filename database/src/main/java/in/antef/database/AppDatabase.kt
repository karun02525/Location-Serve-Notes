package `in`.antef.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import `in`.antef.database.dao.CoordinateDao
import `in`.antef.database.dao.ProjectDao
import `in`.antef.database.dao.MediaDao
import `in`.antef.database.entity.CoordinateEntity
import `in`.antef.database.entity.ProjectEntity
import `in`.antef.database.entity.MediaEntity


@Database(
    entities = [ProjectEntity::class, CoordinateEntity::class, MediaEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(StringListConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun coordinateDao(): CoordinateDao
    abstract fun photoDao(): MediaDao

    companion object {
        fun create(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "geo_notes_database"
            ).build()
        }
    }
}