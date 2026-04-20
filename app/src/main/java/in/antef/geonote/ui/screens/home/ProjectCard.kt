import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.antef.geonote.R
import `in`.antef.geonote.domain.model.ProjectModel
import `in`.antef.geonote.ui.components.TextMedium
import `in`.antef.geonote.ui.components.TextRegular
import `in`.antef.geonote.ui.theme.BORDER_COLOR
import `in`.antef.geonote.ui.theme.CARD_BORDER_COLOR
import `in`.antef.geonote.ui.theme.TERTIARY
import `in`.antef.geonote.ui.theme.TEXT_COLOR
import `in`.antef.geonote.ui.theme.TEXT_DARK_COLOR

@Composable
fun ProductItem(
    project: ProjectModel,
    onMenuClick: (model: ProjectModel) -> Unit,
    onCardClick: (projectId: Long) -> Unit,
) {
    Column(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(8.dp))
            .border(1.dp, BORDER_COLOR, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .clickable { onCardClick(project.projectId) }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextMedium(text = project.title)
            IconButton(
                onClick = { onMenuClick(project) },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More options"
                )
            }
        }

        if (project.description.isNotEmpty()) {
            TextRegular(
                modifier = Modifier.padding(top = 4.dp),
                text = project.description,
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
            StatItem(
                icon = R.drawable.ic_loc,
                count = project.coordinateCount
            )
            Spacer(Modifier.weight(1f))
            TextRegular(
                text = project.createdAt,
                fontSize = 12.sp,
                color = TEXT_DARK_COLOR,
            )
        }
    }
}


@Composable
fun StatItem(icon: Int, count: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .height(25.dp)
            .width(45.dp)
            .clip(RoundedCornerShape(5.dp))
            .border(1.dp, Color.Black.copy(0.30f), RoundedCornerShape(5.dp))
            .background(Color(0xFFFBF1E4))
            .padding(1.dp)
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = TERTIARY,
            modifier = Modifier.size(15.dp)
        )
        TextRegular(
            text = ": $count",
            fontSize = 12.sp,
            color = Color.Black.copy(0.60f),
            modifier = Modifier.padding(start = 4.dp, end = 4.dp)
        )
    }
}
