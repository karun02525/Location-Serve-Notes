package `in`.antef.database.entity

import androidx.room.Embedded
import androidx.room.Relation

data class CoordinateWithMedias(
    @Embedded
    val coordinate: CoordinateEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "coordinateId"
    )
    val paths: List<MediaEntity>,
)