package `in`.antef.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import `in`.antef.database.StringListConverter

@Entity(tableName = "coordinate")
data class CoordinateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val projectId: Long, // Reference to the parent project
    val createdAt: String = getCurrentDateTime()
)