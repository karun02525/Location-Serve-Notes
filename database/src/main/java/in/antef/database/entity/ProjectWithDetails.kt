package `in`.antef.database.entity

import androidx.room.Embedded
import androidx.room.Relation

data class ProjectWithDetails(
    @Embedded
    val project: ProjectEntity,

    @Relation(
        entity = CoordinateEntity::class,
        parentColumn = "id",
        entityColumn = "projectId"
    )
    val coordinatesWithMedias: List<CoordinateWithMedias>
)