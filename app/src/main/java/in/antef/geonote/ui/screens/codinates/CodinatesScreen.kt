package `in`.antef.geonote.ui.screens.codinates

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import `in`.antef.geonote.domain.model.MediaType
import `in`.antef.geonote.domain.model.UploadingMedia
import `in`.antef.geonote.ui.components.CustomAlertDialog
import `in`.antef.geonote.ui.components.CustomBottomSheet
import `in`.antef.geonote.ui.components.TextMedium
import `in`.antef.geonote.ui.components.TextRegular
import `in`.antef.geonote.ui.screens.navigation.Navigation
import `in`.antef.geonote.ui.theme.BG_COLOR
import `in`.antef.geonote.ui.theme.BORDER_COLOR
import `in`.antef.geonote.ui.theme.TEXT_DARK_COLOR
import `in`.antef.geonote.ui.viewmodel.CoordinatesViewModel
import org.koin.androidx.compose.koinViewModel

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun CoordinatesScreen(
    navStack: MutableList<Any> = mutableListOf(),
    coordinateId: Long = 4L,
    viewModel: CoordinatesViewModel = koinViewModel(),
) {

    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val editSheet = remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var media by remember { mutableStateOf<UploadingMedia?>(null) }

    LifecycleEventEffect(Lifecycle.Event.ON_START) {
        viewModel.getCoordinate(coordinateId)
        viewModel.getAllMedia(coordinateId)
    }

    Box(
        modifier = Modifier
            .background(BG_COLOR)
            .fillMaxSize(),
    ) {
        Column {
            CustomTopAppBar(
                onBackClick = {
                    navStack.removeLastOrNull()
                },
                onEditClick = {
                    editSheet.value = true
                })


            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp)
            ) {
                TextMedium(
                    text = uiState.title,
                    fontSize = 24.sp,
                )
                Spacer(modifier = Modifier.height(4.dp))
                TextRegular(
                    text = "Location coordinates : ${uiState.coordinate.latitude}, ${uiState.coordinate.longitude}",
                    color = TEXT_DARK_COLOR
                )
                Spacer(modifier = Modifier.height(4.dp))
                TextRegular(
                    text = uiState.createdAt,
                    color = TEXT_DARK_COLOR,
                    fontSize = 12.sp
                )
            }


            Spacer(modifier = Modifier.height(16.dp))
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .border(1.dp, BORDER_COLOR, shape = RoundedCornerShape(8.dp))
                    .background(Color.White, shape = RoundedCornerShape(8.dp))
                    .padding(16.dp)
            ) {
                TextRegular(
                    text = "Description",
                    fontSize = 16.sp,
                )
                Spacer(modifier = Modifier.height(4.dp))
                TextRegular(
                    text = uiState.description,
                    fontSize = 16.sp,
                    color = TEXT_DARK_COLOR,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            PhotosAndAudioTabs(
                uiState.media,
                onDeleteMedia = { selectedMedia ->
                    media = selectedMedia
                    showDeleteDialog = true
                },
                onPreviewMedia = {
                    if (it.mediaType == MediaType.PHOTO) {
                        navStack.add(Navigation.PhotoPreviewAction(it.path))
                    } else if (it.mediaType == MediaType.VIDEO) {
                        navStack.add(Navigation.VideoPlayerAction(it.path))
                    }
                }
            )
        }

        ActionCameraOrMic(
            Modifier.align(Alignment.BottomCenter),
            onClickCamera = {
                navStack.add(
                    Navigation.CameraAction(
                        coordinateId,
                        uiState.coordinate.latitude,
                        uiState.coordinate.longitude
                    )
                )
            },
            onClickMic = {
                navStack.add(Navigation.RecordingAction(coordinateId,
                    uiState.coordinate.latitude,
                    uiState.coordinate.longitude))
            })

        CustomBottomSheet(
            header = "Update Details",
            placeholder1 = "Name",
            isPoint = true,
            title = uiState.title,
            description = uiState.description,
            showBottomSheet = editSheet.value,
            onDismiss = { editSheet.value = false },
            onCreateClick = { title, description ->
                viewModel.updateCoordinate(coordinateId, title, description)
                editSheet.value = false
                Toast.makeText(context, "Coordinate updated", Toast.LENGTH_SHORT).show()
            }
        )

        CustomAlertDialog(
            showDialog = showDeleteDialog,
            title = "Are you sure?",
            message = "Do you want to delete this item?",
            onConfirm = {
                showDeleteDialog = false
                media?.let { viewModel.deletePhoto(it) }
            },
            onDismiss = {
                showDeleteDialog = false
            }
        )

    }
}