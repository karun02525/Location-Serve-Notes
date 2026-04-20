package `in`.antef.geonote.ui.screens.codinates

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import `in`.antef.geonote.R
import `in`.antef.geonote.domain.model.MediaType
import `in`.antef.geonote.domain.model.UploadingMedia
import `in`.antef.geonote.domain.model.getFormattedTime
import `in`.antef.geonote.ui.components.TextMedium
import `in`.antef.geonote.ui.theme.SEARCH_PLACEHOLDER_COLOR
import `in`.antef.geonote.utils.VideoThumbnailAndDuration

@Composable
fun TabPhotosGrid(
    media: List<UploadingMedia>,
    onDeleteMedia: (UploadingMedia) -> Unit,
    onPreviewMedia: (UploadingMedia) -> Unit,
) {
    if (media.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            TextMedium(
                text = "No photos/Videos added yet",
                color = Color.Gray,
            )
        }
        return
    }

    val groupedPhotos = media.groupBy { photo ->
        getFormattedTime(photo.createdAt)
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(9.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        groupedPhotos.forEach { (date, photosInGroup) ->
            item(span = { GridItemSpan(maxLineSpan) }) {
                TextMedium(
                    text = date,
                    color = SEARCH_PLACEHOLDER_COLOR,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, top = 16.dp, bottom = 8.dp)
                )
            }

            // Photos/videos in this date group
            items(photosInGroup) { item ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxSize()
                            .background(Color.LightGray, RoundedCornerShape(8.dp))
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onPreviewMedia(item) }
                    ) {
                        when (item.mediaType) {
                            MediaType.VIDEO -> {
                                VideoThumbnailAndDuration(
                                    videoUrl = item.path,
                                    modifier = Modifier
                                        .fillMaxSize()
                                )
                            }
                            MediaType.PHOTO -> {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(item.path)
                                        .crossfade(true)
                                        .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
                                        .diskCachePolicy(coil.request.CachePolicy.ENABLED)    // Enable disk cache (default)
                                        .build(),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                    error = painterResource(id = R.drawable.ic_launcher_foreground),
                                    placeholder = painterResource(id = R.drawable.ic_launcher_foreground)
                                )
                            }
                            else -> {}
                        }
                    }
                    Icon(
                        painter = painterResource(id = R.drawable.ic_cross),
                        contentDescription = "Delete",
                        tint = Color.Unspecified,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .clickable { onDeleteMedia(item) }
                            .size(24.dp)
                    )
                }
            }
        }
    }
}
