package `in`.antef.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "media",
    foreignKeys = [
        ForeignKey(
            entity = CoordinateEntity::class,
            parentColumns = ["id"],
            childColumns = ["coordinateId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class MediaEntity(
    @PrimaryKey val id: String,
    val path: String, // Always holds local URI (before and after upload)
    val coordinateId: Long, // Reference to the parent coordinate
    val createdAt: Long = System.currentTimeMillis(),
    val status: Boolean = false // false: not uploaded, true: uploaded
)