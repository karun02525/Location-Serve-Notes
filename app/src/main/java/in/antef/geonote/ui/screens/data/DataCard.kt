import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.antef.geonote.R
import `in`.antef.geonote.domain.model.Coordinate
import `in`.antef.geonote.domain.model.MediaType
import `in`.antef.geonote.ui.components.TextMedium
import `in`.antef.geonote.ui.components.TextRegular
import `in`.antef.geonote.ui.theme.BORDER_COLOR
import `in`.antef.geonote.ui.theme.TEXT_DARK_COLOR

@Composable
fun DataItem(
    model: Coordinate,
    onMenuClick: (model: Coordinate) -> Unit,
    onCardClick: (id: Long) -> Unit,
) {
    Column(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(8.dp))
            .border(1.dp, BORDER_COLOR, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .clickable { onCardClick(model.id) }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextMedium(text = model.title)
            IconButton(
                onClick = { onMenuClick(model) },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More options"
                )
            }
        }

        TextRegular(
            modifier = Modifier.padding(top = 4.dp),
            text = "Location Coordinate: ${model.latitude},${model.longitude}",
            fontSize = 12.sp,
            color = TEXT_DARK_COLOR,
        )

        if (model.description.isNotEmpty()) {
            TextRegular(
                modifier = Modifier.padding(top = 4.dp),
                text = model.description,
                fontSize = 12.sp,
                color = TEXT_DARK_COLOR,
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val photos = model.media.filter { it.mediaType != MediaType.AUDIO }
            val audios = model.media.filter { it.mediaType == MediaType.AUDIO }
            StatItem(
                icon = R.drawable.ic_place,
                count = photos.size
            )
            Spacer(Modifier.width(8.dp))
            StatItem(
                icon = R.drawable.ic_mic,
                count = audios.size
            )
            Spacer(Modifier.weight(1f))
            TextRegular(
                text = model.createdAt,
                fontSize = 12.sp,
                color = TEXT_DARK_COLOR,
            )
        }
    }
}
