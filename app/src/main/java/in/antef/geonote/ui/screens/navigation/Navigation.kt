package `in`.antef.geonote.ui.screens.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.google.android.gms.maps.model.LatLng
import `in`.antef.geonote.ui.screens.camera.CameraScreen
import `in`.antef.geonote.ui.screens.camera.PhotoPreviewScreen
import `in`.antef.geonote.ui.screens.codinates.CoordinatesScreen
import `in`.antef.geonote.ui.screens.data.DataPageScreen
import `in`.antef.geonote.ui.screens.home.HomeScreen
import `in`.antef.geonote.ui.screens.map.GoogleMapScreen
import `in`.antef.geonote.ui.screens.recording.RecordingScreen
import `in`.antef.geonote.ui.screens.videoplayer.VideoPlayer
import kotlinx.serialization.Serializable


@Composable
fun Navigation() {
    val backStack = remember { mutableStateListOf<Any>(Navigation.HomeAction) }
    NavDisplay(
        backStack = backStack,
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<Navigation.HomeAction> {
                HomeScreen(backStack)
            }

            entry<Navigation.DataAction> { key ->
                DataPageScreen(
                    navStack = backStack,
                    projectId = key.projectId
                )
            }
            entry<Navigation.GoogleMapAction> { key ->
                GoogleMapScreen(
                    navStack = backStack,
                    projectId = key.projectId
                )
            }
            entry<Navigation.CoordinatesAction> { key ->
                CoordinatesScreen(
                    navStack = backStack,
                    coordinateId = key.coordinateId
                )
            }
            entry<Navigation.CameraAction> { key ->
                CameraScreen(coordinateId = key.coordinateId, latitude = key.latitude, longitude = key.longitude) {
                    backStack.removeLastOrNull()
                }
            }
            entry<Navigation.RecordingAction> { key ->
                RecordingScreen(coordinateId = key.coordinateId,latitude = key.latitude, longitude = key.longitude) {
                    backStack.removeLastOrNull()
                }
            }
            entry<Navigation.VideoPlayerAction> { key ->
                VideoPlayer(url = key.url) {
                    backStack.removeLastOrNull()
                }
            }
            entry<Navigation.PhotoPreviewAction> { key ->
                PhotoPreviewScreen(url = key.url) {
                    backStack.removeLastOrNull()
                }
            }
        })
}


sealed interface Navigation {

    @Serializable
    data object HomeAction : Navigation

    @Serializable
    data class DataAction(val projectId: Long) : Navigation

    @Serializable
    data class GoogleMapAction(val projectId: Long) : Navigation

    @Serializable
    data class CoordinatesAction(val coordinateId: Long) : Navigation

    @Serializable
    data class CameraAction(val coordinateId: Long, val latitude: Double, val longitude: Double) : Navigation

    @Serializable
    data class RecordingAction(val coordinateId: Long, val latitude: Double, val longitude: Double) : Navigation

    @Serializable
    data class VideoPlayerAction(val url: String) : Navigation

    @Serializable
    data class PhotoPreviewAction(val url: String) : Navigation

}


