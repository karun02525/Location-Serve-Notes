package `in`.antef.geonote.ui.screens.codinates

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import `in`.antef.geonote.R
import `in`.antef.geonote.domain.model.MediaType
import `in`.antef.geonote.domain.model.UploadingMedia
import `in`.antef.geonote.ui.components.CircleBorderCounter
import `in`.antef.geonote.ui.components.CircleCounter
import `in`.antef.geonote.ui.components.CustomIcon
import `in`.antef.geonote.ui.components.TextRegular
import `in`.antef.geonote.ui.theme.TAB_BG_COLOR
import `in`.antef.geonote.ui.theme.TERTIARY

@Composable
fun PhotosAndAudioTabs(
    media: List<UploadingMedia>,
    onDeleteMedia: (UploadingMedia) -> Unit,
    onPreviewMedia: (UploadingMedia) -> Unit,
) {
    val selectedTabIndex = remember { mutableIntStateOf(0) }

    Column {
        TabRow(
            selectedTabIndex = selectedTabIndex.intValue,
            containerColor = Color.White,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex.intValue]),
                    height = 2.dp,
                    color = TERTIARY
                )
            },
        ) {
            Tab(
                modifier = Modifier.background(
                    color = if (selectedTabIndex.intValue == 0) TAB_BG_COLOR else Color.White,
                ),
                selected = selectedTabIndex.intValue == 0,
                onClick = { selectedTabIndex.intValue = 0 },
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CustomIcon(icon = R.drawable.ic_photo)
                        Spacer(modifier = Modifier.width(8.dp))
                        TextRegular(text = "Album")
                        val photos = media.filter { it.mediaType != MediaType.AUDIO }
                        if(selectedTabIndex.intValue == 1) {
                            Spacer(modifier = Modifier.width(8.dp))
                            CircleBorderCounter(count = photos.size)
                        }else{
                            Spacer(modifier = Modifier.width(8.dp))
                            CircleCounter(count = photos.size)
                        }
                    }
                }
            )
            Tab(
                modifier = Modifier.background(
                    color = if (selectedTabIndex.intValue == 1) TAB_BG_COLOR else Color.White,
                ),
                selected = selectedTabIndex.intValue == 1,
                onClick = { selectedTabIndex.intValue = 1 },
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.background(
                            color = if (selectedTabIndex.intValue == 1) TAB_BG_COLOR else Color.White,
                        )
                    ) {
                        CustomIcon(icon = R.drawable.ic_music)
                        Spacer(modifier = Modifier.width(8.dp))
                        TextRegular(text = "Audio")
                        val audios = media.filter { it.mediaType == MediaType.AUDIO }
                        if(selectedTabIndex.intValue == 0) {
                            Spacer(modifier = Modifier.width(8.dp))
                            CircleBorderCounter(count = audios.size)
                        }else{
                            Spacer(modifier = Modifier.width(8.dp))
                            CircleCounter(count = audios.size)
                        }
                    }
                }
            )
        }

        when (selectedTabIndex.intValue) {
            0 -> TabPhotosGrid(media.filter { it.mediaType != MediaType.AUDIO }, onDeleteMedia,onPreviewMedia)
            1 -> TabAudioList(media.filter { it.mediaType == MediaType.AUDIO },onDeleteMedia)
            else -> {}
        }
    }
}