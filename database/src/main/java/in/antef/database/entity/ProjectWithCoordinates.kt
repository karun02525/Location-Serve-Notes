package `in`.antef.database.entity

import androidx.room.Embedded
import androidx.room.Relation

data class ProjectWithCoordinates(
    @Embedded
    val project: ProjectEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "projectId"
    )
    val coordinates: List<CoordinateEntity>
)